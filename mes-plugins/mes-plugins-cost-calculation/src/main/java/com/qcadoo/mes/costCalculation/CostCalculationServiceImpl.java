package com.qcadoo.mes.costCalculation;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.mes.costNormsForOperation.OperationsCostCalculationService;
import com.qcadoo.mes.costNormsForProduct.ProductsCostCalculationService;
import com.qcadoo.model.api.Entity;

public class CostCalculationServiceImpl implements CostCalculationService {

    @Autowired
    private OperationsCostCalculationService operationsCalculationService;
    
    @Autowired
    private ProductsCostCalculationService productsCostCalculationService;
    
    public Map<String, Object> calculateTotalCost(final Entity technology, final Entity order, final Map<String, Object> parameters) {
        checkArgument(technology != null, "technology is null");
        checkArgument(parameters.size() != 0, "parameter is empty");
        
        if(Boolean.valueOf(parameters.get("includeTPZs").toString())) {
            // nothing
        }
        
        return Collections.emptyMap();
    }

}
