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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

// TODO lupo fix problem with test
@Ignore
public class DeliveredProductDetailsHooksTest {

    private DeliveredProductDetailsHooks deliveredProductDetailsHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private LookupComponent productLookup;

    @Mock
    private Entity product;

    @Mock
    private FieldComponent deliveredUnitField, damagedUnitField, orderedUnitField, deliveredQuantityField;

    @Before
    public void init() {
        deliveredProductDetailsHooks = new DeliveredProductDetailsHooks();
        MockitoAnnotations.initMocks(this);

        when(view.getComponentByReference("product")).thenReturn(productLookup);
        when(view.getComponentByReference("deliveredQuantityUNIT")).thenReturn(deliveredUnitField);
        when(view.getComponentByReference("damagedQuantityUNIT")).thenReturn(damagedUnitField);
        when(view.getComponentByReference("orderedQuantityUNIT")).thenReturn(orderedUnitField);
        when(view.getComponentByReference("deliveredQuantity")).thenReturn(deliveredQuantityField);
        when(productLookup.getEntity()).thenReturn(product);
    }

    @Test
    public void shouldSetProductUnitWhenProductIsSelected() throws Exception {
        // given
        String unit = "szt";
        when(productLookup.getEntity()).thenReturn(product);
        when(product.getStringField("unit")).thenReturn(unit);
        // when
        deliveredProductDetailsHooks.fillUnitFields(view);
        // then
        verify(deliveredUnitField).setFieldValue("szt");
        verify(damagedUnitField).setFieldValue("szt");
        verify(orderedUnitField).setFieldValue("szt");
    }

    @Test
    public void shouldReturnWhenProductIsNull() throws Exception {
        // given
        when(productLookup.getEntity()).thenReturn(null);
        // when
        deliveredProductDetailsHooks.fillUnitFields(view);
        // then
        verify(deliveredUnitField).setFieldValue("");
        verify(damagedUnitField).setFieldValue("");
        verify(orderedUnitField).setFieldValue("");
    }

    @Test
    public void shouldSetRequiredOnDeliveredQuantityField() throws Exception {
        // when
        deliveredProductDetailsHooks.setDeliveredQuantityFieldRequired(view);
        // then
        verify(deliveredQuantityField).setRequired(true);
    }

}
