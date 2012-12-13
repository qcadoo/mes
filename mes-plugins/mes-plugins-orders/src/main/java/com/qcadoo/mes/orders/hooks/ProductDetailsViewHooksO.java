/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class ProductDetailsViewHooksO {

    private static final String L_FORM = "form";

    public void updateRibbonState(final ViewDefinitionState view) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(L_FORM);
        Entity product = productForm.getEntity();

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonGroup orders = (RibbonGroup) window.getRibbon().getGroupByName("orders");

        RibbonActionItem showOrdersWithProductMain = (RibbonActionItem) orders.getItemByName("showOrdersWithProductMain");
        RibbonActionItem showOrdersWithProductPlanned = (RibbonActionItem) orders.getItemByName("showOrdersWithProductPlanned");

        if (product.getId() != null) {
            updateButtonState(showOrdersWithProductMain, true);
            updateButtonState(showOrdersWithProductPlanned, true);

            return;
        }

        updateButtonState(showOrdersWithProductMain, false);
        updateButtonState(showOrdersWithProductPlanned, false);
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

}
