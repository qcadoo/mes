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
package com.qcadoo.mes.basic;

import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.basic.util.DateTimeRange;
import com.qcadoo.model.api.Entity;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalTime;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ShiftsService {

    List<Shift> findAll();

    List<Shift> findAll(final Entity productionLine);

    List<Entity> getShifts();

    List<Entity> getShiftsWorkingAtDate(final Date date);

    LocalTime[][] convertDayHoursToInt(final String string);

    Entity getShiftFromDateWithTime(final Date date);

    List<DateTimeRange> getDateTimeRanges(final List<Shift> shifts, final Date dateFrom, final Date dateTo);

    Date findDateToForProductionLine(final Date dateFrom, final long seconds, final Entity productionLine);

    long getTotalAvailableTimeForProductionLine(final Date dateFrom, final Date dateTo, final Entity productionLine);

    String getWeekDayName(final DateTime dateTime);

    Optional<Shift> getShiftForNearestWorkingDate(DateTime nearestWorkingDate, Entity productionLine);

    Optional<DateTime> getNearestWorkingDate(final DateTime dateFrom, final Entity productionLine);

    List<DateTime> getDaysBetweenGivenDates(final DateTime dateFrom, final DateTime dateTo);

    int getNumberOfDaysBetweenGivenDates(final DateTime dateFrom, final DateTime dateTo);

    BigDecimal getWorkedHoursOfWorker(final Shift shift, final DateTime dateOfDay);

    List<Interval> mergeIntervals(List<Interval> intervals);

    long sumIntervals(List<Interval> intervals);
}
