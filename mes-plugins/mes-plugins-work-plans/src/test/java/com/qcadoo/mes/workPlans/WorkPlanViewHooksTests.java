package com.qcadoo.mes.workPlans;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

public class WorkPlanViewHooksTests {

    private WorkPlanViewHooks workPlanViewHooks;

    @Mock
    private FieldComponent generated;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent form;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        workPlanViewHooks = new WorkPlanViewHooks();

        when(view.getComponentByReference("form")).thenReturn(form);
        when(view.getComponentByReference("generated")).thenReturn(generated);
    }

    @Test
    public void shouldDisableFormIfDocumentIsGenerated() {
        // given
        when(generated.getFieldValue()).thenReturn("1");

        // when
        workPlanViewHooks.disableFormForGeneratedWorkPlan(view);

        // then
        verify(form).setFormEnabled(false);
    }

    @Test
    public void shoulntDisableFormIfDocumentIsntGenerated() {
        // given
        when(generated.getFieldValue()).thenReturn("0");

        // when
        workPlanViewHooks.disableFormForGeneratedWorkPlan(view);

        // then
        verify(form, never()).setFormEnabled(false);
    }
}
