/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.operationalTasksForOrders.hooks;

import static com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksOTFOFields.ORDER;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class OperationalTaskHooksOTFOTest {

    private OperationalTaskHooksOTFO hooksOTFO;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity entity, order, technology;

    @Before
    public void init() {
        hooksOTFO = new OperationalTaskHooksOTFO();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldReturnTrueWhenOrderIsNotSave() throws Exception {
        // given
        when(entity.getBelongsToField(ORDER)).thenReturn(null);

        // when
        boolean result = hooksOTFO.checkIfOrderHasTechnology(dataDefinition, entity);
        // then
        Assert.assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenOrderDoesNotHaveTechnology() throws Exception {
        // given
        when(entity.getBelongsToField(ORDER)).thenReturn(order);
        when(order.getBelongsToField("technology")).thenReturn(null);
        // when
        boolean result = hooksOTFO.checkIfOrderHasTechnology(dataDefinition, entity);
        // then
        Assert.assertFalse(result);
        Mockito.verify(entity).addError(dataDefinition.getField(ORDER),
                "operationalTasks.operationalTask.order.error.technologyIsNull");
    }

    @Test
    public void shouldReturnTrueWhenOrderHaveTechnology() throws Exception {
        // given
        when(entity.getBelongsToField(ORDER)).thenReturn(order);
        when(order.getBelongsToField("technology")).thenReturn(technology);
        // when
        boolean result = hooksOTFO.checkIfOrderHasTechnology(dataDefinition, entity);

        // then
        Assert.assertTrue(result);
    }
}
