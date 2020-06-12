/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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

import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class AssignmentToShiftReportHooksTest {

    private AssignmentToShiftReportHooks hooks;

    @Mock
    private ShiftsService shiftsService;

    @Mock
    private DataDefinition assignmentToShiftReportDD;

    @Mock
    private Entity assignmentToShiftReport;

    @Before
    public void init() {
        hooks = new AssignmentToShiftReportHooks();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(hooks, "shiftsService", shiftsService);
    }

    @Test
    public void shouldReturnFalseWhenCheckIfIsMoreThatFiveDays() {
        // given
        given(shiftsService.getNumberOfDaysBetweenGivenDates(Mockito.anyObject(), Mockito.anyObject())).willReturn(10);

        // when
        boolean result = hooks.checkIfIsMoreThatFiveDays(assignmentToShiftReportDD, assignmentToShiftReport);

        // then
        Assert.assertFalse(result);

        verify(assignmentToShiftReport, times(2)).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnTrueWhenCheckIfIsMoreThatFiveDays() {
        // given
        given(shiftsService.getNumberOfDaysBetweenGivenDates(Mockito.anyObject(), Mockito.anyObject())).willReturn(3);

        // when
        boolean result = hooks.checkIfIsMoreThatFiveDays(assignmentToShiftReportDD, assignmentToShiftReport);

        // then
        Assert.assertTrue(result);

        verify(assignmentToShiftReport, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldClearGenerated() {
        // given

        // when
        hooks.clearGenerated(assignmentToShiftReportDD, assignmentToShiftReport);

        // then
        verify(assignmentToShiftReport, times(2)).setField(Mockito.anyString(), Mockito.any());
    }

}
