package com.qcadoo.mes.technologies.states.aop;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TechnologyStateChangeAspectTest {

    @Test
    public final void checkSelectorPointcutDefinition() {
        assertEquals("com.qcadoo.mes.technologies.states.aop.TechnologyStateChangeAspect",
                TechnologyStateChangeAspect.class.getCanonicalName());
    }

}
