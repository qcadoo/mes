package com.qcadoo.mes.cmmsMachineParts.hooks;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.cmmsMachineParts.constants.MachinePartForEventFields;
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
    }
}
