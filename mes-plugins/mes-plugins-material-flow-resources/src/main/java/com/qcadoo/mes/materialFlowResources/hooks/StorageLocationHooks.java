package com.qcadoo.mes.materialFlowResources.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class StorageLocationHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(final DataDefinition storageLocationDD, final Entity storageLocation) {

        clearMaxNumberOfPallets(storageLocationDD, storageLocation);
    }


    private void clearMaxNumberOfPallets(final DataDefinition storageLocationDD, final Entity storageLocation) {
        boolean placeStorageLocation = storageLocation.getBooleanField(StorageLocationFields.PLACE_STORAGE_LOCATION);

        if (!placeStorageLocation) {
            storageLocation.setField(StorageLocationFields.MAXIMUM_NUMBER_OF_PALLETS, null);
        }
    }

}
