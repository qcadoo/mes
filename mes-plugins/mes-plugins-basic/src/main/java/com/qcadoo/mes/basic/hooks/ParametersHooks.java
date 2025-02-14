/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.basic.hooks;

import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParametersHooks {

    private static final String L_COMPANY = "company";

    private static final String L_REDIRECT_TO_COMPANY = "redirectToCompany";

    @Autowired
    private SecurityService securityService;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent parametersForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        LookupComponent companyLookup = (LookupComponent) view.getComponentByReference(ParameterFields.COMPANY);

        boolean isSaved = (parametersForm.getEntityId() != null);
        boolean isCompany = (companyLookup.getEntity() != null);

        changeButtonsState(view, isSaved && isCompany);
        toggleEmailTab(view);
    }

    private void toggleEmailTab(ViewDefinitionState view) {
        ComponentState emailTab = view.getComponentByReference("emailTab");
        emailTab.setVisible(securityService.hasCurrentUserRole("ROLE_EMAIL_PARAMETERS"));
    }

    private void changeButtonsState(final ViewDefinitionState view, final boolean enabled) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        Ribbon ribbon = window.getRibbon();

        RibbonGroup company = ribbon.getGroupByName(L_COMPANY);

        RibbonActionItem redirectToCompany = company.getItemByName(L_REDIRECT_TO_COMPANY);

        redirectToCompany.setEnabled(enabled);
        redirectToCompany.requestUpdate(true);

        window.requestRibbonRender();
    }

    public void onLicenseBeforeRender(final ViewDefinitionState view) {
        FieldComponent typeTerminalLicenses = (FieldComponent) view.getComponentByReference(ParameterFields.TYPE_TERMINAL_LICENSES);
        FormComponent parametersForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity parameter = parametersForm.getPersistedEntityWithIncludedFormValues();

        typeTerminalLicenses.setEnabled(securityService.hasCurrentUserRole("ROLE_SUPERADMIN")
                && parameter.getField(ParameterFields.NUMBER_TERMINAL_LICENSES) != null
                && NumberUtils.isDigits(parameter.getField(ParameterFields.NUMBER_TERMINAL_LICENSES).toString())
                && parameter.getLongField(ParameterFields.NUMBER_TERMINAL_LICENSES) > 0);
    }

}
