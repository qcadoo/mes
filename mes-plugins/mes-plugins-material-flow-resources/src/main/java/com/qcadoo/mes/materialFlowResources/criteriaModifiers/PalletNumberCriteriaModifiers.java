package com.qcadoo.mes.materialFlowResources.criteriaModifiers;

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.search.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PalletNumberCriteriaModifiers {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void showPalletNumbersForLocation(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(StorageLocationFields.LOCATION)) {
            Long locationId = filterValue.getLong(StorageLocationFields.LOCATION);

            SearchCriteriaBuilder resourcesCriteria = getResourceDD().findWithAlias(MaterialFlowResourcesConstants.MODEL_RESOURCE)
                    .createAlias(ResourceFields.LOCATION, ResourceFields.LOCATION, JoinType.INNER)
                    .add(SearchRestrictions.eq(ResourceFields.LOCATION + ".id", locationId))
                    .createAlias(ResourceFields.PALLET_NUMBER, ResourceFields.PALLET_NUMBER, JoinType.INNER)
                    .add(SearchRestrictions.eqField(ResourceFields.PALLET_NUMBER + ".id", "this.id"))
                    .setProjection(SearchProjections.id());
            SearchCriteriaBuilder noResourcesCriteria = getResourceDD().findWithAlias(MaterialFlowResourcesConstants.MODEL_RESOURCE)
                    .createAlias(ResourceFields.PALLET_NUMBER, ResourceFields.PALLET_NUMBER, JoinType.INNER)
                    .add(SearchRestrictions.eqField(ResourceFields.PALLET_NUMBER + ".id", "this.id"))
                    .setProjection(SearchProjections.id());
            scb.add(SearchRestrictions.or(SearchSubqueries.exists(resourcesCriteria), SearchSubqueries.notExists(noResourcesCriteria)));
        }
    }


    private DataDefinition getResourceDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE);
    }
}
