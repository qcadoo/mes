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
package com.qcadoo.mes.basic.hooks;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.CompanyService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class CompanyDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_ORDER_PRODUCTION = "orderProduction";

    private static final String L_REDIRECT_TO_FILTERED_ORDER_PRODUCTION_LIST = "redirectToFilteredOrderProductionList";

    private static final String L_DELETE = "delete";

    private static final String L_ACTIONS = "actions";

    private static final List<String> L_COMPANY_FIELDS = Arrays.asList(CompanyFields.NUMBER, CompanyFields.NAME,
            CompanyFields.CITY, CompanyFields.COUNTRY, CompanyFields.EMAIL, CompanyFields.HOUSE, CompanyFields.FLAT,
            CompanyFields.PHONE, CompanyFields.ZIP_CODE, CompanyFields.WEBSITE, CompanyFields.TAX_COUNTRY_CODE,
            CompanyFields.TAX, CompanyFields.STREET, CompanyFields.STATE);

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void updateRibbonState(final ViewDefinitionState view) {
        disabledRedirectToFilteredOrderProductionListButton(view);
        disabledRibbonForOwnerOrExternal(view);
    }

    private void disabledRedirectToFilteredOrderProductionListButton(final ViewDefinitionState view) {
        FormComponent companyForm = (FormComponent) view.getComponentByReference(L_FORM);
        Entity company = companyForm.getEntity();

        boolean isEnabled = (company.getId() != null);

        companyService.disableButton(view, L_ORDER_PRODUCTION, L_REDIRECT_TO_FILTERED_ORDER_PRODUCTION_LIST, isEnabled, null);
    }

    private void disabledRibbonForOwnerOrExternal(final ViewDefinitionState view) {
        FormComponent companyForm = (FormComponent) view.getComponentByReference(L_FORM);
        Entity company = companyForm.getEntity();

        Boolean isOwner = companyService.isCompanyOwner(companyForm.getEntity());

        boolean isEnabled = !isOwner;

        String buttonMessage = "basic.company.isOwner";

        if ((company != null) && !StringUtils.isEmpty(company.getStringField(CompanyFields.EXTERNAL_NUMBER))) {
            buttonMessage = "basic.company.isExternalNumber";

            isEnabled = false;
        }

        companyService.disableButton(view, L_ACTIONS, L_DELETE, isEnabled, buttonMessage);
    }

    public void generateCompanyNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY,
                L_FORM, CompanyFields.NUMBER);
    }

    public void fillDefaultCountry(final ViewDefinitionState view) {
        FormComponent companyForm = (FormComponent) view.getComponentByReference(L_FORM);

        if (companyForm.getEntityId() != null) {
            return;
        }

        LookupComponent countryField = (LookupComponent) view.getComponentByReference(CompanyFields.COUNTRY);
        LookupComponent taxCountryField = (LookupComponent) view.getComponentByReference(CompanyFields.TAX_COUNTRY_CODE);

        Entity country = countryField.getEntity();

        if (country == null) {
            Entity defaultCountry = parameterService.getParameter().getBelongsToField(CompanyFields.COUNTRY);

            if (defaultCountry == null) {
                countryField.setFieldValue(null);
            } else {
                countryField.setFieldValue(defaultCountry.getId());
                taxCountryField.setFieldValue(defaultCountry.getId());
            }
            taxCountryField.requestComponentUpdateState();
            countryField.requestComponentUpdateState();
        }
    }

    public void disabledFieldsForExternalCompany(final ViewDefinitionState view) {
        FormComponent companyForm = (FormComponent) view.getComponentByReference(L_FORM);

        if (companyForm.getEntityId() == null) {
            companyForm.setFormEnabled(true);

            return;
        }

        Entity company = companyForm.getEntity();

        if (!StringUtils.isEmpty(company.getStringField(CompanyFields.EXTERNAL_NUMBER))) {
            for (String fieldName : L_COMPANY_FIELDS) {
                disabledField(view, fieldName);
            }
        } else {
            companyForm.setFormEnabled(true);
        }
    }

    private void disabledField(final ViewDefinitionState view, final String reference) {
        FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(reference);

        fieldComponent.setEnabled(false);
        fieldComponent.requestComponentUpdateState();
    }

}
