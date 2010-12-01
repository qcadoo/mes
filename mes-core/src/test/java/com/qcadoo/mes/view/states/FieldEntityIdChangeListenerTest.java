package com.qcadoo.mes.view.states;

import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.FieldEntityIdChangeListener;
import com.qcadoo.mes.view.components.FormComponentState;

public class FieldEntityIdChangeListenerTest extends AbstractStateTest {

    @Test
    public void shouldHaveFieldListeners() throws Exception {
        // given
        ComponentState component1 = createMockComponent("component1");
        ComponentState component2 = createMockComponent("component2");

        FormComponentState container = new FormComponentState();
        container.addFieldEntityIdChangeListener("field1", (FieldEntityIdChangeListener) component1);
        container.addFieldEntityIdChangeListener("field2", (FieldEntityIdChangeListener) component2);

        // when
        container.setFieldValue(13L);

        // then
        verify((FieldEntityIdChangeListener) component1).onFieldEntityIdChange(13L);
        verify((FieldEntityIdChangeListener) component2).onFieldEntityIdChange(13L);
    }

}
