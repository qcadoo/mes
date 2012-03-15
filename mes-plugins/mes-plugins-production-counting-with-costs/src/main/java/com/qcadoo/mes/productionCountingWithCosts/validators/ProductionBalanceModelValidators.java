package com.qcadoo.mes.productionCountingWithCosts.validators;

import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.CALCULATE_MATERIAL_COSTS_MODE;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.SOURCE_OF_MATERIAL_COSTS;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.constants.CalculateMaterialCostsMode;
import com.qcadoo.mes.costCalculation.constants.SourceOfMaterialCosts;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionBalanceModelValidators {

    public boolean checkIfOptionsAreNotNull(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        String sourceOfMaterialCost = productionBalance.getStringField(SOURCE_OF_MATERIAL_COSTS);
        String calculateMaterialCostsMode = productionBalance.getStringField(CALCULATE_MATERIAL_COSTS_MODE);

        if (sourceOfMaterialCost == null) {
            productionBalance.addError(productionBalanceDD.getField(SOURCE_OF_MATERIAL_COSTS),
                    "productionCounting.productionBalance.error.sourceOfMaterialCostsIsNotSelected");
        }

        if (calculateMaterialCostsMode == null) {
            productionBalance.addError(productionBalanceDD.getField(CALCULATE_MATERIAL_COSTS_MODE),
                    "productionCounting.productionBalance.error.calculateMaterialCostsModeIsNotSelected");
        }

        if ((sourceOfMaterialCost == null) || (calculateMaterialCostsMode == null)) {
            return false;
        }

        return true;
    }

    public boolean checkIfOptionsAreAvailable(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        String sourceOfMaterialCost = productionBalance.getStringField(SOURCE_OF_MATERIAL_COSTS);
        String calculateMaterialCostsMode = productionBalance.getStringField(CALCULATE_MATERIAL_COSTS_MODE);

        if (SourceOfMaterialCosts.CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT.getStringValue().equals(sourceOfMaterialCost)
                && CalculateMaterialCostsMode.COST_FOR_ORDER.getStringValue().equals(calculateMaterialCostsMode)) {
            productionBalance.addError(productionBalanceDD.getField(CALCULATE_MATERIAL_COSTS_MODE),
                    "productionCountingWithCosts.productionBalance.messages.optionUnavailable");

            return false;
        }

        return true;
    }

}
