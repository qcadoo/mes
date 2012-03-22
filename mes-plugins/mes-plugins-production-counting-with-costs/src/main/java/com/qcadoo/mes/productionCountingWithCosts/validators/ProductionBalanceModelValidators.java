package com.qcadoo.mes.productionCountingWithCosts.validators;

import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.CUMULATED;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.CALCULATE_MATERIAL_COSTS_MODE;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.SOURCE_OF_MATERIAL_COSTS;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.constants.CalculateMaterialCostsMode;
import com.qcadoo.mes.costCalculation.constants.SourceOfMaterialCosts;
import com.qcadoo.mes.orders.constants.OrdersConstants;
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

    public boolean checkIfAverageCostsIsDefined(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        Entity order = productionBalance.getBelongsToField(OrdersConstants.MODEL_ORDER);
        if ((checkIfTypeOfProductionRecordingIsCumulated(order)) && productionBalance.getBooleanField("generated") == false) {

            Object averageMachineHourlyCost = productionBalance.getField("averageMachineHourlyCost");
            Object averageLaborHourlyCost = productionBalance.getField("averageLaborHourlyCost");

            if ((averageLaborHourlyCost == null) && (averageMachineHourlyCost == null)) {
                productionBalance.addError(productionBalanceDD.getField("averageLaborHourlyCost"),
                        "productionCountingWithCosts.productionBalance.messages.averageLaborHourlyCostIsRequired");
                productionBalance.addError(productionBalanceDD.getField("averageMachineHourlyCost"),
                        "productionCountingWithCosts.productionBalance.messages.averageMachineHourlyCostIsRequired");
                return false;

            } else if ((averageMachineHourlyCost == null)) {
                productionBalance.addError(productionBalanceDD.getField("averageMachineHourlyCost"),
                        "productionCountingWithCosts.productionBalance.messages.averageMachineHourlyCostIsRequired");
                return false;

            } else if ((averageLaborHourlyCost == null)) {
                productionBalance.addError(productionBalanceDD.getField("averageLaborHourlyCost"),
                        "productionCountingWithCosts.productionBalance.messages.averageMachineHourlyCostIsRequired");
                return false;
            }

        }
        return true;
    }

    public boolean checkIfTypeOfProductionRecordingIsCumulated(final Entity order) {
        return CUMULATED.getStringValue().equals(order.getStringField("typeOfProductionRecording"));
    }

}
