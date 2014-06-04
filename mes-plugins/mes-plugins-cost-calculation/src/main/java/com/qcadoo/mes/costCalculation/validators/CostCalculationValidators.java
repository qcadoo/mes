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
package com.qcadoo.mes.costCalculation.validators;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.constants.CalculateMaterialCostsMode;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.constants.SourceOfMaterialCosts;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class CostCalculationValidators {

    public boolean checkIfTheTechnologyHasCorrectState(final DataDefinition costCalculationDD, final Entity costCalculation) {
        Entity technology = costCalculation.getBelongsToField(CostCalculationFields.TECHNOLOGY);
        String state = technology.getStringField(TechnologyFields.STATE);

        if (TechnologyState.DRAFT.getStringValue().equals(state) || TechnologyState.DECLINED.getStringValue().equals(state)) {
            costCalculation.addError(costCalculationDD.getField(CostCalculationFields.TECHNOLOGY),
                    "costNormsForOperation.messages.fail.incorrectState");

            return false;
        }

        return true;
    }

    public boolean checkIfCurrentGlobalIsSelected(final DataDefinition costCalculationDD, final Entity costCalculation) {
        String sourceOfMaterialCosts = costCalculation.getStringField(CostCalculationFields.SOURCE_OF_MATERIAL_COSTS);
        String calculateMaterialCostsMode = costCalculation.getStringField(CostCalculationFields.CALCULATE_MATERIAL_COSTS_MODE);

        if (SourceOfMaterialCosts.CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT.getStringValue().equals(sourceOfMaterialCosts)
                && CalculateMaterialCostsMode.COST_FOR_ORDER.getStringValue().equals(calculateMaterialCostsMode)) {
            costCalculation.addError(costCalculationDD.getField(CostCalculationFields.CALCULATE_MATERIAL_COSTS_MODE),
                    "costCalculation.messages.optionUnavailable");

            return false;
        }

        return true;
    }

    public boolean ifSourceOfMaterialIsFromOrderThenOrderIsNeeded(final DataDefinition costCalculationDD,
            final Entity costCalculation) {
        String sourceOfMaterialCosts = costCalculation.getStringField(CostCalculationFields.SOURCE_OF_MATERIAL_COSTS);
        Entity order = costCalculation.getBelongsToField(CostCalculationFields.ORDER);

        if (SourceOfMaterialCosts.FROM_ORDERS_MATERIAL_COSTS.getStringValue().equals(sourceOfMaterialCosts) && (order == null)) {
            costCalculation.addError(costCalculationDD.getField(CostCalculationFields.SOURCE_OF_MATERIAL_COSTS),
                    "costCalculation.messages.sourceOfMaterialFromOrderRequiresAnOrder");

            return false;
        }

        return true;
    }

}
