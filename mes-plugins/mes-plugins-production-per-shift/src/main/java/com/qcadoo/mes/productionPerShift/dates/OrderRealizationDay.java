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

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.shift.Shift;

public class OrderRealizationDay {

    private final List<Shift> workingShifts;

    private final int realizationDayNumber;

    private final LocalDate orderStartDate;

    public static final Function<OrderRealizationDay, Integer> EXTRACT_DAY_NUM = new Function<OrderRealizationDay, Integer>() {

        @Override
        public Integer apply(final OrderRealizationDay input) {
            return input.getRealizationDayNumber();
        }
    };

    public static final Function<OrderRealizationDay, LocalDate> EXTRACT_DATE = new Function<OrderRealizationDay, LocalDate>() {

        @Override
        public LocalDate apply(final OrderRealizationDay input) {
            return input.getDate();
        }
    };

    OrderRealizationDay(final LocalDate orderStartDate, final int day, final Iterable<Shift> shifts) {
        this.orderStartDate = orderStartDate;
        this.realizationDayNumber = day;
        this.workingShifts = Lists.newArrayList(shifts);
    }

    public Optional<Shift> findShiftWorkingAt(final LocalTime time) {
        final int dayOfWeek = orderStartDate.plusDays(realizationDayNumber - 1).getDayOfWeek();
        return FluentIterable.from(workingShifts).firstMatch(new Predicate<Shift>() {

            @Override
            public boolean apply(final Shift shift) {
                return shift.worksAt(dayOfWeek, time);
            }
        });
    }

    public List<Shift> getWorkingShifts() {
        return workingShifts;
    }

    public int getRealizationDayNumber() {
        return realizationDayNumber;
    }

    public LocalDate getDate() {
        return orderStartDate.plusDays(realizationDayNumber - 1);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(realizationDayNumber).append(orderStartDate).append(workingShifts).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OrderRealizationDay)) {
            return false;
        }

        OrderRealizationDay other = (OrderRealizationDay) obj;

        return new EqualsBuilder().append(realizationDayNumber, other.realizationDayNumber)
                .append(orderStartDate, other.orderStartDate).append(workingShifts, other.workingShifts).isEquals();
    }

}
