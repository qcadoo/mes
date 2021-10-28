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

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ProductDetailsViewHooksT {

    @Autowired
    private SecurityService securityService;

    // TODO lupo fix when problem with navigation will be done
    public void updateRibbonState(final ViewDefinitionState view) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity product = productForm.getEntity();

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup technologies = window.getRibbon().getGroupByName("technologies");

        RibbonActionItem showTechnologiesWithTechnologyGroup = technologies
                .getItemByName("showTechnologiesWithTechnologyGroup");
        RibbonActionItem showTechnologiesWithProduct = technologies
                .getItemByName("showTechnologiesWithProduct");
        RibbonActionItem showProductGroupTechnologies = technologies
                .getItemByName("showProductGroupTechnologies");
        RibbonActionItem showTechnologiesWithFamilyProduct = technologies
                .getItemByName("showTechnologiesWithFamilyProduct");

        if (product.getId() != null) {
            Entity technologyGroup = product.getBelongsToField("technologyGroup");

            updateButtonState(showTechnologiesWithTechnologyGroup, technologyGroup != null);
            updateButtonState(showTechnologiesWithProduct, true);
            updateButtonState(showProductGroupTechnologies, true);

        } else {
            updateButtonState(showTechnologiesWithTechnologyGroup, false);
            updateButtonState(showTechnologiesWithProduct, false);
            updateButtonState(showProductGroupTechnologies, false);
        }

        Entity parent = product.getBelongsToField(ProductFields.PARENT);

        updateButtonState(showTechnologiesWithFamilyProduct, Objects.nonNull(parent));
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

    public void updateTabsVisible(final ViewDefinitionState view) {
        if (!securityService.hasCurrentUserRole("ROLE_TECHNOLOGIES")) {
            view.getComponentByReference("technologyTab").setVisible(false);
        }
    }

}
