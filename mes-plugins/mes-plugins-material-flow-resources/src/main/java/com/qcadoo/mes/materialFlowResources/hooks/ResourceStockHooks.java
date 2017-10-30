package com.qcadoo.mes.materialFlowResources.hooks;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.ResourceStockFields;
import com.qcadoo.mes.materialFlowResources.service.ResourceStockService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ResourceStockHooks {

    @Autowired
    private ResourceStockService resourceStockService;

    public boolean onDelete(final DataDefinition resourceStockDD, final Entity resourceStock) {
        BigDecimal quantity = resourceStockService.getResourceStockQuantity(
                resourceStock.getBelongsToField(ResourceStockFields.PRODUCT),
                resourceStock.getBelongsToField(ResourceStockFields.LOCATION));
        if (quantity.compareTo(BigDecimal.ZERO) != 0) {
            resourceStock.addGlobalError("materialFlowResources.resourceStock.delete.error");
            return false;
        }
        return true;
    }
}
