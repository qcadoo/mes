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
package com.qcadoo.mes.materialRequirements.hooks;

import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.materialRequirements.MaterialRequirementService;
import com.qcadoo.mes.materialRequirements.constants.InputProductsRequiredForType;
import com.qcadoo.mes.materialRequirements.constants.OrderFieldsMR;
import com.qcadoo.mes.materialRequirements.constants.ParameterFieldsMR;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class OrderHooksMRTest {

    private OrderHooksMR orderHooksMR;

    @Mock
    private MaterialRequirementService materialRequirementService;

    @Mock
    private ParameterService parameterService;

    @Mock
    private DataDefinition orderDD;

    @Mock
    private Entity order, parameter;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderHooksMR = new OrderHooksMR();

        ReflectionTestUtils.setField(orderHooksMR, "materialRequirementService", materialRequirementService);
        ReflectionTestUtils.setField(orderHooksMR, "parameterService", parameterService);
    }

    @Test
    public void shouldOnCreateSetInputProductsRequiredForTypeDefaultValue() {
        // given
        given(parameterService.getParameter()).willReturn(parameter);
        given(parameter.getStringField(ParameterFieldsMR.INPUT_PRODUCTS_REQUIRED_FOR_TYPE)).willReturn(
                InputProductsRequiredForType.START_ORDER.getStringValue());

        // when
        orderHooksMR.onCreate(orderDD, order);

        // then
        Mockito.verify(materialRequirementService).setInputProductsRequiredForTypeDefaultValue(order,
                OrderFieldsMR.INPUT_PRODUCTS_REQUIRED_FOR_TYPE, InputProductsRequiredForType.START_ORDER.getStringValue());
    }

    @Test
    public void shouldOnCopySetInputProductsRequiredForTypeDefaultValue() {
        // given
        given(parameterService.getParameter()).willReturn(parameter);
        given(parameter.getStringField(ParameterFieldsMR.INPUT_PRODUCTS_REQUIRED_FOR_TYPE)).willReturn(
                InputProductsRequiredForType.START_ORDER.getStringValue());

        // when
        orderHooksMR.onCopy(orderDD, order);

        // then
        Mockito.verify(materialRequirementService).setInputProductsRequiredForTypeDefaultValue(order,
                OrderFieldsMR.INPUT_PRODUCTS_REQUIRED_FOR_TYPE, InputProductsRequiredForType.START_ORDER.getStringValue());
    }

}
