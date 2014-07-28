package com.qcadoo.mes.orders.dates;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.base.Optional;

public class ThreeLevelDateTest {

    private static final DateTime DATE_1 = DateTime.now();

    private static final DateTime DATE_2 = DATE_1.plusDays(1);

    private static final DateTime DATE_3 = DATE_2.plusDays(1);

    @Test
    public final void shouldReturnCorrectDates1() {
        // when
        ThreeLevelDate threeLevelDate = new ThreeLevelDate(DATE_1, Optional.of(DATE_2), Optional.of(DATE_3));

        // then
        Assert.assertEquals(DATE_1, threeLevelDate.planned());
        Assert.assertEquals(Optional.of(DATE_2), threeLevelDate.corrected());
        Assert.assertEquals(Optional.of(DATE_3), threeLevelDate.effective());

        Assert.assertEquals(DATE_2, threeLevelDate.correctedWithFallback());
        Assert.assertEquals(DATE_3, threeLevelDate.effectiveWithFallback());
    }

    @Test
    public final void shouldReturnCorrectDates2() {
        // when
        ThreeLevelDate threeLevelDate = new ThreeLevelDate(DATE_1, Optional.of(DATE_2), Optional.<DateTime> absent());

        // then
        Assert.assertEquals(DATE_1, threeLevelDate.planned());
        Assert.assertEquals(Optional.of(DATE_2), threeLevelDate.corrected());
        Assert.assertEquals(Optional.<DateTime> absent(), threeLevelDate.effective());

        Assert.assertEquals(DATE_2, threeLevelDate.correctedWithFallback());
        Assert.assertEquals(DATE_2, threeLevelDate.effectiveWithFallback());
    }

    @Test
    public final void shouldReturnCorrectDates3() {
        // when
        ThreeLevelDate threeLevelDate = new ThreeLevelDate(DATE_1, Optional.<DateTime> absent(), Optional.<DateTime> absent());

        // then
        Assert.assertEquals(DATE_1, threeLevelDate.planned());
        Assert.assertEquals(Optional.<DateTime> absent(), threeLevelDate.corrected());
        Assert.assertEquals(Optional.<DateTime> absent(), threeLevelDate.effective());

        Assert.assertEquals(DATE_1, threeLevelDate.correctedWithFallback());
        Assert.assertEquals(DATE_1, threeLevelDate.effectiveWithFallback());
    }

    @Test
    public final void shouldReturnCorrectDates4() {
        // when
        ThreeLevelDate threeLevelDate = new ThreeLevelDate(DATE_1, Optional.<DateTime> absent(), Optional.of(DATE_3));

        // then
        Assert.assertEquals(DATE_1, threeLevelDate.planned());
        Assert.assertEquals(Optional.<DateTime> absent(), threeLevelDate.corrected());
        Assert.assertEquals(Optional.of(DATE_3), threeLevelDate.effective());

        Assert.assertEquals(DATE_1, threeLevelDate.correctedWithFallback());
        Assert.assertEquals(DATE_3, threeLevelDate.effectiveWithFallback());
    }

}
