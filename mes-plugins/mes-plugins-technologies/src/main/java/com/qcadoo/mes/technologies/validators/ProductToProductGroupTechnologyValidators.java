package com.qcadoo.mes.technologies.validators;

import org.springframework.stereotype.Component;

import com.qcadoo.mes.technologies.constants.ProductToProductGroupFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Component
public class ProductToProductGroupTechnologyValidators {

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity entity) {
        boolean isNotAlreadyAdded = true;

        Long id = entity.getId();

        SearchCriteriaBuilder scb = dataDefinition.find()
                .add(SearchRestrictions.belongsTo(ProductToProductGroupFields.FINAL_PRODUCT,
                        entity.getBelongsToField(ProductToProductGroupFields.FINAL_PRODUCT)))
                .add(SearchRestrictions.belongsTo(ProductToProductGroupFields.PRODUCT_FAMILY,
                        entity.getBelongsToField(ProductToProductGroupFields.PRODUCT_FAMILY)));
        if (id != null) {
            scb.add(SearchRestrictions.idNe(id));
        }

        if (scb.list().getTotalNumberOfEntities() > 0) {
            entity.addGlobalError("technologies.productToProductGroupTechnology.validate.error.bindingAlreadyAdded");

            isNotAlreadyAdded = false;
        }

        return isNotAlreadyAdded;
    }
}
