package com.qcadoo.mes.productionScheduling;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ShiftExceptionService;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.basic.shift.ShiftsDataProvider;
import com.qcadoo.mes.basic.util.DateTimeRange;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionSchedulingService {

    private static final int MAX_LOOPS = 1000;

    private static final int MILLS = 1000;

    private static final int ONE_DAY = 1;

    @Autowired
    private ShiftsDataProvider shiftsDataProvider;

    @Autowired
    private ShiftExceptionService shiftExceptionService;

    private Optional<DateTime> findDateFromDatePlusMilliseconds(Entity order, Date orderStartDate, long milliseconds) {
        DateTime orderStartDateDT = new DateTime(orderStartDate, DateTimeZone.getDefault());

        List<Shift> shifts = shiftsDataProvider.findAll();
        DateTime dateOfDay = new DateTime(orderStartDate);
        dateOfDay = dateOfDay.minusDays(ONE_DAY);
        dateOfDay = dateOfDay.toLocalDate().toDateTimeAtStartOfDay();
        Long leftMilliseconds = milliseconds;
        int loopCount = 0;
        while (leftMilliseconds > 0L) {
            if (loopCount > MAX_LOOPS) {
                return Optional.empty();
            }
            for (Shift shift : shifts) {
                for (DateTimeRange range : shiftExceptionService
                        .getShiftWorkDateTimes(order.getBelongsToField(OrderFields.PRODUCTION_LINE), shift, dateOfDay)) {
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

    private Optional<DateTime> findFirstWorkingDate(Entity order, Date orderStartDate) {
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
                for (DateTimeRange range : shiftExceptionService
                        .getShiftWorkDateTimes(order.getBelongsToField(OrderFields.PRODUCTION_LINE), shift, dateOfDay)) {
                    if (orderStartDate.after(dateOfDay.toDate())) {
                        range = range.trimBefore(orderStartDateDT);
                    }
                    if (range != null && range.durationInMins() > 0l) {

                        return Optional.of(range.getFrom());

                    }
                }
            }
            loopCount++;
            dateOfDay = dateOfDay.plusDays(1);
        }

        return Optional.empty();
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
