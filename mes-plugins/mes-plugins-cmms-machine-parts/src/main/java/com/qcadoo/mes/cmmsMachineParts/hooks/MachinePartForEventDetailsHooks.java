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
package com.qcadoo.mes.cmmsMachineParts.hooks;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MachinePartForEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class MachinePartForEventDetailsHooks {

    private static final String[] unitFields = { "plannedQuantityUnit", "availableQuantityUnit" };

    private static final String L_FORM = "form";

    public void disableFieldsForIssuedPart(final ViewDefinitionState view) {

        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity machinePartForEvent = form.getPersistedEntityWithIncludedFormValues();
        BigDecimal issuedQuantity = machinePartForEvent.getDecimalField(MachinePartForEventFields.ISSUED_QUANTITY);

        if (issuedQuantity != null && issuedQuantity.compareTo(BigDecimal.ZERO) > 0) {
            LookupComponent machinePart = (LookupComponent) view.getComponentByReference(MachinePartForEventFields.MACHINE_PART);
            LookupComponent warehouse = (LookupComponent) view.getComponentByReference(MachinePartForEventFields.WAREHOUSE);
            machinePart.setEnabled(false);
            warehouse.setEnabled(false);
        }
    }

    public void fillUnitFields(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String args[]) {
        fillUnitFields(viewDefinitionState);
    }

    public void fillUnitFields(final ViewDefinitionState view) {

        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity machinePartForEvent = form.getPersistedEntityWithIncludedFormValues();
        Entity machinePart = machinePartForEvent.getBelongsToField(MachinePartForEventFields.MACHINE_PART);
        if (machinePart != null) {
            String unit = machinePart.getStringField(ProductFields.UNIT);
            for (String field : unitFields) {
                FieldComponent component = (FieldComponent) view.getComponentByReference(field);
                component.setFieldValue(unit);
            }
        }

    }

    public void fillWarehouse(final ViewDefinitionState view) {

        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity machinePartForEvent = form.getPersistedEntityWithIncludedFormValues();

        Entity maintenanceEvent = machinePartForEvent.getBelongsToField(MachinePartForEventFields.MAINTENANCE_EVENT);

        Entity plannedEvent = machinePartForEvent.getBelongsToField(MachinePartForEventFields.PLANNED_EVENT);
        if (maintenanceEvent != null) {
            Entity factory = maintenanceEvent.getBelongsToField(MaintenanceEventFields.FACTORY);
            setWarehouseLookup(view, factory);
            
        } else if (plannedEvent != null) {
            Entity factory = plannedEvent.getBelongsToField(PlannedEventFields.FACTORY);
            setWarehouseLookup(view, factory);
        }
    }

    private void setWarehouseLookup(final ViewDefinitionState view, final Entity factory) {
        if (factory != null) {
            LookupComponent warehouseLookup = (LookupComponent) view.getComponentByReference(MachinePartForEventFields.WAREHOUSE);
            Entity warehouse = factory.getBelongsToField("warehouse");
            if (warehouse != null && warehouseLookup.getFieldValue() == null) {
                warehouseLookup.setFieldValue(warehouse.getId());
            }
        }
    }
}
