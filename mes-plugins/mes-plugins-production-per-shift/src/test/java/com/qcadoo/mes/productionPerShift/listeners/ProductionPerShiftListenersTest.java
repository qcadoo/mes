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
package com.qcadoo.mes.productionPerShift.listeners;

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.EntityTestUtils.stubBelongsToField;
import static com.qcadoo.testing.model.EntityTestUtils.stubDateField;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.commons.functional.LazyStream;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.basic.shift.ShiftsDataProvider;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.PPSHelper;
import com.qcadoo.mes.productionPerShift.PpsDetailsViewAwareTest;
import com.qcadoo.mes.productionPerShift.constants.DailyProgressFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressType;
import com.qcadoo.mes.productionPerShift.dataProvider.ProgressForDayDataProvider;
import com.qcadoo.mes.productionPerShift.dates.OrderRealizationDay;
import com.qcadoo.mes.productionPerShift.dates.OrderRealizationDaysResolver;
import com.qcadoo.mes.productionPerShift.hooks.ProductionPerShiftDetailsHooks;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

public class ProductionPerShiftListenersTest extends PpsDetailsViewAwareTest {

    private static final DateTime PLANNED_ORDER_START = new DateTime(2014, 8, 18, 12, 0, 0);

    private static final DateTime CORRECTED_ORDER_START = PLANNED_ORDER_START.plusDays(2);

    private ProductionPerShiftListeners productionPerShiftListeners;

    @Mock
    private AwesomeDynamicListComponent progressForDaysAdl;

    @Mock
    private OrderRealizationDaysResolver orderRealizationDaysResolver;

    @Mock
    private ShiftsDataProvider shiftsDataProvider;

    @Mock
    private ProgressForDayDataProvider progressForDayDataProvider;

    @Mock
    private Entity firstShiftEntity, secondShiftEntity, firstShiftDailyProgress, secondShiftDailyProgress;

    @Mock
    private PPSHelper ppsHelper;

    @Mock
    private ProductionPerShiftDetailsHooks detailsHooks;

    @Captor
    private ArgumentCaptor<Iterable<Long>> iterableOfLongsCaptor;

    private Shift firstShift, secondShift;

    private List<Shift> shifts;

    @Before
    public void init() {
        super.init();

        stubOrderStartDates(PLANNED_ORDER_START, CORRECTED_ORDER_START, PLANNED_ORDER_START.plusDays(4));

        stubViewComponent(PROGRESS_FOR_DAYS_ADL_REF, progressForDaysAdl);

        firstShift = mockShift(firstShiftEntity);
        secondShift = mockShift(secondShiftEntity);
        shifts = ImmutableList.of(firstShift, secondShift);
        given(shiftsDataProvider.findAll()).willReturn(shifts);
        stubBelongsToField(firstShiftDailyProgress, DailyProgressFields.SHIFT, firstShiftEntity);
        stubBelongsToField(secondShiftDailyProgress, DailyProgressFields.SHIFT, secondShiftEntity);

        given(ppsHelper.createDailyProgressWithShift(any(Entity.class))).willAnswer(new Answer<Entity>() {

            @Override
            public Entity answer(final InvocationOnMock invocation) throws Throwable {
                Entity shiftEntity = (Entity) invocation.getArguments()[0];
                if (shiftEntity == firstShiftEntity) {
                    return firstShiftDailyProgress;
                } else if (shiftEntity == secondShiftEntity) {
                    return secondShiftDailyProgress;
                }
                Entity dailyProgressEntity = mockEntity();
                stubBelongsToField(dailyProgressEntity, DailyProgressFields.SHIFT, shiftEntity);
                return dailyProgressEntity;
            }
        });

        productionPerShiftListeners = new ProductionPerShiftListeners();
        ReflectionTestUtils.setField(productionPerShiftListeners, "orderRealizationDaysResolver", orderRealizationDaysResolver);
        ReflectionTestUtils.setField(productionPerShiftListeners, "shiftsDataProvider", shiftsDataProvider);
        ReflectionTestUtils.setField(productionPerShiftListeners, "ppsHelper", ppsHelper);
        ReflectionTestUtils.setField(productionPerShiftListeners, "detailsHooks", detailsHooks);
        ReflectionTestUtils.setField(productionPerShiftListeners, "progressForDayDataProvider", progressForDayDataProvider);
    }

    @Override
    protected void stubProgressType(final ProgressType progressType) {
        given(detailsHooks.resolveProgressType(any(ViewDefinitionState.class))).willReturn(progressType);
    }

    private Shift mockShift(final Entity shiftEntity) {
        Shift shift = mock(Shift.class);
        given(shift.getEntity()).willReturn(shiftEntity);
        return shift;
    }

    private void stubOrderStartDates(final DateTime planned, final DateTime corrected, final DateTime effective) {
        stubOrderDate(OrderFields.DATE_FROM, planned);
        stubOrderDate(OrderFields.CORRECTED_DATE_FROM, corrected);
        stubOrderDate(OrderFields.EFFECTIVE_DATE_FROM, effective);
        stubOrderDate(OrderFields.DATE_TO,
                Optional.fromNullable(effective).or(Optional.fromNullable(corrected)).or(Optional.fromNullable(planned)).orNull());
    }

    private void stubOrderDate(final String dateFieldName, final DateTime value) {
        Date valueAsPlainOldDateOrNull = Optional.fromNullable(value).transform(new Function<DateTime, Date>() {

            @Override
            public Date apply(final DateTime input) {
                return input.toDate();
            }
        }).orNull();
        stubDateField(order, dateFieldName, valueAsPlainOldDateOrNull);
    }

    /**
     * At day 0, only firstShift works
     */
    private void stubRealizationDaysStream(final DateTime fromDateTime, final Set<Integer> realizationDayNumbers,
            final List<Shift> shifts) {
        OrderRealizationDay[] realizationDays = FluentIterable.from(realizationDayNumbers)
                .transform(new Function<Integer, OrderRealizationDay>() {

                    @Override
                    public OrderRealizationDay apply(final Integer dayNum) {
                        if (dayNum == 0) {
                            return mockRealizationDay(fromDateTime, dayNum, Lists.newArrayList(firstShift));
                        }
                        return mockRealizationDay(fromDateTime, dayNum, shifts);
                    }
                }).toArray(OrderRealizationDay.class);
        stubRealizationDaysStream(fromDateTime, realizationDays);
    }

    private void stubRealizationDaysStream(final DateTime fromDateTime, final OrderRealizationDay... elems) {
        LazyStream<OrderRealizationDay> realizationDaysStreamMock = mockLazyStream(elems);
        given(orderRealizationDaysResolver.asStreamFrom(eq(fromDateTime), anyListOf(Shift.class))).willReturn(
                realizationDaysStreamMock);
    }

    private <T> LazyStream<T> mockLazyStream(final T... elems) {
        return LazyStream.create(elems[0], new Function<T, T>() {

            @Override
            public T apply(final T input) {
                return Optional.fromNullable(ArrayUtils.indexOf(elems, input)).transform(new Function<Integer, T>() {

                    @Override
                    public T apply(final Integer input) {
                        if (input + 1 < elems.length) {
                            return elems[input + 1];
                        }
                        return null;
                    }
                }).orNull();
            }
        });
    }

    private OrderRealizationDay mockRealizationDay(final DateTime start, final int numOfDay, final List<Shift> shifts) {
        OrderRealizationDay realizationDay = mock(OrderRealizationDay.class);
        given(realizationDay.getDate()).willReturn(start.plusDays(numOfDay - 1).toLocalDate());
        given(realizationDay.getRealizationDayNumber()).willReturn(numOfDay);
        given(realizationDay.getWorkingShifts()).willReturn(shifts);
        return realizationDay;
    }

    private void stubProgressForDaysAdlRows(final FormComponent... rows) {
        List<FormComponent> adlRows = ImmutableList.copyOf(rows);
        given(progressForDaysAdl.getFormComponents()).willReturn(adlRows);
    }

    private FormComponent mockFormComponent(final FieldComponent dayField, final FieldComponent dateField) {
        FormComponent formComponent = mockForm(mockEntity());
        stubFormComponent(formComponent, "day", dayField);
        stubFormComponent(formComponent, "date", dateField);
        return formComponent;
    }

    private AwesomeDynamicListComponent mockDailyProgressForShiftAdl(final FormComponent dayRowForm) {
        AwesomeDynamicListComponent dailyProgressForShiftAdl = mock(AwesomeDynamicListComponent.class);
        given(dayRowForm.findFieldComponentByName("dailyProgress")).willReturn(dailyProgressForShiftAdl);
        return dailyProgressForShiftAdl;
    }

    @Test
    public final void shouldFillNewlyAddedRowsAccordingToPlannedDaysAndDates() {
        // given
        stubRealizationDaysStream(PLANNED_ORDER_START, Sets.newHashSet(0, 1, 2, 3, 4, 5, 8, 9, 10), shifts);
        stubRealizationDaysStream(CORRECTED_ORDER_START, Sets.newHashSet(1, 2, 3, 6, 7, 8, 9, 10), shifts);
        stubProgressType(ProgressType.PLANNED);

        FieldComponent day1 = mockFieldComponent("0");
        FieldComponent date1 = mockFieldComponent(null);
        FormComponent row1 = mockFormComponent(day1, date1);
        AwesomeDynamicListComponent row1DailyProgresses = mockDailyProgressForShiftAdl(row1);

        FieldComponent day2 = mockFieldComponent("3");
        FieldComponent date2 = mockFieldComponent(null);
        FormComponent row2 = mockFormComponent(day2, date2);
        AwesomeDynamicListComponent row2DailyProgresses = mockDailyProgressForShiftAdl(row2);

        FieldComponent day3 = mockFieldComponent(null);
        FieldComponent date3 = mockFieldComponent(null);
        FormComponent row3 = mockFormComponent(day3, date3);
        AwesomeDynamicListComponent row3DailyProgresses = mockDailyProgressForShiftAdl(row3);

        FieldComponent day4 = mockFieldComponent(null);
        FieldComponent date4 = mockFieldComponent(null);
        FormComponent row4 = mockFormComponent(day4, date4);
        AwesomeDynamicListComponent row4DailyProgresses = mockDailyProgressForShiftAdl(row4);

        stubProgressForDaysAdlRows(row1, row2, row3, row4);

        // when
        productionPerShiftListeners.updateProgressForDays(view, progressForDaysAdl, new String[] {});

        // then
        verify(day1, never()).setFieldValue(any());
        verify(date1, never()).setFieldValue(any());
        verify(row1DailyProgresses, never()).setFieldValue(any());

        verify(day2, never()).setFieldValue(any());
        verify(date2, never()).setFieldValue(any());
        verify(row2DailyProgresses, never()).setFieldValue(any());

        verify(day3).setFieldValue(4);
        verify(date3).setFieldValue(DateUtils.toDateString(PLANNED_ORDER_START.plusDays(3).toLocalDate().toDate()));
        verify(row3DailyProgresses).setFieldValue(eq(Lists.newArrayList(firstShiftDailyProgress, secondShiftDailyProgress)));

        verify(day4).setFieldValue(5);
        verify(date4).setFieldValue(DateUtils.toDateString(PLANNED_ORDER_START.plusDays(4).toLocalDate().toDate()));
        verify(row4DailyProgresses).setFieldValue(eq(Lists.newArrayList(firstShiftDailyProgress, secondShiftDailyProgress)));
    }

    @Test
    public final void shouldFillNewlyAddedRowsAccordingToCorrectedDaysAndDates() {
        // given
        stubRealizationDaysStream(PLANNED_ORDER_START, Sets.newHashSet(1, 2, 3, 4, 5, 8, 9, 10), shifts);
        stubRealizationDaysStream(CORRECTED_ORDER_START, Sets.newHashSet(1, 2, 3, 6, 7, 8, 9, 10), shifts);
        stubProgressType(ProgressType.CORRECTED);

        FieldComponent day1 = mockFieldComponent("1");
        FieldComponent date1 = mockFieldComponent(null);
        FormComponent row1 = mockFormComponent(day1, date1);
        AwesomeDynamicListComponent row1DailyProgresses = mockDailyProgressForShiftAdl(row1);

        FieldComponent day2 = mockFieldComponent("3");
        FieldComponent date2 = mockFieldComponent(null);
        FormComponent row2 = mockFormComponent(day2, date2);
        AwesomeDynamicListComponent row2DailyProgresses = mockDailyProgressForShiftAdl(row2);

        FieldComponent day3 = mockFieldComponent(null);
        FieldComponent date3 = mockFieldComponent(null);
        FormComponent row3 = mockFormComponent(day3, date3);
        AwesomeDynamicListComponent row3DailyProgresses = mockDailyProgressForShiftAdl(row3);

        FieldComponent day4 = mockFieldComponent(null);
        FieldComponent date4 = mockFieldComponent(null);
        FormComponent row4 = mockFormComponent(day4, date4);
        AwesomeDynamicListComponent row4DailyProgresses = mockDailyProgressForShiftAdl(row4);

        stubProgressForDaysAdlRows(row1, row2, row3, row4);

        // when
        productionPerShiftListeners.updateProgressForDays(view, progressForDaysAdl, new String[] {});

        // then
        verify(day1, never()).setFieldValue(any());
        verify(date1, never()).setFieldValue(any());
        verify(row1DailyProgresses, never()).setFieldValue(any());

        verify(day2, never()).setFieldValue(any());
        verify(date2, never()).setFieldValue(any());
        verify(row2DailyProgresses, never()).setFieldValue(any());

        verify(day3).setFieldValue(6);
        verify(date3).setFieldValue(DateUtils.toDateString(CORRECTED_ORDER_START.plusDays(5).toLocalDate().toDate()));
        verify(row3DailyProgresses).setFieldValue(eq(Lists.newArrayList(firstShiftDailyProgress, secondShiftDailyProgress)));

        verify(day4).setFieldValue(7);
        verify(date4).setFieldValue(DateUtils.toDateString(CORRECTED_ORDER_START.plusDays(6).toLocalDate().toDate()));
        verify(row4DailyProgresses).setFieldValue(eq(Lists.newArrayList(firstShiftDailyProgress, secondShiftDailyProgress)));
    }

    @Test
    public final void shouldDoNothingIfPlannedDateIsNotPresent() {
        // given
        stubOrderStartDates(null, null, null);
        stubProgressType(ProgressType.PLANNED);

        FieldComponent day1 = mockFieldComponent(null);
        FieldComponent date1 = mockFieldComponent(null);
        FormComponent row1 = mockFormComponent(day1, date1);
        AwesomeDynamicListComponent row1DailyProgresses = mockDailyProgressForShiftAdl(row1);

        stubProgressForDaysAdlRows(row1);

        // when
        productionPerShiftListeners.updateProgressForDays(view, progressForDaysAdl, new String[] {});

        // then
        verifyNoMoreInteractions(day1);
        verifyNoMoreInteractions(date1);
        verifyNoMoreInteractions(row1DailyProgresses);
    }

    @Test
    public final void shouldFillFirstDayForShiftThatStartsWorkDayBefore() {
        // given
        stubRealizationDaysStream(PLANNED_ORDER_START, Sets.newHashSet(0, 1, 2, 3, 4, 5, 8, 9, 10), shifts);
        stubRealizationDaysStream(CORRECTED_ORDER_START, Sets.newHashSet(1, 2, 3, 6, 7, 8, 9, 10), shifts);
        stubProgressType(ProgressType.PLANNED);

        FieldComponent day1 = mockFieldComponent(null);
        FieldComponent date1 = mockFieldComponent(null);
        FormComponent row1 = mockFormComponent(day1, date1);
        AwesomeDynamicListComponent row1DailyProgresses = mockDailyProgressForShiftAdl(row1);

        stubProgressForDaysAdlRows(row1);

        // lack of finish date should not be a problem
        stubOrderDate(OrderFields.DATE_TO, null);

        // when
        productionPerShiftListeners.updateProgressForDays(view, progressForDaysAdl, new String[] {});

        // then
        verify(day1).setFieldValue(0);
        verify(date1).setFieldValue(DateUtils.toDateString(PLANNED_ORDER_START.minusDays(1).toDate()));
        verify(row1DailyProgresses).setFieldValue(Lists.newArrayList(firstShiftDailyProgress));
    }

    @Test
    public final void shouldCopyPlannedProgressesToCorrection() {
        // given
        DataDefinition tocDataDefinition = mock(DataDefinition.class);
        Entity technologyOperation = mockEntity(tocDataDefinition);

        stubViewComponent(OPERATION_LOOKUP_REF, mockLookup(technologyOperation));

        List<Entity> correctedPfds = Lists.newArrayList(mockEntity(1L), mockEntity(2L), mockEntity(3L));
        given(progressForDayDataProvider.findForOperation(technologyOperation, ProgressType.CORRECTED)).willReturn(correctedPfds);

        final DataDefinition pfdDataDefinition = mock(DataDefinition.class);

        Entity firstPfd = mockEntity(101L, pfdDataDefinition);
        Entity copyOfFirstPfd = mockEntity(201L, pfdDataDefinition);
        Entity secondPfd = mockEntity(102L, pfdDataDefinition);
        Entity copyOfSecondPfd = mockEntity(202L, pfdDataDefinition);

        given(pfdDataDefinition.copy(101L)).willReturn(Lists.newArrayList(copyOfFirstPfd));
        given(pfdDataDefinition.copy(102L)).willReturn(Lists.newArrayList(copyOfSecondPfd));

        List<Entity> plannedPfds = Lists.newArrayList(firstPfd, secondPfd);
        given(progressForDayDataProvider.findForOperation(technologyOperation, ProgressType.PLANNED)).willReturn(plannedPfds);

        // when
        productionPerShiftListeners.copyFromPlanned(view, mockFieldComponent(null), new String[] {});

        // then
        assertPfdsWereDeleted(1L, 2L, 3L);

        verify(pfdDataDefinition, never()).save(firstPfd);
        verify(pfdDataDefinition).save(copyOfFirstPfd);
        verify(copyOfFirstPfd).setField(ProgressForDayFields.CORRECTED, true);

        verify(pfdDataDefinition, never()).save(secondPfd);
        verify(pfdDataDefinition).save(copyOfSecondPfd);
        verify(copyOfSecondPfd).setField(ProgressForDayFields.CORRECTED, true);
    }

    private void assertPfdsWereDeleted(final Long... ids) {
        verify(progressForDayDataProvider).delete(iterableOfLongsCaptor.capture());
        Assert.assertEquals(Lists.newArrayList(Arrays.asList(ids)), Lists.newArrayList(iterableOfLongsCaptor.getValue()));
    }

}
