package com.qcadoo.mes.lineChangeoverNorms.hooks;

import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.CHANGEOVER_TYPE;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY_GROUP;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY_GROUP;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

public class LineChangeoverNormsDetailsHooksTest {

    private LineChangeoverNormsDetailsHooks hooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private ComponentState state;

    @Mock
    private FieldComponent changeoverType, fromTechnology, toTechnology, fromGroupTech, toGroupTech;

    @Before
    public void init() {
        hooks = new LineChangeoverNormsDetailsHooks();

        MockitoAnnotations.initMocks(this);
        when(view.getComponentByReference(CHANGEOVER_TYPE)).thenReturn(changeoverType);
        when(view.getComponentByReference(FROM_TECHNOLOGY)).thenReturn(fromTechnology);
        when(view.getComponentByReference(TO_TECHNOLOGY)).thenReturn(toTechnology);
        when(view.getComponentByReference(FROM_TECHNOLOGY_GROUP)).thenReturn(fromGroupTech);
        when(view.getComponentByReference(TO_TECHNOLOGY_GROUP)).thenReturn(toGroupTech);
    }

    @Test
    public void shouldInvisibleField() throws Exception {
        // given

        when(changeoverType.getFieldValue()).thenReturn("01forTechnology");
        // when
        hooks.invisibleAndSetRequiredFields(view, state, null);
        // then

        Mockito.verify(fromTechnology).setVisible(true);
        Mockito.verify(toTechnology).setVisible(true);
        Mockito.verify(fromGroupTech).setVisible(false);
        Mockito.verify(toGroupTech).setVisible(false);
    }

    @Test
    public void shouldSetRequiredField() throws Exception {
        // given
        when(changeoverType.getFieldValue()).thenReturn("02forTechnologyGroup");

        // when
        hooks.invisibleAndSetRequiredFields(view, state, null);
        // then
        Mockito.verify(fromTechnology).setRequired(false);
        Mockito.verify(toTechnology).setRequired(false);
        Mockito.verify(fromGroupTech).setRequired(true);
        Mockito.verify(toGroupTech).setRequired(true);
    }

    @Test
    public void shouldClearFieldValue() throws Exception {
        // given
        when(changeoverType.getFieldValue()).thenReturn("02forTechnologyGroup");

        // when
        hooks.invisibleAndSetRequiredFields(view, state, null);

        // then
        Mockito.verify(fromTechnology).setFieldValue(null);
        Mockito.verify(toTechnology).setFieldValue(null);
        Mockito.verify(fromGroupTech).setFieldValue(null);
        Mockito.verify(toGroupTech).setFieldValue(null);
    }
}
