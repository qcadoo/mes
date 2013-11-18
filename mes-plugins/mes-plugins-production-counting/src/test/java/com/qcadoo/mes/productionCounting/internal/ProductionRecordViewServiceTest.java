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
    private FieldComponent technologyInstanceOperationComponent;

    @Mock
    private ComponentState dummyComponent, borderLayoutTime, borderLayoutPiecework;

    @Mock
    private Entity order;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        productionRecordViewService = new ProductionRecordViewService();

        given(view.getComponentByReference(Mockito.anyString())).willReturn(dummyComponent);

        given(view.getComponentByReference("technologyInstanceOperationComponent")).willReturn(
                technologyInstanceOperationComponent);

        given(view.getComponentByReference("borderLayoutTime")).willReturn(borderLayoutTime);
        given(view.getComponentByReference("borderLayoutPiecework")).willReturn(borderLayoutPiecework);
    }

    @Test
    public void shouldDisableTimePanelIfTimeRegistrationIsDisabled() {
        // when
        productionRecordViewService.setTimeAndPiecworkComponentsVisible(null, order, view);

        // then
        verify(borderLayoutTime).setVisible(false);
    }

    @Test
    public void shouldDisablePieceworkPanelIfPieceworkRegistrationIsDisabled() {
        // when
        productionRecordViewService.setTimeAndPiecworkComponentsVisible(null, order, view);

        // then
        verify(borderLayoutPiecework).setVisible(false);
    }

    @Test
    public void shouldEnableTimePanelIfProductionRecordingTypeIsntSetToSimpleAndRegisterTimeIsSetToTrue() {
        // when
        productionRecordViewService.setTimeAndPiecworkComponentsVisible(TypeOfProductionRecording.FOR_EACH.getStringValue(),
                order, view);

        // then
        verify(borderLayoutTime).setVisible(false);
    }

    @Test
    public void shouldEnablePieceworkPanelIfProductionRecordingTypeIsForOperationAndRegisterPiecworkIsSetToTrue() {
        // given
        given(order.getBooleanField("registerPiecework")).willReturn(true);

        // when
        productionRecordViewService.setTimeAndPiecworkComponentsVisible(TypeOfProductionRecording.FOR_EACH.getStringValue(),
                order, view);

        // then
        verify(borderLayoutPiecework).setVisible(true);
    }
}
