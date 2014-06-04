/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.operationalTasks.constants.OperationalTaskFields;
import com.qcadoo.mes.operationalTasks.constants.OperationalTaskTypeTask;
import com.qcadoo.mes.operationalTasksForOrders.OperationalTasksForOrdersService;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskFieldsOTFO;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskTypeTaskOTFO;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

public class OperationalTaskDetailsHooksOTFOTest {

    private static final String L_TECHNOLOGY_OPERATION_COMPONENT = "technologyOperationComponent";

    private OperationalTaskDetailsHooksOTFO operationalTaskDetailsHooksOTFO;

    @Mock
    private OperationalTasksForOrdersService operationalTasksForOrdersService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private LookupComponent orderField, productionLineField, technologyOperationComponentField;

    @Mock
    private FieldComponent typeTaskField, nameField, descriptionField;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        operationalTaskDetailsHooksOTFO = new OperationalTaskDetailsHooksOTFO();

        ReflectionTestUtils.setField(operationalTaskDetailsHooksOTFO, "operationalTasksForOrdersService",
                operationalTasksForOrdersService);

        given(view.getComponentByReference(OperationalTaskFields.TYPE_TASK)).willReturn(typeTaskField);
        given(view.getComponentByReference(OperationalTaskFields.NAME)).willReturn(nameField);
        given(view.getComponentByReference(OperationalTaskFields.DESCRIPTION)).willReturn(descriptionField);
        given(view.getComponentByReference(OperationalTaskFieldsOTFO.ORDER)).willReturn(orderField);
        given(view.getComponentByReference(OperationalTaskFields.PRODUCTION_LINE)).willReturn(productionLineField);
        given(view.getComponentByReference(L_TECHNOLOGY_OPERATION_COMPONENT)).willReturn(technologyOperationComponentField);
    }

    @Test
    public void shouldDisabledFieldWhenTypeForOrderIsSelected() throws Exception {
        // given
        String typeTask = OperationalTaskTypeTaskOTFO.EXECUTION_OPERATION_IN_ORDER.getStringValue();

        given(typeTaskField.getFieldValue()).willReturn(typeTask);

        given(operationalTasksForOrdersService.isOperationalTaskTypeTaskOtherCase(typeTask)).willReturn(false);

        // when
        operationalTaskDetailsHooksOTFO.disableFieldsWhenOrderTypeIsSelected(view);

        // then
        Mockito.verify(nameField).setEnabled(false);
        Mockito.verify(descriptionField).setEnabled(false);
        Mockito.verify(productionLineField).setEnabled(false);
        Mockito.verify(orderField).setEnabled(true);
        Mockito.verify(technologyOperationComponentField).setEnabled(true);
    }

    @Test
    public void shouldEnabledFieldWhenTypeOtherCaseIsSelected() throws Exception {
        // given
        String typeTask = OperationalTaskTypeTask.OTHER_CASE.getStringValue();

        given(typeTaskField.getFieldValue()).willReturn(typeTask);

        given(operationalTasksForOrdersService.isOperationalTaskTypeTaskOtherCase(typeTask)).willReturn(true);

        // when
        operationalTaskDetailsHooksOTFO.disableFieldsWhenOrderTypeIsSelected(view);

        // then
        Mockito.verify(nameField).setEnabled(true);
        Mockito.verify(descriptionField).setEnabled(true);
        Mockito.verify(productionLineField).setEnabled(true);
        Mockito.verify(orderField).setEnabled(false);
        Mockito.verify(technologyOperationComponentField).setEnabled(false);
    }

}
