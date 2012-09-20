package com.qcadoo.mes.techSubcontrForOperTasks.aop;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.Test;

import com.qcadoo.mes.operationalTasksForOrders.listeners.OperationalTasksDetailsListenersOTFO;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public class OperationalTasksDetailsListenersOTFOOverrideAspectTest {

    @Test
    public final void checkSetProductionLineFromOrderPointcutDefinition() throws NoSuchMethodException {
        Class<?> clazz = OperationalTasksDetailsListenersOTFO.class;
        assertEquals("com.qcadoo.mes.operationalTasksForOrders.listeners.OperationalTasksDetailsListenersOTFO",
                clazz.getCanonicalName());
        final Method method = clazz.getDeclaredMethod("setProductionLineFromOrderAndClearOperation", ViewDefinitionState.class,
                ComponentState.class, String[].class);
        assertNotNull(method);
        assertTrue(Modifier.isPublic(method.getModifiers()));
    }

    @Test
    public final void checkSetOperationalNameAndDescriptionPointcutDefinition() throws NoSuchMethodException {
        Class<?> clazz = OperationalTasksDetailsListenersOTFO.class;
        assertEquals("com.qcadoo.mes.operationalTasksForOrders.listeners.OperationalTasksDetailsListenersOTFO",
                clazz.getCanonicalName());
        final Method method = clazz.getDeclaredMethod("setOperationalNameAndDescription", ViewDefinitionState.class,
                ComponentState.class, String[].class);
        assertNotNull(method);
        assertTrue(Modifier.isPublic(method.getModifiers()));
    }
}
