package com.qcadoo.mes.techSubcontrForDeliveries.aop;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.Test;

import com.qcadoo.mes.deliveries.print.DeliveryColumnFetcher;
import com.qcadoo.mes.deliveries.print.DeliveryProduct;
import com.qcadoo.model.api.Entity;

public class DeliveryColumnFetcherTSFDOverrideAspectTest {

    @Test
    public final void checkContainsOrderedWithProductAlreadyExistsExecution() throws NoSuchMethodException {
        Class<?> clazz = DeliveryColumnFetcher.class;
        assertEquals("com.qcadoo.mes.deliveries.print.DeliveryColumnFetcher", clazz.getCanonicalName());
        final Method method = clazz.getDeclaredMethod("containsOrderedWithProduct", DeliveryProduct.class, Entity.class);
        assertNotNull(method);
        assertTrue(Modifier.isPrivate(method.getModifiers()));
        assertEquals(boolean.class, method.getReturnType());
    }
}
