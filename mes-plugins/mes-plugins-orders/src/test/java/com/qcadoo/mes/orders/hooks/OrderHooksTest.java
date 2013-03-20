package com.qcadoo.mes.orders.hooks;

import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Date;

import org.junit.Test;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class OrderHooksTest {

    private OrderHooks orderHooks;

    @org.junit.Before
    public void init() {
        orderHooks = new OrderHooks();
    }

    @Test
    public void shouldClearOrderFieldsOnCopy() throws Exception {
        // given
        Date startDate = new Date();
        Date finishDate = new Date();
        Entity order = mock(Entity.class);

        DataDefinition dataDefinition = mock(DataDefinition.class);
        given(order.getDateField(OrderFields.START_DATE)).willReturn(startDate);
        given(order.getDateField(OrderFields.FINISH_DATE)).willReturn(finishDate);

        // when
        boolean result = orderHooks.clearOrSetSpecyfiedValueOrderFieldsOnCopy(dataDefinition, order);
        // then
        assertTrue(result);
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
