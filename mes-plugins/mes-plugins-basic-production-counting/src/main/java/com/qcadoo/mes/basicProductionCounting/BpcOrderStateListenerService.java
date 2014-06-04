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
package com.qcadoo.mes.basicProductionCounting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.model.api.Entity;

@Service
public class BpcOrderStateListenerService {

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    public void onAccept(final StateChangeContext stateChangeContext) {
        final Entity order = stateChangeContext.getOwner();
        final Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (technology == null) {
            stateChangeContext.addValidationError("orders.order.technology.isEmpty");
        } else {
            basicProductionCountingService.createProductionCountingQuantitiesAndOperationRuns(order);
            basicProductionCountingService.createBasicProductionCountings(order);
            basicProductionCountingService.associateProductionCountingQuantitiesWithBasicProductionCountings(order);
        }
    }

}