/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
