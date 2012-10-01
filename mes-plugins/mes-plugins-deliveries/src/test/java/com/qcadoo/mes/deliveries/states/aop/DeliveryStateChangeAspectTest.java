package com.qcadoo.mes.deliveries.states.aop;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DeliveryStateChangeAspectTest {

    @Test
    public final void checkSelectorPointcutDefinition() throws NoSuchMethodException {
        final Class<?> clazz = DeliveryStateChangeAspect.class;
        assertEquals("com.qcadoo.mes.deliveries.states.aop.DeliveryStateChangeAspect", clazz.getCanonicalName());
    }
}
