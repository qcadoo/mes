package com.qcadoo.mes.basic.criteriaModifiers;

import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

import org.springframework.stereotype.Service;

@Service
public class AttributeCriteriaModifiers {

    public void showForProduct(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(AttributeFields.FOR_PRODUCT, Boolean.TRUE));
    }

    public void showForResource(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(AttributeFields.FOR_RESOURCE, Boolean.TRUE));
    }
}
