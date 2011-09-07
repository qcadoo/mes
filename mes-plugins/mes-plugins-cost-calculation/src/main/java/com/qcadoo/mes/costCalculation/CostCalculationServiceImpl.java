package com.qcadoo.mes.costCalculation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants.HOURLY;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costNormsForOperation.OperationsCostCalculationService;
import com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants;
import com.qcadoo.mes.costNormsForProduct.ProductsCostCalculationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class CostCalculationServiceImpl implements CostCalculationService {

    @Autowired
    private OperationsCostCalculationService operationsCostCalculationService;

    @Autowired
    private ProductsCostCalculationService productsCostCalculationService;

    public Entity calculateTotalCost(Entity costCalculation) {

        BigDecimal productionCosts;
        BigDecimal materialCostMargin = getBigDecimal(costCalculation.getField("materialCostMargin"));
        BigDecimal productionCostMargin = getBigDecimal(costCalculation.getField("productionCostMargin"));
        BigDecimal additionalOverhead = getBigDecimal(costCalculation.getField("additionalOverhead"));
        BigDecimal quantity = getBigDecimal(costCalculation.getField("quantity"));
        OperationsCostCalculationConstants operationMode = getOperationModeFromField(costCalculation
                .getField("calculateOperationCostsMode"));

        costCalculation.setField("dateOfCalculation", new Date());
        costCalculation = costCalculation.getDataDefinition().save(costCalculation);
        operationsCostCalculationService.calculateOperationsCost(costCalculation);
        productsCostCalculationService.calculateProductsCost(costCalculation);

        if (operationMode == HOURLY) {
            BigDecimal totalMachine = getBigDecimal(costCalculation.getField("totalMachineHourlyCosts"));
            BigDecimal totalLabor = getBigDecimal(costCalculation.getField("totalLaborHourlyCosts"));
            productionCosts = totalMachine.add(totalLabor);
        } else {
            productionCosts = getBigDecimal(costCalculation.getField("totalPieceworkCosts"));
        }

        BigDecimal materialCosts = getBigDecimal(costCalculation.getField("totalMaterialCosts"));
        BigDecimal productionCostMarginValue = productionCosts.multiply(productionCostMargin).divide(BigDecimal.valueOf(100));
        BigDecimal materialCostMarginValue = materialCosts.multiply(materialCostMargin).divide(BigDecimal.valueOf(100));
        BigDecimal totalTechnicalProductionCosts = productionCosts.add(materialCosts);
        BigDecimal totalOverhead = productionCostMarginValue.add(materialCostMarginValue).add(additionalOverhead);
        BigDecimal totalCosts = totalOverhead.add(totalTechnicalProductionCosts);

        costCalculation.setField("productionCostMarginValue", productionCostMarginValue);
        costCalculation.setField("productionCostMarginValue", productionCostMarginValue);
        costCalculation.setField("materialCostMarginValue", materialCostMarginValue);
        costCalculation.setField("totalOverhead", totalOverhead);
        costCalculation.setField("totalTechnicalProductionCosts", totalTechnicalProductionCosts);
        costCalculation.setField("totalCosts", totalCosts);
        costCalculation.setField("totalCostsPerUnit", totalCosts.divide(quantity, 3));

        return costCalculation;
    }

    private OperationsCostCalculationConstants getOperationModeFromField(Object value) {
        checkArgument(value != null, "field value is null");
        return OperationsCostCalculationConstants.valueOf(value.toString().toUpperCase());
    }

    private BigDecimal getBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.toString());
    }
    
    public void copyTechnologyTree(final DataDefinition dd, final Entity costCalculation) {
        operationsCostCalculationService.copyTechnologyTree(dd, costCalculation);
    }
}
