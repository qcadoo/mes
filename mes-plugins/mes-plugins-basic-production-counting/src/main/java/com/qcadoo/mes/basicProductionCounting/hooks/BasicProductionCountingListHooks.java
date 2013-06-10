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
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class BasicProductionCountingListHooks {

    private static final String L_WINDOW = "window";

    private static final String L_GRID = "grid";

    private static final String L_PRODUCTION_COUNTING = "productionCounting";

    private static final String L_SHOW_DETAILED_PRODUCTION_COUNTING_AND_PROGRESS = "showDetailedProductionCountingAndProgress";

    private static final String L_SHOW_DETAILED_PRODUCTION_COUNTING = "showDetailedProductionCounting";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void updateRibbonState(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(ORDER);

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonGroup technologies = (RibbonGroup) window.getRibbon().getGroupByName(L_PRODUCTION_COUNTING);

        RibbonActionItem showDetailedProductionCountingAndProgress = (RibbonActionItem) technologies
                .getItemByName(L_SHOW_DETAILED_PRODUCTION_COUNTING_AND_PROGRESS);
        RibbonActionItem showDetailedProductionCounting = (RibbonActionItem) technologies
                .getItemByName(L_SHOW_DETAILED_PRODUCTION_COUNTING);

        Long orderId = orderForm.getEntityId();
        if (orderId == null) {
            return;
        }

        Entity order = getOrderFromDB(orderId);

        boolean isSaved = (order != null);
        boolean isForEach = false;

        if ("03forEach".equals(order.getStringField("typeOfProductionRecording"))) {
            isForEach = true;
        }

        updateButtonState(showDetailedProductionCountingAndProgress, isSaved && isForEach);
        updateButtonState(showDetailedProductionCounting, isSaved && !isForEach);
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

    public void setGridEditableDependsOfOrderState(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(ORDER);
        GridComponent grid = (GridComponent) view.getComponentByReference(L_GRID);

        Long orderId = orderForm.getEntityId();
        if (orderId == null) {
            return;
        }

        Entity order = getOrderFromDB(orderId);

        if (order == null) {
            return;
        }

        String orderState = order.getStringField(STATE);

        if (OrderStateStringValues.ACCEPTED.equals(orderState) || OrderStateStringValues.IN_PROGRESS.equals(orderState)
                || OrderStateStringValues.INTERRUPTED.equals(orderState)) {
            grid.setEditable(true);
        } else {
            grid.setEditable(false);
        }
    }

    private Entity getOrderFromDB(Long orderId) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);
    }

}
