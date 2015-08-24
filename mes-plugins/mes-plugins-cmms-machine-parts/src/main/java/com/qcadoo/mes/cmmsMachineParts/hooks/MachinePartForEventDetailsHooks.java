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
            // Entity context = maintenanceEvent.getBelongsToField(MaintenanceEventFields.MAINTENANCE_EVENT_CONTEXT);
            Entity factory = maintenanceEvent.getBelongsToField(MaintenanceEventFields.FACTORY);
            setWarehouseLookup(view, factory);
        } else if (plannedEvent != null) {
            // Entity context = plannedEvent.getBelongsToField(PlannedEventFields.PLANNED_EVENT_CONTEXT);
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
