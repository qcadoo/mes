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
package com.qcadoo.mes.productionPerShift.util;

import org.junit.Before;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.productionPerShift.PpsDetailsViewAwareTest;
import com.qcadoo.mes.productionPerShift.dataProvider.ProgressForDayDataProvider;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

public class NonWorkingShiftsNotifierTest extends PpsDetailsViewAwareTest {

    private NonWorkingShiftsNotifier nonWorkingShiftsNotifier;

    @Mock
    private ProgressForDayDataProvider progressForDayDataProvider;

    @Mock
    private FormComponent form;

    @Before
    public void init() {
        super.init();
        nonWorkingShiftsNotifier = new NonWorkingShiftsNotifier();

        ReflectionTestUtils.setField(nonWorkingShiftsNotifier, "progressForDayDataProvider", progressForDayDataProvider);

        stubViewComponent(QcadooViewConstants.L_FORM, form);
    }
/*
    private void stubProgressForDayFindResults(final Iterable<Entity> progresses) {
        given(progressForDayDataProvider.findForOperation(any(Entity.class), any(ProgressType.class))).willAnswer(
                new Answer<List<Entity>>() {

                    @Override
                    public List<Entity> answer(final InvocationOnMock invocation) throws Throwable {
                        return Lists.newLinkedList(progresses);
                    }
                });
    }

    private Entity mockProgressForDay(final LocalDate actualDateOfDay, final int realizationDayNumber,
            final Iterable<Entity> shifts) {
        Entity progressForDay = mockEntity();
        stubIntegerField(progressForDay, ProgressForDayFields.DAY, realizationDayNumber);
        stubDateField(progressForDay, ProgressForDayFields.ACTUAL_DATE_OF_DAY, actualDateOfDay.toDate());
        stubHasManyField(progressForDay, ProgressForDayFields.DAILY_PROGRESS, mockDailyProgresses(shifts));
        return progressForDay;
    }

    private EntityList mockDailyProgresses(final Iterable<Entity> shifts) {
        return EntityListMock.create(FluentIterable.from(shifts).transform(MOCK_DAILY_PROGRESS_WITH_SHIFT).toList());
    }

    private static final Function<Entity, Entity> MOCK_DAILY_PROGRESS_WITH_SHIFT = new Function<Entity, Entity>() {

        @Override
        public Entity apply(final Entity shift) {
            Entity dailyProgress = mockEntity();
            stubBelongsToField(dailyProgress, DailyProgressFields.SHIFT, shift);
            return dailyProgress;
        }
    };

    private Entity mockShiftEntity(final String name, final Set<Integer> workingWeekDayNumbers) {
        Entity shift = mockEntity();
        stubStringField(shift, ShiftFields.NAME, name);

        stubBooleanField(shift, ShiftFields.MONDAY_WORKING, workingWeekDayNumbers.contains(DateTimeConstants.MONDAY));
        stubBooleanField(shift, ShiftFields.TUESDAY_WORKING, workingWeekDayNumbers.contains(DateTimeConstants.TUESDAY));
        stubBooleanField(shift, ShiftFields.WENSDAY_WORKING, workingWeekDayNumbers.contains(DateTimeConstants.WEDNESDAY));
        stubBooleanField(shift, ShiftFields.THURSDAY_WORKING, workingWeekDayNumbers.contains(DateTimeConstants.THURSDAY));
        stubBooleanField(shift, ShiftFields.FRIDAY_WORKING, workingWeekDayNumbers.contains(DateTimeConstants.FRIDAY));
        stubBooleanField(shift, ShiftFields.SATURDAY_WORKING, workingWeekDayNumbers.contains(DateTimeConstants.SATURDAY));
        stubBooleanField(shift, ShiftFields.SUNDAY_WORKING, workingWeekDayNumbers.contains(DateTimeConstants.SUNDAY));

        stubHasManyField(shift, ShiftFields.TIMETABLE_EXCEPTIONS, EntityListMock.create());
        given(shift.copy()).willReturn(shift);
        return shift;
    }

    @Test
    public final void shouldNotify() {
        // given
        LocalDate mondayDate = new LocalDate(2014, 9, 1);
        LocalDate tuesdayDate = new LocalDate(2014, 9, 2);
        DateTime orderStartDateTime = mondayDate.toDateTime(new LocalTime(23, 0, 0));

        Entity shift1 = mockShiftEntity("firstShift", ImmutableSet.of(DateTimeConstants.MONDAY, DateTimeConstants.TUESDAY));
        Entity shift2 = mockShiftEntity("secondShift", ImmutableSet.of(DateTimeConstants.TUESDAY, DateTimeConstants.WEDNESDAY));

        stubStringField(shift1, ShiftFields.MONDAY_HOURS, "22:00-10:00");
        stubStringField(shift1, ShiftFields.TUESDAY_HOURS, "22:00-10:00");
        stubStringField(shift2, ShiftFields.TUESDAY_HOURS, "10:00-20:00");

        Entity pfd1 = mockProgressForDay(mondayDate, 1, ImmutableList.of(shift1, shift2));
        Entity pfd2 = mockProgressForDay(tuesdayDate, 2, ImmutableList.of(shift1, shift2));

        stubProgressForDayFindResults(ImmutableList.of(pfd1, pfd2));

        // when
        nonWorkingShiftsNotifier.checkAndNotify(view, orderStartDateTime, mockEntity(), ProgressType.CORRECTED);

        // then
        verify(form).addMessage("productionPerShift.progressForDay.shiftDoesNotWork", ComponentState.MessageType.INFO,
                "secondShift", DateUtils.toDateString(mondayDate.toDate()));
        verifyNoMoreInteractions(form);
    }

    @Test
    public final void shouldNotifyAboutZeroDay() {
        // given
        LocalDate mondayDate = new LocalDate(2014, 9, 1);
        LocalDate tuesdayDate = new LocalDate(2014, 9, 2);
        DateTime orderStartDateTime = tuesdayDate.toDateTime(new LocalTime(2, 0, 0));

        Entity shift1 = mockShiftEntity("firstShift", ImmutableSet.of(DateTimeConstants.MONDAY, DateTimeConstants.TUESDAY));
        Entity shift2 = mockShiftEntity("secondShift",
                ImmutableSet.of(DateTimeConstants.MONDAY, DateTimeConstants.TUESDAY, DateTimeConstants.WEDNESDAY));

        stubStringField(shift1, ShiftFields.MONDAY_HOURS, "22:00-10:00");
        stubStringField(shift1, ShiftFields.TUESDAY_HOURS, "22:00-10:00");
        stubStringField(shift2, ShiftFields.TUESDAY_HOURS, "10:00-20:00");

        Entity pfd1 = mockProgressForDay(mondayDate, 0, ImmutableList.of(shift1, shift2));
        Entity pfd2 = mockProgressForDay(tuesdayDate, 1, ImmutableList.of(shift1, shift2));

        stubProgressForDayFindResults(ImmutableList.of(pfd1, pfd2));

        // when
        nonWorkingShiftsNotifier.checkAndNotify(view, orderStartDateTime, mockEntity(), ProgressType.CORRECTED);

        // then
        verify(form).addMessage("productionPerShift.progressForDay.shiftDoesNotStartOrderAtZeroDay",
                ComponentState.MessageType.INFO, "secondShift", DateUtils.toDateString(mondayDate.toDate()));
        verifyNoMoreInteractions(form);
    }

    @Test
    public final void shouldNotNotify() {
        // given
        LocalDate mondayDate = new LocalDate(2014, 9, 1);
        LocalDate tuesdayDate = new LocalDate(2014, 9, 2);
        DateTime orderStartDateTime = mondayDate.toDateTime(new LocalTime(9, 0, 0));

        Entity shift1 = mockShiftEntity("firstShift", ImmutableSet.of(DateTimeConstants.MONDAY, DateTimeConstants.TUESDAY));
        Entity shift2 = mockShiftEntity("secondShift", ImmutableSet.of(DateTimeConstants.TUESDAY, DateTimeConstants.WEDNESDAY));

        stubStringField(shift1, ShiftFields.MONDAY_HOURS, "22:00-10:00");
        stubStringField(shift1, ShiftFields.TUESDAY_HOURS, "22:00-10:00");
        stubStringField(shift2, ShiftFields.TUESDAY_HOURS, "10:00-20:00");

        Entity pfd1 = mockProgressForDay(mondayDate, 1, ImmutableList.of(shift1));
        Entity pfd2 = mockProgressForDay(tuesdayDate, 2, ImmutableList.of(shift1, shift2));

        stubProgressForDayFindResults(ImmutableList.of(pfd1, pfd2));

        // when
        nonWorkingShiftsNotifier.checkAndNotify(view, orderStartDateTime, mockEntity(), ProgressType.CORRECTED);

        // then
        verify(form, never()).addMessage(eq("productionPerShift.progressForDay.shiftDoesNotWork"),
                any(ComponentState.MessageType.class), Matchers.<String> anyVararg());
        verifyNoMoreInteractions(form);
    }*/

}
