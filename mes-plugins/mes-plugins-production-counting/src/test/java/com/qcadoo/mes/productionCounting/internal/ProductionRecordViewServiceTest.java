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
package com.qcadoo.mes.productionCounting.internal;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

public class ProductionRecordViewServiceTest {

    private ProductionRecordViewService productionRecordViewService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent form;

    @Mock
    private FieldComponent state, technologyInstanceOperationComponent;

    @Mock
    private ComponentState lookup, dummyComponent, borderLayoutTime, borderLayoutPiecework;

    @Mock
    private Entity formEntity, order;

    @Mock
    private LookupComponent lookupComponent;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        productionRecordViewService = new ProductionRecordViewService();

        ReflectionTestUtils.setField(productionRecordViewService, "dataDefinitionService", dataDefinitionService);

        given(view.getComponentByReference(Mockito.anyString())).willReturn(dummyComponent);

        given(view.getComponentByReference("technologyInstanceOperationComponent")).willReturn(
                technologyInstanceOperationComponent);
        given(view.getComponentByReference("form")).willReturn(form);
        given(view.getComponentByReference("state")).willReturn(state);
        given(view.getComponentByReference("order")).willReturn(lookupComponent);

        given(lookupComponent.getEntity()).willReturn(order);

        given(form.getEntity()).willReturn(formEntity);
        given(formEntity.getDataDefinition()).willReturn(dataDefinition);
        given(form.getEntityId()).willReturn(1L);
        given(dataDefinition.get(1L)).willReturn(formEntity);
        given(view.getComponentByReference("order")).willReturn(lookupComponent);

        given(dataDefinitionService.get("orders", "order")).willReturn(dataDefinition);
    }

    @Test
    public void shouldDisableTimePanelIfTimeRegistrationIsDisabled() {
        // given
        given(order.getBooleanField("registerProductionTime")).willReturn(false);
        given(view.getComponentByReference("borderLayoutTime")).willReturn(borderLayoutTime);
        // when
        productionRecordViewService.initializeRecordDetailsView(view);

        // then
        verify(borderLayoutTime).setVisible(false);
    }

    @Test
    public void shouldDisablePieceworkPanelIfPieceworkRegistrationIsDisabled() {
        // given
        given(order.getBooleanField("registerPiecework")).willReturn(false);
        given(view.getComponentByReference("borderLayoutPiecework")).willReturn(borderLayoutPiecework);

        // when
        productionRecordViewService.initializeRecordDetailsView(view);

        // then
        verify(borderLayoutPiecework).setVisible(false);
    }

    @Test
    public void shouldEnableTimePanelIfProductionRecordingTypeIsntSetToSimpleAndRegisterTimeIsSetToTrue() {
        // given
        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(
                TypeOfProductionRecording.FOR_EACH.getStringValue());
        given(order.getBooleanField("registerProductionTime")).willReturn(false);
        given(view.getComponentByReference("borderLayoutTime")).willReturn(borderLayoutTime);

        // when
        productionRecordViewService.initializeRecordDetailsView(view);

        // then
        verify(borderLayoutTime).setVisible(false);
    }

    @Test
    public void shouldEnablePieceworkPanelIfProductionRecordingTypeIsForOperationAndRegisterPiecworkIsSetToTrue() {
        // given
        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(
                TypeOfProductionRecording.FOR_EACH.getStringValue());
        given(order.getBooleanField("registerPiecework")).willReturn(true);
        given(view.getComponentByReference("borderLayoutPiecework")).willReturn(borderLayoutPiecework);

        // when
        productionRecordViewService.initializeRecordDetailsView(view);

        // then
        verify(borderLayoutPiecework).setVisible(true);
    }
}
