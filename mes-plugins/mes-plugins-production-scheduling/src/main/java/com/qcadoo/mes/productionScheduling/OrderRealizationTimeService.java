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
        int operationTime = 0;
        if (operation.getField("useMachineNorm") != null && (Boolean) operation.getField("useMachineNorm")) {
            DataDefinition machineInOrderOperationComponentDD = dataDefinitionService.get("productionScheduling",
                    "machineInOrderOperationComponent");
            List<Entity> machineComponents = machineInOrderOperationComponentDD.find()
                    .add(SearchRestrictions.belongsTo("orderOperationComponent", operation))
                    .addOrder(SearchOrders.asc("priority")).list().getEntities();
            for (Entity machineComponent : machineComponents) {
                if ((Boolean) machineComponent.getField("isActive")) {
                    operation.setField("tj", machineComponent.getField("tj"));
                    operation.setField("tpz", machineComponent.getField("tpz"));
                    operation.setField("effectiveMachine", machineComponent.getBelongsToField("machine"));
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
                operationTime = (plannedQuantity
                        .multiply(BigDecimal.valueOf(operation.getField("tj") != null ? (Integer) operation.getField("tj") : 0)))
                        .intValue()
                        + (operation.getField("tpz") != null ? (Integer) operation.getField("tpz") : Integer.valueOf(0))
                        + (operation.getField("timeNextOperation") != null ? (Integer) operation.getField("timeNextOperation")
                                : Integer.valueOf(0));
            } else {
                operationTime = ((operation.getField("countMachine") != null ? (BigDecimal) operation.getField("countMachine")
                        : BigDecimal.ZERO).multiply(BigDecimal.valueOf(operation.getField("tj") != null ? (Integer) operation
                        .getField("tj") : 0))).intValue()
                        + (operation.getField("tpz") != null ? (Integer) operation.getField("tpz") : Integer.valueOf(0))
                        + (operation.getField("timeNextOperation") != null ? (Integer) operation.getField("timeNextOperation")
                                : Integer.valueOf(0));

            }
            operation.setField("effectiveOperationRealizationTime", operationTime);
            pathTime += operationTime;

            while (operationHasParent) {
                int parentOperationTime = 0;
                if (parent.getField("operationOffSet") != null
                        && ((Integer) parent.getField("operationOffSet")).compareTo(pathTime) < 0) {
                    parent.setField("operationOffSet", pathTime);
                }

                if ("01all".equals(parent.getField("countRealized"))) {
                    parentOperationTime = (plannedQuantity
                            .multiply(BigDecimal.valueOf(parent.getField("tj") != null ? (Integer) parent.getField("tj") : 0)))
                            .intValue()
                            + (parent.getField("tpz") != null ? (Integer) parent.getField("tpz") : Integer.valueOf(0))
                            + (parent.getField("timeNextparent") != null ? (Integer) parent.getField("timeNextOperation")
                                    : Integer.valueOf(0));
                } else {
                    parentOperationTime = ((parent.getField("countMachine") != null ? (BigDecimal) parent
                            .getField("countMachine") : BigDecimal.ZERO)
                            .multiply(BigDecimal.valueOf(parent.getField("tj") != null ? (Integer) parent.getField("tj") : 0)))
                            .intValue()
                            + (parent.getField("tpz") != null ? (Integer) parent.getField("tpz") : Integer.valueOf(0))
                            + (parent.getField("timeNextOperation") != null ? (Integer) parent.getField("timeNextOperation")
                                    : Integer.valueOf(0));
                }
                parent.setField("effectiveOperationRealizationTime", parentOperationTime);
                pathTime += parentOperationTime;

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
