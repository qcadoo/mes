package com.qcadoo.mes.materialFlowResources.listeners;

import com.qcadoo.mes.materialFlowResources.hooks.StorageLocationsDetailsHooks;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StorageLocationsDetailsListeners {

    @Autowired
    private StorageLocationsDetailsHooks storageLocationsDetailsHooks;

    public void updateFields(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        storageLocationsDetailsHooks.onBeforeRender(view);
    }

}
