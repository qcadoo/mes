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
package com.qcadoo.mes.basic.validators;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.MachineWorkingPeriodFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class MachineWorkingPeriodValidators {

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity entity) {
        boolean isValid = checkIfPeriodWithoutStopAlreadyAdded(dataDefinition, entity);
        isValid = checkDates(dataDefinition, entity) && isValid;

        return isValid;
    }

    private boolean checkIfPeriodWithoutStopAlreadyAdded(final DataDefinition dataDefinition, final Entity entity) {
        boolean isNotAlreadyAdded = true;

        Long entityId = entity.getId();

        List<Entity> machineWorkingPeriods = entity.getBelongsToField(MachineWorkingPeriodFields.WORKSTATION)
                .getHasManyField(WorkstationFields.MACHINE_WORKING_PERIODS).stream()
                .filter(e -> e.getDateField(MachineWorkingPeriodFields.STOP_DATE) == null).collect(Collectors.toList());
        if (!Objects.isNull(entityId)) {
            machineWorkingPeriods = machineWorkingPeriods.stream().filter(e -> !e.getId().equals(entityId)).collect(Collectors.toList());
        }

        if (machineWorkingPeriods.size() >0) {
            entity.addError(dataDefinition.getField(MachineWorkingPeriodFields.STOP_DATE), "basic.validate.global.error.machineWorkingPeriodWithoutStopDateExists");

            isNotAlreadyAdded = false;
        }

        return isNotAlreadyAdded;
    }

    private boolean checkDates(final DataDefinition dataDefinition, final Entity entity) {
        Date launchDate = entity.getDateField(MachineWorkingPeriodFields.LAUNCH_DATE);
        Date stopDate = entity.getDateField(MachineWorkingPeriodFields.STOP_DATE);

        if (Objects.isNull(stopDate) || stopDate.after(launchDate)) {
            return true;
        }

        entity.addError(dataDefinition.getField(MachineWorkingPeriodFields.STOP_DATE), "basic.validate.global.error.datesMachineWorkingPeriod");

        return false;
    }

}
