package com.qcadoo.mes.view;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.internal.DefaultEntity;

public class AbstractContainerComponentTest {

    @Test
    public void shouldHaveComponents() throws Exception {
        // given
        Component<?> child1 = mock(Component.class);
        given(child1.getName()).willReturn("child1Name");

        Component<?> child2 = mock(Component.class);
        given(child2.getName()).willReturn("child2Name");

        CustomAbstractContainerComponent container = new CustomAbstractContainerComponent("test", null, null, null, null);
        container.addComponent(child1);
        container.addComponent(child2);

        // then
        assertEquals(2, container.getComponents().size());
        assertEquals(child1, container.getComponents().get("child1Name"));
        assertEquals(child2, container.getComponents().get("child2Name"));
    }

    @Test
    public void shouldBeAContainer() throws Exception {
        // given
        CustomAbstractContainerComponent container = new CustomAbstractContainerComponent("test", null, null, null, null);

        // then
        Assert.assertTrue(container.isContainer());
    }

    @Test
    public void shouldUpdateContainerTranslations() throws Exception {
        // given
        Component<?> child1 = mock(Component.class);
        given(child1.getName()).willReturn("child1Name");

        Component<?> child2 = mock(Component.class);
        given(child2.getName()).willReturn("child2Name");

        CustomAbstractContainerComponent container = spy(new CustomAbstractContainerComponent("test", null, null, null, null));
        container.addComponent(child1);
        container.addComponent(child2);

        Map<String, String> translations = new HashMap<String, String>();
        // given

        // when
        container.updateTranslations(translations, Locale.ENGLISH);

        // then
        verify(container).addComponentTranslations(translations, Locale.ENGLISH);
        verify(child1).updateTranslations(translations, Locale.ENGLISH);
        verify(child2).updateTranslations(translations, Locale.ENGLISH);
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void shouldCastContainerValue() throws Exception {
        // given
        Component<?> child1 = mock(Component.class);
        given(child1.getName()).willReturn("child1Name");

        Component<?> child2 = mock(Component.class);
        given(child2.getName()).willReturn("child2Name");

        CustomAbstractContainerComponent container = spy(new CustomAbstractContainerComponent("test", null, null, null, null));
        container.addComponent(child1);
        container.addComponent(child2);

        Map<String, Entity> selectedEntities = new HashMap<String, Entity>();

        JSONObject jsonTestObject = new JSONObject();
        JSONObject jsonComponentsObject = new JSONObject();
        JSONObject jsonChild1Object = new JSONObject();
        JSONObject jsonChild2Object = new JSONObject();
        jsonTestObject.put("components", jsonComponentsObject);
        jsonComponentsObject.put("child1Name", jsonChild1Object);
        jsonComponentsObject.put("child2Name", jsonChild2Object);
        jsonTestObject.put("value", "testValue");
        jsonTestObject.put("enabled", true);
        jsonTestObject.put("visible", true);
        jsonChild1Object.put("value", "child1Value");
        jsonChild1Object.put("enabled", true);
        jsonChild1Object.put("visible", true);
        jsonChild2Object.put("value", "");
        jsonChild2Object.put("enabled", true);
        jsonChild2Object.put("visible", true);

        given(child1.castValue(selectedEntities, jsonChild1Object)).willReturn(new ViewValue("valueChild1"));
        given(child2.castValue(selectedEntities, jsonChild2Object)).willReturn(new ViewValue("valueChild2"));
        given(container.castContainerValue(selectedEntities, jsonTestObject)).willReturn("valueTest");
        // given

        // when
        ViewValue<Object> value = container.castValue(selectedEntities, jsonTestObject);

        // then
        verify(child1).castValue(selectedEntities, jsonChild1Object);
        verify(child2).castValue(selectedEntities, jsonChild2Object);
        verify(container).castContainerValue(selectedEntities, jsonTestObject);

        assertNotNull(value.getValue());
        assertNotNull(value.getComponent("child1Name"));
        assertNotNull(value.getComponent("child2Name"));
        assertNotNull(value.getComponent("child1Name").getValue());
        assertNotNull(value.getComponent("child2Name").getValue());
        assertEquals(2, value.getComponents().size());
        assertEquals(0, value.getComponent("child1Name").getComponents().size());
        assertEquals(0, value.getComponent("child2Name").getComponents().size());
        assertEquals("valueTest", value.getValue());
        assertEquals("valueChild1", value.getComponent("child1Name").getValue());
        assertEquals("valueChild2", value.getComponent("child2Name").getValue());
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void shouldCastNullContainerValue() throws Exception {
        // given
        Component<?> child1 = mock(Component.class);
        given(child1.getName()).willReturn("child1Name");

        Component<?> child2 = mock(Component.class);
        given(child2.getName()).willReturn("child2Name");

        CustomAbstractContainerComponent container = spy(new CustomAbstractContainerComponent("test", null, null, null, null));
        container.addComponent(child1);
        container.addComponent(child2);

        Map<String, Entity> selectedEntities = new HashMap<String, Entity>();

        given(child1.castValue(selectedEntities, null)).willReturn(new ViewValue("valueChild1"));
        given(child2.castValue(selectedEntities, null)).willReturn(new ViewValue("valueChild2"));
        given(container.castContainerValue(selectedEntities, null)).willReturn("valueTest");
        // given

        // when
        ViewValue<Object> value = container.castValue(selectedEntities, null);

        // then
        verify(child1).castValue(selectedEntities, null);
        verify(child2).castValue(selectedEntities, null);
        verify(container).castContainerValue(selectedEntities, null);
        assertNotNull(value.getValue());
        assertNotNull(value.getComponent("child1Name").getValue());
        assertNotNull(value.getComponent("child2Name").getValue());
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void shouldGetContainerValue() throws Exception {
        // given
        Component<?> child1 = mock(Component.class);
        given(child1.getName()).willReturn("child1Name");

        Component<?> child2 = mock(Component.class);
        given(child2.getName()).willReturn("child2Name");

        CustomAbstractContainerComponent container = spy(new CustomAbstractContainerComponent("test", null, null, null, null));
        container.addComponent(child1);
        container.addComponent(child2);

        Map<String, Entity> selectedEntities = new HashMap<String, Entity>();
        Set<String> pathsToUpdate = new HashSet<String>();

        Entity entity = new DefaultEntity(1L);

        ViewValue<Object> viewValue = new ViewValue<Object>();
        ViewValue<Object> viewChild1Value = new ViewValue<Object>();
        ViewValue<Object> viewChild2Value = new ViewValue<Object>();
        viewValue.addComponent("child1Name", viewChild1Value);
        viewValue.addComponent("child2Name", viewChild2Value);

        given(container.getContainerValue(entity, selectedEntities, viewValue, pathsToUpdate)).willReturn("testValue");
        given(child1.getValue(entity, selectedEntities, viewChild1Value, pathsToUpdate)).willReturn(new ViewValue("child1Value"));
        given(child2.getValue(entity, selectedEntities, viewChild2Value, pathsToUpdate)).willReturn(new ViewValue("child2Value"));

        // when
        ViewValue<Object> newViewValue = container.getValue(entity, selectedEntities, viewValue, pathsToUpdate);

        // then
        verify(container).addContainerMessages(entity, newViewValue);
        verify(container).getContainerValue(entity, selectedEntities, viewValue, pathsToUpdate);
        verify(child1).getValue(entity, selectedEntities, viewChild1Value, pathsToUpdate);
        verify(child2).getValue(entity, selectedEntities, viewChild2Value, pathsToUpdate);
        assertEquals("testValue", newViewValue.getValue());
        assertEquals(2, newViewValue.getComponents().size());
        assertEquals("child1Value", newViewValue.getComponent("child1Name").getValue());
        assertEquals(0, newViewValue.getComponent("child1Name").getComponents().size());
        assertEquals("child2Value", newViewValue.getComponent("child2Name").getValue());
        assertEquals(0, newViewValue.getComponent("child2Name").getComponents().size());
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void shouldGetNullContainerValue() throws Exception {
        // given
        Component<?> child1 = mock(Component.class);
        given(child1.getName()).willReturn("child1Name");

        Component<?> child2 = mock(Component.class);
        given(child2.getName()).willReturn("child2Name");

        CustomAbstractContainerComponent container = spy(new CustomAbstractContainerComponent("test", null, null, null, null));
        container.addComponent(child1);
        container.addComponent(child2);

        Map<String, Entity> selectedEntities = new HashMap<String, Entity>();
        Set<String> pathsToUpdate = new HashSet<String>();

        Entity entity = new DefaultEntity(1L);

        given(container.getContainerValue(entity, selectedEntities, null, pathsToUpdate)).willReturn(null);
        given(child1.getValue(entity, selectedEntities, null, pathsToUpdate)).willReturn(new ViewValue("child1Value"));
        given(child2.getValue(entity, selectedEntities, null, pathsToUpdate)).willReturn(new ViewValue("child2Value"));

        // when
        ViewValue<Object> newViewValue = container.getValue(entity, selectedEntities, null, pathsToUpdate);

        // then
        verify(container).addContainerMessages(entity, newViewValue);
        verify(container).getContainerValue(entity, selectedEntities, null, pathsToUpdate);
        verify(child1).getValue(entity, selectedEntities, null, pathsToUpdate);
        verify(child2).getValue(entity, selectedEntities, null, pathsToUpdate);
        assertEquals(null, newViewValue.getValue());
        assertEquals("child1Value", newViewValue.getComponent("child1Name").getValue());
        assertEquals("child2Value", newViewValue.getComponent("child2Name").getValue());
    }

    @Test
    public void shouldHasEmptyValueIfNothingHasChanged() throws Exception {
        // given
        Component<?> child1 = mock(Component.class);
        given(child1.getName()).willReturn("child1Name");

        Component<?> child2 = mock(Component.class);
        given(child2.getName()).willReturn("child2Name");

        CustomAbstractContainerComponent container = spy(new CustomAbstractContainerComponent("test", null, null, null, null));
        container.addComponent(child1);
        container.addComponent(child2);

        Map<String, Entity> selectedEntities = new HashMap<String, Entity>();
        Set<String> pathsToUpdate = new HashSet<String>();

        Entity entity = new DefaultEntity(1L);

        ViewValue<Object> viewValue = new ViewValue<Object>();
        ViewValue<Object> viewChild1Value = new ViewValue<Object>();
        ViewValue<Object> viewChild2Value = new ViewValue<Object>();
        viewValue.addComponent("child1Name", viewChild1Value);
        viewValue.addComponent("child2Name", viewChild2Value);

        given(container.getContainerValue(entity, selectedEntities, viewValue, pathsToUpdate)).willReturn(null);
        given(child1.getValue(entity, selectedEntities, viewChild1Value, pathsToUpdate)).willReturn(null);
        given(child2.getValue(entity, selectedEntities, viewChild2Value, pathsToUpdate)).willReturn(null);

        // when
        ViewValue<Object> newViewValue = container.getValue(entity, selectedEntities, viewValue, pathsToUpdate);

        // then
        assertNull(newViewValue);
    }

    private static class CustomAbstractContainerComponent extends AbstractContainerComponent<Object> {

        public CustomAbstractContainerComponent(final String name, final ContainerComponent<?> parentContainer,
                final String fieldPath, final String sourceFieldPath, final TranslationService translationService) {
            super(name, parentContainer, fieldPath, sourceFieldPath, translationService);
        }

        @Override
        public String getType() {
            return null;
        }

        @Override
        public Object castContainerValue(final Map<String, Entity> selectedEntities, final JSONObject viewObject)
                throws JSONException {
            return null;
        }

        @Override
        public Object getContainerValue(final Entity entity, final Map<String, Entity> selectedEntities,
                final ViewValue<Object> viewValue, final Set<String> pathsToUpdate) {
            return null;
        }

        @Override
        public void addContainerMessages(final Entity entity, final ViewValue<Object> viewValue) {
        }

        @Override
        public void addComponentTranslations(final Map<String, String> translationsMap, final Locale locale) {
        }

    }

}
