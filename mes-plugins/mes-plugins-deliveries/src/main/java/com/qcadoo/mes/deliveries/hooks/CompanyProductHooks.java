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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.CompanyProductService;
import com.qcadoo.mes.deliveries.constants.CompanyFieldsD;
import com.qcadoo.mes.deliveries.constants.CompanyProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class CompanyProductHooks {

    @Autowired
    private CompanyProductService companyProductService;

    public boolean checkIfProductIsNotAlreadyUsed(final DataDefinition companyProductDD, final Entity companyProduct) {
        if (!companyProductService.checkIfProductIsNotUsed(companyProduct)) {
            companyProduct.addError(companyProductDD.getField(CompanyProductFields.PRODUCT),
                    "basic.company.message.productIsAlreadyUsed");
            companyProduct.addError(companyProductDD.getField(CompanyProductFields.COMPANY),
                    "basic.company.message.companyIsAlreadyUsed");

            return false;
        }

        return true;
    }

    public boolean checkIfProductHasDefaultSupplier(final DataDefinition companyProductDD, final Entity companyProduct) {
        if (companyProductService.checkIfDefaultAlreadyExists(companyProduct)) {
            companyProduct.addError(companyProductDD.getField(CompanyProductFields.IS_DEFAULT),
                    "basic.company.message.defaultAlreadyExistsForProduct");

            return false;
        }

        return true;
    }

    public void onSave(final DataDefinition companyProductDD, final Entity companyProduct) {
        Entity product = companyProduct.getBelongsToField(CompanyProductFields.PRODUCT);
        Entity company = companyProduct.getBelongsToField(CompanyProductFields.COMPANY);
        boolean isDefault = companyProduct.getBooleanField(CompanyProductFields.IS_DEFAULT);

        if (Objects.isNull(companyProduct.getId())) {
            if (isDefault) {
                Entity supplier = product.getBelongsToField(ProductFields.SUPPLIER);

                if (Objects.isNull(supplier) || !supplier.getId().equals(company.getId())) {
                    product.setField(ProductFields.SUPPLIER, company);

                    product.getDataDefinition().fastSave(product);
                }
            }
        } else {
            Entity companyProductFromDb = companyProductDD.get(companyProduct.getId());

            if (isDefault != companyProductFromDb.getBooleanField(CompanyProductFields.IS_DEFAULT)) {
                product.setField(ProductFields.SUPPLIER, isDefault ? company : null);

                product.getDataDefinition().fastSave(product);
            }
        }
    }

    public boolean onDelete(final DataDefinition companyProductDD, final Entity companyProduct) {
        Entity product = companyProduct.getBelongsToField(CompanyProductFields.PRODUCT);
        boolean isDefault = companyProduct.getBooleanField(CompanyProductFields.IS_DEFAULT);

        if (isDefault) {
            product.setField(ProductFields.SUPPLIER, null);

            product.getDataDefinition().fastSave(product);
        }

        return true;
    }

}