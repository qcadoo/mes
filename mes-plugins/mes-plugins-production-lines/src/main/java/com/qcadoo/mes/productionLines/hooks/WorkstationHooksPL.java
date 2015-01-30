package com.qcadoo.mes.productionLines.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionLines.constants.WorkstationFieldsPL;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class WorkstationHooksPL {

    public boolean onDelete(final DataDefinition dataDefinition, final Entity workstation) {
        if (workstation.getBelongsToField(WorkstationFieldsPL.PRODUCTION_LINE) == null) {
            return true;
        }
        workstation.addGlobalError("productionLines.workstation.onDelete.error");
        return false;
    }

}
