/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.materialFlowResources.validators;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.qcadoo.model.api.search.SearchOrders.asc;
import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.rowCount;

@Service
public class ProductValidatorsMFR {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity entity) {
        boolean result = true;
        if (entity.getBooleanField(ProductFields.BATCH_EVIDENCE) && resourceWithoutValueExists(entity, ResourceFields.BATCH)) {
            entity.addError(dataDefinition.getField(ProductFields.BATCH_EVIDENCE),
                    "basic.product.error.resourceWithoutBatchExists");
            result = false;
        }

        if (entity.getBooleanField(ProductFields.EXPIRATION_DATE_EVIDENCE) && resourceWithoutValueExists(entity, ResourceFields.EXPIRATION_DATE)) {
            entity.addError(dataDefinition.getField(ProductFields.EXPIRATION_DATE_EVIDENCE),
                    "basic.product.error.resourceWithoutExpirationDateExists");
            result = false;
        }
        return result;
    }

    public boolean resourceWithoutValueExists(Entity entity, String field) {
        if (entity.getId() == null) {
            return true;
        }

        SearchCriteriaBuilder scb = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE).find();
        scb.add(SearchRestrictions.isNull(field));
        scb.add(SearchRestrictions.belongsTo(ResourceFields.PRODUCT, entity));
        scb.setProjection(alias(rowCount(), "cnt"));
        scb.addOrder(asc("cnt"));

        Entity countProjection = scb.setMaxResults(1).uniqueResult();

        return ((Long) countProjection.getField("cnt")).compareTo(0L) != 0;
    }


}
