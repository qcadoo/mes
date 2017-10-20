/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.productionCounting.validators;

import com.qcadoo.mes.costCalculation.constants.CalculateMaterialCostsMode;
import com.qcadoo.mes.costCalculation.constants.SourceOfMaterialCosts;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductionBalanceValidators {

    @Autowired
    private ProductionCountingService productionCountingService;

    public boolean validatesWith(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        boolean isValid = true;

        isValid = isValid && checkIfOptionsAreNotNull(productionBalanceDD, productionBalance);
        isValid = isValid && checkIfOptionsAreAvailable(productionBalanceDD, productionBalance);
        isValid = isValid && checkIfAverageCostsAreDefined(productionBalanceDD, productionBalance);

        return isValid
                && ProductionBalanceType.MANY_ORDERS.getStringValue().equals(
                        productionBalance.getStringField(ProductionBalanceFields.TYPE))
                || validateOrder(productionBalanceDD, productionBalance);
    }

    private boolean validateOrder(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        return productionCountingService.validateOrder(productionBalanceDD, productionBalance);
    }

    public boolean checkIfOptionsAreNotNull(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        String sourceOfMaterialCost = productionBalance.getStringField(ProductionBalanceFields.SOURCE_OF_MATERIAL_COSTS);
        String calculateMaterialCostsMode = productionBalance
                .getStringField(ProductionBalanceFields.CALCULATE_MATERIAL_COSTS_MODE);

        if (sourceOfMaterialCost == null) {
            productionBalance.addError(productionBalanceDD.getField(ProductionBalanceFields.SOURCE_OF_MATERIAL_COSTS),
                    "productionCounting.productionBalance.error.sourceOfMaterialCostsIsNotSelected");
        }

        if (calculateMaterialCostsMode == null) {
            productionBalance.addError(productionBalanceDD.getField(ProductionBalanceFields.CALCULATE_MATERIAL_COSTS_MODE),
                    "productionCounting.productionBalance.error.calculateMaterialCostsModeIsNotSelected");
        }

        if ((sourceOfMaterialCost == null) || (calculateMaterialCostsMode == null)) {
            return false;
        }

        return true;
    }

    public boolean checkIfOptionsAreAvailable(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        String sourceOfMaterialCost = productionBalance.getStringField(ProductionBalanceFields.SOURCE_OF_MATERIAL_COSTS);
        String calculateMaterialCostsMode = productionBalance
                .getStringField(ProductionBalanceFields.CALCULATE_MATERIAL_COSTS_MODE);

        if (SourceOfMaterialCosts.CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT.getStringValue().equals(sourceOfMaterialCost)
                && CalculateMaterialCostsMode.COST_FOR_ORDER.getStringValue().equals(calculateMaterialCostsMode)) {
            productionBalance.addError(productionBalanceDD.getField(ProductionBalanceFields.CALCULATE_MATERIAL_COSTS_MODE),
                    "productionCounting.productionBalance.messages.optionUnavailable");

            return false;
        }

        if(SourceOfMaterialCosts.FROM_ORDERS_MATERIAL_COSTS.getStringValue().equals(
                productionBalance.getStringField(ProductionBalanceFields.SOURCE_OF_MATERIAL_COSTS))
                && !CalculateMaterialCostsMode.COST_FOR_ORDER.getStringValue().equals(
                productionBalance.getStringField(ProductionBalanceFields.CALCULATE_MATERIAL_COSTS_MODE))){
            productionBalance.addError(productionBalanceDD.getField(ProductionBalanceFields.CALCULATE_MATERIAL_COSTS_MODE), "basic.parameter.sourceOfMaterialCostsPB.calculateMaterialCostsModePBWrongValue");
            return false;
        }
        return true;
    }

    public boolean checkIfAverageCostsAreDefined(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);
        if (order != null
                && productionCountingService.isCalculateOperationCostModeHourly(productionBalance
                        .getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE))
                && productionCountingService.isTypeOfProductionRecordingCumulated(order
                        .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            Object averageMachineHourlyCost = productionBalance.getField(ProductionBalanceFields.AVERAGE_MACHINE_HOURLY_COST);
            Object averageLaborHourlyCost = productionBalance.getField(ProductionBalanceFields.AVERAGE_LABOR_HOURLY_COST);

            if ((averageLaborHourlyCost == null) && (averageMachineHourlyCost == null)) {
                productionBalance.addError(productionBalanceDD.getField(ProductionBalanceFields.AVERAGE_MACHINE_HOURLY_COST),
                        "productionCounting.productionBalance.messages.averageLaborHourlyCostIsRequired");
                productionBalance.addError(productionBalanceDD.getField(ProductionBalanceFields.AVERAGE_LABOR_HOURLY_COST),
                        "productionCounting.productionBalance.messages.averageMachineHourlyCostIsRequired");
                return false;
            } else if ((averageMachineHourlyCost == null)) {
                productionBalance.addError(productionBalanceDD.getField(ProductionBalanceFields.AVERAGE_MACHINE_HOURLY_COST),
                        "productionCounting.productionBalance.messages.averageMachineHourlyCostIsRequired");
                return false;
            } else if ((averageLaborHourlyCost == null)) {
                productionBalance.addError(productionBalanceDD.getField(ProductionBalanceFields.AVERAGE_LABOR_HOURLY_COST),
                        "productionCounting.productionBalance.messages.averageMachineHourlyCostIsRequired");
                return false;
            }
        }

        return true;
    }

}
