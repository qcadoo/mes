/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.orders.states;

import static com.qcadoo.mes.orders.constants.OrderFields.NUMBER;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;
import static com.qcadoo.mes.orders.constants.OrdersConstants.PLUGIN_IDENTIFIER;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class TechnologyStateChangeListenerService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void deleteCheckedTechnologyFromOrder(final StateChangeContext stateChangeContext) {
        final Entity technology = stateChangeContext.getOwner();
        final DataDefinition orderDD = dataDefinitionService.get(PLUGIN_IDENTIFIER, MODEL_ORDER);
        final List<Entity> ordersList = orderDD.find().add(SearchRestrictions.belongsTo(TECHNOLOGY, technology)).list()
                .getEntities();

        StringBuilder ordersNumberList = new StringBuilder();
        for (Entity order : ordersList) {
            if (ordersNumberList.length() != 0) {
                ordersNumberList.append(", ");
            }
            order.setField(TECHNOLOGY, null);
            orderDD.save(order);
            ordersNumberList.append("{");
            ordersNumberList.append(order.getStringField(NUMBER));
            ordersNumberList.append("}");
        }
        if (!ordersList.isEmpty()) {
            stateChangeContext.addMessage("orders.order.technology.removed", StateMessageType.INFO, ordersNumberList.toString());
        }
    }

}
