package com.qcadoo.mes.basic.criteriaModifiers;

import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

import org.springframework.stereotype.Service;

@Service
public class AttributeCriteriaModifiers {

    public void showForProductCalculatedType(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(AttributeFields.FOR_PRODUCT, Boolean.TRUE));
        scb.add(SearchRestrictions.eq(AttributeFields.DATA_TYPE, "01calculated"));
    }

    public void showForProduct(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(AttributeFields.FOR_PRODUCT, Boolean.TRUE));
    }

    public void showForResource(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(AttributeFields.FOR_RESOURCE, Boolean.TRUE));
    }

    public void showForQualityControl(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(AttributeFields.FOR_QUALITY_CONTROL, Boolean.TRUE));
    }
}
