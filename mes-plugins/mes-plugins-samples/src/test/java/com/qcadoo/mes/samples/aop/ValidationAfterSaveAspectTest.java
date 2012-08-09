package com.qcadoo.mes.samples.aop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;

import org.junit.Test;

import com.qcadoo.mes.samples.api.SamplesLoader;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class ValidationAfterSaveAspectTest {

    @Test
    public final void checkDataDefinitionSaveCallPointcutDefinition() throws NoSuchMethodException {
        final Class<?> clazz = DataDefinition.class;
        assertEquals("com.qcadoo.model.api.DataDefinition", clazz.getCanonicalName());
        final Method method = clazz.getDeclaredMethod("save", Entity.class);
        assertEquals("com.qcadoo.model.api.Entity", Entity.class.getCanonicalName());
        assertNotNull(method);
        assertEquals("com.qcadoo.mes.samples.api.SamplesLoader", SamplesLoader.class.getCanonicalName());
    }

}
