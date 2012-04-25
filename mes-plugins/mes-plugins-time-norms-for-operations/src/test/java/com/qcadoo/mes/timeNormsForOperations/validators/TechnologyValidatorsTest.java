/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.5
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
package com.qcadoo.mes.timeNormsForOperations.validators;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.timeNormsForOperations.NormService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class TechnologyValidatorsTest {

    private TechnologyValidators technologyValidators;

    @Mock
    private Entity technology;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity operComp1, operComp2, prod1, prod2, prod1Comp, prod2Comp;

    @Mock
    private NormService normService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        technologyValidators = new TechnologyValidators();

        // BDDMockito.
    }

    @Test
    public void shouldAlwaysPassIfTheTechnologyIsInDraftState() {
        // given
        given(technology.getStringField("state")).willReturn("01draft");

        // when
        boolean isValid = technologyValidators.checkOperationOutputQuantities(dataDefinition, technology);

        // then
        assertTrue(isValid);
    }

    @Ignore
    @Test
    public void shouldPassValidationErrorsToTheEntityForAcceptedTechnology() {
        // given
        given(technology.getStringField("state")).willReturn("02accepted");
        given(technology.getDataDefinition()).willReturn(dataDefinition);
        given(technology.getId()).willReturn(0L);
        given(dataDefinition.get(0L)).willReturn(technology);

        ReflectionTestUtils.setField(technologyValidators, "normService", normService);

        // given(normService.checkOperationOutputQuantities(technology)).willReturn(asList("err1", "err2"));

        // when
        boolean isValid = technologyValidators.checkOperationOutputQuantities(dataDefinition, technology);

        // then
        assertFalse(isValid);
        verify(technology).addGlobalError("err1");
        verify(technology).addGlobalError("err2");
    }
}
