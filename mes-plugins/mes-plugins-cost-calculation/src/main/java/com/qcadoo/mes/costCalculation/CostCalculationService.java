package com.qcadoo.mes.costCalculation;

import com.qcadoo.model.api.Entity;

public interface CostCalculationService {

    public Entity calculateTotalCost(final Entity costCalculation);
}
