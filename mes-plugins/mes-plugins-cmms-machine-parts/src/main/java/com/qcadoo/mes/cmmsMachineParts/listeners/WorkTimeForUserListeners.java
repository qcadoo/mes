package com.qcadoo.mes.cmmsMachineParts.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class WorkTimeForUserListeners {

    public void showWorkTime(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        view.openModal(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER + "/workTimeForUserList.html");
    }
}
