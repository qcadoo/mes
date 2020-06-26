/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.ganttForShifts;

import com.qcadoo.mes.basic.ShiftsServiceImpl;
import com.qcadoo.mes.basic.constants.ShiftFields;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.basic.util.DateTimeRange;
import com.qcadoo.view.api.components.ganttChart.GanttChartItem;
import com.qcadoo.view.api.components.ganttChart.GanttChartScale;
import org.joda.time.LocalTime;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ShiftsGanttChartItemResolverImpl implements ShiftsGanttChartItemResolver {

    @Autowired
    private ShiftsServiceImpl shiftsService;

    @Override
    @Transactional
    public Map<String, List<GanttChartItem>> resolve(final GanttChartScale scale, final JSONObject context, final Locale locale) {
        List<Shift> shifts = shiftsService.findAll();
        Map<String, List<GanttChartItem>> items = new LinkedHashMap<>();

        for (Shift shift : shifts) {
            items.put(shift.getEntity().getStringField(ShiftFields.NAME), getItemsForShift(shift, scale));
        }

        return items;
    }

    private List<GanttChartItem> getItemsForShift(final Shift shift, final GanttChartScale scale) {
        String shiftName = shift.getEntity().getStringField(ShiftFields.NAME);

        List<DateTimeRange> timeRanges = shiftsService.getDateTimeRanges(Collections.singletonList(shift), scale.getDateFrom(),
                scale.getDateTo());

        List<GanttChartItem> items = new ArrayList<>();

        for (DateTimeRange timeRange : timeRanges) {
            items.add(scale.createGanttChartItem(shiftName, shiftName, null, timeRange.getFrom().toDate(),
                    timeRange.getTo().toDate()));
        }

        return items;
    }

    public LocalTime[][] convertDayHoursToInt(final String string) {
        return shiftsService.convertDayHoursToInt(string);
    }

}
