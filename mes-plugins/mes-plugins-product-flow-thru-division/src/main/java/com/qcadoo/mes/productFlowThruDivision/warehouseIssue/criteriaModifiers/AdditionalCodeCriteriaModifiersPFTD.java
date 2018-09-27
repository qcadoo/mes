package com.qcadoo.mes.productFlowThruDivision.warehouseIssue.criteriaModifiers;

import com.qcadoo.mes.basic.constants.AdditionalCodeFields;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Service;

@Service
public class AdditionalCodeCriteriaModifiersPFTD {

    private static final String L_PRODUCT = "product";

    public void showForProduct(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(L_PRODUCT)) {
            Long productId = filterValueHolder.getLong(L_PRODUCT);
            addCriteria(scb, productId);
        } else {
            Long productId = 0l;
            addCriteria(scb, productId);
        }
    }

    private void addCriteria(final SearchCriteriaBuilder scb, final Long productId) {
        scb.createAlias(AdditionalCodeFields.PRODUCT, L_PRODUCT, JoinType.INNER).add(
                SearchRestrictions.eq(L_PRODUCT + ".id", productId));
    }
}
