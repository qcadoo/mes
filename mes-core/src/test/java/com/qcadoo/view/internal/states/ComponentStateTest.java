/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.view.internal.states;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Locale;

import org.json.JSONObject;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.internal.ExpressionServiceImpl;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.components.SimpleComponentState;
import com.qcadoo.view.components.form.FormComponentState;
import com.qcadoo.view.internal.states.AbstractComponentState;

public class ComponentStateTest {

    @Test
    public void shouldHaveFieldValueAfterInitialize() throws Exception {
        // given
        ComponentState componentState = new SimpleComponentState();

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
        ComponentState componentState = new SimpleComponentState();
        componentState.setFieldValue("text");
        componentState.initialize(new JSONObject(), Locale.ENGLISH);

        // when
        JSONObject json = componentState.render();

        // then
        assertEquals("text", json.getJSONObject(ComponentState.JSON_CONTENT).getString(ComponentState.JSON_VALUE));
    }

    @Test
    public void shouldRenderJsonWithNullFieldValue() throws Exception {
        // given
        ComponentState componentState = new SimpleComponentState();
        componentState.setFieldValue(null);
        componentState.initialize(new JSONObject(), Locale.ENGLISH);

        // when
        JSONObject json = componentState.render();

        // then
        assertFalse(json.getJSONObject(ComponentState.JSON_CONTENT).has(ComponentState.JSON_VALUE));
    }

    @Test
    public void shouldNotRenderComponentIfNotRequested() throws Exception {
        // given
        ComponentState componentState = new SimpleComponentState();

        // when
        JSONObject json = componentState.render();

        // then
        assertFalse(json.has(ComponentState.JSON_CONTENT));
    }

    @Test
    public void shouldHaveRequestUpdateStateFlag() throws Exception {
        // given
        new ExpressionServiceImpl().init();

        TranslationService translationService = mock(TranslationService.class);
        DataDefinition dataDefinition = mock(DataDefinition.class);
        AbstractComponentState componentState = new FormComponentState(null, "2");
        componentState.setTranslationService(translationService);
        componentState.setDataDefinition(dataDefinition);
        componentState.setFieldValue(13L);
        componentState.initialize(new JSONObject(ImmutableMap.of("components", new JSONObject())), Locale.ENGLISH);

        // when
        JSONObject json = componentState.render();

        // then
        assertTrue(json.getBoolean(ComponentState.JSON_UPDATE_STATE));
    }

    @Test
    public void shouldNotHaveRequestUpdateStateIfNotValid() throws Exception {
        // given
        TranslationService translationService = mock(TranslationService.class);
        AbstractComponentState componentState = new FormComponentState(null, null);
        componentState.setTranslationService(translationService);
        componentState.initialize(new JSONObject(ImmutableMap.of("components", new JSONObject())), Locale.ENGLISH);
        componentState.addMessage("test", MessageType.FAILURE);

        // when
        JSONObject json = componentState.render();

        // then
        assertFalse(json.getBoolean(ComponentState.JSON_UPDATE_STATE));
    }

    @Test
    public void shouldHaveVisibleFlag() throws Exception {
        // given
        ComponentState componentState = new SimpleComponentState();

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
        ComponentState componentState = new SimpleComponentState();

        // when
        componentState.setVisible(false);

        // then
        assertFalse(componentState.isVisible());
        assertFalse(componentState.render().getBoolean(ComponentState.JSON_VISIBLE));
    }

    @Test
    public void shouldHaveEnableFlag() throws Exception {
        // given
        ComponentState componentState = new SimpleComponentState();

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
        ComponentState componentState = new SimpleComponentState();

        // when
        componentState.setEnabled(false);

        // then
        assertFalse(componentState.isEnabled());
        assertFalse(componentState.render().getBoolean(ComponentState.JSON_ENABLED));
    }

}
