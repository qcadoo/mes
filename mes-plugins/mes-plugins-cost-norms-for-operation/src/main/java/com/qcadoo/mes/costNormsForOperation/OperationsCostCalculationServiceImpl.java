package com.qcadoo.mes.costNormsForOperation;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;

import com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants;
import com.qcadoo.model.api.Entity;


public class OperationsCostCalculationServiceImpl implements OperationsCostCalculationService {

    @Override
    public BigDecimal calculateOperationsCost(Entity source, OperationsCostCalculationConstants mode, boolean includeTPZs,
            BigDecimal quantity) {
        checkArgument(source != null, "source is null");
        checkArgument("order".equals(source.getDataDefinition().getName()) || "technology".equals(source.getDataDefinition().getName()), "wrong source entity type");
        checkArgument(quantity != null, "quantity is null");
        checkArgument(quantity.compareTo(BigDecimal.valueOf(0)) == 1, "quantity should be greather than 0");
        
        return BigDecimal.valueOf(50).multiply(quantity);
    }

}
