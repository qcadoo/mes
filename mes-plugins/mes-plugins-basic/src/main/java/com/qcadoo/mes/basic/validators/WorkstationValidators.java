package com.qcadoo.mes.basic.validators;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

@Service
public class WorkstationValidators {

    public boolean checkIfDevisionChanged(final DataDefinition workstationDD, final FieldDefinition divisionFD,
            final Entity workstation, final Object divisionOldValue, final Object divisionNewValue) {
        Entity divisionOld = (Entity) divisionOldValue;
        Entity divisionNew = (Entity) divisionNewValue;

        if ((divisionOld != null) && (divisionNew != null) && !(divisionOld.getId().equals(divisionNew.getId()))) {
            workstation.addError(workstationDD.getField(WorkstationFields.DIVISION), "basic.workstation.error.divisionIsUsed");

            return false;
        }

        return true;
    }

}
