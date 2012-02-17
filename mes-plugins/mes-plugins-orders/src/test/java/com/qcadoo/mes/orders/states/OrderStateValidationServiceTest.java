/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
package com.qcadoo.mes.orders.states;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.mes.basic.ShiftsServiceImpl;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;

public class OrderStateValidationServiceTest {

    private OrderStateValidationService orderStateValidationService;

    private DataDefinitionService dataDefinitionService;

    private DataDefinition dataDefinition;

    private SecurityService securityService;

    private ShiftsServiceImpl shiftsServiceImpl;

    private Entity entity;

    private Entity order;

    private Entity shift;

    private String previousState;

    private String currentState;

    @Before
    public void init() {
        orderStateValidationService = new OrderStateValidationService();

        dataDefinitionService = mock(DataDefinitionService.class);
        securityService = mock(SecurityService.class);
        shiftsServiceImpl = mock(ShiftsServiceImpl.class);
        entity = mock(Entity.class);
        order = mock(Entity.class);
        dataDefinition = mock(DataDefinition.class);
        shift = mock(Entity.class);

        setField(orderStateValidationService, "dataDefinitionService", dataDefinitionService);
        setField(orderStateValidationService, "securityService", securityService);
        setField(orderStateValidationService, "shiftsServiceImpl", shiftsServiceImpl);
    }

    @Test
    public void shouldSetValueToEntity() throws Exception {
        // given

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_LOGGING)).willReturn(
                dataDefinition);
        given(dataDefinition.create()).willReturn(entity);
        given(shiftsServiceImpl.getShiftFromDate(Mockito.any(Date.class))).willReturn(shift);
        given(securityService.getCurrentUserName()).willReturn("userName");
        given(entity.getDataDefinition()).willReturn(dataDefinition);

        orderStateValidationService.saveLogging(order, previousState, currentState);
        // then
        verify(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_LOGGING)).create();
        verify(entity).setField("order", order);
        verify(entity).setField("previousState", previousState);
        verify(entity).setField("currentState", currentState);
        verify(entity).setField("shift", shift);
        verify(entity).setField("worker", "userName");
        verify(dataDefinition).save(entity);
    }

    @Test
    public void shouldThrowExceptionWhenShiftIsNull() throws Exception {
        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_LOGGING)).willReturn(
                dataDefinition);
        given(dataDefinition.create()).willReturn(entity);
        given(shiftsServiceImpl.getShiftFromDate(Mockito.any(Date.class))).willReturn(null);
        given(entity.getDataDefinition()).willReturn(dataDefinition);

        orderStateValidationService.saveLogging(order, previousState, currentState);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenEntityIsNullValidationAccepted() throws Exception {
        // when
        orderStateValidationService.validationAccepted(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenEntityIsNullValidationInProgress() throws Exception {
        // when
        orderStateValidationService.validationInProgress(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenEntityIsNullValidationCompleted() throws Exception {
        // when
        orderStateValidationService.validationCompleted(null);
    }

    @Test
    public void shouldReturnErrorWhenValidatingAccepted() throws Exception {
        // when
        orderStateValidationService.validationAccepted(order);
    }

    @Test
    public void shouldPerformValidationAccepted() throws Exception {
        // given
        Mockito.when(order.getField(Mockito.anyString())).thenReturn("fieldValue");
        // when
        List<ChangeOrderStateMessage> errors = orderStateValidationService.validationAccepted(order);
        // then
        Assert.assertTrue(errors.isEmpty());
    }

    @Test
    public void shouldReturnErrorWhenValidatingInProgress() throws Exception {
        // when
        orderStateValidationService.validationInProgress(order);
    }

    @Test
    public void shouldPerformValidationInProgress() throws Exception {
        // given
        Mockito.when(order.getField(Mockito.anyString())).thenReturn("fieldValue");
        // when
        List<ChangeOrderStateMessage> errors = orderStateValidationService.validationInProgress(order);
        // then
        Assert.assertTrue(errors.isEmpty());
    }

    @Test
    public void shouldReturnErrorWhenValidatingCompleted() throws Exception {
        // when
        orderStateValidationService.validationCompleted(order);
    }

    @Test
    public void shouldPerformValidationCompleted() throws Exception {
        // given
        Mockito.when(order.getField(Mockito.anyString())).thenReturn("fieldValue");
        // when
        List<ChangeOrderStateMessage> errors = orderStateValidationService.validationCompleted(order);
        // then
        Assert.assertTrue(errors.isEmpty());
    }

}