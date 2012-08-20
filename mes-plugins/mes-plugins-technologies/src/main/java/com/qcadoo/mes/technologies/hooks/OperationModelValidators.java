/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.technologies.hooks;

import static com.qcadoo.mes.technologies.constants.OperationFields.PRODUCT_IN_COMPONENTS;
import static com.qcadoo.mes.technologies.constants.OperationFields.PRODUCT_OUT_COMPONENTS;
import static com.qcadoo.mes.technologies.constants.ProductComponentFields.PRODUCT;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

@Service
public class OperationModelValidators {

    public boolean checkIfProductsInProductComponentsAreDistinct(final DataDefinition operationDD, final Entity operation) {
        List<Entity> productInComponents = operation.getHasManyField(PRODUCT_IN_COMPONENTS);
        List<Entity> productOutComponents = operation.getHasManyField(PRODUCT_OUT_COMPONENTS);

        return (checkIfProductsInProductComponentsAreDistinct(productInComponents) && checkIfProductsInProductComponentsAreDistinct(productOutComponents));
    }

    private boolean checkIfProductsInProductComponentsAreDistinct(final List<Entity> productInComponents) {
        boolean isValid = true;

        for (Entity productInComponent : productInComponents) {
            Entity product = productInComponent.getBelongsToField(PRODUCT);
            if (isProductAlreadyAdded(productInComponents, product)) {
                appendErrorToModelField(productInComponent, PRODUCT, "technologies.productComponent.error.productAlreadyAdded");

                isValid = false;
            }
        }

        return isValid;
    }

    private boolean isProductAlreadyAdded(final List<Entity> productInComponents, final Entity product) {
        if (product == null) {
            return false;
        }

        int count = 0;

        for (Entity productInComponent : productInComponents) {
            Entity productAlreadyAdded = productInComponent.getBelongsToField(PRODUCT);
            if (product.equals(productAlreadyAdded)) {
                count++;

                if (count > 1) {
                    return true;
                }
            }
        }

        return false;
    }

    private void appendErrorToModelField(final Entity entity, final String fieldName, final String messageKey) {
        FieldDefinition productInFieldDef = entity.getDataDefinition().getField(fieldName);
        entity.addError(productInFieldDef, messageKey);
    }

}
