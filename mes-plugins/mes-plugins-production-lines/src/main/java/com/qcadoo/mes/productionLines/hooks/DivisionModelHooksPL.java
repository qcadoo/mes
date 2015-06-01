package com.qcadoo.mes.productionLines.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionLines.constants.DivisionFieldsPL;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class DivisionModelHooksPL {

    public boolean onDelete(final DataDefinition dataDefinition, final Entity division) {
        boolean canDelete = division.getManyToManyField(DivisionFieldsPL.PRODUCTION_LINES).isEmpty();
        if (!canDelete) {
            division.addGlobalError("productionLines.division.onDelete.hasProductionLines");
        }
        return canDelete;
    }
}
