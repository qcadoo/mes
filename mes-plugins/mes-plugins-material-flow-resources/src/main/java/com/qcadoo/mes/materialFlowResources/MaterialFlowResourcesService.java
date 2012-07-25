package com.qcadoo.mes.materialFlowResources;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.qcadoo.model.api.Entity;

public interface MaterialFlowResourcesService {

    boolean areResourcesSufficient(final Entity location, final Entity product, final BigDecimal quantity);

    void manageResources(final Entity transfer);

    List<Entity> getResourcesForLocationAndProduct(final Entity location, final Entity product);

    Map<Entity, BigDecimal> groupResourcesByProduct(final Entity location);

    void addResource(final Entity locationTo, final Entity product, final BigDecimal quantity, final Date time,
            final String batch, final BigDecimal price);

    void updateResource(final Entity locationFrom, final Entity product, BigDecimal quantity);

    void moveResource(final Entity locationFrom, final Entity locationTo, final Entity product, BigDecimal quantity,
            final Date time, final String batch, final BigDecimal price);

    BigDecimal calculatePrice(final Entity location, final Entity product);

    String generateBatchForTransfer(final String model);

}
