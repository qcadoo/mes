/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.view;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.mockito.Mockito;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.HookDefinition;
import com.qcadoo.mes.viewold.RootComponent;
import com.qcadoo.mes.viewold.ViewValue;
import com.qcadoo.mes.viewold.internal.ViewDefinitionImpl;

public class ViewDefinitionTest {

    @Test
    public void shouldGetTranslationFromRootComponent() throws Exception {
        // given
        RootComponent root = mock(RootComponent.class);

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("test", "test");
        viewDefinition.setRoot(root);

        Map<String, String> translations = new HashMap<String, String>();

        // when
        viewDefinition.updateTranslations(translations, Locale.ENGLISH);

        // then
        verify(root).updateTranslations(translations, Locale.ENGLISH);
    }

    @Test
    public void shouldCastVauleFromRootComponent() throws Exception {
        // given
        Map<String, Entity> selectedEntities = new HashMap<String, Entity>();

        JSONObject jsonObject = new JSONObject();
        JSONObject rootJsonObject = new JSONObject();
        jsonObject.put("rootName", rootJsonObject);

        RootComponent root = mock(RootComponent.class);
        given(root.castValue(selectedEntities, rootJsonObject)).willReturn(new ViewValue<Object>("test"));
        given(root.getName()).willReturn("rootName");

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("test", "test");
        viewDefinition.setRoot(root);

        // when
        ViewValue<Long> value = viewDefinition.castValue(selectedEntities, jsonObject);

        // then
        assertEquals("test", value.getComponent("rootName").getValue());
        verify(root).castValue(selectedEntities, rootJsonObject);
    }

    @Test
    public void shouldCastNullVauleFromRootComponent() throws Exception {
        // given
        Map<String, Entity> selectedEntities = new HashMap<String, Entity>();

        RootComponent root = mock(RootComponent.class);
        given(root.castValue(selectedEntities, null)).willReturn(null);
        given(root.getName()).willReturn("rootName");

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("test", "test");
        viewDefinition.setRoot(root);

        // when
        ViewValue<Long> value = viewDefinition.castValue(selectedEntities, null);

        // then
        assertNull(value.getComponent("rootName"));
        verify(root).castValue(selectedEntities, null);
    }

    @Test
    public void shouldGetVauleFromRootComponent() throws Exception {
        // given
        Entity entity = new DefaultEntity("", "", 1L);

        Map<String, Entity> selectedEntities = new HashMap<String, Entity>();
        selectedEntities.put("removeIt", new DefaultEntity("", "", 2L));
        selectedEntities.put("keepIt", new DefaultEntity("", "", 3L));

        String triggerComponentName = "trigger";
        Set<String> pathsToUpdate = new HashSet<String>();
        pathsToUpdate.add("removeIt");

        ViewValue<Long> globalViewValue = new ViewValue<Long>();
        ViewValue<Long> viewValue = new ViewValue<Long>();
        globalViewValue.addComponent("rootName", viewValue);

        RootComponent root = mock(RootComponent.class);
        given(root.getValue(entity, selectedEntities, viewValue, pathsToUpdate, Locale.ENGLISH)).willReturn(
                new ViewValue<Object>("test"));
        given(root.getName()).willReturn("rootName");
        given(root.lookupListeners(triggerComponentName)).willReturn(pathsToUpdate);

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("test", "test");
        viewDefinition.setRoot(root);

        // when
        ViewValue<Long> value = viewDefinition.getValue(entity, selectedEntities, globalViewValue, triggerComponentName, true,
                Locale.ENGLISH);

        // then
        assertEquals(2, pathsToUpdate.size());
        assertThat(pathsToUpdate, JUnitMatchers.hasItems(triggerComponentName, "removeIt"));
        assertEquals(1, selectedEntities.size());
        assertEquals(new DefaultEntity("", "", 3L), selectedEntities.get("keepIt"));
        assertEquals("test", value.getComponent("rootName").getValue());
        verify(root).getValue(entity, selectedEntities, viewValue, pathsToUpdate, Locale.ENGLISH);
    }

    @Test
    public void shouldGetNullVauleFromRootComponent() throws Exception {
        // given
        Map<String, Entity> selectedEntities = new HashMap<String, Entity>();
        String triggerComponentName = "trigger";
        Set<String> pathsToUpdate = new HashSet<String>();

        RootComponent root = mock(RootComponent.class);
        given(root.getValue(null, selectedEntities, null, pathsToUpdate, Locale.ENGLISH)).willReturn(null);
        given(root.getName()).willReturn("rootName");
        given(root.lookupListeners(triggerComponentName)).willReturn(pathsToUpdate);

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("test", "test");
        viewDefinition.setRoot(root);

        // when
        ViewValue<Long> value = viewDefinition.getValue(null, selectedEntities, null, triggerComponentName, true, Locale.ENGLISH);

        // then
        assertNull(value.getComponent("rootName"));
        verify(root).getValue(null, selectedEntities, null, pathsToUpdate, Locale.ENGLISH);
    }

    @Test
    public void shouldCallHook() throws Exception {
        // given
        Entity entity = new DefaultEntity("", "", 1L);
        ViewValue<Long> globalViewValue = new ViewValue<Long>();
        Map<String, Entity> selectedEntities = new HashMap<String, Entity>();
        String triggerComponentName = "trigger";
        Set<String> pathsToUpdate = new HashSet<String>();
        HookDefinition hookDefinition = mock(HookDefinition.class);

        RootComponent root = mock(RootComponent.class);
        given(root.getName()).willReturn("rootName");
        given(root.lookupListeners(triggerComponentName)).willReturn(pathsToUpdate);

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("test", "test");
        viewDefinition.setRoot(root);
        viewDefinition.setViewHook(hookDefinition);

        // when
        ViewValue<Long> value = viewDefinition.getValue(entity, selectedEntities, globalViewValue, triggerComponentName, false,
                Locale.ENGLISH);

        // then
        assertEquals(1, pathsToUpdate.size());
        // verify(hookDefinition).callWithViewState(value, triggerComponentName, entity, Locale.ENGLISH);
    }

    @Test
    public void shouldNotLookupListenersIfThereIsNotTriggerComponent() throws Exception {
        // given
        Entity entity = new DefaultEntity("", "", 1L);

        Map<String, Entity> selectedEntities = new HashMap<String, Entity>();

        HookDefinition hookDefinition = mock(HookDefinition.class);

        ViewValue<Long> globalViewValue = new ViewValue<Long>();

        RootComponent root = mock(RootComponent.class);
        given(root.getName()).willReturn("rootName");

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("test", "test");
        viewDefinition.setRoot(root);
        viewDefinition.setViewHook(hookDefinition);

        // when
        viewDefinition.getValue(entity, selectedEntities, globalViewValue, null, false, Locale.ENGLISH);

        // then
        verify(root, never()).lookupListeners(Mockito.anyString());
    }

    @Test
    public void shouldGetDataDefinitionFromRootComponent() throws Exception {
        // given
        RootComponent root = mock(RootComponent.class);

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("test", "test");
        viewDefinition.setRoot(root);

        // when
        viewDefinition.getDataDefinition();

        // then
        verify(root).getDataDefinition();
    }

    @Test
    public void shouldLookupComponentOnRootComponent() throws Exception {
        // given
        RootComponent root = mock(RootComponent.class);

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("test", "test");
        viewDefinition.setRoot(root);

        // when
        viewDefinition.lookupComponent("path");

        // then
        verify(root).lookupComponent("path");
    }

    @Test
    public void shouldHasRootComponent() throws Exception {
        // given
        RootComponent root = mock(RootComponent.class);

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl("test", "test");
        viewDefinition.setRoot(root);

        // then
        assertEquals(root, viewDefinition.getRoot());
    }
}
