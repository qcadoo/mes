package com.qcadoo.mes.basic.shift;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Pattern;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import com.google.common.collect.Sets;
import com.qcadoo.commons.dateTime.TimeRange;

public class WorkingHours implements Comparable<WorkingHours> {

    private static final Pattern WORKING_HOURS_PATTERN = Pattern
            .compile("\\d{1,2}:\\d{2}-\\d{1,2}:\\d{2}(,\\d{1,2}:\\d{2}-\\d{1,2}:\\d{2})*");

    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder().appendHourOfDay(1).appendLiteral(':')
            .appendMinuteOfHour(2).toFormatter();

    private final SortedSet<TimeRange> hours;

    public WorkingHours(final String hourRanges) {
        hours = Sets.newTreeSet(Collections.unmodifiableSet(parseIntervals(hourRanges)));
    }

    private Set<TimeRange> parseIntervals(final String hoursRanges) {
        if (StringUtils.isBlank(hoursRanges)) {
            return Collections.emptySet();
        }
        String trimmedHoursRanges = StringUtils.remove(hoursRanges, ' ');
        if (!WORKING_HOURS_PATTERN.matcher(trimmedHoursRanges).matches()) {
            throw new IllegalArgumentException(String.format("Invalid shift's work time definition format: %s", hoursRanges));
        }
        final Set<TimeRange> intervals = Sets.newHashSet();
        for (String hoursRange : StringUtils.split(trimmedHoursRanges, ',')) {
            TimeRange interval = stringToInterval(hoursRange);
            if (interval != null) {
                intervals.add(interval);
            }
        }
        return intervals;
    }

    private TimeRange stringToInterval(final String hoursRange) {
        String[] lowerUpperBound = StringUtils.split(hoursRange, '-');
        LocalTime lower = LocalTime.parse(lowerUpperBound[0], TIME_FORMATTER);
        LocalTime upper = LocalTime.parse(lowerUpperBound[1], TIME_FORMATTER);
        return new TimeRange(lower, upper);
    }

    public boolean isEmpty() {
        return hours.isEmpty();
    }

    public boolean contains(final LocalTime time) {
        return findRangeFor(time) != null;
    }

    public TimeRange findRangeFor(final LocalTime time) {
        for (TimeRange hoursRange : hours) {
            if (hoursRange.contains(time)) {
                return hoursRange;
            }
        }
        return null;
    }

    public Set<TimeRange> getTimeRanges() {
        return Collections.unmodifiableSet(hours);
    }

    @Override
    public int hashCode() {
        return hours.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof WorkingHours)) {
            return false;
        }
        WorkingHours other = (WorkingHours) obj;
        return ObjectUtils.equals(hours, other.hours);
    }

    @Override
    public int compareTo(final WorkingHours other) {
        if (isEmpty()) {
            return -1;
        }
        if (other.isEmpty()) {
            return 1;
        }
        return hours.first().compareTo(other.hours.first());
    }
}