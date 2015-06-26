package com.qcadoo.mes.cmmsMachineParts.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class EventsListListeners {

    public void showFactoryStructure(final ViewDefinitionState view, final ComponentState state, final String args[]) {

        view.redirectTo("/page/cmmsMachineParts/factoryStructureForEvent.html", false, true);
    }
}
