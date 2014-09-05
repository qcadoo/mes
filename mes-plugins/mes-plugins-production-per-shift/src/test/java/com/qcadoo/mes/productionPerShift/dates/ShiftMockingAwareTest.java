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
package com.qcadoo.mes.productionPerShift.dates;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;

import java.util.Date;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Optional;
import com.qcadoo.commons.dateTime.DateRange;
import com.qcadoo.commons.dateTime.TimeRange;
import com.qcadoo.mes.basic.shift.Shift;

public abstract class ShiftMockingAwareTest {

    protected Shift mockShift(final TimeRange workingTime, final Set<Integer> workingWeekDays) {
        Shift shift = mock(Shift.class);
        stubWorkingWeekDays(shift, workingWeekDays);
        stubWorkingHours(shift, workingTime, workingWeekDays);
        return shift;
    }

    private void stubWorkingWeekDays(final Shift shiftMock, final Set<Integer> workingWeekDays) {
        for (int weekDayNum = 1; weekDayNum <= 7; weekDayNum++) {
            given(shiftMock.worksAt(weekDayNum)).willReturn(workingWeekDays.contains(weekDayNum));
        }
    }

    private void stubWorkingHours(final Shift shiftMock, final TimeRange workingTime, final Set<Integer> workingWeekDays) {
        given(shiftMock.worksAt(anyInt(), any(LocalTime.class))).willAnswer(new Answer<Boolean>() {

            @Override
            public Boolean answer(final InvocationOnMock invocation) throws Throwable {
                Integer weekDayNum = (Integer) invocation.getArguments()[0];
                LocalTime time = (LocalTime) invocation.getArguments()[1];
                return workingTime.contains(time) && workingWeekDays.contains(weekDayNum);
            }
        });

        given(shiftMock.worksAt(any(Date.class))).willAnswer(new Answer<Boolean>() {

            @Override
            public Boolean answer(final InvocationOnMock invocation) throws Throwable {
                Date date = (Date) invocation.getArguments()[0];
                LocalTime time = LocalTime.fromDateFields(date);
                int weekDayNum = LocalDate.fromDateFields(date).getDayOfWeek();
                return workingTime.contains(time) && workingWeekDays.contains(weekDayNum);
            }
        });
        given(shiftMock.findWorkTimeAt(anyInt(), any(LocalTime.class))).willAnswer(new Answer<Optional<TimeRange>>() {

            @Override
            public Optional<TimeRange> answer(final InvocationOnMock invocation) throws Throwable {
                Integer weekDayNum = (Integer) invocation.getArguments()[0];
                LocalTime time = (LocalTime) invocation.getArguments()[1];
                if (workingTime.contains(time) && workingWeekDays.contains(weekDayNum)) {
                    return Optional.of(workingTime);
                }
                return Optional.absent();
            }
        });
        given(shiftMock.findWorkTimeAt(any(Date.class))).willAnswer(new Answer<Optional<DateRange>>() {

            @Override
            public Optional<DateRange> answer(final InvocationOnMock invocation) throws Throwable {
                Date date = (Date) invocation.getArguments()[0];
                LocalTime time = LocalTime.fromDateFields(date);
                int weekDayNum = LocalDate.fromDateFields(date).getDayOfWeek();
                if (workingTime.contains(time) && workingWeekDays.contains(weekDayNum)) {
                    LocalDate startDate = LocalDate.fromDateFields(date);
                    if (workingTime.startsDayBefore()) {
                        startDate = startDate.minusDays(1);
                    }
                    LocalDate endDate = LocalDate.fromDateFields(date);
                    DateTime startDateTime = startDate.toDateTime(workingTime.getFrom());
                    DateTime endDateTime = endDate.toDateTime(workingTime.getTo());
                    return Optional.of(new DateRange(startDateTime.toDate(), endDateTime.toDate()));
                }
                return Optional.absent();
            }
        });
    }

}
