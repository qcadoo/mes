package com.qcadoo.mes.orders.dates;

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.EntityTestUtils.stubDateField;

import java.util.Date;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;

import com.google.common.base.Optional;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;

public class OrderDatesTest {

    private static final DateTime START_DATE_1 = DateTime.now();

    private static final DateTime START_DATE_2 = START_DATE_1.plusDays(1);

    private static final DateTime START_DATE_3 = START_DATE_1.plusDays(2);

    private static final DateTime END_DATE_1 = START_DATE_1.plusDays(3);

    private static final DateTime END_DATE_2 = START_DATE_1.plusDays(4);

    private static final DateTime END_DATE_3 = START_DATE_1.plusDays(5);

    private Entity mockOrder(final Date plannedStart, final Date correctedStart, final Date effectiveStart,
            final Date plannedEnd, final Date correctedEnd, final Date effectiveEnd) {
        Entity order = mockEntity();
        stubDateField(order, OrderFields.DATE_FROM, plannedStart);
        stubDateField(order, OrderFields.CORRECTED_DATE_FROM, correctedStart);
        stubDateField(order, OrderFields.EFFECTIVE_DATE_FROM, effectiveStart);
        stubDateField(order, OrderFields.DATE_TO, plannedEnd);
        stubDateField(order, OrderFields.CORRECTED_DATE_TO, correctedEnd);
        stubDateField(order, OrderFields.EFFECTIVE_DATE_TO, effectiveEnd);
        return order;
    }

    @Test
    public final void shouldCreateCorrectOrderDates1() {
        // given
        Entity order = mockOrder(START_DATE_1.toDate(), START_DATE_2.toDate(), START_DATE_3.toDate(), END_DATE_1.toDate(),
                END_DATE_2.toDate(), END_DATE_3.toDate());

        // when
        Optional<OrderDates> maybeOrderDates = OrderDates.of(order);

        // then
        Assert.assertTrue(maybeOrderDates.isPresent());
        OrderDates orderDates = maybeOrderDates.get();

        Assert.assertEquals(START_DATE_1, orderDates.getStart().planned());
        Assert.assertEquals(Optional.of(START_DATE_2), orderDates.getStart().corrected());
        Assert.assertEquals(Optional.of(START_DATE_3), orderDates.getStart().effective());

        Assert.assertEquals(START_DATE_2, orderDates.getStart().correctedWithFallback());
        Assert.assertEquals(START_DATE_3, orderDates.getStart().effectiveWithFallback());

        Assert.assertEquals(END_DATE_1, orderDates.getEnd().planned());
        Assert.assertEquals(Optional.of(END_DATE_2), orderDates.getEnd().corrected());
        Assert.assertEquals(Optional.of(END_DATE_3), orderDates.getEnd().effective());

        Assert.assertEquals(END_DATE_2, orderDates.getEnd().correctedWithFallback());
        Assert.assertEquals(END_DATE_3, orderDates.getEnd().effectiveWithFallback());
    }

    @Test
    public final void shouldCreateCorrectOrderDates2() {
        // given
        Entity order = mockOrder(START_DATE_1.toDate(), START_DATE_2.toDate(), null, END_DATE_1.toDate(), END_DATE_2.toDate(),
                null);

        // when
        Optional<OrderDates> maybeOrderDates = OrderDates.of(order);

        // then
        Assert.assertTrue(maybeOrderDates.isPresent());
        OrderDates orderDates = maybeOrderDates.get();

        Assert.assertEquals(START_DATE_1, orderDates.getStart().planned());
        Assert.assertEquals(Optional.of(START_DATE_2), orderDates.getStart().corrected());
        Assert.assertEquals(Optional.<LocalDate> absent(), orderDates.getStart().effective());

        Assert.assertEquals(START_DATE_2, orderDates.getStart().correctedWithFallback());
        Assert.assertEquals(START_DATE_2, orderDates.getStart().effectiveWithFallback());

        Assert.assertEquals(END_DATE_1, orderDates.getEnd().planned());
        Assert.assertEquals(Optional.of(END_DATE_2), orderDates.getEnd().corrected());
        Assert.assertEquals(Optional.<LocalDate> absent(), orderDates.getEnd().effective());

        Assert.assertEquals(END_DATE_2, orderDates.getEnd().correctedWithFallback());
        Assert.assertEquals(END_DATE_2, orderDates.getEnd().effectiveWithFallback());
    }

    @Test
    public final void shouldCreateCorrectOrderDates3() {
        // given
        Entity order = mockOrder(START_DATE_1.toDate(), null, null, END_DATE_1.toDate(), null, null);

        // when
        Optional<OrderDates> maybeOrderDates = OrderDates.of(order);

        // then
        Assert.assertTrue(maybeOrderDates.isPresent());
        OrderDates orderDates = maybeOrderDates.get();

        Assert.assertEquals(START_DATE_1, orderDates.getStart().planned());
        Assert.assertEquals(Optional.<LocalDate> absent(), orderDates.getStart().corrected());
        Assert.assertEquals(Optional.<LocalDate> absent(), orderDates.getStart().effective());

        Assert.assertEquals(START_DATE_1, orderDates.getStart().correctedWithFallback());
        Assert.assertEquals(START_DATE_1, orderDates.getStart().effectiveWithFallback());

        Assert.assertEquals(END_DATE_1, orderDates.getEnd().planned());
        Assert.assertEquals(Optional.<LocalDate> absent(), orderDates.getEnd().corrected());
        Assert.assertEquals(Optional.<LocalDate> absent(), orderDates.getEnd().effective());

        Assert.assertEquals(END_DATE_1, orderDates.getEnd().correctedWithFallback());
        Assert.assertEquals(END_DATE_1, orderDates.getEnd().effectiveWithFallback());
    }

    @Test
    public final void shouldReturnAbsentIfOrderDoesNotHaveDefinedPlannedDates() {
        // given
        Entity order = mockOrder(null, null, null, null, null, null);

        // when
        Optional<OrderDates> maybeOrderDates = OrderDates.of(order);

        // then
        Assert.assertFalse(maybeOrderDates.isPresent());
    }

    @Test
    public final void shouldUsePassedDefaultDatesIfOrderDoesNotHaveDefinedPlannedDates() {
        // given
        Entity order = mockOrder(null, null, null, null, null, null);

        // when
        OrderDates orderDates = OrderDates.of(order, START_DATE_1, END_DATE_1);

        // then
        Assert.assertEquals(START_DATE_1, orderDates.getStart().planned());
        Assert.assertEquals(Optional.<LocalDate> absent(), orderDates.getStart().corrected());
        Assert.assertEquals(Optional.<LocalDate> absent(), orderDates.getStart().effective());

        Assert.assertEquals(START_DATE_1, orderDates.getStart().correctedWithFallback());
        Assert.assertEquals(START_DATE_1, orderDates.getStart().effectiveWithFallback());

        Assert.assertEquals(END_DATE_1, orderDates.getEnd().planned());
        Assert.assertEquals(Optional.<LocalDate> absent(), orderDates.getEnd().corrected());
        Assert.assertEquals(Optional.<LocalDate> absent(), orderDates.getEnd().effective());

        Assert.assertEquals(END_DATE_1, orderDates.getEnd().correctedWithFallback());
        Assert.assertEquals(END_DATE_1, orderDates.getEnd().effectiveWithFallback());
    }

}
