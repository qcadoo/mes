package com.qcadoo.mes.basic.criteriaModifiers;

import static com.qcadoo.mes.basic.constants.ProductFields.ENTITY_TYPE;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductCriteriaModifiers {

    public void showProductFamilyOnly(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(ENTITY_TYPE, ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue()));
    }

    public void showParticularProductOnly(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(ENTITY_TYPE, ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue()));
    }

}
