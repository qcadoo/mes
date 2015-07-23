package com.qcadoo.mes.cmmsMachineParts.hooks;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.MachinePartForEventFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class MachinePartForEventHooks {

    public void onView(final DataDefinition machinePartDD, final Entity machinePartForEvent) {
        BigDecimal plannedQuantity = machinePartForEvent.getDecimalField(MachinePartForEventFields.PLANNED_QUANTITY);
        BigDecimal issuedQuantity = machinePartForEvent.getDecimalField(MachinePartForEventFields.ISSUED_QUANTITY);
        if (plannedQuantity != null && issuedQuantity != null) {
            BigDecimal remainingQuantity = plannedQuantity.subtract(issuedQuantity);
            if (remainingQuantity.compareTo(BigDecimal.ZERO) < 0) {
                remainingQuantity = BigDecimal.ZERO;
            }
            machinePartForEvent.setField(MachinePartForEventFields.REMAINING_QUANTITY, remainingQuantity);
        } else {
            machinePartForEvent.setField(MachinePartForEventFields.REMAINING_QUANTITY, plannedQuantity);
        }
    }
}
