package com.qcadoo.mes.materialFlowMultitransfers.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;
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
        given(view.getComponentByReference(TIME)).willReturn(time);
        given(view.getComponentByReference(TYPE)).willReturn(type);

        // when
        multitransferViewHooks.makeFieldsRequired(view);

        // then
        verify(time).setRequired(true);
        verify(type).setRequired(true);
    }
}
