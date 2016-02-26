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
package com.qcadoo.mes.warehouseMinimalState.hooks;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import org.springframework.stereotype.Service;

@Service
public class WarehouseMinimumStateAddMulti {

    private static final String L_WINDOW = "window";

    public void onBeforeRender(final ViewDefinitionState view) {
        LookupComponent location = (LookupComponent) view.getComponentByReference("location");
        location.setRequired(true);
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonActionItem addMultiButton = window.getRibbon().getGroupByName("action").getItemByName("createMultiMinimalStates");
        addMultiButton.setMessage("warehouseMinimalState.warehouseMinimumStateAddMulti.createMultiMinimalStates.button.message");
        addMultiButton.requestUpdate(true);
        window.requestRibbonRender();
    }
}
