package com.qcadoo.mes.assignmentToShift.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ParametersListenersATS {

    public void redirectToAssignmentToShiftParameters(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        Long parameterId = (Long) state.getFieldValue();

        if (parameterId != null) {
            String url = "../page/assignmentToShift/assignmentToShiftParameters.html?context={\"form.id\":\"" + parameterId
                    + "\"}";
            view.redirectTo(url, false, true);
        }
    }

}
