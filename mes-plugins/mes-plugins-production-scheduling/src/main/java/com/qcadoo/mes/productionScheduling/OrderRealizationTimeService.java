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
            // realizationTime.setFieldValue(BigDecimal.valueOf(estimateRealizationTime((Long) viewDefinitionState
            // .getComponentByReference("form").getFieldValue())));
            // TODO KRNA value > max
        } else {
            realizationTime.setFieldValue(BigDecimal.ZERO);
        }
        // TODO KRNA what with product lookup ?
    }

    private int estimateRealizationTime(final Long id) {
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
        EntityTree tree = order.getTreeField("orderOperationComponents");
        EntityTreeNode node = tree.getRoot();

        int pathTime = 0;

        if ((Boolean) node.getField("useMachineNorm")) {
            node.setField("tj", "1");
            node.setField("tpz", "1");
            // TODO KRNA machines
        }
        if (node.getChildren().size() == 0) {
            // TODO KRNA empty parent
            boolean operationHasParent = true;
            Entity parent = node.getBelongsToField("parent");
            if ("01all".equals(parent.getField("countRealized"))) {
                pathTime += (((BigDecimal) order.getField("plannedQuantity")).multiply(BigDecimal.valueOf((Integer) node
                        .getField("tj")))).intValue()
                        + (Integer) node.getField("tpz")
                        + (Integer) node.getField("timeNextOperation");
            } else {
                pathTime += (((BigDecimal) order.getField("countMachine")).multiply(BigDecimal.valueOf((Integer) node
                        .getField("tj")))).intValue()
                        + (Integer) node.getField("tpz")
                        + (Integer) node.getField("timeNextOperation");
            }

            while (operationHasParent) {
                operationHasParent = false;
            }
        }

        return 1;
    }
}
