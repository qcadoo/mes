package com.qcadoo.mes.states.aop;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.qcadoo.mes.states.StateChangeContext;

public class StatesXpiAspectTest {

    @Test
    public final void checkPointcutsDefinition() throws NoSuchMethodException {
        assertEquals("com.qcadoo.mes.states.aop.AbstractStateListenerAspect",
                AbstractStateListenerAspect.class.getCanonicalName());
        assertEquals("com.qcadoo.mes.states.StateChangeContext", StateChangeContext.class.getCanonicalName());
    }

}
