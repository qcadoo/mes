package com.qcadoo.mes.lineChangeoverNorms.hooks;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

public class MatchingChangeoverNormsDetailsHooksTest {

    private MatchingChangeoverNormsDetailsHooks hooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    FormComponent form;

    @Mock
    ComponentState matchingNorm, matchingNormNotFound;

    @Before
    public void init() {
        hooks = new MatchingChangeoverNormsDetailsHooks();
        MockitoAnnotations.initMocks(this);

        when(view.getComponentByReference("form")).thenReturn(form);
        when(view.getComponentByReference("matchingNorm")).thenReturn(matchingNorm);
        when(view.getComponentByReference("matchingNormNotFound")).thenReturn(matchingNormNotFound);
    }

    @Test
    public void shouldInvisibleBorderLayoutWhenNormsNotFound() throws Exception {
        // given
        when(form.getEntityId()).thenReturn(null);

        // when
        hooks.invisibleField(view);
        // then

        verify(matchingNorm).setVisible(false);
        verify(matchingNormNotFound).setVisible(true);
    }

    @Test
    public void shouldInvisibleLabelWhenNormsFound() throws Exception {
        // given
        when(form.getEntityId()).thenReturn(1L);

        // when
        hooks.invisibleField(view);
        // then

        verify(matchingNorm).setVisible(true);
        verify(matchingNormNotFound).setVisible(false);
    }
}
