package com.qcadoo.mes.orders.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;

import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.commons.dateTime.DateRange;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;

public class OrderDatesServiceTest {

    private OrderDatesService orderDatesService;

    @Mock
    private Entity order;

    @Before
    public final void init() {
        orderDatesService = new OrderDatesServiceImpl();

        MockitoAnnotations.initMocks(this);

    }

    @Test
    public final void shouldReturnEmptyDateRangeIfDatesIsNotSpecified() {
        // when
        DateRange dateRange = orderDatesService.getCalculatedDates(order);

        // then
        assertNull(dateRange.getFrom());
        assertNull(dateRange.getTo());
    }

    @Test
    public final void shouldReturnDateRangeWithPlannedStartDate() {
        // given
        Date plannedStartDate = new DateTime(2013, 01, 01, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.DATE_FROM, plannedStartDate);

        // when
        DateRange dateRange = orderDatesService.getCalculatedDates(order);

        // then
        assertEquals(plannedStartDate, dateRange.getFrom());
        assertNull(dateRange.getTo());
    }

    @Test
    public final void shouldReturnDateRangeWithCorrectedStartDate() {
        // given
        Date plannedStartDate = new DateTime(2013, 1, 1, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.DATE_FROM, plannedStartDate);

        Date correctedStartDate = new DateTime(2013, 1, 5, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.CORRECTED_DATE_FROM, correctedStartDate);

        // when
        DateRange dateRange = orderDatesService.getCalculatedDates(order);

        // then
        assertEquals(correctedStartDate, dateRange.getFrom());
        assertNull(dateRange.getTo());
    }

    @Test
    public final void shouldReturnDateRangeWithCorrectedStartDate2() {
        // given
        Date correctedStartDate = new DateTime(2013, 1, 5, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.CORRECTED_DATE_FROM, correctedStartDate);

        // when
        DateRange dateRange = orderDatesService.getCalculatedDates(order);

        // then
        assertEquals(correctedStartDate, dateRange.getFrom());
        assertNull(dateRange.getTo());
    }

    @Test
    public final void shouldReturnDateRangeWithEffectiveStartDate() {
        // given
        Date plannedStartDate = new DateTime(2013, 1, 1, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.DATE_FROM, plannedStartDate);

        Date correctedStartDate = new DateTime(2013, 1, 5, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.CORRECTED_DATE_FROM, correctedStartDate);

        Date effectiveStartDate = new DateTime(2013, 2, 8, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.EFFECTIVE_DATE_FROM, effectiveStartDate);

        // when
        DateRange dateRange = orderDatesService.getCalculatedDates(order);

        // then
        assertEquals(effectiveStartDate, dateRange.getFrom());
        assertNull(dateRange.getTo());
    }

    @Test
    public final void shouldReturnDateRangeWithEffectiveStartDate2() {
        // given
        Date correctedStartDate = new DateTime(2013, 1, 5, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.CORRECTED_DATE_FROM, correctedStartDate);

        Date effectiveStartDate = new DateTime(2013, 2, 8, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.EFFECTIVE_DATE_FROM, effectiveStartDate);

        // when
        DateRange dateRange = orderDatesService.getCalculatedDates(order);

        // then
        assertEquals(effectiveStartDate, dateRange.getFrom());
        assertNull(dateRange.getTo());
    }

    @Test
    public final void shouldReturnDateRangeWithEffectiveStartDate3() {
        // given
        Date plannedStartDate = new DateTime(2013, 1, 1, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.DATE_FROM, plannedStartDate);

        Date effectiveStartDate = new DateTime(2013, 2, 8, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.EFFECTIVE_DATE_FROM, effectiveStartDate);

        // when
        DateRange dateRange = orderDatesService.getCalculatedDates(order);

        // then
        assertEquals(effectiveStartDate, dateRange.getFrom());
        assertNull(dateRange.getTo());
    }

    @Test
    public final void shouldReturnDateRangeWithEffectiveStartDate4() {
        // given
        Date effectiveStartDate = new DateTime(2013, 2, 8, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.EFFECTIVE_DATE_FROM, effectiveStartDate);

        // when
        DateRange dateRange = orderDatesService.getCalculatedDates(order);

        // then
        assertEquals(effectiveStartDate, dateRange.getFrom());
        assertNull(dateRange.getTo());
    }

    @Test
    public final void shouldReturnDateRangeWithPlannedEndDate() {
        // given
        Date plannedEndDate = new DateTime(2013, 1, 1, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.DATE_TO, plannedEndDate);

        // when
        DateRange dateRange = orderDatesService.getCalculatedDates(order);

        // then
        assertNull(dateRange.getFrom());
        assertEquals(plannedEndDate, dateRange.getTo());
    }

    @Test
    public final void shouldReturnDateRangeWithCorrectedEndDate() {
        // given
        Date plannedEndDate = new DateTime(2013, 1, 1, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.DATE_TO, plannedEndDate);

        Date correctedEndDate = new DateTime(2013, 1, 5, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.CORRECTED_DATE_TO, correctedEndDate);

        // when
        DateRange dateRange = orderDatesService.getCalculatedDates(order);

        // then
        assertNull(dateRange.getFrom());
        assertEquals(correctedEndDate, dateRange.getTo());
    }

    @Test
    public final void shouldReturnDateRangeWithCorrectedEndDate2() {
        // given
        Date correctedEndDate = new DateTime(2013, 1, 5, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.CORRECTED_DATE_TO, correctedEndDate);

        // when
        DateRange dateRange = orderDatesService.getCalculatedDates(order);

        // then
        assertNull(dateRange.getFrom());
        assertEquals(correctedEndDate, dateRange.getTo());
    }

    @Test
    public final void shouldReturnDateRangeWithEffectiveEndDate() {
        // given
        Date plannedEndDate = new DateTime(2013, 1, 1, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.DATE_TO, plannedEndDate);

        Date correctedEndDate = new DateTime(2013, 1, 5, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.CORRECTED_DATE_TO, correctedEndDate);

        Date effectiveEndDate = new DateTime(2013, 2, 8, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.EFFECTIVE_DATE_TO, effectiveEndDate);

        // when
        DateRange dateRange = orderDatesService.getCalculatedDates(order);

        // then
        assertNull(dateRange.getFrom());
        assertEquals(effectiveEndDate, dateRange.getTo());
    }

    @Test
    public final void shouldReturnDateRangeWithEffectiveEndDate2() {
        // given
        Date plannedEndDate = new DateTime(2013, 1, 1, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.DATE_TO, plannedEndDate);

        Date effectiveEndDate = new DateTime(2013, 2, 8, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.EFFECTIVE_DATE_TO, effectiveEndDate);

        // when
        DateRange dateRange = orderDatesService.getCalculatedDates(order);

        // then
        assertNull(dateRange.getFrom());
        assertEquals(effectiveEndDate, dateRange.getTo());
    }

    @Test
    public final void shouldReturnDateRangeWithEffectiveEndDate3() {
        // given
        Date correctedEndDate = new DateTime(2013, 1, 5, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.CORRECTED_DATE_TO, correctedEndDate);

        Date effectiveEndDate = new DateTime(2013, 2, 8, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.EFFECTIVE_DATE_TO, effectiveEndDate);

        // when
        DateRange dateRange = orderDatesService.getCalculatedDates(order);

        // then
        assertNull(dateRange.getFrom());
        assertEquals(effectiveEndDate, dateRange.getTo());
    }

    @Test
    public final void shouldReturnDateRangeWithEffectiveEndDate4() {
        // given
        Date effectiveEndDate = new DateTime(2013, 2, 8, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.EFFECTIVE_DATE_TO, effectiveEndDate);

        // when
        DateRange dateRange = orderDatesService.getCalculatedDates(order);

        // then
        assertNull(dateRange.getFrom());
        assertEquals(effectiveEndDate, dateRange.getTo());
    }

    @Test
    public final void shouldReturnDateRangeWithPlannedStartAndPlannedEndDate() {
        // given
        Date plannedStartDate = new DateTime(2012, 12, 30, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.DATE_FROM, plannedStartDate);

        Date plannedEndDate = new DateTime(2013, 1, 1, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.DATE_TO, plannedEndDate);

        // when
        DateRange dateRange = orderDatesService.getCalculatedDates(order);

        // then
        assertEquals(plannedStartDate, dateRange.getFrom());
        assertEquals(plannedEndDate, dateRange.getTo());
    }

    @Test
    public final void shouldReturnDateRangeWithCorrectedStartAndCorrectedEndDate() {
        // given
        Date plannedStartDate = new DateTime(2012, 12, 30, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.DATE_FROM, plannedStartDate);

        Date plannedEndDate = new DateTime(2013, 1, 1, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.DATE_TO, plannedEndDate);

        Date correctedStartDate = new DateTime(2013, 1, 3, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.CORRECTED_DATE_FROM, correctedStartDate);

        Date correctedEndDate = new DateTime(2013, 1, 5, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.CORRECTED_DATE_TO, correctedEndDate);

        // when
        DateRange dateRange = orderDatesService.getCalculatedDates(order);

        // then
        assertEquals(correctedStartDate, dateRange.getFrom());
        assertEquals(correctedEndDate, dateRange.getTo());
    }

    @Test
    public final void shouldReturnDateRangeWithEffectiveStartAndEffectiveEndDate() {
        // given
        Date plannedStartDate = new DateTime(2012, 12, 30, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.DATE_FROM, plannedStartDate);

        Date plannedEndDate = new DateTime(2013, 1, 1, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.DATE_TO, plannedEndDate);

        Date correctedStartDate = new DateTime(2013, 1, 3, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.CORRECTED_DATE_FROM, correctedStartDate);

        Date correctedEndDate = new DateTime(2013, 1, 5, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.CORRECTED_DATE_TO, correctedEndDate);

        Date effectiveStartDate = new DateTime(2013, 2, 3, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.EFFECTIVE_DATE_FROM, effectiveStartDate);

        Date effectiveEndDate = new DateTime(2013, 2, 8, 0, 0, 0, 0).toDate();
        stubDateField(order, OrderFields.EFFECTIVE_DATE_TO, effectiveEndDate);

        // when
        DateRange dateRange = orderDatesService.getCalculatedDates(order);

        // then
        assertEquals(effectiveStartDate, dateRange.getFrom());
        assertEquals(effectiveEndDate, dateRange.getTo());
    }

    private void stubDateField(final Entity entity, final String fieldName, final Date fieldValue) {
        given(entity.getField(fieldName)).willReturn(fieldValue);
        given(entity.getDateField(fieldName)).willReturn(fieldValue);
    }

}
