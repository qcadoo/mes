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
        generateNumber(view);
    }

    private void updateRibbonState(final ViewDefinitionState view) {
        FormComponent addressForm = (FormComponent) view.getComponentByReference(L_FORM);

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);

        RibbonGroup ribbonGroup = window.getRibbon().getGroupByName(L_ACTIONS);

        boolean isEnabled = shouldBeEnabled(addressForm);

        ribbonGroup.getItems().stream().forEach(ribbonActionItem -> {
            ribbonActionItem.setEnabled(isEnabled);
            ribbonActionItem.requestUpdate(true);
        });
    }

    private void updateFormState(final ViewDefinitionState view) {
        FormComponent addressForm = (FormComponent) view.getComponentByReference(L_FORM);

        boolean isEnabled = shouldBeEnabled(addressForm);

        addressForm.setEnabled(isEnabled);
        addressForm.setFormEnabled(isEnabled);
    }

    private boolean shouldBeEnabled(final FormComponent addressForm) {
        Entity address = addressForm.getPersistedEntityWithIncludedFormValues();

        Long addressId = address.getId();

        String externalNumber = address.getStringField(AddressFields.EXTERNAL_NUMBER);
        String addressType = getAddressType(address);

        return ((addressId == null) || (StringUtils.isEmpty(externalNumber) && !basicService.checkIfIsMainAddressType(addressType)));
    }

    private String getAddressType(final Entity address) {
        Long addressId = address.getId();

        if (addressId == null) {
            return address.getStringField(AddressFields.ADDRESS_TYPE);
        } else {
            return address.getDataDefinition().get(addressId).getStringField(AddressFields.ADDRESS_TYPE);
        }
    }

    private void generateNumber(final ViewDefinitionState view) {
        FormComponent addressForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent numberField = (FieldComponent) view.getComponentByReference(AddressFields.NUMBER);

        Entity address = addressForm.getPersistedEntityWithIncludedFormValues();

        String number = address.getStringField(AddressFields.NUMBER);

        if ((address.getId() == null) && StringUtils.isEmpty(number)) {
            Entity company = address.getBelongsToField(AddressFields.COMPANY);

            numberField.setFieldValue(basicService.generateAddressNumber(company));
            numberField.requestComponentUpdateState();
        }
    }

}
