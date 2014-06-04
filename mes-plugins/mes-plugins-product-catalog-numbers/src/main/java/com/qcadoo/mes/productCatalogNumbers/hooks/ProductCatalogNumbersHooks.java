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
package com.qcadoo.mes.productCatalogNumbers.hooks;

import static com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumberFields.CATALOG_NUMBER;
import static com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumberFields.COMPANY;
import static com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumberFields.PRODUCT;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumbersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductCatalogNumbersHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean checkIfExistsCatalogNumberWithProductAndCompany(final DataDefinition dataDefinition, final Entity entity) {
        SearchCriteriaBuilder criteria = dataDefinitionService
                .get(ProductCatalogNumbersConstants.PLUGIN_IDENTIFIER,
                        ProductCatalogNumbersConstants.MODEL_PRODUCT_CATALOG_NUMBERS).find()
                .add(SearchRestrictions.belongsTo(PRODUCT, entity.getBelongsToField(PRODUCT)))
                .add(SearchRestrictions.belongsTo(COMPANY, entity.getBelongsToField(COMPANY)));
        if (entity.getId() != null) {
            criteria.add(SearchRestrictions.ne("id", entity.getId()));
        }
        List<Entity> catalogsNumbers = criteria.list().getEntities();
        if (catalogsNumbers.isEmpty()) {
            return true;
        } else {
            entity.addGlobalError("productCatalogNumbers.productCatalogNumber.validationError.alreadyExistsProductForCompany");
            return false;
        }
    }

    public boolean checkIfExistsCatalogNumberWithNumberAndCompany(final DataDefinition dataDefinition, final Entity entity) {
        SearchCriteriaBuilder criteria = dataDefinitionService
                .get(ProductCatalogNumbersConstants.PLUGIN_IDENTIFIER,
                        ProductCatalogNumbersConstants.MODEL_PRODUCT_CATALOG_NUMBERS).find()
                .add(SearchRestrictions.eq(CATALOG_NUMBER, entity.getStringField(CATALOG_NUMBER)))
                .add(SearchRestrictions.belongsTo(COMPANY, entity.getBelongsToField(COMPANY)));
        if (entity.getId() != null) {
            criteria.add(SearchRestrictions.ne("id", entity.getId()));
        }
        List<Entity> catalogsNumbers = criteria.list().getEntities();
        if (catalogsNumbers.isEmpty()) {
            return true;
        } else {
            entity.addGlobalError("productCatalogNumbers.productCatalogNumber.validationError.alreadyExistsCatalogNumerForCompany");
            return false;
        }
    }
}
