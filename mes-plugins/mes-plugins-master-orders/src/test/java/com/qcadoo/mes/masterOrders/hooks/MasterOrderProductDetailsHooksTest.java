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

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

public class MasterOrderProductDetailsHooksTest {

    private static final String L_FORM = "form";

    private MasterOrderProductDetailsHooks masterOrderProductDetailsHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent masterOrderProductForm;

    @Mock
    private FieldComponent cumulatedOrderQuantityUnitField, masterOrderQuantityUnitField, producedOrderQuantityUnitField,
            leftToRelease, leftToReleaseUnit, comments, masterOrderPositionStatus;

    @Mock
    private LookupComponent productField;

    @Mock
    private Entity product, masterOrderProduct, masterOrder;

    @Before
    public void init() {
        masterOrderProductDetailsHooks = new MasterOrderProductDetailsHooks();

        MockitoAnnotations.initMocks(this);

        given(view.getComponentByReference(L_FORM)).willReturn(masterOrderProductForm);

        given(view.getComponentByReference(MasterOrderProductFields.PRODUCT)).willReturn(productField);

        given(view.getComponentByReference("cumulatedOrderQuantityUnit")).willReturn(cumulatedOrderQuantityUnitField);
        given(view.getComponentByReference("masterOrderQuantityUnit")).willReturn(masterOrderQuantityUnitField);
        given(view.getComponentByReference("producedOrderQuantityUnit")).willReturn(producedOrderQuantityUnitField);
        given(view.getComponentByReference("leftToReleaseUnit")).willReturn(leftToReleaseUnit);
        given(view.getComponentByReference("leftToRelease")).willReturn(leftToRelease);
        given(view.getComponentByReference("comments")).willReturn(comments);
        given(view.getComponentByReference("masterOrderPositionStatus")).willReturn(masterOrderPositionStatus);
        given(masterOrderProductForm.getEntity()).willReturn(masterOrderProduct);
        given(masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER)).willReturn(masterOrder);
    }

    @Test
    public final void shouldSetNullWhenProductDoesnotExists() {
        // given
        given(productField.getEntity()).willReturn(null);

        // when
        masterOrderProductDetailsHooks.fillUnitField(view);

        // then
        verify(cumulatedOrderQuantityUnitField).setFieldValue(null);
        verify(masterOrderQuantityUnitField).setFieldValue(null);
        verify(producedOrderQuantityUnitField).setFieldValue(null);
    }

    @Test
    public final void shouldSetUnitFromProduct() {
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

    @Test
    public final void shouldShowMessageError() {
        // given
        String masterOrderType = "03manyProducts";
        BigDecimal cumulatedOrderQuantity = BigDecimal.ONE;
        BigDecimal masterOrderQuantity = BigDecimal.TEN;

        given(masterOrderProduct.isValid()).willReturn(true);

        given(masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_TYPE)).willReturn(masterOrderType);

        given(masterOrderProduct.getDecimalField(MasterOrderProductFields.CUMULATED_ORDER_QUANTITY)).willReturn(
                cumulatedOrderQuantity);
        given(masterOrderProduct.getDecimalField(MasterOrderProductFields.MASTER_ORDER_QUANTITY)).willReturn(masterOrderQuantity);

        // when
        masterOrderProductDetailsHooks.showErrorWhenCumulatedQuantity(view);

        // then
        verify(masterOrderProductForm).addMessage("masterOrders.masterOrder.masterOrderCumulatedQuantityField.wrongQuantity",
                MessageType.INFO, false);
    }

    @Test
    public final void shouldDonotShowMessageError() {
        // given
        String masterOrderType = "03manyProducts";
        BigDecimal cumulatedOrderQuantity = BigDecimal.TEN;
        BigDecimal masterOrderQuantity = BigDecimal.ONE;

        given(masterOrderProduct.isValid()).willReturn(true);

        given(masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_TYPE)).willReturn(masterOrderType);

        given(masterOrderProduct.getDecimalField(MasterOrderProductFields.CUMULATED_ORDER_QUANTITY)).willReturn(
                cumulatedOrderQuantity);
        given(masterOrderProduct.getDecimalField(MasterOrderProductFields.MASTER_ORDER_QUANTITY)).willReturn(masterOrderQuantity);

        // when
        masterOrderProductDetailsHooks.showErrorWhenCumulatedQuantity(view);
        // then
        verify(masterOrderProductForm, Mockito.never()).addMessage(
                "masterOrders.masterOrder.masterOrderCumulatedQuantityField.wrongQuantity", MessageType.INFO, false);
    }

}
