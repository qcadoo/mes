/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.productionPerShift.dates;

import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.qcadoo.commons.dateTime.TimeRange;
import com.qcadoo.commons.functional.LazyStream;
import com.qcadoo.mes.basic.shift.Shift;

@Service
public class OrderRealizationDaysResolver {

    public LazyStream<OrderRealizationDay> asStreamFrom(final DateTime orderStartDateTime, final List<Shift> shifts) {
        OrderRealizationDay firstDay = find(orderStartDateTime, 1, true, shifts);
        return LazyStream.create(firstDay,
                prevElement -> find(orderStartDateTime, prevElement.getRealizationDayNumber() + 1, false, shifts));
    }

    public OrderRealizationDay find(final DateTime orderStartDateTime, final int startingFrom, final boolean isFirstDay,
            final List<Shift> shifts) {
        LocalDate orderStartDate = orderStartDateTime.toLocalDate();
        LocalTime orderStartTime = orderStartDateTime.toLocalTime();
        OrderRealizationDay firstWorkingDay = findFirstWorkingDayFrom(orderStartDate, startingFrom, shifts);
        Optional<Shift> shiftStartingOrder = firstWorkingDay.findShiftWorkingAt(orderStartDateTime.toLocalTime());
        if (isFirstDay && startingShiftIsWorkingSincePrevDay(shiftStartingOrder, firstWorkingDay, orderStartTime)) {
            return new OrderRealizationDay(orderStartDate, firstWorkingDay.getRealizationDayNumber() - 1,
                    Lists.newArrayList(shiftStartingOrder.asSet()));
        }
        return firstWorkingDay;
    }

    private boolean startingShiftIsWorkingSincePrevDay(final Optional<Shift> maybeShift,
            final OrderRealizationDay firstWorkingDay, final LocalTime orderStartTime) {
        return maybeShift.transform(new Function<Shift, Boolean>() {

            @Override
            public Boolean apply(final Shift shift) {
                int prevDayOfWeek = firstWorkingDay.getDate().minusDays(1).getDayOfWeek();
                return shift.findWorkTimeAt(prevDayOfWeek, orderStartTime).transform(TIME_RANGE_STARTS_PREV_DAY).or(false);
            }
        }).or(false);
    }

    private static final Function<TimeRange, Boolean> TIME_RANGE_STARTS_PREV_DAY = new Function<TimeRange, Boolean>() {

        @Override
        public Boolean apply(final TimeRange timeRange) {
            return timeRange.startsDayBefore();
        }
    };

    private static final int DAYS_IN_YEAR = 365;

    private OrderRealizationDay findFirstWorkingDayFrom(final LocalDate orderStartDate, final int startFrom,
            final List<Shift> shifts) {
        for (int offset = startFrom; offset < startFrom + DAYS_IN_YEAR; offset++) {
            List<Shift> workingShifts = getShiftsWorkingAt(orderStartDate.plusDays(offset - 1).getDayOfWeek(), shifts);
            if (!workingShifts.isEmpty()) {
                return new OrderRealizationDay(orderStartDate, offset, workingShifts);
            }
        }
        // assuming that no working shifts in period of year length means that client does not use shifts calendar and there won't
        // be any offsets caused by work free time boundaries.
        return new OrderRealizationDay(orderStartDate, startFrom, Collections.<Shift> emptyList());
    }

    private List<Shift> getShiftsWorkingAt(final int dayOfWeek, final List<Shift> shifts) {
        return FluentIterable.from(shifts).filter(new Predicate<Shift>() {

            @Override
            public boolean apply(final Shift shift) {
                return shift.worksAt(dayOfWeek);
            }
        }).toList();
    }

}
