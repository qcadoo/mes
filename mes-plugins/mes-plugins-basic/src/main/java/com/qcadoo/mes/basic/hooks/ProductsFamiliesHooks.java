package com.qcadoo.mes.basic.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class ProductsFamiliesHooks {

    private static CustomRestriction customRestriction = new CustomRestriction() {

        @Override
        public void addRestriction(final SearchCriteriaBuilder searchBuilder) {
            searchBuilder.add(SearchRestrictions.isNull("parent")).add(
                    SearchRestrictions.eq(ProductFields.PRODUCT_FAMILY_ELEMENT_TYPE,
                            ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue()));
        }

    };

    public final void addDiscriminatorRestrictionToProductsFamilies(final ViewDefinitionState view) {
        GridComponent grid = (GridComponent) view.getComponentByReference("parents");
        grid.setCustomRestriction(customRestriction);
    }
}
