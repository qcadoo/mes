package com.qcadoo.mes.view.patterns;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ContainerPattern;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FormComponentPattern;
import com.qcadoo.mes.view.components.TextInputComponentPattern;

public class ContainerPatternTest {

    @Test
    public void shouldHaveChildren() throws Exception {
        // given
        ComponentPattern child1 = new TextInputComponentPattern("test1", null, null, null);
        ComponentPattern child2 = new TextInputComponentPattern("test2", null, null, null);

        ContainerPattern pattern = new FormComponentPattern("test", null, null, null);
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
        ComponentPattern child1 = new TextInputComponentPattern("test1", null, null, null);
        ComponentPattern child2 = new TextInputComponentPattern("test2", null, null, null);

        ContainerPattern pattern = new FormComponentPattern("test", null, null, null);
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
    public void shouldNotCallInitializeOnChildren() throws Exception {
        // given
        ViewDefinition vd = Mockito.mock(ViewDefinition.class);

        ComponentPattern child1 = Mockito.mock(ComponentPattern.class);
        given(child1.getName()).willReturn("test1");

        ComponentPattern child2 = Mockito.mock(ComponentPattern.class);
        given(child2.getName()).willReturn("test2");

        ContainerPattern pattern = new FormComponentPattern("test", null, null, null);
        pattern.addChild(child1);
        pattern.addChild(child2);

        // when
        pattern.initialize(vd);

        // then
        Mockito.verify(child1, never()).initialize(vd);
        Mockito.verify(child2, never()).initialize(vd);
    }

    @Test
    public void shouldCallStateOnChildren() throws Exception {
        // given
        ViewDefinitionState vds = Mockito.mock(ViewDefinitionState.class);

        AbstractComponentPattern child1 = Mockito.mock(AbstractComponentPattern.class);
        given(child1.getName()).willReturn("test1");

        AbstractComponentPattern child2 = Mockito.mock(AbstractComponentPattern.class);
        given(child2.getName()).willReturn("test2");

        ComponentPatternMock pattern = new ComponentPatternMock("f1", null, null, null);
        pattern.addChild(child1);
        pattern.addChild(child2);

        // when
        pattern.updateComponentStateListeners(vds);

        // then
        Mockito.verify(child1).updateComponentStateListeners(vds);
        Mockito.verify(child2).updateComponentStateListeners(vds);
    }
}
