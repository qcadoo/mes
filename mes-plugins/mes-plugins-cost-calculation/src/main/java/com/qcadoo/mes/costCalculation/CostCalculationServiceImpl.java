package com.qcadoo.mes.costCalculation;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.util.Map;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;

public class CostCalculationServiceImpl implements CostCalculationService {

    @Override
    public BigDecimal calculateTotalCostView(ViewDefinitionState state) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BigDecimal calculateTotalCost(final Entity technology, final Entity order, final Map<String, Object> parameters) {
        checkArgument(technology != null, "technology is null");
        checkArgument(order != null, "order is null");
        checkArgument(parameters.size() != 0, "parameter is empty");
        return null;
    }

}
