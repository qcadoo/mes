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
package com.qcadoo.mes.costCalculation.validators;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.costCalculation.constants.CalculateMaterialCostsMode;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.constants.SourceOfMaterialCosts;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class CostCalculationValidatorsTest {

    private CostCalculationValidators costCalculationValidators;

    @Mock
    private DataDefinition costCalculationDD;

    @Mock
    private Entity costCalculation, technology;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        costCalculationValidators = new CostCalculationValidators();
    }

    @Test
    public void shouldTechnologyHasIncorrectState() {
        // given
        given(costCalculation.getBelongsToField(CostCalculationFields.TECHNOLOGY)).willReturn(technology);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyStateStringValues.DRAFT);

        // when
        boolean result = costCalculationValidators.checkIfTheTechnologyHasCorrectState(costCalculationDD, costCalculation);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldTechnologyHasCorrectState() {
        // given
        given(costCalculation.getBelongsToField(CostCalculationFields.TECHNOLOGY)).willReturn(technology);
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(TechnologyStateStringValues.CHECKED);

        // when
        boolean result = costCalculationValidators.checkIfTheTechnologyHasCorrectState(costCalculationDD, costCalculation);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenCurrencyGlobalIsSelected() throws Exception {
        // given
        given(costCalculation.getStringField(CostCalculationFields.SOURCE_OF_MATERIAL_COSTS)).willReturn(
                SourceOfMaterialCosts.CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT.getStringValue());
        given(costCalculation.getStringField(CostCalculationFields.CALCULATE_MATERIAL_COSTS_MODE)).willReturn(
                CalculateMaterialCostsMode.COST_FOR_ORDER.getStringValue());

        // when
        boolean result = costCalculationValidators.checkIfCurrentGlobalIsSelected(costCalculationDD, costCalculation);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueWhenCalculateMaterialCostsModeIsNominal() throws Exception {
        // given
        given(costCalculation.getStringField(CostCalculationFields.SOURCE_OF_MATERIAL_COSTS)).willReturn(
                SourceOfMaterialCosts.CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT.getStringValue());
        given(costCalculation.getStringField(CostCalculationFields.CALCULATE_MATERIAL_COSTS_MODE)).willReturn(
                CalculateMaterialCostsMode.NOMINAL.getStringValue());

        // when
        boolean result = costCalculationValidators.checkIfCurrentGlobalIsSelected(costCalculationDD, costCalculation);

        // then
        assertTrue(result);
    }

}
