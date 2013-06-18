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
package com.qcadoo.mes.productionCounting.hooks;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.productionCounting.ProductionRecordService;
import com.qcadoo.mes.productionCounting.constants.ProductionRecordFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionRecordStateStringValues;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

public class ProductionRecordDetailsHooksTest {

    private static final String L_FORM = "form";

    private static final String L_IS_DISABLED = "isDisabled";

    private ProductionRecordDetailsHooks productionRecordDetailsHooks;

    @Mock
    private ProductionRecordService productionRecordService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent productionRecordForm;

    @Mock
    private FieldComponent stateField, isDisabledField;

    @Mock
    private LookupComponent orderLookup;

    @Mock
    private Entity productionRecord, order;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productionRecordDetailsHooks = new ProductionRecordDetailsHooks();

        ReflectionTestUtils.setField(productionRecordDetailsHooks, "productionRecordService", productionRecordService);

        given(view.getComponentByReference(L_FORM)).willReturn(productionRecordForm);

        given(view.getComponentByReference(ProductionRecordFields.STATE)).willReturn(stateField);
        given(view.getComponentByReference(ProductionRecordFields.ORDER)).willReturn(orderLookup);
        given(view.getComponentByReference(L_IS_DISABLED)).willReturn(isDisabledField);
    }

    @Test
    public void shouldSetStateToDraftWhenInitializeRecordDetailsViewIfProductionRecordIsntSaved() {
        // given
        given(productionRecordForm.getEntityId()).willReturn(null);

        // when
        productionRecordDetailsHooks.initializeRecordDetailsView(view);

        // then
        verify(stateField, Mockito.times(1)).setFieldValue(ProductionRecordStateStringValues.DRAFT);
        verify(isDisabledField, never()).setFieldValue(false);
        verify(productionRecordService, never()).setTimeAndPieceworkComponentsVisible(view, order);
    }

    @Test
    public void shouldSetStateToAcceptedWhenInitializeRecordDetailsViewIfProductionRecordIsSaved() {
        // given
        given(productionRecordForm.getEntityId()).willReturn(1L);
        given(productionRecordForm.getEntity()).willReturn(productionRecord);
        given(productionRecord.getField(ProductionRecordFields.STATE)).willReturn(ProductionRecordStateStringValues.ACCEPTED);
        given(orderLookup.getEntity()).willReturn(order);

        // when
        productionRecordDetailsHooks.initializeRecordDetailsView(view);

        // then
        verify(stateField, times(1)).setFieldValue(ProductionRecordStateStringValues.ACCEPTED);
        verify(isDisabledField, times(1)).setFieldValue(false);
        verify(productionRecordService, times(1)).setTimeAndPieceworkComponentsVisible(view, order);
    }

}
