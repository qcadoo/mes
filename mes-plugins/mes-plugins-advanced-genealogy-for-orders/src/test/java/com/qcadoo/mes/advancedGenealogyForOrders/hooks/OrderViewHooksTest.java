/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.advancedGenealogyForOrders.hooks;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.util.OrderDetailsRibbonHelper;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

public class OrderViewHooksTest {

    private OrderViewHooks orderViewHooks;

    private static final String L_FORM = "form";

    private static final long L_ID = 1L;

    private static final String L_TRACKING_RECORD_FOR_ORDER_TREATMENT = "trackingRecordForOrderTreatment";

    private static final String L_TRACKING_RECORD_TREATMENT = "trackingRecordTreatment";

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent form;

    @Mock
    private FieldComponent trackingRecordTreatment;

    @Mock
    private Entity parameter;

    @Mock
    private OrderDetailsRibbonHelper orderDetailsRibbonHelper;

    @Before
    public void init() {
        ParameterService parameterService = mock(ParameterService.class);

        MockitoAnnotations.initMocks(this);

        orderViewHooks = new OrderViewHooks();

        setField(orderViewHooks, "parameterService", parameterService);
        setField(orderViewHooks, "orderDetailsRibbonHelper", orderDetailsRibbonHelper);

        given(view.getComponentByReference(L_FORM)).willReturn(form);
        given(view.getComponentByReference(L_TRACKING_RECORD_TREATMENT)).willReturn(trackingRecordTreatment);

        given(parameterService.getParameter()).willReturn(parameter);
    }

    @Test
    public void shouldntSetOrderDefaultValueIfFormIsSaved() {
        // given
        given(form.getEntityId()).willReturn(L_ID);
        given(trackingRecordTreatment.getFieldValue()).willReturn(null);

        // when
        orderViewHooks.setOrderDefaultValue(view, form);

        // then
        verify(trackingRecordTreatment, never()).setFieldValue(L_TRACKING_RECORD_TREATMENT);
    }

    @Test
    public void shouldSetOrderDefaultValuesIfFormIsNotSavedAndTrackingRecordTreatmentIsNotNull() {
        // given
        given(form.getEntityId()).willReturn(null);
        given(trackingRecordTreatment.getFieldValue()).willReturn(L_TRACKING_RECORD_FOR_ORDER_TREATMENT);

        // when
        orderViewHooks.setOrderDefaultValue(view, form);

        // then
        verify(trackingRecordTreatment, never()).setFieldValue(L_TRACKING_RECORD_TREATMENT);
    }

    @Test
    public void shouldSetOrderDefaultValueIfFormIsNotSaved() {
        // given
        given(form.getEntityId()).willReturn(null);
        given(trackingRecordTreatment.getFieldValue()).willReturn(null);

        given(parameter.getStringField(L_TRACKING_RECORD_FOR_ORDER_TREATMENT)).willReturn(L_TRACKING_RECORD_TREATMENT);

        // when
        orderViewHooks.setOrderDefaultValue(view, form);

        // then
        verify(trackingRecordTreatment).setFieldValue(L_TRACKING_RECORD_TREATMENT);
    }

}
