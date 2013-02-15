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
package com.qcadoo.mes.assignmentToShift.hooks;

import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.STATE;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.GridComponent;

public class AssignmentToShiftDetailsHooksTest {

    private AssignmentToShiftDetailsHooks detailsHooks;

    @Mock
    private FieldComponent stateField;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private GridComponent grid;

    @Before
    public void init() {
        detailsHooks = new AssignmentToShiftDetailsHooks();

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldDisabledGridWhenStateIsAccepted() {
        // given
        String state = "02accepted";

        given(view.getComponentByReference(STATE)).willReturn(stateField);
        given(stateField.getFieldValue()).willReturn(state);
        given(view.getComponentByReference(STAFF_ASSIGNMENT_TO_SHIFTS)).willReturn(grid);

        // when
        detailsHooks.disabledGridWhenStateIsAcceptedOrCorrected(view);

        // then
        verify(grid).setEditable(false);
    }

    @Test
    public void shouldDisabledGridWhenStateIsCorrected() {
        // given
        String state = "04corrected";

        given(view.getComponentByReference(STATE)).willReturn(stateField);
        given(stateField.getFieldValue()).willReturn(state);
        given(view.getComponentByReference(STAFF_ASSIGNMENT_TO_SHIFTS)).willReturn(grid);

        // when
        detailsHooks.disabledGridWhenStateIsAcceptedOrCorrected(view);

        // then
        verify(grid).setEditable(false);
    }

    @Test
    public void shouldEnabledGridWhenStateIsDraft() {
        // given
        String state = "01draft";

        given(view.getComponentByReference(STATE)).willReturn(stateField);
        given(stateField.getFieldValue()).willReturn(state);
        given(view.getComponentByReference(STAFF_ASSIGNMENT_TO_SHIFTS)).willReturn(grid);

        // when
        detailsHooks.disabledGridWhenStateIsAcceptedOrCorrected(view);

        // then
        verify(grid).setEditable(true);
    }

}
