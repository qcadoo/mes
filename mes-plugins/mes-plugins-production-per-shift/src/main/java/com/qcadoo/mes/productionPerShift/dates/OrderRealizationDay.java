package com.qcadoo.mes.productionPerShift.dates;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
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
