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
package com.qcadoo.mes.technologies.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.constants.QcadooSecurityConstants;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class ProductDetailsViewHooksT {

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    // TODO lupo fix when problem with navigation will be done
    public void updateRibbonState(final ViewDefinitionState view) {
        Entity loggedUser = dataDefinitionService
                .get(QcadooSecurityConstants.PLUGIN_IDENTIFIER, QcadooSecurityConstants.MODEL_USER)
                .get(securityService.getCurrentUserId());

        if (!securityService.hasRole(loggedUser, "ROLE_TECHNOLOGIES")) {
            view.getComponentByReference("technologyTab").setVisible(false);
        }
        FormComponent productForm = (FormComponent) view.getComponentByReference(L_FORM);
        Entity product = productForm.getEntity();

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonGroup technologies = (RibbonGroup) window.getRibbon().getGroupByName("technologies");

        RibbonActionItem showTechnologiesWithTechnologyGroup = (RibbonActionItem) technologies
                .getItemByName("showTechnologiesWithTechnologyGroup");
        RibbonActionItem showTechnologiesWithProduct = (RibbonActionItem) technologies
                .getItemByName("showTechnologiesWithProduct");

        if (product.getId() != null) {

            Entity technologyGroup = product.getBelongsToField("technologyGroup");

            if (technologyGroup == null) {
                updateButtonState(showTechnologiesWithTechnologyGroup, false);
            } else {
                updateButtonState(showTechnologiesWithTechnologyGroup, true);
            }

            updateButtonState(showTechnologiesWithProduct, true);

            return;
        }

        updateButtonState(showTechnologiesWithTechnologyGroup, false);
        updateButtonState(showTechnologiesWithProduct, false);
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

}
