/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.assignmentToShift.hooks;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

public class AssignmentToShiftDetailsHooksTest {

    public static final String L_FORM = "form";

    private AssignmentToShiftDetailsHooks detailsHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent assignmentToShiftForm;

    @Mock
    private FieldComponent stateField;

    @Mock
    private GridComponent assignmentToShiftGrid;

    @Before
    public void init() {
        detailsHooks = new AssignmentToShiftDetailsHooks();

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldDisableFormWhenStateIsAccepted() {
        // given
        String state = AssignmentToShiftState.ACCEPTED.getStringValue();

        // when

        // detailsHooks.disableFormWhenStateIsAcceptedOrCorrected(view);

        // then
        // verify(assignmentToShiftForm).setFormEnabled(false);
    }

    @Test
    public void shouldDisableFormWhenStateIsCorrected() {
        // given
        String state = AssignmentToShiftState.CORRECTED.getStringValue();

        // when
        // detailsHooks.disableFormWhenStateIsAcceptedOrCorrected(view);

        // then
        // verify(assignmentToShiftForm).setFormEnabled(false);
    }

    @Test
    public void shouldEnableFormWhenStateIsDraft() {
        // given
        String state = AssignmentToShiftState.DRAFT.getStringValue();

        // when
        // detailsHooks.disableFormWhenStateIsAcceptedOrCorrected(view);

        // then
        // verify(assignmentToShiftForm).setFormEnabled(true);
    }

}
