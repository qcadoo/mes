package com.qcadoo.mes.technologies;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.Entity;

public interface MrpAlgorithmStrategy {

    boolean isApplicableFor(final MrpAlgorithm algorithm);

    Map<Entity, BigDecimal> perform(final Map<Entity, BigDecimal> productComponentQuantities, final Set<Entity> nonComponents,
            MrpAlgorithm algorithm, final String type);
}
