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

import java.util.*;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.qcadoo.mes.timeGapsPreview.constants.TimeGapFields;
import com.qcadoo.mes.timeGapsPreview.util.IntervalsComparator;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class TimeGapsSearchResult {

    private final Duration totalDuration;

    private final List<Entity> timeGapEntities;

    private final Map.Entry<Long, Interval> longestIntervalWithLineId;

    public static TimeGapsSearchResult create(final Multimap<Long, Interval> multimap, final DataDefinition timeGapDataDef) {
        Collection<Entity> entities = Collections2.transform(multimap.entries(),
                new Function<Map.Entry<Long, Interval>, Entity>() {

                    @Override
                    public Entity apply(final Map.Entry<Long, Interval> lineIdToInterval) {
                        if (lineIdToInterval == null) {
                            return null;
                        }
                        return buildEntityFromGapsEntry(timeGapDataDef, lineIdToInterval);
                    }
                });
        return new TimeGapsSearchResult(multimap, entities);
    }

    private static Entity buildEntityFromGapsEntry(final DataDefinition timeGapDataDef, final Map.Entry<Long, Interval> mapEntry) {
        Entity timeGap = timeGapDataDef.create();
        Interval gapInterval = mapEntry.getValue();
        timeGap.setField(TimeGapFields.PRODUCTION_LINE, mapEntry.getKey());
        timeGap.setField(TimeGapFields.FROM_DATE, gapInterval.getStart().toDate());
        timeGap.setField(TimeGapFields.TO_DATE, gapInterval.getEnd().toDate());
        timeGap.setField(TimeGapFields.DURATION, ((Long) gapInterval.toDuration().getStandardSeconds()).intValue());
        return timeGap;
    }

    private TimeGapsSearchResult(final Multimap<Long, Interval> multimap, final Iterable<Entity> timeGapEntities) {
        if (multimap.isEmpty()) {
            this.timeGapEntities = Collections.unmodifiableList(Collections.<Entity> emptyList());
            this.longestIntervalWithLineId = null;
            this.totalDuration = Duration.ZERO;
        } else {
            this.timeGapEntities = Collections.unmodifiableList(TIME_GAPS_ENTITIES_BY_DURATION_ORDERING.reverse().sortedCopy(
                    timeGapEntities));
            this.longestIntervalWithLineId = findLongestInterval(multimap);
            this.totalDuration = calcTotalDuration(multimap.values());
        }

    }

    private Map.Entry<Long, Interval> findLongestInterval(final Multimap<Long, Interval> multimap) {
        return ENTRY_BY_DURATION_ORDERING.max(multimap.entries());
    }

    private Duration calcTotalDuration(final Collection<Interval> intervals) {
        Duration duration = Duration.ZERO;
        for (Interval interval : intervals) {
            duration = duration.plus(interval.toDuration());
        }
        return duration;
    }

    public List<Entity> asEntities() {
        return timeGapEntities;
    }

    public Duration getTotalDuration() {
        return totalDuration;
    }

    public Interval getLongestInterval() {
        if (longestIntervalWithLineId == null) {
            return new Interval(DateTime.now(), Duration.ZERO);
        }
        return longestIntervalWithLineId.getValue();
    }

    public Long getLongestIntervalLineId() {
        if (longestIntervalWithLineId == null) {
            return null;
        }
        return longestIntervalWithLineId.getKey();
    }

    private static final Ordering<Entity> TIME_GAPS_ENTITIES_BY_DURATION_ORDERING = Ordering.from(new Comparator<Entity>() {

        @Override
        public int compare(final Entity e1, final Entity e2) {
            Integer d1 = e1.getIntegerField(TimeGapFields.DURATION);
            Integer d2 = e2.getIntegerField(TimeGapFields.DURATION);
            return d1.compareTo(d2);
        }
    });

    private static final Ordering<Map.Entry<Long, Interval>> ENTRY_BY_DURATION_ORDERING = Ordering
            .from(new Comparator<Map.Entry<Long, Interval>>() {

                @Override
                public int compare(final Map.Entry<Long, Interval> o1, final Map.Entry<Long, Interval> o2) {
                    return IntervalsComparator.BY_DURATION.compare(o1.getValue(), o2.getValue());
                }
            });

}
