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
package com.qcadoo.mes.cmmsMachineParts.listeners;

import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import org.springframework.stereotype.Service;

@Service
public class ContextEventListeners {

    public void factoryChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        clearSelectionOnDivision(view);
    }

    private void clearSelectionOnDivision(final ViewDefinitionState view) {
        clearFieldIfExists(view, MaintenanceEventFields.DIVISION);
        clearSelectionOnProductionLine(view);
    }

    private void clearSelectionOnProductionLine(final ViewDefinitionState view) {
        clearFieldIfExists(view, MaintenanceEventFields.PRODUCTION_LINE);
        clearSelectionOnWorkstation(view);
    }

    private void clearSelectionOnWorkstation(final ViewDefinitionState view) {
        clearFieldIfExists(view, MaintenanceEventFields.WORKSTATION);
        clearSelectionOnSubassembly(view);
    }

    private void clearSelectionOnSubassembly(final ViewDefinitionState view) {
        clearFieldIfExists(view, MaintenanceEventFields.SUBASSEMBLY);
    }

    private void clearFieldIfExists(ViewDefinitionState view, String reference) {
        FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(reference);
        if (fieldComponent != null) {
            fieldComponent.setFieldValue(null);
            fieldComponent.requestComponentUpdateState();
        }
    }


}
