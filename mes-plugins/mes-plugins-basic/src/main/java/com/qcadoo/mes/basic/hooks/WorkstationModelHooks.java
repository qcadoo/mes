/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.basic.hooks;

import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.basic.states.constants.WorkstationStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class WorkstationModelHooks {

    private static final String L_MM = "mm";

    private static final String L_CM = "cm";

    @Autowired
    private UnitConversionService unitConversionService;

    public boolean validatesWith(final DataDefinition workstationDD, final Entity workstation) {
        boolean isValid = true;

        BigDecimal minimumDimension = workstation.getDecimalField(WorkstationFields.MINIMUM_DIMENSION);
        BigDecimal maximumDimension = workstation.getDecimalField(WorkstationFields.MAXIMUM_DIMENSION);
        String minimumDimensionUnit = workstation.getStringField(WorkstationFields.MINIMUM_DIMENSION_UNIT);
        String maximumDimensionUnit = workstation.getStringField(WorkstationFields.MAXIMUM_DIMENSION_UNIT);

        if (Objects.nonNull(minimumDimension) && StringUtils.isEmpty(minimumDimensionUnit)) {
            workstation.addError(workstationDD.getField(WorkstationFields.MINIMUM_DIMENSION_UNIT), "qcadooView.validate.field.error.missing");

            isValid = false;
        }

        if (Objects.nonNull(maximumDimension) && StringUtils.isEmpty(maximumDimensionUnit)) {
            workstation.addError(workstationDD.getField(WorkstationFields.MAXIMUM_DIMENSION_UNIT), "qcadooView.validate.field.error.missing");

            isValid = false;
        }

        if (Objects.nonNull(minimumDimension) && Objects.nonNull(maximumDimension)
                && Objects.nonNull(minimumDimensionUnit) && Objects.nonNull(maximumDimensionUnit)) {
            if (!minimumDimensionUnit.equals(maximumDimensionUnit)) {
                minimumDimension = convertToMM(minimumDimension, minimumDimensionUnit);
                maximumDimension = convertToMM(maximumDimension, maximumDimensionUnit);
            }

            if (Objects.nonNull(minimumDimension) && Objects.nonNull(maximumDimension)
                    && minimumDimension.compareTo(maximumDimension) > 0) {
                workstation.addError(workstationDD.getField(WorkstationFields.MAXIMUM_DIMENSION), "basic.workstation.maximumDimension.smallerThanMinimum");

                isValid = false;
            }
        }

        return isValid;
    }

    private BigDecimal convertToMM(final BigDecimal dimension, final String unit) {
        if (L_MM.equals(unit)) {
            return dimension;
        } else {
            PossibleUnitConversions possibleUnitConversions = unitConversionService.getPossibleConversions(unit, L_CM);

            if (possibleUnitConversions.isDefinedFor(L_MM)) {
                return possibleUnitConversions.convertTo(dimension, L_MM);
            }
        }

        return null;
    }

    public void onCreate(final DataDefinition workstationDD, final Entity workstation) {
        workstation.setField(WorkstationFields.STATE, WorkstationStateStringValues.STOPPED);
    }

    public void onSave(final DataDefinition workstationDD, final Entity workstation) {
        BigDecimal minimumDimension = workstation.getDecimalField(WorkstationFields.MINIMUM_DIMENSION);
        BigDecimal maximumDimension = workstation.getDecimalField(WorkstationFields.MAXIMUM_DIMENSION);

        if (Objects.isNull(minimumDimension)) {
            workstation.setField(WorkstationFields.MINIMUM_DIMENSION_UNIT, null);
        }
        if (Objects.isNull(maximumDimension)) {
            workstation.setField(WorkstationFields.MAXIMUM_DIMENSION_UNIT, null);
        }
    }

    public void onCopy(final DataDefinition workstationDD, final Entity workstation) {
        workstation.setField(WorkstationFields.STATE, WorkstationStateStringValues.STOPPED);
    }

    public boolean onDelete(final DataDefinition workstationDD, final Entity workstation) {
        boolean canDelete = workstation.getHasManyField(WorkstationFields.SUBASSEMBLIES).isEmpty();

        if (!canDelete) {
            workstation.addGlobalError("basic.workstation.delete.hasSubassemblies");
        }

        return canDelete;
    }

    public void onView(final DataDefinition workstationDD, final Entity workstation) {
        workstation.setField("entityId", workstation.getId());
    }

}
