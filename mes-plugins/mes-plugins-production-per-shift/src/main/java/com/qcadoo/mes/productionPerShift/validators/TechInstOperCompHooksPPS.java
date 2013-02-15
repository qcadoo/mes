/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.productionPerShift.validators;

import static com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields.CORRECTED;
import static com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields.DAY;
import static com.qcadoo.mes.productionPerShift.constants.TechInstOperCompFieldsPPS.HAS_CORRECTIONS;
import static com.qcadoo.mes.productionPerShift.constants.TechInstOperCompFieldsPPS.PROGRESS_FOR_DAYS;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TechInstOperCompHooksPPS {

    public boolean checkGrowingNumberOfDays(final DataDefinition technologyInstanceOperationComponentDD,
            final Entity technologyInstanceOperationComponent) {
        List<Entity> progressForDays = technologyInstanceOperationComponent.getHasManyField(PROGRESS_FOR_DAYS);
        if (progressForDays.isEmpty()) {
            return true;
        }
        Integer dayNumber = Integer.valueOf(0);
        for (Entity progressForDay : progressForDays) {
            if (progressForDay.getBooleanField(CORRECTED) != technologyInstanceOperationComponent
                    .getBooleanField(HAS_CORRECTIONS) || progressForDay.getField(DAY) == null) {
                continue;
            }
            Integer day = ((Long) progressForDay.getField(DAY)).intValue();
            if (day != null && dayNumber.compareTo(day) == -1) {
                dayNumber = day;
            } else {
                technologyInstanceOperationComponent.addGlobalError(
                        "productionPerShift.progressForDay.daysAreNotInAscendingOrder", progressForDay.getField(DAY).toString());
                return false;
            }
        }
        return true;
    }

    public boolean checkShiftsIfWorks(final DataDefinition technologyInstanceOperationComponentDD,
            final Entity technologyInstanceOperationComponent) {
        List<Entity> progressForDays = technologyInstanceOperationComponent.getHasManyField(PROGRESS_FOR_DAYS);
        for (Entity progressForDay : progressForDays) {
            if (progressForDay.getField(DAY) == null) {
                technologyInstanceOperationComponent.addGlobalError("productionPerShift.progressForDay.dayIsNull");
                return false;
            }
        }
        return true;
    }

}
