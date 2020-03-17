package com.qcadoo.mes.technologies.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.QualityCardFields;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class QualityCardCriteriaModifiers {

    public static final String L_PRODUCT_ID = "productId";

    public void showQualityCardsForProduct(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(L_PRODUCT_ID)) {
            Long productId = filterValue.getLong(L_PRODUCT_ID);
            scb.createAlias(QualityCardFields.PRODUCTS, QualityCardFields.PRODUCTS, JoinType.INNER)
                    .add(SearchRestrictions.eq(QualityCardFields.PRODUCTS + ".id", productId))
                    .add(SearchRestrictions.eq(QualityCardFields.STATE, "02accepted"));
        }
    }
}
