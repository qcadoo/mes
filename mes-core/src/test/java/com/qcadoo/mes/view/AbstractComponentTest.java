/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
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

package com.qcadoo.mes.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.DataDefinition;

public class AbstractComponentTest {

    private DataDefinition dataDefinition;

    private ViewDefinition viewDefinition;

    private TranslationService translationService;

    private Map<String, Component<?>> componentRegistry;

    private RootComponent root;

    @Before
    public void init() {
        dataDefinition = mock(DataDefinition.class);
        viewDefinition = mock(ViewDefinition.class);
        translationService = mock(TranslationService.class);
        componentRegistry = new HashMap<String, Component<?>>();
        root = mock(RootComponent.class);
        given(root.getDataDefinition()).willReturn(dataDefinition);
        given(root.getViewDefinition()).willReturn(viewDefinition);
        given(root.getPath()).willReturn("rootPath");
        given(root.isInitialized()).willReturn(true);
    }

    @Test
    public void shouldCreateComponentWithoudFieldAndSourcePath() throws Exception {
        // given
        CustomAbstractComponent component = new CustomAbstractComponent("name", root, null, null, translationService);
        component.initializeComponent(componentRegistry);

        // then
        assertEquals(dataDefinition, component.getDataDefinition());
        assertEquals("name", component.getName());
        assertEquals("rootPath.name", component.getPath());
        assertNull(component.getFieldPath());
        assertNull(component.getSourceComponent());
        assertNull(component.getSourceFieldPath());
        assertEquals(0, component.getListeners().size());
        assertEquals(root, component.getParentContainer());
        assertEquals("component", component.getType());
        assertEquals(viewDefinition, component.getViewDefinition());
        assertEquals(translationService, component.getTranslationService());
    }

    @Test
    public void shouldGetOptions() throws Exception {
        // given
        CustomAbstractComponent component = new CustomAbstractComponent("name", root, null, null, translationService);
        component.initializeComponent(componentRegistry);

        // TODO masz listeners

        // when
        component.addOption("test", "testValue");

        // then
        Map<String, Object> options = component.getOptions();

        assertEquals("name", options.get("name"));
        assertEquals(Collections.emptySet(), options.get("listeners"));
        assertEquals("testValue", options.get("test"));

        JSONObject json = new JSONObject(component.getOptionsAsJson());

        assertEquals("name", json.getString("name"));
        assertEquals(0, json.getJSONArray("listeners").length());
        assertEquals("testValue", json.getString("test"));

    }

    // assertNull(component.getOptions());
    // assertEquals("component", component.getOptionsAsJson());

    private class CustomAbstractComponent extends AbstractComponent<Object> {

        public CustomAbstractComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
                final String sourceFieldPath, final TranslationService translationService) {
            super(name, parentContainer, fieldPath, sourceFieldPath, translationService);
        }

        @Override
        public String getType() {
            return "component";
        }

        @Override
        public ViewValue<Object> castComponentValue(final Map<String, Entity> selectedEntities, final JSONObject viewObject)
                throws JSONException {
            return null;
        }

        @Override
        public ViewValue<Object> getComponentValue(final Entity entity, final Entity parentEntity,
                final Map<String, Entity> selectedEntities, final ViewValue<Object> viewValue, final Set<String> pathsToUpdate,
                final Locale locale) {
            return null;
        }

    }

}
