package com.qcadoo.mes.newview;

import static org.mockito.BDDMockito.given;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.newview.components.TextInputComponentPattern;

public class ViewDefinitionImplTest {

    @Test
    public void shouldHaveChildrenWhenAddSome() throws Exception {
        // given
        ViewDefinitionImpl vd = new ViewDefinitionImpl();
        ComponentPattern child1 = new TextInputComponentPattern("test1", null, null, null);
        ComponentPattern child2 = new TextInputComponentPattern("test2", null, null, null);
        vd.addChild(child1);
        vd.addChild(child2);

        // when
        Map<String, ComponentPattern> children = vd.getChildren();

        // then
        Assert.assertEquals(2, children.size());
        Assert.assertEquals(child1, children.get("test1"));
        Assert.assertEquals(child2, children.get("test2"));
    }

    @Test
    public void shouldReturnChildByName() throws Exception {
        // given
        ViewDefinitionImpl vd = new ViewDefinitionImpl();
        ComponentPattern child1 = new TextInputComponentPattern("test1", null, null, null);
        ComponentPattern child2 = new TextInputComponentPattern("test2", null, null, null);
        vd.addChild(child1);
        vd.addChild(child2);

        // when
        ComponentPattern testChild1 = vd.getChild("test1");
        ComponentPattern testChild2 = vd.getChild("test2");
        ComponentPattern testChild3 = vd.getChild("test3");

        // then
        Assert.assertEquals(child1, testChild1);
        Assert.assertEquals(child2, testChild2);
        Assert.assertNull(testChild3);
    }

    @Test
    public void shouldCallInitializeOnChildren() throws Exception {
        // given
        ViewDefinitionImpl vd = new ViewDefinitionImpl();
        ComponentPattern child1 = Mockito.mock(ComponentPattern.class);
        given(child1.getName()).willReturn("test1");
        ComponentPattern child2 = Mockito.mock(ComponentPattern.class);
        given(child2.getName()).willReturn("test2");
        vd.addChild(child1);
        vd.addChild(child2);

        // when
        vd.initialize();

        // then
        Mockito.verify(child1).initialize(vd);
        Mockito.verify(child2).initialize(vd);
    }

    @Test
    public void shouldFindComponentByPathWhenOneStep() throws Exception {
        // given
        ViewDefinitionImpl vd = new ViewDefinitionImpl();
        ComponentPattern child1 = Mockito.mock(ComponentPattern.class);
        given(child1.getName()).willReturn("test1");
        vd.addChild(child1);

        // when
        ComponentPattern testChild1 = vd.getComponentByPath("test1");

        // then
        Assert.assertEquals(child1, testChild1);
    }

    @Test
    public void shouldFindComponentByPathWhenMoreThanOneStep() throws Exception {
        // given
        ViewDefinitionImpl vd = new ViewDefinitionImpl();

        ContainerPattern child1 = Mockito.mock(ContainerPattern.class);
        ContainerPattern child2 = Mockito.mock(ContainerPattern.class);
        ContainerPattern child3 = Mockito.mock(ContainerPattern.class);

        given(child1.getName()).willReturn("test1");
        given(child1.getChild("test2")).willReturn(child2);
        given(child2.getChild("test3")).willReturn(child3);

        vd.addChild(child1);

        // when
        ComponentPattern testChild1 = vd.getComponentByPath("test1.test2.test3");

        // then
        Assert.assertEquals(child3, testChild1);
    }

    @Test
    public void shouldReturnNullWhenNoComponentFound() throws Exception {
        // given
        ViewDefinitionImpl vd = new ViewDefinitionImpl();

        ContainerPattern child1 = Mockito.mock(ContainerPattern.class);
        ContainerPattern child2 = Mockito.mock(ContainerPattern.class);
        ContainerPattern child3 = Mockito.mock(ContainerPattern.class);

        given(child1.getName()).willReturn("test1");
        given(child1.getChild("test2")).willReturn(child2);
        given(child2.getChild("test3")).willReturn(child3);

        vd.addChild(child1);

        // when
        ComponentPattern testChild1 = vd.getComponentByPath("test1.test2.test4");

        // then
        Assert.assertNull(testChild1);
    }

}
