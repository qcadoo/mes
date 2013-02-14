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

import static com.qcadoo.mes.orders.constants.ParameterFieldsO.DELAYED_EFFECTIVE_DATE_FROM_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.DELAYED_EFFECTIVE_DATE_TO_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.EARLIER_EFFECTIVE_DATE_FROM_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.EARLIER_EFFECTIVE_DATE_TO_TIME;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.view.api.ViewDefinitionState;

public class ParametersHooksOTest {

    @Mock
    private ViewDefinitionState view;

    @Mock
    private OrderService orderService;

    private ParametersHooksO parametersHooksO;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        parametersHooksO = new ParametersHooksO();

        setField(parametersHooksO, "orderService", orderService);

    }

    @Test
    public void shouldShowTimeFields() {
        // given

        // when
        parametersHooksO.showTimeFields(view);

        // then
        verify(orderService).changeFieldState(view, REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM,
                DELAYED_EFFECTIVE_DATE_FROM_TIME);
        verify(orderService).changeFieldState(view, REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM,
                EARLIER_EFFECTIVE_DATE_FROM_TIME);
        verify(orderService).changeFieldState(view, REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_TO,
                DELAYED_EFFECTIVE_DATE_TO_TIME);
        verify(orderService).changeFieldState(view, REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_TO,
                EARLIER_EFFECTIVE_DATE_TO_TIME);
    }

}
