/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.materialRequirementCoverageForOrder.validators;

import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.CoverageForOrderFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CoverageForOrderValidators {

    public boolean validatesWith(final DataDefinition materialRequirementCoverageDD, final Entity materialRequirementCoverage) {
        boolean isValid = checkCoverageDates(materialRequirementCoverageDD, materialRequirementCoverage);
        isValid = isValid && checkCoverageLocations(materialRequirementCoverage);
        isValid = isValid
                && checkIfCoverageToDateIsBeforeStartOrderDate(materialRequirementCoverageDD, materialRequirementCoverage);
        return isValid;
    }

    private boolean checkIfCoverageToDateIsBeforeStartOrderDate(final DataDefinition materialRequirementCoverageDD,
            final Entity materialRequirementCoverage) {
        Entity order = materialRequirementCoverage.getBelongsToField(CoverageForOrderFields.ORDER);
        Date coverageToDate = materialRequirementCoverage.getDateField(CoverageForOrderFields.COVERAGE_TO_DATE);

        Date orderStartDate = materialRequirementCoverage.getBelongsToField(CoverageForOrderFields.ORDER)
                .getDateField(OrderFields.START_DATE);
        if (OrderState.PENDING.getStringValue().equals(order.getStringField(OrderFields.STATE)) && orderStartDate == null) {
            return true;
        }
        if (orderStartDate.before(coverageToDate)) {
            materialRequirementCoverage.addError(materialRequirementCoverageDD.getField(CoverageForOrderFields.COVERAGE_TO_DATE),
                    "materialRequirementCoverageForOrder.coverageForOrder.covarageToDate.isBeforeOrderStartDate");

            return false;
        }

        return true;
    }

    private boolean checkCoverageDates(final DataDefinition materialRequirementCoverageDD,
            final Entity materialRequirementCoverage) {
        Date actualDate = materialRequirementCoverage.getDateField(CoverageForOrderFields.ACTUAL_DATE);
        Date coverageToDate = materialRequirementCoverage.getDateField(CoverageForOrderFields.COVERAGE_TO_DATE);

        if (coverageToDate.before(actualDate)) {
            materialRequirementCoverage.addError(materialRequirementCoverageDD.getField(CoverageForOrderFields.COVERAGE_TO_DATE),
                    "materialRequirementCoverageForOrder.coverageForOrder.covarageToDate.isBeforeActualDate");

            return false;
        }

        return true;
    }

    private boolean checkCoverageLocations(final Entity materialRequirementCoverage) {
        List<Entity> coverageLocations = materialRequirementCoverage.getHasManyField(CoverageForOrderFields.COVERAGE_LOCATIONS);

        if (coverageLocations.isEmpty()) {
            materialRequirementCoverage
                    .addGlobalError("materialRequirementCoverageForOrder.coverageForOrder.emptyCoverageLocations");

            return false;
        }

        return true;
    }
}
