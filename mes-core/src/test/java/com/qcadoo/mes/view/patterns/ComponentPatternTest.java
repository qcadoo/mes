package com.qcadoo.mes.view.patterns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.components.FormComponentPattern;
import com.qcadoo.mes.view.components.FormComponentState;
import com.qcadoo.mes.view.components.TextInputComponentPattern;

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
        ComponentPattern root = new TextInputComponentPattern("rootName", null, null, null);
        ComponentPattern child1 = new TextInputComponentPattern("child1", null, null, root);
        ComponentPattern child2 = new TextInputComponentPattern("child2", null, null, root);
        ComponentPattern child11 = new TextInputComponentPattern("child11", null, null, child1);

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
        ComponentPattern pattern = new TextInputComponentPattern("testName", "testField", null, parent);

        // when
        pattern.initialize(null);

        // then
        Mockito.verify(parent).addFieldEntityIdChangeListener("testField", pattern);
    }

    @Test
    public void shouldHaveDefaultEnabledFlag() throws Exception {
        // given
        AbstractComponentPattern pattern = new TextInputComponentPattern("testName", null, null, null);
        pattern.setDefaultEnabled(true);

        // then
        assertTrue(pattern.isDefaultEnabled());
    }

    @Test
    public void shouldHaveDefaultVisibleFlag() throws Exception {
        // given
        AbstractComponentPattern pattern = new TextInputComponentPattern("testName", null, null, null);
        pattern.setDefaultVisible(true);

        // then
        assertTrue(pattern.isDefaultVisible());
    }

    @Test
    public void shouldHaveHasDescriptionFlag() throws Exception {
        // given
        AbstractComponentPattern pattern = new TextInputComponentPattern("testName", null, null, null);
        pattern.setHasDescription(true);

        // then
        assertTrue(pattern.isHasDescription());
    }

    @Test
    public void shouldHaveReferenceName() throws Exception {
        // given
        AbstractComponentPattern pattern = new TextInputComponentPattern("testName", null, null, null);
        pattern.setReference("uniqueReferenceName");

        // then
        assertEquals("uniqueReferenceName", pattern.getReference());
    }
}
