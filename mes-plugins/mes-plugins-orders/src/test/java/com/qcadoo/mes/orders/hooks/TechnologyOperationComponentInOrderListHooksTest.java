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
package com.qcadoo.mes.orders.hooks;

import static com.qcadoo.mes.orders.constants.OrdersConstants.PLUGIN_IDENTIFIER;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

public class TechnologyOperationComponentInOrderListHooksTest {

    private TechnologyOperationComponentInOrderListHooks hooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FieldComponent technologyField;

    @Mock
    private FormComponent form;

    @Mock
    private Entity order, technology;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Before
    public void init() {
        hooks = new TechnologyOperationComponentInOrderListHooks();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(hooks, "dataDefinitionService", dataDefinitionService);

        Long orderId = 1L;
        when(dataDefinitionService.get(PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)).thenReturn(dataDefinition);
        when(view.getComponentByReference("form")).thenReturn(form);
        when(form.getEntityId()).thenReturn(orderId);
        when(dataDefinition.get(orderId)).thenReturn(order);
        when(view.getComponentByReference("technology")).thenReturn(technologyField);
    }

    @Test
    public void shouldSetTechnologyNumberToField() throws Exception {
        // given

        String technologyNumber = "0001";

        when(order.getBelongsToField("technology")).thenReturn(technology);
        when(technology.getStringField("number")).thenReturn(technologyNumber);
        // when
        hooks.setTechnologyNumber(view);
        // then
        Mockito.verify(technologyField).setFieldValue(technologyNumber);
    }

    @Test
    public void shouldSetNullWhenTechnologyIsNull() throws Exception {
        // given
        when(order.getBelongsToField("technology")).thenReturn(null);

        // when
        hooks.setTechnologyNumber(view);
        // then
        Mockito.verify(technologyField).setFieldValue(null);
    }
}
