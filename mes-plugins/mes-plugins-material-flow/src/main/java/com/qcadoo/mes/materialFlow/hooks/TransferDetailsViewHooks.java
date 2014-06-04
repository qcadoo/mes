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
package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STAFF;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TRANSFORMATIONS_CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.materialFlow.MaterialFlowService;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlow.constants.TransferFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;

@Component
public class TransferDetailsViewHooks {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String RIBBON_GROUP = "actions";

    private static final List<String> RIBBON_ACTION_ITEM = Arrays.asList("saveBack", "saveNew", "save");

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private MaterialFlowService materialFlowService;

    public void onBeforeRender(final ViewDefinitionState view) {
        checkIfTransferHasTransformations(view);
        disableFormWhenTransferIsSaved(view);
    }

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

    public void disableFormWhenTransferIsSaved(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        FormComponent transferForm = (FormComponent) view.getComponentByReference(L_FORM);

        Long transferId = transferForm.getEntityId();
        if (transferId == null) {
            return;
        }

        transferForm.setFormEnabled(false);
        enableRibbon(window, false);

    }

    private void enableRibbon(final WindowComponent window, final boolean enable) {
        for (String actionItem : RIBBON_ACTION_ITEM) {
            window.getRibbon().getGroupByName(RIBBON_GROUP).getItemByName(actionItem).setEnabled(enable);
            window.getRibbon().getGroupByName(RIBBON_GROUP).getItemByName(actionItem).requestUpdate(true);
        }
        window.requestRibbonRender();
    }

    public void checkIfLocationFromHasExternalNumber(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        checkIfLocationFromHasExternalNumber(view);
    }

    public void checkIfLocationToHasExternalNumber(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        checkIfLocationToHasExternalNumber(view);
    }

    public void checkIfLocationToHasExternalNumber(final ViewDefinitionState view) {
        materialFlowService.checkIfLocationHasExternalNumber(view, LOCATION_TO);
    }

    public void checkIfLocationFromHasExternalNumber(final ViewDefinitionState view) {
        materialFlowService.checkIfLocationHasExternalNumber(view, LOCATION_FROM);
    }

    public void showMessageAfterSaving(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {

        Long transferId = (Long) componentState.getFieldValue();

        if (transferId != null) {
            view.getComponentByReference(L_FORM).addMessage("materialFlow.transformations.save.infoAterSave", MessageType.INFO,
                    false);
        }
    }

}
