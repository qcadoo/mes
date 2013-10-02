package com.qcadoo.mes.basic.shift;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalTime;
import org.junit.Test;

import com.qcadoo.commons.dateTime.TimeRange;

public class WorkingHoursTest {

    @Test
    public final void shouldBuildWorkingHoursFromSingleTimeRangeWithTwoDigitsHour() {
        // given
        String timeRangeString = "09:00-17:00";

        // when
        WorkingHours workingHours = new WorkingHours(timeRangeString);

        // then
        assertEquals(1, workingHours.getTimeRanges().size());
        TimeRange timeRange = workingHours.getTimeRanges().iterator().next();
        assertEquals(new LocalTime(9, 0, 0), timeRange.getFrom());
        assertEquals(new LocalTime(17, 0, 0), timeRange.getTo());
    }

    @Test
    public final void shouldBuildWorkingHoursFromSingleTimeRangeWithOneDigitsHour() {
        // given
        String timeRangeString = "9:00-17:00";

        // when
        WorkingHours workingHours = new WorkingHours(timeRangeString);

        // then
        assertEquals(1, workingHours.getTimeRanges().size());
        TimeRange timeRange = workingHours.getTimeRanges().iterator().next();
        assertEquals(new LocalTime(9, 0, 0), timeRange.getFrom());
        assertEquals(new LocalTime(17, 0, 0), timeRange.getTo());
    }

}
