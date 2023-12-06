package com.qcadoo.mes.materialFlowResources.listeners;

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationHelperFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class StorageLocationsMultiAddListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void updateFields(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        CheckBoxComponent isPlaceCheckBox = (CheckBoxComponent) view.getComponentByReference(StorageLocationFields.PLACE_STORAGE_LOCATION);
        FieldComponent maximumNumberOfPalletsField = (FieldComponent) view.getComponentByReference(StorageLocationFields.MAXIMUM_NUMBER_OF_PALLETS);

        if (isPlaceCheckBox.isChecked()) {
            maximumNumberOfPalletsField.setEnabled(true);
        } else {
            maximumNumberOfPalletsField.setEnabled(false);
            maximumNumberOfPalletsField.setFieldValue(null);
        }

        maximumNumberOfPalletsField.requestComponentUpdateState();
    }

    public void createStorageLocations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent storageLocationHelperForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity storageLocationHelper = storageLocationHelperForm.getPersistedEntityWithIncludedFormValues();

        boolean valid = validate(view, storageLocationHelper);

        if (!valid) {
            return;
        }

        state.performEvent(view, "save");

        if (state.isHasError()) {
            return;
        }

        state.performEvent(view, "reset");

        storageLocationHelper = getStorageLocationHelperDD().get(storageLocationHelper.getId());

        generateLocations(view, storageLocationHelper);
    }

    private boolean validate(final ViewDefinitionState view, final Entity storageLocationHelper) {
        boolean valid = true;

        BigDecimal numberOfStorageLocations = storageLocationHelper.getDecimalField(StorageLocationHelperFields.NUMBER_OF_STORAGE_LOCATIONS);
        String number = storageLocationHelper.getStringField(StorageLocationHelperFields.NUMBER);
        String prefix = storageLocationHelper.getStringField(StorageLocationHelperFields.PREFIX);
        Entity location = storageLocationHelper.getBelongsToField(StorageLocationHelperFields.LOCATION);

        if (Objects.isNull(numberOfStorageLocations)) {
            view.addMessage("materialFlowResources.storageLocationsHelper.error.requiredNumberOf",
                    ComponentState.MessageType.FAILURE);

            valid = false;
        } else {
            if (!StringUtils.isNumeric(number)) {
                view.addMessage("materialFlowResources.storageLocationsHelper.error.lastCharNotNumeric",
                        ComponentState.MessageType.FAILURE);

                valid = false;
            }
        }

        if (StringUtils.isBlank(number)) {
            view.addMessage("materialFlowResources.storageLocationsHelper.error.requiredNumber",
                    ComponentState.MessageType.FAILURE);

            valid = false;
        }

        if (StringUtils.isBlank(prefix)) {
            view.addMessage("materialFlowResources.storageLocationsHelper.error.requiredPrefix",
                    ComponentState.MessageType.FAILURE);

            valid = false;
        }

        if (Objects.isNull(location)) {
            view.addMessage("materialFlowResources.storageLocationsHelper.error.requiredLocation",
                    ComponentState.MessageType.FAILURE);

            valid = false;
        }

        return valid;
    }

    private void generateLocations(final ViewDefinitionState view, final Entity storageLocationHelper) {
        DataDefinition storageLocationDD = getStorageLocationDD();

        int numberOfLocations = storageLocationHelper.getDecimalField(StorageLocationHelperFields.NUMBER_OF_STORAGE_LOCATIONS).intValue();
        String number = storageLocationHelper.getStringField(StorageLocationHelperFields.NUMBER);
        Entity location = storageLocationHelper.getBelongsToField(StorageLocationHelperFields.LOCATION);
        BigDecimal maximumNumberOfPallets = storageLocationHelper.getDecimalField(StorageLocationHelperFields.MAXIMUM_NUMBER_OF_PALLETS);
        boolean highStorageLocation = storageLocationHelper.getBooleanField(StorageLocationHelperFields.HIGH_STORAGE_LOCATION);
        boolean placeStorageLocation = storageLocationHelper.getBooleanField(StorageLocationHelperFields.PLACE_STORAGE_LOCATION);

        int numberLength = number.length();
        int currentNumber = Integer.parseInt(number);

        for (int i = currentNumber; i < numberOfLocations + currentNumber; i++) {
            Entity storageLocation = storageLocationDD.create();

            storageLocation.setField(StorageLocationFields.LOCATION, location);
            storageLocation.setField(StorageLocationFields.MAXIMUM_NUMBER_OF_PALLETS, maximumNumberOfPallets);
            storageLocation.setField(StorageLocationFields.PLACE_STORAGE_LOCATION, placeStorageLocation);
            storageLocation.setField(StorageLocationFields.NUMBER, fillNumber(storageLocationHelper, i, numberLength));
            storageLocation.setField(StorageLocationFields.HIGH_STORAGE_LOCATION, highStorageLocation);

            storageLocation = storageLocation.getDataDefinition().save(storageLocation);

            if (!storageLocation.isValid()) {
                view.addMessage("materialFlowResources.storageLocationsHelper.error.locationExist",
                        ComponentState.MessageType.INFO, number);
            }
        }
    }

    private String fillNumber(final Entity storageLocationHelper, final Integer nextNumber, final int numberLength) {
        StringBuilder restOfNumber = new StringBuilder();

        if (nextNumber.toString().length() > numberLength) {
            restOfNumber.append(nextNumber);
        } else {
            restOfNumber.append(String.format("%0" + numberLength + "d", nextNumber));
        }

        return storageLocationHelper.getStringField(StorageLocationHelperFields.PREFIX) + restOfNumber;
    }

    private DataDefinition getStorageLocationDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION);
    }

    public DataDefinition getStorageLocationHelperDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION_HELPER);
    }

}
