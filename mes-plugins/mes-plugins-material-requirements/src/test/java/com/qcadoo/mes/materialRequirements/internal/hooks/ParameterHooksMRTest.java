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
package com.qcadoo.mes.materialRequirements.internal.hooks;

import static com.qcadoo.mes.materialRequirements.internal.constants.InputProductsRequiredForType.START_ORDER;
import static com.qcadoo.mes.materialRequirements.internal.constants.OrderFieldsMR.INPUT_PRODUCTS_REQUIRED_FOR_TYPE;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.materialRequirements.internal.MaterialRequirementService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class ParameterHooksMRTest {

    private ParameterHooksMR parameterHooksMR;

    @Mock
    private MaterialRequirementService materialRequirementService;

    @Mock
    private DataDefinition parameterDD;

    @Mock
    private Entity parameter;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        parameterHooksMR = new ParameterHooksMR();

        ReflectionTestUtils.setField(parameterHooksMR, "materialRequirementService", materialRequirementService);
    }

    @Test
    public void shouldReturnTrueWhenCheckIfInputProductsRequiredForTypeIsSelectedIfIsSelected() {
        // given
        given(
                materialRequirementService.checkIfInputProductsRequiredForTypeIsSelected(parameterDD, parameter,
                        INPUT_PRODUCTS_REQUIRED_FOR_TYPE, "basic.parameter.message.inputProductsRequiredForTypeIsNotSelected"))
                .willReturn(true);

        // when
        boolean result = parameterHooksMR.checkIfInputProductsRequiredForTypeIsSelected(parameterDD, parameter);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenCheckIfInputProductsRequiredForTypeIsSelectedIfIsSelected() {
        // given
        given(
                materialRequirementService.checkIfInputProductsRequiredForTypeIsSelected(parameterDD, parameter,
                        INPUT_PRODUCTS_REQUIRED_FOR_TYPE, "basic.parameter.message.inputProductsRequiredForTypeIsNotSelected"))
                .willReturn(false);

        // when
        boolean result = parameterHooksMR.checkIfInputProductsRequiredForTypeIsSelected(parameterDD, parameter);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldSetInputProductsRequiredForTypeDefaultValue() {
        // given

        // when
        parameterHooksMR.setInputProductsRequiredForTypeDefaultValue(parameterDD, parameter);

        // then
        Mockito.verify(materialRequirementService).setInputProductsRequiredForTypeDefaultValue(parameter,
                INPUT_PRODUCTS_REQUIRED_FOR_TYPE, START_ORDER.getStringValue());
    }

}
