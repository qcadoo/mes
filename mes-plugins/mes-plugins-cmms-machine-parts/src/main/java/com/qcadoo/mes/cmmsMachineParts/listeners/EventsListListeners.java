package com.qcadoo.mes.cmmsMachineParts.listeners;

import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class EventsListListeners {

    public void newEventAction(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String args[]) {
        viewDefinitionState.redirectTo("../page/" + CmmsMachinePartsConstants.PLUGIN_IDENTIFIER + "/addNewEvent.html", false, true);
    }
}
