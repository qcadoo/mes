package com.qcadoo.mes.basic;

import com.google.common.collect.Lists;
import com.qcadoo.commons.dateTime.TimeRange;
import com.qcadoo.mes.basic.constants.ShiftFields;
import com.qcadoo.mes.basic.constants.ShiftTimetableExceptionFields;
import com.qcadoo.mes.basic.constants.TimetableExceptionType;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.basic.util.DateTimeRange;
import com.qcadoo.model.api.Entity;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ShiftExceptionService {

    @Autowired
    private TimetableExceptionService timetableExceptionService;

    public List<DateTimeRange> manageExceptions(List<DateTimeRange> shiftWorkDateTime, final Entity productionLine,
            final Shift shift, final Date dateOfDay, final boolean removeFreeTimeException) {
        List<Entity> exceptions;
        Entity shiftEntity = shift.getEntity();

        if (Objects.isNull(productionLine)) {
            exceptions = shiftEntity.getHasManyField(ShiftFields.TIMETABLE_EXCEPTIONS);
        } else {
            exceptions = timetableExceptionService.findFor(productionLine, shiftEntity, dateOfDay);
        }

        Shift shiftForDay = new Shift(shiftEntity, new DateTime(dateOfDay), false);

        for (Entity exception : exceptions) {
            if (removeFreeTimeException && TimetableExceptionType.FREE_TIME.getStringValue()
                    .equals(exception.getStringField(ShiftTimetableExceptionFields.TYPE))) {
                shiftWorkDateTime = removeFreeTimeException(shiftWorkDateTime, exception, shiftForDay);
            }

            if (TimetableExceptionType.WORK_TIME.getStringValue()
                    .equals(exception.getStringField(ShiftTimetableExceptionFields.TYPE))) {
                shiftWorkDateTime = addWorkTimeException(shiftWorkDateTime, exception, shiftForDay);
            }
        }

        return shiftWorkDateTime;
    }

    public List<DateTimeRange> getShiftWorkDateTimes(final Entity productionLine, final Shift shift, DateTime dateOfDay,
            final boolean removeFreeTimeException) {
        List<TimeRange> shiftWorkTime = Lists.newArrayList();
        List<DateTimeRange> shiftWorkDateTime = Lists.newArrayList();
        if (shift.worksAt(dateOfDay.dayOfWeek().get())) {
            shiftWorkTime = shift.findWorkTimeAt(dateOfDay.toLocalDate());
        }
        for (TimeRange range : shiftWorkTime) {
            shiftWorkDateTime.add(new DateTimeRange(dateOfDay, range));
        }

        shiftWorkDateTime = manageExceptions(shiftWorkDateTime, productionLine, shift, dateOfDay.toDate(),
                removeFreeTimeException);

        return shiftWorkDateTime;
    }

    private List<DateTimeRange> removeFreeTimeException(final List<DateTimeRange> shiftWorkDateTime, final Entity exception,
            final Shift shift) {
        Optional<DateTimeRange> exceptionRange = getExceptionRange(exception, shift, false);

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

    private List<DateTimeRange> addWorkTimeException(final List<DateTimeRange> shiftWorkDateTime, final Entity exception,
            final Shift shift) {
        Optional<DateTimeRange> exceptionRange = getExceptionRange(exception, shift, true);

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

    private Optional<DateTimeRange> getExceptionRange(final Entity exception, final Shift shift, boolean workingException) {
        Date fromDate = exception.getDateField(ShiftTimetableExceptionFields.FROM_DATE);
        Date toDate = exception.getDateField(ShiftTimetableExceptionFields.TO_DATE);

        if (Objects.isNull(shift.getShiftStartDate()) || Objects.isNull(shift.getShiftEndDate())) {
            if (workingException) {
                return Optional.of(new DateTimeRange(fromDate, toDate));
            } else {
                return Optional.empty();
            }
        }

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
}
