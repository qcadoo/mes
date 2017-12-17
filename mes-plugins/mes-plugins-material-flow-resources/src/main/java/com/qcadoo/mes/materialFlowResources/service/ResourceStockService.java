package com.qcadoo.mes.materialFlowResources.service;

import java.math.BigDecimal;

import com.qcadoo.model.api.Entity;

public interface ResourceStockService {

    void createResourceStock(final Entity resource);

    BigDecimal getResourceStockAvailableQuantity(final Entity product, final Entity location);

    BigDecimal getResourceStockQuantity(Entity product, Entity location);

    void checkResourcesStock(Entity document);
}
