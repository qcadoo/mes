package com.qcadoo.mes.assignmentToShift.states.aop;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AssignmentToShiftStateChangeAspectTest {

    @Test
    public final void checkSelectorPointcutDefinition() throws NoSuchMethodException {
        final Class<?> clazz = AssignmentToShiftStateChangeAspect.class;
        assertEquals("com.qcadoo.mes.assignmentToShift.states.aop.AssignmentToShiftStateChangeAspect", clazz.getCanonicalName());
    }

}
