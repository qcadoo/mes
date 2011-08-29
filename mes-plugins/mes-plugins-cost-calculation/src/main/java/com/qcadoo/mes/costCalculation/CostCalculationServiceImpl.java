package com.qcadoo.mes.costCalculation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants.HOURLY;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_TECHNOLOGY;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.mes.costNormsForOperation.OperationsCostCalculationService;
import com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants;
import com.qcadoo.mes.costNormsForProduct.ProductsCostCalculationService;
import com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class CostCalculationServiceImpl implements CostCalculationService {

    @Autowired
    private OperationsCostCalculationService operationsCostCalculationService;

    @Autowired
    private ProductsCostCalculationService productsCostCalculationService;

    public Map<String, BigDecimal> calculateTotalCost(Entity source, Map<String, Object> parameters) {
        checkArgument(source != null && source.getDataDefinition() != null, "incompatible source entity");
        checkArgument(parameters.size() != 0, "parameter is empty");

        DataDefinition dd = source.getDataDefinition();
        BigDecimal quantity = (BigDecimal) parameters.get("quantity");
        BigDecimal productionCosts;
        BigDecimal materialCosts;
        BigDecimal productionCostMarginValue;
        BigDecimal materialCostMarginValue;
        BigDecimal materialCostMargin = (BigDecimal) parameters.get("materialCostMargin");
        BigDecimal productionCostMargin = (BigDecimal) parameters.get("productionCostMargin");
        BigDecimal additionalOverhead = (BigDecimal) parameters.get("additionalOverhead");
        BigDecimal totalTechnicalProductionCosts;
        BigDecimal totalOverhead;
        BigDecimal totalCosts;
        Entity technology;
        Map<String, BigDecimal> resultMap = new HashMap<String, BigDecimal>();
        OperationsCostCalculationConstants operationMode = (OperationsCostCalculationConstants) parameters
                .get("calculateOperationCostsMode");
        ProductsCostCalculationConstants productMode = (ProductsCostCalculationConstants) parameters
                .get("calculateMaterialCostsMode");

        checkArgument(quantity != null && quantity.compareTo(BigDecimal.valueOf(0)) == 1);

        // Be sure that source Entity isn't in detached state
        source = dd.get(source.getId());
        
        if (MODEL_TECHNOLOGY.equals(dd.getName())) {
            technology = source;
        } else if (MODEL_ORDER.equals(dd.getName())) {
            technology = source.getBelongsToField("technology");
        } else {
            throw new IllegalArgumentException("incompatible source entity!");
        }

        resultMap.putAll(operationsCostCalculationService.calculateOperationsCost(source, operationMode,
                (Boolean) parameters.get("includeTPZ"), quantity));
        resultMap.putAll(productsCostCalculationService.calculateProductsCost(technology, productMode,
                quantity));

        materialCosts = resultMap.get("totalMaterialCosts");
        
        if (operationMode == HOURLY) {
            productionCosts = resultMap.get("totalMachineHourlyCosts").add(resultMap.get("totalLaborHourlyCosts"));
        } else {
            productionCosts = resultMap.get("totalPieceworkCosts");
        }

        productionCostMarginValue = productionCosts.multiply(productionCostMargin).divide(BigDecimal.valueOf(100));
        materialCostMarginValue = materialCosts.multiply(materialCostMargin).divide(BigDecimal.valueOf(100));
        totalTechnicalProductionCosts = productionCosts.add(materialCosts);
        totalOverhead = productionCostMarginValue.add(materialCostMarginValue).add(additionalOverhead);
        totalCosts = totalOverhead.add(totalTechnicalProductionCosts);
        
        resultMap.put("productionCostMarginValue", productionCostMarginValue);
        resultMap.put("materialCostMarginValue", materialCostMarginValue);
        resultMap.put("totalOverhead", totalOverhead);
        resultMap.put("totalTechnicalProductionCosts", totalTechnicalProductionCosts);
        resultMap.put("totalCosts", totalCosts);
        resultMap.put("totalCostsPerUnit", totalCosts.divide(quantity));

        return resultMap;
    }

}
