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
package com.qcadoo.mes.timeGapsPreview.provider;

import static com.qcadoo.mes.basic.ShiftsServiceImpl.ShiftHour;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.timeGapsPreview.TimeGapsContext;
import com.qcadoo.mes.timeGapsPreview.provider.helper.ShiftIntervalsModelHelper;
import com.qcadoo.mes.timeGapsPreview.util.TimeGapsBuilder;
import com.qcadoo.mes.timeGapsPreview.util.TimeGapsBuilderImpl;

@Service
public class ShiftIntervalsProvider implements IntervalsProvider {

    @Autowired
    private ShiftIntervalsModelHelper shiftIntervalsModelHelper;

    @Autowired
    private ShiftsService shiftsService;

    @Override
    public Multimap<Long, Interval> getIntervalsPerProductionLine(final TimeGapsContext context) {
        Set<Interval> workIntervals = getWorkIntervals(context);
        Collection<Interval> spareTimeIntervals = invertIntervals(context.getInterval(), workIntervals);
        Multimap<Long, Interval> spareTimeMultiMap = HashMultimap.create();
        for (Long productionLne : context.getProductionLines()) {
            spareTimeMultiMap.putAll(productionLne, spareTimeIntervals);
        }
        return spareTimeMultiMap;
    }

    private Collection<Interval> invertIntervals(final Interval domainInterval, final Iterable<Interval> intervals) {
        TimeGapsBuilder gapsBuilder = new TimeGapsBuilderImpl(domainInterval);
        gapsBuilder.addOccupiedIntervals(intervals);
        return gapsBuilder.calculateGaps();
    }

    private Set<Interval> getWorkIntervals(final TimeGapsContext context) {
        Interval searchInterval = context.getInterval();
        Date fromDate = searchInterval.getStart().toDate();
        Date toDate = searchInterval.getEnd().toDate();
        List<ShiftHour> shiftHours = shiftsService.getHoursForAllShifts(fromDate, toDate);
        Set<Interval> shiftWorkTimeIntervals = Sets.newHashSet();
        for (ShiftHour shiftHour : shiftHours) {
            Interval shiftWorkTimeInterval = new Interval(shiftHour.getDateFrom().getTime(), shiftHour.getDateTo().getTime());
            shiftWorkTimeIntervals.add(shiftWorkTimeInterval);
        }
        return shiftWorkTimeIntervals;
    }

}
