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
package com.qcadoo.mes.orders.hooks;

import com.qcadoo.commons.dateTime.DateRange;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.orders.util.OrderDatesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Date;

import static com.qcadoo.testing.model.EntityTestUtils.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class OrderHooksTest {

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
    private Entity order, product;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderHooks = new OrderHooks();

        setField(orderHooks, "orderService", orderService);
        setField(orderHooks, "orderDatesService", orderDatesService);
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
        stubBelongsToField(order, OrderFields.PRODUCT, null);

        // when
        boolean result = orderHooks.checkOrderPlannedQuantity(orderDD, order);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueForPlannedQuantityValidation() throws Exception {
        // given
        stubBelongsToField(order, OrderFields.PRODUCT, product);
        stubDecimalField(order, OrderFields.PLANNED_QUANTITY, BigDecimal.ONE);
        stubStringField(product, ProductFields.UNIT, "szt.");

        // when
        boolean result = orderHooks.checkOrderPlannedQuantity(orderDD, order);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseForPlannedQuantityValidation() throws Exception {
        // given
        stubBelongsToField(order, OrderFields.PRODUCT, product);
        stubDecimalField(order, OrderFields.PLANNED_QUANTITY, null);
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

        stubDateField(order, OrderFields.START_DATE, startDate);
        stubDateField(order, OrderFields.FINISH_DATE, finishDate);

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
