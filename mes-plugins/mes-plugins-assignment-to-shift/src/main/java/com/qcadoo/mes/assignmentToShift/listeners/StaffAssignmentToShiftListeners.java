package com.qcadoo.mes.assignmentToShift.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.hooks.StaffAssignmentToShiftDetailsHooks;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class StaffAssignmentToShiftListeners {

    @Autowired
    private StaffAssignmentToShiftDetailsHooks staffAssignmentToShiftDetailsHooks;

    public void enabledFieldWhenTypeIsSpecific(final ViewDefinitionState view, final ComponentState state, String[] args) {
        staffAssignmentToShiftDetailsHooks.enabledFieldWhenTypeIsSpecific(view);
    }
}
