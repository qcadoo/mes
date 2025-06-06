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

import com.qcadoo.mes.basic.CompanyService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CompanyDetailsHooks {

    private static final String L_ORDER_PRODUCTION = "orderProduction";

    private static final String L_REDIRECT_TO_FILTERED_ORDER_PRODUCTION_LIST = "redirectToFilteredOrderProductionList";

    private static final String L_DELETE = "delete";

    private static final String L_ACTIONS = "actions";

    public static final List<String> L_COMPANY_FIELDS = Arrays.asList(CompanyFields.NUMBER, CompanyFields.NAME,
            CompanyFields.TAX_COUNTRY_CODE, CompanyFields.TAX, CompanyFields.PHONE, CompanyFields.EMAIL, CompanyFields.WEBSITE,
            CompanyFields.STREET, CompanyFields.HOUSE, CompanyFields.FLAT, CompanyFields.ZIP_CODE, CompanyFields.CITY,
            CompanyFields.STATE, CompanyFields.COUNTRY, CompanyFields.CONTACT_PERSON, CompanyFields.IS_SUPPLIER,
            CompanyFields.IS_RECEIVER);

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private CompanyService companyService;

    public void updateRibbonState(final ViewDefinitionState view) {
        disabledRedirectToFilteredOrderProductionListButton(view);
        disabledRibbonForOwnerOrExternal(view);
    }

    private void disabledRedirectToFilteredOrderProductionListButton(final ViewDefinitionState view) {
        FormComponent companyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity company = companyForm.getEntity();

        boolean isEnabled = (company.getId() != null);

        companyService.disableButton(view, L_ORDER_PRODUCTION, L_REDIRECT_TO_FILTERED_ORDER_PRODUCTION_LIST, isEnabled, null);
    }

    private void disabledRibbonForOwnerOrExternal(final ViewDefinitionState view) {
        FormComponent companyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
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

    public void fillDefaultCountry(final ViewDefinitionState view) {
        FormComponent companyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (companyForm.getEntityId() != null) {
            return;
        }

        CheckBoxComponent isSetFieldsFromParameter = (CheckBoxComponent) view
                .getComponentByReference(CompanyFields.IS_SET_FIELDS_FROM_PARAMETER);
        if (isSetFieldsFromParameter.isChecked()) {
            return;
        }

        LookupComponent countryField = (LookupComponent) view.getComponentByReference(CompanyFields.COUNTRY);
        LookupComponent taxCountryField = (LookupComponent) view.getComponentByReference(CompanyFields.TAX_COUNTRY_CODE);

        Entity defaultCountry = parameterService.getParameter().getBelongsToField(CompanyFields.COUNTRY);

        if (defaultCountry != null) {
            countryField.setFieldValue(defaultCountry.getId());
            taxCountryField.setFieldValue(defaultCountry.getId());
            taxCountryField.requestComponentUpdateState();
            countryField.requestComponentUpdateState();
        }
        isSetFieldsFromParameter.setFieldValue(true);
        isSetFieldsFromParameter.requestComponentUpdateState();
    }

    public void disabledFieldsForExternalCompany(final ViewDefinitionState view) {
        FormComponent companyForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

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
