package com.qcadoo.mes.basic.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class WorkstationModelHooks {

    public boolean onDelete(final DataDefinition dataDefinition, final Entity workstation) {
        boolean canDelete = workstation.getHasManyField(WorkstationFields.SUBASSEMBLIES).isEmpty();
        if (!canDelete) {
            workstation.addGlobalError("basic.workstation.delete.hasSubassemblies");
        }
        return canDelete;
    }
}
