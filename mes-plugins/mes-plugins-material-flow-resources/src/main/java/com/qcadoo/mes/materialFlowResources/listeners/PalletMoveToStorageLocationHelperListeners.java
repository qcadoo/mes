package com.qcadoo.mes.materialFlowResources.listeners;

import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlowResources.PalletValidatorService;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PalletStorageStateDtoFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.materialFlowResources.service.ResourceCorrectionService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.qcadoo.model.api.search.SearchRestrictions.eq;

@Service
public class PalletMoveToStorageLocationHelperListeners {

    private static final String L_PALLET_STORAGE_STATE_DTOS = "palletStorageStateDtos";

    private static final String L_GENERATED = "generated";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ResourceCorrectionService resourceCorrectionService;

    @Autowired
    private PalletValidatorService palletValidatorService;

    private SearchCriterion typeOfPalletCriterion(final Entity palletStorageStateDto) {
        String typeOfPallet = palletStorageStateDto.getStringField(PalletStorageStateDtoFields.TYPE_OF_PALLET);

        if (StringUtils.isBlank(typeOfPallet)) {
            return SearchRestrictions.isNull(ResourceFields.TYPE_OF_PALLET);
        } else {
            return SearchRestrictions.eq(ResourceFields.TYPE_OF_PALLET, typeOfPallet);
        }
    }

    private SearchCriterion storageLocationCriterion(final Entity palletStorageStateDto) {
        String storageLocationNumber = palletStorageStateDto.getStringField(PalletStorageStateDtoFields.STORAGE_LOCATION_NUMBER);

        if (StringUtils.isBlank(storageLocationNumber)) {
            return SearchRestrictions.isNull(ResourceFields.STORAGE_LOCATION + ".number");
        } else {
            return SearchRestrictions.eq(ResourceFields.STORAGE_LOCATION + ".number", storageLocationNumber);
        }
    }

    public void movePallets(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent palletMoveToStorageLocationHelperForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity palletMoveToStorageLocationHelper = palletMoveToStorageLocationHelperForm.getPersistedEntityWithIncludedFormValues();

        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view.getComponentByReference(L_GENERATED);

        List<Entity> palletStorageStateDtos = palletMoveToStorageLocationHelper.getHasManyField(L_PALLET_STORAGE_STATE_DTOS);

        if (!validateRequiredFields(view, palletStorageStateDtos)) {
            generatedCheckBox.setChecked(false);

            return;
        }

        DataDefinition resourceDD = getResourceDD();

        for (Entity palletStorageStateDto : palletStorageStateDtos) {
            final List<Entity> resources = resourceDD
                    .find()
                    .createAlias(ResourceFields.PALLET_NUMBER, ResourceFields.PALLET_NUMBER, JoinType.INNER)
                    .createAlias(ResourceFields.LOCATION, ResourceFields.LOCATION, JoinType.INNER)
                    .createAlias(ResourceFields.STORAGE_LOCATION, ResourceFields.STORAGE_LOCATION, JoinType.LEFT)
                    .add(eq(ResourceFields.PALLET_NUMBER + ".number",
                            palletStorageStateDto.getStringField(PalletStorageStateDtoFields.PALLET_NUMBER)))
                    .add(eq(ResourceFields.LOCATION + ".number", palletStorageStateDto.getStringField(PalletStorageStateDtoFields.LOCATION_NUMBER)))
                    .add(storageLocationCriterion(palletStorageStateDto)).add(typeOfPalletCriterion(palletStorageStateDto)).list().getEntities();

            Entity newStorageLocation = palletStorageStateDto.getBelongsToField(PalletStorageStateDtoFields.NEW_STORAGE_LOCATION);
            String previousLocationNumber = palletStorageStateDto.getStringField(PalletStorageStateDtoFields.STORAGE_LOCATION_NUMBER);

            List<String> resourcesOnDifferentLocations = Lists.newArrayList();

            for (Entity resource : resources) {
                Entity oldStorageLocation = resource.getBelongsToField(ResourceFields.STORAGE_LOCATION);

                if (Objects.nonNull(newStorageLocation)
                        && (Objects.isNull(oldStorageLocation) || !oldStorageLocation.getId().equals(newStorageLocation.getId()))) {
                    if (Objects.nonNull(previousLocationNumber) && Objects.nonNull(oldStorageLocation)
                            && !oldStorageLocation.getStringField(StorageLocationFields.NUMBER).equals(previousLocationNumber)) {
                        resourcesOnDifferentLocations.add(resource.getStringField(ResourceFields.NUMBER));
                    }

                    resource.setField(ResourceFields.STORAGE_LOCATION, newStorageLocation);
                    resource.setField(ResourceFields.VALIDATE_PALLET, false);

                    resourceCorrectionService.createCorrectionForResource(resource, false);
                }
            }

            if (!resourcesOnDifferentLocations.isEmpty()) {
                view.addMessage("materialFlowResources.palletMoveToStorageLocation.info.resourcesInDifferentLocations",
                        ComponentState.MessageType.INFO, false,
                        String.join(", ", resourcesOnDifferentLocations),
                        palletStorageStateDto.getStringField(PalletStorageStateDtoFields.PALLET_NUMBER), previousLocationNumber);
            }
        }

        view.addMessage("materialFlowResources.palletMoveToStorageLocation.success", ComponentState.MessageType.SUCCESS);

        generatedCheckBox.setChecked(true);
    }

    private boolean validateRequiredFields(final ViewDefinitionState view, final List<Entity> dtos) {
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference(L_PALLET_STORAGE_STATE_DTOS);

        boolean isValid = true;

        for (FormComponent formComponent : adl.getFormComponents()) {
            LookupComponent newStorageLocation = (LookupComponent) formComponent.findFieldComponentByName(PalletStorageStateDtoFields.NEW_STORAGE_LOCATION);

            if (Objects.isNull(newStorageLocation.getFieldValue())) {
                newStorageLocation.addMessage("qcadooView.validate.field.error.missing", ComponentState.MessageType.FAILURE);

                isValid = false;
            }

            Entity newLocation = newStorageLocation.getEntity();

            if (palletValidatorService.checkMaximumNumberOfPallets(newLocation, getNewPalletsCountInStorageLocation(newLocation, dtos))) {
                newStorageLocation.addMessage("materialFlowResources.palletMoveToStorageLocation.error.tooManyPallets",
                        ComponentState.MessageType.FAILURE);

                isValid = false;
            }
        }

        return isValid;
    }

    private long getNewPalletsCountInStorageLocation(final Entity newStorageLocation, final List<Entity> palletStorageStateDtos) {
        return palletStorageStateDtos
                .stream()
                .filter(palletStorageStateDto -> Objects.nonNull(palletStorageStateDto.getBelongsToField(PalletStorageStateDtoFields.NEW_STORAGE_LOCATION))
                        && palletStorageStateDto.getBelongsToField(PalletStorageStateDtoFields.NEW_STORAGE_LOCATION).getId().equals(newStorageLocation.getId())).count();
    }

    private DataDefinition getResourceDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);
    }

}
