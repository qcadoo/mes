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
package com.qcadoo.mes.costCalculation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class CostCalculationModelValidatorsTest {

    private CostCalculationModelValidators costCalculationModelValidators;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity costCalculation, technology;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        costCalculationModelValidators = new CostCalculationModelValidators();

    }

    @Test
    public void shouldTechnologyHasIncorrectState() {
        // given
        when(costCalculation.getBelongsToField("technology")).thenReturn(technology);
        when(technology.getStringField("state")).thenReturn("01draft");

        // when
        boolean result = costCalculationModelValidators.checkIfTheTechnologyHasCorrectState(dataDefinition, costCalculation);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldTechnologyHasCorrectState() {
        // given
        when(costCalculation.getBelongsToField("technology")).thenReturn(technology);
        when(technology.getStringField("state")).thenReturn("05checked");

        // when
        boolean result = costCalculationModelValidators.checkIfTheTechnologyHasCorrectState(dataDefinition, costCalculation);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenCurrencyGlobalIsSelected() throws Exception {
        // given
        when(costCalculation.getField("sourceOfMaterialCosts")).thenReturn("01currentGlobalDefinitionsInProduct");
        when(costCalculation.getField("calculateMaterialCostsMode")).thenReturn("04costForOrder");

        // when
        boolean result = costCalculationModelValidators.checkIfCurrentGlobalIsSelected(dataDefinition, costCalculation);
        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueWhenCalculateMaterialCostsModeIsNominal() throws Exception {
        // given
        when(costCalculation.getField("sourceOfMaterialCosts")).thenReturn("01currentGlobalDefinitionsInProduct");
        when(costCalculation.getField("calculateMaterialCostsMode")).thenReturn("01nominal");

        // when
        boolean result = costCalculationModelValidators.checkIfCurrentGlobalIsSelected(dataDefinition, costCalculation);
        // then
        assertTrue(result);
    }

}
