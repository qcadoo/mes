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
package com.qcadoo.mes.lineChangeoverNormsForOrders.hooks;

import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.ORDER;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.PREVIOUS_ORDER;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class OrderModelValidatorsLCNFOTest {

    private OrderModelValidatorsLCNFO orderModelValidatorsLCNFO;

    @Mock
    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    @Mock
    private DataDefinition orderDD;

    @Mock
    private Entity order, previousOrderDB, orderDB;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderModelValidatorsLCNFO = new OrderModelValidatorsLCNFO();

        setField(orderModelValidatorsLCNFO, "lineChangeoverNormsForOrdersService", lineChangeoverNormsForOrdersService);
    }

    @Test
    public void shouldReturnFalseWhenCheckIfOrderHasCorrectStateAndIsPreviousIfPreviousOrderIsntCorrectAndPrevious() {
        // given
        given(order.getBelongsToField(PREVIOUS_ORDER)).willReturn(previousOrderDB);
        given(order.getBelongsToField(ORDER)).willReturn(orderDB);

        given(lineChangeoverNormsForOrdersService.checkIfOrderHasCorrectStateAndIsPrevious(previousOrderDB, orderDB)).willReturn(
                false);

        // when
        boolean result = orderModelValidatorsLCNFO.checkIfOrderHasCorrectStateAndIsPrevious(orderDD, order);

        // then
        assertFalse(result);

        verify(order).addError(Mockito.eq(orderDD.getField(PREVIOUS_ORDER)), Mockito.anyString());
    }

    @Test
    public void shouldReturnTrueWhenCheckIfOrderHasCorrectStateAndIsPreviousIfPreviousOrderIsCorrectAndPrevious() {
        // given
        given(order.getBelongsToField(PREVIOUS_ORDER)).willReturn(previousOrderDB);
        given(order.getBelongsToField(ORDER)).willReturn(orderDB);

        given(lineChangeoverNormsForOrdersService.checkIfOrderHasCorrectStateAndIsPrevious(previousOrderDB, orderDB)).willReturn(
                true);

        // when
        boolean result = orderModelValidatorsLCNFO.checkIfOrderHasCorrectStateAndIsPrevious(orderDD, order);

        // then
        assertTrue(result);

        verify(order, never()).addError(Mockito.eq(orderDD.getField(PREVIOUS_ORDER)), Mockito.anyString());
    }
}
