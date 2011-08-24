package com.qcadoo.mes.costNormsForOperation;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants;
import com.qcadoo.mes.productionScheduling.OrderRealizationTimeService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

public class OperationsCostCalculationServiceImpl implements OperationsCostCalculationService {

    @Autowired
    private OrderRealizationTimeService orderRealizationTimeService;
    
    @Override
    public Map<String, BigDecimal> calculateOperationsCost(Entity source, OperationsCostCalculationConstants mode,
            boolean includeTPZs, BigDecimal quantity) {
        checkArgument(source != null, "source is null");
        checkArgument(quantity != null, "quantity is null");
        checkArgument(quantity.compareTo(BigDecimal.valueOf(0)) == 1, "quantity should be greather than 0");

        Map<String, BigDecimal> result = new HashMap<String, BigDecimal>();
        
        EntityTree operationComponents = getOperationComponentsTree(source);
        if(operationComponents == null) {
            throw new IllegalArgumentException("Incompatible source entity type..");
        }
        
        result.put("machineHourlyCost", BigDecimal.valueOf(1));
        result.put("laborHourlyCost", BigDecimal.valueOf(1));
        return result;
    }
    
    private EntityTree getOperationComponentsTree(final Entity source) {
        DataDefinition dd = source.getDataDefinition();
        if(dd == null) {
            return null;
        }
        
        Entity operationComponentsProvider = dd.get(source.getId()); 
        if ("order".equals(dd.getName())) {
            operationComponentsProvider = operationComponentsProvider.getBelongsToField("technology");
        } else if (!("technology".equals(dd.getName())) || operationComponentsProvider == null) {
            return null;
        }
        
        return operationComponentsProvider.getTreeField("operationComponents");
    }
    
}
