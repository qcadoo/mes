/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.productionPerShift.hooks;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.productionPerShift.report.PPSReportXlsHelper;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

public class PPSReportHooksTest {

    private PPSReportHooks hooks;

    @Mock
    private PPSReportXlsHelper ppsReportXlsHelper;

    @Mock
    private DataDefinition reportDD;

    @Mock
    private Entity report;

    @Before
    public void init() {
        hooks = new PPSReportHooks();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(hooks, "ppsReportXlsHelper", ppsReportXlsHelper);
    }

    @Test
    public void shouldReturnFalseWhenCheckIfIsMoreThatFiveDays() {
        // given
        given(ppsReportXlsHelper.getNumberOfDaysBetweenGivenDates(report)).willReturn(10);

        // when
        boolean result = hooks.checkIfIsMoreThatFiveDays(reportDD, report);

        // then
        Assert.assertFalse(result);

        verify(report, times(2)).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnTrueWhenCheckIfIsMoreThatFiveDays() {
        // given
        given(ppsReportXlsHelper.getNumberOfDaysBetweenGivenDates(report)).willReturn(3);

        // when
        boolean result = hooks.checkIfIsMoreThatFiveDays(reportDD, report);

        // then
        Assert.assertTrue(result);

        verify(report, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldClearGenerated() {
        // given

        // when
        hooks.clearGenerated(reportDD, report);

        // then
        verify(report, times(2)).setField(Mockito.anyString(), Mockito.any());
    }

}
