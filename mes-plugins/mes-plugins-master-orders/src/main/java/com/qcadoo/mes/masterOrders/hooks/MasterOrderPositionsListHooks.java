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
package com.qcadoo.mes.masterOrders.hooks;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MasterOrderPositionsListHooks {

    private static final String L_ORDERS = "orders";

    private static final String L_CREATE_ORDER = "createOrder";

    private static final String L_DELIVERIES = "deliveries";

    private static final String L_CREATE_DELIVERY = "createDelivery";

    public void disableButton(final ViewDefinitionState view) {
        GridComponent masterOrderPositionComponent = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup ordersRibbonGroup = window.getRibbon().getGroupByName(L_ORDERS);
        RibbonActionItem createOrderRibbonActionItem = ordersRibbonGroup.getItemByName(L_CREATE_ORDER);
        RibbonActionItem generateOrders = ordersRibbonGroup.getItemByName("generateOrders");
        generateOrders.setMessage("qcadooView.ribbon.orders.generateOrders.message");

        List<Entity> selectedEntities = masterOrderPositionComponent.getSelectedEntities();

        generateOrders.setEnabled(!selectedEntities.isEmpty());

        generateOrders.requestUpdate(true);
        boolean isEnabled = (selectedEntities.size() == 1);
        createOrderRibbonActionItem.setEnabled(isEnabled);

        createOrderRibbonActionItem.requestUpdate(true);
        window.requestRibbonRender();
        createOrderRibbonActionItem.setMessage("masterOrders.masterOrder.masterOrdersPosition.lessEntitiesSelectedThanAllowed");
    }

    public void setRibbonEnabled(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        GridComponent grid = (GridComponent) view
                .getComponentByReference(QcadooViewConstants.L_GRID);
        Ribbon ribbon = window.getRibbon();
        RibbonGroup deliveriesRibbonGroup = ribbon.getGroupByName(L_DELIVERIES);
        RibbonActionItem createDeliveryRibbonActionItem = deliveriesRibbonGroup.getItemByName(L_CREATE_DELIVERY);
        createDeliveryRibbonActionItem.setEnabled(!grid.getSelectedEntitiesIds().isEmpty());
        createDeliveryRibbonActionItem.requestUpdate(true);
    }
}
