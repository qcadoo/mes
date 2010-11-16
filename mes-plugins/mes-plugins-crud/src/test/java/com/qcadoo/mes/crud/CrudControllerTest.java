/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.crud;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.view.Component;
import com.qcadoo.mes.view.SaveableComponent;
import com.qcadoo.mes.view.SelectableComponent;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.ViewValue;

public class CrudControllerTest {

    private CrudController crudController = null;

    private ViewDefinitionService viewDefinitionService = null;

    private ViewDefinition viewDefinition = null;

    private TranslationService translationService = null;

    @Before
    public void init() {
        viewDefinitionService = mock(ViewDefinitionService.class);

        translationService = mock(TranslationService.class);

        viewDefinition = mock(ViewDefinition.class, RETURNS_DEEP_STUBS);

        crudController = new CrudController();
        ReflectionTestUtils.setField(crudController, "viewDefinitionService", viewDefinitionService);
        ReflectionTestUtils.setField(crudController, "translationService", translationService);

        Map<String, String> translations = new HashMap<String, String>();
        translations.put("commons.message1", "test1");
        translations.put("commons.message2", "test2");

        given(translationService.getCommonsMessages(Locale.ENGLISH)).willReturn(translations);
        given(
                translationService.translate(
                        Arrays.asList(new String[] { "pluginName.viewName.trigger.component.saveMessage", "core.message.save" }),
                        Locale.ENGLISH)).willReturn("saved");
        given(
                translationService.translate(
                        Arrays.asList(new String[] { "pluginName.viewName.trigger.deleteMessage", "core.message.delete" }),
                        Locale.ENGLISH)).willReturn("deleted");
        given(
                translationService.translate(
                        Arrays.asList(new String[] { "pluginName.viewName.trigger.moveMessage", "core.message.move" }),
                        Locale.ENGLISH)).willReturn("moved");
        given(viewDefinitionService.get("pluginName", "viewName")).willReturn(viewDefinition);

        doAnswer(new Answer<Object>() {

            @Override
            @SuppressWarnings("unchecked")
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                Map<String, String> translations = (Map<String, String>) invocation.getArguments()[0];
                translations.put("plugin.message", "test3");
                return null;
            }

        }).when(viewDefinition).updateTranslations(translations, Locale.ENGLISH);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldGetView() throws Exception {
        // given
        Map<String, String> arguments = new HashMap<String, String>();
        arguments.put("entityId", "1");
        arguments.put("context", "test");
        arguments.put("message", "hello world");
        arguments.put("messageType", "error");

        // when
        ModelAndView modelAndView = crudController.getView("pluginName", "viewName", arguments, Locale.ENGLISH);

        // then
        assertEquals("crud/crudView", modelAndView.getViewName());
        assertEquals(viewDefinition, modelAndView.getModel().get("viewDefinition"));
        assertEquals("1", modelAndView.getModel().get("entityId"));
        assertEquals("test", modelAndView.getModel().get("context"));
        assertEquals("hello world", modelAndView.getModel().get("message"));
        assertEquals("error", modelAndView.getModel().get("messageType"));

        Map<String, String> translation = (Map<String, String>) modelAndView.getModel().get("translationsMap");

        assertEquals("test1", translation.get("commons.message1"));
        assertEquals("test2", translation.get("commons.message2"));
        assertEquals("test3", translation.get("plugin.message"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldGetViewWithoutAnyArguments() throws Exception {
        // given
        Map<String, String> arguments = new HashMap<String, String>();

        // when
        ModelAndView modelAndView = crudController.getView("pluginName", "viewName", arguments, Locale.ENGLISH);

        // then
        assertEquals("crud/crudView", modelAndView.getViewName());
        assertEquals(viewDefinition, modelAndView.getModel().get("viewDefinition"));
        assertNull(modelAndView.getModel().get("entityId"));
        assertNull(modelAndView.getModel().get("contextEntityId"));
        assertNull(modelAndView.getModel().get("contextFieldName"));
        assertFalse(modelAndView.getModel().containsKey("message"));
        assertFalse(modelAndView.getModel().containsKey("messageType"));

        Map<String, String> translation = (Map<String, String>) modelAndView.getModel().get("translationsMap");

        assertEquals("test1", translation.get("commons.message1"));
        assertEquals("test2", translation.get("commons.message2"));
        assertEquals("test3", translation.get("plugin.message"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldGetViewWithMessage() throws Exception {
        // given
        Map<String, String> arguments = new HashMap<String, String>();
        arguments.put("message", "hello world");

        // when
        ModelAndView modelAndView = crudController.getView("pluginName", "viewName", arguments, Locale.ENGLISH);

        // then
        assertEquals("crud/crudView", modelAndView.getViewName());
        assertEquals(viewDefinition, modelAndView.getModel().get("viewDefinition"));
        assertNull(modelAndView.getModel().get("entityId"));
        assertNull(modelAndView.getModel().get("contextEntityId"));
        assertNull(modelAndView.getModel().get("contextFieldName"));
        assertEquals("hello world", modelAndView.getModel().get("message"));
        assertEquals("info", modelAndView.getModel().get("messageType"));

        Map<String, String> translation = (Map<String, String>) modelAndView.getModel().get("translationsMap");

        assertEquals("test1", translation.get("commons.message1"));
        assertEquals("test2", translation.get("commons.message2"));
        assertEquals("test3", translation.get("plugin.message"));
    }

    @Test
    public void shouldGetDataWithoutEntityId() throws Exception {
        // given
        ViewValue<Long> expectedViewValue = new ViewValue<Long>(118L);

        given(viewDefinition.getValue(null, Collections.<String, Entity> emptyMap(), null, "", false, Locale.ENGLISH))
                .willReturn(expectedViewValue);

        // when
        Object viewValue = crudController.getData("pluginName", "viewName", null, Locale.ENGLISH);

        // then
        assertEquals(expectedViewValue, viewValue);
    }

    @Test
    public void shouldGetDataWithEntityId() throws Exception {
        // given
        JSONObject json = new JSONObject();
        json.put("entityId", "11");

        Entity entity = new DefaultEntity("", "", 1L);

        ViewValue<Long> expectedViewValue = new ViewValue<Long>(117L);

        given(viewDefinition.getValue(entity, Collections.<String, Entity> emptyMap(), null, null, false, Locale.ENGLISH))
                .willReturn(expectedViewValue);

        given(viewDefinition.getDataDefinition().get(11L)).willReturn(entity);

        // when
        Object viewValue = crudController.getData("pluginName", "viewName", new StringBuilder(json.toString()), Locale.ENGLISH);

        // then
        assertEquals(expectedViewValue, viewValue);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldGetDataUpdate() throws Exception {
        // given
        JSONObject json = new JSONObject();
        json.put("componentName", "trigger-component");
        json.put("data", new JSONObject());

        ViewValue<Long> oldViewValue = new ViewValue<Long>(221L);
        ViewValue<Long> newViewValue = new ViewValue<Long>(116L);

        given(viewDefinition.castValue(anyMap(), any(JSONObject.class))).willReturn(oldViewValue);
        given(
                viewDefinition.getValue(null, Collections.<String, Entity> emptyMap(), oldViewValue, "trigger.component", false,
                        Locale.ENGLISH)).willReturn(newViewValue);

        // when
        Object viewValue = crudController.getData("pluginName", "viewName", new StringBuilder(json.toString()), Locale.ENGLISH);

        // then
        assertEquals(newViewValue, viewValue);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfJsonIsInvalid() throws Exception {
        // when
        crudController.getData("pluginName", "viewName", new StringBuilder("{ sss"), Locale.ENGLISH);
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void shouldPerformSave() throws Exception {
        // given
        SaveableComponent component = mock(SaveableComponent.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS)
                .extraInterfaces(Component.class));

        Entity entity = mock(Entity.class);

        JSONObject json = new JSONObject();
        json.put("componentName", "trigger-component");
        json.put("data", new JSONObject());

        ViewValue<Long> oldViewValue = new ViewValue<Long>(221L);
        ViewValue<Long> newViewValue = new ViewValue<Long>(115L);
        newViewValue.addComponent("root", new ViewValue<Object>("root"));

        given(viewDefinition.castValue(anyMap(), any(JSONObject.class))).willReturn(oldViewValue);
        given(viewDefinition.getRoot().getName()).willReturn("root");
        given(viewDefinition.lookupComponent("trigger.component")).willReturn((Component) component);
        given(
                viewDefinition.getValue(entity, ImmutableMap.of("trigger.component", entity), oldViewValue, "trigger.component",
                        true, Locale.ENGLISH)).willReturn(newViewValue);
        given(component.getSaveableEntity(oldViewValue)).willReturn(entity);
        given(((Component<?>) component).getDataDefinition().save(entity)).willReturn(entity);

        // when
        Object viewValue = crudController.performSave("pluginName", "viewName", new StringBuilder(json.toString()),
                Locale.ENGLISH);

        // then
        assertEquals(newViewValue, viewValue);
        verify(((Component<?>) component).getDataDefinition()).save(entity);
        verify(entity, never()).setField(anyString(), anyLong());
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void shouldPerformSaveWithContextId() throws Exception {
        // given
        SaveableComponent component = mock(SaveableComponent.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS)
                .extraInterfaces(Component.class));

        Entity entity = mock(Entity.class);

        JSONObject json = new JSONObject();
        json.put("componentName", "trigger-component");
        JSONArray contextArr = new JSONArray();
        JSONObject contextObj = new JSONObject();
        contextObj.put("fieldName", "testField");
        contextObj.put("entityId", "44");
        contextArr.put(contextObj);
        json.put("context", contextArr);
        json.put("data", new JSONObject());

        ViewValue<Long> oldViewValue = new ViewValue<Long>(222L);
        ViewValue<Long> newViewValue = new ViewValue<Long>(114L);
        newViewValue.addComponent("root", new ViewValue<Object>("root"));

        given(entity.isValid()).willReturn(true);
        given(viewDefinition.getRoot().getName()).willReturn("root");
        given(viewDefinition.castValue(anyMap(), any(JSONObject.class))).willReturn(oldViewValue);
        given(viewDefinition.lookupComponent("trigger.component")).willReturn((Component) component);
        given(
                viewDefinition.getValue(entity, ImmutableMap.of("trigger.component", entity), oldViewValue, "trigger.component",
                        true, Locale.ENGLISH)).willReturn(newViewValue);
        given(component.getSaveableEntity(oldViewValue)).willReturn(entity);
        given(((Component<?>) component).getDataDefinition().save(entity)).willReturn(entity);
        given(((Component<?>) component).isRelatedToMainEntity()).willReturn(true);

        // when
        ViewValue<Object> viewValue = (ViewValue<Object>) crudController.performSave("pluginName", "viewName", new StringBuilder(
                json.toString()), Locale.ENGLISH);

        // then
        assertEquals(newViewValue, viewValue);
        assertEquals(1, viewValue.getSuccessMessages().size());
        assertEquals("saved", viewValue.getSuccessMessages().get(0));
        verify(((Component<?>) component).getDataDefinition()).save(entity);
        verify(entity).setField("testField", 44L);
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void shouldPerformDelete() throws Exception {
        // given
        SelectableComponent component = mock(SelectableComponent.class, Mockito.withSettings().extraInterfaces(Component.class)
                .defaultAnswer(Mockito.RETURNS_DEEP_STUBS));

        Entity entity = mock(Entity.class);

        JSONObject json = new JSONObject();
        json.put("componentName", "trigger");
        json.put("data", new JSONObject());

        final ViewValue<Long> oldViewValue = new ViewValue<Long>(223L);
        oldViewValue.addComponent("trigger", new ViewValue(12L));

        ViewValue<Long> newViewValue = new ViewValue<Long>(113L);
        newViewValue.addComponent("root", new ViewValue<Object>("root"));

        given(viewDefinition.getRoot().getName()).willReturn("root");
        given(viewDefinition.castValue(anyMap(), any(JSONObject.class))).willAnswer(new Answer<ViewValue<Long>>() {

            @Override
            public ViewValue<Long> answer(final InvocationOnMock invocation) throws Throwable {
                Map<String, Entity> selectedEntities = (Map<String, Entity>) invocation.getArguments()[0];
                selectedEntities.put("trigger", new DefaultEntity("", "", 11L));
                return oldViewValue;
            }

        });

        given(viewDefinition.lookupComponent("trigger")).willReturn((Component) component);
        given(
                viewDefinition.getValue(null, Collections.<String, Entity> emptyMap(), oldViewValue, "trigger", true,
                        Locale.ENGLISH)).willReturn(newViewValue);
        given(component.getSelectedEntityId(oldViewValue)).willReturn(12L);

        // when
        ViewValue<Object> viewValue = (ViewValue<Object>) crudController.performDelete("pluginName", "viewName",
                new StringBuilder(json.toString()), Locale.ENGLISH);

        // then
        assertEquals(newViewValue, viewValue);
        assertEquals(1, viewValue.getSuccessMessages().size());
        assertEquals("deleted", viewValue.getSuccessMessages().get(0));
        verify(((Component) component).getDataDefinition()).delete(12L);
        verify(entity, never()).setField(anyString(), anyLong());
    }

    // @Test
    // @SuppressWarnings({ "rawtypes", "unchecked" })
    // public void shouldPerformMove() throws Exception {
    // // given
    // SelectableComponent component = mock(SelectableComponent.class, Mockito.withSettings().extraInterfaces(Component.class)
    // .defaultAnswer(Mockito.RETURNS_DEEP_STUBS));
    //
    // Entity entity = mock(Entity.class);
    //
    // JSONObject json = new JSONObject();
    // json.put("componentName", "trigger");
    // json.put("offset", "1");
    // json.put("data", new JSONObject());
    //
    // final ViewValue<Object> oldViewValue = new ViewValue<Object>("test");
    // oldViewValue.addComponent("trigger", new ViewValue(12L));
    //
    // ViewValue<Object> newViewValue = new ViewValue<Object>("test");
    // newViewValue.addComponent("root", new ViewValue<Object>("root"));
    //
    // given(viewDefinition.getRoot().getName()).willReturn("root");
    // given(viewDefinition.castValue(anyMap(), any(JSONObject.class))).willReturn(oldViewValue);
    // given(viewDefinition.lookupComponent("trigger")).willReturn((Component) component);
    // given(
    // viewDefinition.getValue(null, Collections.<String, Entity> emptyMap(), oldViewValue, "trigger", true,
    // Locale.ENGLISH)).willReturn(newViewValue);
    // given(component.getSelectedEntityId(oldViewValue)).willReturn(12L);
    //
    // // when
    // ViewValue<Object> viewValue = (ViewValue<Object>) crudController.performMove("pluginName", "viewName", new StringBuilder(
    // json.toString()), Locale.ENGLISH);
    //
    // // then
    // assertEquals(newViewValue, viewValue);
    // assertEquals(1, viewValue.getSuccessMessages().size());
    // assertEquals("moved", viewValue.getSuccessMessages().get(0));
    // verify(((Component) component).getDataDefinition()).move(12L, 1);
    // verify(entity, never()).setField(anyString(), anyLong());
    // }

}
