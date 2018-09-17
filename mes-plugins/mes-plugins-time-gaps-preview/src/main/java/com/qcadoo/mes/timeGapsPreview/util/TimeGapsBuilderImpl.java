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
package com.qcadoo.mes.timeGapsPreview.util;

import java.util.Collection;
import java.util.SortedSet;

import org.joda.time.Interval;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

public class TimeGapsBuilderImpl implements TimeGapsBuilder {

    private final Interval searchInterval;

    private final SortedSet<Interval> occupiedIntervals;

    public TimeGapsBuilderImpl(final Interval interval) {
        Preconditions.checkArgument(interval != null, "Search interval have to be provided.");
        searchInterval = interval;
        occupiedIntervals = Sets.newTreeSet(IntervalsComparator.START_DATE_ASC_AND_DURATION_DESC);
    }

    @Override
    public void addOccupiedIntervals(final Iterable<Interval> intervals) {
        Preconditions.checkArgument(intervals != null, "Given intervals iterable should be not null");
        for (Interval interval : intervals) {
            addOccupiedInterval(interval);
        }
    }

    @Override
    public void addOccupiedInterval(final Interval interval) {
        if (interval != null && searchInterval.overlaps(interval)) {
            occupiedIntervals.add(interval);
        }
    }

    public static SortedSet<Interval> flatten(final SortedSet<Interval> intervalsSet) {
        if (intervalsSet.size() < 2) {
            return intervalsSet;
        }
        SortedSet<Interval> flattenIntervals = newSortedSet();
        Interval prev = intervalsSet.first();

        flattenIntervals.add(prev);
        for (Interval interval : intervalsSet) {
            if (interval.contains(prev.getEnd()) || interval.abuts(prev)) {
                Interval sumInterval = new Interval(prev.getStart(), interval.getEnd());
                flattenIntervals.remove(prev);
                flattenIntervals.add(sumInterval);
                prev = sumInterval;
            } else if (!prev.contains(interval)) {
                flattenIntervals.add(interval);
                prev = interval;
            }
        }
        return flattenIntervals;
    }

    private static SortedSet<Interval> newSortedSet() {
        return Sets.newTreeSet(IntervalsComparator.START_DATE_ASC_AND_DURATION_DESC);
    }

    @Override
    public Collection<Interval> calculateGaps() {
        SortedSet<Interval> gaps = newSortedSet();
        SortedSet<Interval> flattenOccupiedIntervals = flatten(occupiedIntervals);
        if (flattenOccupiedIntervals.isEmpty()) {
            gaps.add(searchInterval);
            return gaps;
        }

        Interval prev = flattenOccupiedIntervals.first();
        if (searchInterval.getStart().isBefore(prev.getStart())) {
            gaps.add(new Interval(searchInterval.getStart(), prev.getStart()));
        }
        flattenOccupiedIntervals.remove(prev);
        for (Interval occupied : flattenOccupiedIntervals) {
            gaps.add(prev.gap(occupied));
            prev = occupied;
        }
        if (prev.getEnd().isBefore(searchInterval.getEnd())) {
            gaps.add(new Interval(prev.getEnd(), searchInterval.getEnd()));
        }
        return gaps;
    }

}
