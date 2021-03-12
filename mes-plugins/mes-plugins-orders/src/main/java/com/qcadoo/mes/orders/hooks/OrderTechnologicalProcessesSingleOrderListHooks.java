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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.OrderTechnologicalProcessService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OrderTechnologicalProcessesSingleOrderListHooks {

    private static final String L_ORDER_TECHNOLOGICAL_PROCESSES = "orderTechnologicalProcesses";

    private static final String L_GENERATE_ORDER_TECHNOLOGICAL_PROCESSES = "generateOrderTechnologicalProcesses";

    @Autowired
    private OrderTechnologicalProcessService orderTechnologicalProcessService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity order = orderForm.getEntity();
        Long orderId = order.getId();

        if (Objects.nonNull(orderId)) {
            order = order.getDataDefinition().get(orderId);

            updateRibbonState(view, order);
        }
    }

    private void updateRibbonState(final ViewDefinitionState view, final Entity order) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup orderTechnologicalProcessesGroup = window.getRibbon().getGroupByName(L_ORDER_TECHNOLOGICAL_PROCESSES);
        RibbonActionItem splitOrderTechnologicalProcessActionItem = orderTechnologicalProcessesGroup
                .getItemByName(L_GENERATE_ORDER_TECHNOLOGICAL_PROCESSES);

        boolean isOrderStateValid = !orderTechnologicalProcessService.checkOrderState(order);

        splitOrderTechnologicalProcessActionItem.setEnabled(isOrderStateValid);
        splitOrderTechnologicalProcessActionItem.requestUpdate(true);
    }

    private boolean checkOrderState(final Entity order) {
        if (Objects.nonNull(order)) {
            String state = order.getStringField(OrderFields.STATE);

            return OrderStateStringValues.DECLINED.equals(state) || OrderStateStringValues.COMPLETED.equals(state)
                    || OrderStateStringValues.ABANDONED.equals(state);
        }

        return false;
    }
}
