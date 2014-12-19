package com.qcadoo.mes.productionLines.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionLineHooks {

    public boolean onDelete(final DataDefinition dataDefinition, final Entity productionLine) {
        if (productionLine.getBelongsToField(ProductionLineFields.DIVISION) == null) {
            return true;
        }
        productionLine.addGlobalError("productionLines.productionLine.onDelete.error");
        return false;
    }
}
