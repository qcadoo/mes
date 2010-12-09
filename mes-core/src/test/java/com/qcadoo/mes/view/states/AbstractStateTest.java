package com.qcadoo.mes.view.states;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.FieldEntityIdChangeListener;
import com.qcadoo.mes.view.ScopeEntityIdChangeListener;

public abstract class AbstractStateTest {

    protected ComponentState createMockComponent(final String name) {
        ComponentState component1 = mock(ComponentState.class,
                withSettings().extraInterfaces(ScopeEntityIdChangeListener.class, FieldEntityIdChangeListener.class));
        given(component1.getName()).willReturn(name);
        return component1;
    }

}
