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
package com.qcadoo.mes.masterOrders.hooks;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

public class MasterOrderProductDetailsHooksTest {

    private MasterOrderProductDetailsHooks masterOrderProductDetailsHooks;

    @Mock private ViewDefinitionState view;

    @Mock private FormComponent masterOrderProductForm;

    @Mock private FieldComponent cumulatedOrderQuantityUnitField, masterOrderQuantityUnitField, producedOrderQuantityUnitField, leftToRelease, leftToReleaseUnit, comments, masterOrderPositionStatus,
            quantityRemainingToOrderUnit, quantityTakenFromWarehouseUnit, quantityRemainingToOrder, quantityTakenFromWarehouse, releaseQuantityUnit, releaseQuantity, quantityToReleaseUnit, quantityToRelease;

    @Mock private LookupComponent productField;

    @Mock private Entity product, masterOrderProduct, masterOrder;

    @Before public void init() {
        masterOrderProductDetailsHooks = new MasterOrderProductDetailsHooks();

        MockitoAnnotations.initMocks(this);

        given(view.getComponentByReference(QcadooViewConstants.L_FORM)).willReturn(masterOrderProductForm);

        given(view.getComponentByReference(MasterOrderProductFields.PRODUCT)).willReturn(productField);

        given(view.getComponentByReference("cumulatedOrderQuantityUnit")).willReturn(cumulatedOrderQuantityUnitField);
        given(view.getComponentByReference("masterOrderQuantityUnit")).willReturn(masterOrderQuantityUnitField);
        given(view.getComponentByReference("producedOrderQuantityUnit")).willReturn(producedOrderQuantityUnitField);
        given(view.getComponentByReference("quantityRemainingToOrderUnit")).willReturn(quantityRemainingToOrderUnit);
        given(view.getComponentByReference("quantityTakenFromWarehouseUnit")).willReturn(quantityTakenFromWarehouseUnit);
        given(view.getComponentByReference("quantityRemainingToOrder")).willReturn(quantityRemainingToOrder);
        given(view.getComponentByReference("quantityTakenFromWarehouse")).willReturn(quantityTakenFromWarehouse);
        given(view.getComponentByReference("leftToReleaseUnit")).willReturn(leftToReleaseUnit);
        given(view.getComponentByReference("leftToRelease")).willReturn(leftToRelease);
        given(view.getComponentByReference("releaseQuantityUnit")).willReturn(releaseQuantityUnit);
        given(view.getComponentByReference("releaseQuantity")).willReturn(releaseQuantity);
        given(view.getComponentByReference("quantityToReleaseUnit")).willReturn(quantityToReleaseUnit);
        given(view.getComponentByReference("quantityToRelease")).willReturn(quantityToRelease);
        given(view.getComponentByReference("comments")).willReturn(comments);
        given(view.getComponentByReference("masterOrderPositionStatus")).willReturn(masterOrderPositionStatus);
        given(masterOrderProductForm.getPersistedEntityWithIncludedFormValues()).willReturn(masterOrderProduct);
        given(masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER)).willReturn(masterOrder);
    }

    @Test public final void shouldSetNullWhenProductDoesnotExists() {
        // given
        given(productField.getEntity()).willReturn(null);

        // when
        masterOrderProductDetailsHooks.fillUnitField(view);

        // then
        verify(cumulatedOrderQuantityUnitField).setFieldValue(null);
        verify(masterOrderQuantityUnitField).setFieldValue(null);
        verify(producedOrderQuantityUnitField).setFieldValue(null);
    }

    @Test public final void shouldSetUnitFromProduct() {
        // given
        String unit = "szt";

        given(productField.getEntity()).willReturn(product);
        given(product.getStringField(ProductFields.UNIT)).willReturn(unit);

        // when
        masterOrderProductDetailsHooks.fillUnitField(view);

        // then
        verify(cumulatedOrderQuantityUnitField).setFieldValue(unit);
        verify(masterOrderQuantityUnitField).setFieldValue(unit);
        verify(producedOrderQuantityUnitField).setFieldValue(unit);
    }

}
