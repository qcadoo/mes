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

import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.ORDER;
import static com.qcadoo.mes.orders.constants.OrderFields.STATE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class BasicProductionCountingListHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void setUneditableGridWhenOrderTypeRecordingIsBasic(final ViewDefinitionState view) {
        FormComponent order = (FormComponent) view.getComponentByReference(ORDER);

        if (order.getEntityId() == null) {
            return;
        }

        Entity orderFromDB = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                order.getEntityId());

        if (orderFromDB == null) {
            return;
        }

        String orderState = orderFromDB.getStringField(STATE);
        String productionRecordType = orderFromDB.getStringField("typeOfProductionRecording");
        GridComponent grid = (GridComponent) view.getComponentByReference("grid");

        if (("01basic".equals(productionRecordType)) && (OrderStateStringValues.IN_PROGRESS.equals(orderState))) {
            grid.setEditable(true);
        } else {
            grid.setEditable(false);
        }
    }

}
