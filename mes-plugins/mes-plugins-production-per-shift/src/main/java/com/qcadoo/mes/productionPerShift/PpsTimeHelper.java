package com.qcadoo.mes.productionPerShift;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.commons.dateTime.TimeRange;
import com.qcadoo.mes.basic.constants.ShiftFields;
import com.qcadoo.mes.basic.constants.ShiftTimetableExceptionFields;
import com.qcadoo.mes.basic.constants.TimetableExceptionType;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Service
public class PpsTimeHelper {

    @Autowired
    private NumberService numberService;

    public Date findFinishDate(final Entity dailyProgress, Date dateOfDay, Entity order) {
        DateTime endDate = null;
        DateTime dateOfDayDT = new DateTime(dateOfDay, DateTimeZone.getDefault());
        DateTime orderStartDate = new DateTime(order.getDateField(OrderFields.START_DATE), DateTimeZone.getDefault());
        Entity shiftEntity = dailyProgress.getBelongsToField(DailyProgressFields.SHIFT);
        Shift shift = new Shift(shiftEntity);
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

        shiftWorkDateTime = manageExceptions(shiftWorkDateTime, shift.getEntity(), dateOfDay);

        for (DateTimeRange range : shiftWorkDateTime) {
            if (endDate == null || endDate.isBefore(range.getTo())) {
                endDate = range.getTo();
            }
        }
        return endDate.toDate();
    }

    public List<DateTimeRange> manageExceptions(List<DateTimeRange> shiftWorkDateTime, Entity shiftEntity, Date dateOfDay) {
        List<Entity> exceptions = Lists.newArrayList(shiftEntity.getHasManyField(ShiftFields.TIMETABLE_EXCEPTIONS));
        if (!exceptions.isEmpty()) {
            trimWorkExceptions(exceptions, dateOfDay);

            for (Entity exception : exceptions) {
                if (TimetableExceptionType.FREE_TIME.getStringValue().equals(
                        exception.getStringField(ShiftTimetableExceptionFields.TYPE))) {
                    shiftWorkDateTime = removeFreeTimeException(shiftWorkDateTime, exception);
                }
            }
            for (Entity exception : exceptions) {
                if (TimetableExceptionType.WORK_TIME.getStringValue().equals(
                        exception.getStringField(ShiftTimetableExceptionFields.TYPE))) {
                    shiftWorkDateTime = addWorkTimeException(shiftWorkDateTime, exception);
                }
            }
        }
        return shiftWorkDateTime;
    }

    private void trimWorkExceptions(final List<Entity> exceptions, Date dateOfDay) {
        Date dayStart = new Date(dateOfDay.getYear(), dateOfDay.getMonth(), dateOfDay.getDate());
        Date dayEnd = new Date(dayStart.getTime() + TimeUnit.DAYS.toMillis(1));
        Date nextDayEnd = new Date(dayEnd.getTime() + TimeUnit.DAYS.toMillis(1));

        List<Entity> hoursToRemove = Lists.newArrayList();

        for (Entity exception : exceptions) {
            if (TimetableExceptionType.WORK_TIME.getStringValue().equals(
                    exception.getStringField(ShiftTimetableExceptionFields.TYPE))) {

                Date fromDate = exception.getDateField(ShiftTimetableExceptionFields.FROM_DATE);
                Boolean relatesToPrevDay = exception.getBooleanField(ShiftTimetableExceptionFields.RELATES_TO_PREV_DAY);
                if (!relatesToPrevDay && (fromDate.before(dayStart) || !fromDate.before(dayEnd))) {
                    hoursToRemove.add(exception);
                }
                if (relatesToPrevDay && (fromDate.before(dayEnd) || !fromDate.before(nextDayEnd))) {
                    hoursToRemove.add(exception);
                }
            }
        }
        exceptions.removeAll(hoursToRemove);
    }

    private List<DateTimeRange> removeFreeTimeException(final List<DateTimeRange> shiftWorkDateTime, final Entity exception) {

        Date fromDate = exception.getDateField(ShiftTimetableExceptionFields.FROM_DATE);
        Date toDate = exception.getDateField(ShiftTimetableExceptionFields.TO_DATE);
        DateTimeRange exceptionRange = new DateTimeRange(fromDate, toDate);

        List<DateTimeRange> result = Lists.newArrayList();

        for (DateTimeRange range : shiftWorkDateTime) {
            result.addAll(range.remove(exceptionRange));
        }

        return result;
    }

    private List<DateTimeRange> addWorkTimeException(List<DateTimeRange> shiftWorkDateTime, final Entity exception) {

        Date fromDate = exception.getDateField(ShiftTimetableExceptionFields.FROM_DATE);
        Date toDate = exception.getDateField(ShiftTimetableExceptionFields.TO_DATE);
        DateTimeRange exceptionRange = new DateTimeRange(fromDate, toDate);

        if (shiftWorkDateTime.isEmpty()) {
            return Lists.newArrayList(exceptionRange);
        }
        List<DateTimeRange> result = Lists.newArrayList();

        for (DateTimeRange range : shiftWorkDateTime) {
            result.addAll(range.add(exceptionRange));
        }

        return result;
    }
}
