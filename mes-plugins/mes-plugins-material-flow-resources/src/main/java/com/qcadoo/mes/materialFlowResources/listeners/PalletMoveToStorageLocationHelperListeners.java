package com.qcadoo.mes.materialFlowResources.listeners;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.service.ResourceCorrectionService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class PalletMoveToStorageLocationHelperListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ResourceCorrectionService resourceCorrectionService;

    public void movePallets(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity helper = form.getPersistedEntityWithIncludedFormValues();
        if (!validateRequiredFields(view)) {
            return;
        }
        List<Entity> dtos = helper.getHasManyField("palletStorageStateDtos");

        DataDefinition resourceDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);
        for (Entity dto : dtos) {
            List<Entity> resources = resourceDD.find()
                    .createAlias(ResourceFields.PALLET_NUMBER, ResourceFields.PALLET_NUMBER, JoinType.INNER)
                    .add(SearchRestrictions.eq(ResourceFields.PALLET_NUMBER + ".number", dto.getStringField("palletNumber")))
                    .list().getEntities();
            Entity newStorageLocation = dto.getBelongsToField("newStorageLocation");
            resources.forEach(resource -> {
                Entity oldStorageLocation = resource.getBelongsToField(ResourceFields.STORAGE_LOCATION);
                if (newStorageLocation != null
                        && (oldStorageLocation == null || (!oldStorageLocation.getId().equals(newStorageLocation.getId())))) {
                    resource.setField(ResourceFields.STORAGE_LOCATION, newStorageLocation);
                    resourceCorrectionService.createCorrectionForResource(resource);
                }
            });
        }
    }

    private boolean validateRequiredFields(final ViewDefinitionState view) {
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference("palletStorageStateDtos");
        boolean isValid = true;
        for (FormComponent form : adl.getFormComponents()) {
            LookupComponent newStorageLocation = (LookupComponent) form.findFieldComponentByName("newStorageLocation");
            if (newStorageLocation.getFieldValue() == null) {
                newStorageLocation.addMessage("qcadooView.validate.field.error.missing", ComponentState.MessageType.FAILURE);
                isValid = false;
            }
        }
        return isValid;
    }

}
