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
package com.qcadoo.mes.orders.listeners;

import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.client.OrderStateChangeViewClient;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.states.service.client.util.ViewContextHolder;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.TechnologyStateChangeViewClient;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrdersListListeners {

    @Autowired
    private OrderStateChangeViewClient orderStateChangeViewClient;

    @Autowired
    private TechnologyStateChangeViewClient technologyStateChangeViewClient;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    public void generateProductionCounting(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        List<Entity> orders = getOrderDD().find().add(SearchRestrictions.eq(OrderFields.STATE, OrderStateStringValues.PENDING))
                .add(SearchRestrictions.isNotNull(OrderFields.TECHNOLOGY))
                .add(SearchRestrictions.isEmpty("productionCountingQuantities")).setMaxResults(50).list().getEntities();
        if (orders.isEmpty()) {
            view.addMessage("orders.ordersList.info.allOrdersHasGeneratedProductionCounting", ComponentState.MessageType.INFO);
        }
        for (Entity order : orders) {
            order.getDataDefinition().save(order);
        }
        if (!orders.isEmpty()) {
            view.addMessage("orders.ordersList.info.generatedProductionCounting", ComponentState.MessageType.INFO,
                    String.valueOf(orders.size()));
        }

    }

    public void changeState(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent gridComponent = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        for (Long orderId : gridComponent.getSelectedEntitiesIds()) {
            Entity order = getOrderDD().get(orderId);
            order = getOrderDD().save(order);
            if (order.isValid()) {
                Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
                if (TechnologyStateStringValues.DRAFT.equals(technology.getStringField(TechnologyFields.STATE))) {
                    technologyStateChangeViewClient.changeState(new ViewContextHolder(view, state),
                            TechnologyStateStringValues.ACCEPTED, technology);
                } else if (TechnologyStateStringValues.CHECKED.equals(technology.getStringField(TechnologyFields.STATE))) {
                    technologyServiceO.changeTechnologyStateToAccepted(technology);
                }
                orderStateChangeViewClient.changeState(new ViewContextHolder(view, state), args[0], order);
            } else {
                view.addMessage("orders.ordersList.changeState.validationErrors", ComponentState.MessageType.FAILURE, false,
                        order.getStringField(OrderFields.NUMBER));
            }
        }
    }

    private DataDefinition getOrderDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
    }
}
