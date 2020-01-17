package com.qcadoo.mes.productionCounting.criteriaModifiers;

import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

import org.springframework.stereotype.Service;

@Service
public class StorageLocationCriteriaModifierPC {

    private static final String L_LOCATION_ID = "locationId";

    public void filter(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(L_LOCATION_ID)) {
            Long location = filterValue.getLong(L_LOCATION_ID);
            scb.add(SearchRestrictions.eq("location.id", location));
        } else {
            scb.add(SearchRestrictions.eq("location.id", Long.valueOf("-1")));
        }

    }
}
