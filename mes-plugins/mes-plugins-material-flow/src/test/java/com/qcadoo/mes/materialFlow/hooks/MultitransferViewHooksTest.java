package com.qcadoo.mes.materialFlow.hooks;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

public class MultitransferViewHooksTest {

    private MultitransferViewHooks multitransferViewHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FieldComponent time, type;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        multitransferViewHooks = new MultitransferViewHooks();
    }

    @Test
    public void shouldMakeTimeAndTypeFieldsRequired() {
        // given
        given(view.getComponentByReference("time")).willReturn(time);
        given(view.getComponentByReference("type")).willReturn(type);

        // when
        multitransferViewHooks.makeAllFieldsRequired(view);

        // then
        verify(time).setRequired(true);
        verify(type).setRequired(true);
    }
}
