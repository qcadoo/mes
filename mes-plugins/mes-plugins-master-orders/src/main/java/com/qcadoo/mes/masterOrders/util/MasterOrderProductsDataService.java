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
package com.qcadoo.mes.masterOrders.util;

import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.id;
import static com.qcadoo.model.api.search.SearchRestrictions.belongsTo;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityOpResult;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.utils.EntityUtils;

@Service
public class MasterOrderProductsDataService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Entity createProductEntryFor(final Entity masterOrder) {
        Entity product = masterOrder.getBelongsToField(MasterOrderFields.PRODUCT);
        Entity technology = masterOrder.getBelongsToField(MasterOrderFields.TECHNOLOGY);

        Entity masterOrderProduct = getMasterOrderProductDD().create();

        masterOrderProduct.setField(MasterOrderProductFields.PRODUCT, product);
        masterOrderProduct.setField(MasterOrderProductFields.TECHNOLOGY, technology);
        return masterOrderProduct;
    }

    public EntityOpResult deleteExistingMasterOrderProducts(final Entity masterOrder) {
        if (masterOrder == null || masterOrder.getId() == null) {
            return EntityOpResult.successfull();
        }
        Collection<Long> belongingProductIds = findBelongingProductIds(masterOrder);
        if (belongingProductIds.isEmpty()) {
            return EntityOpResult.successfull();
        }
        return getMasterOrderProductDD().delete(belongingProductIds.toArray(new Long[] {}));
    }

    private Collection<Long> findBelongingProductIds(final Entity masterOrder) {
        SearchCriteriaBuilder scb = getMasterOrderProductDD().find();
        scb.add(belongsTo(MasterOrderProductFields.MASTER_ORDER, masterOrder));
        scb.setProjection(alias(id(), "id"));
        return EntityUtils.getFieldsView(scb.list().getEntities(), "id");
    }

    private DataDefinition getMasterOrderProductDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_MASTER_ORDER_PRODUCT);
    }

}
