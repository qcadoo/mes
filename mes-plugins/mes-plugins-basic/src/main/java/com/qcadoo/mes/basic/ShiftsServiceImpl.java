/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
package com.qcadoo.mes.basic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ShiftsServiceImpl implements ShiftsService {

    private static final String TYPE_FIELD = "type";

    private static final String TIMETABLE_EXCEPTIONS_FIELD = "timetableExceptions";

    private static final String HOURS_LITERAL = "Hours";

    private static final String WORKING_LITERAL = "Working";

    private static final String TO_DATE_FIELD = "toDate";

    private static final String FROM_DATE_FIELD = "fromDate";

    @Autowired
    DataDefinitionService dataDefinitionService;

    private static final String[] WEEK_DAYS = { "monday", "tuesday", "wensday", "thursday", "friday", "saturday", "sunday" };

    public boolean validateShiftTimetableException(final DataDefinition dataDefinition, final Entity entity) {
        Date dateFrom = (Date) entity.getField(FROM_DATE_FIELD);
        Date dateTo = (Date) entity.getField(TO_DATE_FIELD);
        if (dateFrom.compareTo(dateTo) > 0) {
            entity.addError(dataDefinition.getField(FROM_DATE_FIELD), "basic.validate.global.error.shiftTimetable.datesError");
            entity.addError(dataDefinition.getField(TO_DATE_FIELD), "basic.validate.global.error.shiftTimetable.datesError");
            return false;
        }
        return true;
    }

    public void onDayCheckboxChange(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        updateDayFieldsState(viewDefinitionState);
    }

    public void setHourFieldsState(final ViewDefinitionState viewDefinitionState) {
        updateDayFieldsState(viewDefinitionState);
    }

    public void updateDayFieldsState(final ViewDefinitionState viewDefinitionState) {
        for (String day : WEEK_DAYS) {
            updateDayFieldState(day, viewDefinitionState);
        }
    }

    public void updateDayFieldState(final String day, final ViewDefinitionState viewDefinitionState) {
        FieldComponent mondayWorking = (FieldComponent) viewDefinitionState.getComponentByReference(day + WORKING_LITERAL);
        FieldComponent mondayHours = (FieldComponent) viewDefinitionState.getComponentByReference(day + HOURS_LITERAL);
        if (mondayWorking.getFieldValue().equals("0")) {
            mondayHours.setEnabled(false);
            mondayHours.setRequired(false);
        } else {
            mondayHours.setEnabled(true);
            mondayHours.setRequired(true);
        }
    }

    public boolean validateShiftHoursField(final DataDefinition dataDefinition, final Entity entity) {
        boolean valid = true;
        for (String day : WEEK_DAYS) {
            if (!validateHourField(day, dataDefinition, entity)) {
                valid = false;
            }
        }
        return valid;
    }

    public boolean validateHourField(final String day, final DataDefinition dataDefinition, final Entity entity) {
        boolean isDayActive = (Boolean) entity.getField(day + WORKING_LITERAL);
        String fieldValue = entity.getStringField(day + HOURS_LITERAL);
        if (!isDayActive) {
            return true;
        }
        if (fieldValue == null || "".equals(fieldValue.trim())) {
            entity.addError(dataDefinition.getField(day + HOURS_LITERAL), "qcadooView.validate.field.error.missing");
            return false;
        }
        try {
            convertDayHoursToInt(fieldValue);
        } catch (IllegalStateException e) {
            entity.addError(dataDefinition.getField(day + HOURS_LITERAL),
                    "basic.validate.global.error.shift.hoursFieldWrongFormat");
            return false;
        }
        return true;
    }

    private static final long STEP = 604800000;

    private static final long MAX_TIMESTAMP = new DateTime(2100, 1, 1, 0, 0, 0, 0).toDate().getTime();

    private static final long MIN_TIMESTAMP = new DateTime(2000, 1, 1, 0, 0, 0, 0).toDate().getTime();

    @Override
    public Date findDateToForOrder(final Date dateFrom, final long seconds) {
        if (dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SHIFT).find().list()
                .getTotalNumberOfEntities() == 0) {
            return null;
        }
        long start = dateFrom.getTime();
        long remaining = seconds;
        while (remaining > 0) {
            List<ShiftsServiceImpl.ShiftHour> hours = getHoursForAllShifts(new Date(start), new Date(start + STEP));
            for (ShiftsServiceImpl.ShiftHour hour : hours) {
                long diff = (hour.getDateTo().getTime() - hour.getDateFrom().getTime()) / 1000;
                if (diff >= remaining) {
                    return new Date(hour.getDateFrom().getTime() + (remaining * 1000));
                } else {
                    remaining -= diff;
                }
            }
            start += STEP;
            if (start > MAX_TIMESTAMP) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Date findDateFromForOrder(final Date dateTo, final long seconds) {
        if (dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SHIFT).find().list()
                .getTotalNumberOfEntities() == 0) {
            return null;
        }
        long stop = dateTo.getTime();
        long remaining = seconds;
        while (remaining > 0) {
            List<ShiftsServiceImpl.ShiftHour> hours = getHoursForAllShifts(new Date(stop - STEP), new Date(stop));
            for (int i = hours.size() - 1; i >= 0; i--) {
                ShiftsServiceImpl.ShiftHour hour = hours.get(i);
                long diff = (hour.getDateTo().getTime() - hour.getDateFrom().getTime()) / 1000;
                if (diff >= remaining) {
                    return new Date(hour.getDateTo().getTime() - (remaining * 1000));
                } else {
                    remaining -= diff;
                }
            }
            stop -= STEP;
            if (stop < MIN_TIMESTAMP) {
                return null;
            }
        }
        return null;
    }

    @Override
    public List<ShiftHour> getHoursForAllShifts(final Date dateFrom, final Date dateTo) {
        List<Entity> shifts = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SHIFT).find()
                .list().getEntities();

        List<ShiftHour> hours = new ArrayList<ShiftHour>();

        for (Entity shift : shifts) {
            hours.addAll(getHoursForShift(shift, dateFrom, dateTo));
        }

        Collections.sort(hours, new ShiftHoursComparator());

        return mergeOverlappedHours(hours);
    }

    @Override
    public List<ShiftHour> getHoursForShift(final Entity shift, final Date dateFrom, final Date dateTo) {
        List<ShiftHour> hours = new ArrayList<ShiftHour>();
        hours.addAll(getHourForDay(shift, dateFrom, dateTo, "monday", 1));
        hours.addAll(getHourForDay(shift, dateFrom, dateTo, "tuesday", 2));
        hours.addAll(getHourForDay(shift, dateFrom, dateTo, "wensday", 3));
        hours.addAll(getHourForDay(shift, dateFrom, dateTo, "thursday", 4));
        hours.addAll(getHourForDay(shift, dateFrom, dateTo, "friday", 5));
        hours.addAll(getHourForDay(shift, dateFrom, dateTo, "saturday", 6));
        hours.addAll(getHourForDay(shift, dateFrom, dateTo, "sunday", 7));

        List<Entity> exceptions = shift.getHasManyField(TIMETABLE_EXCEPTIONS_FIELD);

        addWorkTimeExceptions(hours, exceptions);
        removeFreeTimeExceptions(hours, exceptions);

        Collections.sort(hours, new ShiftHoursComparator());

        return removeHoursOutOfRange(mergeOverlappedHours(hours), dateFrom, dateTo);
    }

    public List<ShiftHour> removeHoursOutOfRange(final List<ShiftHour> hours, final Date dateFrom, final Date dateTo) {
        List<ShiftHour> list = new ArrayList<ShiftHour>();

        for (ShiftHour hour : hours) {
            if (hour.getDateTo().compareTo(dateFrom) <= 0 || hour.getDateFrom().compareTo(dateTo) >= 0) {
                continue;
            }
            if (hour.getDateFrom().compareTo(dateFrom) >= 0 && hour.getDateTo().compareTo(dateTo) <= 0) {
                list.add(hour);
            } else if (hour.getDateFrom().compareTo(dateFrom) < 0 && hour.getDateTo().compareTo(dateTo) > 0) {
                list.add(new ShiftHour(dateFrom, dateTo));
            } else if (hour.getDateFrom().compareTo(dateFrom) < 0 && hour.getDateTo().compareTo(dateTo) <= 0) {
                list.add(new ShiftHour(dateFrom, hour.getDateTo()));
            } else if (hour.getDateFrom().compareTo(dateFrom) >= 0 && hour.getDateTo().compareTo(dateTo) > 0) {
                list.add(new ShiftHour(hour.getDateFrom(), dateTo));
            }
        }
        return list;
    }

    public void removeFreeTimeExceptions(final List<ShiftHour> hours, final List<Entity> exceptions) {
        for (Entity exception : exceptions) {
            if (!"01freeTime".equals(exception.getStringField(TYPE_FIELD))) {
                continue;
            }

            Date from = (Date) exception.getField(FROM_DATE_FIELD);
            Date to = (Date) exception.getField(TO_DATE_FIELD);

            List<ShiftHour> hoursToRemove = new ArrayList<ShiftHour>();
            List<ShiftHour> hoursToAdd = new ArrayList<ShiftHour>();

            for (ShiftHour hour : hours) {
                if (hour.getDateFrom().after(to)) {
                    continue;
                }
                if (hour.getDateTo().before(from)) {
                    continue;
                }
                if (hour.getDateTo().before(to) && hour.getDateFrom().after(from)) {
                    hoursToRemove.add(hour);
                    continue;
                }
                if (hour.getDateTo().after(to) && hour.getDateFrom().after(from)) {
                    hoursToRemove.add(hour);
                    hoursToAdd.add(new ShiftHour(to, hour.getDateTo()));
                    continue;
                }
                if (hour.getDateTo().before(to) && hour.getDateFrom().before(from)) {
                    hoursToRemove.add(hour);
                    hoursToAdd.add(new ShiftHour(hour.getDateFrom(), from));
                    continue;
                }
                if (hour.getDateTo().after(to) && hour.getDateFrom().before(from)) {
                    hoursToRemove.add(hour);
                    hoursToAdd.add(new ShiftHour(hour.getDateFrom(), from));
                    hoursToAdd.add(new ShiftHour(to, hour.getDateTo()));
                    continue;
                }
            }

            hours.removeAll(hoursToRemove);
            hours.addAll(hoursToAdd);
        }
    }

    public void addWorkTimeExceptions(final List<ShiftHour> hours, final List<Entity> exceptions) {
        for (Entity exception : exceptions) {
            if (!"02workTime".equals(exception.getStringField(TYPE_FIELD))) {
                continue;
            }

            Date from = (Date) exception.getField(FROM_DATE_FIELD);
            Date to = (Date) exception.getField(TO_DATE_FIELD);

            hours.add(new ShiftHour(from, to));
        }
    }

    public List<ShiftHour> mergeOverlappedHours(final List<ShiftHour> hours) {
        if (hours.size() < 2) {
            return hours;
        }

        List<ShiftHour> mergedHours = new ArrayList<ShiftHour>();

        ShiftHour currentHour = hours.get(0);

        for (int i = 1; i < hours.size(); i++) {
            if (currentHour.getDateTo().before(hours.get(i).getDateFrom())) {
                mergedHours.add(currentHour);
                currentHour = hours.get(i);
            } else if (currentHour.getDateTo().before(hours.get(i).getDateTo())) {
                currentHour = new ShiftHour(currentHour.getDateFrom(), hours.get(i).getDateTo());
            }
        }

        mergedHours.add(currentHour);

        return mergedHours;
    }

    public Collection<ShiftHour> getHourForDay(final Entity shift, final Date dateFrom, final Date dateTo, final String day,
            final int offset) {
        if ((Boolean) shift.getField(day + WORKING_LITERAL) && StringUtils.hasText(shift.getStringField(day + HOURS_LITERAL))) {
            List<ShiftHour> hours = new ArrayList<ShiftHour>();

            LocalTime[][] dayHours = convertDayHoursToInt(shift.getStringField(day + HOURS_LITERAL));

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

    @Override
    public LocalTime[][] convertDayHoursToInt(final String string) {
        String[] parts = string.trim().split(",");

        LocalTime[][] hours = new LocalTime[parts.length][];

        for (int i = 0; i < parts.length; i++) {
            hours[i] = convertRangeHoursToInt(parts[i]);
        }

        return hours;
    }

    public LocalTime[] convertRangeHoursToInt(final String string) {
        String[] parts = string.trim().split("-");

        if (parts.length != 2) {
            throw new IllegalStateException("Invalid time range " + string + ", should be hh:mm-hh:mm");
        }

        LocalTime[] range = new LocalTime[2];

        range[0] = convertHoursToInt(parts[0]);
        range[1] = convertHoursToInt(parts[1]);

        return range;
    }

    public LocalTime convertHoursToInt(final String string) {
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

    public static class ShiftHoursComparator implements Comparator<ShiftHour>, Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = -3204783429616635555L;

        @Override
        public int compare(final ShiftHour o1, final ShiftHour o2) {
            int i = o1.getDateFrom().compareTo(o2.getDateFrom());

            if (i == 0) {
                return o1.getDateTo().compareTo(o2.getDateTo());
            } else {
                return i;
            }
        }

    }

    public static class ShiftHour {

        private final Date dateTo;

        private final Date dateFrom;

        public ShiftHour(final Date dateFrom, final Date dateTo) {
            this.dateFrom = new Date(dateFrom.getTime());
            if (dateFrom.after(dateTo)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(dateTo);
                cal.add(Calendar.DATE, 1);
                this.dateTo = cal.getTime();
            } else {
                this.dateTo = new Date(dateTo.getTime());
            }
        }

        public Date getDateTo() {
            return new Date(dateTo.getTime());
        }

        public Date getDateFrom() {
            return new Date(dateFrom.getTime());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((dateFrom == null) ? 0 : dateFrom.hashCode());
            result = prime * result + ((dateTo == null) ? 0 : dateTo.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof ShiftHour)) {
                return false;
            }
            ShiftHour other = (ShiftHour) obj;
            if (dateFrom == null) {
                if (other.dateFrom != null) {
                    return false;
                }
            } else if (!dateFrom.equals(other.dateFrom)) {
                return false;
            }
            if (dateTo == null) {
                if (other.dateTo != null) {
                    return false;
                }
            } else if (!dateTo.equals(other.dateTo)) {
                return false;
            }
            return true;
        }

    }

    @Override
    public Entity getShiftFromDate(final Date date) {
        Map<Integer, String> dayOfWeek = new HashMap<Integer, String>();
        dayOfWeek.put(Calendar.MONDAY, "monday");
        dayOfWeek.put(Calendar.TUESDAY, "tuesday");
        dayOfWeek.put(Calendar.WEDNESDAY, "wensday");
        dayOfWeek.put(Calendar.THURSDAY, "thursday");
        dayOfWeek.put(Calendar.FRIDAY, "friday");
        dayOfWeek.put(Calendar.SATURDAY, "saturday");
        dayOfWeek.put(Calendar.SUNDAY, "sunday");

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        SearchCriteriaBuilder searchCriteriaBuilder = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                BasicConstants.MODEL_SHIFT).find();
        searchCriteriaBuilder.add(SearchRestrictions.eq(dayOfWeek.get(day) + WORKING_LITERAL, true));
        List<Entity> shifts = searchCriteriaBuilder.list().getEntities();

        int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        int minuteOfHour = cal.get(Calendar.MINUTE);

        for (Entity shift : shifts) {
            String stringHours = shift.getStringField(dayOfWeek.get(day) + HOURS_LITERAL);
            LocalTime[][] dayHours = convertDayHoursToInt(stringHours);
            for (LocalTime[] dayHour : dayHours) {
                if ((dayHour[0].getHourOfDay() < hourOfDay || (dayHour[0].getHourOfDay() == hourOfDay && dayHour[0]
                        .getMinuteOfHour() <= minuteOfHour))
                        && (hourOfDay < dayHour[1].getHourOfDay() || (hourOfDay == dayHour[1].getHourOfDay() && minuteOfHour < dayHour[1]
                                .getMinuteOfHour()))) {
                    return shift;
                }
            }
        }
        return null;
    }
}
