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
package com.qcadoo.mes.orders.hooks;

import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.OrderTechnologicalProcessService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OrderTechnologicalProcessesSingleOrderListHooks {

    private static final String L_ORDER_TECHNOLOGICAL_PROCESSES = "orderTechnologicalProcesses";

    private static final String L_GENERATE_ORDER_TECHNOLOGICAL_PROCESSES = "generateOrderTechnologicalProcesses";

    private static final String L_DIVIDE_ORDER_TECHNOLOGICAL_PROCESS = "divideOrderTechnologicalProcess";

    @Autowired
    private OrderTechnologicalProcessService orderTechnologicalProcessService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        updateRibbonState(view);
    }

    private void updateRibbonState(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup orderTechnologicalProcessesGroup = window.getRibbon().getGroupByName(L_ORDER_TECHNOLOGICAL_PROCESSES);
        RibbonActionItem generateOrderTechnologicalProcessesActionItem = orderTechnologicalProcessesGroup
                .getItemByName(L_GENERATE_ORDER_TECHNOLOGICAL_PROCESSES);
        RibbonActionItem divideOrderTechnologicalProcessActionItem = orderTechnologicalProcessesGroup
                .getItemByName(L_DIVIDE_ORDER_TECHNOLOGICAL_PROCESS);

        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent orderTechnologicalProcessesGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Set<Long> orderTechnologicalProcessesIds = orderTechnologicalProcessesGrid.getSelectedEntitiesIds();

        boolean isOrderTechnologicalProcessSelected = orderTechnologicalProcessesIds.size() == 1;
        boolean isOrderStateValid = false;

        Entity order = orderForm.getEntity();
        Long orderId = order.getId();

        if (Objects.nonNull(orderId)) {
            order = order.getDataDefinition().get(orderId);

            isOrderStateValid = !orderTechnologicalProcessService.checkOrderState(order);
        }

        generateOrderTechnologicalProcessesActionItem.setEnabled(isOrderStateValid);
        generateOrderTechnologicalProcessesActionItem.requestUpdate(true);
        divideOrderTechnologicalProcessActionItem.setEnabled(isOrderTechnologicalProcessSelected && isOrderStateValid);
        divideOrderTechnologicalProcessActionItem.requestUpdate(true);
    }

}
