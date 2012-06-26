package com.qcadoo.mes.materialFlowResources;

import java.math.BigDecimal;
import java.util.Map;

import com.qcadoo.model.api.Entity;

public interface MaterialFlowResourcesService {

    boolean areResourcesSufficient(final Entity location, final Entity product, final BigDecimal quantity);

    void manageResources(final Entity transfer);

    Map<Entity, BigDecimal> groupResourcesByProduct(final Entity location);
}
