package com.qcadoo.mes.deliveries.listeners;

import org.springframework.stereotype.Component;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Component
public class DeliveryDetailsReportListeners {

    public final void printDeliveryReport(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponent) {
            state.performEvent(viewDefinitionState, "save", args);

            viewDefinitionState.redirectTo("/deliveries/deliveryReport." + args[0] + "?id=" + state.getFieldValue(), true, false);
        } else {
            state.addMessage("deliveries.delivery.report.componentFormError", MessageType.FAILURE);
        }
    }

    public final void printOrderReport(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponent) {
            state.performEvent(viewDefinitionState, "save", args);

            viewDefinitionState.redirectTo("/deliveries/orderReport." + args[0] + "?id=" + state.getFieldValue(), true, false);
        } else {
            state.addMessage("deliveries.delivery.report.componentFormError", MessageType.FAILURE);
        }
    }
}
