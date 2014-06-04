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
package com.qcadoo.mes.masterOrders.hooks;

import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.CUMULATED_ORDER_QUANTITY;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.MASTER_ORDER_QUANTITY;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderType;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.ExpressionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;

public class MasterOrderDetailsHooksTest {

    private MasterOrderDetailsHooks masterOrderDetailsHooks;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FieldComponent masterOrderTypeField, technologyField, defaultTechnologyField, cumulatedQuantityField,
            masterOrderQuantityField, cumulatedOrderQuantityUnitField, masterOrderQuantityUnitField;

    @Mock
    private FormComponent form;

    @Mock
    private LookupComponent productField;

    @Mock
    private GridComponent masterOrderProducts;

    @Mock
    private ExpressionService expressionService;

    @Mock
    private TechnologyServiceO technologyServiceO;

    @Mock
    private Entity productEntity, defaultTechnologyEntity, masterOrderEntity;

    @Mock
    private ComponentState borderLayoutProductQuantity;

    @Before
    public void init() {
        masterOrderDetailsHooks = new MasterOrderDetailsHooks();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(masterOrderDetailsHooks, "technologyServiceO", technologyServiceO);
        ReflectionTestUtils.setField(masterOrderDetailsHooks, "expressionService", expressionService);
        given(view.getComponentByReference("form")).willReturn(form);
        given(view.getComponentByReference(MasterOrderFields.MASTER_ORDER_TYPE)).willReturn(masterOrderTypeField);
        given(view.getComponentByReference(MasterOrderFields.PRODUCT)).willReturn(productField);
        given(view.getComponentByReference(MasterOrderFields.TECHNOLOGY)).willReturn(technologyField);
        given(view.getComponentByReference(MasterOrderFields.DEFAULT_TECHNOLOGY)).willReturn(defaultTechnologyField);
        given(view.getComponentByReference(MasterOrderFields.CUMULATED_ORDER_QUANTITY)).willReturn(cumulatedQuantityField);
        given(view.getComponentByReference(MasterOrderFields.MASTER_ORDER_QUANTITY)).willReturn(masterOrderQuantityField);
        given(view.getComponentByReference("productsGrid")).willReturn(masterOrderProducts);
        given(view.getComponentByReference("borderLayoutProductQuantity")).willReturn(borderLayoutProductQuantity);
        given(view.getComponentByReference("cumulatedOrderQuantityUnit")).willReturn(cumulatedOrderQuantityUnitField);
        given(view.getComponentByReference("masterOrderQuantityUnit")).willReturn(masterOrderQuantityUnitField);
    }

    @Test
    public final void shouldInvisibleFieldWhenMasterOrderTypeValueIsEmty() {
        given(masterOrderTypeField.getFieldValue()).willReturn(null);
        // given
        masterOrderDetailsHooks.hideFieldDependOnMasterOrderType(view);
        // then
        Mockito.verify(productField).setVisible(false);
        Mockito.verify(defaultTechnologyField).setVisible(false);
        Mockito.verify(cumulatedQuantityField).setVisible(false);
        Mockito.verify(technologyField).setVisible(false);
        Mockito.verify(masterOrderQuantityField).setVisible(false);
        Mockito.verify(borderLayoutProductQuantity).setVisible(false);
        Mockito.verify(masterOrderProducts).setVisible(false);
    }

    @Test
    public final void shouldInvisibleFieldWhenMasterOrderTypeIsManyProducts() {
        // given
        given(masterOrderTypeField.getFieldValue()).willReturn(MasterOrderType.MANY_PRODUCTS.getStringValue());
        // given
        masterOrderDetailsHooks.hideFieldDependOnMasterOrderType(view);
        // then
        Mockito.verify(productField).setVisible(false);
        Mockito.verify(defaultTechnologyField).setVisible(false);
        Mockito.verify(cumulatedQuantityField).setVisible(false);
        Mockito.verify(technologyField).setVisible(false);
        Mockito.verify(masterOrderQuantityField).setVisible(false);
        Mockito.verify(borderLayoutProductQuantity).setVisible(false);
        Mockito.verify(masterOrderProducts).setVisible(true);
    }

    @Test
    public final void shouldInvisibleFieldWhenMasterOrderTypeIsUndefined() {
        // given
        given(masterOrderTypeField.getFieldValue()).willReturn(MasterOrderType.UNDEFINED.getStringValue());
        // given
        masterOrderDetailsHooks.hideFieldDependOnMasterOrderType(view);
        // then
        Mockito.verify(productField).setVisible(false);
        Mockito.verify(defaultTechnologyField).setVisible(false);
        Mockito.verify(cumulatedQuantityField).setVisible(false);
        Mockito.verify(technologyField).setVisible(false);
        Mockito.verify(masterOrderQuantityField).setVisible(false);
        Mockito.verify(borderLayoutProductQuantity).setVisible(false);
        Mockito.verify(masterOrderProducts).setVisible(false);

    }

    @Test
    public final void shouldVisibleFieldWhenMasterOrderTypeIsOnProduct() {
        // given
        given(masterOrderTypeField.getFieldValue()).willReturn(MasterOrderType.ONE_PRODUCT.getStringValue());
        // given
        masterOrderDetailsHooks.hideFieldDependOnMasterOrderType(view);
        // then
        verify(productField).setVisible(true);
        verify(defaultTechnologyField).setVisible(true);
        verify(cumulatedQuantityField).setVisible(true);
        verify(technologyField).setVisible(true);
        verify(masterOrderQuantityField).setVisible(true);
        verify(borderLayoutProductQuantity).setVisible(true);
        verify(masterOrderProducts).setVisible(false);

    }

    @Test
    public final void shouldFillDefaultTechnologyIfExists() {
        // given
        String defaultTechnologyExpression = "00001 - Tech-1";
        given(productField.getEntity()).willReturn(productEntity);
        given(technologyServiceO.getDefaultTechnology(productEntity)).willReturn(defaultTechnologyEntity);
        given(view.getLocale()).willReturn(Locale.getDefault());
        given(expressionService.getValue(defaultTechnologyEntity, "#number + ' - ' + #name", Locale.getDefault())).willReturn(
                defaultTechnologyExpression);
        // given
        masterOrderDetailsHooks.fillDefaultTechnology(view);
        // then

        verify(defaultTechnologyField).setFieldValue(defaultTechnologyExpression);
    }

    @Test
    public final void shouldFillNullWhenDefaultTechnlogyDoesnotExists() {
        // given
        given(productField.getEntity()).willReturn(null);
        // given
        masterOrderDetailsHooks.fillDefaultTechnology(view);
        // then
        verify(defaultTechnologyField).setFieldValue(null);
    }

    @Test
    public final void shouldShowMessageError() {
        // given
        BigDecimal cumulatedQuantity = BigDecimal.ONE;
        BigDecimal masterQuantity = BigDecimal.TEN;
        String masterOrderType = "02oneProduct";
        given(form.getEntity()).willReturn(masterOrderEntity);
        given(masterOrderEntity.getStringField(MasterOrderFields.MASTER_ORDER_TYPE)).willReturn(masterOrderType);
        given(masterOrderEntity.getDecimalField(MASTER_ORDER_QUANTITY)).willReturn(masterQuantity);
        given(masterOrderEntity.getDecimalField(CUMULATED_ORDER_QUANTITY)).willReturn(cumulatedQuantity);
        // when
        masterOrderDetailsHooks.showErrorWhenCumulatedQuantity(view);
        // then
        verify(form).addMessage("masterOrders.masterOrder.masterOrderCumulatedQuantityField.wrongQuantity", MessageType.INFO,
                false);
    }

    @Test
    public final void shouldDonotShowMessageError() {
        // given
        BigDecimal cumulatedQuantity = BigDecimal.TEN;
        BigDecimal masterQuantity = BigDecimal.ONE;
        String masterOrderType = "02oneProduct";
        given(form.getEntity()).willReturn(masterOrderEntity);
        given(masterOrderEntity.getStringField(MasterOrderFields.MASTER_ORDER_TYPE)).willReturn(masterOrderType);
        given(masterOrderEntity.getDecimalField(MASTER_ORDER_QUANTITY)).willReturn(masterQuantity);
        given(masterOrderEntity.getDecimalField(CUMULATED_ORDER_QUANTITY)).willReturn(cumulatedQuantity);
        // when
        masterOrderDetailsHooks.showErrorWhenCumulatedQuantity(view);
        // then
        verify(form, Mockito.never()).addMessage("masterOrders.masterOrder.masterOrderCumulatedQuantityField.wrongQuantity",
                MessageType.INFO, false);
    }

    @Test
    public final void shouldSetNullToUnitField() {
        // given
        given(productField.getEntity()).willReturn(null);

        // when
        masterOrderDetailsHooks.fillUnitField(view);
        // then
        verify(masterOrderQuantityUnitField).setFieldValue(null);
        verify(cumulatedOrderQuantityUnitField).setFieldValue(null);
    }

    @Test
    public final void shouldSetproductUnitToField() {
        String unit = "szt";
        // given
        given(productField.getEntity()).willReturn(productEntity);
        given(productEntity.getStringField("unit")).willReturn(unit);
        // when
        masterOrderDetailsHooks.fillUnitField(view);
        // then
        verify(masterOrderQuantityUnitField).setFieldValue(unit);
        verify(cumulatedOrderQuantityUnitField).setFieldValue(unit);

    }

}
