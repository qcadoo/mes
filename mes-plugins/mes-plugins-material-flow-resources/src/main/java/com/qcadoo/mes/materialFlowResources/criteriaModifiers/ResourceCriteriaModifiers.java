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

        scb.createCriteria(ResourceFields.PRODUCT, L_PRODUCT, JoinType.INNER).add(
                SearchRestrictions.idEq(filterValue.getLong(L_PRODUCT)));

        scb.createCriteria(ResourceFields.LOCATION, L_LOCATION_FROM, JoinType.INNER).add(
                SearchRestrictions.idEq(filterValue.getLong(L_LOCATION_FROM)));
    }

}
