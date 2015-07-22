package com.qcadoo.mes.cmmsMachineParts.criteriaModifiers;

import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.criteriaModifiers.ProductCriteriaModifiers;
import com.qcadoo.mes.cmmsMachineParts.constants.ProductFieldsCMP;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

import static com.qcadoo.mes.basic.constants.ProductFields.ENTITY_TYPE;

@Service
public class ProductCriteriaModifiersCMP {

    @Autowired
    private ProductCriteriaModifiers productCriteriaModifiers;

    public void showMachineParts(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(ProductFieldsCMP.MACHINE_PART, true));
    }

    public void showMachinePartsWithoutGivenProduct(final SearchCriteriaBuilder scb, final FilterValueHolder filter) {
        productCriteriaModifiers.showProductsWithoutGivenProduct(scb, filter);
        scb.add(SearchRestrictions.eq(ProductFieldsCMP.MACHINE_PART, true));
    }

    public void showFamilyMachineParts(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(ProductFieldsCMP.MACHINE_PART, true));
        scb.add(SearchRestrictions.eq(ENTITY_TYPE, ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue()));
    }

    public void showFamilyParticularProduct(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.or(SearchRestrictions.eq(ProductFieldsCMP.MACHINE_PART, false), SearchRestrictions.isNull(ProductFieldsCMP.MACHINE_PART)));
        scb.add(SearchRestrictions.eq(ENTITY_TYPE, ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue()));
    }
}
