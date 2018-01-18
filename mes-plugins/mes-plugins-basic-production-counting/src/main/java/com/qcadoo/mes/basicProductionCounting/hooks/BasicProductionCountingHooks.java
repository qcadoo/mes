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
package com.qcadoo.mes.basicProductionCounting.hooks;

import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.list;
import static com.qcadoo.model.api.search.SearchProjections.rowCount;
import static com.qcadoo.model.api.search.SearchProjections.sum;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields;
import com.qcadoo.mes.basicProductionCounting.constants.OrderFieldsBPC;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class BasicProductionCountingHooks {

    private static final String QUANTITIES_SUM_ALIAS = "sum";

    @Autowired
    private NumberService numberService;

    public void onView(final DataDefinition basicProductionCountingDD, final Entity basicProductionCounting) {
        fillPlannedQuantity(basicProductionCounting);
    }

    private void fillPlannedQuantity(final Entity basicProductionCounting) {
        basicProductionCounting.setField(BasicProductionCountingFields.PLANNED_QUANTITY,
                numberService.setScale(getPlannedQuantity(basicProductionCounting)));
    }

    private BigDecimal getPlannedQuantity(final Entity basicProductionCounting) {
        BigDecimal plannedQuantity;

        Entity order = basicProductionCounting.getBelongsToField(BasicProductionCountingFields.ORDER);
        Entity product = basicProductionCounting.getBelongsToField(BasicProductionCountingFields.PRODUCT);

        if (checkIfProductIsOrderFinalProduct(order, product)) {
            plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
        } else {
            Entity entity = order.getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES).find()
                    .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                            ProductionCountingQuantityRole.USED.getStringValue()))
                    .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product))
                    .setProjection(list().add(alias(sum(ProductionCountingQuantityFields.PLANNED_QUANTITY), QUANTITIES_SUM_ALIAS))
                            .add(rowCount()))
                    .addOrder(SearchOrders.asc(QUANTITIES_SUM_ALIAS)).setMaxResults(1).uniqueResult();
            plannedQuantity = BigDecimalUtils.convertNullToZero(entity.getDecimalField(QUANTITIES_SUM_ALIAS));
        }

        return plannedQuantity;
    }

    private boolean checkIfProductIsOrderFinalProduct(final Entity order, final Entity product) {
        Entity orderProduct = order.getBelongsToField(OrderFields.PRODUCT);

        return orderProduct != null && product.getId().equals(orderProduct.getId());
    }

}
