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

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.orders.OrderTechnologicalProcessService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OrderTechnologicalProcessesSingleOrderListListeners {

    @Autowired
    private OrderTechnologicalProcessService orderTechnologicalProcessService;

    public void generateOrderTechnologicalProcesses(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long orderId = orderForm.getEntityId();

        Entity order = orderForm.getEntity().getDataDefinition().get(orderId);

        boolean isCreated = orderTechnologicalProcessService.generateOrderTechnologicalProcesses(order);

        if (isCreated) {
            view.addMessage("orders.orderTechnologicalProcessesGeneration.success", ComponentState.MessageType.SUCCESS);
        } else {
            view.addMessage("orders.orderTechnologicalProcessesGeneration.error.orderHasTechnologicalProcesses",
                    ComponentState.MessageType.INFO);
        }
    }

    public void divideOrderTechnologicalProcess(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent orderTechnologicalProcessesGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Set<Long> orderTechnologicalProcessesIds = orderTechnologicalProcessesGrid.getSelectedEntitiesIds();

        if (orderTechnologicalProcessesIds.isEmpty()) {
            return;
        }

        Long orderTechnologicalProcessId = orderTechnologicalProcessesIds.stream().findFirst().get();

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", orderTechnologicalProcessId);

        String url = "../page/orders/divideOrderTechnologicalProcess.html";
        view.openModal(url, parameters);
    }

}
