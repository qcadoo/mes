package com.qcadoo.mes.materialFlowResources.listeners;

import com.beust.jcommander.internal.Lists;
import com.lowagie.text.pdf.Barcode128;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationHelperFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationNumberHelperFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StorageLocationsListListeners {

    private static final String L_ZERO = "0";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void openStorageLocationsImportPage(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        StringBuilder url = new StringBuilder("../page/materialFlowResources/storageLocationsImport.html");

        view.openModal(url.toString());
    }

    public void redirectToAddManyStorageLocations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Entity storageLocation = getStorageLocationHelperDD().create();

        storageLocation = storageLocation.getDataDefinition().save(storageLocation);

        String url = "../page/materialFlowResources/storageLocationsMultiAdd.html?context={\"form.id\":\"" + storageLocation.getId()
                + "\"}";

        view.openModal(url);
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
            int positionToAdd = numberLength - nextNumber.toString().length();

            for (int i = 0; i < positionToAdd; i++) {
                restOfNumber.append(L_ZERO);
            }

            restOfNumber.append(nextNumber);
        }

        return storageLocationHelper.getStringField(StorageLocationHelperFields.PREFIX) + restOfNumber;
    }

    public void printStorageLocationNumbersReport(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent storageLocationsGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Set<Long> storageLocationIds = storageLocationsGrid.getSelectedEntitiesIds();

        List<Entity> storageLocations = getStorageLocations(storageLocationIds);

        if (!storageLocations.isEmpty()) {
            List<String> invalidNumbers = Lists.newArrayList();

            storageLocations.forEach(storageLocation -> {
                String number = storageLocation.getStringField(StorageLocationFields.NUMBER);

                try {
                    String text = Barcode128.getRawText(number, false);
                } catch (RuntimeException exception) {
                    invalidNumbers.add(number);
                }
            });

            if (invalidNumbers.isEmpty()) {
                Entity storageLocationNumberHelper = createStorageLocationNumberHelper(storageLocations);

                if (Objects.nonNull(storageLocationNumberHelper)) {
                    Long storageLocationNumberHelperId = storageLocationNumberHelper.getId();

                    view.redirectTo("/materialFlowResources/storageLocationNumberHelperReport.pdf?id=" + storageLocationNumberHelperId, true, false);
                }
            } else {
                view.addMessage("materialFlowResources.storageLocation.report.number.invalidCharacters", ComponentState.MessageType.FAILURE, String.join(", ", invalidNumbers));
            }
        }
    }

    public Entity createStorageLocationNumberHelper(final List<Entity> storageLocations) {
        Entity storageLocationNumberHelper = getStorageLocationNumberHelperDD().create();

        storageLocationNumberHelper.setField(StorageLocationNumberHelperFields.STORAGE_LOCATIONS, storageLocations);

        storageLocationNumberHelper = storageLocationNumberHelper.getDataDefinition().save(storageLocationNumberHelper);

        return storageLocationNumberHelper;
    }

    private List<Entity> getStorageLocations(final Set<Long> storageLocationIds) {
        return storageLocationIds.stream().map(this::getStorageLocation).collect(Collectors.toList());
    }

    public Entity getStorageLocation(final Long storageLocationId) {
        return getStorageLocationDD().get(storageLocationId);
    }

    private DataDefinition getStorageLocationDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION);
    }

    public DataDefinition getStorageLocationHelperDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION_HELPER);
    }

    public DataDefinition getStorageLocationNumberHelperDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION_NUMBER_HELPER);
    }

}
