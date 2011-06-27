package com.qcadoo.mes.productionScheduling;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class OrderRealizationTimeService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void changeDateFrom(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent dateTo = (FieldComponent) state;
        FieldComponent dateFrom = (FieldComponent) viewDefinitionState.getComponentByReference("dateFrom");
        if (StringUtils.hasText((String) dateTo.getFieldValue()) && !StringUtils.hasText((String) dateFrom.getFieldValue())) {
            // TODO KRNA update dateFrom
            dateFrom.setFieldValue(dateTo.getFieldValue());
        }
        // TODO KRNA value > max
    }

    public void changeDateTo(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent dateFrom = (FieldComponent) state;
        FieldComponent dateTo = (FieldComponent) viewDefinitionState.getComponentByReference("dateTo");
        if (!StringUtils.hasText((String) dateTo.getFieldValue()) && StringUtils.hasText((String) dateFrom.getFieldValue())) {
            // TODO KRNA update dateTo
            dateTo.setFieldValue(dateFrom.getFieldValue());
        }
        // TODO KRNA value > max
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
            realizationTime.setFieldValue(BigDecimal.valueOf(estimateRealizationTime((Long) viewDefinitionState
                    .getComponentByReference("form").getFieldValue())));
            // TODO KRNA value > max
        } else {
            realizationTime.setFieldValue(BigDecimal.ZERO);
        }
        // TODO KRNA what with product lookup ?
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
            for (Entity machine : operation.getHasManyField("machineInOrderOperationComponents")) {
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
