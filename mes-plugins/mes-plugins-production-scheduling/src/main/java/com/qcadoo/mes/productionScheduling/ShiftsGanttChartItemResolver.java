package com.qcadoo.mes.productionScheduling;

import java.util.Date;
import java.util.List;

import org.joda.time.LocalTime;

import com.qcadoo.mes.productionScheduling.ShiftsGanttChartItemResolverImpl.ShiftHour;
import com.qcadoo.view.api.components.ganttChart.GanttChartItemResolver;

public interface ShiftsGanttChartItemResolver extends GanttChartItemResolver {

    List<ShiftHour> getHoursForAllShifts(final Date dateFrom, final Date dateTo);

    LocalTime[][] convertDayHoursToInt(final String string);

}