package com.qcadoo.mes.technologies.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class OrderProductCriteriaModifiers {

    public static final String L_PRODUCT_FAMILY_ID = "productFamilyId";

    public void showOnlyProductsBelongsToFamily(final SearchCriteriaBuilder searchCriteriaBuilder,
            final FilterValueHolder filterValue) {
        long productFamilyId = 0;

        if (filterValue.has(L_PRODUCT_FAMILY_ID)) {
            productFamilyId = filterValue.getLong(L_PRODUCT_FAMILY_ID);
        }

        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(ProductFields.PARENT, BasicConstants.PLUGIN_IDENTIFIER,
                BasicConstants.MODEL_PRODUCT, productFamilyId));
    }
}
