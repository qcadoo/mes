package com.qcadoo.mes.materialFlowResources.hooks;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.ResourceStockFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ResourceStockHooks {

    public boolean onDelete(final DataDefinition resourceStockDD, final Entity resourceStock) {
        BigDecimal quantity = resourceStock.getDecimalField(ResourceStockFields.QUANTITY);
        if (quantity.compareTo(BigDecimal.ZERO) != 0) {
            resourceStock.addGlobalError("materialFlowResources.resourceStock.delete.error");
            return false;
        }
        return true;
    }
}
