package com.qcadoo.mes.techSubcontracting.aop;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.qcadoo.mes.technologies.ProductQuantitiesServiceImpl;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;

public class MrpAlgorithmStrategyResolverAspectTest {

    @Test
    public final void checGetProductsPointcutDefinition() throws NoSuchMethodException {
        Class<?> clazz = ProductQuantitiesServiceImpl.class;
        assertEquals("com.qcadoo.mes.technologies.ProductQuantitiesServiceImpl", clazz.getCanonicalName());
        final Method method = clazz.getDeclaredMethod("getProducts", Map.class, Set.class, MrpAlgorithm.class, String.class);
        assertNotNull(method);
        assertTrue(Modifier.isPrivate(method.getModifiers()));
        assertEquals(Map.class, method.getReturnType());
    }
}
