package com.qcadoo.mes.basic.shift;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalTime;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.qcadoo.commons.dateTime.DateRange;
import com.qcadoo.commons.dateTime.TimeRange;
import com.qcadoo.mes.basic.constants.TimetableExceptionType;
import com.qcadoo.model.api.Entity;

/**
 * Shift with some common methods for checking its work time.
 * 
 * This class assumes that first day of week is Monday (as opposite to the Calendar constants, where first day of week is Sunday).
 * Be aware of that.
 * 
 * @author Marcin Kubala
 * @since 1.2.1
 */
public class Shift {

    private final Entity shift;

    private final Long shiftId;

    private final Multimap<Integer, WorkingHours> workingHoursPerDay;

    private final ShiftTimetableExceptions timetableExceptions;

    public Shift(final Entity shiftEntity) {
        Entity shiftEntityCopy = shiftEntity.copy();
        this.shift = shiftEntityCopy;
        this.shiftId = shiftEntityCopy.getId();
        this.workingHoursPerDay = getWorkingHoursPerDay(shiftEntityCopy);
        this.timetableExceptions = new ShiftTimetableExceptions(shiftEntityCopy);
    }

    private Multimap<Integer, WorkingHours> getWorkingHoursPerDay(final Entity shiftEntity) {
        ImmutableSetMultimap.Builder<Integer, WorkingHours> builder = ImmutableSetMultimap.builder();
        for (Entry<Integer, String> dayNumToName : Constants.DAYS_OF_WEEK.entrySet()) {
            String hoursRanges = shiftEntity.getStringField(dayNumToName.getValue() + "Hours");
            WorkingHours workingHoursForGivenDay = new WorkingHours(hoursRanges);
            builder.put(dayNumToName.getKey(), workingHoursForGivenDay);
        }
        return builder.build();
    }

    /**
     * Check if this shift works at given day of week and time. This method is NOT aware of timetable exceptions.
     * 
     * @param dayOfWeek
     *            1 == MONDAY !
     * @param time
     * @return true if this shift works at given day of week and time.
     */
    public boolean worksAt(final int dayOfWeek, final LocalTime time) {
        return findWorkTimeAt(dayOfWeek, time) != null;
    }

    /**
     * Check if this shift works at given day of week. This method is NOT aware of timetable exceptions.
     * 
     * @param dayOfWeek
     *            1 == MONDAY !
     * @return true if this shift works at given day of week.
     */
    public boolean worksAt(final int dayOfWeek) {
        Preconditions.checkArgument(dayOfWeek > 0 && dayOfWeek <= 7, "Given day of week have to be > 0 and <= 7.");
        return shift.getBooleanField(Constants.DAYS_OF_WEEK.get(dayOfWeek) + "Working");
    }

    /**
     * Check if this shift will be working at given date and time. This method is aware of timetable exceptions
     * 
     * @param date
     * @return true if this shift will be working at given date and time.
     */
    public boolean worksAt(final Date date) {
        DateTime dateTime = new DateTime(date);
        return (worksAt(dateTime.getDayOfWeek(), dateTime.toLocalTime()) && !timetableExceptions.hasFreeTimeAt(date))
                || timetableExceptions.hasWorkTimeAt(date);
    }

    /**
     * Returns date range containing given date or null. This method IS AWARE of timetable exceptions.
     * 
     * <b>Be aware</b> - this method doesn't compose returned date range with the timetable exclusions/inclusions. This means that
     * if you have a shift which is working at Monday from 8:00-16:00 and there is defined work time exclusion from 12:00-20:00
     * and you ask for 10:00 then you will get date range from 8:00-16:00 (as in plan). But if you ask for 14:00 you will get
     * null.
     * 
     * @param date
     * @return
     */
    public DateRange findWorkTimeAt(final Date date) {
        DateTime dateTime = new DateTime(date);
        TimeRange timeRangeFromPlan = findWorkTimeAt(dateTime.getDayOfWeek(), dateTime.toLocalTime());

        if (timeRangeFromPlan == null) {
            DateRange additionalWorkTime = timetableExceptions.findDateRangeFor(TimetableExceptionType.WORK_TIME, date);
            return additionalWorkTime;
        } else {
            DateRange additionalFreeTime = timetableExceptions.findDateRangeFor(TimetableExceptionType.FREE_TIME, date);
            if (additionalFreeTime == null) {
                return buildDateRangeFrom(timeRangeFromPlan, date);
            }
            return null;
        }
    }

    private DateRange buildDateRangeFrom(final TimeRange timeRange, final Date date) {
        DateTime dateTime = new DateTime(date);
        DateTime midnight = dateTime.withTimeAtStartOfDay();
        DateTime from;
        DateTime to;
        if (timeRange.startsDayBefore()) {
            if (dateTime.toLocalTime().isBefore(timeRange.getFrom())) {
                from = timeRange.getFrom().toDateTime(midnight.minusDays(1));
                to = timeRange.getTo().toDateTime(midnight);
            } else {
                from = timeRange.getFrom().toDateTime(midnight);
                to = timeRange.getTo().toDateTime(midnight.plusDays(1));
            }
        } else {
            from = timeRange.getFrom().toDateTime(midnight);
            to = timeRange.getTo().toDateTime(midnight);
        }
        return new DateRange(from.toDate(), to.toDate());
    }

    /**
     * Returns date range containing given date or null. This method IS NOT AWARE of timetable exceptions.
     * 
     * @param dayOfWeek
     *            1 == MONDAY !
     * @param time
     * @return
     */
    public TimeRange findWorkTimeAt(final int dayOfWeek, final LocalTime time) {
        for (WorkingHours workingHours : workingHoursPerDay.get(dayOfWeek)) {
            TimeRange timeRange = workingHours.findRangeFor(time);
            if (timeRange != null) {
                return timeRange;
            }
        }
        // TODO MAKU maybe it should returns empty TimeRange instead of null?
        return null;
    }

    /**
     * Returns copy of the underlying entity.
     * 
     * @return copy of the underlying entity.
     */
    public Entity getEntity() {
        return shift.copy();
    }

    /**
     * Returns shift's identifier
     * 
     * @return shift's identifier
     */
    public Long getId() {
        return shiftId;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(shiftId).append(timetableExceptions).append(workingHoursPerDay).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof Shift)) {
            return false;
        }
        Shift other = (Shift) obj;
        return new EqualsBuilder().append(shiftId, other.shiftId).append(timetableExceptions, other.timetableExceptions)
                .append(workingHoursPerDay, other.workingHoursPerDay).isEquals();
    }

    private static final class Constants {

        public static final String SUNDAY_LITERAL = "sunday";

        public static final String SATURDAY_LITERAL = "saturday";

        public static final String FRIDAY_LITERAL = "friday";

        public static final String THURSDAY_LITERAL = "thursday";

        public static final String WENSDAY_LITERAL = "wensday";

        public static final String TUESDAY_LITERAL = "tuesday";

        public static final String MONDAY_LITERAL = "monday";

        public static final Map<Integer, String> DAYS_OF_WEEK = buildDayNumToNameMap();

        private static Map<Integer, String> buildDayNumToNameMap() {
            Map<Integer, String> dayNumsToDayName = Maps.newHashMapWithExpectedSize(7);
            dayNumsToDayName.put(DateTimeConstants.MONDAY, MONDAY_LITERAL);
            dayNumsToDayName.put(DateTimeConstants.TUESDAY, TUESDAY_LITERAL);
            dayNumsToDayName.put(DateTimeConstants.WEDNESDAY, WENSDAY_LITERAL);
            dayNumsToDayName.put(DateTimeConstants.THURSDAY, THURSDAY_LITERAL);
            dayNumsToDayName.put(DateTimeConstants.FRIDAY, FRIDAY_LITERAL);
            dayNumsToDayName.put(DateTimeConstants.SATURDAY, SATURDAY_LITERAL);
            dayNumsToDayName.put(DateTimeConstants.SUNDAY, SUNDAY_LITERAL);
            return Collections.unmodifiableMap(dayNumsToDayName);
        }

        private Constants() {
        };

    }

}
