package com.qcadoo.mes.cmmsMachineParts.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.FaultTypeAppliesTo;
import com.qcadoo.mes.cmmsMachineParts.constants.FaultTypeFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class FaultTypeHooks {

    public void onSave(final DataDefinition faultTypeDD, final Entity faultType) {
        FaultTypeAppliesTo appliesTo = FaultTypeAppliesTo.from(faultType);
        if (appliesTo.compareTo(FaultTypeAppliesTo.WORKSTATION_OR_SUBASSEMBLY) == 0) {
            clearFields(faultType, false, true);
        } else if (appliesTo.compareTo(FaultTypeAppliesTo.WORKSTATION_TYPE) == 0) {
            clearFields(faultType, true, false);
        } else {
            clearFields(faultType, true, true);
        }
    }

    private void clearFields(final Entity faultType, boolean clearWorkstations, boolean clearWorkstationTypes) {
        if (clearWorkstations) {
            faultType.setField(FaultTypeFields.WORKSTATIONS, null);
            faultType.setField(FaultTypeFields.SUBASSEMBLIES, null);
        }
        if (clearWorkstationTypes) {
            faultType.setField(FaultTypeFields.WORKSTATION_TYPES, null);
        }
    }
}
