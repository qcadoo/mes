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

import java.util.Date;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.OrderTechnologicalProcessService;
import com.qcadoo.mes.orders.constants.OrderTechnologicalProcessFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OrderTechnologicalProcessesListHooks {

    private static final String L_ORDER_TECHNOLOGICAL_PROCESSES = "orderTechnologicalProcesses";

    private static final String L_DIVIDE_ORDER_TECHNOLOGICAL_PROCESS = "divideOrderTechnologicalProcess";

    private static final String L_ORDER_TECHNOLOGICAL_PROCESS_WASTES = "orderTechnologicalProcessWastes";

    private static final String L_CREATE_ORDER_TECHNOLOGICAL_PROCESS_WASTE = "createOrderTechnologicalProcessWaste";

    @Autowired
    private OrderTechnologicalProcessService orderTechnologicalProcessService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        updateRibbonState(view);
    }

    private void updateRibbonState(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup orderTechnologicalProcessesGroup = window.getRibbon().getGroupByName(L_ORDER_TECHNOLOGICAL_PROCESSES);
        RibbonGroup orderTechnologicalProcessWastesGroup = window.getRibbon()
                .getGroupByName(L_ORDER_TECHNOLOGICAL_PROCESS_WASTES);
        RibbonActionItem divideOrderTechnologicalProcessActionItem = orderTechnologicalProcessesGroup
                .getItemByName(L_DIVIDE_ORDER_TECHNOLOGICAL_PROCESS);
        RibbonActionItem createOrderTechnologicalProcessWasteActionItem = orderTechnologicalProcessWastesGroup
                .getItemByName(L_CREATE_ORDER_TECHNOLOGICAL_PROCESS_WASTE);

        GridComponent orderTechnologicalProcessesGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Set<Long> orderTechnologicalProcessesIds = orderTechnologicalProcessesGrid.getSelectedEntitiesIds();

        String message = null;

        boolean isOrderTechnologicalProcessSelected = orderTechnologicalProcessesIds.size() == 1;
        boolean isOrderStateValid = false;
        boolean isOrderTechnologicalProcessFilled = false;

        if (isOrderTechnologicalProcessSelected) {
            Long orderTechnologicalProcessId = orderTechnologicalProcessesIds.stream().findFirst().get();

            Entity orderTechnologicalProcess = orderTechnologicalProcessService
                    .getOrderTechnologicalProcess(orderTechnologicalProcessId);

            if (Objects.nonNull(orderTechnologicalProcess)) {
                Entity order = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.ORDER);
                Date date = orderTechnologicalProcess.getDateField(OrderTechnologicalProcessFields.DATE);
                Entity worker = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.WORKER);

                if (Objects.nonNull(order)) {
                    isOrderStateValid = !orderTechnologicalProcessService.checkOrderState(order);
                }

                if (isOrderStateValid) {
                    isOrderTechnologicalProcessFilled = Objects.nonNull(date) && Objects.nonNull(worker);

                    if (!isOrderTechnologicalProcessFilled) {
                        message = "orders.ribbon.message.orderTechnologicalProcessNotFilled";
                    }
                } else {
                    message = "orders.ribbon.message.canNotCreateOrderTechnologicalProcessWaste";
                }
            }
        }

        divideOrderTechnologicalProcessActionItem.setEnabled(isOrderTechnologicalProcessSelected && isOrderStateValid);
        divideOrderTechnologicalProcessActionItem.requestUpdate(true);
        createOrderTechnologicalProcessWasteActionItem
                .setEnabled(isOrderTechnologicalProcessSelected && isOrderStateValid && isOrderTechnologicalProcessFilled);
        createOrderTechnologicalProcessWasteActionItem.setMessage(message);
        createOrderTechnologicalProcessWasteActionItem.requestUpdate(true);
    }

}
