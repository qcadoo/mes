package com.qcadoo.mes.productFlowThruDivision.warehouseIssue.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlow.constants.LocationType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class ProductToIssueCorrectionCriteriaModifiers {

    public void showAvailableWarehouses(SearchCriteriaBuilder scb, FilterValueHolder filter) {
        if (filter.has("locationFrom")) {
            scb.add(SearchRestrictions.ne("id", filter.getLong("locationFrom")));
        }
        if (filter.has("locationTo")) {
            scb.add(SearchRestrictions.ne("id", filter.getLong("locationTo")));
        }
        scb.add(SearchRestrictions.eq(LocationFields.TYPE, LocationType.WAREHOUSE.getStringValue()));
    }

}
