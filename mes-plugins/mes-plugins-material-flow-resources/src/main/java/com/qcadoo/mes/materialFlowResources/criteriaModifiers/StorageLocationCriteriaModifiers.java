package com.qcadoo.mes.materialFlowResources.criteriaModifiers;

import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StorageLocationCriteriaModifiers {

    public static final String L_LOCATION = "location";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void showStorageLocationsForLocation(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(L_LOCATION)) {
            Long locationId = filterValue.getLong(L_LOCATION);

            scb.add(SearchRestrictions.belongsTo(StorageLocationFields.LOCATION, MaterialFlowConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowConstants.MODEL_LOCATION, locationId));
        }
    }

}
