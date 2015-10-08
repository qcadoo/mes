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
        return true;
    }

    public DataDefinition getStorageLocationtDD() {
        return dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION);
    }

    public Optional<Entity> findStorageLocationForProduct(final Entity product){
        SearchCriteriaBuilder scb = getStorageLocationtDD().find();
        scb.add(SearchRestrictions.belongsTo(StorageLocationFields.PRODUCT, product));
        return Optional.ofNullable(scb.setMaxResults(1).uniqueResult());
    }
}
