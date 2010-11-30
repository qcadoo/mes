package com.qcadoo.mes.newview.patterns;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import com.qcadoo.mes.newview.AbstractComponentPattern;
import com.qcadoo.mes.newview.AbstractComponentState;
import com.qcadoo.mes.newview.ComponentPattern;
import com.qcadoo.mes.newview.ComponentState;
import com.qcadoo.mes.newview.FieldEntityIdChangeListener;
import com.qcadoo.mes.newview.ScopeEntityIdChangeListener;
import com.qcadoo.mes.newview.ViewDefinition;
import com.qcadoo.mes.newview.ViewDefinitionState;
import com.qcadoo.mes.newview.components.TextInputComponentPattern;

public class FieldAndScopeListenerPatternTest {

    @Test
    public void shouldHaveFieldListeners() throws Exception {
        // given
        ViewDefinition vd = mock(ViewDefinition.class);

        ComponentPatternMock pattern = new ComponentPatternMock("f1", null, null, null);

        ComponentPatternMock t1 = new ComponentPatternMock("t1", "t1", null, pattern);
        ComponentPatternMock t2 = new ComponentPatternMock("t2", "t2", null, pattern);

        pattern.addChild(t1);
        pattern.addChild(t2);

        pattern.initialize(vd);

        // when
        Map<String, ComponentPattern> listeners = pattern.getFieldEntityIdChangeListeners();

        // then
        Assert.assertEquals(2, listeners.size());
        Assert.assertEquals(t1, listeners.get("t1"));
        Assert.assertEquals(t2, listeners.get("t2"));
    }

    @Test
    public void shouldUpdateStateFieldListeners() throws Exception {
        // given
        ViewDefinition vd = mock(ViewDefinition.class);

        ComponentPatternMock pattern = new ComponentPatternMock("f1", null, null, null);
        ComponentPatternMock t1 = new ComponentPatternMock("t1", "field1", null, pattern);
        ComponentPatternMock t2 = new ComponentPatternMock("t2", "field2", null, pattern);

        pattern.addChild(t1);
        pattern.addChild(t2);

        pattern.initialize(vd);

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

    @Test
    public void shouldHaveScopeListeners() throws Exception {
        // given
        ViewDefinition vd = mock(ViewDefinition.class);

        ComponentPatternMock pattern = new ComponentPatternMock("f1", null, null, null);

        ComponentPatternMock t1 = new ComponentPatternMock("t1", null, "t1", pattern);
        ComponentPatternMock t2 = new ComponentPatternMock("t2", null, "t2", pattern);

        pattern.addChild(t1);
        pattern.addChild(t2);

        pattern.initialize(vd);

        // when
        Map<String, ComponentPattern> listeners = pattern.getScopeEntityIdChangeListeners();

        // then
        Assert.assertEquals(2, listeners.size());
        Assert.assertEquals(t1, listeners.get("t1"));
        Assert.assertEquals(t2, listeners.get("t2"));
    }

    @Test
    public void shouldUpdateStateScopeListeners() throws Exception {
        // given
        ViewDefinition vd = mock(ViewDefinition.class);

        ComponentPatternMock pattern = new ComponentPatternMock("f1", null, null, null);
        ComponentPatternMock t1 = new ComponentPatternMock("t1", null, "field1", pattern);
        ComponentPatternMock t2 = new ComponentPatternMock("t2", null, "field2", pattern);

        pattern.addChild(t1);
        pattern.addChild(t2);

        pattern.initialize(vd);

        AbstractComponentState f1State = Mockito.mock(AbstractComponentState.class);

        ComponentState t1State = Mockito.mock(ComponentState.class,
                withSettings().extraInterfaces(ScopeEntityIdChangeListener.class));

        ComponentState t2State = Mockito.mock(ComponentState.class,
                withSettings().extraInterfaces(ScopeEntityIdChangeListener.class));

        ViewDefinitionState vds = Mockito.mock(ViewDefinitionState.class);
        BDDMockito.given(vds.getComponentByPath("f1")).willReturn(f1State);
        BDDMockito.given(vds.getComponentByPath("f1.t1")).willReturn(t1State);
        BDDMockito.given(vds.getComponentByPath("f1.t2")).willReturn(t2State);

        // when
        pattern.updateComponentStateListeners(vds);

        // then
        Mockito.verify(f1State).addScopeEntityIdChangeListener("field1", (ScopeEntityIdChangeListener) t1State);
        Mockito.verify(f1State).addScopeEntityIdChangeListener("field2", (ScopeEntityIdChangeListener) t2State);
    }

    @Test
    public void shouldAddItselfToRelationScopeComponentWhenComplexFieldPath() throws Exception {
        // given
        AbstractComponentPattern parent = Mockito.mock(AbstractComponentPattern.class);
        TextInputComponentPattern pattern = new TextInputComponentPattern("testName", null, "#{testComponent}.testField", parent);
        AbstractComponentPattern testComponent = Mockito.mock(AbstractComponentPattern.class);
        ViewDefinition vd = Mockito.mock(ViewDefinition.class);
        given(vd.getComponentByPath("testComponent")).willReturn(testComponent);

        // when
        pattern.initialize(vd);

        // then
        Mockito.verify(testComponent).addScopeEntityIdChangeListener("testField", pattern);
    }

}
