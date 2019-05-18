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
package com.qcadoo.mes.orderSupplies.states;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.materialRequirements.constants.OrderFieldsMR;
import com.qcadoo.mes.operationalTasks.constants.InputProductsRequiredForType;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.model.api.Entity;

public class OrderSuppliesOrderStateValidationServiceTest {

    private OrderSuppliesOrderStateValidationService orderSuppliesOrderStateValidationService;

    @Mock
    private StateChangeContext stateChangeContext;

    @Mock
    private Entity order;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderSuppliesOrderStateValidationService = new OrderSuppliesOrderStateValidationService();
    }

    @Test
    public void shouldAddErrorWhenValidatorOnAccepted() {
        // given
        given(stateChangeContext.getOwner()).willReturn(order);
        given(order.getStringField(OrderFieldsMR.INPUT_PRODUCTS_REQUIRED_FOR_TYPE)).willReturn(
                InputProductsRequiredForType.START_OPERATIONAL_TASK.getStringValue());
        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(
                TypeOfProductionRecording.CUMULATED.getStringValue());

        // when
        orderSuppliesOrderStateValidationService.validationOnAccepted(stateChangeContext);

        // then
        verify(stateChangeContext).addFieldValidationError(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void shouldntAddErrorWhenValidatorOnAccepted() {
        // given
        given(stateChangeContext.getOwner()).willReturn(order);
        given(order.getStringField(OrderFieldsMR.INPUT_PRODUCTS_REQUIRED_FOR_TYPE)).willReturn(
                InputProductsRequiredForType.START_OPERATIONAL_TASK.getStringValue());
        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(
                TypeOfProductionRecording.FOR_EACH.getStringValue());

        // when
        orderSuppliesOrderStateValidationService.validationOnAccepted(stateChangeContext);

        // then
        verify(stateChangeContext, never()).addFieldValidationError(Mockito.anyString(), Mockito.anyString());
    }

}
