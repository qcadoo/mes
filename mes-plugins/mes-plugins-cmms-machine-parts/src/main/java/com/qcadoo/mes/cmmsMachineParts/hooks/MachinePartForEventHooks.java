package com.qcadoo.mes.cmmsMachineParts.hooks;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MachinePartForEventFields;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class MachinePartForEventHooks {

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public void onView(final DataDefinition machinePartDD, final Entity machinePartForEvent) {
        Entity machinePart = machinePartForEvent.getBelongsToField(MachinePartForEventFields.MACHINE_PART);
        Entity warehouse = machinePartForEvent.getBelongsToField(MachinePartForEventFields.WAREHOUSE);
        if (machinePart != null && warehouse != null) {
            Map<Long, BigDecimal> quantitiesInWarehouse = materialFlowResourcesService.getQuantitiesForProductsAndLocation(
                    Lists.newArrayList(machinePart), warehouse);
            BigDecimal quantity = quantitiesInWarehouse.get(machinePart.getId()) != null ? quantitiesInWarehouse.get(machinePart
                    .getId()) : BigDecimal.ZERO;
            machinePartForEvent.setField(MachinePartForEventFields.AVAILABLE_QUANTITY, quantity);
        }
        onSave(machinePartDD, machinePartForEvent);
    }

    public void onSave(final DataDefinition machinePartDD, final Entity machinePartForEvent) {
        Entity machinePart = machinePartForEvent.getBelongsToField(MachinePartForEventFields.MACHINE_PART);
        if (machinePart != null) {
            machinePartForEvent.setField(MachinePartForEventFields.MACHINE_PART_NAME,
                    machinePart.getStringField(ProductFields.NAME));
            machinePartForEvent.setField(MachinePartForEventFields.MACHINE_PART_NUMBER,
                    machinePart.getStringField(ProductFields.NUMBER));
            machinePartForEvent.setField(MachinePartForEventFields.MACHINE_PART_UNIT,
                    machinePart.getStringField(ProductFields.UNIT));
        }
        Entity warehouse = machinePartForEvent.getBelongsToField(MachinePartForEventFields.WAREHOUSE);
        if (warehouse != null) {
            machinePartForEvent.setField(MachinePartForEventFields.WAREHOUSE_NUMBER,
                    warehouse.getStringField(LocationFields.NUMBER));
        }

    }
}
