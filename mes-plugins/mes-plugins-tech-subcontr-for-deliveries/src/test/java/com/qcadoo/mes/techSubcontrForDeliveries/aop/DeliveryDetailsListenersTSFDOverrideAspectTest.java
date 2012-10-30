package com.qcadoo.mes.techSubcontrForDeliveries.aop;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.Test;

import com.qcadoo.mes.deliveries.listeners.DeliveryDetailsListeners;
import com.qcadoo.model.api.Entity;

public class DeliveryDetailsListenersTSFDOverrideAspectTest {

    @Test
    public final void checkValidateTransferResourcesPointcutDefinition() throws NoSuchMethodException {
        Class<?> clazz = DeliveryDetailsListeners.class;
        assertEquals("com.qcadoo.mes.deliveries.listeners.DeliveryDetailsListeners", clazz.getCanonicalName());
        final Method method = clazz.getDeclaredMethod("copyOrderedProductToDelivered", Entity.class, List.class);
        assertNotNull(method);
        assertTrue(Modifier.isPrivate(method.getModifiers()));
        assertEquals(Entity.class, method.getReturnType());
    }
}
