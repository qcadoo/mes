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
package com.qcadoo.mes.operationalTasksForOrders.validators;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskFieldsOTFO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class OperationalTaskValidatorsOTFOTest {

    private OperationalTaskValidatorsOTFO operationalTaskValidatorsOTFO;

    @Mock
    private DataDefinition operationalTaskDD;

    @Mock
    private Entity operationalTask, order, technology;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        operationalTaskValidatorsOTFO = new OperationalTaskValidatorsOTFO();
    }

    @Test
    public void shouldReturnTrueWhenOrderIsNotSave() throws Exception {
        // given
        given(operationalTask.getBelongsToField(OperationalTaskFieldsOTFO.ORDER)).willReturn(null);

        // when
        boolean result = operationalTaskValidatorsOTFO.validatesWith(operationalTaskDD, operationalTask);

        // then
        Assert.assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenOrderDoesNotHaveTechnology() throws Exception {
        // given
        given(operationalTask.getBelongsToField(OperationalTaskFieldsOTFO.ORDER)).willReturn(order);
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(null);

        // when
        boolean result = operationalTaskValidatorsOTFO.validatesWith(operationalTaskDD, operationalTask);

        // then
        Assert.assertFalse(result);
        Mockito.verify(operationalTask).addError(operationalTaskDD.getField(OperationalTaskFieldsOTFO.ORDER),
                "operationalTasks.operationalTask.order.error.technologyIsNull");
    }

    @Test
    public void shouldReturnTrueWhenOrderHaveTechnology() throws Exception {
        // given
        when(operationalTask.getBelongsToField(OperationalTaskFieldsOTFO.ORDER)).thenReturn(order);
        when(order.getBelongsToField(OrderFields.TECHNOLOGY)).thenReturn(technology);
        // when
        boolean result = operationalTaskValidatorsOTFO.validatesWith(operationalTaskDD, operationalTask);

        // then
        Assert.assertTrue(result);
    }

}
