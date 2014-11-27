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

import com.qcadoo.mes.technologies.constants.AssignedToOperation;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class TOCDetailsHooks {

    public static final String L_FORM = "form";

    private static final List<String> L_WORKSTATIONS_TAB_FIELDS = Arrays
            .asList(TechnologyOperationComponentFields.ASSIGNED_TO_OPERATION,
                    TechnologyOperationComponentFields.QUANTITY_OF_WORKSTATIONS);

    public final void onBeforeRender(final ViewDefinitionState view) {
        disableWorkstationsTabFieldsIfOperationIsNotSaved(view);
    }

    private void disableWorkstationsTabFieldsIfOperationIsNotSaved(ViewDefinitionState view) {
        FormComponent operationForm = (FormComponent) view.getComponentByReference(L_FORM);
        LookupComponent workstationType = (LookupComponent) view
                .getComponentByReference(TechnologyOperationComponentFields.WORKSTATION_TYPE);
        LookupComponent division = (LookupComponent) view.getComponentByReference(TechnologyOperationComponentFields.DIVISION);
        GridComponent workstations = (GridComponent) view
                .getComponentByReference(TechnologyOperationComponentFields.WORKSTATIONS);

        if (operationForm.getEntityId() == null) {
            changedEnabledFields(view, L_WORKSTATIONS_TAB_FIELDS, false);
            workstationType.setEnabled(false);
            workstations.setEnabled(false);
            division.setEnabled(false);

        } else {
            changedEnabledFields(view, L_WORKSTATIONS_TAB_FIELDS, true);
            workstationType.setEnabled(true);
            workstations.setEnabled(true);
            division.setEnabled(true);
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
        FieldComponent assignedToOperation = (FieldComponent) view
                .getComponentByReference(TechnologyOperationComponentFields.ASSIGNED_TO_OPERATION);
        String assignedToOperationValue = (String) assignedToOperation.getFieldValue();
        GridComponent workstations = (GridComponent) view.getComponentByReference(OperationFields.WORKSTATIONS);

        if (AssignedToOperation.WORKSTATIONS.getStringValue().equals(assignedToOperationValue)) {
            enableWorkstationsTabFields(view, true, false, false, !workstations.getEntities().isEmpty());
        } else if (AssignedToOperation.WORKSTATIONS_TYPE.getStringValue().equals(assignedToOperationValue)) {
            enableWorkstationsTabFields(view, false, true, false, false);
        } else if (AssignedToOperation.DIVISION.getStringValue().equals(assignedToOperationValue)) {
            enableWorkstationsTabFields(view, false, false, true, false);
        }

    }

    private void enableWorkstationsTabFields(final ViewDefinitionState view, final boolean workstationsEnabled,
            final boolean workstationTypeEnabled, final boolean divisionEnabled, final boolean ribbonEnabled) {
        LookupComponent workstationType = (LookupComponent) view
                .getComponentByReference(TechnologyOperationComponentFields.WORKSTATION_TYPE);
        LookupComponent division = (LookupComponent) view.getComponentByReference(TechnologyOperationComponentFields.DIVISION);
        GridComponent workstations = (GridComponent) view
                .getComponentByReference(TechnologyOperationComponentFields.WORKSTATIONS);
        workstations.setEnabled(workstationsEnabled);
        workstationType.setEnabled(workstationTypeEnabled);
        enableRibbonItem(view, ribbonEnabled);
        division.setEnabled(divisionEnabled);

    }

    private void enableRibbonItem(final ViewDefinitionState view, final boolean enable) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonActionItem addUp = window.getRibbon().getGroupByName("workstations").getItemByName("addUpTheNumberOfWorktations");
        addUp.setEnabled(enable);
        addUp.requestUpdate(true);
    }

}
