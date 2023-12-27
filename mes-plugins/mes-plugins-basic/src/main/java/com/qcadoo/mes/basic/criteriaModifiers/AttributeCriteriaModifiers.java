package com.qcadoo.mes.basic.criteriaModifiers;

import com.qcadoo.mes.basic.constants.AttributeDataType;
import com.qcadoo.mes.basic.constants.AttributeFields;
import com.qcadoo.mes.basic.constants.AttributeValueType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

import org.springframework.stereotype.Service;

@Service
public class AttributeCriteriaModifiers {

    public void showForProductCalculatedType(final SearchCriteriaBuilder scb) {
        showForProduct(scb);
        showCalculatedOnly(scb);
    }

    public void showForProduct(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(AttributeFields.FOR_PRODUCT, Boolean.TRUE));
    }
    public void showNumericForProduct(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(AttributeFields.FOR_PRODUCT, Boolean.TRUE));
        scb.add(SearchRestrictions.eq(AttributeFields.VALUE_TYPE, AttributeValueType.NUMERIC.getStringValue()));
    }

    public void showForResource(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(AttributeFields.FOR_RESOURCE, Boolean.TRUE));
    }

    public void showForQualityControl(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(AttributeFields.FOR_QUALITY_CONTROL, Boolean.TRUE));
    }

    private void showCalculatedOnly(SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(AttributeFields.DATA_TYPE, AttributeDataType.CALCULATED.getStringValue()));
    }

    private void showContinuousOnly(SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(AttributeFields.DATA_TYPE, AttributeDataType.CONTINUOUS.getStringValue()));
    }

}
