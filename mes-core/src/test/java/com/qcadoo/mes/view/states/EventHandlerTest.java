package com.qcadoo.mes.view.states;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.components.FormComponentState;

public class EventHandlerTest {

    @Test
    public void shouldCallEventMethod() throws Exception {
        // given
        FormComponentState component = new FormComponentState();
        component.setFieldValue(13L);

        // when
        component.performEvent("clear");

        // then
        assertNull("value is " + component.getFieldValue(), component.getFieldValue());

    }

    @Test
    public void shouldCallCustomEventMethod() throws Exception {
        // given
        CustomEventBean bean = mock(CustomEventBean.class);
        FormComponentState component = new FormComponentState();
        component.registemCustomEvent("custom", bean, "customMethod");

        // when
        component.performEvent("custom", "arg0", "arg1");

        // then
        Mockito.verify(bean).customMethod(component, new String[] { "arg0", "arg1" });

    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenEventNotExists() throws Exception {
        // given
        FormComponentState component = new FormComponentState();
        component.setFieldValue(13L);

        // when
        component.performEvent("noSuchMethod");
    }

    // shouldNotCallEventBeforeInitialazing

    private interface CustomEventBean {

        void customMethod(ComponentState componentState, String[] args);

    }

}
