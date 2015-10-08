package com.qcadoo.mes.materialFlowResources.validators;

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

import java.util.Optional;

@Service
public class StorageLocationModelValidators {

    @Autowired private DataDefinitionService dataDefinitionService;

    public boolean validate(final DataDefinition storageLocationDefinition, final Entity storageLocation) {
        Entity product = storageLocation.getBelongsToField(StorageLocationFields.PRODUCT);
        if(product != null){
            Entity location = storageLocation.getBelongsToField(StorageLocationFields.LOCATION);
            return checkUniqueParProductLocation(storageLocation, product, location);
        }
        return true;
    }

    private boolean checkUniqueParProductLocation(final Entity storageLocation, final Entity product, final Entity location) {
        Optional<Entity> optional = findStorageLocationForProduct(product, location);
        if (optional.isPresent()) {
            Entity sc = optional.get();

            if (storageLocation.getId() == null) {
                storageLocation.addError(storageLocation.getDataDefinition().getField(StorageLocationFields.PRODUCT),
                        "materialFlowResources.storageLocations.error.locationExist");
                return false;
            } else if (sc.getId().equals(storageLocation.getId())) {
                return true;
            } else {
                storageLocation.addError(storageLocation.getDataDefinition().getField(StorageLocationFields.PRODUCT),
                        "materialFlowResources.storageLocations.error.locationExist");
                return false;
            }

        } else {
            return true;
        }
    }

    public DataDefinition getStorageLocationtDD() {
        return dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION);
    }

    public Optional<Entity> findStorageLocationForProduct(final Entity product, final Entity location){
        SearchCriteriaBuilder scb = getStorageLocationtDD().find();
        scb.add(SearchRestrictions.belongsTo(StorageLocationFields.PRODUCT, product));
        scb.add(SearchRestrictions.belongsTo(StorageLocationFields.LOCATION, location));
        return Optional.ofNullable(scb.setMaxResults(1).uniqueResult());
    }
}
