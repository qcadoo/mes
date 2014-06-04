/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productionCountingWithCosts.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.constants.CalculateMaterialCostsMode;
import com.qcadoo.mes.costCalculation.constants.SourceOfMaterialCosts;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionBalanceValidatorsPCWC {

    @Autowired
    private ProductionCountingService productionCountingService;

    public boolean validatesWith(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        boolean isValid = true;

        isValid = isValid && checkIfOptionsAreNotNull(productionBalanceDD, productionBalance);
        isValid = isValid && checkIfOptionsAreAvailable(productionBalanceDD, productionBalance);
        isValid = isValid && checkIfAverageCostsAreDefined(productionBalanceDD, productionBalance);

        return isValid;
    }

    public boolean checkIfOptionsAreNotNull(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        String sourceOfMaterialCost = productionBalance.getStringField(ProductionBalanceFieldsPCWC.SOURCE_OF_MATERIAL_COSTS);
        String calculateMaterialCostsMode = productionBalance
                .getStringField(ProductionBalanceFieldsPCWC.CALCULATE_MATERIAL_COSTS_MODE);

        if (sourceOfMaterialCost == null) {
            productionBalance.addError(productionBalanceDD.getField(ProductionBalanceFieldsPCWC.SOURCE_OF_MATERIAL_COSTS),
                    "productionCounting.productionBalance.error.sourceOfMaterialCostsIsNotSelected");
        }

        if (calculateMaterialCostsMode == null) {
            productionBalance.addError(productionBalanceDD.getField(ProductionBalanceFieldsPCWC.CALCULATE_MATERIAL_COSTS_MODE),
                    "productionCounting.productionBalance.error.calculateMaterialCostsModeIsNotSelected");
        }

        if ((sourceOfMaterialCost == null) || (calculateMaterialCostsMode == null)) {
            return false;
        }

        return true;
    }

    public boolean checkIfOptionsAreAvailable(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        String sourceOfMaterialCost = productionBalance.getStringField(ProductionBalanceFieldsPCWC.SOURCE_OF_MATERIAL_COSTS);
        String calculateMaterialCostsMode = productionBalance
                .getStringField(ProductionBalanceFieldsPCWC.CALCULATE_MATERIAL_COSTS_MODE);

        if (SourceOfMaterialCosts.CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT.getStringValue().equals(sourceOfMaterialCost)
                && CalculateMaterialCostsMode.COST_FOR_ORDER.getStringValue().equals(calculateMaterialCostsMode)) {
            productionBalance.addError(productionBalanceDD.getField(ProductionBalanceFieldsPCWC.CALCULATE_MATERIAL_COSTS_MODE),
                    "productionCountingWithCosts.productionBalance.messages.optionUnavailable");

            return false;
        }

        return true;
    }

    public boolean checkIfAverageCostsAreDefined(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);
        if (productionCountingService.isCalculateOperationCostModeHourly(productionBalance
                .getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE))
                && productionCountingService.isTypeOfProductionRecordingCumulated(order
                        .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            Object averageMachineHourlyCost = productionBalance.getField(ProductionBalanceFieldsPCWC.AVERAGE_MACHINE_HOURLY_COST);
            Object averageLaborHourlyCost = productionBalance.getField(ProductionBalanceFieldsPCWC.AVERAGE_LABOR_HOURLY_COST);

            if ((averageLaborHourlyCost == null) && (averageMachineHourlyCost == null)) {
                productionBalance.addError(productionBalanceDD.getField(ProductionBalanceFieldsPCWC.AVERAGE_MACHINE_HOURLY_COST),
                        "productionCountingWithCosts.productionBalance.messages.averageLaborHourlyCostIsRequired");
                productionBalance.addError(productionBalanceDD.getField(ProductionBalanceFieldsPCWC.AVERAGE_LABOR_HOURLY_COST),
                        "productionCountingWithCosts.productionBalance.messages.averageMachineHourlyCostIsRequired");
                return false;
            } else if ((averageMachineHourlyCost == null)) {
                productionBalance.addError(productionBalanceDD.getField(ProductionBalanceFieldsPCWC.AVERAGE_MACHINE_HOURLY_COST),
                        "productionCountingWithCosts.productionBalance.messages.averageMachineHourlyCostIsRequired");
                return false;
            } else if ((averageLaborHourlyCost == null)) {
                productionBalance.addError(productionBalanceDD.getField(ProductionBalanceFieldsPCWC.AVERAGE_LABOR_HOURLY_COST),
                        "productionCountingWithCosts.productionBalance.messages.averageMachineHourlyCostIsRequired");
                return false;
            }
        }

        return true;
    }

}
