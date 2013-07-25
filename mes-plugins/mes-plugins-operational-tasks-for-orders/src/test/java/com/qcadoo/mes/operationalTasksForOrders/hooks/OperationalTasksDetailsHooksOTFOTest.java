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

import com.qcadoo.mes.operationalTasks.constants.OperationalTasksFields;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTasksOTFOFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

public class OperationalTasksDetailsHooksOTFOTest {

    private OperationalTasksDetailsHooksOTFO detailsHooksOTFO;

    @Mock
    private ViewDefinitionState viewDefinitionState;

    @Mock
    private FieldComponent typeField, nameField, descriptionField, orderField, productionLineField, tocField;

    @Before
    public void init() {
        detailsHooksOTFO = new OperationalTasksDetailsHooksOTFO();
        MockitoAnnotations.initMocks(this);

        Mockito.when(viewDefinitionState.getComponentByReference(OperationalTasksFields.TYPE_TASK)).thenReturn(typeField);
        Mockito.when(viewDefinitionState.getComponentByReference(OperationalTasksFields.NAME)).thenReturn(nameField);
        Mockito.when(viewDefinitionState.getComponentByReference(OperationalTasksFields.DESCRIPTION))
                .thenReturn(descriptionField);
        Mockito.when(viewDefinitionState.getComponentByReference(OperationalTasksOTFOFields.ORDER)).thenReturn(orderField);
        Mockito.when(viewDefinitionState.getComponentByReference(OperationalTasksFields.PRODUCTION_LINE)).thenReturn(
                productionLineField);
        Mockito.when(viewDefinitionState.getComponentByReference("technologyOperationComponent")).thenReturn(tocField);
    }

    @Test
    public void shouldDisabledFieldWhenTypeForOrderIsSelected() throws Exception {
        // given
        when(typeField.getFieldValue()).thenReturn("02executionOperationInOrder");
        // when
        detailsHooksOTFO.disabledFieldWhenOrderTypeIsSelected(viewDefinitionState);
        // then

        Mockito.verify(nameField).setEnabled(false);
        Mockito.verify(descriptionField).setEnabled(false);
        Mockito.verify(productionLineField).setEnabled(false);
        Mockito.verify(orderField).setEnabled(true);
        Mockito.verify(tocField).setEnabled(true);
    }

    @Test
    public void shouldEnabledFieldWhenTypeOtherCaseIsSelected() throws Exception {
        // given
        when(typeField.getFieldValue()).thenReturn("01otherCase");
        // when
        detailsHooksOTFO.disabledFieldWhenOrderTypeIsSelected(viewDefinitionState);
        // then

        Mockito.verify(nameField).setEnabled(true);
        Mockito.verify(descriptionField).setEnabled(true);
        Mockito.verify(productionLineField).setEnabled(true);
        Mockito.verify(orderField).setEnabled(false);
        Mockito.verify(tocField).setEnabled(false);
    }
}
