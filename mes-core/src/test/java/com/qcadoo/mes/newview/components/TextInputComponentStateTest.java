package com.qcadoo.mes.newview.components;

import static junit.framework.Assert.assertEquals;

import java.util.Locale;

import org.json.JSONObject;
import org.junit.Test;

import com.qcadoo.mes.newview.ComponentState;

public class TextInputComponentStateTest {

    @Test
    public void shouldHaveFieldValueAfterInitialize() throws Exception {
        // given
        ComponentState componentState = new TextInputComponentState();

        JSONObject json = new JSONObject();
        JSONObject jsonContent = new JSONObject();
        jsonContent.put("value", "text");
        json.put("content", jsonContent);

        // when
        componentState.initialize(json, Locale.ENGLISH);

        // then
        assertEquals("text", componentState.getFieldValue());
    }

    @Test
    public void shouldRenderJsonWhenCalled() throws Exception {
        // given
        ComponentState componentState = new TextInputComponentState();
        componentState.setFieldValue("text");

        // when
        JSONObject json = componentState.render();

        // then
        assertEquals("text", json.getJSONObject("content").getString("value"));
    }

    @Test
    public void shouldRenderJsonWhenCalled2() throws Exception {
        // given
        ComponentState componentState = new TextInputComponentState();
        componentState.setFieldValue("text2");

        // when
        JSONObject json = componentState.render();

        // then
        assertEquals("text2", json.getJSONObject("content").getString("value"));
    }
}
