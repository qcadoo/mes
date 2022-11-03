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

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.constants.CompanyProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.ProductFieldsD;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductHooksD {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(final DataDefinition dataDefinition, final Entity product) {

        updateDefaultSupplier(dataDefinition, product);
    }

    private void updateDefaultSupplier(final DataDefinition productDD, final Entity product) {
        Long productId = product.getId();
        Entity company = product.getBelongsToField(ProductFields.SUPPLIER);

        if (Objects.isNull(productId)) {
            if (Objects.nonNull(company)) {
                createDefaultProductCompany(product, company);
            }
        } else {
            if (Objects.nonNull(company)) {
                createOrUpdateCompanyProduct(product, company);
            } else {
                unmarkDefaultSupplierForProduct(product);
            }
        }
    }

    private void createOrUpdateCompanyProduct(final Entity product, final Entity company) {
        boolean isNewProduct = Objects.isNull(product.getId());

        if (isNewProduct || isSupplierChanged(product, company)) {
            unmarkDefaultSupplierForProduct(product);

            Entity productCompany = getCompanyProductDD().find()
                    .add(SearchRestrictions.belongsTo(CompanyProductFields.PRODUCT, product))
                    .add(SearchRestrictions.belongsTo(CompanyProductFields.COMPANY, company)).setMaxResults(1).uniqueResult();

            if (Objects.isNull(productCompany)) {
                productCompany = getCompanyProductDD().create();
                productCompany.setField(CompanyProductFields.COMPANY, company);
                productCompany.setField(CompanyProductFields.PRODUCT, product);
            }

            productCompany.setField(CompanyProductFields.IS_DEFAULT, Boolean.TRUE);

            productCompany.getDataDefinition().save(productCompany);
        }
    }

    private boolean isSupplierChanged(final Entity product, final Entity company) {
        Entity productFromDB = product.getDataDefinition().get(product.getId());

        Entity supplier = productFromDB.getBelongsToField(ProductFields.SUPPLIER);

        if (Objects.isNull(company)) {
            return false;
        }

        if (Objects.isNull(supplier)) {
            return true;
        }

        return !company.getId().equals(supplier.getId());
    }

    private void unmarkDefaultSupplierForProduct(final Entity product) {
        Entity defaultProductCompany = getCompanyProductDD().find()
                .add(SearchRestrictions.belongsTo(CompanyProductFields.PRODUCT, product))
                .add(SearchRestrictions.eq(CompanyProductFields.IS_DEFAULT, Boolean.TRUE)).setMaxResults(1).uniqueResult();

        if (Objects.nonNull(defaultProductCompany)) {
            defaultProductCompany.setField(CompanyProductFields.IS_DEFAULT, Boolean.FALSE);
            defaultProductCompany.getDataDefinition().save(defaultProductCompany);
        }

    }

    private void createDefaultProductCompany(final Entity product, final Entity company) {
        Entity productCompany = getCompanyProductDD().create();

        productCompany.setField(CompanyProductFields.COMPANY, company);
        productCompany.setField(CompanyProductFields.PRODUCT, product);
        productCompany.setField(CompanyProductFields.IS_DEFAULT, Boolean.TRUE);

        product.setField(ProductFieldsD.PRODUCT_COMPANIES, Lists.newArrayList(productCompany));
    }

    private DataDefinition getCompanyProductDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_COMPANY_PRODUCT);
    }

}
