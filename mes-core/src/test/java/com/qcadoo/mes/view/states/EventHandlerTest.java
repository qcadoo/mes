package com.qcadoo.mes.view.states;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.form.FormComponentState;

public class EventHandlerTest {

    @Test
    public void shouldCallEventMethod() throws Exception {
        // given
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        FormComponentState component = new FormComponentState(null, null);
        component.setFieldValue(13L);

        // when
        component.performEvent(viewDefinitionState, "clear");

        // then
        assertNull("value is " + component.getFieldValue(), component.getFieldValue());

    }

    @Test
    public void shouldCallCustomEventMethod() throws Exception {
        // given
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        CustomEventBean bean = mock(CustomEventBean.class);
        FormComponentState component = new FormComponentState(null, null);
        component.registerCustomEvent("custom", bean, "customMethod");

        // when
        component.performEvent(viewDefinitionState, "custom", "arg0", "arg1");

        // then
        Mockito.verify(bean).customMethod(viewDefinitionState, component, new String[] { "arg0", "arg1" });
    }

    @Test
    public void shouldCallMultipleEventMethods() throws Exception {
        // given
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        FormComponentState component = new FormComponentState(null, null);
        component.setFieldValue(13L);

        CustomEventBean bean1 = mock(CustomEventBean.class);
        component.registerCustomEvent("clear", bean1, "customMethod");

        CustomEventBean bean2 = mock(CustomEventBean.class);
        component.registerCustomEvent("clear", bean2, "customMethod");

        // when
        component.performEvent(viewDefinitionState, "clear");

        // then
        assertNull("value is " + component.getFieldValue(), component.getFieldValue());
        Mockito.verify(bean1).customMethod(viewDefinitionState, component, new String[0]);
        Mockito.verify(bean2).customMethod(viewDefinitionState, component, new String[0]);
    }

    @Test
    public void shouldNotThrowExceptionWhenEventNotExists() throws Exception {
        // given
        ViewDefinitionState viewDefinitionState = mock(ViewDefinitionState.class);

        FormComponentState component = new FormComponentState(null, null);
        component.setFieldValue(13L);

        // when
        component.performEvent(viewDefinitionState, "noSuchMethod");
    }

    // shouldNotCallEventBeforeInitialazing

    private interface CustomEventBean {

        void customMethod(ViewDefinitionState viewDefinitionState, ComponentState componentState, String[] args);

    }

}
