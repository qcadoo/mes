/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.CompanyProductService;
import com.qcadoo.mes.deliveries.constants.CompanyFieldsD;
import com.qcadoo.mes.deliveries.constants.CompanyProductsFamilyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class CompanyProductsFamilyHooks {

    @Autowired
    private CompanyProductService companyProductService;

    public boolean checkIfProductsFamilyIsNotAlreadyUsed(final DataDefinition companyProductsFamilyDD,
            final Entity companyProductsFamily) {
        if (!companyProductService.checkIfProductIsNotUsed(companyProductsFamily, CompanyProductsFamilyFields.PRODUCT,
                CompanyProductsFamilyFields.COMPANY, CompanyFieldsD.PRODUCTS_FAMILIES)) {
            companyProductsFamily.addError(companyProductsFamilyDD.getField(CompanyProductsFamilyFields.PRODUCT),
                    "basic.company.message.productsFamilyIsAlreadyUsed");
            companyProductsFamily.addError(companyProductsFamilyDD.getField(CompanyProductsFamilyFields.COMPANY),
                    "basic.company.message.companyIsAlreadyUsed");

            return false;
        }

        return true;
    }

    public boolean checkIfProductHasDefaultSupplier(final DataDefinition companyProductsFamilyDD,
            final Entity companyProductsFamily) {
        if (companyProductService.checkIfDefaultExistsForFamily(companyProductsFamily)) {
            companyProductsFamily.addError(companyProductsFamilyDD.getField(CompanyProductsFamilyFields.IS_DEFAULT),
                    "basic.company.message.defaultAlreadyExistsForProductFamily");

            return false;
        }

        String productsWithDefault = companyProductService.checkIfDefaultExistsForProductsInFamily(companyProductsFamily);

        if (!StringUtils.isEmpty(productsWithDefault)) {
            companyProductsFamily.addGlobalError("basic.company.message.defaultAlreadyExistsForChildren", productsWithDefault);

            return false;
        }

        return true;
    }

    public void onSave(final DataDefinition companyProductsFamilyDD, final Entity companyProductsFamily) {
        Entity product = companyProductsFamily.getBelongsToField(CompanyProductsFamilyFields.PRODUCT);
        Entity company = companyProductsFamily.getBelongsToField(CompanyProductsFamilyFields.COMPANY);
        boolean isDefault = companyProductsFamily.getBooleanField(CompanyProductsFamilyFields.IS_DEFAULT);

        if (Objects.isNull(companyProductsFamily.getId())) {
            if (isDefault) {
                Entity supplier = product.getBelongsToField(ProductFields.SUPPLIER);

                if (Objects.isNull(supplier) || !supplier.getId().equals(company.getId())) {
                    product.setField(ProductFields.SUPPLIER, company);

                    product.getDataDefinition().fastSave(product);
                }
            }
        } else {
            Entity companyProductFromDb = companyProductsFamily.getDataDefinition().get(companyProductsFamily.getId());

            if (isDefault != companyProductFromDb.getBooleanField(CompanyProductsFamilyFields.IS_DEFAULT)) {
                product.setField(ProductFields.SUPPLIER, isDefault ? company : null);

                product.getDataDefinition().fastSave(product);
            }
        }
    }

}
