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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.orders.constants.OrderFields;
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

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_OPERATIONAL_TASKS = "operationalTasks";

    private static final String L_CREATE_OPERATIONAL_TASKS = "createOperationalTasks";

    private static final String L_GENERATED_END_DATE = "generatedEndDate";

    private OperationDurationDetailsInOrderDetailsHooksOTFO operationDurationDetailsInOrderDetailsHooksOTFO;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private WindowComponentState window;

    @Mock
    private Ribbon ribbon;

    @Mock
    private RibbonGroup operationalTasks;

    @Mock
    private RibbonActionItem createOperationalTasks;

    @Mock
    private FormComponent orderForm;

    @Mock
    private FieldComponent generatedEndDateField;

    @Mock
    private DataDefinition orderDD;

    @Mock
    private Entity order;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        operationDurationDetailsInOrderDetailsHooksOTFO = new OperationDurationDetailsInOrderDetailsHooksOTFO();

        Long orderId = 1L;

        given(view.getComponentByReference(L_WINDOW)).willReturn((ComponentState) window);
        given(window.getRibbon()).willReturn(ribbon);
        given(ribbon.getGroupByName(L_OPERATIONAL_TASKS)).willReturn(operationalTasks);
        given(operationalTasks.getItemByName(L_CREATE_OPERATIONAL_TASKS)).willReturn(createOperationalTasks);

        given(view.getComponentByReference(L_GENERATED_END_DATE)).willReturn(generatedEndDateField);

        given(view.getComponentByReference(L_FORM)).willReturn(orderForm);
        given(orderForm.getEntityId()).willReturn(orderId);
        given(orderForm.getEntity()).willReturn(order);
        given(order.getDataDefinition()).willReturn(orderDD);
        given(orderDD.get(orderId)).willReturn(order);
    }

    @Test
    public void shouldEnableButtonWhenIsGeneratedAndOrderStateIsAccepted() throws Exception {
        // given
        String generatedEndDate = "01-02-2012";

        given(generatedEndDateField.getFieldValue()).willReturn(generatedEndDate);
        given(order.getStringField(OrderFields.STATE)).willReturn(OrderStateStringValues.ACCEPTED);

        // when
        operationDurationDetailsInOrderDetailsHooksOTFO.disableCreateButton(view);

        // then
        Mockito.verify(createOperationalTasks).setEnabled(true);
    }

    @Test
    public void shouldDisableButtonWhenIsNotGeneratedAndOrderStateIsInProgress() throws Exception {
        // given
        String generatedEndDate = "";

        given(generatedEndDateField.getFieldValue()).willReturn(generatedEndDate);
        given(order.getStringField(OrderFields.STATE)).willReturn(OrderStateStringValues.IN_PROGRESS);

        // when
        operationDurationDetailsInOrderDetailsHooksOTFO.disableCreateButton(view);

        // then
        Mockito.verify(createOperationalTasks).setEnabled(false);
    }

    @Test
    public void shouldDisableButtonWhenIsNotGeneratedAndOrderStateIsIncorrect() throws Exception {
        // given
        String generatedEndDate = "";

        given(generatedEndDateField.getFieldValue()).willReturn(generatedEndDate);
        given(order.getStringField(OrderFields.STATE)).willReturn(OrderStateStringValues.ABANDONED);

        // when
        operationDurationDetailsInOrderDetailsHooksOTFO.disableCreateButton(view);

        // then
        Mockito.verify(createOperationalTasks).setEnabled(false);
    }

    @Test
    public void shouldDisableFieldWhenIsGeneratedAndOrderStateIsIncorrect() throws Exception {
        // given
        String generatedEndDate = "01-02-2012";

        given(generatedEndDateField.getFieldValue()).willReturn(generatedEndDate);
        given(order.getStringField(OrderFields.STATE)).willReturn(OrderStateStringValues.DECLINED);

        // when
        operationDurationDetailsInOrderDetailsHooksOTFO.disableCreateButton(view);

        // then
        Mockito.verify(createOperationalTasks).setEnabled(false);
    }

}
