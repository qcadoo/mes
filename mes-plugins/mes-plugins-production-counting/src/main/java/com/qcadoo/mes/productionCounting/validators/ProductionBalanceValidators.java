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

import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.constants.CalculateMaterialCostsMode;
import com.qcadoo.mes.costCalculation.constants.SourceOfMaterialCosts;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionBalanceValidators {

    public boolean validatesWith(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        boolean isValid = checkIfOptionsAreNotNull(productionBalanceDD, productionBalance);
        isValid = isValid && checkIfOptionsAreAvailable(productionBalanceDD, productionBalance);

        return isValid;
    }

    private boolean checkIfOptionsAreNotNull(final DataDefinition productionBalanceDD, final Entity productionBalance) {
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

        return (sourceOfMaterialCost != null) && (calculateMaterialCostsMode != null);
    }

    private boolean checkIfOptionsAreAvailable(final DataDefinition productionBalanceDD, final Entity productionBalance) {
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
}
