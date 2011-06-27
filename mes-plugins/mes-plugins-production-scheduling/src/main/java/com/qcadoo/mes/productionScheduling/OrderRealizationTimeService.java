package com.qcadoo.mes.productionScheduling;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class OrderRealizationTimeService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ShiftsService shiftsService;

    public void changeDateFrom(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent dateTo = (FieldComponent) state;
        FieldComponent dateFrom = (FieldComponent) viewDefinitionState.getComponentByReference("dateFrom");
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference("realizationTime");
        if (StringUtils.hasText((String) dateTo.getFieldValue()) && !StringUtils.hasText((String) dateFrom.getFieldValue())) {
            Date date = shiftsService.findDateFromForOrder(getDateFromField(dateTo.getFieldValue()),
                    Integer.valueOf((String) realizationTime.getFieldValue()));
            if (date != null) {
                dateFrom.setFieldValue(setDateToField(date));
            }
        }
    }

    public void changeDateTo(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent dateFrom = (FieldComponent) state;
        FieldComponent dateTo = (FieldComponent) viewDefinitionState.getComponentByReference("dateTo");
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference("realizationTime");
        if (!StringUtils.hasText((String) dateTo.getFieldValue()) && StringUtils.hasText((String) dateFrom.getFieldValue())) {
            Date date = shiftsService.findDateToForOrder(getDateFromField(dateFrom.getFieldValue()),
                    Integer.valueOf((String) realizationTime.getFieldValue()));
            if (date != null) {
                dateTo.setFieldValue(setDateToField(date));
            }
        }
    }

    private Object setDateToField(final Date date) {
        return new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT).format(date);
    }

    private Date getDateFromField(final Object value) {
        try {
            return new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT).parse((String) value);
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void changeRealizationTime(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }

        FieldComponent plannedQuantity = (FieldComponent) viewDefinitionState.getComponentByReference("plannedQuantity");
        FieldComponent technology = (FieldComponent) viewDefinitionState.getComponentByReference("technology");
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference("realizationTime");

        if (technology.getFieldValue() != null && StringUtils.hasText((String) plannedQuantity.getFieldValue())) {
            int time = estimateRealizationTime((Long) viewDefinitionState.getComponentByReference("form").getFieldValue());
            if (time > 99999 * 60 * 60) {
                viewDefinitionState.addMessage("orders.validate.global.error.RealizationTimeIsToLong", MessageType.INFO);
            } else {
                realizationTime.setFieldValue(time);
            }
        } else {
            realizationTime.setFieldValue(BigDecimal.ZERO);
        }
    }

    private int estimateRealizationTime(final Long id) {
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
        EntityTree tree = order.getTreeField("orderOperationComponents");
        int maxPathTime = 0;
        estimateRealizationTimeForOperation(tree.getRoot(), (BigDecimal) order.getField("plannedQuantity"), maxPathTime);
        return maxPathTime;

    }

    private void estimateRealizationTimeForOperation(final EntityTreeNode operation, final BigDecimal plannedQuantity,
            int maxPathTime) {
        int pathTime = 0;
        if ((Boolean) operation.getField("useMachineNorm")) {
            DataDefinition machineInOrderOperationComponentDD = dataDefinitionService.get("productionScheduling",
                    "machineInOrderOperationComponent");
            List<Entity> machines = machineInOrderOperationComponentDD.find()
                    .add(SearchRestrictions.belongsTo("orderOperationComponent", operation))
                    .addOrder(SearchOrders.asc("priority")).list().getEntities();
            for (Entity machine : machines) {
                if ((Boolean) machine.getField("isActive")) {
                    operation.setField("tj", machine.getField("tj"));
                    operation.setField("tpz", machine.getField("tpz"));
                    break;
                }
            }
        }
        if (operation.getChildren().size() == 0) {
            boolean operationHasParent = true;
            Entity parent = operation.getBelongsToField("parent");
            if (parent == null) {
                operationHasParent = false;
            }
            if ("01all".equals(operation.getField("countRealized"))) {
                pathTime += (plannedQuantity.multiply(BigDecimal.valueOf((Integer) operation.getField("tj")))).intValue()
                        + (Integer) operation.getField("tpz") + (Integer) operation.getField("timeNextOperation");
            } else {
                pathTime += (((BigDecimal) operation.getField("countMachine")).multiply(BigDecimal.valueOf((Integer) operation
                        .getField("tj")))).intValue()
                        + (Integer) operation.getField("tpz")
                        + (Integer) operation.getField("timeNextOperation");
            }

            while (operationHasParent) {
                if (((Integer) parent.getField("offSet")).compareTo(pathTime) < 0) {
                    parent.setField("offSet", pathTime);
                }

                if ("01all".equals(parent.getField("countRealized"))) {
                    pathTime += (plannedQuantity.multiply(BigDecimal.valueOf((Integer) parent.getField("tj")))).intValue()
                            + (Integer) parent.getField("tpz") + (Integer) parent.getField("timeNextOperation");
                } else {
                    pathTime += (((BigDecimal) parent.getField("countMachine")).multiply(BigDecimal.valueOf((Integer) parent
                            .getField("tj")))).intValue()
                            + (Integer) parent.getField("tpz")
                            + (Integer) parent.getField("timeNextOperation");
                }

                if (parent.getBelongsToField("parent") != null) {
                    operationHasParent = true;
                    parent = parent.getBelongsToField("parent");
                } else {
                    operationHasParent = false;
                    if (pathTime > maxPathTime) {
                        maxPathTime = pathTime;
                    }
                    pathTime = 0;
                }
            }
        } else {
            for (EntityTreeNode child : operation.getChildren()) {
                estimateRealizationTimeForOperation(child, plannedQuantity, maxPathTime);
            }
        }
    }
}
