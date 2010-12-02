package com.qcadoo.mes.view.states;

import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ScopeEntityIdChangeListener;
import com.qcadoo.mes.view.components.FormComponentState;

public class ScopeEntityIdChangeListenerTest extends AbstractStateTest {

    @Test
    public void shouldHaveScopeListeners() throws Exception {
        // given
        ComponentState component1 = createMockComponent("component1");
        ComponentState component2 = createMockComponent("component2");

        FormComponentState container = new FormComponentState(null);
        container.addScopeEntityIdChangeListener("component1", (ScopeEntityIdChangeListener) component1);
        container.addScopeEntityIdChangeListener("component2", (ScopeEntityIdChangeListener) component2);

        // when
        container.setFieldValue(13L);

        // then
        verify((ScopeEntityIdChangeListener) component1).onScopeEntityIdChange(13L);
        verify((ScopeEntityIdChangeListener) component2).onScopeEntityIdChange(13L);
    }

}
