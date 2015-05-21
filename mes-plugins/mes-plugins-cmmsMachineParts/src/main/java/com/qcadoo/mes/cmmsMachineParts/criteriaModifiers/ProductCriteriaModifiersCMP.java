package com.qcadoo.mes.cmmsMachineParts.criteriaModifiers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.criteriaModifiers.ProductCriteriaModifiers;
import com.qcadoo.mes.cmmsMachineParts.constants.ProductFieldsCMP;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

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
}
