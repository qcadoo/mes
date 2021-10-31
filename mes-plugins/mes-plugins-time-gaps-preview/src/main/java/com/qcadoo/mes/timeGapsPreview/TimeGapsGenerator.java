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

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.qcadoo.mes.timeGapsPreview.constants.TimeGapsPreviewConstants;
import com.qcadoo.mes.timeGapsPreview.provider.OrderAndChangeoverIntervalsProvider;
import com.qcadoo.mes.timeGapsPreview.provider.ShiftIntervalsProvider;
import com.qcadoo.mes.timeGapsPreview.util.TimeGapsBuilder;
import com.qcadoo.mes.timeGapsPreview.util.TimeGapsBuilderImpl;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TimeGapsGenerator {

    @Autowired
    private OrderAndChangeoverIntervalsProvider orderAndChangeoverIntervalsProvider;

    @Autowired
    private ShiftIntervalsProvider shiftIntervalsProvider;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public TimeGapsSearchResult generate(final TimeGapsContext timeGapsContext) {
        Multimap<Long, Interval> occupiedIntervals = createMultiMapWithKeys(timeGapsContext.getProductionLines());
        occupiedIntervals.putAll(orderAndChangeoverIntervalsProvider.getIntervalsPerProductionLine(timeGapsContext));
        occupiedIntervals.putAll(shiftIntervalsProvider.getIntervalsPerProductionLine(timeGapsContext));

        Multimap<Long, Interval> timeGaps = HashMultimap.create();
        for (Map.Entry<Long, Collection<Interval>> lineIntervals : occupiedIntervals.asMap().entrySet()) {
            Collection<Interval> gapsForLine = invertOccupiedIntervals(timeGapsContext.getInterval(), lineIntervals.getValue());
            Collection<Interval> gapsForLineFilteredByDuration = Collections2.filter(gapsForLine, new Predicate<Interval>() {

                @Override
                public boolean apply(final Interval interval) {
                    if (interval == null) {
                        return false;
                    }
                    return interval.toDuration().compareTo(timeGapsContext.getDuration()) >= 0;
                }
            });
            timeGaps.putAll(lineIntervals.getKey(), gapsForLineFilteredByDuration);
        }
        return TimeGapsSearchResult.create(timeGaps, getTimeGapDataDef());
    }

    private Multimap<Long, Interval> createMultiMapWithKeys(final Set<Long> keys) {
        Multimap<Long, Interval> multiMap = HashMultimap.create();
        Interval emptyInterval = new Interval(new DateTime(0L), Duration.ZERO);
        for (Long key : keys) {
            multiMap.put(key, emptyInterval);
        }
        return multiMap;
    }

    private Collection<Interval> invertOccupiedIntervals(final Interval domainInterval, final Iterable<Interval> occupiedIntervals) {
        TimeGapsBuilder gapsBuilder = new TimeGapsBuilderImpl(domainInterval);
        gapsBuilder.addOccupiedIntervals(occupiedIntervals);
        return gapsBuilder.calculateGaps();
    }

    private DataDefinition getTimeGapDataDef() {
        return dataDefinitionService.get(TimeGapsPreviewConstants.PLUGIN_IDENTIFIER, TimeGapsPreviewConstants.MODEL_TIME_GAP);
    }

}
