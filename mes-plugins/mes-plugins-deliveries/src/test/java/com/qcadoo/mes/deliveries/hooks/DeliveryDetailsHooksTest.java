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
package com.qcadoo.mes.deliveries.hooks;

import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DELIVERED_PRODUCTS;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.ORDERED_PRODUCTS;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.STATE;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.SUPPLIER;
import static com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues.APPROVED;
import static com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues.DECLINED;
import static com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues.DRAFT;
import static com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues.PREPARED;
import static com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues.RECEIVED;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.deliveries.constants.CompanyFieldsD;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;

public class DeliveryDetailsHooksTest {

    private static final String L_FORM = "form";

    private static final String L_DELIVERY_DATE_BUFFER = "deliveryDateBuffer";

    private static final String L_PAYMENT_FORM = "paymentForm";

    private DeliveryDetailsHooks deliveryDetailsHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent form;

    @Mock
    private FieldComponent stateField;

    @Mock
    private GridComponent deliveredProducts;

    @Mock
    private GridComponent orderedProducts;

    @Mock
    private LookupComponent supplierLookup;

    @Mock
    private FieldComponent deliveryDateBufferField;

    @Mock
    private FieldComponent paymentFormField;

    @Mock
    private Entity supplier;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        deliveryDetailsHooks = new DeliveryDetailsHooks();

        when(view.getComponentByReference(L_FORM)).thenReturn(form);

        when(view.getComponentByReference(STATE)).thenReturn(stateField);
        when(view.getComponentByReference(DELIVERED_PRODUCTS)).thenReturn(deliveredProducts);
        when(view.getComponentByReference(ORDERED_PRODUCTS)).thenReturn(orderedProducts);
    }

    @Test
    public void shouldFillCompanyFields() throws Exception {
        // given
        Integer buffer = Integer.valueOf(10);
        String paymentForm = "cash";

        when(view.getComponentByReference(SUPPLIER)).thenReturn(supplierLookup);
        when(view.getComponentByReference(L_DELIVERY_DATE_BUFFER)).thenReturn(deliveryDateBufferField);
        when(view.getComponentByReference(L_PAYMENT_FORM)).thenReturn(paymentFormField);

        when(supplierLookup.getEntity()).thenReturn(supplier);
        when(supplier.getIntegerField(CompanyFieldsD.BUFFER)).thenReturn(buffer);
        when(supplier.getStringField(CompanyFieldsD.PAYMENT_FORM)).thenReturn(paymentForm);

        // when
        deliveryDetailsHooks.fillCompanyFieldsForSupplier(view);

        // then
        verify(deliveryDateBufferField).setFieldValue(buffer);
        verify(paymentFormField).setFieldValue(paymentForm);
    }

    @Test
    public void shouldntFillCompanyFieldsWhenSupplierIsNotSelected() throws Exception {
        // given
        when(view.getComponentByReference(SUPPLIER)).thenReturn(supplierLookup);
        when(view.getComponentByReference(L_DELIVERY_DATE_BUFFER)).thenReturn(deliveryDateBufferField);
        when(view.getComponentByReference(L_PAYMENT_FORM)).thenReturn(paymentFormField);

        when(supplierLookup.getEntity()).thenReturn(null);

        // when
        deliveryDetailsHooks.fillCompanyFieldsForSupplier(view);

        // then
        verify(deliveryDateBufferField).setFieldValue(null);
        verify(paymentFormField).setFieldValue(null);
    }

    @Test
    public void shouldReturnWhenEntityIdIsNull() throws Exception {
        // given
        final boolean enabledForm = true;
        final boolean enabledOrderedGrid = false;
        final boolean enabledDeliveredGrid = false;
        when(form.getEntityId()).thenReturn(null);
        when(stateField.getFieldValue()).thenReturn(PREPARED);

        // when
        deliveryDetailsHooks.changeFieldsEnabledDependOnState(view);

        // then
        verify(form).setFormEnabled(enabledForm);
        verify(orderedProducts).setEnabled(enabledOrderedGrid);
        verify(orderedProducts).setEditable(enabledOrderedGrid);
        verify(deliveredProducts).setEnabled(enabledDeliveredGrid);
        verify(deliveredProducts).setEditable(enabledDeliveredGrid);
    }

    @Test
    public void shouldDisabledFormAndOrderedFieldWhenStateIsPrepared() throws Exception {
        // given
        final boolean enabledForm = false;
        final boolean enabledOrderedGrid = false;
        final boolean enabledDeliveredGrid = true;
        when(stateField.getFieldValue()).thenReturn(PREPARED);

        // when
        deliveryDetailsHooks.changeFieldsEnabledDependOnState(view);

        // then
        verify(form).setFormEnabled(enabledForm);
        verify(orderedProducts).setEnabled(enabledOrderedGrid);
        verify(orderedProducts).setEditable(enabledOrderedGrid);
        verify(deliveredProducts).setEnabled(enabledDeliveredGrid);
        verify(deliveredProducts).setEditable(enabledDeliveredGrid);
    }

    @Test
    public void shouldDisabledFormAndOrderedFieldWhenStateIsApproved() throws Exception {
        // given
        final boolean enabledForm = false;
        final boolean enabledOrderedGrid = false;
        final boolean enabledDeliveredGrid = true;
        when(stateField.getFieldValue()).thenReturn(APPROVED);

        // when
        deliveryDetailsHooks.changeFieldsEnabledDependOnState(view);

        // then
        verify(form).setFormEnabled(enabledForm);
        verify(orderedProducts).setEnabled(enabledOrderedGrid);
        verify(orderedProducts).setEditable(enabledOrderedGrid);
        verify(deliveredProducts).setEnabled(enabledDeliveredGrid);
        verify(deliveredProducts).setEditable(enabledDeliveredGrid);
    }

    @Test
    public void shouldEnabledFieldsWhenStateIsDraft() throws Exception {
        // given
        final boolean enabledForm = true;
        final boolean enabledOrderedGrid = true;
        final boolean enabledDeliveredGrid = true;
        when(stateField.getFieldValue()).thenReturn(DRAFT);

        // when
        deliveryDetailsHooks.changeFieldsEnabledDependOnState(view);

        // then
        verify(form).setFormEnabled(enabledForm);
        verify(orderedProducts).setEnabled(enabledOrderedGrid);
        verify(orderedProducts).setEditable(enabledOrderedGrid);
        verify(deliveredProducts).setEnabled(enabledDeliveredGrid);
        verify(deliveredProducts).setEditable(enabledDeliveredGrid);
    }

    @Test
    public void shouldDisabledFieldsWhenStateIsDeclined() throws Exception {
        // given
        final boolean enabledForm = false;
        final boolean enabledOrderedGrid = false;
        final boolean enabledDeliveredGrid = false;
        when(stateField.getFieldValue()).thenReturn(DECLINED);

        // when
        deliveryDetailsHooks.changeFieldsEnabledDependOnState(view);

        // then
        verify(form).setFormEnabled(enabledForm);
        verify(orderedProducts).setEnabled(enabledOrderedGrid);
        verify(orderedProducts).setEditable(enabledOrderedGrid);
        verify(deliveredProducts).setEnabled(enabledDeliveredGrid);
        verify(deliveredProducts).setEditable(enabledDeliveredGrid);
    }

    @Test
    public void shouldDisabledFieldsWhenStateIsReceived() throws Exception {
        // given
        final boolean enabledForm = false;
        final boolean enabledOrderedGrid = false;
        final boolean enabledDeliveredGrid = false;
        when(stateField.getFieldValue()).thenReturn(RECEIVED);

        // when
        deliveryDetailsHooks.changeFieldsEnabledDependOnState(view);

        // then
        verify(form).setFormEnabled(enabledForm);
        verify(orderedProducts).setEnabled(enabledOrderedGrid);
        verify(orderedProducts).setEditable(enabledOrderedGrid);
        verify(deliveredProducts).setEnabled(enabledDeliveredGrid);
        verify(deliveredProducts).setEditable(enabledDeliveredGrid);
    }

}
