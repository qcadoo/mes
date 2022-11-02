/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.productionScheduling.hooks;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.orders.util.OrderDetailsRibbonHelper;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.testing.model.EntityListMock;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import com.qcadoo.view.internal.components.window.WindowComponentState;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.BDDMockito.given;

import java.util.Collection;
import java.util.Collections;

public class OperationDurationDetailsInOrderHooksTest {


    private static final String L_OPERATIONAL_TASKS = "operationalTasks";

    private static final String L_CREATE_OPERATIONAL_TASKS = "createOperationalTasks";

    private static final String L_GENERATED_END_DATE = "generatedEndDate";

    private OperationDurationDetailsInOrderHooks operationDurationDetailsInOrderHooksOTFO;

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
    private Entity order, technology;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        operationDurationDetailsInOrderHooksOTFO = new OperationDurationDetailsInOrderHooks();

        Long orderId = 1L;

        given(view.getComponentByReference(QcadooViewConstants.L_WINDOW)).willReturn(window);
        given(window.getRibbon()).willReturn(ribbon);
        given(ribbon.getGroupByName(L_OPERATIONAL_TASKS)).willReturn(operationalTasks);
        given(operationalTasks.getItemByName(L_CREATE_OPERATIONAL_TASKS)).willReturn(createOperationalTasks);

        given(view.getComponentByReference(L_GENERATED_END_DATE)).willReturn(generatedEndDateField);

        given(view.getComponentByReference(QcadooViewConstants.L_FORM)).willReturn(orderForm);
        given(orderForm.getEntityId()).willReturn(orderId);
        given(orderForm.getEntity()).willReturn(order);
        given(order.getDataDefinition()).willReturn(orderDD);
        given(orderDD.get(orderId)).willReturn(order);
    }

    @Test
    public void shouldEnableButtonWhenIsGeneratedAndOrderStateIsAccepted() {
        // given
        String generatedEndDate = "01-02-2012";

        given(generatedEndDateField.getFieldValue()).willReturn(generatedEndDate);
        given(order.getStringField(OrderFields.STATE)).willReturn(OrderStateStringValues.ACCEPTED);
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);
        given(order.getStringField(OrderDetailsRibbonHelper.L_TYPE_OF_PRODUCTION_RECORDING)).willReturn(OrderDetailsRibbonHelper.FOR_EACH);
        given(order.getHasManyField(OrderFields.OPERATIONAL_TASKS)).willReturn(EntityListMock.create());

        // when
        operationDurationDetailsInOrderHooksOTFO.disableCreateButton(view);

        // then
        Mockito.verify(createOperationalTasks).setEnabled(true);
    }

    @Test
    public void shouldDisableButtonWhenIsNotGeneratedAndOrderStateIsInProgress() {
        // given
        String generatedEndDate = "";

        given(generatedEndDateField.getFieldValue()).willReturn(generatedEndDate);
        given(order.getStringField(OrderFields.STATE)).willReturn(OrderStateStringValues.IN_PROGRESS);
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);

        // when
        operationDurationDetailsInOrderHooksOTFO.disableCreateButton(view);

        // then
        Mockito.verify(createOperationalTasks).setEnabled(false);
    }

    @Test
    public void shouldDisableButtonWhenIsNotGeneratedAndOrderStateIsIncorrect() {
        // given
        String generatedEndDate = "";

        given(generatedEndDateField.getFieldValue()).willReturn(generatedEndDate);
        given(order.getStringField(OrderFields.STATE)).willReturn(OrderStateStringValues.ABANDONED);
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);

        // when
        operationDurationDetailsInOrderHooksOTFO.disableCreateButton(view);

        // then
        Mockito.verify(createOperationalTasks).setEnabled(false);
    }

    @Test
    public void shouldDisableFieldWhenIsGeneratedAndOrderStateIsIncorrect() {
        // given
        String generatedEndDate = "01-02-2012";

        given(generatedEndDateField.getFieldValue()).willReturn(generatedEndDate);
        given(order.getStringField(OrderFields.STATE)).willReturn(OrderStateStringValues.DECLINED);
        given(order.getBelongsToField(OrderFields.TECHNOLOGY)).willReturn(technology);

        // when
        operationDurationDetailsInOrderHooksOTFO.disableCreateButton(view);

        // then
        Mockito.verify(createOperationalTasks).setEnabled(false);
    }

}
