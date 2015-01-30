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
package com.qcadoo.mes.technologies.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.AssignedToOperation;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.hooks.OperationDetailsHooks;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OperationDetailsListeners {

    private static final String L_FORM = "form";

    @Autowired
    private OperationDetailsHooks operationDetailsHooks;

    public void setProductionLineLookup(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        operationDetailsHooks.setProductionLineLookup(view);
    }

    public void setWorkstationsLookup(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        operationDetailsHooks.setWorkstationsLookup(view);
    }

    public void setWorkstationsTabFields(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        operationDetailsHooks.setWorkstationsTabFields(view);
        FieldComponent assignedToOperation = (FieldComponent) view.getComponentByReference(OperationFields.ASSIGNED_TO_OPERATION);
        String assignedToOperationValue = (String) assignedToOperation.getFieldValue();
        if (AssignedToOperation.WORKSTATIONS.getStringValue().equals(assignedToOperationValue)) {
            operationDetailsHooks.clearLookupField(view, OperationFields.WORKSTATION_TYPE);
            operationDetailsHooks.clearLookupField(view, OperationFields.DIVISION);
            operationDetailsHooks.clearLookupField(view, OperationFields.PRODUCTION_LINE);
        } else if (AssignedToOperation.WORKSTATIONS_TYPE.getStringValue().equals(assignedToOperationValue)) {
            operationDetailsHooks.clearWorkstationsField(view);
            operationDetailsHooks.clearLookupField(view, OperationFields.DIVISION);
            operationDetailsHooks.clearLookupField(view, OperationFields.PRODUCTION_LINE);
        }
    }

    public void addUpTheNumberOfWorktations(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        int size = form.getPersistedEntityWithIncludedFormValues().getHasManyField(OperationFields.WORKSTATIONS).size();
        FieldComponent quantityOfWorkstations = (FieldComponent) view
                .getComponentByReference(OperationFields.QUANTITY_OF_WORKSTATIONS);
        quantityOfWorkstations.setFieldValue(size);
        quantityOfWorkstations.requestComponentUpdateState();
    }
}
