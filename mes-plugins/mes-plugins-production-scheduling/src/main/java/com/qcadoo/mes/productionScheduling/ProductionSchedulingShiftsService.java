package com.qcadoo.mes.productionScheduling;

/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.4.5
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

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionSchedulingShiftsService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ShiftsGanttChartItemResolver shiftsGanttChartItemResolver;

    private static final String[] WEEK_DAYS = { "monday", "tuesday", "wensday", "thursday", "friday", "saturday", "sunday" };

    public boolean validateShiftHoursField(final DataDefinition dataDefinition, final Entity entity) {
        boolean valid = true;
        for (String day : WEEK_DAYS) {
            if (!validateHourField(day, dataDefinition, entity)) {
                valid = false;
            }
        }
        return valid;
    }

    private boolean validateHourField(final String day, final DataDefinition dataDefinition, final Entity entity) {
        boolean isDayActive = (Boolean) entity.getField(day + "Working");
        String fieldValue = entity.getStringField(day + "Hours");
        if (!isDayActive) {
            return true;
        }
        if (fieldValue == null || "".equals(fieldValue.trim())) {
            entity.addError(dataDefinition.getField(day + "Hours"), "qcadooView.validate.field.error.missing");
            return false;
        }
        try {
            shiftsGanttChartItemResolver.convertDayHoursToInt(fieldValue);
        } catch (IllegalStateException e) {
            entity.addError(dataDefinition.getField(day + "Hours"),
                    "productionScheduling.validate.global.error.shift.hoursFieldWrongFormat");
            return false;
        }
        return true;
    }

    private static final long STEP = 604800000;

    private static final long MAX_TIMESTAMP = new DateTime(2100, 1, 1, 0, 0, 0, 0).toDate().getTime();

    private static final long MIN_TIMESTAMP = new DateTime(2000, 1, 1, 0, 0, 0, 0).toDate().getTime();

    public Date findDateToForOrder(final Date dateFrom, final long seconds) {
        if (dataDefinitionService.get("basic", "shift").find().list().getTotalNumberOfEntities() == 0) {
            return null;
        }
        long start = dateFrom.getTime();
        long remaining = seconds;
        while (remaining > 0) {
            List<ShiftsGanttChartItemResolverImpl.ShiftHour> hours = shiftsGanttChartItemResolver.getHoursForAllShifts(new Date(
                    start), new Date(start + STEP));
            for (ShiftsGanttChartItemResolverImpl.ShiftHour hour : hours) {
                long diff = (hour.getDateTo().getTime() - hour.getDateFrom().getTime()) / 1000;
                if (diff >= remaining) {
                    return new Date(hour.getDateFrom().getTime() + (remaining * 1000));
                } else {
                    remaining -= diff;
                }
            }
            start += STEP;
            if (start > MAX_TIMESTAMP) {
                return null;
            }
        }
        return null;
    }

    public Date findDateFromForOrder(final Date dateTo, final long seconds) {
        if (dataDefinitionService.get("basic", "shift").find().list().getTotalNumberOfEntities() == 0) {
            return null;
        }
        long stop = dateTo.getTime();
        long remaining = seconds;
        while (remaining > 0) {
            List<ShiftsGanttChartItemResolverImpl.ShiftHour> hours = shiftsGanttChartItemResolver.getHoursForAllShifts(new Date(
                    stop - STEP), new Date(stop));
            for (int i = hours.size() - 1; i >= 0; i--) {
                ShiftsGanttChartItemResolverImpl.ShiftHour hour = hours.get(i);
                long diff = (hour.getDateTo().getTime() - hour.getDateFrom().getTime()) / 1000;
                if (diff >= remaining) {
                    return new Date(hour.getDateTo().getTime() - (remaining * 1000));
                } else {
                    remaining -= diff;
                }
            }
            stop -= STEP;
            if (stop < MIN_TIMESTAMP) {
                return null;
            }
        }
        return null;
    }

}
