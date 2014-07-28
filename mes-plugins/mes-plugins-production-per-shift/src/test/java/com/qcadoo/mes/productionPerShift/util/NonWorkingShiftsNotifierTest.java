package com.qcadoo.mes.productionPerShift.util;

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.EntityTestUtils.stubBelongsToField;
import static com.qcadoo.testing.model.EntityTestUtils.stubBooleanField;
import static com.qcadoo.testing.model.EntityTestUtils.stubDateField;
import static com.qcadoo.testing.model.EntityTestUtils.stubHasManyField;
import static com.qcadoo.testing.model.EntityTestUtils.stubIntegerField;
import static com.qcadoo.testing.model.EntityTestUtils.stubStringField;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.ShiftFields;
import com.qcadoo.mes.productionPerShift.PpsDetailsViewAwareTest;
import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressType;
import com.qcadoo.mes.productionPerShift.dataProvider.ProgressForDayDataProvider;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.testing.model.EntityListMock;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.components.FormComponent;

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

        stubViewComponent(FORM_REF, form);
    }

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
    }

}
