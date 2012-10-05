package com.qcadoo.mes.supplyNegotiations.listeners;

import org.springframework.stereotype.Component;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Component
public class RequestForQuotationListeners {

    public final void printRequestForQuotationProductReport(final ViewDefinitionState viewDefinitionState,
            final ComponentState state, final String[] args) {
        if (state instanceof FormComponent) {
            state.performEvent(viewDefinitionState, "save", args);

            viewDefinitionState.redirectTo(
                    "/supplyNegotiations/requestsForQuotationReport." + args[0] + "?id=" + state.getFieldValue(), true, false);
        } else {
            state.addMessage("supplyNegotiations.requestsForQuatation.report.componentFormError", MessageType.FAILURE);
        }
    }

}
