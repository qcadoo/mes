package com.qcadoo.mes.basic.criteriaModifiers;

import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

import org.springframework.stereotype.Service;

@Service
public class ReplacementsCriteriaModifiers {

    public void filter(final SearchCriteriaBuilder scb, final FilterValueHolder filter) {
        if (filter.has("PRODUCT_ID")) {
            Long productId = filter.getLong("PRODUCT_ID");
            scb.add(SearchRestrictions.idNe(productId));
        }
    }
}
