/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.OrderFieldsBPC;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class OrderHooksBPC {

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    public void onSave(final DataDefinition orderDD, final Entity order) {
        updateProductionCountingQuantitiesAndOperationRuns(orderDD, order);
    }

    private void updateProductionCountingQuantitiesAndOperationRuns(final DataDefinition orderDD, final Entity order) {
        BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);

        String state = order.getStringField(OrderFields.STATE);

        if (OrderStateStringValues.ACCEPTED.equals(state) || OrderStateStringValues.IN_PROGRESS.equals(state)
                || OrderStateStringValues.INTERRUPTED.equals(state)) {
            if (hasPlannedQuantityChanged(order, plannedQuantity)) {
                basicProductionCountingService.updateProductionCountingQuantitiesAndOperationRuns(order);
            } else {
                if (checkIfProductionCountingQuantitiesAndOperationsRunsAreEmpty(order)) {
                    basicProductionCountingService.createProductionCountingQuantitiesAndOperationRuns(order);
                    basicProductionCountingService.associateProductionCountingQuantitiesWithBasicProductionCountings(order);
                }
            }
        }
    }

    private boolean hasPlannedQuantityChanged(final Entity order, final BigDecimal plannedQuantity) {
        Entity existingOrder = getExistingOrder(order);

        if (existingOrder == null) {
            return false;
        }

        BigDecimal existingOrderPlannedQuantity = existingOrder.getDecimalField(OrderFields.PLANNED_QUANTITY);
        if (existingOrderPlannedQuantity == null) {
            return true;
        }

        return !existingOrderPlannedQuantity.equals(plannedQuantity);
    }

    private Entity getExistingOrder(final Entity order) {
        if (order.getId() == null) {
            return null;
        }
        return order.getDataDefinition().get(order.getId());
    }

    boolean checkIfProductionCountingQuantitiesAndOperationsRunsAreEmpty(final Entity order) {
        List<Entity> productionCountingQuantities = order.getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES);
        List<Entity> productionCountingOperationRuns = order.getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_OPERATION_RUNS);

        return (((productionCountingQuantities == null) || productionCountingQuantities.isEmpty()) && ((productionCountingOperationRuns == null) || productionCountingOperationRuns
                .isEmpty()));
    }

}
