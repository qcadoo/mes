package com.qcadoo.mes.cmmsMachineParts.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.ActionAppliesTo;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ActionHooks {

    public void onSave(final DataDefinition actionDD, final Entity action) {
        ActionAppliesTo appliesTo = ActionAppliesTo.from(action);
        if (appliesTo.compareTo(ActionAppliesTo.WORKSTATION_OR_SUBASSEMBLY) == 0) {
            clearFields(action, false, true);
        } else if (appliesTo.compareTo(ActionAppliesTo.WORKSTATION_TYPE) == 0) {
            clearFields(action, true, false);
        } else {
            clearFields(action, true, true);
        }
    }

    private void clearFields(final Entity action, boolean clearWorkstations, boolean clearWorkstationTypes) {
        if (clearWorkstations) {
            action.setField(ActionFields.WORKSTATIONS, null);
            action.setField(ActionFields.SUBASSEMBLIES, null);
        }
        if (clearWorkstationTypes) {
            action.setField(ActionFields.WORKSTATION_TYPES, null);
        }
    }
}
