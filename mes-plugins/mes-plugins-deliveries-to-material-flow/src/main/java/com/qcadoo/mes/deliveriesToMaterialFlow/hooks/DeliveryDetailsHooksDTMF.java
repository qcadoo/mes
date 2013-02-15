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
package com.qcadoo.mes.deliveriesToMaterialFlow.hooks;

import static com.qcadoo.mes.deliveries.constants.DeliveryFields.STATE;
import static com.qcadoo.mes.deliveries.states.constants.DeliveryState.DECLINED;
import static com.qcadoo.mes.deliveries.states.constants.DeliveryState.RECEIVED;
import static com.qcadoo.mes.deliveriesToMaterialFlow.constants.DeliveryFieldsDTMF.LOCATION;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class DeliveryDetailsHooksDTMF {

    private static final String L_FORM = "form";

    @Autowired
    private ParameterService parameterService;

    public void fillLocationDefaultValue(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(L_FORM);

        if (deliveryForm.getEntityId() != null) {
            return;
        }

        LookupComponent locationField = (LookupComponent) view.getComponentByReference(LOCATION);
        Entity location = locationField.getEntity();

        if (location == null) {
            Entity defaultLocation = parameterService.getParameter().getBelongsToField(LOCATION);

            if (defaultLocation != null) {
                locationField.setFieldValue(defaultLocation.getId());
                locationField.requestComponentUpdateState();
            }
        }
    }

    public void changeLocationEnabledDependOnState(final ViewDefinitionState view) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(L_FORM);

        FieldComponent stateField = (FieldComponent) view.getComponentByReference(STATE);
        String state = stateField.getFieldValue().toString();

        LookupComponent locationField = (LookupComponent) view.getComponentByReference(LOCATION);

        if (deliveryForm.getEntityId() == null) {
            locationField.setEnabled(true);
        } else {
            if (DECLINED.getStringValue().equals(state) || RECEIVED.getStringValue().equals(state)) {
                locationField.setEnabled(false);
            } else {
                locationField.setEnabled(true);
            }
        }
    }

}
