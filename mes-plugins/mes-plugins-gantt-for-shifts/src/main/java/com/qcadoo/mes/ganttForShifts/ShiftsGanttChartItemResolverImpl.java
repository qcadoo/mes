/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.LocalTime;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.basic.ShiftsServiceImpl;
import com.qcadoo.mes.basic.ShiftsServiceImpl.ShiftHour;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.components.ganttChart.GanttChartItem;
import com.qcadoo.view.api.components.ganttChart.GanttChartScale;

@Service
public class ShiftsGanttChartItemResolverImpl implements ShiftsGanttChartItemResolver {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    ShiftsServiceImpl shiftsService;

    @Override
    @Transactional
    public Map<String, List<GanttChartItem>> resolve(final GanttChartScale scale, final JSONObject context, final Locale locale) {
        List<Entity> shifts = dataDefinitionService.get("basic", "shift").find().list().getEntities();
        Map<String, List<GanttChartItem>> items = new LinkedHashMap<String, List<GanttChartItem>>();

        for (Entity shift : shifts) {
            items.put(shift.getStringField("name"), getItemsForShift(shift, scale));
        }

        return items;
    }

    @Override
    public List<GanttChartItem> getItemsForShift(final Entity shift, final GanttChartScale scale) {
        String shiftName = shift.getStringField("name");

        List<ShiftHour> hours = shiftsService.getHoursForShift(shift, scale.getDateFrom(), scale.getDateTo());

        List<GanttChartItem> items = new ArrayList<GanttChartItem>();

        for (ShiftHour hour : hours) {
            items.add(scale.createGanttChartItem(shiftName, shiftName, null, hour.getDateFrom(), hour.getDateTo()));
        }

        return items;
    }

    public List<ShiftsServiceImpl.ShiftHour> getHoursForAllShifts(final Date dateFrom, final Date dateTo) {
        return shiftsService.getHoursForAllShifts(dateFrom, dateTo);
    }

    public LocalTime[][] convertDayHoursToInt(final String string) {
        return shiftsService.convertDayHoursToInt(string);
    }

}
