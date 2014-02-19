package com.qcadoo.mes.orders.hooks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.commons.dateTime.DateRange;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.orders.util.OrderDatesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

public class OrderHooksTest {

    private static final Long L_ID = 1L;

    private OrderHooks orderHooks;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderDatesService orderDatesService;

    @Mock
    private DataDefinition orderDD;

    @Mock
    private FieldDefinition dateToField, plannedQuantityField;

    @Mock
    private Entity order, product, productionLine, defaultProductionLine;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderHooks = new OrderHooks();

        setField(orderHooks, "orderService", orderService);
        setField(orderHooks, "orderDatesService", orderDatesService);
    }

    @Test
    public void shouldntFillProductionLineIfFormIsSavedAndProductionLineIsntNull() {
        // given
        given(order.getId()).willReturn(null);
        given(order.getBelongsToField(OrderFields.PRODUCTION_LINE)).willReturn(productionLine);

        // when
        orderHooks.fillProductionLine(orderDD, order);

        // then
        verify(order, never()).setField(OrderFields.PRODUCTION_LINE, defaultProductionLine);
    }

    @Test
    public void shouldntFillProductionLineIfOrderIsntSaved() {
        // given
        given(order.getId()).willReturn(L_ID);

        // when
        orderHooks.fillProductionLine(orderDD, order);

        // then
        verify(order, never()).setField(OrderFields.PRODUCTION_LINE, defaultProductionLine);
    }

    @Test
    public void shouldntFillProductionLineIfOrderIsSavedAndProductionLineIsntNull() {
        // given
        given(order.getId()).willReturn(null);
        given(order.getBelongsToField(OrderFields.PRODUCTION_LINE)).willReturn(productionLine);

        // when
        orderHooks.fillProductionLine(orderDD, order);

        // then
        verify(order, never()).setField(OrderFields.PRODUCTION_LINE, defaultProductionLine);
    }

    @Test
    public void shouldntFillProductionLineIfOrderIsSavedAndProductionLineIsNullAndDefaultProductionLineIsNull() {
        // given
        given(order.getId()).willReturn(null);
        given(order.getBelongsToField(OrderFields.PRODUCTION_LINE)).willReturn(null);

        given(orderService.getDefaultProductionLine()).willReturn(null);

        // when
        orderHooks.fillProductionLine(orderDD, order);

        // then
        verify(order, never()).setField(OrderFields.PRODUCTION_LINE, defaultProductionLine);
    }

    @Test
    public void shouldFillProductionLineIfOrderIsSavedAndProductionLineIsNullAndDefaultProductionLineIsntNull() {
        // given
        given(order.getId()).willReturn(null);
        given(order.getBelongsToField(OrderFields.PRODUCTION_LINE)).willReturn(null);

        given(orderService.getDefaultProductionLine()).willReturn(defaultProductionLine);

        // when
        orderHooks.fillProductionLine(orderDD, order);

        // then
        verify(order).setField(OrderFields.PRODUCTION_LINE, defaultProductionLine);
    }

    @Test
    public void shouldReturnTrueForValidOrderDates() throws Exception {
        // given
        DateRange dateRange = new DateRange(new Date(System.currentTimeMillis() - 10000), new Date());

        given(orderDatesService.getCalculatedDates(order)).willReturn(dateRange);

        // when
        boolean result = orderHooks.checkOrderDates(orderDD, order);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueForNullDates() throws Exception {
        // given
        DateRange dateRange = new DateRange(null, null);

        given(orderDatesService.getCalculatedDates(order)).willReturn(dateRange);

        // when
        boolean result = orderHooks.checkOrderDates(orderDD, order);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueForNullFromDate() throws Exception {
        // given
        DateRange dateRange = new DateRange(null, new Date());

        given(orderDatesService.getCalculatedDates(order)).willReturn(dateRange);

        // when
        boolean result = orderHooks.checkOrderDates(orderDD, order);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueForNullToDate() throws Exception {
        // given
        DateRange dateRange = new DateRange(new Date(), null);

        given(orderDatesService.getCalculatedDates(order)).willReturn(dateRange);

        // when
        boolean result = orderHooks.checkOrderDates(orderDD, order);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseForInvalidOrderDates() throws Exception {
        // given
        DateRange dateRange = new DateRange(new Date(), new Date(System.currentTimeMillis() - 10000));

        given(orderDatesService.getCalculatedDates(order)).willReturn(dateRange);
        given(orderDD.getField(OrderFields.FINISH_DATE)).willReturn(dateToField);

        // when
        boolean result = orderHooks.checkOrderDates(orderDD, order);

        // then
        assertFalse(result);
        verify(order).addError(dateToField, "orders.validate.global.error.datesOrder");
    }

    @Test
    public void shouldReturnFalseForEqualOrderDates() throws Exception {
        // given
        Date currDate = new Date();
        DateRange dateRange = new DateRange(currDate, currDate);

        given(orderDatesService.getCalculatedDates(order)).willReturn(dateRange);
        given(orderDD.getField(OrderFields.FINISH_DATE)).willReturn(dateToField);

        // when
        boolean result = orderHooks.checkOrderDates(orderDD, order);

        // then
        assertFalse(result);
        verify(order).addError(dateToField, "orders.validate.global.error.datesOrder");
    }

    @Test
    public void shouldReturnTrueForPlannedQuantityValidationIfThereIsNoProduct() throws Exception {
        // given
        given(order.getBelongsToField(OrderFields.PRODUCT)).willReturn(null);

        // when
        boolean result = orderHooks.checkOrderPlannedQuantity(orderDD, order);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueForPlannedQuantityValidation() throws Exception {
        // given
        given(order.getBelongsToField(OrderFields.PRODUCT)).willReturn(product);
        given(order.getDecimalField(OrderFields.PLANNED_QUANTITY)).willReturn(BigDecimal.ONE);

        // when
        boolean result = orderHooks.checkOrderPlannedQuantity(orderDD, order);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseForPlannedQuantityValidation() throws Exception {
        // given
        given(order.getBelongsToField(OrderFields.PRODUCT)).willReturn(product);
        given(order.getDecimalField(OrderFields.PLANNED_QUANTITY)).willReturn(null);
        given(orderDD.getField(OrderFields.PLANNED_QUANTITY)).willReturn(plannedQuantityField);

        // when
        boolean result = orderHooks.checkOrderPlannedQuantity(orderDD, order);

        // then
        assertFalse(result);
        verify(order).addError(plannedQuantityField, "orders.validate.global.error.plannedQuantityError");
    }

    @Test
    public void shouldClearOrderFieldsOnCopy() throws Exception {
        // given
        Date startDate = new Date();
        Date finishDate = new Date();

        given(order.getDateField(OrderFields.START_DATE)).willReturn(startDate);
        given(order.getDateField(OrderFields.FINISH_DATE)).willReturn(finishDate);

        // when
        orderHooks.clearOrSetSpecyfiedValueOrderFieldsOnCopy(orderDD, order);

        // then
        verify(order).setField(OrderFields.STATE, OrderState.PENDING.getStringValue());
        verify(order).setField(OrderFields.EFFECTIVE_DATE_TO, null);
        verify(order).setField(OrderFields.EFFECTIVE_DATE_FROM, null);
        verify(order).setField(OrderFields.CORRECTED_DATE_FROM, null);
        verify(order).setField(OrderFields.CORRECTED_DATE_TO, null);
        verify(order).setField(OrderFields.DONE_QUANTITY, null);
        verify(order).setField(OrderFields.DATE_FROM, startDate);
        verify(order).setField(OrderFields.DATE_TO, finishDate);
    }

}
