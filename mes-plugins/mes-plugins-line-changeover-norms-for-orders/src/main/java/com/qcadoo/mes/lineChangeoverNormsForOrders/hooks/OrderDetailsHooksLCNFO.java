/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.lineChangeoverNormsForOrders.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class OrderDetailsHooksLCNFO {

    public void onBeforeRender(final ViewDefinitionState view) {
        enableOrDisableChangeoverButton(view);
    }

    private void enableOrDisableChangeoverButton(final ViewDefinitionState view) {
        boolean hasPatternTechnology = hasDefinedPatternTechnology(view);
        setChangeoverButtonEnabled(view, hasPatternTechnology);
    }

    private boolean hasDefinedPatternTechnology(ViewDefinitionState view) {
        ComponentState orderTypeSelect = view.getComponentByReference("orderType");
        LookupComponent technologyPrototypeLookup = (LookupComponent) view.getComponentByReference("technologyPrototype");
        if (technologyPrototypeLookup == null || orderTypeSelect == null || orderTypeSelect.getFieldValue() == null) {
            return false;
        }

        OrderType type = OrderType.parseString((String) orderTypeSelect.getFieldValue());
        return type == OrderType.WITH_PATTERN_TECHNOLOGY && !technologyPrototypeLookup.isEmpty();
    }

    private void setChangeoverButtonEnabled(final ViewDefinitionState view, final boolean enabled) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        if (window == null) {
            return;
        }
        Ribbon ribbon = window.getRibbon();
        RibbonGroup changeoverGroup = ribbon.getGroupByName("changeover");
        if (changeoverGroup == null) {
            return;
        }
        RibbonActionItem showChangeoverButton = changeoverGroup.getItemByName("showChangeover");
        if (showChangeoverButton == null) {
            return;
        }

        showChangeoverButton.setEnabled(enabled);
        showChangeoverButton.requestUpdate(true);
        window.requestRibbonRender();
    }

}
