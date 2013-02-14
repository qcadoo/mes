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

import static com.qcadoo.mes.basic.constants.CompanyFields.CITY;
import static com.qcadoo.mes.basic.constants.CompanyFields.COUNTRY;
import static com.qcadoo.mes.basic.constants.CompanyFields.EMAIL;
import static com.qcadoo.mes.basic.constants.CompanyFields.FLAT;
import static com.qcadoo.mes.basic.constants.CompanyFields.HOUSE;
import static com.qcadoo.mes.basic.constants.CompanyFields.NAME;
import static com.qcadoo.mes.basic.constants.CompanyFields.NUMBER;
import static com.qcadoo.mes.basic.constants.CompanyFields.PHONE;
import static com.qcadoo.mes.basic.constants.CompanyFields.STATE;
import static com.qcadoo.mes.basic.constants.CompanyFields.STREET;
import static com.qcadoo.mes.basic.constants.CompanyFields.TAX;
import static com.qcadoo.mes.basic.constants.CompanyFields.WEBSITE;
import static com.qcadoo.mes.basic.constants.CompanyFields.ZIP_CODE;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class CompanyDetailsHooks {

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    private static final String L_FORM = "form";

    public void updateRibbonState(final ViewDefinitionState view) {
        disabledRedirectToFilteredOrderProductionListButton(view);
        disabledRibbonForOwner(view);
    }

    private void updateButtonState(final RibbonActionItem ribbonActionItem, final boolean isEnabled) {
        ribbonActionItem.setEnabled(isEnabled);
        ribbonActionItem.requestUpdate(true);
    }

    private void disabledRedirectToFilteredOrderProductionListButton(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);

        Entity company = form.getEntity();

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");

        RibbonGroup productionOrderGroups = (RibbonGroup) window.getRibbon().getGroupByName("orderProduction");

        RibbonActionItem redirectToFilteredOrderProductionList = (RibbonActionItem) productionOrderGroups
                .getItemByName("redirectToFilteredOrderProductionList");

        if (company.getId() == null) {
            updateButtonState(redirectToFilteredOrderProductionList, false);
        } else {
            updateButtonState(redirectToFilteredOrderProductionList, true);
        }
    }

    private void disabledRibbonForOwner(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        if (form.getEntityId() == null) {
            return;
        }
        boolean enabled = true;
        Entity company = form.getEntity();
        Entity parameter = parameterService.getParameter();
        Entity owner = parameter.getBelongsToField(ParameterFields.COMPANY);

        if (company.getId().equals(owner.getId())) {
            enabled = false;
        }

        disabledButton(view, "actions", "delete", enabled, "basic.company.isOwner");
    }

    public void generateCompanyNumber(final ViewDefinitionState state) {
        numberGeneratorService.generateAndInsertNumber(state, BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY,
                L_FORM, "number");
    }

    public void disabledFieldAndRibbonForExternalCompany(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        if (form.getEntityId() == null) {
            return;
        }
        Entity company = form.getEntity();
        if (!StringUtils.isEmpty(company.getStringField(CompanyFields.EXTERNAL_NUMBER))) {
            for (String reference : Arrays.asList(NUMBER, NAME, CITY, COUNTRY, EMAIL, HOUSE, FLAT, PHONE, ZIP_CODE, WEBSITE, TAX,
                    STREET, STATE)) {
                disabledField(view, reference);
            }
            for (String reference : Arrays.asList("delete")) {
                disabledButton(view, "actions", reference, false, "basic.company.isExternalNumber");
            }
        }
    }

    private void disabledField(final ViewDefinitionState view, final String reference) {
        FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
        field.setEnabled(false);
        field.requestComponentUpdateState();
    }

    private void disabledButton(final ViewDefinitionState view, final String groupName, final String buttonName,
            final boolean enabled, final String message) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonGroup actions = (RibbonGroup) window.getRibbon().getGroupByName(groupName);
        RibbonActionItem button = (RibbonActionItem) actions.getItemByName(buttonName);
        button.setEnabled(enabled);
        if (enabled) {
            button.setMessage(null);
        } else {
            button.setMessage(message);
        }
        button.requestUpdate(true);
    }
}
