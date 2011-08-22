package com.qcadoo.mes.costCalculation;

import java.math.BigDecimal;
import java.util.Map;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;

public interface CostCalculationService {

    public BigDecimal calculateTotalCostView(final ViewDefinitionState state);

    public BigDecimal calculateTotalCost(final Entity technology, final Entity order, final Map<String, Object> parameters);
}
