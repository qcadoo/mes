package com.qcadoo.mes.deliveries.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ParametersListenersD {

    public void redirectToSupplyParameters(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        Long parameterId = (Long) componentState.getFieldValue();

        if (parameterId != null) {
            String url = "../page/deliveries/supplyParameters.html?context={\"form.id\":\"" + parameterId + "\"}";
            view.redirectTo(url, false, true);
        }
    }
}
