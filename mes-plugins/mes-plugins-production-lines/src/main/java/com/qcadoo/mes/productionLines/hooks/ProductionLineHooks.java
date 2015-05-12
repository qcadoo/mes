package com.qcadoo.mes.productionLines.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionLineHooks {

    public boolean onDelete(final DataDefinition dataDefinition, final Entity productionLine) {
        boolean canDelete = productionLine.getManyToManyField(ProductionLineFields.DIVISIONS).isEmpty();
        if (!canDelete) {
            productionLine.addGlobalError("productionLines.productionLine.onDelete.hasDivisions");
        }
        return canDelete;
    }
}
