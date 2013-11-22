package com.qcadoo.mes.basic.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class WorkstationValidators {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean checkIfDevisionChange(final DataDefinition workstationDD, final Entity workstation) {
        boolean isValid = true;
        if (workstation == null) {
            return isValid;
        }
        Entity division = workstation.getBelongsToField(WorkstationFields.DIVISION);
        if (division == null) {
            return isValid;
        }
        Entity workstationDB = workstationDD.get(workstation.getId());
        Entity divisionDB = workstationDB.getBelongsToField(WorkstationFields.DIVISION);
        if (divisionDB == null) {
            return isValid;
        }
        if (division.getId() != divisionDB.getId()) {
            isValid = false;
            workstation.addError(workstationDD.getField(WorkstationFields.DIVISION), "basic.workstation.error.divisionIsUsed");
        }
        return isValid;
    }
}
