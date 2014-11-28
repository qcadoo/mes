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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.technologies.constants.AssignedToOperation;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.hooks.TOCDetailsHooks;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class TOCDetailsListeners {

    private static final String L_FORM = "form";

    @Autowired
    private TOCDetailsHooks tOCDetailsHooks;

    public void setWorkstationsTabFields(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        tOCDetailsHooks.setWorkstationsTabFields(view);
        FieldComponent assignedToOperation = (FieldComponent) view
                .getComponentByReference(TechnologyOperationComponentFields.ASSIGNED_TO_OPERATION);
        String assignedToOperationValue = (String) assignedToOperation.getFieldValue();
        if (AssignedToOperation.WORKSTATIONS.getStringValue().equals(assignedToOperationValue)) {
            clearLookupField(view, TechnologyOperationComponentFields.WORKSTATION_TYPE);
            clearLookupField(view, TechnologyOperationComponentFields.DIVISION);
            clearLookupField(view, TechnologyOperationComponentFields.PRODUCTION_LINE);
        } else if (AssignedToOperation.WORKSTATIONS_TYPE.getStringValue().equals(assignedToOperationValue)) {
            clearWorkstationsField(view);
            clearLookupField(view, TechnologyOperationComponentFields.DIVISION);
            clearLookupField(view, TechnologyOperationComponentFields.PRODUCTION_LINE);
        } else if (AssignedToOperation.DIVISION.getStringValue().equals(assignedToOperationValue)) {
            clearWorkstationsField(view);
            clearLookupField(view, TechnologyOperationComponentFields.WORKSTATION_TYPE);
            clearLookupField(view, TechnologyOperationComponentFields.PRODUCTION_LINE);
        } else if (AssignedToOperation.PRODUCTION_LINE.getStringValue().equals(assignedToOperationValue)) {
            clearWorkstationsField(view);
            clearLookupField(view, TechnologyOperationComponentFields.WORKSTATION_TYPE);
            clearLookupField(view, TechnologyOperationComponentFields.DIVISION);
        }

    }

    private void clearWorkstationsField(final ViewDefinitionState view) {
        GridComponent workstations = (GridComponent) view
                .getComponentByReference(TechnologyOperationComponentFields.WORKSTATIONS);
        List<Entity> entities = Lists.newArrayList();
        workstations.setEntities(entities);
        workstations.setFieldValue(null);
    }

    private void clearLookupField(final ViewDefinitionState view, String fieldName) {
        LookupComponent lookup = (LookupComponent) view.getComponentByReference(fieldName);
        lookup.setFieldValue(null);
        lookup.requestComponentUpdateState();
    }

    public void addUpTheNumberOfWorktations(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        int size = form.getPersistedEntityWithIncludedFormValues()
                .getHasManyField(TechnologyOperationComponentFields.WORKSTATIONS).size();
        FieldComponent quantityOfWorkstations = (FieldComponent) view
                .getComponentByReference(TechnologyOperationComponentFields.QUANTITY_OF_WORKSTATIONS);
        quantityOfWorkstations.setFieldValue(size);
        quantityOfWorkstations.requestComponentUpdateState();
    }

}
