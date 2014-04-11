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
package com.qcadoo.mes.productionCounting;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.internal.components.window.WindowComponentState;

public class ProductionTrackingServiceImplTest {

    private static final String L_TIME_TAB = "timeTab";

    private static final String L_PIECEWORK_TAB = "pieceworkTab";

    private ProductionTrackingService productionTrackingService;

    @Mock
    private ProductionCountingService productionCountingService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private Entity order;

    @Mock
    private LookupComponent technologyOperationComponentLookup;

    @Mock
    private ComponentState timeTab, pieceworkTab;

    @Mock
    private WindowComponentState window;

    @Mock
    private Ribbon ribbon;

    @Mock
    private RibbonActionItem calcTotalLaborTimeRibbonBtn;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        productionTrackingService = new ProductionTrackingServiceImpl();

        ReflectionTestUtils.setField(productionTrackingService, "productionCountingService", productionCountingService);

        given(view.getComponentByReference(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT)).willReturn(
                technologyOperationComponentLookup);

        given(view.getComponentByReference("window")).willReturn(window);
        given(window.getRibbon()).willReturn(ribbon);
        RibbonGroup workTimeGroup = mock(RibbonGroup.class);
        given(ribbon.getGroupByName("workTime")).willReturn(workTimeGroup);
        given(workTimeGroup.getItemByName("calcTotalLaborTime")).willReturn(calcTotalLaborTimeRibbonBtn);

        FormComponent form = mock(FormComponent.class);
        given(view.getComponentByReference("form")).willReturn(form);
        given(form.getEntity()).willReturn(order);
        given(form.getPersistedEntityWithIncludedFormValues()).willReturn(order);

        given(view.getComponentByReference(L_TIME_TAB)).willReturn(timeTab);
        given(view.getComponentByReference(L_PIECEWORK_TAB)).willReturn(pieceworkTab);
    }

    @Test
    public void shouldNotSetTimeAndPieceworkTabVisibleIfTypeIsBasic() {
        // given
        String typeOfProductionRecording = TypeOfProductionRecording.BASIC.getStringValue();

        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(typeOfProductionRecording);

        given(order.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME)).willReturn(true);
        given(order.getBooleanField(OrderFieldsPC.REGISTER_PIECEWORK)).willReturn(true);

        given(productionCountingService.isTypeOfProductionRecordingBasic(typeOfProductionRecording)).willReturn(true);
        given(productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)).willReturn(false);

        // when
        productionTrackingService.setTimeAndPieceworkComponentsVisible(view, order);

        // then
        verify(technologyOperationComponentLookup).setVisible(false);

        verify(timeTab).setVisible(false);
        verify(pieceworkTab).setVisible(false);
        verify(calcTotalLaborTimeRibbonBtn).setEnabled(false);
        verify(calcTotalLaborTimeRibbonBtn).requestUpdate(true);
    }

    @Test
    public void shoulsSetTimeAndPieceworkTabVisibleIfTypeIsCumulated() {
        // given
        String typeOfProductionRecording = TypeOfProductionRecording.CUMULATED.getStringValue();

        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(typeOfProductionRecording);

        given(order.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME)).willReturn(true);
        given(order.getBooleanField(OrderFieldsPC.REGISTER_PIECEWORK)).willReturn(true);

        given(productionCountingService.isTypeOfProductionRecordingBasic(typeOfProductionRecording)).willReturn(false);
        given(productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)).willReturn(false);

        // when
        productionTrackingService.setTimeAndPieceworkComponentsVisible(view, order);

        // then
        verify(technologyOperationComponentLookup).setVisible(false);

        verify(timeTab).setVisible(true);
        verify(pieceworkTab).setVisible(false);
        verify(calcTotalLaborTimeRibbonBtn).setEnabled(true);
        verify(calcTotalLaborTimeRibbonBtn).requestUpdate(true);
    }

    @Test
    public void shoulsSetTimeAndPieceworkTabVisibleIfTypeIsForEach() {
        // given
        String typeOfProductionRecording = TypeOfProductionRecording.FOR_EACH.getStringValue();

        given(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)).willReturn(typeOfProductionRecording);

        given(order.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME)).willReturn(true);
        given(order.getBooleanField(OrderFieldsPC.REGISTER_PIECEWORK)).willReturn(true);

        given(productionCountingService.isTypeOfProductionRecordingBasic(typeOfProductionRecording)).willReturn(false);
        given(productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)).willReturn(true);

        // when
        productionTrackingService.setTimeAndPieceworkComponentsVisible(view, order);

        // then
        verify(technologyOperationComponentLookup).setVisible(true);

        verify(timeTab).setVisible(true);
        verify(pieceworkTab).setVisible(true);
        verify(calcTotalLaborTimeRibbonBtn).setEnabled(true);
        verify(calcTotalLaborTimeRibbonBtn).requestUpdate(true);
    }

}
