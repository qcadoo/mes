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
package com.qcadoo.mes.costCalculation.validators;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.constants.MaterialCostsUsed;
import com.qcadoo.mes.costCalculation.constants.SourceOfOperationCosts;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class CostCalculationValidators {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING = "qcadooView.validate.field.error.missing";

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity costCalculation) {
        return checkRequiredFields(dataDefinition, costCalculation);
    }

    public boolean checkRequiredFields(final DataDefinition dataDefinition, final Entity costCalculation) {
        boolean isValid = true;
        String sourceOfOperationCosts = costCalculation.getStringField(CostCalculationFields.SOURCE_OF_OPERATION_COSTS);
        if (SourceOfOperationCosts.STANDARD_LABOR_COSTS.getStringValue().equals(sourceOfOperationCosts)
                && costCalculation.getBelongsToField(CostCalculationFields.STANDARD_LABOR_COST) == null) {
            costCalculation.addError(dataDefinition.getField(CostCalculationFields.STANDARD_LABOR_COST),
                    L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
            isValid = false;
        }
        String materialCostsUsed = costCalculation.getStringField(CostCalculationFields.MATERIAL_COSTS_USED);
        if (MaterialCostsUsed.OFFER_COST_OR_LAST_PURCHASE.getStringValue().equals(materialCostsUsed)
                && costCalculation.getBelongsToField(CostCalculationFields.OFFER) == null) {
            costCalculation.addError(dataDefinition.getField(CostCalculationFields.OFFER),
                    L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
            isValid = false;
        }
        return isValid;
    }

}
