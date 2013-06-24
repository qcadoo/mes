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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class DetailedProductionCountingAndProgressListHooks {

    private static final String L_ORDER = "order";

    private static final String L_GRID = "grid";

    @Autowired
    private OrderService orderService;

    public void setGridEditableDependsOfOrderState(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_ORDER);
        GridComponent grid = (GridComponent) view.getComponentByReference(L_GRID);

        Long orderId = orderForm.getEntityId();
        if (orderId == null) {
            return;
        }

        Entity order = orderService.getOrder(orderId);

        if (order == null) {
            return;
        }

        String state = order.getStringField(OrderFields.STATE);

        if (OrderStateStringValues.ACCEPTED.equals(state) || OrderStateStringValues.IN_PROGRESS.equals(state)
                || OrderStateStringValues.INTERRUPTED.equals(state)) {
            grid.setEditable(true);
        } else {
            grid.setEditable(false);
        }
    }

}
