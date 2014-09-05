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
