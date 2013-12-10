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

import com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.internal.components.window.WindowComponentState;

public class ProductionRecordViewServiceTest {

    private ProductionRecordViewService productionRecordViewService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FieldComponent technologyInstanceOperationComponent;

    @Mock
    private WindowComponentState windowComponent;

    @Mock
    private Ribbon ribbon;

    @Mock
    private RibbonGroup workTimeRibbonGroup;

    @Mock
    private RibbonActionItem calcTotalLaborTimeBtn;

    @Mock
    private ComponentState dummyComponent, timeTab, pieceworkTab;

    @Mock
    private Entity order, formEntity;

    @Mock
    private FormComponent formComponent;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        productionRecordViewService = new ProductionRecordViewService();

        given(view.getComponentByReference(Mockito.anyString())).willReturn(dummyComponent);

        given(view.getComponentByReference("technologyInstanceOperationComponent")).willReturn(
                technologyInstanceOperationComponent);

        given(view.getComponentByReference("timeTab")).willReturn(timeTab);
        given(view.getComponentByReference("pieceworkTab")).willReturn(pieceworkTab);
        given(view.getComponentByReference("window")).willReturn(windowComponent);
        given(windowComponent.getRibbon()).willReturn(ribbon);
        given(ribbon.getGroupByName("workTime")).willReturn(workTimeRibbonGroup);
        given(workTimeRibbonGroup.getItemByName("calcTotalLaborTime")).willReturn(calcTotalLaborTimeBtn);

        given(view.getComponentByReference("form")).willReturn(formComponent);
        given(formComponent.getEntity()).willReturn(formEntity);
    }

    @Test
    public void shouldDisableTimePanelIfTimeRegistrationIsDisabled() {
        // when
        productionRecordViewService.setTimeAndPiecworkComponentsVisible(null, order, view);

        // then
        verify(timeTab).setVisible(false);
        verify(calcTotalLaborTimeBtn).setEnabled(false);
    }

    @Test
    public void shouldDisablePieceworkPanelIfPieceworkRegistrationIsDisabled() {
        // when
        productionRecordViewService.setTimeAndPiecworkComponentsVisible(null, order, view);

        // then
        verify(pieceworkTab).setVisible(false);
    }

    @Test
    public void shouldEnableTimePanelIfProductionRecordingTypeIsntSetToSimpleAndRegisterTimeIsSetToTrue() {
        // when
        productionRecordViewService.setTimeAndPiecworkComponentsVisible(TypeOfProductionRecording.FOR_EACH.getStringValue(),
                order, view);

        // then
        verify(timeTab).setVisible(false);
        verify(calcTotalLaborTimeBtn).setEnabled(false);
    }

    @Test
    public void shouldEnablePieceworkPanelIfProductionRecordingTypeIsForOperationAndRegisterPiecworkIsSetToTrue() {
        // given
        given(order.getBooleanField("registerPiecework")).willReturn(true);

        // when
        productionRecordViewService.setTimeAndPiecworkComponentsVisible(TypeOfProductionRecording.FOR_EACH.getStringValue(),
                order, view);

        // then
        verify(pieceworkTab).setVisible(true);
    }
}
