/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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

import static com.qcadoo.mes.productionCounting.internal.constants.CalculateOperationCostsMode.HOURLY;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.CUMULATED;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.AVERAGE_LABOR_HOURLY_COST;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.AVERAGE_MACHINE_HOURLY_COST;
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

    public boolean checkIfAverageCostsAreDefined(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        Entity order = productionBalance.getBelongsToField(ORDER);
        if (HOURLY.getStringValue().equals(productionBalance.getStringField(CALCULATE_OPERATION_COST_MODE))
                && checkIfTypeOfProductionRecordingIsCumulated(order)) {
            Object averageMachineHourlyCost = productionBalance.getField(AVERAGE_MACHINE_HOURLY_COST);
            Object averageLaborHourlyCost = productionBalance.getField(AVERAGE_LABOR_HOURLY_COST);

            if ((averageLaborHourlyCost == null) && (averageMachineHourlyCost == null)) {
                productionBalance.addError(productionBalanceDD.getField(AVERAGE_MACHINE_HOURLY_COST),
                        "productionCountingWithCosts.productionBalance.messages.averageLaborHourlyCostIsRequired");
                productionBalance.addError(productionBalanceDD.getField(AVERAGE_LABOR_HOURLY_COST),
                        "productionCountingWithCosts.productionBalance.messages.averageMachineHourlyCostIsRequired");
                return false;

            } else if ((averageMachineHourlyCost == null)) {
                productionBalance.addError(productionBalanceDD.getField(AVERAGE_MACHINE_HOURLY_COST),
                        "productionCountingWithCosts.productionBalance.messages.averageMachineHourlyCostIsRequired");
                return false;

            } else if ((averageLaborHourlyCost == null)) {
                productionBalance.addError(productionBalanceDD.getField(AVERAGE_LABOR_HOURLY_COST),
                        "productionCountingWithCosts.productionBalance.messages.averageMachineHourlyCostIsRequired");
                return false;
            }

        }
        return true;
    }

    public boolean checkIfTypeOfProductionRecordingIsCumulated(final Entity order) {
        return CUMULATED.getStringValue().equals(order.getStringField(TYPE_OF_PRODUCTION_RECORDING));
    }

}
