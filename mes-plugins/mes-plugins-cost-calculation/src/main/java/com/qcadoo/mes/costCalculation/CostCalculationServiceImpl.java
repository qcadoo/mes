package com.qcadoo.mes.costCalculation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.costCalculation.constants.CostCalculateConstants.MODEL_COST_CALCULATION;
import static com.qcadoo.mes.costCalculation.constants.CostCalculateConstants.PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants.HOURLY;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_TECHNOLOGY;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costNormsForOperation.OperationsCostCalculationService;
import com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants;
import com.qcadoo.mes.costNormsForProduct.ProductsCostCalculationService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class CostCalculationServiceImpl implements CostCalculationService {

    // @Autowired
    // private DataDefinitionService dataDefinitionService;

    @Autowired
    private OperationsCostCalculationService operationsCostCalculationService;

    @Autowired
    private ProductsCostCalculationService productsCostCalculationService;

    /*
     * public Entity calculateTotalCost(final Entity givenSource, final Map<String, Object> parameters) {
     * checkArgument(givenSource != null && givenSource.getDataDefinition() != null, "incompatible source entity");
     * checkArgument(parameters.size() != 0, "parameter is empty"); Entity costCalculation =
     * dataDefinitionService.get(PLUGIN_IDENTIFIER, MODEL_COST_CALCULATION).create(); for (String key : parameters.keySet()) {
     * costCalculation.setField(key, parameters.get(key)); }
     */
    public Entity calculateTotalCost(final Entity costCalculation) {

        BigDecimal productionCosts;
        BigDecimal materialCostMargin = getBigDecimal(costCalculation.getField("materialCostMargin"));
        BigDecimal productionCostMargin = getBigDecimal(costCalculation.getField("productionCostMargin"));
        BigDecimal additionalOverhead = getBigDecimal(costCalculation.getField("additionalOverhead"));
        BigDecimal quantity = getBigDecimal(costCalculation.getField("quantity"));
        // Entity technology;
        OperationsCostCalculationConstants operationMode = getOperationModeFromField(costCalculation
                .getField("calculateOperationCostsMode"));

        // Be sure that source Entity isn't in detached state
        // Entity source = dd.get(givenSource.getId());
        // Entity order = costCalculation.getBelongsToField("order");

        // if (MODEL_TECHNOLOGY.equals(dd.getName())) {
        // technology = source;
        // } else if (MODEL_ORDER.equals(dd.getName())) {
        // technology = source.getBelongsToField("technology");
        // } else {
        // throw new IllegalArgumentException("incompatible source entity!");
        // }

        // costCalculation.setField("technology", technology);

        costCalculation.setField("dateOfCalculation", new Date());
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
}
