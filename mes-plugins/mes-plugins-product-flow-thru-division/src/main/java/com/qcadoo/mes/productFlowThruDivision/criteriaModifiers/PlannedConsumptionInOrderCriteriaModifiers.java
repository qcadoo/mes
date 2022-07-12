package com.qcadoo.mes.productFlowThruDivision.criteriaModifiers;

import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Service;

@Service
public class PlannedConsumptionInOrderCriteriaModifiers {

    public static final String L_PRODUCT_ID = "productId";

    public void filterByProduct(final SearchCriteriaBuilder searchCriteriaBuilder, final FilterValueHolder filterValue) {
        if (filterValue.has(L_PRODUCT_ID)) {
            searchCriteriaBuilder.add(SearchRestrictions.eq("productId", filterValue.getInteger(L_PRODUCT_ID)));
        }
    }
}
