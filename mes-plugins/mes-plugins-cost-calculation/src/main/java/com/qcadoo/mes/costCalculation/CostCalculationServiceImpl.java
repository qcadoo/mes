package com.qcadoo.mes.costCalculation;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.mes.costNormsForOperation.OperationsCostCalculationService;
import com.qcadoo.mes.costNormsForProduct.ProductsCostCalculationService;
import com.qcadoo.model.api.Entity;

public class CostCalculationServiceImpl implements CostCalculationService {

    @Autowired
    private OperationsCostCalculationService operationsCostCalculationService;
    
    @Autowired
    private ProductsCostCalculationService productsCostCalculationService;
    
    public Map<String, Object> calculateTotalCost(final Entity source,
            final Map<String, Object> parameters) {
        checkArgument(source != null && source.getDataDefinition() != null, "incompatible source entity");
        checkArgument(parameters.size() != 0, "parameter is empty");

        BigDecimal quantity = (BigDecimal) parameters.get("quantity");

        checkArgument(quantity != null && quantity.compareTo(BigDecimal.valueOf(0)) == 1);
        return Collections.emptyMap();
    }

}
