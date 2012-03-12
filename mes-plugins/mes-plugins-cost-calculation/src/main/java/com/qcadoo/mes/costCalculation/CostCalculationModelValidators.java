/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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
package com.qcadoo.mes.costCalculation;

import static com.qcadoo.mes.costCalculation.constants.CalculateMaterialCostsMode.COST_FOR_ORDER;
import static com.qcadoo.mes.costCalculation.constants.CostCalculationFields.CALCULATE_MATERIAL_COSTS_MODE;
import static com.qcadoo.mes.costCalculation.constants.CostCalculationFields.SOURCE_OF_MATERIAL_COSTS;
import static com.qcadoo.mes.costCalculation.constants.CostCalculationFields.TECHNOLOGY;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.STATE;
import static com.qcadoo.mes.technologies.constants.TechnologyState.DECLINED;
import static com.qcadoo.mes.technologies.constants.TechnologyState.DRAFT;

import org.springframework.stereotype.Service;

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
        if ((costCalculation.getField(SOURCE_OF_MATERIAL_COSTS).equals("01currentGlobalDefinitionsInProduct"))
                && (costCalculation.getField(CALCULATE_MATERIAL_COSTS_MODE).equals(COST_FOR_ORDER.getStringValue()))) {
            costCalculation.addError(costCalculationDD.getField(CALCULATE_MATERIAL_COSTS_MODE),
                    "costCalculation.messages.optionUnavailable");
            return false;
        } else {
            return true;
        }
    }

}
