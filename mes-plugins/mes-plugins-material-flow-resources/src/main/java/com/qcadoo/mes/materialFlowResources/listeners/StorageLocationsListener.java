package com.qcadoo.mes.materialFlowResources.listeners;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationHelperFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class StorageLocationsListener {

    private static final String L_ZERO = "0";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void redirectToAddManyStorageLocations(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        Entity entity = createEntity();

        String url = "../page/materialFlowResources/storageLocationsMultiAdd.html?context={\"form.id\":\"" + entity.getId()
                + "\"}";
        view.openModal(url);
    }

    private Entity createEntity() {
        Entity state = getStorageLocationtDD().create();
        return state.getDataDefinition().save(state);
    }

    public DataDefinition getStorageLocationtDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, "storageLocationHelper");
    }

    public void createStorageLocations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity entity = form.getPersistedEntityWithIncludedFormValues();
        boolean valid = validate(entity, state);
        if (!valid) {
            return;
        }
        state.performEvent(view, "save", new String[0]);
        if (state.isHasError()) {
            return;
        }
        state.performEvent(view, "reset", new String[0]);
        entity = getStorageLocationtDD().get(entity.getId());
        generateLocations(entity, state);

    }

    private void generateLocations(Entity entity, ComponentState state) {
        DataDefinition dd = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, "storageLocation");
        int numberOfLocatons = entity.getDecimalField(StorageLocationHelperFields.NUMBER_OF_STORAGE_LOCATIONS).intValue();
        String number = entity.getStringField(StorageLocationHelperFields.NUMBER);

        // number length
        Integer numberLength = number.length();
        // trim 0
        Integer currentNumber = Integer.valueOf(number);

        for (int i = currentNumber; i < numberOfLocatons + currentNumber; i++) {
            Entity sl = dd.create();
            sl.setField(StorageLocationFields.LOCATION, entity.getBelongsToField(StorageLocationHelperFields.LOCATION));
            sl.setField(StorageLocationFields.MAXIMUM_NUMBER_OF_PALLETS,
                    entity.getDecimalField(StorageLocationHelperFields.MAXIMUM_NUMBER_OF_PALLETS));
            sl.setField(StorageLocationFields.PLACE_STORAGE_LOCATION,
                    entity.getBooleanField(StorageLocationHelperFields.PLACE_STORAGE_LOCATION));
            sl.setField(StorageLocationFields.NUMBER, fillNumber(i, numberLength, entity));
            sl = sl.getDataDefinition().save(sl);
            if (!sl.isValid()) {
                state.addMessage("materialFlowResources.storageLocationsHelper.error.locationExist",
                        ComponentState.MessageType.INFO, entity.getStringField(StorageLocationFields.NUMBER));
            }
        }
    }

    private String fillNumber(final Integer nextNumber, int numberLength, Entity entity) {
        StringBuilder restOfNumber = new StringBuilder();

        if (nextNumber.toString().length() > numberLength) {
            restOfNumber.append(nextNumber.toString());
        } else {
            Integer positonToAdd = numberLength - nextNumber.toString().length();
            for (int i = 0; i < positonToAdd; i++) {
                restOfNumber.append(L_ZERO);
            }
            restOfNumber.append(nextNumber);
        }

        return entity.getStringField(StorageLocationHelperFields.PREFIX) + restOfNumber.toString();
    }

    private boolean validate(Entity entity, ComponentState state) {
        boolean valid = true;
        if (entity.getField(StorageLocationHelperFields.NUMBER_OF_STORAGE_LOCATIONS) == null) {
            state.addMessage("materialFlowResources.storageLocationsHelper.error.requiredNumberOf",
                    ComponentState.MessageType.FAILURE);
            valid = false;
        } else {
            String number = entity.getStringField(StorageLocationHelperFields.NUMBER);
            if (!StringUtils.isNumeric(number)) {
                state.addMessage("materialFlowResources.storageLocationsHelper.error.lastCharNotNumeric",
                        ComponentState.MessageType.FAILURE);
                valid = false;
            }
        }

        if (StringUtils.isBlank(entity.getStringField(StorageLocationHelperFields.NUMBER))) {
            state.addMessage("materialFlowResources.storageLocationsHelper.error.requiredNumber",
                    ComponentState.MessageType.FAILURE);
            valid = false;
        }
        if (StringUtils.isBlank(entity.getStringField(StorageLocationHelperFields.PREFIX))) {
            state.addMessage("materialFlowResources.storageLocationsHelper.error.requiredPrefix",
                    ComponentState.MessageType.FAILURE);
            valid = false;
        }
        if (entity.getField(StorageLocationHelperFields.LOCATION) == null) {
            state.addMessage("materialFlowResources.storageLocationsHelper.error.requiredLocation",
                    ComponentState.MessageType.FAILURE);
            valid = false;
        }
        return valid;
    }
}
