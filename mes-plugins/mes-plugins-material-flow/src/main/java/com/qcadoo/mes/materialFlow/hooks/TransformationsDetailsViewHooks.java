/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_TO;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.OPERATION;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.STAFF;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TRANSFERS_CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.TRANSFERS_PRODUCTION;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Component
public class TransformationsDetailsViewHooks {

    private static final String L_FORM = "form";

    private static final List<String> FIELDS = Arrays.asList(TIME, STAFF, LOCATION_FROM, LOCATION_TO, OPERATION);

    public void disableFields(final ViewDefinitionState view) {
        FormComponent transformationsForm = (FormComponent) view.getComponentByReference(L_FORM);

        if (transformationsForm.getEntityId() == null) {
            changeFieldComponentsState(view, true);
        } else {
            changeFieldComponentsState(view, false);
        }
    }

    public void disableExistingADLelements(final ViewDefinitionState view) {
        disableADL(view, TRANSFERS_CONSUMPTION);
        disableADL(view, TRANSFERS_PRODUCTION);
    }

    private void disableADL(final ViewDefinitionState view, final String name) {
        FormComponent transformationsForm = (FormComponent) view.getComponentByReference(L_FORM);

        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference(name);

        if (transformationsForm.getEntityId() == null) {
            adl.setEnabled(false);
        } else {
            adl.setEnabled(true);

            List<FormComponent> formComponents = adl.getFormComponents();

            for (FormComponent formComponent : formComponents) {
                if ((formComponent.getEntityId() != null) && formComponent.getEntity().isValid()) {
                    formComponent.setFormEnabled(false);
                } else {
                    formComponent.setFormEnabled(true);
                }
            }
        }
    }

    private void changeFieldComponentsState(final ViewDefinitionState view, final boolean isEnabled) {
        for (String fieldName : FIELDS) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(fieldName);

            if (OPERATION.equals(fieldName)) {
                fieldComponent.setEnabled(!isEnabled);
            } else {
                fieldComponent.setEnabled(isEnabled);
            }
            fieldComponent.requestComponentUpdateState();
        }
    }

    public void checkIfLocationFromHasExternalNumber(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        checkIfLocationFromHasExternalNumber(view);
    }

    public void checkIfLocationToHasExternalNumber(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        checkIfLocationToHasExternalNumber(view);
    }

    public void checkIfLocationToHasExternalNumber(final ViewDefinitionState view) {
        LookupComponent locationLookup = (LookupComponent) view.getComponentByReference(LOCATION_TO);
        Entity location = locationLookup.getEntity();
        if (location != null) {
            if (location.getStringField("externalNumber") != null) {
                locationLookup.addMessage("materialFlow.validate.global.error.locationHasExternalNumber",
                        ComponentState.MessageType.FAILURE);
            }
        }
    }

    public void checkIfLocationFromHasExternalNumber(final ViewDefinitionState view) {
        LookupComponent locationLookup = (LookupComponent) view.getComponentByReference(LOCATION_FROM);
        Entity location = locationLookup.getEntity();
        if (location != null) {
            if (location.getStringField("externalNumber") != null) {
                locationLookup.addMessage("materialFlow.validate.global.error.locationHasExternalNumber",
                        ComponentState.MessageType.FAILURE);
            }
        }
    }

}
