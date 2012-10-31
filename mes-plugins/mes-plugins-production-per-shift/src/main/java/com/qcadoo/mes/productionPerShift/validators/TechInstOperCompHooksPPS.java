/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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

import static com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields.DAY;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TechInstOperCompHooksPPS {

    public boolean checkGrowingNumberOfDays(final DataDefinition dataDefinition, final Entity entity) {
        List<Entity> progressForDays = entity.getHasManyField("progressForDays");
        if (progressForDays.isEmpty()) {
            return true;
        }
        Integer dayNumber = Integer.valueOf(0);
        for (Entity progressForDay : progressForDays) {
            if (progressForDay.getBooleanField("corrected") != entity.getBooleanField("hasCorrections")
                    || progressForDay.getField("day") == null) {
                continue;
            }
            Integer day = Integer.valueOf(progressForDay.getField("day").toString());
            if (day != null && dayNumber.compareTo(day) == -1) {
                dayNumber = day;
            } else {
                entity.addGlobalError("productionPerShift.progressForDay.daysIsNotInAscendingOrder", day.toString());
                return false;
            }
        }
        return true;
    }

    public boolean checkShiftsIfWorks(final DataDefinition tiocDD, final Entity tioc) {
        List<Entity> progressForDays = tioc.getHasManyField("progressForDays");
        for (Entity progressForDay : progressForDays) {
            if (progressForDay.getField(DAY) == null) {
                tioc.addGlobalError("productionPerShift.progressForDay.dayIsNull");
                return false;
            }
        }
        return true;
    }

}
