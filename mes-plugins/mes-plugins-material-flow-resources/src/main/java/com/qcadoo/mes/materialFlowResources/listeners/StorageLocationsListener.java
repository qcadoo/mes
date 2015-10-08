package com.qcadoo.mes.materialFlowResources.listeners;

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service public class StorageLocationsListener {

    @Autowired private DataDefinitionService dataDefinitionService;

    public void redirectToAddManyStorageLocations(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        Entity entity = createEntity();

        String url = "../page/materialFlowResources/storageLocationsMultiAdd.html?context={\"form.id\":\""
                + entity.getId() + "\"}";
        view.openModal(url);
    }

    private Entity createEntity() {
        Entity state = getStorageLocationtDD().create();
        return state.getDataDefinition().save(state);
    }

    public DataDefinition getStorageLocationtDD() {
        return dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, "storageLocationHelper");
    }

    public void createStorageLocations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity entity = form.getPersistedEntityWithIncludedFormValues();
        entity.isActive();
    }
}
