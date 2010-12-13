package com.qcadoo.mes.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.view.internal.ViewDefinitionStateImpl;
import com.qcadoo.mes.view.states.AbstractStateTest;

public class ViewDefinitionStateTest extends AbstractStateTest {

    @Test
    public void shouldReturnStateByFunctionalPath() throws Exception {
        // given
        ViewDefinitionState viewDefinitionState = new ViewDefinitionStateImpl();

        ContainerState state = mock(ContainerState.class);

        viewDefinitionState.registerComponent("reference", "path", state);

        // when
        ComponentState actualState = viewDefinitionState.getComponentByReference("reference");

        // then
        assertEquals(state, actualState);
    }

    @Test
    public void shouldReturnNullWhenStateNoExists() throws Exception {
        // given
        ViewDefinitionState viewDefinitionState = new ViewDefinitionStateImpl();

        // when
        ComponentState actualState = viewDefinitionState.getComponentByReference("xxx");

        // then
        assertNull(actualState);
    }

    @Test
    public void shouldPerformEventOnState() throws Exception {
        // given
        ViewDefinitionState viewDefinitionState = new ViewDefinitionStateImpl();

        ContainerState state1 = mock(ContainerState.class);
        given(state1.getName()).willReturn("name1");

        ComponentState state2 = mock(ComponentState.class);

        viewDefinitionState.addChild(state1);
        given(state1.getChild("name2")).willReturn(state2);

        // when
        viewDefinitionState.performEvent("name1.name2", "event", new String[] { "arg1", "arg2" });

        // then
        Mockito.verify(state2).performEvent(viewDefinitionState, "event", "arg1", "arg2");
    }

    @Test
    public void shouldPerformEventOnAllComponent() throws Exception {
        // given
        ViewDefinitionState viewDefinitionState = new ViewDefinitionStateImpl();

        ContainerState state1 = mock(ContainerState.class);
        given(state1.getName()).willReturn("name1");

        ComponentState state2 = mock(ComponentState.class);

        viewDefinitionState.addChild(state1);
        given(state1.getChildren()).willReturn(Collections.singletonMap("name2", state2));

        // when
        viewDefinitionState.performEvent((String) null, "event", new String[] { "arg1", "arg2" });

        // then
        Mockito.verify(state1).performEvent(viewDefinitionState, "event", "arg1", "arg2");
        Mockito.verify(state2).performEvent(viewDefinitionState, "event", "arg1", "arg2");
    }

}
