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
package com.qcadoo.mes.masterOrders.hooks;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderType;
import com.qcadoo.mes.masterOrders.util.MasterOrderOrdersDataProvider;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class MasterOrderProductHooks {

    @Autowired
    private MasterOrderOrdersDataProvider masterOrderOrdersDataProvider;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean onDelete(final DataDefinition masterOrderProductDD, final Entity masterOrderProduct) {
        return checkAssignedOrder(masterOrderProduct);
    }

    public void onView(final DataDefinition masterOrderProductDD, final Entity masterOrderProduct) {
        countCumulativeOrderQuantity(masterOrderProduct);
        fillRegisteredQuantity(masterOrderProduct);

    }

    private void fillRegisteredQuantity(Entity masterOrderProduct) {
        BigDecimal doneQuantity = masterOrderOrdersDataProvider.sumBelongingOrdersDoneQuantities(
                masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER),
                masterOrderProduct.getBelongsToField(MasterOrderFields.PRODUCT));
        masterOrderProduct.setField("producedOrderQuantity", doneQuantity);

    }

    private BigDecimal getRegisteredQuantity(final Entity masterOrderProduct) {
        String sqlQuery = "select sum(o.doneQuantity) as sumOfRegistered \n"
                + "                from #orders_order o\n"
                + "               left join o.masterOrder as master\n"
                + "               where o.product.id=:productId and master.id=:masterOrderId";

        List<Entity> entities = getOrderDD().find(sqlQuery)
                .setLong("productId", masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT).getId())
                .setLong("masterOrderId", masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER).getId())
                .list().getEntities();
        for (Entity entity : entities) {
            return BigDecimalUtils.convertNullToZero(entity.getDecimalField("sumOfRegistered"));
        }
        return BigDecimal.ZERO;
    }

    private DataDefinition getOrderDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                OrdersConstants.MODEL_ORDER);
    }

    private void countCumulativeOrderQuantity(final Entity masterOrderProduct) {
        if (masterOrderProduct.getId() == null) {
            return;
        }
        BigDecimal cumulativeQuantity = masterOrderOrdersDataProvider.sumBelongingOrdersPlannedQuantities(
                masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER),
                masterOrderProduct.getBelongsToField(MasterOrderFields.PRODUCT));
        masterOrderProduct.setField(MasterOrderFields.CUMULATED_ORDER_QUANTITY, cumulativeQuantity);
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
