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
package com.qcadoo.mes.productionCounting.listeners;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.productionCounting.internal.ProductionRecordViewService;
import com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

public class ProductionRecordDetailsHooksTest {

    private ProductionRecordDetailsHooks productionRecordDetailsHooks;

    @Mock
    private ProductionRecordViewService productionRecordViewService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent form;

    @Mock
    private FieldComponent technologyInstanceOperationComponent;

    @Mock
    private ComponentState dummyComponent, recordOperationProductOutComponent, recordOperationProductInComponent, isDisabled;

    @Mock
    private Entity order;

    @Mock
    private LookupComponent lookupComponent;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        productionRecordDetailsHooks = new ProductionRecordDetailsHooks();

        ReflectionTestUtils.setField(productionRecordDetailsHooks, "productionRecordViewService", productionRecordViewService);

        given(view.getComponentByReference(Mockito.anyString())).willReturn(dummyComponent);

        given(view.getComponentByReference("technologyInstanceOperationComponent")).willReturn(
                technologyInstanceOperationComponent);
        given(view.getComponentByReference("recordOperationProductOutComponent")).willReturn(recordOperationProductOutComponent);
        given(view.getComponentByReference("recordOperationProductInComponent")).willReturn(recordOperationProductInComponent);
        given(view.getComponentByReference("isDisabled")).willReturn(isDisabled);

        given(view.getComponentByReference("form")).willReturn(form);
        given(view.getComponentByReference("order")).willReturn(lookupComponent);

        given(lookupComponent.getEntity()).willReturn(order);
    }

    @Test
    public void shouldDisableTiocLookupIfRecordingTypeIsNotEqualsToForEachOperation() {
        // given
        String recordingType = "DefinitelyNot" + TypeOfProductionRecording.FOR_EACH.getStringValue();
        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(recordingType);
        given(order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT)).willReturn(true);
        given(order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT)).willReturn(true);

        // when
        productionRecordDetailsHooks.initializeRecordDetailsView(view);

        // then
        verify(productionRecordViewService).setTimeAndPiecworkComponentsVisible(recordingType, order, view);
        verify(technologyInstanceOperationComponent).setVisible(false);
        verify(recordOperationProductOutComponent).setVisible(true);
        verify(recordOperationProductInComponent).setVisible(true);
        verify(isDisabled).setFieldValue(false);
    }

    @Test
    public void shouldDisableInputProductsGridIfItsRegistrationIsDisabled() {
        // given
        String recordingType = TypeOfProductionRecording.FOR_EACH.getStringValue();
        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(recordingType);
        given(order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT)).willReturn(true);
        given(order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT)).willReturn(false);

        // when
        productionRecordDetailsHooks.initializeRecordDetailsView(view);

        // then
        verify(productionRecordViewService).setTimeAndPiecworkComponentsVisible(recordingType, order, view);
        verify(technologyInstanceOperationComponent).setVisible(true);
        verify(recordOperationProductOutComponent).setVisible(true);
        verify(recordOperationProductInComponent).setVisible(false);
        verify(isDisabled).setFieldValue(false);
    }

    @Test
    public void shouldDisableOutputProductsGridIfItsRegistrationIsDisabled() {
        // given
        String recordingType = TypeOfProductionRecording.FOR_EACH.getStringValue();
        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(recordingType);
        given(order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT)).willReturn(false);
        given(order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT)).willReturn(true);

        // when
        productionRecordDetailsHooks.initializeRecordDetailsView(view);

        // then
        verify(productionRecordViewService).setTimeAndPiecworkComponentsVisible(recordingType, order, view);
        verify(technologyInstanceOperationComponent).setVisible(true);
        verify(recordOperationProductOutComponent).setVisible(false);
        verify(recordOperationProductInComponent).setVisible(true);
        verify(isDisabled).setFieldValue(false);
    }

    @Test
    public void shouldPassNullToViewService() {
        // given
        given(order.getBooleanField("registerPiecework")).willReturn(false);

        // when
        productionRecordDetailsHooks.initializeRecordDetailsView(view);

        // then
        verify(productionRecordViewService).setTimeAndPiecworkComponentsVisible(null, order, view);
    }

    @Test
    public void shouldPassRecordingTypeToViewService() {
        // given
        String recordingType = TypeOfProductionRecording.FOR_EACH.getStringValue();
        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(recordingType);
        given(order.getBooleanField("registerProductionTime")).willReturn(false);

        // when
        productionRecordDetailsHooks.initializeRecordDetailsView(view);

        // then
        verify(productionRecordViewService).setTimeAndPiecworkComponentsVisible(recordingType, order, view);
    }
}
