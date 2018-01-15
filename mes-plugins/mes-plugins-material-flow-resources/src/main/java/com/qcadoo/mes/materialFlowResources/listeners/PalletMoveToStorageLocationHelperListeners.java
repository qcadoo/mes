package com.qcadoo.mes.materialFlowResources.listeners;

import static com.qcadoo.model.api.search.SearchRestrictions.eq;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
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

@Service
public class PalletMoveToStorageLocationHelperListeners {

    private final static String L_PALLET_NUMBER = "palletNumber";

    private static final String L_PALLET_STORAGE_STATE_DTOS = "palletStorageStateDtos";

    private static final String L_NEW_STORAGE_LOCATION = "newStorageLocation";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ResourceCorrectionService resourceCorrectionService;

    private SearchCriterion typeOfPalletCriterion(Entity entity) {
        String typeOfPallet = entity.getStringField(PalletStorageStateDtoFields.TYPE_OF_PALLET);
        if (StringUtils.isBlank(typeOfPallet)) {
            return SearchRestrictions.isNull(ResourceFields.TYPE_OF_PALLET);
        } else {
            return eq(ResourceFields.TYPE_OF_PALLET, typeOfPallet);
        }
    }

    private SearchCriterion storageLocationCriterion(Entity entity) {
        String storageLocationNumber = entity.getStringField(PalletStorageStateDtoFields.STORAGE_LOCATION_NUMBER);
        if (StringUtils.isBlank(storageLocationNumber)) {
            return SearchRestrictions.isNull(ResourceFields.STORAGE_LOCATION + ".number");
        } else {
            return eq(ResourceFields.STORAGE_LOCATION + ".number", storageLocationNumber);
        }
    }

    public void movePallets(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity helper = form.getPersistedEntityWithIncludedFormValues();
        CheckBoxComponent generated = (CheckBoxComponent) view.getComponentByReference("generated");
        List<Entity> dtos = helper.getHasManyField(L_PALLET_STORAGE_STATE_DTOS);
        if (!validateRequiredFields(view, dtos)) {
            generated.setChecked(false);
            return;
        }

        DataDefinition resourceDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);
        for (Entity dto : dtos) {
            final List<Entity> resources = resourceDD
                    .find()
                    .createAlias(ResourceFields.PALLET_NUMBER, ResourceFields.PALLET_NUMBER, JoinType.INNER)
                    .createAlias(ResourceFields.LOCATION, ResourceFields.LOCATION, JoinType.INNER)
                    .createAlias(ResourceFields.STORAGE_LOCATION, ResourceFields.STORAGE_LOCATION, JoinType.LEFT)
                    .add(eq(ResourceFields.PALLET_NUMBER + ".number",
                            dto.getStringField(PalletStorageStateDtoFields.PALLET_NUMBER)))
                    .add(eq(ResourceFields.LOCATION + ".number", dto.getStringField(PalletStorageStateDtoFields.LOCATION_NUMBER)))
                    .add(storageLocationCriterion(dto)).add(typeOfPalletCriterion(dto)).list().getEntities();
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

    private long getNewPalletsCountInStorageLocation(final Entity newStorageLocation, final List<Entity> dtos) {
        return dtos
                .stream()
                .filter(dto -> dto.getBelongsToField(L_NEW_STORAGE_LOCATION) != null
                        && dto.getBelongsToField(L_NEW_STORAGE_LOCATION).getId().equals(newStorageLocation.getId())).count();
    }

    private boolean validateRequiredFields(final ViewDefinitionState view, final List<Entity> dtos) {
        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference(L_PALLET_STORAGE_STATE_DTOS);
        boolean isValid = true;
        for (FormComponent form : adl.getFormComponents()) {
            LookupComponent newStorageLocation = (LookupComponent) form.findFieldComponentByName(L_NEW_STORAGE_LOCATION);
            if (newStorageLocation.getFieldValue() == null) {
                newStorageLocation.addMessage("qcadooView.validate.field.error.missing", ComponentState.MessageType.FAILURE);
                isValid = false;
            }
            Entity newLocation = newStorageLocation.getEntity();
            BigDecimal maxNumberOfPallets = null;
            if (newLocation != null) {
                maxNumberOfPallets = newLocation.getDecimalField(StorageLocationFields.MAXIMUM_NUMBER_OF_PALLETS);
            }

            if (maxNumberOfPallets != null) {
                BigDecimal totalPallets = BigDecimal.valueOf(getNewPalletsCountInStorageLocation(newLocation, dtos)
                        + resourceCorrectionService.getPalletsCountInStorageLocation(newLocation));
                if (totalPallets.compareTo(maxNumberOfPallets) > 0) {
                    newStorageLocation.addMessage("materialFlowResources.palletMoveToStorageLocation.error.tooManyPallets",
                            ComponentState.MessageType.FAILURE);
                    isValid = false;
                }
            }
        }
        return isValid;
    }
}
