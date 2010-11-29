package com.qcadoo.mes.newview.components;

import static org.mockito.BDDMockito.given;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.newview.ComponentPattern;
import com.qcadoo.mes.newview.ComponentState;
import com.qcadoo.mes.newview.ViewDefinition;

public class FormComponentPatternTest {

    @Test
    public void shouldReturnValidFormComponentStateInstance() throws Exception {
        // given
        FormComponentPattern pattern = new FormComponentPattern(null, null, null, null);

        // when
        ComponentState state = pattern.createComponentState();

        // then
        Assert.assertTrue(state instanceof FormComponentState);
    }

    @Test
    public void shouldHaveChildrenWhenAddSome() throws Exception {
        // given
        FormComponentPattern pattern = new FormComponentPattern(null, null, null, null);
        ComponentPattern child1 = new TextInputComponentPattern("test1", null, null, null);
        ComponentPattern child2 = new TextInputComponentPattern("test2", null, null, null);
        pattern.addChild(child1);
        pattern.addChild(child2);

        // when
        Map<String, ComponentPattern> children = pattern.getChildren();

        // then
        Assert.assertEquals(2, children.size());
        Assert.assertEquals(child1, children.get("test1"));
        Assert.assertEquals(child2, children.get("test2"));
    }

    @Test
    public void shouldReturnChildByName() throws Exception {
        // given
        FormComponentPattern pattern = new FormComponentPattern(null, null, null, null);
        ComponentPattern child1 = new TextInputComponentPattern("test1", null, null, null);
        ComponentPattern child2 = new TextInputComponentPattern("test2", null, null, null);
        pattern.addChild(child1);
        pattern.addChild(child2);

        // when
        ComponentPattern testChild1 = pattern.getChild("test1");
        ComponentPattern testChild2 = pattern.getChild("test2");
        ComponentPattern testChild3 = pattern.getChild("test3");

        // then
        Assert.assertEquals(child1, testChild1);
        Assert.assertEquals(child2, testChild2);
        Assert.assertNull(testChild3);
    }

    @Test
    public void shouldCallInitializeOnChildren() throws Exception {
        // given
        FormComponentPattern pattern = new FormComponentPattern(null, null, null, null);
        ComponentPattern child1 = Mockito.mock(ComponentPattern.class);
        given(child1.getName()).willReturn("test1");
        ComponentPattern child2 = Mockito.mock(ComponentPattern.class);
        given(child2.getName()).willReturn("test2");
        pattern.addChild(child1);
        pattern.addChild(child2);
        ViewDefinition vd = Mockito.mock(ViewDefinition.class);

        // when
        pattern.initialize(vd);

        // then
        Mockito.verify(child1).initialize(vd);
        Mockito.verify(child2).initialize(vd);
    }

}
