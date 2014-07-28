package com.qcadoo.mes.productionPerShift.dates;

import junit.framework.Assert;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.qcadoo.mes.basic.shift.Shift;

public class OrderRealizationDayTest {

    private static final LocalDate START_DATE = new LocalDate(2014, 8, 11);

    @Mock
    private Shift shift1, shift2;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public final void shouldReturnCorrectDayNumberAndDate() {
        // given
        Iterable<Shift> shifts = ImmutableList.of(shift1, shift2);

        // when
        OrderRealizationDay prevDay = new OrderRealizationDay(START_DATE, 0, shifts);
        OrderRealizationDay firstDay = new OrderRealizationDay(START_DATE, 1, shifts);
        OrderRealizationDay secondDay = new OrderRealizationDay(START_DATE, 2, shifts);

        // then
        Assert.assertEquals(START_DATE.minusDays(1), prevDay.getDate());
        Assert.assertEquals(0, prevDay.getRealizationDayNumber());
        Assert.assertEquals(shifts, prevDay.getWorkingShifts());

        Assert.assertEquals(START_DATE, firstDay.getDate());
        Assert.assertEquals(1, firstDay.getRealizationDayNumber());
        Assert.assertEquals(shifts, firstDay.getWorkingShifts());

        Assert.assertEquals(START_DATE.plusDays(1), secondDay.getDate());
        Assert.assertEquals(2, secondDay.getRealizationDayNumber());
        Assert.assertEquals(shifts, secondDay.getWorkingShifts());
    }

}
