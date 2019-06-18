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

import java.util.Collection;
import java.util.SortedSet;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Sets;
import com.qcadoo.mes.timeGapsPreview.util.IntervalsComparator;
import com.qcadoo.mes.timeGapsPreview.util.TimeGapsBuilderImpl;

public class TimeGapsBuilderImplTest {

    private static final DateTime FROM = new DateTime(2013, 1, 8, 0, 0, 0);

    private static final DateTime TO = FROM.plusDays(1);

    private static final Interval DOMAIN_INTERVAL = new Interval(FROM, TO);

    private TimeGapsBuilderImpl timeGapsBuilder;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        timeGapsBuilder = new TimeGapsBuilderImpl(DOMAIN_INTERVAL);
    }

    private SortedSet<Interval> makeSortedSet(final Collection<Interval> intervals) {
        SortedSet<Interval> sortedSet = makeSortedSet();
        sortedSet.addAll(intervals);
        return sortedSet;
    }

    private SortedSet<Interval> makeSortedSet() {
        return Sets.newTreeSet(IntervalsComparator.START_DATE_ASC_AND_DURATION_DESC);
    }

    // calculateGaps

    @Test
    public void shouldCalculateGaps() {
        // given
        SortedSet<Interval> intervals = makeSortedSet();
        SortedSet<Interval> expectedGaps = makeSortedSet();

        Interval abuts1Left = new Interval(FROM, FROM.plusHours(1));
        Interval abuts1Right = new Interval(FROM.plusHours(1), FROM.plusHours(2));
        intervals.add(abuts1Left);
        intervals.add(abuts1Right);

        Interval abuts1Sum = new Interval(abuts1Left.getStart(), abuts1Right.getEnd());

        Interval overlaps1Left = new Interval(FROM.plusHours(3), FROM.plusHours(5));
        Interval overlaps1Right = new Interval(FROM.plusHours(4), FROM.plusHours(6));
        intervals.add(overlaps1Left);
        intervals.add(overlaps1Right);

        Interval overlaps1Sum = new Interval(overlaps1Left.getStart(), overlaps1Right.getEnd());

        expectedGaps.add(abuts1Sum.gap(overlaps1Sum));

        Interval single1 = new Interval(FROM.plusHours(7), FROM.plusHours(8));
        intervals.add(single1);

        expectedGaps.add(overlaps1Sum.gap(single1));

        Interval contains1Left = new Interval(FROM.plusHours(10), FROM.plusHours(13));
        Interval contains1Right = new Interval(FROM.plusHours(11), FROM.plusHours(12));
        intervals.add(contains1Left);
        intervals.add(contains1Right);
        Interval contains1Sum = new Interval(contains1Left.getStart(), contains1Left.getEnd());

        expectedGaps.add(single1.gap(contains1Sum));

        Interval contains2Left = new Interval(FROM.plusHours(14), FROM.plusHours(16));
        Interval contains2Right = new Interval(FROM.plusHours(14), FROM.plusHours(15));
        intervals.add(contains2Left);
        intervals.add(contains2Right);
        Interval contains2Sum = new Interval(contains2Left.getStart(), contains2Left.getEnd());

        expectedGaps.add(contains1Sum.gap(contains2Sum));

        Interval contains3Left = new Interval(FROM.plusHours(17), FROM.plusHours(19));
        Interval contains3Right = new Interval(FROM.plusHours(18), FROM.plusHours(19));
        intervals.add(contains3Left);
        intervals.add(contains3Right);
        Interval contains3Sum = new Interval(contains3Left.getStart(), contains3Right.getEnd());

        expectedGaps.add(contains2Sum.gap(contains3Sum));

        Interval equals1First = new Interval(FROM.plusHours(20), FROM.plusHours(21));
        intervals.add(equals1First);
        intervals.add(equals1First);

        expectedGaps.add(contains3Sum.gap(equals1First));

        Interval single2 = new Interval(FROM.plusHours(22), FROM.plusHours(24));
        intervals.add(single2);

        expectedGaps.add(equals1First.gap(single2));

        if (single2.getEnd().isBefore(TO)) {
            expectedGaps.add(new Interval(single2.getEnd(), TO));
        }

        // when
        for (Interval interval : intervals) {
            timeGapsBuilder.addOccupiedInterval(interval);
        }
        Collection<Interval> actualGaps = timeGapsBuilder.calculateGaps();

        // then
        Assert.assertEquals(expectedGaps, actualGaps);
    }

    @Test
    public void shouldCalculateGaps2() {
        // given
        SortedSet<Interval> intervals = makeSortedSet();
        SortedSet<Interval> expectedGaps = makeSortedSet();

        Interval i1 = new Interval(FROM, FROM.plusHours(5));
        Interval i2 = new Interval(FROM.plusHours(1), FROM.plusHours(2));
        Interval i3 = new Interval(FROM.plusHours(4), FROM.plusHours(6));
        intervals.add(i1);
        intervals.add(i2);
        intervals.add(i3);

        expectedGaps.add(new Interval(FROM.plusHours(6), TO));

        // when
        for (Interval interval : intervals) {
            timeGapsBuilder.addOccupiedInterval(interval);
        }
        Collection<Interval> actualGaps = timeGapsBuilder.calculateGaps();

        // then
        Assert.assertEquals(expectedGaps, actualGaps);
    }

    @Test
    public void shouldCalculateGapsReturnsTheSearchRangeIntervalIfThereIsNoOccupiedIntervals() {
        // given
        Collection<Interval> expectedGaps = Sets.newTreeSet(IntervalsComparator.START_DATE_ASC_AND_DURATION_DESC);
        expectedGaps.add(new Interval(FROM, TO));

        // when
        Collection<Interval> actualGaps = timeGapsBuilder.calculateGaps();

        // then
        Assert.assertEquals(expectedGaps, actualGaps);
    }

    @Test
    public void shouldCalculateGapsReturnsTheSearchRangeIntervalIfThereIsNoOccupiedIntervalsOverlapingTheSearchRange() {
        // given
        Interval i1 = new Interval(FROM.minusHours(5), FROM);
        Interval i2 = new Interval(TO, TO.plusHours(8));

        Collection<Interval> expectedGaps = Sets.newTreeSet(IntervalsComparator.START_DATE_ASC_AND_DURATION_DESC);
        expectedGaps.add(new Interval(FROM, TO));

        // when
        timeGapsBuilder.addOccupiedInterval(i1);
        timeGapsBuilder.addOccupiedInterval(i2);
        Collection<Interval> actualGaps = timeGapsBuilder.calculateGaps();

        // then
        Assert.assertEquals(expectedGaps, actualGaps);
    }

    // addOccupiedInterval

    @Test
    public void shouldConstructorThrowIllegalArgumentExceptionForNullInterval() {
        try {
            new TimeGapsBuilderImpl(null);
            Assert.fail();
        } catch (IllegalArgumentException ignored) {
            // SUCCESS
        }
    }

    // flatten

    @Test
    public void shouldFlattenDoNothingWithEmptyIntervalsCollection() {
        // given
        SortedSet<Interval> intervals = makeSortedSet();

        // when
        SortedSet<Interval> flattenIntervals = timeGapsBuilder.flatten(intervals);

        // then
        Assert.assertEquals(intervals, flattenIntervals);
    }

    @Test
    public void shouldFlattenDoNothingWithOneElementIntervalsCollection() {
        // given
        SortedSet<Interval> intervals = makeSortedSet(Sets.newHashSet(new Interval(FROM.plusHours(1), FROM.plusHours(3))));

        // when
        SortedSet<Interval> flattenIntervals = timeGapsBuilder.flatten(intervals);

        // then
        Assert.assertEquals(intervals, flattenIntervals);
    }

    @Test
    public void shouldFlattenDoNothingIfAllIntervalsAreDisjoint() {
        // given
        SortedSet<Interval> intervals = makeSortedSet();
        intervals.add(new Interval(FROM, FROM.plusHours(2)));
        intervals.add(new Interval(FROM.plusHours(3), FROM.plusHours(4)));
        intervals.add(new Interval(FROM.plusHours(5), FROM.plusHours(8)));
        intervals.add(new Interval(FROM.plusHours(12), FROM.plusHours(16)));

        // when
        SortedSet<Interval> flattenIntervals = timeGapsBuilder.flatten(intervals);

        // then
        Assert.assertEquals(intervals, flattenIntervals);
    }

    @Test
    public void shouldFlattenJoinIntervals() {
        // given
        SortedSet<Interval> intervals = makeSortedSet();
        SortedSet<Interval> expected = makeSortedSet();

        Interval abuts1Left = new Interval(FROM, FROM.plusHours(1));
        Interval abuts1Right = new Interval(FROM.plusHours(1), FROM.plusHours(2));
        intervals.add(abuts1Left);
        intervals.add(abuts1Right);
        Interval abuts1Sum = new Interval(abuts1Left.getStart(), abuts1Right.getEnd());
        expected.add(abuts1Sum);

        Interval overlaps1Left = new Interval(FROM.plusHours(3), FROM.plusHours(5));
        Interval overlaps1Right = new Interval(FROM.plusHours(4), FROM.plusHours(6));
        intervals.add(overlaps1Left);
        intervals.add(overlaps1Right);
        Interval overlaps1Sum = new Interval(overlaps1Left.getStart(), overlaps1Right.getEnd());
        expected.add(overlaps1Sum);

        Interval single1 = new Interval(FROM.plusHours(7), FROM.plusHours(8));
        intervals.add(single1);
        expected.add(single1);

        Interval contains1Left = new Interval(FROM.plusHours(10), FROM.plusHours(13));
        Interval contains1Right = new Interval(FROM.plusHours(11), FROM.plusHours(12));
        intervals.add(contains1Left);
        intervals.add(contains1Right);
        Interval contains1Sum = new Interval(contains1Left.getStart(), contains1Left.getEnd());
        expected.add(contains1Sum);

        Interval contains2Left = new Interval(FROM.plusHours(14), FROM.plusHours(16));
        Interval contains2Right = new Interval(FROM.plusHours(14), FROM.plusHours(15));
        intervals.add(contains2Left);
        intervals.add(contains2Right);
        Interval contains2Sum = new Interval(contains2Left.getStart(), contains2Left.getEnd());
        expected.add(contains2Sum);

        Interval contains3Left = new Interval(FROM.plusHours(17), FROM.plusHours(19));
        Interval contains3Right = new Interval(FROM.plusHours(18), FROM.plusHours(19));
        intervals.add(contains3Left);
        intervals.add(contains3Right);
        Interval contains3Sum = new Interval(contains3Left.getStart(), contains3Right.getEnd());
        expected.add(contains3Sum);

        Interval equals1First = new Interval(FROM.plusHours(20), FROM.plusHours(21));
        intervals.add(equals1First);
        intervals.add(equals1First);
        expected.add(equals1First);

        Interval single2 = new Interval(FROM.plusHours(22), FROM.plusHours(24));
        intervals.add(single2);
        expected.add(single2);

        // when
        SortedSet<Interval> flattenIntervals = timeGapsBuilder.flatten(intervals);

        // then
        Assert.assertEquals(expected, flattenIntervals);
    }

    @Test
    public void shouldFlattenJoinIntervals2() {
        // given
        SortedSet<Interval> intervals = makeSortedSet();
        SortedSet<Interval> expected = makeSortedSet();

        Interval i1 = new Interval(FROM, FROM.plusHours(5));
        Interval i2 = new Interval(FROM.plusHours(1), FROM.plusHours(2));
        Interval i3 = new Interval(FROM.plusHours(4), FROM.plusHours(6));
        intervals.add(i1);
        intervals.add(i2);
        intervals.add(i3);

        expected.add(new Interval(FROM, FROM.plusHours(6)));

        // when
        SortedSet<Interval> flattenIntervals = timeGapsBuilder.flatten(intervals);

        // then
        Assert.assertEquals(expected, flattenIntervals);
    }
}
