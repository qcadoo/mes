package com.qcadoo.mes.materialFlow.criteriaModifiers;

import static com.qcadoo.mes.materialFlow.constants.LocationFields.TYPE;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class LocationCriteriaModifiers {

    public void showWarehousesOnly(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(TYPE, "02warehouse"));
    }
}