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

package com.qcadoo.view.internal.patterns;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinition;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.internal.ComponentPattern;
import com.qcadoo.view.internal.FieldEntityIdChangeListener;
import com.qcadoo.view.internal.ScopeEntityIdChangeListener;
import com.qcadoo.view.internal.components.TextInputComponentPattern;
import com.qcadoo.view.internal.patterns.AbstractContainerPattern;
import com.qcadoo.view.internal.states.ComponentStateMock;

public class FieldAndScopeListenerPatternTest extends AbstractPatternTest {

    @Test
    public void shouldHaveFieldListeners() throws Exception {
        // given
        ViewDefinition viewDefinition = mock(ViewDefinition.class);

        AbstractContainerPattern parent = new ContainerPatternMock(getComponentDefinition("f1", viewDefinition));

        ComponentPatternMock child1 = new ComponentPatternMock(getComponentDefinition("t1", "t1", null, parent, viewDefinition));

        ComponentPatternMock child2 = new ComponentPatternMock(getComponentDefinition("t2", "t2", null, parent, viewDefinition));

        parent.addChild(child1);
        parent.addChild(child2);

        parent.initialize();
        child1.initialize();
        child2.initialize();

        // when
        Map<String, ComponentPattern> listeners = parent.getFieldEntityIdChangeListeners();

        // then
        Assert.assertEquals(2, listeners.size());
        Assert.assertEquals(child1, listeners.get("t1"));
        Assert.assertEquals(child2, listeners.get("t2"));
    }

    @Test
    public void shouldUpdateStateFieldListeners() throws Exception {
        // given
        ViewDefinition viewDefinition = mock(ViewDefinition.class);

        AbstractContainerPattern parent = new ContainerPatternMock(getComponentDefinition("f1", viewDefinition));

        ComponentPatternMock child1 = new ComponentPatternMock(getComponentDefinition("t1", "field1", null, parent,
                viewDefinition));

        ComponentPatternMock child2 = new ComponentPatternMock(getComponentDefinition("t2", "field2", null, parent,
                viewDefinition));

        parent.addChild(child1);
        parent.addChild(child2);

        parent.initialize();
        child1.initialize();
        child2.initialize();

        ComponentStateMock f1State = new ComponentStateMock();

        ComponentState t1State = Mockito.mock(ComponentState.class,
                withSettings().extraInterfaces(FieldEntityIdChangeListener.class));

        ComponentState t2State = Mockito.mock(ComponentState.class,
                withSettings().extraInterfaces(FieldEntityIdChangeListener.class));

        ViewDefinitionState viewDefinitions = Mockito.mock(ViewDefinitionState.class);
        BDDMockito.given(viewDefinitions.getComponentByReference("f1")).willReturn(f1State);
        BDDMockito.given(viewDefinitions.getComponentByReference("f1.t1")).willReturn(t1State);
        BDDMockito.given(viewDefinitions.getComponentByReference("f1.t2")).willReturn(t2State);

        // when
        parent.updateComponentStateListeners(viewDefinitions);

        // then

        assertEquals(t1State, f1State.getPublicFieldEntityIdChangeListeners().get("field1"));
        assertEquals(t2State, f1State.getPublicFieldEntityIdChangeListeners().get("field2"));
    }

    @Test
    public void shouldAddItselfToRelationFieldComponentWhenComplexFieldPath() throws Exception {
        // given
        ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);

        AbstractContainerPattern parent = Mockito.mock(AbstractContainerPattern.class);

        TextInputComponentPattern pattern = new TextInputComponentPattern(getComponentDefinition("testName",
                "#{testComponent}.testField", null, parent, viewDefinition));

        ComponentPatternMock testComponent = new ComponentPatternMock(getComponentDefinition("name", viewDefinition));

        given(viewDefinition.getComponentByReference("testComponent")).willReturn(testComponent);

        // when
        pattern.initialize();

        // then
        Assert.assertEquals(pattern, testComponent.getFieldEntityIdChangeListeners().get("testField"));
    }

    @Test
    public void shouldHaveScopeListeners() throws Exception {
        // given
        ViewDefinition viewDefinition = mock(ViewDefinition.class);

        AbstractContainerPattern parent = new ContainerPatternMock(getComponentDefinition("f1", viewDefinition));

        ComponentPatternMock child1 = new ComponentPatternMock(getComponentDefinition("t1", null, "t1", parent, viewDefinition));

        ComponentPatternMock child2 = new ComponentPatternMock(getComponentDefinition("t2", null, "t2", parent, viewDefinition));

        parent.addChild(child1);
        parent.addChild(child2);

        parent.initialize();
        child1.initialize();
        child2.initialize();

        // when
        Map<String, ComponentPattern> listeners = parent.getScopeEntityIdChangeListeners();

        // then
        Assert.assertEquals(2, listeners.size());
        Assert.assertEquals(child1, listeners.get("t1"));
        Assert.assertEquals(child2, listeners.get("t2"));
    }

    @Test
    public void shouldUpdateStateScopeListeners() throws Exception {
        // given
        ViewDefinition viewDefinition = mock(ViewDefinition.class);

        AbstractContainerPattern parent = new ContainerPatternMock(getComponentDefinition("f1", viewDefinition));

        ComponentPatternMock child1 = new ComponentPatternMock(getComponentDefinition("t1", null, "field1", parent,
                viewDefinition));

        ComponentPatternMock child2 = new ComponentPatternMock(getComponentDefinition("t2", null, "field2", parent,
                viewDefinition));

        parent.addChild(child1);
        parent.addChild(child2);

        parent.initialize();
        child1.initialize();
        child2.initialize();

        ComponentStateMock f1State = new ComponentStateMock();

        ComponentState t1State = Mockito.mock(ComponentState.class,
                withSettings().extraInterfaces(ScopeEntityIdChangeListener.class));

        ComponentState t2State = Mockito.mock(ComponentState.class,
                withSettings().extraInterfaces(ScopeEntityIdChangeListener.class));

        ViewDefinitionState viewDefinitions = Mockito.mock(ViewDefinitionState.class);
        BDDMockito.given(viewDefinitions.getComponentByReference("f1")).willReturn(f1State);
        BDDMockito.given(viewDefinitions.getComponentByReference("f1.t1")).willReturn(t1State);
        BDDMockito.given(viewDefinitions.getComponentByReference("f1.t2")).willReturn(t2State);

        // when
        parent.updateComponentStateListeners(viewDefinitions);

        // then
        assertEquals(t1State, f1State.getPublicScopeFieldEntityIdChangeListeners().get("field1"));
        assertEquals(t2State, f1State.getPublicScopeFieldEntityIdChangeListeners().get("field2"));
    }

    @Test
    public void shouldAddItselfToRelationScopeComponentWhenComplexFieldPath() throws Exception {
        // given
        ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);

        AbstractContainerPattern parent = Mockito.mock(AbstractContainerPattern.class);

        TextInputComponentPattern pattern = new TextInputComponentPattern(getComponentDefinition("testName", null,
                "#{testComponent}.testField", parent, viewDefinition));

        ComponentPatternMock testComponent = new ComponentPatternMock(getComponentDefinition("name", viewDefinition));
        given(viewDefinition.getComponentByReference("testComponent")).willReturn(testComponent);

        // when
        pattern.initialize();

        // then
        assertEquals(pattern, testComponent.getScopeEntityIdChangeListeners().get("testField"));
    }

}
