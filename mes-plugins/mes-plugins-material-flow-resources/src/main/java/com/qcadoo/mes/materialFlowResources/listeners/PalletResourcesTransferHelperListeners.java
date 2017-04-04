package com.qcadoo.mes.materialFlowResources.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.service.ResourceCorrectionService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class PalletResourcesTransferHelperListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ResourceCorrectionService resourceCorrectionService;

    public void transferResources(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        // TODO make me usable
    }

    public void onPalletNumberSelected(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        // TODO perform validation
        state.addMessage(new ErrorMessage("Error message test"));
    }

}
