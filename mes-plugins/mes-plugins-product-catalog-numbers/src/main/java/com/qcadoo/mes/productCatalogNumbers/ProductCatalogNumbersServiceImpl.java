/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.productCatalogNumbers;

import static com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumberFields.COMPANY;
import static com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumberFields.PRODUCT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumbersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductCatalogNumbersServiceImpl implements ProductCatalogNumbersService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public Entity getProductCatalogNumber(final Entity product, final Entity supplier) {
        return dataDefinitionService
                .get(ProductCatalogNumbersConstants.PLUGIN_IDENTIFIER,
                        ProductCatalogNumbersConstants.MODEL_PRODUCT_CATALOG_NUMBERS).find()
                .add(SearchRestrictions.belongsTo(PRODUCT, product)).add(SearchRestrictions.belongsTo(COMPANY, supplier))
                .setMaxResults(1).uniqueResult();
    }

}
