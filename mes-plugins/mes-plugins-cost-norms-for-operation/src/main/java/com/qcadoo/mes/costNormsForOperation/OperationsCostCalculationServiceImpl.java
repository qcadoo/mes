package com.qcadoo.mes.costNormsForOperation;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants;
import com.qcadoo.model.api.Entity;

public class OperationsCostCalculationServiceImpl implements OperationsCostCalculationService {

    @Override
    public HashMap<String, BigDecimal> calculateOperationsCost(Entity source, OperationsCostCalculationConstants mode,
            boolean includeTPZs, BigDecimal quantity) {
        Map<String, BigDecimal> result = new HashMap<String, BigDecimal>();
        checkArgument(source != null, "source is null");
        checkArgument(
                "order".equals(source.getDataDefinition().getName()) || "technology".equals(source.getDataDefinition().getName()),
                "wrong source entity type");
        checkArgument(quantity != null, "quantity is null");
        checkArgument(quantity.compareTo(BigDecimal.valueOf(0)) == 1, "quantity should be greather than 0");

        result.put("machineHourlyCost", BigDecimal.valueOf(0));
        result.put("laborHourlyCost", BigDecimal.valueOf(0));
        return (HashMap<String, BigDecimal>) result;
    }
}
