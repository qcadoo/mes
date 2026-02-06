package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.materialFlowResources.PalletValidatorService;
import com.qcadoo.mes.materialFlowResources.constants.*;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class StorageLocationHooks {

    @Autowired
    private PalletValidatorService palletValidatorService;

    public boolean validatesWith(final DataDefinition storageLocationDD, final Entity storageLocation) {
        boolean isValid = checkLocationChange(storageLocationDD, storageLocation);

        isValid = isValid && checkResourcePalletNumbers(storageLocationDD, storageLocation);
        isValid = isValid && checkMaxNumberOfPallets(storageLocationDD, storageLocation);

        return isValid;
    }

    private boolean checkLocationChange(final DataDefinition storageLocationDD, final Entity storageLocation) {
        Long storageLocationId = storageLocation.getId();
        Entity location = storageLocation.getBelongsToField(StorageLocationFields.LOCATION);
        List<Entity> resources = storageLocation.getHasManyField(StorageLocationFields.RESOURCES);

        if (Objects.nonNull(storageLocationId)) {
            Entity storageLocationFromDB = storageLocationDD.get(storageLocationId);
            Entity locationFromDB = storageLocationFromDB.getBelongsToField(StorageLocationFields.LOCATION);

            if (Objects.nonNull(location) && !location.getId().equals(locationFromDB.getId())) {
                if (!resources.isEmpty()) {
                    storageLocation.addError(storageLocationDD.getField(StorageLocationFields.LOCATION),
                            "materialFlowResources.storageLocation.location.resourcesExists");
                    return false;
                } else if (storageLocation.getHasManyField(StorageLocationFields.POSITIONS).stream().map(p -> p.getBelongsToField(PositionFields.DOCUMENT))
                        .anyMatch(d -> DocumentType.isInbound(d.getStringField(DocumentFields.TYPE)) &&
                                DocumentState.DRAFT.getStringValue().equals(d.getStringField(DocumentFields.STATE)))) {
                    storageLocation.addError(storageLocationDD.getField(StorageLocationFields.LOCATION),
                            "materialFlowResources.storageLocation.location.inboundDocumentExists");
                    return false;
                }
            }
        }

        return true;
    }

    private boolean checkResourcePalletNumbers(final DataDefinition storageLocationDD, final Entity storageLocation) {
        if (palletValidatorService.checkPalletNumbersInStorageLocation(storageLocation)) {
            storageLocation.addError(storageLocationDD.getField(StorageLocationFields.PLACE_STORAGE_LOCATION),
                    "materialFlowResources.storageLocation.maximumNumberOfPallets.palletsWithoutPalletNumbers");

            return false;
        }

        return true;
    }

    private boolean checkMaxNumberOfPallets(final DataDefinition storageLocationDD, final Entity storageLocation) {
        if (palletValidatorService.checkMaximumNumberOfPallets(storageLocation, 0)) {
            storageLocation.addError(storageLocationDD.getField(StorageLocationFields.MAXIMUM_NUMBER_OF_PALLETS),
                    "materialFlowResources.storageLocation.maximumNumberOfPallets.toManyPallets");

            return false;
        }

        return true;
    }

    public void onSave(final DataDefinition storageLocationDD, final Entity storageLocation) {
        clearMaxNumberOfPallets(storageLocationDD, storageLocation);
    }

    private void clearMaxNumberOfPallets(final DataDefinition storageLocationDD, final Entity storageLocation) {
        boolean placeStorageLocation = storageLocation.getBooleanField(StorageLocationFields.PLACE_STORAGE_LOCATION);

        if (!placeStorageLocation) {
            storageLocation.setField(StorageLocationFields.MAXIMUM_NUMBER_OF_PALLETS, null);
        }
    }

    public boolean onDelete(final DataDefinition dataDefinition, final Entity entity) {
        if (!entity.getHasManyField(StorageLocationFields.POSITIONS).isEmpty()
                || !entity.getHasManyField(StorageLocationFields.RESOURCES).isEmpty()
                || !entity.getHasManyField(StorageLocationFields.REPACKING).isEmpty()
                || !entity.getHasManyField(StorageLocationFields.REPACKING_POSITIONS).isEmpty()
                || !entity.getManyToManyField(StorageLocationFields.STOCKTAKINGS).isEmpty()
                || !entity.getHasManyField(StorageLocationFields.STOCKTAKING_POSITIONS).isEmpty()
                || !entity.getHasManyField(StorageLocationFields.STOCKTAKING_DIFFERENCES).isEmpty()) {
            entity.addGlobalError("qcadooView.errorPage.error.dataIntegrityViolationException.objectInUse.explanation");
            return false;
        }
        return true;
    }
}
