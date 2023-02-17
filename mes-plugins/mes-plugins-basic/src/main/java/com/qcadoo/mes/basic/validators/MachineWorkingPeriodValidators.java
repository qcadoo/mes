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
        boolean isValid = checkDates(dataDefinition, entity);
        isValid = checkFutureDates(dataDefinition, entity) && isValid;
        isValid = checkIfPeriodWithoutStopAlreadyAdded(dataDefinition, entity) && isValid;
        isValid = checkIfPeriodWithoutStopIsLast(dataDefinition, entity) && isValid;
        isValid = checkIfLaunchDateIsBeforeThanPreviousPeriodStopDate(dataDefinition, entity) && isValid;
        isValid = checkIfStopDateIsLaterThanNextPeriodLaunchDate(dataDefinition, entity) && isValid;
        isValid = checkIfPeriodWithLaunchDateExists(dataDefinition, entity) && isValid;
        isValid = checkIfPeriodWithStopDateExists(dataDefinition, entity) && isValid;

        return isValid;
    }

    private boolean checkIfStopDateIsLaterThanNextPeriodLaunchDate(DataDefinition dataDefinition, Entity entity) {
        Date stopDate = entity.getDateField(MachineWorkingPeriodFields.STOP_DATE);
        if (stopDate != null) {
            List<Entity> machineWorkingPeriods = entity.getBelongsToField(MachineWorkingPeriodFields.WORKSTATION)
                    .getHasManyField(WorkstationFields.MACHINE_WORKING_PERIODS).stream()
                    .filter(e -> e.getDateField(MachineWorkingPeriodFields.STOP_DATE) == null || e.getDateField(MachineWorkingPeriodFields.STOP_DATE)
                            .after(stopDate))
                    .filter(e -> e.getDateField(MachineWorkingPeriodFields.LAUNCH_DATE).before(stopDate))
                    .collect(Collectors.toList());
            Long entityId = entity.getId();
            if (!Objects.isNull(entityId)) {
                machineWorkingPeriods = machineWorkingPeriods.stream().filter(e -> !e.getId().equals(entityId)).collect(Collectors.toList());
            }
            if (!machineWorkingPeriods.isEmpty()) {
                entity.addError(dataDefinition.getField(MachineWorkingPeriodFields.STOP_DATE), "basic.validate.global.error.machineWorkingPeriodLaunchDateBeforePeriodStopDate");

                return false;
            }
        }
        return true;
    }

    private boolean checkIfPeriodWithLaunchDateExists(DataDefinition dataDefinition, Entity entity) {
        Date launchDate = entity.getDateField(MachineWorkingPeriodFields.LAUNCH_DATE);
        List<Entity> machineWorkingPeriods = entity.getBelongsToField(MachineWorkingPeriodFields.WORKSTATION)
                .getHasManyField(WorkstationFields.MACHINE_WORKING_PERIODS).stream()
                .filter(e -> e.getDateField(MachineWorkingPeriodFields.LAUNCH_DATE)
                        .equals(launchDate))
                .collect(Collectors.toList());
        Long entityId = entity.getId();
        if (!Objects.isNull(entityId)) {
            machineWorkingPeriods = machineWorkingPeriods.stream().filter(e -> !e.getId().equals(entityId)).collect(Collectors.toList());
        }
        if (!machineWorkingPeriods.isEmpty()) {
            entity.addError(dataDefinition.getField(MachineWorkingPeriodFields.LAUNCH_DATE), "basic.validate.global.error.machineWorkingPeriodWithLaunchDateExists");

            return false;
        }
        return true;
    }

    private boolean checkIfPeriodWithStopDateExists(DataDefinition dataDefinition, Entity entity) {
        Date stopDate = entity.getDateField(MachineWorkingPeriodFields.STOP_DATE);
        if (stopDate != null) {
            List<Entity> machineWorkingPeriods = entity.getBelongsToField(MachineWorkingPeriodFields.WORKSTATION)
                    .getHasManyField(WorkstationFields.MACHINE_WORKING_PERIODS).stream()
                    .filter(e -> e.getDateField(MachineWorkingPeriodFields.STOP_DATE) != null)
                    .filter(e -> e.getDateField(MachineWorkingPeriodFields.STOP_DATE)
                            .equals(stopDate))
                    .collect(Collectors.toList());
            Long entityId = entity.getId();
            if (!Objects.isNull(entityId)) {
                machineWorkingPeriods = machineWorkingPeriods.stream().filter(e -> !e.getId().equals(entityId)).collect(Collectors.toList());
            }
            if (!machineWorkingPeriods.isEmpty()) {
                entity.addError(dataDefinition.getField(MachineWorkingPeriodFields.STOP_DATE), "basic.validate.global.error.machineWorkingPeriodWithStopDateExists");

                return false;
            }
        }
        return true;
    }

    private boolean checkIfLaunchDateIsBeforeThanPreviousPeriodStopDate(DataDefinition dataDefinition, Entity entity) {
        Date launchDate = entity.getDateField(MachineWorkingPeriodFields.LAUNCH_DATE);
        Date stopDate = entity.getDateField(MachineWorkingPeriodFields.STOP_DATE);
        List<Entity> machineWorkingPeriods;
        Long entityId = entity.getId();
        if (stopDate != null) {
            machineWorkingPeriods = entity.getBelongsToField(MachineWorkingPeriodFields.WORKSTATION)
                    .getHasManyField(WorkstationFields.MACHINE_WORKING_PERIODS).stream()
                    .filter(e -> e.getDateField(MachineWorkingPeriodFields.STOP_DATE) != null)
                    .filter(e -> e.getDateField(MachineWorkingPeriodFields.LAUNCH_DATE)
                            .before(launchDate) || e.getDateField(MachineWorkingPeriodFields.LAUNCH_DATE).after(launchDate)
                            && e.getDateField(MachineWorkingPeriodFields.STOP_DATE).before(stopDate))
                    .filter(e -> e.getDateField(MachineWorkingPeriodFields.STOP_DATE).after(launchDate))
                    .collect(Collectors.toList());

        } else {
            machineWorkingPeriods = entity.getBelongsToField(MachineWorkingPeriodFields.WORKSTATION)
                    .getHasManyField(WorkstationFields.MACHINE_WORKING_PERIODS).stream()
                    .filter(e -> e.getDateField(MachineWorkingPeriodFields.STOP_DATE) != null)
                    .filter(e -> e.getDateField(MachineWorkingPeriodFields.STOP_DATE).after(launchDate))
                    .collect(Collectors.toList());
        }
        if (!Objects.isNull(entityId)) {
            machineWorkingPeriods = machineWorkingPeriods.stream().filter(e -> !e.getId().equals(entityId)).collect(Collectors.toList());
        }
        if (!machineWorkingPeriods.isEmpty()) {
            entity.addError(dataDefinition.getField(MachineWorkingPeriodFields.LAUNCH_DATE), "basic.validate.global.error.machineWorkingPeriodStopDateAfterPeriodLaunchDate");

            return false;
        }
        return true;
    }

    private boolean checkIfPeriodWithoutStopIsLast(DataDefinition dataDefinition, Entity entity) {
        Date launchDate = entity.getDateField(MachineWorkingPeriodFields.LAUNCH_DATE);
        Date stopDate = entity.getDateField(MachineWorkingPeriodFields.STOP_DATE);
        Long entityId = entity.getId();
        if (stopDate == null) {
            List<Entity> machineWorkingPeriods = entity.getBelongsToField(MachineWorkingPeriodFields.WORKSTATION)
                    .getHasManyField(WorkstationFields.MACHINE_WORKING_PERIODS).stream()
                    .filter(e -> e.getDateField(MachineWorkingPeriodFields.LAUNCH_DATE)
                            .after(launchDate))
                    .collect(Collectors.toList());
            if (!Objects.isNull(entityId)) {
                machineWorkingPeriods = machineWorkingPeriods.stream().filter(e -> !e.getId().equals(entityId)).collect(Collectors.toList());
            }
            if (!machineWorkingPeriods.isEmpty()) {
                entity.addError(dataDefinition.getField(MachineWorkingPeriodFields.STOP_DATE), "basic.validate.global.error.machineWorkingPeriodWithoutStopDateNotLast");

                return false;
            }
        } else if (entityId == null) {
            List<Entity> machineWorkingPeriods = entity.getBelongsToField(MachineWorkingPeriodFields.WORKSTATION)
                    .getHasManyField(WorkstationFields.MACHINE_WORKING_PERIODS).stream()
                    .filter(e -> e.getDateField(MachineWorkingPeriodFields.STOP_DATE) == null)
                    .filter(e -> e.getDateField(MachineWorkingPeriodFields.LAUNCH_DATE)
                            .before(stopDate))
                    .collect(Collectors.toList());
            if (!machineWorkingPeriods.isEmpty()) {
                entity.addError(dataDefinition.getField(MachineWorkingPeriodFields.STOP_DATE), "basic.validate.global.error.machineWorkingPeriodWithoutStopDateNotLast");

                return false;
            }
        }
        return true;
    }

    private boolean checkFutureDates(DataDefinition dataDefinition, Entity entity) {
        Date launchDate = entity.getDateField(MachineWorkingPeriodFields.LAUNCH_DATE);
        Date stopDate = entity.getDateField(MachineWorkingPeriodFields.STOP_DATE);
        Date actualDate = new Date();
        if (launchDate.after(actualDate)) {
            entity.addError(dataDefinition.getField(MachineWorkingPeriodFields.LAUNCH_DATE), "basic.validate.global.error.datesMachineWorkingPeriodInFuture");

            return false;
        } else if (stopDate != null && stopDate.after(actualDate)) {
            entity.addError(dataDefinition.getField(MachineWorkingPeriodFields.STOP_DATE), "basic.validate.global.error.datesMachineWorkingPeriodInFuture");

            return false;
        }
        return true;
    }

    private boolean checkIfPeriodWithoutStopAlreadyAdded(final DataDefinition dataDefinition, final Entity entity) {
        Date stopDate = entity.getDateField(MachineWorkingPeriodFields.STOP_DATE);
        if (stopDate == null) {

            Long entityId = entity.getId();

            List<Entity> machineWorkingPeriods = entity.getBelongsToField(MachineWorkingPeriodFields.WORKSTATION)
                    .getHasManyField(WorkstationFields.MACHINE_WORKING_PERIODS).stream()
                    .filter(e -> e.getDateField(MachineWorkingPeriodFields.STOP_DATE) == null).collect(Collectors.toList());
            if (!Objects.isNull(entityId)) {
                machineWorkingPeriods = machineWorkingPeriods.stream().filter(e -> !e.getId().equals(entityId)).collect(Collectors.toList());
            }

            if (!machineWorkingPeriods.isEmpty()) {
                entity.addError(dataDefinition.getField(MachineWorkingPeriodFields.STOP_DATE), "basic.validate.global.error.machineWorkingPeriodWithoutStopDateExists");

                return false;
            }
        }

        return true;
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
