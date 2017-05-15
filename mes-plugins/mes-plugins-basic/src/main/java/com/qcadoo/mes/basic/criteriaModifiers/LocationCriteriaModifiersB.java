package com.qcadoo.mes.basic.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class LocationCriteriaModifiersB {

    public void showWarehousesOnly(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq("type", "02warehouse"));
    }

}
