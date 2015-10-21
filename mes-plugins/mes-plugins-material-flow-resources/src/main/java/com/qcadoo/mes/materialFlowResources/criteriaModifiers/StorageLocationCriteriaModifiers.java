package com.qcadoo.mes.materialFlowResources.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class StorageLocationCriteriaModifiers {

    private static final String L_PRODUCT = "product";

    private static final String L_LOCATION = "location";

    public void showStorageLocationsForProductAndLocation(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {

        if (filterValue.has(L_PRODUCT)) {
            Long productId = filterValue.getLong(L_PRODUCT);
            scb.createAlias(StorageLocationFields.PRODUCT, L_PRODUCT, JoinType.LEFT).add(
                    SearchRestrictions.or(SearchRestrictions.isNull(StorageLocationFields.PRODUCT),
                            SearchRestrictions.eq(L_PRODUCT + ".id", productId)));
        }
        if (filterValue.has(L_LOCATION)) {
            Long locationId = filterValue.getLong(L_LOCATION);
            scb.createAlias(StorageLocationFields.LOCATION, L_LOCATION, JoinType.INNER).add(
                    SearchRestrictions.eq(L_LOCATION + ".id", locationId));
        }
    }
}
