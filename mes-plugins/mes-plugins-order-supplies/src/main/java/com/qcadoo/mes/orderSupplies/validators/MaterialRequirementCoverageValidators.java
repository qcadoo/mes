/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.orderSupplies.validators;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orderSupplies.constants.MaterialRequirementCoverageFields;
import com.qcadoo.mes.productionCounting.constants.ParameterFieldsPC;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class MaterialRequirementCoverageValidators {

    @Autowired
    private ParameterService parameterService;

    public boolean validatesWith(final DataDefinition materialRequirementCoverageDD, final Entity materialRequirementCoverage) {
        boolean isValid = checkCoverageDates(materialRequirementCoverageDD, materialRequirementCoverage);
        isValid = isValid && checkCoverageLocations(materialRequirementCoverage);
        isValid = isValid && checkIfParametersSet(materialRequirementCoverage);
        return isValid;
    }

    private boolean checkIfParametersSet(Entity materialRequirementCoverage) {
        Entity parameter = parameterService.getParameter();
        if (StringUtils.isEmpty(parameter.getStringField(ParameterFieldsPC.MATERIAL_COSTS_USED_PB))) {

            materialRequirementCoverage.addGlobalError("orderSupplies.materialRequirementCoverage.materialCostsUsedPB.empty");

            return false;
        }

        return true;

    }

    private boolean checkCoverageDates(final DataDefinition materialRequirementCoverageDD,
                                       final Entity materialRequirementCoverage) {
        Date actualDate = materialRequirementCoverage.getDateField(MaterialRequirementCoverageFields.ACTUAL_DATE);
        Date coverageToDate = materialRequirementCoverage.getDateField(MaterialRequirementCoverageFields.COVERAGE_TO_DATE);

        if (coverageToDate.before(actualDate)) {
            materialRequirementCoverage.addError(
                    materialRequirementCoverageDD.getField(MaterialRequirementCoverageFields.COVERAGE_TO_DATE),
                    "orderSupplies.materialRequirementCoverage.covarageToDate.isBeforeActualDate");

            return false;
        }

        return true;
    }

    private boolean checkCoverageLocations(final Entity materialRequirementCoverage) {
        List<Entity> coverageLocations = materialRequirementCoverage
                .getHasManyField(MaterialRequirementCoverageFields.COVERAGE_LOCATIONS);

        if (coverageLocations.isEmpty()) {
            materialRequirementCoverage.addGlobalError("orderSupplies.materialRequirementCoverage.emptyCoverageLocations");

            return false;
        }

        return true;
    }
}
