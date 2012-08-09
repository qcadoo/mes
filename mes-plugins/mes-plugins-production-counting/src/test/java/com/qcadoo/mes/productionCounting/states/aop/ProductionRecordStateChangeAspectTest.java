package com.qcadoo.mes.productionCounting.states.aop;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ProductionRecordStateChangeAspectTest {

    @Test
    public final void checkSelectorPointcutDefinition() throws NoSuchMethodException {
        final Class<?> clazz = ProductionRecordStateChangeAspect.class;
        assertEquals("com.qcadoo.mes.productionCounting.states.aop.ProductionRecordStateChangeAspect", clazz.getCanonicalName());
    }

}
