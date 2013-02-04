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
package com.qcadoo.mes.technologies.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class ProductDetailsViewHooksT {

    private static final String L_FORM = "form";

    // TODO lupo fix when problem with navigation will be done
    public void updateRibbonState(final ViewDefinitionState view) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(L_FORM);
        Entity product = productForm.getEntity();

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonGroup technologies = (RibbonGroup) window.getRibbon().getGroupByName("technologies");

        // RibbonActionItem addTechnologyGroup = (RibbonActionItem) technologies.getItemByName("addTechnologyGroup");
        RibbonActionItem showTechnologiesWithTechnologyGroup = (RibbonActionItem) technologies
                .getItemByName("showTechnologiesWithTechnologyGroup");
        RibbonActionItem showTechnologiesWithProduct = (RibbonActionItem) technologies
                .getItemByName("showTechnologiesWithProduct");

        if (product.getId() != null) {
            // updateButtonState(addTechnologyGroup, true);

            Entity technologyGroup = product.getBelongsToField("technologyGroup");

            if (technologyGroup == null) {
                updateButtonState(showTechnologiesWithTechnologyGroup, false);
            } else {
                updateButtonState(showTechnologiesWithTechnologyGroup, true);
            }

            updateButtonState(showTechnologiesWithProduct, true);

            return;
        }

        // updateButtonState(addTechnologyGroup, false);
        updateButtonState(showTechnologiesWithTechnologyGroup, false);
        updateButtonState(showTechnologiesWithProduct, false);
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

}
