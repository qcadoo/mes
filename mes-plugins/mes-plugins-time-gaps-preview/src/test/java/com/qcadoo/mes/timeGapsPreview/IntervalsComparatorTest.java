/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.timeGapsPreview;

import com.qcadoo.mes.timeGapsPreview.util.IntervalsComparator;
import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Test;

public class IntervalsComparatorTest {

    private static final DateTime TEST_DATE = new DateTime(2013, 1, 8, 12, 0, 0);

    @Test
    public void shouldByStartDateComparatorReturnZeroForEqualIntervals() {
        // given
        Interval someInterval = new Interval(TEST_DATE, TEST_DATE.plusHours(3));

        // when
        int compareRes = IntervalsComparator.START_DATE_ASC_AND_DURATION_DESC.compare(someInterval, someInterval);

        // then
        Assert.assertEquals(0, compareRes);
    }

    @Test
    public void shouldByStartDateComparatorReturnOneForTwoIntervalsWithEqualStartDatesButDifferentDurations() {
        // given
        Interval shortInterval = new Interval(TEST_DATE, TEST_DATE.plusHours(3));
        Interval longInterval = new Interval(TEST_DATE, TEST_DATE.plusHours(12));

        // when
        int compareRes = IntervalsComparator.START_DATE_ASC_AND_DURATION_DESC.compare(shortInterval, longInterval);

        // then
        Assert.assertEquals(1, compareRes);
    }

    @Test
    public void shouldByStartDateComparatorCompareTwoIntervalsWithDifferentStartDates() {
        // given
        Interval earlyInterval = new Interval(TEST_DATE, TEST_DATE.plusHours(3));
        Interval lateInterval = new Interval(TEST_DATE.plusHours(6), TEST_DATE.plusHours(12));

        // when
        int compareRes = IntervalsComparator.START_DATE_ASC_AND_DURATION_DESC.compare(earlyInterval, lateInterval);

        // then
        Assert.assertEquals(-1, compareRes);
    }

}
