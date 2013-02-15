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

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.internal.components.window.WindowComponentState;

public class OperationDurationDetailsInOrderDetailsHooksOTFOTest {

    private OperationDurationDetailsInOrderDetailsHooksOTFO otfo;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private WindowComponentState windowComponent;

    @Mock
    private Ribbon ribbon;

    @Mock
    private RibbonGroup ribbonGroup;

    @Mock
    private RibbonActionItem actionItem;

    @Mock
    private FieldComponent generatedEndDate;

    @Mock
    private FormComponent form;

    @Mock
    private Entity formEntity, order;

    @Mock
    private DataDefinition dataDefinition;

    @Before
    public void init() {
        otfo = new OperationDurationDetailsInOrderDetailsHooksOTFO();

        MockitoAnnotations.initMocks(this);

        Long orderId = 1L;
        when(view.getComponentByReference("window")).thenReturn((ComponentState) windowComponent);
        when(windowComponent.getRibbon()).thenReturn(ribbon);
        when(ribbon.getGroupByName("operationalTasks")).thenReturn(ribbonGroup);
        when(ribbonGroup.getItemByName("createOperationalTasks")).thenReturn(actionItem);

        when(view.getComponentByReference("generatedEndDate")).thenReturn(generatedEndDate);

        when(view.getComponentByReference("form")).thenReturn(form);
        when(form.getEntity()).thenReturn(formEntity);
        when(formEntity.getDataDefinition()).thenReturn(dataDefinition);
        when(form.getEntityId()).thenReturn(orderId);
        when(dataDefinition.get(orderId)).thenReturn(order);
    }

    @Test
    public void shouldEnabledButtonWhenIsGeneratedAndOrderStateIsAccepted() throws Exception {
        // given
        String date = "01-02-2012";
        when(generatedEndDate.getFieldValue()).thenReturn(date);
        when(order.getStringField("state")).thenReturn(OrderStateStringValues.ACCEPTED);
        // when
        otfo.disabledCreateButton(view);
        // then
        Mockito.verify(actionItem).setEnabled(true);
    }

    @Test
    public void shouldDisabledWhenIsNotGeneratedAndOrderStateIsInProgress() throws Exception {
        // given
        when(generatedEndDate.getFieldValue()).thenReturn("");
        when(order.getStringField("state")).thenReturn(OrderStateStringValues.IN_PROGRESS);

        // when
        otfo.disabledCreateButton(view);
        // then
        Mockito.verify(actionItem).setEnabled(false);
    }

    @Test
    public void shouldDisabledFieldWhenOrderStateIsIncorrectAndIsNotGEnerated() throws Exception {
        // given
        // given
        when(generatedEndDate.getFieldValue()).thenReturn("");
        when(order.getStringField("state")).thenReturn(OrderStateStringValues.ABANDONED);

        // when
        otfo.disabledCreateButton(view);
        // then
        Mockito.verify(actionItem).setEnabled(false);
        // then
    }

    @Test
    public void shouldDisabledFieldWhenOrderStateIsIncorrectAndIsGEnerated() throws Exception {
        // given
        // given
        String date = "01-02-2012";
        when(generatedEndDate.getFieldValue()).thenReturn(date);
        when(order.getStringField("state")).thenReturn(OrderStateStringValues.DECLINED);

        // when
        otfo.disabledCreateButton(view);
        // then
        Mockito.verify(actionItem).setEnabled(false);
        // then
    }
}
