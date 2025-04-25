package com.qcadoo.mes.materialFlowResources.criteriaModifiers;

import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Service;

@Service
public class StorageLocationCriteriaModifiers {

    public void showStorageLocationsForLocation(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(StorageLocationFields.LOCATION)) {
            Long locationId = filterValue.getLong(StorageLocationFields.LOCATION);

            scb.add(SearchRestrictions.belongsTo(StorageLocationFields.LOCATION, MaterialFlowConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowConstants.MODEL_LOCATION, locationId));
        }
    }

    public void showStorageLocationsForLocationWithoutProducts(final SearchCriteriaBuilder scb,
                                                               final FilterValueHolder filterValue) {
        showStorageLocationsForLocation(scb, filterValue);
        scb.add(SearchRestrictions.isEmpty(StorageLocationFields.PRODUCTS));
    }

}
