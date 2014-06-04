package com.qcadoo.mes.basic.validators;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductValidators {

    public boolean checkEanUniqueness(final DataDefinition productDD, final FieldDefinition eanFieldDefinition,
            final Entity product, final Object eanOldValue, final Object eanNewValue) {
        String ean = (String) eanNewValue;
        if (StringUtils.isEmpty(ean) || ObjectUtils.equals(eanOldValue, ean)) {
            return true;
        }

        if (productWithEanAlreadyExists(productDD, ean)) {
            product.addError(eanFieldDefinition, "qcadooView.validate.field.error.duplicated");
            return false;
        }

        return true;
    }

    private boolean productWithEanAlreadyExists(final DataDefinition productDD, final String notEmptyEan) {
        SearchCriteriaBuilder scb = productDD.find();
        scb.setProjection(SearchProjections.id());
        scb.add(SearchRestrictions.eq(ProductFields.EAN, notEmptyEan));
        return scb.setMaxResults(1).uniqueResult() != null;
    }

}
