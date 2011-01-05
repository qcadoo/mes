package com.qcadoo.mes.view.states.components;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.internal.StringType;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ContainerState;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.form.FormComponentState;
import com.qcadoo.mes.view.states.AbstractComponentState;
import com.qcadoo.mes.view.states.AbstractContainerState;
import com.qcadoo.mes.view.states.AbstractStateTest;

public class FormComponentStateTest extends AbstractStateTest {

    private Entity entity;

    private ViewDefinitionState viewDefinitionState;

    private FieldComponentState name;

    private ContainerState form;

    private DataDefinition dataDefinition;

    private FieldDefinition fieldDefinition;

    private TranslationService translationService;

    @Before
    public void init() throws Exception {
        entity = mock(Entity.class);
        given(entity.getField("name")).willReturn("text");

        viewDefinitionState = mock(ViewDefinitionState.class);

        translationService = mock(TranslationService.class);

        fieldDefinition = mock(FieldDefinition.class);
        given(fieldDefinition.getType()).willReturn(new StringType());
        given(fieldDefinition.getName()).willReturn("name");

        dataDefinition = mock(DataDefinition.class, RETURNS_DEEP_STUBS);
        given(dataDefinition.get(12L)).willReturn(null);
        given(dataDefinition.get(13L)).willReturn(entity);
        given(dataDefinition.getPluginIdentifier()).willReturn("plugin");
        given(dataDefinition.getName()).willReturn("name");
        given(dataDefinition.getField("name")).willReturn(fieldDefinition);

        name = new FieldComponentState();
        ((AbstractComponentState) name).setTranslationService(translationService);
        name.setName("name");
        name.initialize(new JSONObject(), Locale.ENGLISH);

        form = new FormComponentState("'static expression'");
        ((AbstractContainerState) form).setDataDefinition(dataDefinition);
        ((AbstractContainerState) form).setTranslationService(translationService);
        ((AbstractContainerState) form).addFieldEntityIdChangeListener("name", name);
        form.initialize(new JSONObject(ImmutableMap.of("components", new JSONObject())), Locale.ENGLISH);
    }

    @Test
    public void shouldInitialeFormWithEntityId() throws Exception {
        // given
        ComponentState componentState = new FormComponentState(null);

        JSONObject json = new JSONObject();
        JSONObject jsonContent = new JSONObject();
        jsonContent.put(FormComponentState.JSON_ENTITY_ID, 13L);
        json.put(ComponentState.JSON_CONTENT, jsonContent);
        JSONObject jsonChildren = new JSONObject();
        json.put(ComponentState.JSON_CHILDREN, jsonChildren);

        // when
        componentState.initialize(json, Locale.ENGLISH);

        // then
        assertEquals(13L, componentState.getFieldValue());
    }

    @Test
    public void shouldInitialeFormWithNullEntityId() throws Exception {
        // given
        ComponentState componentState = new FormComponentState(null);

        JSONObject json = new JSONObject();
        JSONObject jsonContent = new JSONObject();
        jsonContent.put(FormComponentState.JSON_ENTITY_ID, (String) null);
        json.put(ComponentState.JSON_CONTENT, jsonContent);
        JSONObject jsonChildren = new JSONObject();
        json.put(ComponentState.JSON_CHILDREN, jsonChildren);

        // when
        componentState.initialize(json, Locale.ENGLISH);

        // then
        assertNull(componentState.getFieldValue());
    }

    @Test
    public void shouldRenderFormEntityId() throws Exception {
        // given
        TranslationService translationService = mock(TranslationService.class);
        DataDefinition dataDefinition = mock(DataDefinition.class);
        AbstractComponentState componentState = new FormComponentState("2");
        componentState.setTranslationService(translationService);
        componentState.setDataDefinition(dataDefinition);
        componentState.setFieldValue(13L);
        componentState.initialize(new JSONObject(ImmutableMap.of("components", new JSONObject())), Locale.ENGLISH);

        // when
        JSONObject json = componentState.render();

        // then
        assertEquals(13L, json.getJSONObject(ComponentState.JSON_CONTENT).getLong(FormComponentState.JSON_ENTITY_ID));
    }

    @Test
    public void shouldHaveMessageIfEntityIsNotExistsAndEntityIdIsNotNull() throws Exception {
        // given
        form.setFieldValue(12L);
        List<String> codes = Arrays.asList(new String[] { "null.entityNotFound", "core.message.entityNotFound" });
        given(translationService.translate(eq(codes), any(Locale.class))).willReturn("translated entityNotFound");

        // when
        form.performEvent(viewDefinitionState, "initialize", new String[0]);

        // then
        assertFalse(((FormComponentState) form).isValid());
        assertTrue(form.render().toString().contains("translated entityNotFound"));

    }

    @Test
    public void shouldClearFormIfEntityIsNotExistsAndEntityIdIsNull() throws Exception {
        // given
        form.setFieldValue(null);

        // when
        form.performEvent(viewDefinitionState, "initialize", new String[0]);

        // then
        assertNull(form.getFieldValue());
    }

    @Test
    public void shouldResetForm() throws Exception {
        // given
        form.setFieldValue(13L);

        // when
        form.performEvent(viewDefinitionState, "reset", new String[0]);

        // then
        assertEquals("text", name.getFieldValue());
    }

    @Test
    public void shouldClearFormEntity() throws Exception {
        // given
        form.setFieldValue(13L);

        // when
        form.performEvent(viewDefinitionState, "clear", new String[0]);

        // then
        assertNull(name.getFieldValue());
        assertNull(form.getFieldValue());
    }

    @Test
    public void shouldDeleteFormEntity() throws Exception {
        // given
        form.setFieldValue(13L);

        // when
        form.performEvent(viewDefinitionState, "delete", new String[0]);

        // then
        assertNull(name.getFieldValue());
        verify(dataDefinition).delete(13L);
        assertNull(form.getFieldValue());
    }

    @Test
    public void shouldSaveFormEntity() throws Exception {
        // given
        Entity entity = new DefaultEntity("plugin", "name", null, Collections.singletonMap("name", (Object) "text"));
        Entity savedEntity = new DefaultEntity("plugin", "name", 13L, Collections.singletonMap("name", (Object) "text2"));
        given(dataDefinition.save(eq(entity))).willReturn(savedEntity);
        name.setFieldValue("text");

        form.setFieldValue(null);

        // when
        form.performEvent(viewDefinitionState, "save", new String[0]);

        // then
        verify(dataDefinition).save(eq(entity));
        assertEquals("text2", name.getFieldValue());
        assertEquals(13L, form.getFieldValue());
        assertTrue(((FormComponentState) form).isValid());
    }

    @Test
    public void shouldCopyFormEntity() throws Exception {
        // given
        Entity copiedEntity = new DefaultEntity("plugin", "name", 14L, Collections.singletonMap("name", (Object) "text(1)"));
        given(dataDefinition.copy(13L)).willReturn(copiedEntity);
        given(dataDefinition.get(14L)).willReturn(copiedEntity);
        name.setFieldValue("text");
        form.setFieldValue(13L);

        // when
        form.performEvent(viewDefinitionState, "copy", new String[0]);

        // then
        verify(dataDefinition).copy(13L);
        verify(dataDefinition).get(14L);
        assertEquals("text(1)", name.getFieldValue());
        assertEquals(14L, form.getFieldValue());
    }

    @Test
    public void shouldUseContextWhileSaving() throws Exception {
        // given
        Entity entity = new DefaultEntity("plugin", "name", 13L, Collections.singletonMap("name", (Object) "text2"));
        Entity savedEntity = new DefaultEntity("plugin", "name", 13L, Collections.singletonMap("name", (Object) "text2"));
        given(dataDefinition.save(eq(entity))).willReturn(savedEntity);
        given(dataDefinition.getFields().keySet()).willReturn(Collections.singleton("name"));
        name.setFieldValue("text");

        JSONObject json = new JSONObject();
        JSONObject jsonContext = new JSONObject();
        jsonContext.put("id", 14L);
        jsonContext.put("name", "text2");
        JSONObject jsonContent = new JSONObject();
        jsonContent.put(FormComponentState.JSON_ENTITY_ID, 13L);
        json.put(ComponentState.JSON_CONTEXT, jsonContext);
        json.put(ComponentState.JSON_CONTENT, jsonContent);
        json.put(ComponentState.JSON_CHILDREN, new JSONObject());

        form.initialize(json, Locale.ENGLISH);

        // when
        form.performEvent(viewDefinitionState, "save", new String[0]);

        // then
        verify(dataDefinition).save(eq(entity));
        assertEquals("text2", name.getFieldValue());
        assertEquals(13L, form.getFieldValue());
        assertTrue(((FormComponentState) form).isValid());
    }

    @Test
    public void shouldHaveValidationErrors() throws Exception {
        // given
        Entity entity = new DefaultEntity("plugin", "name", null, Collections.singletonMap("name", (Object) "text"));
        Entity savedEntity = new DefaultEntity("plugin", "name", null, Collections.singletonMap("name", (Object) "text2"));
        savedEntity.addGlobalError("global.error");
        savedEntity.addError(fieldDefinition, "field.error");

        given(translationService.translate(eq("global.error"), any(Locale.class))).willReturn("translated global error");
        given(translationService.translate(eq("field.error"), any(Locale.class))).willReturn("translated field error");
        given(dataDefinition.save(eq(entity))).willReturn(savedEntity);
        name.setFieldValue("text");

        form.setFieldValue(null);

        // when
        form.performEvent(viewDefinitionState, "save", new String[0]);

        // then
        verify(dataDefinition).save(eq(entity));
        assertFalse(((FormComponentState) form).isValid());
        assertTrue(form.render().toString().contains("translated global error"));
    }
}
