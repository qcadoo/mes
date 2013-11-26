package com.qcadoo.mes.technologies.validators;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

@Service
public class WorkstationValidatorsT {

    public boolean checkIfOperationChanged(final DataDefinition workstationDD, final FieldDefinition divisionFD,
            final Entity workstation, final Object operationOldValue, final Object operationNewValue) {
        Entity operationOld = (Entity) operationOldValue;
        Entity operationNew = (Entity) operationNewValue;

        if ((operationOld != null) && (operationNew != null) && (operationOld.getId() != operationNew.getId())) {
            workstation.addError(workstationDD.getField(WorkstationFields.DIVISION), "basic.workstation.error.operationIsUsed");

            return false;
        }

        return true;
    }

}
