package com.qcadoo.mes.costCalculation;

import java.math.BigDecimal;
import java.util.Map;

import com.qcadoo.model.api.Entity;

public interface CostCalculationService {

    public Map<String, BigDecimal> calculateTotalCost(Entity source, Map<String, Object> parameters);
}
