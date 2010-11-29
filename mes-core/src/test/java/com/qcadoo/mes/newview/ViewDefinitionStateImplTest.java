package com.qcadoo.mes.newview;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

public class ViewDefinitionStateImplTest extends AbstractStateTest {

    @Test
    public void shouldGetComponentByPath() throws Exception {
        // given
        ViewDefinitionState vds = new ViewDefinitionStateImpl();

        ContainerState c1 = mock(ContainerState.class);
        given(c1.getName()).willReturn("c1");
        ContainerState c2 = mock(ContainerState.class);
        given(c2.getName()).willReturn("c2");
        ContainerState c11 = mock(ContainerState.class);

        vds.addChild(c1);
        vds.addChild(c2);
        given(c1.getChild("c11")).willReturn(c11);

        // when
        ComponentState c1Test = vds.getComponentByPath("c1");
        ComponentState c2Test = vds.getComponentByPath("c2");
        ComponentState c11Test = vds.getComponentByPath("c1.c11");

        // then
        Assert.assertEquals(c1, c1Test);
        Assert.assertEquals(c2, c2Test);
        Assert.assertEquals(c11, c11Test);
    }

    @Test
    public void shouldPerformEventOnComponent() throws Exception {
        // given
        ViewDefinitionState vds = new ViewDefinitionStateImpl();

        ContainerState c1 = mock(ContainerState.class);
        given(c1.getName()).willReturn("c1");
        vds.addChild(c1);
        ComponentState c2 = mock(ComponentState.class);
        given(c1.getChild("c2")).willReturn(c2);

        // when
        vds.performEvent("c1.c2", "event", new String[] { "arg1", "arg2" });

        // then
        Mockito.verify(c2).performEvent("event", "arg1", "arg2");
    }
}
