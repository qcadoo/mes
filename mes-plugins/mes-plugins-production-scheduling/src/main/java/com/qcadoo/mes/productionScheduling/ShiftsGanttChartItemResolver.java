package com.qcadoo.mes.productionScheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.components.ganttChart.GanttChartItem;
import com.qcadoo.view.api.components.ganttChart.GanttChartItemResolver;
import com.qcadoo.view.api.components.ganttChart.GanttChartScale;

// TODO masz - why we can't get this bean by class name?, see GanttChartComponentPattern
@Service("shiftsGanttChartItemResolver")
public class ShiftsGanttChartItemResolver implements GanttChartItemResolver {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    @Transactional
    public Map<String, List<GanttChartItem>> resolve(final GanttChartScale scale) {
        List<Entity> shifts = dataDefinitionService.get("productionScheduling", "shift").find().list().getEntities();
        Map<String, List<GanttChartItem>> items = new LinkedHashMap<String, List<GanttChartItem>>();

        for (Entity shift : shifts) {
            items.put(shift.getStringField("name"), getItemsForShift(shift, scale));
        }

        return items;
    }

    private List<GanttChartItem> getItemsForShift(final Entity shift, final GanttChartScale scale) {
        String shiftName = shift.getStringField("name");

        List<ShiftHour> hours = getHoursForShift(shift, scale.getDateFrom(), scale.getDateTo());

        List<GanttChartItem> items = new ArrayList<GanttChartItem>();

        for (ShiftHour hour : hours) {
            items.add(scale.createGanttChartItem(shiftName, shiftName, hour.getDateFrom(), hour.getDateTo()));
        }

        return items;
    }

    private List<ShiftHour> getHoursForShift(final Entity shift, final Date dateFrom, final Date dateTo) {
        List<ShiftHour> hours = new ArrayList<ShiftHour>();
        hours.addAll(getHourForDay(shift, dateFrom, dateTo, "monday", 1));
        hours.addAll(getHourForDay(shift, dateFrom, dateTo, "tuesday", 2));
        hours.addAll(getHourForDay(shift, dateFrom, dateTo, "wensday", 3));
        hours.addAll(getHourForDay(shift, dateFrom, dateTo, "thursday", 4));
        hours.addAll(getHourForDay(shift, dateFrom, dateTo, "friday", 5));
        hours.addAll(getHourForDay(shift, dateFrom, dateTo, "saturday", 6));
        hours.addAll(getHourForDay(shift, dateFrom, dateTo, "sunday", 7));

        Collections.sort(hours, new ShiftHoursComparator());

        // exceptions

        return hours;
    }

    private Collection<ShiftHour> getHourForDay(final Entity shift, final Date dateFrom, final Date dateTo, final String day,
            final int offset) {
        if ((Boolean) shift.getField(day + "Working") && StringUtils.hasText(shift.getStringField(day + "Hours"))) {
            List<ShiftHour> hours = new ArrayList<ShiftHour>();

            LocalTime[][] dayHours = convertDayHoursToInt(shift.getStringField(day + "Hours"));

            DateTime from = new DateTime(dateFrom).withSecondOfMinute(0);
            DateTime to = new DateTime(dateTo);

            DateTime current = from.plusDays(offset - from.getDayOfWeek());

            if (current.compareTo(from) < 0) {
                current = current.plusDays(7);
            }

            while (current.compareTo(to) <= 0) {
                for (LocalTime[] dayHour : dayHours) {
                    hours.add(new ShiftHour(current.withHourOfDay(dayHour[0].getHourOfDay())
                            .withMinuteOfHour(dayHour[0].getMinuteOfHour()).toDate(), current
                            .withHourOfDay(dayHour[1].getHourOfDay()).withMinuteOfHour(dayHour[1].getMinuteOfHour()).toDate()));
                }
                current = current.plusDays(7);
            }

            return hours;
        } else {
            return Collections.emptyList();
        }
    }

    public LocalTime[][] convertDayHoursToInt(final String string) {
        String[] parts = string.trim().split(",");

        LocalTime[][] hours = new LocalTime[parts.length][];

        for (int i = 0; i < parts.length; i++) {
            hours[i] = convertRangeHoursToInt(parts[i]);
        }

        return hours;
    }

    private LocalTime[] convertRangeHoursToInt(final String string) {
        String[] parts = string.trim().split("-");

        if (parts.length != 2) {
            throw new IllegalStateException("Invalid time range " + string + ", should be hh:mm-hh:mm");
        }

        LocalTime[] range = new LocalTime[2];

        range[0] = convertHoursToInt(parts[0]);
        range[1] = convertHoursToInt(parts[1]);

        if (range[0].compareTo(range[1]) >= 0) {
            throw new IllegalStateException("Invalid time range " + string + ", firts hour must be lower than the second one");
        }

        return range;
    }

    private LocalTime convertHoursToInt(final String string) {
        String[] parts = string.trim().split(":");

        if (parts.length != 2) {
            throw new IllegalStateException("Invalid time " + string + ", should be hh:mm");
        }

        try {
            return new LocalTime(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (IllegalFieldValueException e) {
            throw new IllegalStateException("Invalid time " + string, e);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid time " + string + ", should be hh:mm", e);
        }
    }

    private static class ShiftHoursComparator implements Comparator<ShiftHour> {

        @Override
        public int compare(final ShiftHour o1, final ShiftHour o2) {
            int i = o1.getDateFrom().compareTo(o2.getDateFrom());

            if (i != 0) {
                return i;
            } else {
                return o1.getDateTo().compareTo(o2.getDateTo());
            }
        }

    }

    private static class ShiftHour {

        private final Date dateTo;

        private final Date dateFrom;

        public ShiftHour(final Date dateFrom, final Date dateTo) {
            this.dateFrom = dateFrom;
            this.dateTo = dateTo;
        }

        public Date getDateTo() {
            return dateTo;
        }

        public Date getDateFrom() {
            return dateFrom;
        }

        @Override
        public String toString() {
            return "[" + dateFrom + " - " + dateTo + "]";
        }

    }

}
