package com.qcadoo.mes.materialFlowResources.service;

import java.math.BigDecimal;
import java.util.Optional;

import com.qcadoo.model.api.Entity;

public interface ResourceStockService {

    void addResourceStock(final Entity resource);

    void addResourceStock(final Entity product, final Entity location, final BigDecimal quantity);

    void removeResourceStock(final Entity resource);

    void removeResourceStock(final Entity product, final Entity location, final BigDecimal quantity);

    Optional<Entity> getResourceStockForProductAndLocation(final Entity product, final Entity location);

}
