/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.ordersForSubproductsGeneration.productionScheduling.listeners;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.qcadoo.mes.operationTimeCalculations.constants.OperationTimeCalculationsConstants;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.operationTimeCalculations.constants.OrderTimeCalculationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OperationDurationDetailsInOrderListenerOFSPG {

    

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Transactional
    public final void saveDatesInSubOrders(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long orderId = form.getEntityId();

        List<Entity> orders = getOrdersForComponent(orderId);
        if (!orders.isEmpty()) {

            List<Entity> ordersTimeCalculations = dataDefinitionService.get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER, OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION)
                    .find().createAlias("order", "ord", JoinType.LEFT)
                    .add(SearchRestrictions.in("ord.id", getOrdersForComponent(orderId).stream().map(entity -> entity.getId()).collect(
                            Collectors.toList()))).list().getEntities();
            Map<Long, Entity> ordersTimeCalculationsByOrder = ordersTimeCalculations
                    .stream()
                    .collect(Collectors.toMap(x -> x.getBelongsToField(OrderTimeCalculationFields.ORDER).getId(), x -> x));
            for (Entity order : orders) {
                order.setField(OrderFields.DATE_FROM, getStartDate(ordersTimeCalculationsByOrder.get(order.getId())));
                order.setField(OrderFields.DATE_TO, getFinishDate(ordersTimeCalculationsByOrder.get(order.getId())));
                order.getDataDefinition().save(order);

            }
            form.addMessage("productionScheduling.info.saveDatesInSubOrdersSuccess", ComponentState.MessageType.SUCCESS);

        } else {
            form.addMessage("productionScheduling.info.saveDatesInSubOrdersNoOrders", ComponentState.MessageType.INFO);

        }
        state.performEvent(view, "reset", new String[0]);
    }

    private Object getFinishDate(Entity entity) {
        return entity.getDateField(OrderTimeCalculationFields.EFFECTIVE_DATE_TO);
    }

    private Object getStartDate(Entity entity) {
        return entity.getDateField(OrderTimeCalculationFields.EFFECTIVE_DATE_FROM);
    }

    private List<Entity> getOrdersForComponent(final Long orderID) {
        List<Long> ids = Lists.newArrayList();
        String sql = "select o from #orders_order as o " + "where o.root=:orderID or o.id=:orderID";
        return getOrderDD().find(sql).setLong("orderID", orderID).list().getEntities();
    }

    private DataDefinition getOrderDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
    }


}
