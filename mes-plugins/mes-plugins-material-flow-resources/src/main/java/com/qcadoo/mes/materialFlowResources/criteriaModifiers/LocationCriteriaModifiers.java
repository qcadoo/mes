package com.qcadoo.mes.materialFlowResources.criteriaModifiers;

import static com.qcadoo.mes.materialFlow.constants.LocationFields.TYPE;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.LocationTypeMFR;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class LocationCriteriaModifiers {

    public void showWarehousesOnly(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(TYPE, LocationTypeMFR.WAREHOUSE.getStringValue()));
    }

}