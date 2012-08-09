package com.qcadoo.mes.orders.states.aop;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OrderStateChangeAspectTest {

    @Test
    public final void checkSelectorPointcutDefinition() throws NoSuchMethodException {
        final Class<?> clazz = OrderStateChangeAspect.class;
        assertEquals("com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect", clazz.getCanonicalName());
    }

}
