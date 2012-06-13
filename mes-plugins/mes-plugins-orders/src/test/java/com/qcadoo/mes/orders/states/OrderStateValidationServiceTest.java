/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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

import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.model.api.Entity;

public class OrderStateValidationServiceTest {

    private OrderStateValidationService orderStateValidationService;

    private Entity order;

    @Before
    public void init() {
        orderStateValidationService = new OrderStateValidationService();
        order = mock(Entity.class);
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
    public void shouldPerformValidationAccepted() throws Exception {
        // given
        Mockito.when(order.getField(Mockito.anyString())).thenReturn("fieldValue");
        // when
        List<ChangeOrderStateMessage> errors = orderStateValidationService.validationAccepted(order);
        // then
        Assert.assertTrue(errors.isEmpty());
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
    public void shouldPerformValidationCompleted() throws Exception {
        // given
        Mockito.when(order.getField(Mockito.anyString())).thenReturn("fieldValue");
        // when
        List<ChangeOrderStateMessage> errors = orderStateValidationService.validationCompleted(order);
        // then
        Assert.assertTrue(errors.isEmpty());
    }

}