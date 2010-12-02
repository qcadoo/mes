package com.qcadoo.mes.view.states;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Test;

import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ComponentState.MessageType;
import com.qcadoo.mes.view.components.FormComponentState;
import com.qcadoo.mes.view.components.TextInputComponentState;
import com.qcadoo.mes.view.states.components.ComponentStateMock;

public class ComponentStateTest {

    @Test
    public void shouldHaveFieldValueAfterInitialize() throws Exception {
        // given
        ComponentState componentState = new TextInputComponentState();

        JSONObject json = new JSONObject();
        JSONObject jsonContent = new JSONObject();
        jsonContent.put(ComponentState.JSON_VALUE, "text");
        json.put(ComponentState.JSON_CONTENT, jsonContent);

        // when
        componentState.initialize(json, Locale.ENGLISH);

        // then
        assertEquals("text", componentState.getFieldValue());
    }

    @Test
    public void shouldRenderJsonWithFieldValue() throws Exception {
        // given
        ComponentState componentState = new TextInputComponentState();
        componentState.setFieldValue("text");

        // when
        JSONObject json = componentState.render();

        // then
        assertEquals("text", json.getJSONObject(ComponentState.JSON_CONTENT).getString(ComponentState.JSON_VALUE));
    }

    @Test
    public void shouldRenderJsonWithNullFieldValue() throws Exception {
        // given
        ComponentState componentState = new TextInputComponentState();
        componentState.setFieldValue(null);

        // when
        JSONObject json = componentState.render();

        // then
        assertFalse(json.getJSONObject(ComponentState.JSON_CONTENT).has(ComponentState.JSON_VALUE));
    }

    @Test
    public void shouldNotRenderComponentIfNotRequested() throws Exception {
        // given
        ComponentState componentState = new TextInputComponentState();

        // when
        JSONObject json = componentState.render();

        // then
        assertFalse(json.has(ComponentState.JSON_CONTENT));
    }

    @Test
    public void shouldHaveRequestUpdateStateFlag() throws Exception {
        // given
        ComponentState componentState = new FormComponentState(null);
        componentState.setFieldValue(13L);

        // when
        JSONObject json = componentState.render();

        // then
        assertTrue(json.getBoolean(ComponentState.JSON_UPDATE_STATE));
    }

    @Test
    public void shouldNotHaveRequestUpdateStateIfNotValid() throws Exception {
        // given
        ComponentState componentState = new FormComponentState(null);
        componentState.addMessage("test", MessageType.FAILURE);

        // when
        JSONObject json = componentState.render();

        // then
        assertFalse(json.getBoolean(ComponentState.JSON_UPDATE_STATE));
    }

    @Test
    public void shouldHaveVisibleFlag() throws Exception {
        // given
        ComponentState componentState = new TextInputComponentState();

        JSONObject json = new JSONObject();
        JSONObject jsonContent = new JSONObject();
        jsonContent.put(ComponentState.JSON_VALUE, "text");
        json.put(ComponentState.JSON_CONTENT, jsonContent);
        json.put(ComponentState.JSON_VISIBLE, true);

        // when
        componentState.initialize(json, Locale.ENGLISH);

        // then
        assertTrue(componentState.isVisible());
        assertTrue(componentState.render().getBoolean(ComponentState.JSON_VISIBLE));
    }

    @Test
    public void shouldModifyVisibleFlag() throws Exception {
        // given
        ComponentState componentState = new TextInputComponentState();

        // when
        componentState.setVisible(false);

        // then
        assertFalse(componentState.isVisible());
        assertFalse(componentState.render().getBoolean(ComponentState.JSON_VISIBLE));
    }

    @Test
    public void shouldHaveEnableFlag() throws Exception {
        // given
        ComponentState componentState = new TextInputComponentState();

        JSONObject json = new JSONObject();
        JSONObject jsonContent = new JSONObject();
        jsonContent.put(ComponentState.JSON_VALUE, "text");
        json.put(ComponentState.JSON_CONTENT, jsonContent);
        json.put(ComponentState.JSON_ENABLED, true);

        // when
        componentState.initialize(json, Locale.ENGLISH);

        // then
        assertTrue(componentState.isEnabled());
        assertTrue(componentState.render().getBoolean(ComponentState.JSON_ENABLED));
    }

    @Test
    public void shouldModifyEnableFlag() throws Exception {
        // given
        ComponentState componentState = new TextInputComponentState();

        // when
        componentState.setEnabled(false);

        // then
        assertFalse(componentState.isEnabled());
        assertFalse(componentState.render().getBoolean(ComponentState.JSON_ENABLED));
    }

    @Test
    public void shouldCallBeforeRenderContent() throws Exception {
        // given
        ComponentStateMock componentState = new ComponentStateMock(null);

        // when
        componentState.beforeRender();

        // then
        Assert.assertEquals(1, componentState.getBeforeRenderContentCallNumber());
    }
}
