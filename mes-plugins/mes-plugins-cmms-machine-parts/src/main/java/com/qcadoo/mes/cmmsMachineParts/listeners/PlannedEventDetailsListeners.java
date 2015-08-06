package com.qcadoo.mes.cmmsMachineParts.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.hooks.EventHooks;
import com.qcadoo.mes.cmmsMachineParts.hooks.PlannedEventDetailsHooks;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class PlannedEventDetailsListeners {

    @Autowired
    private PlannedEventDetailsHooks plannedEventDetailsHooks;

    @Autowired
    private EventHooks eventHooks;

    public void toggleEnabledFromBasedOn(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        eventHooks.toggleEnabledFromBasedOn(view);
    }

    public void toggleFieldsVisible(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        plannedEventDetailsHooks.toggleFieldsVisible(view);
    }
}
