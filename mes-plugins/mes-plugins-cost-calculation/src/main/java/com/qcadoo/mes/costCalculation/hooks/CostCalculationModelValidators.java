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
package com.qcadoo.mes.costCalculation.hooks;

import static com.qcadoo.mes.costCalculation.constants.CostCalculationFields.CALCULATE_MATERIAL_COSTS_MODE;
import static com.qcadoo.mes.costCalculation.constants.CostCalculationFields.SOURCE_OF_MATERIAL_COSTS;
import static com.qcadoo.mes.costCalculation.constants.CostCalculationFields.TECHNOLOGY;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.STATE;
import static com.qcadoo.mes.technologies.states.constants.TechnologyState.DECLINED;
import static com.qcadoo.mes.technologies.states.constants.TechnologyState.DRAFT;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.constants.CalculateMaterialCostsMode;
import com.qcadoo.mes.costCalculation.constants.SourceOfMaterialCosts;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class CostCalculationModelValidators {

    public boolean checkIfTheTechnologyHasCorrectState(final DataDefinition dataDefinition, final Entity costCalculation) {
        Entity technology = costCalculation.getBelongsToField(TECHNOLOGY);
        if (technology.getStringField(STATE).equals(DRAFT.getStringValue())
                || technology.getStringField(STATE).equals(DECLINED.getStringValue())) {
            costCalculation.addError(dataDefinition.getField(TECHNOLOGY), "costNormsForOperation.messages.fail.incorrectState");
            return false;
        }
        return true;
    }

    public boolean checkIfCurrentGlobalIsSelected(final DataDefinition costCalculationDD, final Entity costCalculation) {
        if (SourceOfMaterialCosts.CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT.getStringValue().equals(
                costCalculation.getField(SOURCE_OF_MATERIAL_COSTS))
                && CalculateMaterialCostsMode.COST_FOR_ORDER.getStringValue().equals(
                        costCalculation.getField(CALCULATE_MATERIAL_COSTS_MODE))) {
            costCalculation.addError(costCalculationDD.getField(CALCULATE_MATERIAL_COSTS_MODE),
                    "costCalculation.messages.optionUnavailable");
            return false;
        }

        return true;
    }

    public boolean ifSourceOfMaterialIsFromOrderThenOrderIsNeeded(final DataDefinition costCalculationDD,
            final Entity costCalculation) {
        if (SourceOfMaterialCosts.FROM_ORDERS_MATERIAL_COSTS.getStringValue().equals(
                costCalculation.getField(SOURCE_OF_MATERIAL_COSTS))
                && costCalculation.getBelongsToField("order") == null) {
            costCalculation.addError(costCalculationDD.getField(SOURCE_OF_MATERIAL_COSTS),
                    "costCalculation.messages.sourceOfMaterialFromOrderRequiresAnOrder");
            return false;
        }

        return true;
    }
}
