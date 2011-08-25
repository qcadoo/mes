package com.qcadoo.mes.costCalculation;

import java.util.Map;

import com.qcadoo.model.api.Entity;

public interface CostCalculationService {

    public Map<String, Object> calculateTotalCost(final Entity technology, final Entity order, final Map<String, Object> parameters);
}
