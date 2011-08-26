package com.qcadoo.mes.costCalculation;

import java.util.Map;

import com.qcadoo.model.api.Entity;

public interface CostCalculationService {

    public Map<String, Object> calculateTotalCost(final Entity source, final Map<String, Object> parameters);
}
