/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.workPlans.hooks;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.workPlans.constants.WorkPlanFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

public class WorkPlanDetailsHooksTests {

    private static final String L_FORM = "form";

    private WorkPlanDetailsHooks workPlanDetailsHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent workPlanForm;

    @Mock
    private FieldComponent generatedField;

    @Mock
    private ComponentState componentState;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        workPlanDetailsHooks = new WorkPlanDetailsHooks();

        when(view.getComponentByReference(Mockito.anyString())).thenReturn(componentState);
        when(view.getComponentByReference(L_FORM)).thenReturn(workPlanForm);
        when(view.getComponentByReference(WorkPlanFields.GENERATED)).thenReturn(generatedField);
        when(workPlanForm.getEntityId()).thenReturn(1L);
    }

    @Test
    public void shouldDisableFormIfDocumentIsGenerated() {
        // given
        when(generatedField.getFieldValue()).thenReturn("1");

        // when
        workPlanDetailsHooks.disableFormForGeneratedWorkPlan(view);

        // then
        verify(componentState, Mockito.never()).setEnabled(true);
    }

    @Test
    public void shoulntDisableFormIfDocumentIsntGenerated() {
        // given
        when(generatedField.getFieldValue()).thenReturn("0");

        // when
        workPlanDetailsHooks.disableFormForGeneratedWorkPlan(view);

        // then
        verify(componentState, Mockito.times(5)).setEnabled(true);
    }

}
