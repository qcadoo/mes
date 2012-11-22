/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.basic.constants.ProductFamilyElementType.PARTICULAR_PRODUCT;
import static com.qcadoo.mes.basic.constants.ProductFamilyElementType.PRODUCTS_FAMILY;
import static com.qcadoo.mes.basic.constants.ProductFields.ENTITY_TYPE;
import static com.qcadoo.mes.basic.constants.ProductFields.NUMBER;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class CompanyHooksD {

    public boolean checkIfProductIsParticularProduct(final DataDefinition companyDD, final Entity company) {
        List<Entity> products = company.getHasManyField("products");

        return checkIfProductsEntityTypesAreCorrect(company, products, PARTICULAR_PRODUCT,
                "basic.companies.message.productIsNotParticularProduct");
    }

    public boolean checkIfProductIsProductsFamily(final DataDefinition companyDD, final Entity company) {
        List<Entity> productsFamilies = company.getHasManyField("productsFamily");

        return checkIfProductsEntityTypesAreCorrect(company, productsFamilies, PRODUCTS_FAMILY,
                "basic.companies.message.productIsNotProductsFamily");
    }

    private boolean checkIfProductsEntityTypesAreCorrect(final Entity company, final List<Entity> products,
            final ProductFamilyElementType entityType, final String errorMessage) {
        boolean isValid = true;

        for (Entity product : products) {
            if (!entityType.getStringValue().equals(product.getStringField(ENTITY_TYPE))) {
                company.addGlobalError(errorMessage, product.getStringField(NUMBER));

                isValid = false;
            }
        }

        return isValid;
    }

}
