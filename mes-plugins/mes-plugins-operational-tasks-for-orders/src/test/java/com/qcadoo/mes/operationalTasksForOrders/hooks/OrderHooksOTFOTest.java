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
package com.qcadoo.mes.operationalTasksForOrders.hooks;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;
import com.qcadoo.mes.operationalTasksForOrders.OperationalTasksForOrdersService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public class OrderHooksOTFOTest {

    private OrderHooksOTFO orderHooksOTFO;

    @Mock
    private OperationalTasksForOrdersService operationalTasksForOrdersService;

    @Mock
    private DataDefinition orderDD, operationalTaskDD;

    @Mock
    private Entity order, orderFromDB, productionLine, orderProductionLine, operationalTask;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderHooksOTFO = new OrderHooksOTFO();

        ReflectionTestUtils.setField(orderHooksOTFO, "operationalTasksForOrdersService", operationalTasksForOrdersService);

        given(operationalTask.getDataDefinition()).willReturn(operationalTaskDD);
    }

    private EntityList mockEntityList(List<Entity> list) {
        EntityList entityList = mock(EntityList.class);

        given(entityList.iterator()).willReturn(list.iterator());

        return entityList;
    }

    @Test
    public void shouldReturnWhenEntityIdIsNull() throws Exception {
        // given
        Long orderId = null;

        given(order.getId()).willReturn(orderId);

        // when
        orderHooksOTFO.changedProductionLine(orderDD, order);

        // then
    }

    @Test
    public void shouldReturnWhenProductionLineIsTheSame() throws Exception {
        // given
        Long orderId = 1L;
        Long productionLineId = 1L;

        given(order.getId()).willReturn(orderId);
        given(orderDD.get(orderId)).willReturn(orderFromDB);

        given(order.getBelongsToField(OrderFields.PRODUCTION_LINE)).willReturn(productionLine);
        given(orderFromDB.getBelongsToField(OrderFields.PRODUCTION_LINE)).willReturn(productionLine);

        given(productionLine.getId()).willReturn(productionLineId);

        // when
        orderHooksOTFO.changedProductionLine(orderDD, order);

        // then
        Mockito.verify(operationalTask, Mockito.never()).setField(OrderFields.PRODUCTION_LINE, null);
        Mockito.verify(operationalTaskDD, Mockito.never()).save(operationalTask);
    }

    @Test
    public void shouldReturneWhenProductionLineIsNull() throws Exception {
        // given
        Long orderId = 1L;
        Long productionLineId = 1L;

        given(order.getId()).willReturn(orderId);
        given(orderDD.get(orderId)).willReturn(orderFromDB);

        given(order.getBelongsToField(OrderFields.PRODUCTION_LINE)).willReturn(productionLine);
        given(orderFromDB.getBelongsToField(OrderFields.PRODUCTION_LINE)).willReturn(null);

        given(productionLine.getId()).willReturn(productionLineId);

        // when
        orderHooksOTFO.changedProductionLine(orderDD, order);

        // then
        Mockito.verify(operationalTask, Mockito.never()).setField(OrderFields.PRODUCTION_LINE, productionLine);
        Mockito.verify(operationalTaskDD, Mockito.never()).save(operationalTask);
    }

    @Ignore
    @Test
    public void shouldChangeProductionLineWhenProductionLineWasChanged() throws Exception {
        // given
        Long orderId = 1L;
        Long productionLineId = 1L;
        Long orderProductionLineId = 2L;

        given(order.getId()).willReturn(orderId);
        given(orderDD.get(orderId)).willReturn(orderFromDB);

        given(order.getBelongsToField(OrderFields.PRODUCTION_LINE)).willReturn(productionLine);
        given(orderFromDB.getBelongsToField(OrderFields.PRODUCTION_LINE)).willReturn(orderProductionLine);

        given(productionLine.getId()).willReturn(productionLineId);
        given(orderProductionLine.getId()).willReturn(orderProductionLineId);

        EntityList operationalTasks = mockEntityList(Lists.newArrayList(operationalTask));

        given(operationalTasksForOrdersService.getOperationalTasksForOrder(order)).willReturn(operationalTasks);

        // when
        orderHooksOTFO.changedProductionLine(orderDD, order);

        // then
        Mockito.verify(operationalTask).setField(OrderFields.PRODUCTION_LINE, productionLine);
        Mockito.verify(operationalTaskDD).save(operationalTask);
    }

}
