package com.qcadoo.mes.productionPerShift.dates;

import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.qcadoo.commons.dateTime.TimeRange;
import com.qcadoo.commons.functional.LazyStream;
import com.qcadoo.mes.basic.shift.Shift;

public class OrderRealizationDaysResolverTest extends ShiftMockingAwareTest {

    private static final LocalTime SH_1_START = new LocalTime(22, 0, 0);

    private static final LocalTime SH_1_END = new LocalTime(6, 0, 0);

    private static final LocalTime SH_2_START = new LocalTime(6, 0, 0);

    private static final LocalTime SH_2_END = new LocalTime(14, 0, 0);

    private static final LocalTime SH_3_START = new LocalTime(14, 0, 0);

    private static final LocalTime SH_3_END = new LocalTime(22, 0, 0);

    private static final Set<Integer> MON_FRI_WORKING_DAYS = ImmutableSet.of(DateTimeConstants.MONDAY, DateTimeConstants.TUESDAY,
            DateTimeConstants.WEDNESDAY, DateTimeConstants.THURSDAY, DateTimeConstants.FRIDAY);

    private OrderRealizationDaysResolver orderRealizationDaysResolver;

    private Shift shift1, shift2, shift3;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderRealizationDaysResolver = new OrderRealizationDaysResolver();

        shift1 = mockShift(new TimeRange(SH_1_START, SH_1_END), MON_FRI_WORKING_DAYS);
        shift2 = mockShift(new TimeRange(SH_2_START, SH_2_END), MON_FRI_WORKING_DAYS);
        shift3 = mockShift(new TimeRange(SH_3_START, SH_3_END), MON_FRI_WORKING_DAYS);
    }

    private void assertRealizationDayState(final OrderRealizationDay realizationDay, final int numOfDay, final LocalDate date,
            final List<Shift> workingShifts) {
        Assert.assertEquals(workingShifts, realizationDay.getWorkingShifts());
        Assert.assertEquals(numOfDay, realizationDay.getRealizationDayNumber());
        Assert.assertEquals(date, realizationDay.getDate());
    }

    @Test
    public final void shouldResolveRealizationDay1() {
        // given
        DateTime startDate = new DateTime(2014, 8, 14, 10, 0, 0);
        List<Shift> shifts = ImmutableList.of(shift1, shift2, shift3);

        // when
        // - 1st order realization day is Thursday,
        // - it's a first day of order realization,
        // - shift starting order doesn't start day before,
        OrderRealizationDay realizationDay = orderRealizationDaysResolver.find(startDate, 1, true, shifts);

        // then
        assertRealizationDayState(realizationDay, 1, startDate.toLocalDate(), shifts);
    }

    @Test
    public final void shouldResolveRealizationDay2() {
        // given
        DateTime startDate = new DateTime(2014, 8, 14, 3, 0, 0);
        List<Shift> shifts = ImmutableList.of(shift1, shift2, shift3);

        // when
        // - 1st order realization day is Thursday,
        // - it's a first day of order realization,
        // - shift starting order starts day before,
        // - 'day before' mentioned above is work day
        OrderRealizationDay realizationDay = orderRealizationDaysResolver.find(startDate, 1, true, shifts);

        // then
        assertRealizationDayState(realizationDay, 0, startDate.toLocalDate().minusDays(1), ImmutableList.of(shift1));
    }

    @Test
    public final void shouldResolveRealizationDay3() {
        // given
        DateTime startDate = new DateTime(2014, 8, 14, 3, 0, 0);
        List<Shift> shifts = ImmutableList.of(shift1, shift2, shift3);

        // when
        // - 1st order realization day is Thursday,
        // - shift working at given time doesn't work at this date but it's not a first day of order realization,
        OrderRealizationDay realizationDay = orderRealizationDaysResolver.find(startDate, 1, false, shifts);

        // then
        assertRealizationDayState(realizationDay, 1, startDate.toLocalDate(), shifts);
    }

    @Test
    public final void shouldResolveRealizationDay4() {
        // given
        DateTime startDate = new DateTime(2014, 8, 14, 3, 0, 0);
        List<Shift> shifts = ImmutableList.of(shift1, shift2, shift3);

        // when
        // - 3rd order realization day is Saturday,
        // - shift working at given time doesn't work at this date but it's not a first day of order realization,
        OrderRealizationDay realizationDay = orderRealizationDaysResolver.find(startDate, 3, false, shifts);

        // then
        assertRealizationDayState(realizationDay, 5, startDate.toLocalDate().plusDays(4), shifts);
    }

    @Test
    public final void shouldResolveRealizationDay5() {
        // given
        DateTime startDate = new DateTime(2014, 8, 14, 12, 0, 0);
        List<Shift> shifts = ImmutableList.of(shift1, shift2, shift3);

        // when
        // - 3rd order realization day is Saturday,
        // - shift working at given time will work at this date,
        OrderRealizationDay realizationDay = orderRealizationDaysResolver.find(startDate, 3, false, shifts);

        // then
        assertRealizationDayState(realizationDay, 5, startDate.toLocalDate().plusDays(4), shifts);
    }

    @Test
    public final void shouldResolveRealizationDay6() {
        // given
        DateTime startDate = new DateTime(2014, 8, 14, 3, 0, 0);
        List<Shift> shifts = ImmutableList.of(shift1, shift2, shift3);

        // when
        // - 3rd order realization day is Saturday,
        // - it's a first day of order realization,
        // - shift starting order starts day before,
        // - 'day before' mentioned above is spare day
        OrderRealizationDay realizationDay = orderRealizationDaysResolver.find(startDate, 3, true, shifts);

        // then
        assertRealizationDayState(realizationDay, 5, startDate.toLocalDate().plusDays(4), shifts);
    }

    @Test
    public final void shouldNotResultInAnInfiniteCycleIfThereIsNoShiftsDefined() {
        // given
        DateTime startDate = new DateTime(2014, 8, 14, 3, 0, 0);
        List<Shift> shifts = ImmutableList.of();

        // when
        OrderRealizationDay realizationDay = orderRealizationDaysResolver.find(startDate, 1, true, shifts);

        // then
        assertRealizationDayState(realizationDay, 1, startDate.toLocalDate(), ImmutableList.<Shift> of());
    }

    @Test
    public final void shouldNotResultInAnInfiniteCycleIfShiftsNeverWorks() {
        // given
        DateTime startDate = new DateTime(2014, 8, 14, 3, 0, 0);
        Shift lazyShift = mockShift(new TimeRange(SH_1_START, SH_1_END), ImmutableSet.<Integer> of());
        List<Shift> shifts = ImmutableList.of(lazyShift);

        // when
        OrderRealizationDay realizationDay = orderRealizationDaysResolver.find(startDate, 1, true, shifts);

        // then
        assertRealizationDayState(realizationDay, 1, startDate.toLocalDate(), ImmutableList.<Shift> of());
    }

    @Test
    public final void shouldProduceStreamOfRealizationDays1() {
        // given
        DateTime startDate = new DateTime(2014, 8, 14, 3, 0, 0);
        List<Shift> shifts = ImmutableList.of(shift1, shift2, shift3);

        // when
        LazyStream<OrderRealizationDay> stream = orderRealizationDaysResolver.asStreamFrom(startDate, shifts);

        // then
        OrderRealizationDay[] streamVals = FluentIterable.from(stream).limit(5).toArray(OrderRealizationDay.class);
        assertRealizationDayState(streamVals[0], 0, startDate.toLocalDate().minusDays(1), ImmutableList.of(shift1));
        assertRealizationDayState(streamVals[1], 1, startDate.toLocalDate(), shifts);
        assertRealizationDayState(streamVals[2], 2, startDate.toLocalDate().plusDays(1), shifts);
        assertRealizationDayState(streamVals[3], 5, startDate.toLocalDate().plusDays(4), shifts);
        assertRealizationDayState(streamVals[4], 6, startDate.toLocalDate().plusDays(5), shifts);
    }

    @Test
    public final void shouldProduceStreamOfRealizationDays2() {
        // given
        DateTime startDate = new DateTime(2014, 8, 14, 14, 0, 0);
        List<Shift> shifts = ImmutableList.of(shift1, shift2, shift3);

        // when
        LazyStream<OrderRealizationDay> stream = orderRealizationDaysResolver.asStreamFrom(startDate, shifts);

        // then
        OrderRealizationDay[] streamVals = FluentIterable.from(stream).limit(5).toArray(OrderRealizationDay.class);
        assertRealizationDayState(streamVals[0], 1, startDate.toLocalDate(), shifts);
        assertRealizationDayState(streamVals[1], 2, startDate.toLocalDate().plusDays(1), shifts);
        assertRealizationDayState(streamVals[2], 5, startDate.toLocalDate().plusDays(4), shifts);
        assertRealizationDayState(streamVals[3], 6, startDate.toLocalDate().plusDays(5), shifts);
        assertRealizationDayState(streamVals[4], 7, startDate.toLocalDate().plusDays(6), shifts);
    }
}
