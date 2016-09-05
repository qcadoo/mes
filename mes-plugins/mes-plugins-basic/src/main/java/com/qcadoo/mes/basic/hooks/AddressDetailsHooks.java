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
package com.qcadoo.mes.basic.hooks;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.BasicService;
import com.qcadoo.mes.basic.constants.AddressFields;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class AddressDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_ACTIONS = "actions";

    @Autowired
    private BasicService basicService;

    public void beforeRender(final ViewDefinitionState view) {
        updateRibbonState(view);
        updateFormState(view);
        fillNumber(view);
    }

    private void updateRibbonState(final ViewDefinitionState view) {
        FormComponent addressForm = (FormComponent) view.getComponentByReference(L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);

        RibbonGroup ribbonGroup = window.getRibbon().getGroupByName(L_ACTIONS);

        Entity address = addressForm.getPersistedEntityWithIncludedFormValues();

        String addressType = address.getStringField(AddressFields.ADDRESS_TYPE);

        boolean isEnabled = ((address.getId() == null) || !basicService.checkIfIsMainAddressType(addressType));

        ribbonGroup.getItems().stream().forEach(ribbonActionItem -> {
            ribbonActionItem.setEnabled(isEnabled);
            ribbonActionItem.requestUpdate(true);
        });
    }

    private void updateFormState(final ViewDefinitionState view) {
        FormComponent addressForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity address = addressForm.getPersistedEntityWithIncludedFormValues();

        String addressType = address.getStringField(AddressFields.ADDRESS_TYPE);

        boolean isEnabled = ((address.getId() == null) || !basicService.checkIfIsMainAddressType(addressType));

        addressForm.setEnabled(isEnabled);
        addressForm.setFormEnabled(isEnabled);
    }

    private void fillNumber(final ViewDefinitionState view) {
        FormComponent addressForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent numberField = (FieldComponent) view.getComponentByReference(AddressFields.NUMBER);

        Entity address = addressForm.getPersistedEntityWithIncludedFormValues();

        String number = address.getStringField(AddressFields.NUMBER);

        if ((address.getId() == null) && StringUtils.isEmpty(number)) {
            Entity company = address.getBelongsToField(AddressFields.COMPANY);

            numberField.setFieldValue(company.getStringField(CompanyFields.NUMBER) + "-"
                    + basicService.getAddressesNumber(company));
            numberField.requestComponentUpdateState();
        }
    }

}
