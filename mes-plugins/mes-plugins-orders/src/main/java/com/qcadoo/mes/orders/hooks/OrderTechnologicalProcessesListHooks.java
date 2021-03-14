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

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OrderTechnologicalProcessesListHooks {

    private static final String L_ORDER_TECHNOLOGICAL_PROCESS_WASTE = "orderTechnologicalProcessWaste";

    private static final String L_CREATE_ORDER_TECHNOLOGICAL_PROCESS_WASTE = "createOrderTechnologicalProcessWaste";

    public final void onBeforeRender(final ViewDefinitionState view) {
        updateRibbonState(view);
    }

    private void updateRibbonState(final ViewDefinitionState view) {
        GridComponent orderTechnologicalProcessesGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup orderTechnologicalProcessWasteGroup = window.getRibbon().getGroupByName(L_ORDER_TECHNOLOGICAL_PROCESS_WASTE);
        RibbonActionItem createOrderTechnologicalProcessWasteActionItem = orderTechnologicalProcessWasteGroup
                .getItemByName(L_CREATE_ORDER_TECHNOLOGICAL_PROCESS_WASTE);

        boolean isOrderTechnologicalProcessSelected = orderTechnologicalProcessesGrid.getSelectedEntities().size() == 1;

        createOrderTechnologicalProcessWasteActionItem.setEnabled(isOrderTechnologicalProcessSelected);
        createOrderTechnologicalProcessWasteActionItem.requestUpdate(true);
    }

}
