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

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.materialRequirements.internal.MaterialRequirementService;
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
    public void shouldReturnTrueWhenCheckIfInputProductsRequiredForTypeIsSelectedIfIsSelected() {
        // given
        given(
                materialRequirementService.checkIfInputProductsRequiredForTypeIsSelected(orderDD, order,
                        INPUT_PRODUCTS_REQUIRED_FOR_TYPE, "orders.order.message.inputProductsRequiredForTypeIsNotSelected"))
                .willReturn(true);

        // when
        boolean result = orderHooksMR.checkIfInputProductsRequiredForTypeIsSelected(orderDD, order);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenCheckIfInputProductsRequiredForTypeIsSelectedIfIsSelected() {
        // given
        given(
                materialRequirementService.checkIfInputProductsRequiredForTypeIsSelected(orderDD, order,
                        INPUT_PRODUCTS_REQUIRED_FOR_TYPE, "orders.order.message.inputProductsRequiredForTypeIsNotSelected"))
                .willReturn(false);

        // when
        boolean result = orderHooksMR.checkIfInputProductsRequiredForTypeIsSelected(orderDD, order);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldSetInputProductsRequiredForTypeDefaultValue() {
        // given
        given(parameterService.getParameter()).willReturn(parameter);
        given(parameter.getStringField(INPUT_PRODUCTS_REQUIRED_FOR_TYPE)).willReturn(START_ORDER.getStringValue());

        // when
        orderHooksMR.setInputProductsRequiredForTypeDefaultValue(orderDD, order);

        // then
        Mockito.verify(materialRequirementService).setInputProductsRequiredForTypeDefaultValue(order,
                INPUT_PRODUCTS_REQUIRED_FOR_TYPE, START_ORDER.getStringValue());
    }

}
