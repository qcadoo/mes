package com.qcadoo.mes.view.states;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Test;

import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.components.FormComponentState;
import com.qcadoo.mes.view.states.components.ComponentStateMock;

public class ContainerStateTest extends AbstractStateTest {

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
        ComponentState component1 = createMockComponent("component1");
        ComponentState component2 = createMockComponent("component2");

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
        ComponentState component = createMockComponent("component");

        FormComponentState container = new FormComponentState();
        container.addChild(component);

        // when
        ComponentState child = container.getChild("component");

        // then
        assertSame(component, child);
    }

    @Test
    public void shouldReturnNullIfChildNotExist() throws Exception {
        // given
        FormComponentState container = new FormComponentState();

        // when
        ComponentState child = container.getChild("component");

        // then
        assertNull(child);
    }

    @Test
    public void shouldInitializeChildren() throws Exception {
        // given
        ComponentState component1 = createMockComponent("component1");
        ComponentState component2 = createMockComponent("component2");

        FormComponentState container = new FormComponentState();
        container.addChild(component1);
        container.addChild(component2);

        JSONObject json = new JSONObject();
        JSONObject children = new JSONObject();
        JSONObject component1Json = new JSONObject();
        component1Json.put(ComponentState.JSON_CONTENT, new JSONObject());
        JSONObject component2Json = new JSONObject();
        component2Json.put(ComponentState.JSON_CONTENT, new JSONObject());
        children.put("component1", component1Json);
        children.put("component2", component2Json);
        json.put(ComponentState.JSON_CHILDREN, children);
        json.put(ComponentState.JSON_CONTENT, new JSONObject(Collections.singletonMap("entityId", 13L)));

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
        component1Json.put(ComponentState.JSON_CONTENT, "test1");
        JSONObject component2Json = new JSONObject();
        component2Json.put(ComponentState.JSON_CONTENT, "test2");

        ComponentState component1 = createMockComponent("component1");
        given(component1.render()).willReturn(component1Json);
        ComponentState component2 = createMockComponent("component2");
        given(component2.render()).willReturn(component2Json);

        FormComponentState container = new FormComponentState();
        container.addChild(component1);
        container.addChild(component2);

        // when
        JSONObject json = container.render();

        // then
        verify(component1).render();
        verify(component2).render();
        assertEquals(
                "test1",
                json.getJSONObject(ComponentState.JSON_CHILDREN).getJSONObject("component1")
                        .getString(ComponentState.JSON_CONTENT));
        assertEquals(
                "test2",
                json.getJSONObject(ComponentState.JSON_CHILDREN).getJSONObject("component2")
                        .getString(ComponentState.JSON_CONTENT));
    }

    @Test
    public void shouldCallBeforeRenderOnChildren() throws Exception {
        // given
        FormComponentState container = new FormComponentState();
        ComponentStateMock componentState1 = new ComponentStateMock("test1");
        ComponentStateMock componentState2 = new ComponentStateMock("test2");
        container.addChild(componentState1);
        container.addChild(componentState2);

        // when
        container.beforeRender();

        // then
        Assert.assertEquals(1, componentState1.getBeforeRenderContentCallNumber());
        Assert.assertEquals(1, componentState2.getBeforeRenderContentCallNumber());
    }
}
