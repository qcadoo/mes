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
package com.qcadoo.mes.productionPerShift;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.qcadoo.commons.dateTime.TimeRange;
import com.qcadoo.mes.basic.TimetableExceptionService;
import com.qcadoo.mes.basic.constants.ShiftTimetableExceptionFields;
import com.qcadoo.mes.basic.constants.TimetableExceptionType;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.model.api.Entity;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PpsTimeHelper {

    @Autowired
    private TimetableExceptionService timetableExceptionService;

    public Date findFinishDate(final Entity dailyProgress, final Date dateOfDay, final Entity order) {
        DateTime endDate = null;
        DateTime dateOfDayDT = new DateTime(dateOfDay, DateTimeZone.getDefault());
        DateTime orderStartDate = new DateTime(order.getDateField(OrderFields.START_DATE), DateTimeZone.getDefault());

        Entity shiftEntity = dailyProgress.getBelongsToField(DailyProgressFields.SHIFT);
        Shift shift = new Shift(shiftEntity);

        int time = dailyProgress.getIntegerField(DailyProgressFields.EFFICIENCY_TIME);
        List<TimeRange> shiftWorkTime = Lists.newArrayList();
        List<DateTimeRange> shiftWorkDateTime = Lists.newArrayList();

        if (shift.worksAt(dateOfDay.getDay() == 0 ? 7 : dateOfDay.getDay())) {
            shiftWorkTime = shift.findWorkTimeAt(new LocalDate(dateOfDay));
        }

        for (TimeRange range : shiftWorkTime) {
            DateTimeRange dateTimeRange = new DateTimeRange(dateOfDayDT, range);
            DateTimeRange trimmedRange = dateTimeRange.trimBefore(orderStartDate);

            if (trimmedRange != null) {
                shiftWorkDateTime.add(trimmedRange);
            }
        }

        shiftWorkDateTime = manageExceptions(shiftWorkDateTime, order.getBelongsToField(OrderFields.PRODUCTION_LINE), shift,
                dateOfDay);

        for (DateTimeRange range : shiftWorkDateTime) {
            if (range.durationInMins() >= time && time > 0) {
                endDate = range.getFrom().plusMinutes(time);
                time = 0;
            } else {
                endDate = range.getTo();
                time -= range.durationInMins();
            }
        }

        return endDate != null ? endDate.toDate() : null;
    }

    public List<DateTimeRange> manageExceptions(List<DateTimeRange> shiftWorkDateTime, final Entity productionLine, final Shift shift,
            final Date dateOfDay) {
        Entity shiftEntity = shift.getEntity();
        Shift shiftForDay = new Shift(shiftEntity, new DateTime(dateOfDay), false);

        List<Entity> exceptions = timetableExceptionService.findForLineAndShift(productionLine, shiftEntity);

        if (!exceptions.isEmpty()) {
            for (Entity exception : exceptions) {
                if (TimetableExceptionType.FREE_TIME.getStringValue().equals(
                        exception.getStringField(ShiftTimetableExceptionFields.TYPE)) && checkExceptionDates(exception, dateOfDay)) {
                    shiftWorkDateTime = removeFreeTimeException(shiftWorkDateTime, exception, shiftForDay);
                }
            }

            for (Entity exception : exceptions) {
                if (TimetableExceptionType.WORK_TIME.getStringValue().equals(
                        exception.getStringField(ShiftTimetableExceptionFields.TYPE)) && checkExceptionDates(exception, dateOfDay)) {
                    shiftWorkDateTime = addWorkTimeException(shiftWorkDateTime, exception, shiftForDay);
                }
            }
        }

        return shiftWorkDateTime;
    }

    private boolean checkExceptionDates(final Entity exception, final Date dateOfDay) {
        return ((new LocalDate(exception.getDateField(ShiftTimetableExceptionFields.TO_DATE))
                .compareTo(new DateTime(dateOfDay).toLocalDate()) >= 0)
                && (new LocalDate(exception.getDateField(ShiftTimetableExceptionFields.FROM_DATE))
                .compareTo(new DateTime(dateOfDay).toLocalDate()) <= 0));
    }

    private List<DateTimeRange> removeFreeTimeException(final List<DateTimeRange> shiftWorkDateTime, final Entity exception,
            final Shift shift) {
        Optional<DateTimeRange> exceptionRange = getExceptionRange(exception, shift);

        if (exceptionRange.isPresent()) {
            List<DateTimeRange> result = Lists.newArrayList();

            for (DateTimeRange range : shiftWorkDateTime) {
                result.addAll(range.remove(exceptionRange.get()));
            }

            return result;
        } else {
            return shiftWorkDateTime;
        }
    }

    private List<DateTimeRange> addWorkTimeException(final List<DateTimeRange> shiftWorkDateTime, final Entity exception, final Shift shift) {
        Optional<DateTimeRange> exceptionRange = getExceptionRange(exception, shift);

        if (exceptionRange.isPresent()) {
            if (shiftWorkDateTime.isEmpty()) {
                return Lists.newArrayList(exceptionRange.get());
            }

            List<DateTimeRange> result = Lists.newArrayList();

            for (DateTimeRange range : shiftWorkDateTime) {
                result.addAll(range.add(exceptionRange.get()));
            }

            return result;
        } else {
            return shiftWorkDateTime;
        }
    }

    private Optional<DateTimeRange> getExceptionRange(final Entity exception, final Shift shift) {
        Date fromDate = exception.getDateField(ShiftTimetableExceptionFields.FROM_DATE);
        Date toDate = exception.getDateField(ShiftTimetableExceptionFields.TO_DATE);

        if (toDate.before(shift.getShiftStartDate().toDate()) || shift.getShiftEndDate().toDate().before(fromDate)) {
            return Optional.empty();
        }

        if (fromDate.before(shift.getShiftStartDate().toDate())) {
            fromDate = shift.getShiftStartDate().toDate();
        }

        if (toDate.after(shift.getShiftEndDate().toDate())) {
            toDate = shift.getShiftEndDate().toDate();
        }

        return Optional.of(new DateTimeRange(fromDate, toDate));
    }

    public Date calculateOrderFinishDate(final Entity order, final List<Entity> progressForDays) {
        if (!progressForDays.isEmpty()) {
            Entity progressForDay = Iterables.getLast(progressForDays);

            if (progressForDay != null) {
                Date dateOfDay = progressForDay.getDateField(ProgressForDayFields.DATE_OF_DAY);

                List<Entity> dailyProgresses = progressForDay.getHasManyField(ProgressForDayFields.DAILY_PROGRESS);

                if (!dailyProgresses.isEmpty()) {
                    Entity dailyProgress = Iterables.getLast(dailyProgresses);

                    if (dailyProgress != null) {
                        if (dailyProgress.getIntegerField(DailyProgressFields.EFFICIENCY_TIME) == null) {
                            return order.getDateField(OrderFields.FINISH_DATE);
                        }

                        return findFinishDate(dailyProgress, dateOfDay, order);
                    }
                }
            }
        }

        return order.getDateField(OrderFields.FINISH_DATE);
    }

}
