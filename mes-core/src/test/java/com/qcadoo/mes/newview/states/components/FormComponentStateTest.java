package com.qcadoo.mes.newview.states.components;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Locale;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.internal.StringType;
import com.qcadoo.mes.newview.AbstractContainerState;
import com.qcadoo.mes.newview.ComponentState;
import com.qcadoo.mes.newview.ContainerState;
import com.qcadoo.mes.newview.FieldEntityIdChangeListener;
import com.qcadoo.mes.newview.components.FormComponentState;
import com.qcadoo.mes.newview.states.AbstractStateTest;

public class FormComponentStateTest extends AbstractStateTest {

    private Entity entity;

    private ComponentState name;

    private ContainerState form;

    private DataDefinition dataDefinition;

    private FieldDefinition fieldDefinition;

    private TranslationService translationService;

    @Before
    public void init() {
        entity = mock(Entity.class);
        given(entity.getField("name")).willReturn("text");

        translationService = mock(TranslationService.class);

        fieldDefinition = mock(FieldDefinition.class);
        given(fieldDefinition.getType()).willReturn(new StringType());
        given(fieldDefinition.getName()).willReturn("name");

        dataDefinition = mock(DataDefinition.class);
        given(dataDefinition.get(13L)).willReturn(entity);
        given(dataDefinition.getPluginIdentifier()).willReturn("plugin");
        given(dataDefinition.getName()).willReturn("name");
        given(dataDefinition.getField("name")).willReturn(fieldDefinition);

        name = createMockComponent("name");

        form = new FormComponentState();
        ((AbstractContainerState) form).setDataDefinition(dataDefinition);
        ((AbstractContainerState) form).setTranslationService(translationService);
        ((AbstractContainerState) form).addFieldEntityIdChangeListener("name", (FieldEntityIdChangeListener) name);
    }

    @Test
    public void shouldInitialeFormWithEntityId() throws Exception {
        // given
        ComponentState componentState = new FormComponentState();

        JSONObject json = new JSONObject();
        JSONObject jsonContent = new JSONObject();
        jsonContent.put(ComponentState.JSON_VALUE, 13L);
        json.put(ComponentState.JSON_CONTENT, jsonContent);
        JSONObject jsonChildren = new JSONObject();
        json.put(ComponentState.JSON_CHILDREN, jsonChildren);

        // when
        componentState.initialize(json, Locale.ENGLISH);

        // then
        assertEquals(13L, componentState.getFieldValue());
    }

    @Test
    public void shouldRenderFormEntityId() throws Exception {
        // given
        ComponentState componentState = new FormComponentState();
        componentState.setFieldValue(13L);

        // when
        JSONObject json = componentState.render();

        // then
        assertEquals(13L, json.getJSONObject(ComponentState.JSON_CONTENT).getLong(ComponentState.JSON_VALUE));
    }

    @Test
    public void shouldClearFormIfEntityIsNotExists() throws Exception {
        // given
        form.setFieldValue(12L);

        // when
        form.performEvent("initialize", new String[0]);

        // then
        assertNull(form.getFieldValue());
    }

    @Test
    public void shouldResetForm() throws Exception {
        // given
        form.setFieldValue(13L);

        // when
        form.performEvent("reset", new String[0]);

        // then
        verify(name).setFieldValue("text");
    }

    @Test
    public void shouldClearFormEntity() throws Exception {
        // given
        form.setFieldValue(13L);

        // when
        form.performEvent("clear", new String[0]);

        // then
        verify(name).setFieldValue(null);
        assertNull(form.getFieldValue());
    }

    @Test
    public void shouldDeleteFormEntity() throws Exception {
        // given
        form.setFieldValue(13L);

        // when
        form.performEvent("delete", new String[0]);

        // then
        verify(name).setFieldValue(null);
        verify(dataDefinition).delete(13L);
        assertNull(form.getFieldValue());
    }

    @Test
    public void shouldSaveFormEntity() throws Exception {
        // given
        Entity entity = new DefaultEntity("plugin", "name", null, Collections.singletonMap("name", (Object) "text"));
        Entity savedEntity = new DefaultEntity("plugin", "name", 13L, Collections.singletonMap("name", (Object) "text2"));
        given(dataDefinition.save(eq(entity))).willReturn(savedEntity);
        given(name.getFieldValue()).willReturn("text");

        form.setFieldValue(null);

        // when
        form.performEvent("save", new String[0]);

        // then
        verify(dataDefinition).save(eq(entity));
        verify(name).setFieldValue("text2");
        assertEquals(13L, form.getFieldValue());
        assertTrue(((FormComponentState) form).isValid());
    }

    @Test
    public void shouldUseContextWhileSaving() throws Exception {
        // given
        Entity entity = new DefaultEntity("plugin", "name", 14L, Collections.singletonMap("name", (Object) "text2"));
        Entity savedEntity = new DefaultEntity("plugin", "name", 14L, Collections.singletonMap("name", (Object) "text2"));
        given(dataDefinition.save(eq(entity))).willReturn(savedEntity);
        given(name.getFieldValue()).willReturn("text");

        JSONObject json = new JSONObject();
        JSONObject jsonContext = new JSONObject();
        jsonContext.put("id", 14L);
        jsonContext.put("name", "text2");
        JSONObject jsonContent = new JSONObject();
        jsonContent.put(ComponentState.JSON_VALUE, 13L);
        jsonContent.put("context", jsonContext);
        json.put(ComponentState.JSON_CONTENT, jsonContent);
        json.put(ComponentState.JSON_CHILDREN, new JSONObject());

        form.initialize(json, Locale.ENGLISH);

        // when
        form.performEvent("save", new String[0]);

        // then
        verify(dataDefinition).save(eq(entity));
        verify(name).setFieldValue("text2");
        assertEquals(14L, form.getFieldValue());
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
        given(name.getFieldValue()).willReturn("text");

        form.setFieldValue(null);

        // when
        form.performEvent("save", new String[0]);

        // then
        verify(dataDefinition).save(eq(entity));
        assertFalse(((FormComponentState) form).isValid());
        verify(name).addMessage("translated field error", ComponentState.MessageType.FAILURE);
        assertTrue(form.render().toString().contains("translated global error"));
    }
}
