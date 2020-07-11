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
package com.qcadoo.mes.orderSupplies.validators;

import com.qcadoo.mes.orderSupplies.constants.CoverageLocationFields;
import com.qcadoo.mes.orderSupplies.constants.MaterialRequirementCoverageFields;
import com.qcadoo.mes.orderSupplies.constants.ParameterFieldsOS;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoverageLocationValidators {

    public boolean validatesWith(final DataDefinition coverageLocationDD, final Entity coverageLocation) {
        return checkIfCoverageLocationIsNotAlreadyUsed(coverageLocationDD, coverageLocation);
    }

    private boolean checkIfCoverageLocationIsNotAlreadyUsed(final DataDefinition coverageLocationDD, final Entity coverageLocation) {
        Entity location = coverageLocation.getBelongsToField(CoverageLocationFields.LOCATION);
        Entity parameter = coverageLocation.getBelongsToField(CoverageLocationFields.PARAMETER);
        Entity materialRequirementCoverage = coverageLocation
                .getBelongsToField(CoverageLocationFields.MATERIAL_REQUIREMENT_COVERAGE);

        if (location != null) {
            if (parameter != null) {
                return checkIfCoverageLocationIsNotAlreadyUsed(coverageLocationDD, coverageLocation,
                        parameter.getHasManyField(ParameterFieldsOS.COVERAGE_LOCATIONS));
            } else if (materialRequirementCoverage != null) {
                return checkIfCoverageLocationIsNotAlreadyUsed(coverageLocationDD, coverageLocation,
                        materialRequirementCoverage.getHasManyField(MaterialRequirementCoverageFields.COVERAGE_LOCATIONS));
            }
        }

        return true;
    }

    private boolean checkIfCoverageLocationIsNotAlreadyUsed(final DataDefinition coverageLocationDD,
            final Entity coverageLocation, final List<Entity> coverageLocations) {
        Entity location = coverageLocation.getBelongsToField(CoverageLocationFields.LOCATION);

        for (Entity addedCoverageLocation : coverageLocations) {
            Entity addedLocation = addedCoverageLocation.getBelongsToField(CoverageLocationFields.LOCATION);

            if (isCoverageLocationAlreadyUsed(coverageLocation, addedCoverageLocation, location, addedLocation)) {
                coverageLocation.addError(coverageLocationDD.getField(CoverageLocationFields.LOCATION),
                        "orderSupplies.materialRequirementCoverage.coverageLocation.isAlreadyUsed");

                return false;
            }
        }

        return true;
    }

    private boolean isCoverageLocationAlreadyUsed(final Entity coverageLocation, final Entity addedCoverageLocation,
            final Entity location, final Entity addedLocation) {
        return ((coverageLocation.getId() == null) || !coverageLocation.getId().equals(addedCoverageLocation.getId()))
                && addedLocation.getId().equals(location.getId());
    }

}
