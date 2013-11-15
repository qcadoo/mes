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
package com.qcadoo.mes.basic.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class ParametersHooks {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_COMPANY = "company";

    private static final String L_REDIRECT_TO_COMPANY = "redirectToCompany";

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent parametersForm = (FormComponent) view.getComponentByReference(L_FORM);
        LookupComponent companyLookup = (LookupComponent) view.getComponentByReference(ParameterFields.COMPANY);

        boolean isSaved = (parametersForm.getEntityId() != null);
        boolean isCompany = (companyLookup.getEntity() != null);

        changeButtonsState(view, isSaved && isCompany);
    }

    private void changeButtonsState(final ViewDefinitionState view, final boolean enabled) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);

        Ribbon ribbon = window.getRibbon();

        RibbonGroup company = ribbon.getGroupByName(L_COMPANY);

        RibbonActionItem redirectToCompany = company.getItemByName(L_REDIRECT_TO_COMPANY);

        redirectToCompany.setEnabled(enabled);
        redirectToCompany.requestUpdate(true);

        window.requestRibbonRender();
    }

}
