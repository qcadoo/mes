package com.qcadoo.mes.operationalTasksForOrders.hooks;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class OperationDurationDetailsInOrderDetailsHooksOTFO {

    public void disabledCreateButton(final ViewDefinitionState viewDefinitionState) {
        WindowComponent window = (WindowComponent) viewDefinitionState.getComponentByReference("window");
        RibbonActionItem createOperationalTasks = window.getRibbon().getGroupByName("operationalTasks")
                .getItemByName("createOperationalTasks");
        if (isGenerated(viewDefinitionState) && orderHasCorrectState(viewDefinitionState)) {
            createOperationalTasks.setEnabled(true);
        } else {
            createOperationalTasks.setEnabled(false);
        }
        createOperationalTasks.requestUpdate(true);
    }

    private boolean isGenerated(final ViewDefinitionState viewDefinitionState) {
        FieldComponent generatedEndDate = (FieldComponent) viewDefinitionState.getComponentByReference("generatedEndDate");
        return !StringUtils.isEmpty(generatedEndDate.getFieldValue().toString());
    }

    private boolean orderHasCorrectState(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity order = form.getEntity().getDataDefinition().get(form.getEntityId());
        OrderState orderState = OrderState.parseString(order.getStringField(OrderFields.STATE));
        return (orderState.equals(OrderState.PENDING) || orderState.equals(OrderState.ACCEPTED)
                || orderState.equals(OrderState.IN_PROGRESS) || orderState.equals(OrderState.INTERRUPTED));
    }
}
