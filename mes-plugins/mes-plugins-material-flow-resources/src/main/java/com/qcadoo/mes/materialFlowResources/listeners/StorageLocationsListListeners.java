package com.qcadoo.mes.materialFlowResources.listeners;

import com.beust.jcommander.internal.Lists;
import com.lowagie.text.pdf.Barcode128;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationNumberHelperFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StorageLocationsListListeners {

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
