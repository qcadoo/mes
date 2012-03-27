/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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
package com.qcadoo.mes.orders.listeners;

import static com.qcadoo.mes.orders.constants.OrderFields.NUMBER;
import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;
import static com.qcadoo.mes.orders.constants.OrdersConstants.PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.technologies.constants.TechnologyState.CHECKED;
import static com.qcadoo.mes.technologies.constants.TechnologyState.DRAFT;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.technologies.constants.TechnologyState;
import com.qcadoo.mes.technologies.states.MessageHolder;
import com.qcadoo.mes.technologies.states.TechnologyStateAfterChangeNotifierService.AfterStateChangeListener;
import com.qcadoo.mes.technologies.states.TechnologyStateUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;

@Component
public class TechnologyStateListeners implements AfterStateChangeListener {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public void wasChanged(final ComponentState state, final Entity technology, final TechnologyState oldState) {
        Entity existsTechnology = technology.getDataDefinition().get(technology.getId());
        TechnologyState existsState = TechnologyStateUtils.getStateFromField(existsTechnology.getStringField(STATE));
        if (oldState.equals(CHECKED) && existsState.equals(DRAFT)) {
            changedStateFromCheckedToDraft(state, existsTechnology);
        }
        if (existsState.equals(TechnologyState.CHECKED)) {
            state.addMessage("orders.order.technology.info.aboutChecked", MessageType.INFO, false);
        }
    }

    private void changedStateFromCheckedToDraft(final ComponentState state, final Entity technology) {
        MessageHolder message = deleteCheckedTechnologyFromOrder(technology);
        if (message != null) {
            state.addMessage(message.getMessageKey(), message.getMessageType(), false);
        }
    }

    private MessageHolder deleteCheckedTechnologyFromOrder(final Entity technology) {
        DataDefinition orderDD = dataDefinitionService.get(PLUGIN_IDENTIFIER, MODEL_ORDER);
        List<Entity> ordersList = orderDD.find().add(SearchRestrictions.belongsTo(TECHNOLOGY, technology)).list().getEntities();
        StringBuilder ordersNumberList = new StringBuilder();
        for (Entity order : ordersList) {
            order.setField(TECHNOLOGY, null);
            orderDD.save(order);
            ordersNumberList.append(" {");
            ordersNumberList.append(order.getStringField(NUMBER));
            ordersNumberList.append("}");
        }
        if (ordersList.isEmpty()) {
            return null;
        }
        return MessageHolder.info("orders.order.technology.removed", ordersNumberList.toString());
    }

}
