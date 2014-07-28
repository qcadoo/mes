package com.qcadoo.mes.productionPerShift.dates;

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.EntityTestUtils.stubBelongsToField;
import static com.qcadoo.testing.model.EntityTestUtils.stubBooleanField;
import static com.qcadoo.testing.model.EntityTestUtils.stubDateField;
import static com.qcadoo.testing.model.EntityTestUtils.stubIntegerField;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.qcadoo.commons.functional.LazyStream;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.basic.shift.ShiftsDataProvider;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionPerShift.constants.ProgressForDayFields;
import com.qcadoo.mes.productionPerShift.dataProvider.ProgressForDayDataProvider;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class ProgressDatesServiceTest {

    private ProgressDatesService progressDatesService;

    @Mock
    private OrderRealizationDaysResolver orderRealizationDaysResolver;

    @Mock
    private ProgressForDayDataProvider progressForDayDataProvider;

    @Mock
    private ShiftsDataProvider shiftsDataProvider;

    @Mock
    private Entity orderEntity;

    @Mock
    private DataDefinition pfdDataDefinition;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        progressDatesService = new ProgressDatesService();

        ReflectionTestUtils.setField(progressDatesService, "orderRealizationDaysResolver", orderRealizationDaysResolver);
        ReflectionTestUtils.setField(progressDatesService, "progressForDayDataProvider", progressForDayDataProvider);
        ReflectionTestUtils.setField(progressDatesService, "shiftsDataProvider", shiftsDataProvider);

        given(shiftsDataProvider.findAll()).willReturn(ImmutableList.<Shift> of());
    }

    private void stubOrderStartDates(final DateTime planned, final DateTime corrected, final DateTime effective) {
        stubOrderDate(OrderFields.DATE_FROM, planned);
        stubOrderDate(OrderFields.CORRECTED_DATE_FROM, corrected);
        stubOrderDate(OrderFields.EFFECTIVE_DATE_FROM, effective);
        stubOrderDate(OrderFields.DATE_TO, Optional.fromNullable(effective).or(Optional.fromNullable(corrected)).or(planned)
                .plusWeeks(2));
    }

    private void stubOrderDate(final String dateFieldName, final DateTime value) {
        Date valueAsPlainOldDateOrNull = Optional.fromNullable(value).transform(new Function<DateTime, Date>() {

            @Override
            public Date apply(final DateTime input) {
                return input.toDate();
            }
        }).orNull();
        stubDateField(orderEntity, dateFieldName, valueAsPlainOldDateOrNull);
    }

    private void stubProgressForDayFind(final Entity... progressesForDays) {
        given(progressForDayDataProvider.findForOrder(orderEntity, ProgressForDayDataProvider.DEFAULT_SEARCH_ORDER)).willReturn(
                ImmutableList.copyOf(Arrays.asList(progressesForDays)));
    }

    private void stubRealizationDaysStream(final DateTime fromDateTime, final Set<Integer> dayNums) {
        OrderRealizationDay[] realizationDays = FluentIterable.from(dayNums)
                .transform(new Function<Integer, OrderRealizationDay>() {

                    @Override
                    public OrderRealizationDay apply(final Integer dayNum) {
                        return realizationDay(fromDateTime, dayNum);
                    }
                }).toArray(OrderRealizationDay.class);
        stubRealizationDaysStream(fromDateTime, realizationDays);
    }

    private void stubRealizationDaysStream(final DateTime fromDateTime, final OrderRealizationDay... elems) {
        LazyStream<OrderRealizationDay> realizationDaysStreamMock = mockLazyStream(elems);
        given(orderRealizationDaysResolver.asStreamFrom(eq(fromDateTime), anyList())).willReturn(realizationDaysStreamMock);
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

    private OrderRealizationDay realizationDay(final DateTime start, final int numOfDay) {
        return new OrderRealizationDay(start.toLocalDate(), numOfDay, ImmutableList.<Shift> of());
    }

    @Test
    public final void shouldDoNothingIfOrderDoesNotHaveDefinedPlannedDates() {
        // given
        stubOrderDate(OrderFields.DATE_FROM, null);
        stubOrderDate(OrderFields.DATE_TO, null);

        // when
        progressDatesService.setUpDatesFor(orderEntity);

        // then
        verifyNoMoreInteractions(progressForDayDataProvider);
        verifyNoMoreInteractions(pfdDataDefinition);
    }

    @Test
    public final void shouldSetUpDatesForOrderEvenIfPlannedEndDateIsNotSpecified() {
        // given
        DateTime plannedStart = new DateTime(2014, 8, 14, 12, 0, 0); // thursday
        DateTime correctedStart = new DateTime(2014, 8, 16, 12, 0, 0); // saturday
        DateTime effectiveStart = new DateTime(2014, 8, 19, 12, 0, 0); // tuesday

        stubOrderStartDates(plannedStart, correctedStart, effectiveStart);
        stubOrderDate(OrderFields.DATE_TO, null);

        stubRealizationDaysStream(plannedStart, Sets.newHashSet(1, 2, 5, 6, 7, 8, 9, 12, 13, 14));
        stubRealizationDaysStream(correctedStart, Sets.newHashSet(3, 4, 5, 6, 7, 10, 11, 12, 13, 14));
        stubRealizationDaysStream(effectiveStart, Sets.newHashSet(1, 2, 3, 4, 7, 8, 9, 10, 11, 14));

        Entity planned1 = mockPfd(1, false, 1L);
        Entity planned2 = mockPfd(2, false, 1L);
        Entity planned3 = mockPfd(3, false, 1L);
        Entity corrected1 = mockPfd(1, true, 1L);
        Entity corrected3 = mockPfd(3, true, 1L);
        stubProgressForDayFind(planned1, corrected1, planned2, planned3, corrected3);

        // when
        progressDatesService.setUpDatesFor(orderEntity);

        // then
        verify(progressForDayDataProvider).findForOrder(orderEntity, ProgressForDayDataProvider.DEFAULT_SEARCH_ORDER);

        verifyProgressValues(planned1, 1, plannedStart, effectiveStart);
        verifyProgressValues(planned2, 2, plannedStart.plusDays(1), effectiveStart.plusDays(1));
        verifyProgressValues(planned3, 5, plannedStart.plusDays(4), effectiveStart.plusDays(2));
        verifyProgressValues(corrected1, 3, correctedStart.plusDays(2), effectiveStart);
        verifyProgressValues(corrected3, 4, correctedStart.plusDays(3), effectiveStart.plusDays(2));
    }

    @Test
    public final void shouldSetUpDatesForOrderWhenAllProgressesHaveTheSameDayNumber() {
        // given
        DateTime plannedStart = new DateTime(2014, 8, 14, 12, 0, 0); // thursday
        DateTime correctedStart = new DateTime(2014, 8, 16, 12, 0, 0); // saturday
        DateTime effectiveStart = new DateTime(2014, 8, 19, 12, 0, 0); // tuesday

        stubOrderStartDates(plannedStart, correctedStart, effectiveStart);

        stubRealizationDaysStream(plannedStart, Sets.newHashSet(1, 2, 5, 6, 7, 8, 9, 12, 13, 14));
        stubRealizationDaysStream(correctedStart, Sets.newHashSet(3, 4, 5, 6, 7, 10, 11, 12, 13, 14));
        stubRealizationDaysStream(effectiveStart, Sets.newHashSet(1, 2, 3, 4, 7, 8, 9, 10, 11, 14));

        Entity planned1 = mockPfd(1, false, 1L);
        Entity planned2 = mockPfd(1, false, 1L);
        Entity planned3 = mockPfd(1, false, 1L);
        Entity planned4 = mockPfd(1, false, 1L);
        stubProgressForDayFind(planned1, planned2, planned3, planned4);

        // when
        progressDatesService.setUpDatesFor(orderEntity);

        // then
        verify(progressForDayDataProvider).findForOrder(orderEntity, ProgressForDayDataProvider.DEFAULT_SEARCH_ORDER);

        verifyProgressValues(planned1, 1, plannedStart, effectiveStart);
        verifyProgressValues(planned2, 2, plannedStart.plusDays(1), effectiveStart.plusDays(1));
        verifyProgressValues(planned3, 5, plannedStart.plusDays(4), effectiveStart.plusDays(2));
        verifyProgressValues(planned4, 6, plannedStart.plusDays(5), effectiveStart.plusDays(3));
    }

    @Test
    public final void shouldSetUpDatesForOrderWithCorrectionsAndAvoidDateCollisions() {
        // given
        DateTime plannedStart = new DateTime(2014, 8, 11, 12, 0, 0); // monday
        DateTime correctedStart = new DateTime(2014, 8, 15, 12, 0, 0); // friday

        stubOrderStartDates(plannedStart, correctedStart, null);

        stubRealizationDaysStream(plannedStart, Sets.newHashSet(1, 2, 3, 4, 5, 8, 9, 10, 11, 12));
        stubRealizationDaysStream(correctedStart, Sets.newHashSet(4, 5, 6, 7, 8, 11, 12, 13, 14, 15));

        Entity planned1 = mockPfd(1, false, 1L);
        Entity planned3 = mockPfd(3, false, 1L);
        Entity corrected1 = mockPfd(1, true, 1L);
        Entity corrected3 = mockPfd(3, true, 1L);
        stubProgressForDayFind(planned1, corrected1, planned3, corrected3);

        // when
        progressDatesService.setUpDatesFor(orderEntity);

        // then
        verify(progressForDayDataProvider).findForOrder(orderEntity, ProgressForDayDataProvider.DEFAULT_SEARCH_ORDER);

        verifyProgressValues(planned1, 1, plannedStart, correctedStart.plusDays(3));
        verifyProgressValues(planned3, 3, plannedStart.plusDays(2), correctedStart.plusDays(4));
        verifyProgressValues(corrected1, 4, correctedStart.plusDays(3), correctedStart.plusDays(3));
        verifyProgressValues(corrected3, 5, correctedStart.plusDays(4), correctedStart.plusDays(4));
    }

    @Test
    public final void shouldSetUpDatesForOrderWithoutCorrections() {
        // given
        DateTime plannedStart = new DateTime(2014, 8, 11, 12, 0, 0); // monday

        stubOrderStartDates(plannedStart, null, null);

        stubRealizationDaysStream(plannedStart, Sets.newHashSet(1, 2, 3, 4, 5, 8, 9, 10, 11, 12, 13, 16, 17, 18));

        Entity planned1 = mockPfd(1, false, 1L);
        Entity planned3 = mockPfd(3, false, 1L);
        Entity planned6 = mockPfd(6, false, 1L);
        stubProgressForDayFind(planned1, planned3, planned6);

        // when
        progressDatesService.setUpDatesFor(orderEntity);

        // then
        verify(progressForDayDataProvider).findForOrder(orderEntity, ProgressForDayDataProvider.DEFAULT_SEARCH_ORDER);

        verifyProgressValues(planned1, 1, plannedStart, plannedStart);
        verifyProgressValues(planned3, 3, plannedStart.plusDays(2), plannedStart.plusDays(2));
        verifyProgressValues(planned6, 8, plannedStart.plusDays(7), plannedStart.plusDays(7));
    }

    @Test
    public final void shouldSetUpDatesInScopeOfTechnologyOperation() {
        // given
        DateTime plannedStart = new DateTime(2014, 8, 11, 12, 0, 0); // monday

        stubOrderStartDates(plannedStart, null, null);

        stubRealizationDaysStream(plannedStart, Sets.newHashSet(1, 2, 3, 4, 5, 8, 9, 10, 11, 12, 13, 16, 17, 18));

        Entity planned1toc1 = mockPfd(1, false, 1L);
        Entity planned3toc1 = mockPfd(3, false, 1L);
        Entity planned3toc3 = mockPfd(3, false, 3L);
        Entity planned6toc1 = mockPfd(6, false, 1L);
        Entity planned6toc1collision = mockPfd(6, false, 1L);
        stubProgressForDayFind(planned1toc1, planned3toc1, planned3toc3, planned6toc1, planned6toc1collision);

        // when
        progressDatesService.setUpDatesFor(orderEntity);

        // then
        verify(progressForDayDataProvider).findForOrder(orderEntity, ProgressForDayDataProvider.DEFAULT_SEARCH_ORDER);

        verifyProgressValues(planned1toc1, 1, plannedStart, plannedStart);
        verifyProgressValues(planned3toc1, 3, plannedStart.plusDays(2), plannedStart.plusDays(2));
        verifyProgressValues(planned3toc3, 3, plannedStart.plusDays(2), plannedStart.plusDays(2));
        verifyProgressValues(planned6toc1, 8, plannedStart.plusDays(7), plannedStart.plusDays(7));
        verifyProgressValues(planned6toc1collision, 9, plannedStart.plusDays(8), plannedStart.plusDays(8));
    }

    private void verifyProgressValues(final Entity progressForDay, final int numOfDay, final DateTime dateForDay,
            final DateTime effectiveDateForDay) {
        verify(progressForDay).setField(ProgressForDayFields.DAY, numOfDay);
        verify(progressForDay).setField(ProgressForDayFields.DATE_OF_DAY, dateForDay.toLocalDate().toDate());
        verify(progressForDay).setField(ProgressForDayFields.ACTUAL_DATE_OF_DAY, effectiveDateForDay.toLocalDate().toDate());
        verify(pfdDataDefinition).save(progressForDay);
    }

    private Entity mockPfd(final int numOfRealizationDay, final boolean isCorrection, final Long technologyOperationId) {
        Entity pfd = mockEntity(pfdDataDefinition);
        stubBooleanField(pfd, ProgressForDayFields.CORRECTED, isCorrection);
        stubIntegerField(pfd, ProgressForDayFields.DAY, numOfRealizationDay);
        stubBelongsToField(pfd, ProgressForDayFields.TECHNOLOGY_OPERATION_COMPONENT, mockEntity(technologyOperationId));
        return pfd;
    }

}
