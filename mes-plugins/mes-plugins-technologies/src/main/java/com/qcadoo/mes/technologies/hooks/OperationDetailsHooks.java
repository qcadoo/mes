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
package com.qcadoo.mes.technologies.hooks;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.technologies.constants.AssignedToOperation;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class OperationDetailsHooks {

    public static final String L_FORM = "form";

    private static final List<String> L_WORKSTATIONS_TAB_FIELDS = Arrays.asList(OperationFields.ASSIGNED_TO_OPERATION,
            OperationFields.QUANTITY_OF_WORKSTATIONS);

    private static final List<String> L_WORKSTATIONS_TAB_LOOKUPS = Arrays.asList(OperationFields.PRODUCTION_LINE,
            OperationFields.DIVISION, OperationFields.WORKSTATION_TYPE);

    public final void onBeforeRender(final ViewDefinitionState view) {
        disableWorkstationsTabFieldsIfOperationIsNotSaved(view);
    }

    private void disableWorkstationsTabFieldsIfOperationIsNotSaved(ViewDefinitionState view) {
        FormComponent operationForm = (FormComponent) view.getComponentByReference(L_FORM);
        GridComponent workstations = (GridComponent) view.getComponentByReference(OperationFields.WORKSTATIONS);

        if (operationForm.getEntityId() == null) {
            changedEnabledFields(view, L_WORKSTATIONS_TAB_FIELDS, false);
            changeEnabledLookups(view, L_WORKSTATIONS_TAB_LOOKUPS, Lists.newArrayList(""));
            workstations.setEnabled(false);

        } else {
            changedEnabledFields(view, L_WORKSTATIONS_TAB_FIELDS, true);
            changeEnabledLookups(view, L_WORKSTATIONS_TAB_LOOKUPS, L_WORKSTATIONS_TAB_LOOKUPS);
            workstations.setEnabled(true);
            setWorkstationsTabFields(view);
        }
    }

    private void changedEnabledFields(final ViewDefinitionState view, final List<String> references, final boolean enabled) {
        for (String reference : references) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setEnabled(enabled);
        }
    }

    public void setWorkstationsTabFields(final ViewDefinitionState view) {
        FieldComponent assignedToOperation = (FieldComponent) view.getComponentByReference(OperationFields.ASSIGNED_TO_OPERATION);
        String assignedToOperationValue = (String) assignedToOperation.getFieldValue();
        GridComponent workstations = (GridComponent) view.getComponentByReference(OperationFields.WORKSTATIONS);

        if (AssignedToOperation.WORKSTATIONS.getStringValue().equals(assignedToOperationValue)) {
            changeEnabledLookups(view, L_WORKSTATIONS_TAB_LOOKUPS, Lists.newArrayList(""));
            workstations.setEnabled(true);
            enableRibbonItem(view, !workstations.getEntities().isEmpty());
        } else if (AssignedToOperation.WORKSTATIONS_TYPE.getStringValue().equals(assignedToOperationValue)) {
            changeEnabledLookups(view, L_WORKSTATIONS_TAB_LOOKUPS, Lists.newArrayList(OperationFields.WORKSTATION_TYPE));
            workstations.setEnabled(false);
            enableRibbonItem(view, false);
        } else if (AssignedToOperation.DIVISION.getStringValue().equals(assignedToOperationValue)) {
            changeEnabledLookups(view, L_WORKSTATIONS_TAB_LOOKUPS, Lists.newArrayList(OperationFields.DIVISION));
            workstations.setEnabled(false);
            enableRibbonItem(view, false);
        } else if (AssignedToOperation.PRODUCTION_LINE.getStringValue().equals(assignedToOperationValue)) {
            changeEnabledLookups(view, L_WORKSTATIONS_TAB_LOOKUPS, Lists.newArrayList(OperationFields.PRODUCTION_LINE));
            workstations.setEnabled(false);
            enableRibbonItem(view, false);
        }
    }

    private void changeEnabledLookups(final ViewDefinitionState view, final List<String> fields,
            final List<String> enabledFields) {
        for (String field : fields) {
            LookupComponent lookup = (LookupComponent) view.getComponentByReference(field);
            lookup.setEnabled(enabledFields.contains(field));
        }
    }

    private void enableRibbonItem(final ViewDefinitionState view, final boolean enable) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonActionItem addUp = window.getRibbon().getGroupByName("workstations").getItemByName("addUpTheNumberOfWorktations");
        addUp.setEnabled(enable);
        addUp.requestUpdate(true);
    }
}
