package com.qcadoo.mes.newview.patterns;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.newview.AbstractComponentPattern;
import com.qcadoo.mes.newview.ComponentPattern;
import com.qcadoo.mes.newview.ComponentState;
import com.qcadoo.mes.newview.components.FormComponentPattern;
import com.qcadoo.mes.newview.components.FormComponentState;
import com.qcadoo.mes.newview.components.TextInputComponentPattern;

public class ComponentPatternTest {

    @Test
    public void shouldHaveValidInstance() throws Exception {
        // given
        ComponentPattern pattern = new FormComponentPattern("testName", null, null, null);

        // when
        ComponentState state = pattern.createComponentState();

        // then
        Assert.assertTrue(state instanceof FormComponentState);
    }

    @Test
    public void shouldHaveName() throws Exception {
        // given
        ComponentPattern pattern = new FormComponentPattern("testName", null, null, null);

        // when
        String name = pattern.getName();

        // then
        Assert.assertEquals("testName", name);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithoutName() throws Exception {
        // given
        ComponentPattern pattern = new FormComponentPattern(null, null, null, null);

        // when
        pattern.createComponentState();
    }

    @Test
    public void shouldReturnValidPath() throws Exception {
        // given
        TextInputComponentPattern root = new TextInputComponentPattern("rootName", null, null, null);
        TextInputComponentPattern child1 = new TextInputComponentPattern("child1", null, null, root);
        TextInputComponentPattern child2 = new TextInputComponentPattern("child2", null, null, root);
        TextInputComponentPattern child11 = new TextInputComponentPattern("child11", null, null, child1);

        // when
        String rootPathName = root.getPathName();
        String child1PathName = child1.getPathName();
        String child2PathName = child2.getPathName();
        String child11PathName = child11.getPathName();

        // then
        Assert.assertEquals("rootName", rootPathName);
        Assert.assertEquals("rootName.child1", child1PathName);
        Assert.assertEquals("rootName.child2", child2PathName);
        Assert.assertEquals("rootName.child1.child11", child11PathName);
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

}
