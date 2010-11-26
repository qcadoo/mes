package com.qcadoo.mes.newview.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Test;

import com.qcadoo.mes.newview.ComponentState;
import com.qcadoo.mes.newview.FieldEntityIdChangeListener;
import com.qcadoo.mes.newview.ScopeEntityIdChangeListener;

public class FormComponentStateTest {

    @Test
    public void shouldHaveNoChildren() throws Exception {
        // given
        FormComponentState container = new FormComponentState();

        // when
        Map<String, ComponentState> children = container.getChildren();

        // then
        assertNotNull(children);
        assertEquals(0, children.size());
    }

    @Test
    public void shouldHaveChildren() throws Exception {
        // given
        ComponentState component1 = mock(ComponentState.class);
        given(component1.getName()).willReturn("testComponent1");
        ComponentState component2 = mock(ComponentState.class);
        given(component2.getName()).willReturn("testComponent2");

        FormComponentState container = new FormComponentState();
        container.addChild(component1);
        container.addChild(component2);

        // when
        Map<String, ComponentState> children = container.getChildren();

        // then
        assertNotNull(children);
        assertEquals(2, children.size());
    }

    @Test
    public void shouldReturnChildByName() throws Exception {
        // given
        ComponentState component = mock(ComponentState.class);
        given(component.getName()).willReturn("testComponent");

        FormComponentState container = new FormComponentState();
        container.addChild(component);

        // when
        ComponentState child = container.getChild("testComponent");

        // then
        assertSame(component, child);
    }

    @Test
    public void shouldReturnNullIfChildNotExist() throws Exception {
        // given
        FormComponentState container = new FormComponentState();

        // when
        ComponentState child = container.getChild("testComponent");

        // then
        assertNull(child);
    }

    @Test
    public void shouldInitializeChildren() throws Exception {
        // given
        ComponentState component1 = mock(ComponentState.class);
        given(component1.getName()).willReturn("component1");
        ComponentState component2 = mock(ComponentState.class);
        given(component2.getName()).willReturn("component2");

        FormComponentState container = new FormComponentState();
        container.addChild(component1);
        container.addChild(component2);

        JSONObject json = new JSONObject();
        JSONObject children = new JSONObject();
        JSONObject component1Json = new JSONObject();
        component1Json.put("content", new JSONObject());
        JSONObject component2Json = new JSONObject();
        component2Json.put("content", new JSONObject());
        children.put("component1", component1Json);
        children.put("component2", component2Json);
        json.put("children", children);
        json.put("content", new JSONObject());

        // when
        container.initialize(json, Locale.ENGLISH);

        // then
        verify(component1).initialize(component1Json, Locale.ENGLISH);
        verify(component2).initialize(component2Json, Locale.ENGLISH);
    }

    @Test
    public void shouldRenderChildren() throws Exception {
        // given
        JSONObject component1Json = new JSONObject();
        component1Json.put("content", "test1");
        JSONObject component2Json = new JSONObject();
        component2Json.put("content", "test2");

        ComponentState component1 = mock(ComponentState.class);
        given(component1.getName()).willReturn("component1");
        given(component1.render()).willReturn(component1Json);
        ComponentState component2 = mock(ComponentState.class);
        given(component2.getName()).willReturn("component2");
        given(component2.render()).willReturn(component2Json);

        FormComponentState container = new FormComponentState();
        container.addChild(component1);
        container.addChild(component2);

        // when
        JSONObject json = container.render();

        // then
        verify(component1).render();
        verify(component2).render();
        assertEquals("test1", json.getJSONObject("children").getJSONObject("component1").getString("content"));
        assertEquals("test2", json.getJSONObject("children").getJSONObject("component2").getString("content"));
    }

    @Test
    public void shouldHaveFieldListeners() throws Exception {
        // given
        ComponentState component1 = mock(ComponentState.class, withSettings().extraInterfaces(FieldEntityIdChangeListener.class));
        given(component1.getName()).willReturn("component1");
        ComponentState component2 = mock(ComponentState.class, withSettings().extraInterfaces(FieldEntityIdChangeListener.class));
        given(component2.getName()).willReturn("component2");

        FormComponentState container = new FormComponentState();
        container.addFieldEntityIdChangeListener("field1", (FieldEntityIdChangeListener) component1);
        container.addFieldEntityIdChangeListener("field2", (FieldEntityIdChangeListener) component2);

        // when
        container.setFieldValue(13L);

        // then
        verify((FieldEntityIdChangeListener) component1).onFieldEntityIdChange(13L);
        verify((FieldEntityIdChangeListener) component2).onFieldEntityIdChange(13L);
    }

    @Test
    public void shouldHaveScopeListeners() throws Exception {
        // given
        ComponentState component1 = mock(ComponentState.class, withSettings().extraInterfaces(ScopeEntityIdChangeListener.class));
        given(component1.getName()).willReturn("component1");
        ComponentState component2 = mock(ComponentState.class, withSettings().extraInterfaces(ScopeEntityIdChangeListener.class));
        given(component2.getName()).willReturn("component2");

        FormComponentState container = new FormComponentState();
        container.addScopeEntityIdChangeListener((ScopeEntityIdChangeListener) component1);
        container.addScopeEntityIdChangeListener((ScopeEntityIdChangeListener) component2);

        // when
        container.setFieldValue(13L);

        // then
        verify((ScopeEntityIdChangeListener) component1).onScopeEntityIdChange(13L);
        verify((ScopeEntityIdChangeListener) component2).onScopeEntityIdChange(13L);
    }

}
