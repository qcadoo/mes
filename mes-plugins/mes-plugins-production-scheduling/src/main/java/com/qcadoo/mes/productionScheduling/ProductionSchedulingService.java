package com.qcadoo.mes.productionScheduling;

import com.google.common.collect.Lists;
import com.qcadoo.commons.dateTime.TimeRange;
import com.qcadoo.mes.basic.TimetableExceptionService;
import com.qcadoo.mes.basic.constants.ShiftTimetableExceptionFields;
import com.qcadoo.mes.basic.constants.TimetableExceptionType;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.basic.shift.ShiftsDataProvider;
import com.qcadoo.mes.basic.util.DateTimeRange;
import com.qcadoo.mes.orders.constants.OrderFields;
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
public class ProductionSchedulingService {


    public static final int MAX_LOOPS = 1000;

    public static final int MILLS = 1000;

    public static final int ONE_DAY = 1;

    @Autowired
    private ShiftsDataProvider shiftsDataProvider;

    @Autowired
    private TimetableExceptionService timetableExceptionService;

    public Optional<DateTime> findDateFromDatePlusMilliseconds(Entity order, Date orderStartDate, long milliseconds) {
        DateTime orderStartDateDT = new DateTime(orderStartDate, DateTimeZone.getDefault());

        List<Shift> shifts = shiftsDataProvider.findAll();
        DateTime dateOfDay = new DateTime(orderStartDate);
        dateOfDay = dateOfDay.minusDays(ONE_DAY);
        dateOfDay = dateOfDay.toLocalDate().toDateTimeAtStartOfDay();
        Long leftMilliseconds = milliseconds;
        int loopCount = 0;
        while (leftMilliseconds > 0l) {
            if (loopCount > MAX_LOOPS) {
                return Optional.empty();
            }
            for (Shift shift : shifts) {
                for (DateTimeRange range : getShiftWorkDateTimes(order.getBelongsToField(OrderFields.PRODUCTION_LINE), shift,
                        dateOfDay, orderStartDate)) {
                    if (orderStartDate.after(dateOfDay.toDate())) {
                        range = range.trimBefore(orderStartDateDT);
                    }
                    if (range != null) {
                        if (leftMilliseconds > range.durationMillis()) {
                            leftMilliseconds = leftMilliseconds - range.durationMillis();
                        } else {
                            return Optional.of(range.getFrom().plusMillis(leftMilliseconds.intValue()));
                        }
                    }
                }
            }
            loopCount++;
            dateOfDay = dateOfDay.plusDays(1);
        }

        return Optional.empty();
    }

    public Optional<DateTime> findFirstWorkingDate(Entity order, Date orderStartDate) {
        DateTime orderStartDateDT = new DateTime(orderStartDate, DateTimeZone.getDefault());

        List<Shift> shifts = shiftsDataProvider.findAll();
        DateTime dateOfDay = new DateTime(orderStartDate);
        dateOfDay = dateOfDay.minusDays(ONE_DAY);
        dateOfDay = dateOfDay.toLocalDate().toDateTimeAtStartOfDay();
        boolean notFound = true;
        int loopCount = 0;
        while (notFound) {
            if (loopCount > MAX_LOOPS) {
                return Optional.empty();
            }
            for (Shift shift : shifts) {
                for (DateTimeRange range : getShiftWorkDateTimes(order.getBelongsToField(OrderFields.PRODUCTION_LINE), shift,
                        dateOfDay, orderStartDate)) {
                    if (orderStartDate.after(dateOfDay.toDate())) {
                        range = range.trimBefore(orderStartDateDT);
                    }
                    if (range != null) {

                        return Optional.of(range.getFrom());

                    }
                }
            }
            loopCount++;
            dateOfDay = dateOfDay.plusDays(1);
        }

        return Optional.empty();
    }

    private List<DateTimeRange> getShiftWorkDateTimes(final Entity productionLine, final Shift shift, DateTime dateOfDay,
            final Date orderStartDate) {
        DateTime dateOfDayDT = dateOfDay;
        List<TimeRange> shiftWorkTime = Lists.newArrayList();
        List<DateTimeRange> shiftWorkDateTime = Lists.newArrayList();
        if (shift.worksAt(dateOfDay.dayOfWeek().get())) {
            shiftWorkTime = shift.findWorkTimeAt(dateOfDay.toLocalDate());
        }
        for (TimeRange range : shiftWorkTime) {
            shiftWorkDateTime.add(new DateTimeRange(dateOfDayDT, range));
        }

        shiftWorkDateTime = manageExceptions(shiftWorkDateTime, productionLine, shift, dateOfDay.toDate());

        return shiftWorkDateTime;
    }

    public List<DateTimeRange> manageExceptions(List<DateTimeRange> shiftWorkDateTime, final Entity productionLine,
            final Shift shift, final Date dateOfDay) {
        Entity shiftEntity = shift.getEntity();
        Shift shiftForDay = new Shift(shiftEntity, new DateTime(dateOfDay), false);

        List<Entity> exceptions = timetableExceptionService.findForLineAndShift(productionLine, shiftEntity);

        if (!exceptions.isEmpty()) {
            for (Entity exception : exceptions) {
                if (TimetableExceptionType.FREE_TIME.getStringValue().equals(
                        exception.getStringField(ShiftTimetableExceptionFields.TYPE))
                        && checkExceptionDates(exception, dateOfDay)) {
                    shiftWorkDateTime = removeFreeTimeException(shiftWorkDateTime, exception, shiftForDay);
                }
            }

            for (Entity exception : exceptions) {
                if (TimetableExceptionType.WORK_TIME.getStringValue().equals(
                        exception.getStringField(ShiftTimetableExceptionFields.TYPE))
                        && checkExceptionDates(exception, dateOfDay)) {
                    shiftWorkDateTime = addWorkTimeException(shiftWorkDateTime, exception, shiftForDay);
                }
            }
        }

        return shiftWorkDateTime;
    }

    private boolean checkExceptionDates(final Entity exception, final Date dateOfDay) {
        return ((new LocalDate(exception.getDateField(ShiftTimetableExceptionFields.TO_DATE)).compareTo(new DateTime(dateOfDay)
                .toLocalDate()) >= 0) && (new LocalDate(exception.getDateField(ShiftTimetableExceptionFields.FROM_DATE))
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

    private List<DateTimeRange> addWorkTimeException(final List<DateTimeRange> shiftWorkDateTime, final Entity exception,
            final Shift shift) {
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

        if (toDate.before(shift.getShiftStartDate().toDate())) {
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

    public Date getFinishDate(Entity order, Date orderStartDate, long milliseconds) {
        Date dateTo = null;
        Optional<DateTime> maybeDate = findDateFromDatePlusMilliseconds(order, orderStartDate, milliseconds);
        if (maybeDate.isPresent()) {
            dateTo = maybeDate.get().toDate();
        }
        return dateTo;
    }

    public Date getStartDate(Entity order, Date orderStartDate, Integer offset) {
        Date dateFrom = null;
        if (offset == 0) {
            Optional<DateTime> maybeDate = findFirstWorkingDate(order, orderStartDate);
            if (maybeDate.isPresent()) {
                dateFrom = maybeDate.get().toDate();
            }
        } else {
            Optional<DateTime> maybeDate = findDateFromDatePlusMilliseconds(order, orderStartDate, offset * MILLS);
            if (maybeDate.isPresent()) {
                dateFrom = maybeDate.get().toDate();
            }
        }
        return dateFrom;
    }
}
