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
package com.qcadoo.mes.deliveries.listeners;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

public class DeliveryDetailsListenersTest {

    private DeliveryDetailsListeners deliveryDetailsListeners;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private GridComponent grid;

    @Mock
    private FormComponent formComponent;

    @Mock
    private DataDefinition dataDefinition, ddDelivered;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private Entity entity, ordered1, ordered2, deliveredEntity;

    private String[] args = { "pdf" };

    @Before
    public void init() {
        deliveryDetailsListeners = new DeliveryDetailsListeners();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(deliveryDetailsListeners, "dataDefinitionService", dataDefinitionService);
    }

    private EntityList mockEntityList(List<Entity> list) {
        EntityList entityList = mock(EntityList.class);
        when(entityList.iterator()).thenReturn(list.iterator());
        return entityList;
    }

    @Test
    public void shouldRedirectToDeliveryReportOnPrintDelivery() throws Exception {
        // given
        String stateValue = "111";
        when(formComponent.getFieldValue()).thenReturn(stateValue);

        // when
        deliveryDetailsListeners.printDeliveryReport(view, formComponent, args);

        // then
        verify(view).redirectTo("/deliveries/deliveryReport." + args[0] + "?id=" + stateValue, true, false);
    }

    @Test
    public void shouldAddMessageWhenStateIsNotFormComponentOnPrintDelivery() throws Exception {
        // given
        String stateValue = "111";
        when(grid.getFieldValue()).thenReturn(stateValue);

        // when
        deliveryDetailsListeners.printDeliveryReport(view, grid, args);

        // then
        verify(grid).addMessage("deliveries.delivery.report.componentFormError", MessageType.FAILURE);
        verify(view, never()).redirectTo("/deliveries/deliveryReport." + args[0] + "?id=" + stateValue, true, false);
    }

    @Test
    public void shouldRedirectToDeliveryReportOnPrintOrder() throws Exception {
        // given
        String stateValue = "111";
        when(formComponent.getFieldValue()).thenReturn(stateValue);

        // when
        deliveryDetailsListeners.printOrderReport(view, formComponent, args);

        // then
        verify(view).redirectTo("/deliveries/orderReport." + args[0] + "?id=" + stateValue, true, false);

    }

    @Test
    public void shouldAddMessageWhenStateIsNotFormComponentOnPrintOrder() throws Exception {
        // given
        String stateValue = "111";
        when(grid.getFieldValue()).thenReturn(stateValue);

        // when
        deliveryDetailsListeners.printOrderReport(view, grid, args);

        // then
        verify(grid).addMessage("deliveries.order.report.componentFormError", MessageType.FAILURE);
        verify(view, never()).redirectTo("/deliveries/orderReport." + args[0] + "?id=" + stateValue, true, false);
    }

    @Test
    public void shouldReturnWhenEntityIdIsNull() throws Exception {
        // given
        when(view.getComponentByReference("form")).thenReturn(formComponent);
        when(formComponent.getEntityId()).thenReturn(null);

        // when
        deliveryDetailsListeners.copyOrderedProductToDelivered(view, formComponent, args);
    }

    @Test
    public void shouldCopyOrderedProdsToDeliveredProds() throws Exception {
        // given
        Long entityId = 1L;
        when(view.getComponentByReference("form")).thenReturn(formComponent);
        when(formComponent.getEntityId()).thenReturn(entityId);
        when(formComponent.getEntity()).thenReturn(entity);
        when(entity.getDataDefinition()).thenReturn(dataDefinition);
        when(dataDefinition.get(entityId)).thenReturn(entity);

        when(dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERED_PRODUCT))
                .thenReturn(ddDelivered);
        when(ddDelivered.create()).thenReturn(deliveredEntity);

        EntityList ordereds = mockEntityList(Lists.newArrayList(ordered1, ordered2));
        when(entity.getHasManyField(DeliveryFields.ORDERED_PRODUCTS)).thenReturn(ordereds);

        // when
        deliveryDetailsListeners.copyOrderedProductToDelivered(view, formComponent, args);

        // then
    }

}
