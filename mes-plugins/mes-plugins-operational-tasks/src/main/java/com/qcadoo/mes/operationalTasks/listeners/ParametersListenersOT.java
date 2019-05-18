package com.qcadoo.mes.operationalTasks.listeners;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

import org.springframework.stereotype.Service;

@Service
public class ParametersListenersOT {

    public void redirectToOperationalTasksParameters(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        Long parameterId = (Long) componentState.getFieldValue();

        if (parameterId != null) {
            String url = "../page/operationalTasks/operationalTasksParameters.html?context={\"form.id\":\"" + parameterId + "\"}";
            view.redirectTo(url, false, true);
        }
    }
}
