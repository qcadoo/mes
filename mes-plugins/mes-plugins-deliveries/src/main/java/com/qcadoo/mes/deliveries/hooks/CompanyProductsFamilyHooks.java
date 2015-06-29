/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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

import static com.qcadoo.mes.basic.constants.ProductFamilyElementType.PRODUCTS_FAMILY;
import static com.qcadoo.mes.deliveries.constants.CompanyFieldsD.PRODUCTS_FAMILIES;
import static com.qcadoo.mes.deliveries.constants.CompanyProductFields.IS_DEFAULT;
import static com.qcadoo.mes.deliveries.constants.CompanyProductsFamilyFields.COMPANY;
import static com.qcadoo.mes.deliveries.constants.CompanyProductsFamilyFields.PRODUCT;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ProductService;
import com.qcadoo.mes.deliveries.CompanyProductService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class CompanyProductsFamilyHooks {

    @Autowired
    private CompanyProductService companyProductService;

    @Autowired
    private ProductService productService;

    public boolean checkIfProductIsProductsFamily(final DataDefinition companyProductsFamilyDD, final Entity companyProductsFamily) {
        if (!productService.checkIfProductEntityTypeIsCorrect(companyProductsFamily.getBelongsToField(PRODUCT), PRODUCTS_FAMILY)) {
            companyProductsFamily.addError(companyProductsFamilyDD.getField(PRODUCT),
                    "basic.company.message.productIsNotProductsFamily");

            return false;
        }

        return true;
    }

    public boolean checkIfProductsFamilyIsNotAlreadyUsed(final DataDefinition companyProductsFamilyDD,
            final Entity companyProductsFamily) {
        if (!companyProductService.checkIfProductIsNotUsed(companyProductsFamily, PRODUCT, COMPANY, PRODUCTS_FAMILIES)) {
            companyProductsFamily.addError(companyProductsFamilyDD.getField(PRODUCT),
                    "basic.company.message.productsFamilyIsAlreadyUsed");
            companyProductsFamily.addError(companyProductsFamilyDD.getField(COMPANY),
                    "basic.company.message.companyIsAlreadyUsed");
            return false;
        }

        return true;
    }

    public boolean checkIfProductHasDefaultSupplier(final DataDefinition companyProductDD, final Entity companyProduct) {
        if (companyProductService.checkIfDefaultExistsForFamily(companyProduct)) {
            companyProduct.addError(companyProductDD.getField(IS_DEFAULT),
                    "basic.company.message.defaultAlreadyExistsForProductFamily");
            return false;
        }
        String productsWithDefault = companyProductService.checkIfDefaultExistsForProductsInFamily(companyProduct);
        if (!StringUtils.isEmpty(productsWithDefault)) {
            companyProduct.addGlobalError("basic.company.message.defaultAlreadyExistsForChildren", productsWithDefault);
            return false;
        }

        return true;
    }
}
