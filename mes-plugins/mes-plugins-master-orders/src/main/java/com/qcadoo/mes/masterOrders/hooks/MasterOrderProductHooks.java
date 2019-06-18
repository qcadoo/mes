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
package com.qcadoo.mes.masterOrders.hooks;

import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderType;
import com.qcadoo.mes.masterOrders.constants.OrderFieldsMO;
import com.qcadoo.mes.masterOrders.util.MasterOrderOrdersDataProvider;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MasterOrderProductHooks {

    @Autowired
    private MasterOrderOrdersDataProvider masterOrderOrdersDataProvider;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onSave(final DataDefinition masterOrderProductDD, final Entity masterOrderProduct) {
        if (Objects.nonNull(masterOrderProduct.getBelongsToField(MasterOrderProductFields.TECHNOLOGY))) {
            List<Entity> entities = dataDefinitionService
                    .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)
                    .find()
                    .add(SearchRestrictions.belongsTo(OrderFieldsMO.MASTER_ORDER,
                            masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER)))
                    .add(SearchRestrictions.belongsTo(OrderFields.PRODUCT,
                            masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT))).list().getEntities();
            entities = entities
                    .stream()
                    .filter(o -> Objects.nonNull(o.getBelongsToField(OrderFields.TECHNOLOGY))
                            && !o.getBelongsToField(OrderFields.TECHNOLOGY).getId()
                                    .equals(masterOrderProduct.getBelongsToField(MasterOrderProductFields.TECHNOLOGY).getId()))
                    .collect(Collectors.toList());
            if (!entities.isEmpty()) {
                masterOrderProduct
                        .addGlobalMessage("masterOrders.masterOrderProduct.info.needToChangeTechnologyForExistingProductionOrders", false, false);
            }
        }
    }

    public boolean onDelete(final DataDefinition masterOrderProductDD, final Entity masterOrderProduct) {
        return checkAssignedOrder(masterOrderProduct);
    }

    private boolean checkAssignedOrder(final Entity masterOrderProduct) {
        if (masterOrderProduct.getId() == null) {
            return true;
        }
        Entity masterOrder = masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER);
        if (MasterOrderType.of(masterOrder) != MasterOrderType.MANY_PRODUCTS) {
            return true;
        }
        Entity product = masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT);
        long numOfBelongingOrdersMatchingProduct = masterOrderOrdersDataProvider.countBelongingOrders(masterOrder,
                SearchRestrictions.belongsTo(OrderFields.PRODUCT, product));

        if (numOfBelongingOrdersMatchingProduct > 0) {
            masterOrderProduct.addGlobalError("masterOrders.masterOrderProduct.delete.existsAssignedOrder");
            return false;
        }
        return true;
    }
}
