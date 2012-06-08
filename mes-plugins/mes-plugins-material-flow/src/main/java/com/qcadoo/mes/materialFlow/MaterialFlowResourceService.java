package com.qcadoo.mes.materialFlow;

import java.math.BigDecimal;

import com.qcadoo.model.api.Entity;

public interface MaterialFlowResourceService {

    boolean areResourcesSufficient(final Entity stockAreas, final Entity product, final BigDecimal quantity);

    void manageResources(final Entity transfer);

}
