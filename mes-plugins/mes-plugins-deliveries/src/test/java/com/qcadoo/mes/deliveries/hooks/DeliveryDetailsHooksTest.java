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
package com.qcadoo.mes.deliveries.hooks;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;

public class DeliveryDetailsHooksTest {

    private DeliveryDetailsHooks deliveryDetailsHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent form;

    @Mock
    private GridComponent deliveredProducts;

    @Mock
    private GridComponent orderedProducts;

    @Mock
    private LookupComponent supplierLookup;

    @Mock
    private FieldComponent deliveryDateBuffer;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity delivery, supplier;

    @Before
    public void init() {
        deliveryDetailsHooks = new DeliveryDetailsHooks();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(deliveryDetailsHooks, "dataDefinitionService", dataDefinitionService);

        when(view.getComponentByReference("form")).thenReturn(form);
        when(view.getComponentByReference(DeliveryFields.DELIVERED_PRODUCTS)).thenReturn(deliveredProducts);
        when(view.getComponentByReference(DeliveryFields.ORDERED_PRODUCTS)).thenReturn(orderedProducts);

        when(dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY)).thenReturn(
                dataDefinition);

        Long deliveryId = 1L;
        when(form.getEntityId()).thenReturn(deliveryId);
        when(dataDefinition.get(deliveryId)).thenReturn(delivery);
    }

    @Test
    public void shouldSetBufferFromCompany() throws Exception {
        // given
        Integer buffer = Integer.valueOf(10);
        when(view.getComponentByReference(DeliveryFields.SUPPLIER)).thenReturn(supplierLookup);
        when(view.getComponentByReference("deliveryDateBuffer")).thenReturn(deliveryDateBuffer);
        when(supplierLookup.getEntity()).thenReturn(supplier);
        when(supplier.getField("buffer")).thenReturn(buffer);

        // when
        deliveryDetailsHooks.setBufferForSupplier(view);

        // then
        verify(deliveryDateBuffer).setFieldValue(buffer);
    }

    @Test
    public void shouldSetNullForDeliveryBufferFieldWhenSUpplierIsNotSelected() throws Exception {
        // given
        when(view.getComponentByReference(DeliveryFields.SUPPLIER)).thenReturn(supplierLookup);
        when(view.getComponentByReference("deliveryDateBuffer")).thenReturn(deliveryDateBuffer);
        when(supplierLookup.getEntity()).thenReturn(null);

        // when
        deliveryDetailsHooks.setBufferForSupplier(view);

        // then
        verify(deliveryDateBuffer).setFieldValue(null);
    }

    @Test
    public void shouldReturnWhenEntityIdIsNull() throws Exception {
        // given
        when(form.getEntityId()).thenReturn(null);

        // when
        deliveryDetailsHooks.changedEnabledFieldForSpecificDeliveryState(view);
    }

    @Test
    public void shouldDisabledFormAndOrderedFieldWhenStateIsPrepared() throws Exception {
        // given
        final boolean enabledFormAndOrderedProduct = false;
        final boolean enabledDeliveredGrid = true;
        when(delivery.getStringField(DeliveryFields.STATE)).thenReturn(DeliveryStateStringValues.PREPARED);

        // when
        deliveryDetailsHooks.changedEnabledFieldForSpecificDeliveryState(view);

        // then
        verify(form).setFormEnabled(enabledFormAndOrderedProduct);
        verify(deliveredProducts).setEnabled(enabledDeliveredGrid);
        verify(deliveredProducts).setEditable(enabledDeliveredGrid);
        verify(orderedProducts).setEnabled(enabledFormAndOrderedProduct);
        verify(orderedProducts).setEditable(enabledFormAndOrderedProduct);
    }

    @Test
    public void shouldDisabledFormAndOrderedFieldWhenStateIsApproved() throws Exception {
        // given
        final boolean enabledFormAndOrderedProduct = false;
        final boolean enabledDeliveredGrid = true;
        when(delivery.getStringField(DeliveryFields.STATE)).thenReturn(DeliveryStateStringValues.APPROVED);

        // when
        deliveryDetailsHooks.changedEnabledFieldForSpecificDeliveryState(view);

        // then
        verify(form).setFormEnabled(enabledFormAndOrderedProduct);
        verify(deliveredProducts).setEnabled(enabledDeliveredGrid);
        verify(deliveredProducts).setEditable(enabledDeliveredGrid);
        verify(orderedProducts).setEnabled(enabledFormAndOrderedProduct);
        verify(orderedProducts).setEditable(enabledFormAndOrderedProduct);
    }

    @Test
    public void shouldEnabledFieldsWhenStateIsDraft() throws Exception {
        // given
        final boolean enabledFormAndOrderedProduct = true;
        final boolean enabledDeliveredGrid = true;
        when(delivery.getStringField(DeliveryFields.STATE)).thenReturn(DeliveryStateStringValues.DRAFT);

        // when
        deliveryDetailsHooks.changedEnabledFieldForSpecificDeliveryState(view);

        // then
        verify(form).setFormEnabled(enabledFormAndOrderedProduct);
        verify(deliveredProducts).setEnabled(enabledDeliveredGrid);
        verify(deliveredProducts).setEditable(enabledDeliveredGrid);
        verify(orderedProducts).setEnabled(enabledFormAndOrderedProduct);
        verify(orderedProducts).setEditable(enabledFormAndOrderedProduct);
    }

    @Test
    public void shouldDisabledFieldsWhenStateIsDeclined() throws Exception {
        // given
        final boolean enabledFormAndOrderedProduct = false;
        final boolean enabledDeliveredGrid = false;
        when(delivery.getStringField(DeliveryFields.STATE)).thenReturn(DeliveryStateStringValues.DECLINED);

        // when
        deliveryDetailsHooks.changedEnabledFieldForSpecificDeliveryState(view);

        // then
        verify(form).setFormEnabled(enabledFormAndOrderedProduct);
        verify(deliveredProducts).setEnabled(enabledDeliveredGrid);
        verify(deliveredProducts).setEditable(enabledDeliveredGrid);
        verify(orderedProducts).setEnabled(enabledFormAndOrderedProduct);
        verify(orderedProducts).setEditable(enabledFormAndOrderedProduct);
    }

    @Test
    public void shouldDisabledFieldsWhenStateIsReceived() throws Exception {
        // given
        final boolean enabledFormAndOrderedProduct = false;
        final boolean enabledDeliveredGrid = false;
        when(delivery.getStringField(DeliveryFields.STATE)).thenReturn(DeliveryStateStringValues.RECEIVED);

        // when
        deliveryDetailsHooks.changedEnabledFieldForSpecificDeliveryState(view);

        // then
        verify(form).setFormEnabled(enabledFormAndOrderedProduct);
        verify(deliveredProducts).setEnabled(enabledDeliveredGrid);
        verify(deliveredProducts).setEditable(enabledDeliveredGrid);
        verify(orderedProducts).setEnabled(enabledFormAndOrderedProduct);
        verify(orderedProducts).setEditable(enabledFormAndOrderedProduct);
    }

}
