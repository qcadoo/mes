package com.qcadoo.mes.newview.components;

import static org.mockito.BDDMockito.given;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.newview.AbstractComponentPattern;
import com.qcadoo.mes.newview.ComponentState;
import com.qcadoo.mes.newview.ViewDefinition;

public class TextInputComponentPatternTest {

    @Test
    public void shouldReturnValidTextInputComponentStateInstance() throws Exception {
        // given
        TextInputComponentPattern pattern = new TextInputComponentPattern(null, null, null, null);

        // when
        ComponentState state = pattern.createComponentState();

        // then
        Assert.assertTrue(state instanceof TextInputComponentState);
    }

    @Test
    public void shouldHaveName() throws Exception {
        // given
        TextInputComponentPattern pattern = new TextInputComponentPattern("testName", null, null, null);

        // when
        String name = pattern.getName();

        // then
        Assert.assertEquals("testName", name);
    }

    @Test
    public void shouldAddItselfToParentOnInitialize() throws Exception {
        // given
        AbstractComponentPattern parent = Mockito.mock(AbstractComponentPattern.class);
        TextInputComponentPattern pattern = new TextInputComponentPattern("testName", "testField", null, parent);

        // when
        pattern.initialize(null);

        // then
        Mockito.verify(parent).addFieldEntityIdChangeListener("testField", pattern);
    }

    @Test
    public void shouldAddItselfToRelationFieldComponentWhenComplexFieldPath() throws Exception {
        // given
        AbstractComponentPattern parent = Mockito.mock(AbstractComponentPattern.class);
        TextInputComponentPattern pattern = new TextInputComponentPattern("testName", "#{testComponent}.testField", null, parent);
        AbstractComponentPattern testComponent = Mockito.mock(AbstractComponentPattern.class);
        ViewDefinition vd = Mockito.mock(ViewDefinition.class);
        given(vd.getComponentByPath("testComponent")).willReturn(testComponent);

        // when
        pattern.initialize(vd);

        // then
        Mockito.verify(testComponent).addFieldEntityIdChangeListener("testField", pattern);
    }

}
