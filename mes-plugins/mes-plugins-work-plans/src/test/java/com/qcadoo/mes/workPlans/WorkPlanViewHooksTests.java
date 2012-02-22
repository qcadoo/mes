/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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
