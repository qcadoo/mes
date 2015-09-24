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
