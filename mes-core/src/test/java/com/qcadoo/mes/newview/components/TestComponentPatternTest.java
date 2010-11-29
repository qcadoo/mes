package com.qcadoo.mes.newview.components;

import static org.mockito.Mockito.withSettings;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import com.qcadoo.mes.newview.AbstractComponentState;
import com.qcadoo.mes.newview.ComponentPattern;
import com.qcadoo.mes.newview.ComponentState;
import com.qcadoo.mes.newview.FieldEntityIdChangeListener;
import com.qcadoo.mes.newview.ViewDefinitionState;

public class TestComponentPatternTest {

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
    public void shouldHaveFieldListeners() throws Exception {
        // given
        TestComponentPattern pattern = new TestComponentPattern("f1", null, null, null);

        TestComponentPattern t1 = new TestComponentPattern("t1", "t1", null, pattern);
        TestComponentPattern t2 = new TestComponentPattern("t2", "t2", null, pattern);
        pattern.addChild(t1);
        pattern.addChild(t2);

        pattern.initialize(null);

        // when
        Map<String, ComponentPattern> listeners = pattern.getFieldEntityIdChangeListeners();

        // then
        Assert.assertEquals(2, listeners.size());
        Assert.assertEquals(t1, listeners.get("t1"));
        Assert.assertEquals(t2, listeners.get("t2"));
    }

    @Test
    public void shouldUpdateStateListeners() throws Exception {
        // given
        TestComponentPattern pattern = new TestComponentPattern("f1", null, null, null);
        TestComponentPattern t1 = new TestComponentPattern("t1", "field1", null, pattern);
        TestComponentPattern t2 = new TestComponentPattern("t2", "field2", null, pattern);
        pattern.addChild(t1);
        pattern.addChild(t2);
        pattern.initialize(null);

        AbstractComponentState f1State = Mockito.mock(AbstractComponentState.class);
        ComponentState t1State = Mockito.mock(ComponentState.class,
                withSettings().extraInterfaces(FieldEntityIdChangeListener.class));
        ComponentState t2State = Mockito.mock(ComponentState.class,
                withSettings().extraInterfaces(FieldEntityIdChangeListener.class));

        ViewDefinitionState vds = Mockito.mock(ViewDefinitionState.class);
        BDDMockito.given(vds.getComponentByPath("f1")).willReturn(f1State);
        BDDMockito.given(vds.getComponentByPath("f1.t1")).willReturn(t1State);
        BDDMockito.given(vds.getComponentByPath("f1.t2")).willReturn(t2State);

        // when
        pattern.updateComponentStateListeners(vds);

        // then
        Mockito.verify(f1State).addFieldEntityIdChangeListener("field1", (FieldEntityIdChangeListener) t1State);
        Mockito.verify(f1State).addFieldEntityIdChangeListener("field2", (FieldEntityIdChangeListener) t2State);
    }
}
