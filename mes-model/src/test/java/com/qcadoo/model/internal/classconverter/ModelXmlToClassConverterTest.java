package com.qcadoo.model.internal.classconverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.qcadoo.model.Utils;
import com.qcadoo.model.internal.api.ModelXmlToClassConverter;

public class ModelXmlToClassConverterTest {

    private final static ModelXmlToClassConverter modelXmlToClassConverter = new ModelXmlToClassConverterImpl();

    private static Map<String, Class<?>> classes = new HashMap<String, Class<?>>();

    private static Map<String, PropertyDescriptor> propertyDescriptors = new HashMap<String, PropertyDescriptor>();

    @BeforeClass
    public static void init() throws Exception {
        for (Class<?> clazz : modelXmlToClassConverter.convert(Utils.MODEL_XML_RESOURCE, Utils.OTHER_XML_RESOURCE)) {
            classes.put(clazz.getCanonicalName(), clazz);
        }

        for (PropertyDescriptor propertyDescriptor : PropertyUtils.getPropertyDescriptors(classes
                .get("com.qcadoo.model.beans.FullFirstEntity"))) {
            propertyDescriptors.put(propertyDescriptor.getName(), propertyDescriptor);
        }
    }

    @Test
    public void shouldDefineClasses() throws Exception {
        assertEquals(5, classes.size());
    }

    @Test
    public void shouldHaveProperName() throws Exception {
        assertNotNull(classes.get("com.qcadoo.model.beans.FullFirstEntity"));
        assertNotNull(classes.get("com.qcadoo.model.beans.FullSecondEntity"));
        assertNotNull(classes.get("com.qcadoo.model.beans.FullThirdEntity"));
        assertNotNull(classes.get("com.qcadoo.model.beans.OtherFirstEntity"));
        assertNotNull(classes.get("com.qcadoo.model.beans.OtherSecondEntity"));
    }

    @Test
    public void shouldDefineIdentifier() {
        verifyField(propertyDescriptors.get("id"), Long.class);
    }

    private void verifyField(final PropertyDescriptor propertyDescriptor, final Class<?> type) {
        verifyField(propertyDescriptor, type, true, true);
    }

    private void verifyField(final PropertyDescriptor propertyDescriptor, final Class<?> type, final boolean readable,
            final boolean writable) {
        assertEquals(type, propertyDescriptor.getPropertyType());
        if(writable) {
            assertNotNull(propertyDescriptor.getWriteMethod());
        } else {
            assertNull(propertyDescriptor.getWriteMethod());
        }
        if(readable) {
            assertNotNull(propertyDescriptor.getReadMethod());
        } else {
            assertNull(propertyDescriptor.getReadMethod());
        }
    }
}
