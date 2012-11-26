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
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STAFF;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TRANSFORMATIONS_CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlow.constants.TransferFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Component
public class TransferDetailsViewHooks {

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void checkIfTransferHasTransformations(final ViewDefinitionState view) {
        FormComponent transferForm = (FormComponent) view.getComponentByReference(L_FORM);

        Long transferId = transferForm.getEntityId();
        if (transferId == null) {
            return;
        }

        Entity transfer = dataDefinitionService
                .get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_TRANSFER).get(transferId);

        if (transfer == null) {
            return;
        }

        if (transfer.getBelongsToField(TRANSFORMATIONS_CONSUMPTION) != null
                || transfer.getBelongsToField(TransferFields.TRANSFORMATIONS_PRODUCTION) != null) {
            FieldComponent type = (FieldComponent) view.getComponentByReference(TYPE);
            FieldComponent date = (FieldComponent) view.getComponentByReference(TIME);
            FieldComponent locationTo = (FieldComponent) view.getComponentByReference(LOCATION_TO);
            FieldComponent locationFrom = (FieldComponent) view.getComponentByReference(LOCATION_FROM);
            FieldComponent staff = (FieldComponent) view.getComponentByReference(STAFF);

            type.setEnabled(false);
            date.setEnabled(false);
            locationTo.setEnabled(false);
            locationFrom.setEnabled(false);
            staff.setEnabled(false);
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
