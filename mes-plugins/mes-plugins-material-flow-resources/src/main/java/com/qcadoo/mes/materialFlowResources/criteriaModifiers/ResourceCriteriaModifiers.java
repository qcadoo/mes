package com.qcadoo.mes.materialFlowResources.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class ResourceCriteriaModifiers {

    private static final String L_PRODUCT = "product";

    private static final String L_LOCATION_FROM = "locationFrom";

    public void showResourcesForProductInWarehouse(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {

        if (!filterValue.has(L_PRODUCT) || !filterValue.has(L_LOCATION_FROM)) {
            return;
        }

        Long productId = filterValue.getLong(L_PRODUCT);
        Long locationId = filterValue.getLong(L_LOCATION_FROM);
        scb.createAlias(ResourceFields.PRODUCT, L_PRODUCT, JoinType.INNER)
                .createAlias(ResourceFields.LOCATION, L_LOCATION_FROM, JoinType.INNER)
                .add(SearchRestrictions.eq(L_PRODUCT + ".id", productId))
                .add(SearchRestrictions.eq(L_LOCATION_FROM + ".id", locationId));
        // scb.createCriteria(ResourceFields.PRODUCT, L_PRODUCT, JoinType.INNER).add(SearchRestrictions.idEq(productId));
        // scb.createCriteria(ResourceFields.LOCATION, L_LOCATION_FROM, JoinType.INNER).add(SearchRestrictions.idEq(locationId));
    }

}
