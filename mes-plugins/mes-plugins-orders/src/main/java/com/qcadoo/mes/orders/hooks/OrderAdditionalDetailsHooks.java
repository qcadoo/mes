package com.qcadoo.mes.orders.hooks;

import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderAdditionalDetailsHooks {

    @Autowired
    private OrderService orderService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        boolean disabled = false;

        Long orderId = orderForm.getEntityId();

        if (orderId != null) {
            Entity order = orderService.getOrder(orderId);

            if (order == null) {
                return;
            }

            String state = order.getStringField(OrderFields.STATE);

            if (!OrderState.PENDING.getStringValue().equals(state)) {
                disabled = true;
            }
        }

        orderForm.setFormEnabled(!disabled);
    }
}
