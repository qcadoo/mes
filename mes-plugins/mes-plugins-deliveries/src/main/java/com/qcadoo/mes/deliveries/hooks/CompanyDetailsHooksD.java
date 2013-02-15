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
package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.CompanyFieldsD.BUFFER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.CompanyService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class CompanyDetailsHooksD {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_SUPPLIERS = "suppliers";

    private static final String L_REDIRECT_TO_FILTERED_DELIVERIES_LIST = "redirectToFilteredDeliveriesList";

    @Autowired
    private CompanyService companyService;

    public void disabledGridWhenCompanyIsOwner(final ViewDefinitionState view) {
        companyService.disabledGridWhenCompanyIsOwner(view, "productsFamilies", "products");
    }

    public void disableBufferWhenCompanyIsOwner(final ViewDefinitionState view) {
        FormComponent companyForm = (FormComponent) view.getComponentByReference(L_FORM);
        Boolean isOwner = companyService.isCompanyOwner(companyForm.getEntity());

        FieldComponent buffer = (FieldComponent) view.getComponentByReference(BUFFER);

        buffer.setEnabled(!isOwner);
    }

    public void updateRibbonState(final ViewDefinitionState view) {
        FormComponent companyForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity company = companyForm.getEntity();

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);

        RibbonGroup suppliers = window.getRibbon().getGroupByName(L_SUPPLIERS);

        RibbonActionItem redirectToFilteredDeliveriesList = suppliers.getItemByName(L_REDIRECT_TO_FILTERED_DELIVERIES_LIST);

        if (company.getId() == null) {
            updateButtonState(redirectToFilteredDeliveriesList, false);
        } else {
            updateButtonState(redirectToFilteredDeliveriesList, true);
        }
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

}
