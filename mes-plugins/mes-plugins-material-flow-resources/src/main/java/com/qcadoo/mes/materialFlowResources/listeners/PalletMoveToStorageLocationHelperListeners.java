package com.qcadoo.mes.materialFlowResources.listeners;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.materialFlowResources.service.ResourceCorrectionService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class PalletMoveToStorageLocationHelperListeners {

    private final static String L_PALLET_NUMBER = "palletNumber";

    public static final String L_PALLET_STORAGE_STATE_DTOS = "palletStorageStateDtos";

    public static final String L_NEW_STORAGE_LOCATION = "newStorageLocation";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ResourceCorrectionService resourceCorrectionService;

    public void movePallets(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity helper = form.getPersistedEntityWithIncludedFormValues();
        CheckBoxComponent generated = (CheckBoxComponent) view.getComponentByReference("generated");
        if (!validateRequiredFields(view)) {
            generated.setChecked(false);
            return;
        }
        List<Entity> dtos = helper.getHasManyField(L_PALLET_STORAGE_STATE_DTOS);

        DataDefinition resourceDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);
        for (Entity dto : dtos) {
            List<Entity> resources = resourceDD.find()
                    .createAlias(ResourceFields.PALLET_NUMBER, ResourceFields.PALLET_NUMBER, JoinType.INNER)
                    .add(SearchRestrictions.eq(ResourceFields.PALLET_NUMBER + ".number", dto.getStringField(L_PALLET_NUMBER)))
                    .list().getEntities();
            Entity newStorageLocation = dto.getBelongsToField(L_NEW_STORAGE_LOCATION);
            String previousLocationNumber = dto.getStringField("storageLocationNumber");
            List<String> resourcesOnDifferentLocations = Lists.newArrayList();
            for (Entity resource : resources) {
                Entity oldStorageLocation = resource.getBelongsToField(ResourceFields.STORAGE_LOCATION);
                if (newStorageLocation != null
                        && (oldStorageLocation == null || !oldStorageLocation.getId().equals(newStorageLocation.getId()))) {
                    if (previousLocationNumber != null && oldStorageLocation != null
                            && !oldStorageLocation.getStringField(StorageLocationFields.NUMBER).equals(previousLocationNumber)) {
                        resourcesOnDifferentLocations.add(resource.getStringField(ResourceFields.NUMBER));
                    }
                    resource.setField(ResourceFields.STORAGE_LOCATION, newStorageLocation);
                    resource.setField(ResourceFields.VALIDATE_PALLET, false);
                    resourceCorrectionService.createCorrectionForResource(resource);
                }
            }
            if (!resourcesOnDifferentLocations.isEmpty()) {
                view.addMessage("materialFlowResources.palletMoveToStorageLocation.info.resourcesInDifferentLocations",
                        ComponentState.MessageType.INFO, false,
                        resourcesOnDifferentLocations.stream().collect(Collectors.joining(", ")),
                        dto.getStringField(L_PALLET_NUMBER), previousLocationNumber);
            }
        }
        view.addMessage("materialFlowResources.palletMoveToStorageLocation.success", ComponentState.MessageType.SUCCESS);
        generated.setChecked(true);
    }

    private boolean validateRequiredFields(final ViewDefinitionState view) {
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference(L_PALLET_STORAGE_STATE_DTOS);
        boolean isValid = true;
        for (FormComponent form : adl.getFormComponents()) {
            LookupComponent newStorageLocation = (LookupComponent) form.findFieldComponentByName(L_NEW_STORAGE_LOCATION);
            if (newStorageLocation.getFieldValue() == null) {
                newStorageLocation.addMessage("qcadooView.validate.field.error.missing", ComponentState.MessageType.FAILURE);
                isValid = false;
            }
        }
        return isValid;
    }

}
