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

package com.qcadoo.mes.view.patterns;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.EmptyContainerState;
import com.qcadoo.mes.view.components.TextInputComponentPattern;
import com.qcadoo.mes.view.components.form.FormComponentPattern;
import com.qcadoo.mes.view.states.AbstractContainerState;

public class ContainerPatternTest extends AbstractPatternTest {

    @Test
    public void shouldHaveChildren() throws Exception {
        // given
        AbstractContainerPattern parent = new FormComponentPattern(getComponentDefinition("test", null));

        ComponentPattern child1 = new TextInputComponentPattern(getComponentDefinition("test1", parent, null));
        ComponentPattern child2 = new TextInputComponentPattern(getComponentDefinition("test2", parent, null));

        parent.addChild(child1);
        parent.addChild(child2);

        // when
        Map<String, ComponentPattern> children = parent.getChildren();

        // then
        Assert.assertEquals(2, children.size());
        Assert.assertEquals(child1, children.get("test1"));
        Assert.assertEquals(child2, children.get("test2"));
    }

    @Test
    public void shouldReturnChildByName() throws Exception {
        // given
        AbstractContainerPattern parent = new FormComponentPattern(getComponentDefinition("test", null));

        ComponentPattern child1 = new TextInputComponentPattern(getComponentDefinition("test1", parent, null));

        parent.addChild(child1);

        // when
        ComponentPattern child = parent.getChild("test1");

        // then
        Assert.assertEquals(child1, child);
    }

    @Test
    public void shouldReturnNullWhenChildNotExist() throws Exception {
        // given
        AbstractContainerPattern parent = new FormComponentPattern(getComponentDefinition("test", null));

        // when
        ComponentPattern child = parent.getChild("test3");

        // then
        Assert.assertNull(child);
    }

    @Test
    public void shouldNotCallInitializeOnChildren() throws Exception {
        // given
        ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);

        ComponentPattern child = Mockito.mock(ComponentPattern.class);

        AbstractContainerPattern parent = new FormComponentPattern(getComponentDefinition("test", viewDefinition));
        parent.addChild(child);

        // when
        parent.initialize();

        // then
        Mockito.verify(child, never()).initialize();
    }

    @Test
    public void shouldCallStateOnChildren() throws Exception {
        // given
        ViewDefinitionState viewDefinitionState = Mockito.mock(ViewDefinitionState.class);

        AbstractComponentPattern child = Mockito.mock(AbstractComponentPattern.class);

        AbstractContainerPattern parent = new FormComponentPattern(getComponentDefinition("test", null));
        parent.addChild(child);

        // when
        parent.updateComponentStateListeners(viewDefinitionState);

        // then
        Mockito.verify(child).updateComponentStateListeners(viewDefinitionState);
    }

    @Test
    public void shouldCallCreateComponentStateOnChildren() throws Exception {
        // given
        ViewDefinition viewDefinition = mock(ViewDefinition.class);
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);
        AbstractContainerState state = new EmptyContainerState();
        ComponentState state1 = mock(ComponentState.class);
        given(state1.getName()).willReturn("name1");
        ComponentState state2 = mock(ComponentState.class);
        given(state2.getName()).willReturn("name2");
        ComponentPattern pattern1 = mock(ComponentPattern.class);
        given(pattern1.getName()).willReturn("name1");
        given(pattern1.createComponentState(viewDefinitionState)).willReturn(state1);
        ComponentPattern pattern2 = mock(ComponentPattern.class);
        given(pattern2.getName()).willReturn("name2");
        given(pattern2.createComponentState(viewDefinitionState)).willReturn(state2);
        ContainerPatternMock parent = new ContainerPatternMock(getComponentDefinition("name", viewDefinition), state);
        parent.addChild(pattern1);
        parent.addChild(pattern2);

        // when
        ComponentState actualState = parent.createComponentState(viewDefinitionState);

        // then
        assertEquals(state, actualState);
        verify(pattern1).createComponentState(viewDefinitionState);
        verify(pattern2).createComponentState(viewDefinitionState);
        verify(viewDefinitionState).registerComponent("name", "name", actualState);
        assertEquals(state1, state.getChild("name1"));
        assertEquals(state2, state.getChild("name2"));
    }

}
