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
import com.qcadoo.view.api.components.LookupComponent;

public class ProductionTrackingServiceImplTest {

    private static final String L_BORDER_LAYOUT_TIME = "borderLayoutTime";

    private static final String L_BORDER_LAYOUT_PIECEWORK = "borderLayoutPiecework";

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
    private ComponentState borderLayoutTime, borderLayoutPiecework;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        productionTrackingService = new ProductionTrackingServiceImpl();

        ReflectionTestUtils.setField(productionTrackingService, "productionCountingService", productionCountingService);

        given(view.getComponentByReference(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT)).willReturn(
                technologyOperationComponentLookup);

        given(view.getComponentByReference(L_BORDER_LAYOUT_TIME)).willReturn(borderLayoutTime);
        given(view.getComponentByReference(L_BORDER_LAYOUT_PIECEWORK)).willReturn(borderLayoutPiecework);
    }

    @Test
    public void shouldntSetTimeAndPieceworkComponentsVisibleIfTypeIsBasic() {
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

        verify(borderLayoutTime).setVisible(false);
        verify(borderLayoutPiecework).setVisible(false);
    }

    @Test
    public void shoulSetTimeAndPieceworkComponentsVisibleIfTypeIsCumulated() {
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

        verify(borderLayoutTime).setVisible(true);
        verify(borderLayoutPiecework).setVisible(false);
    }

    @Test
    public void shoulSetTimeAndPieceworkComponentsVisibleIfTypeIsForEach() {
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

        verify(borderLayoutTime).setVisible(true);
        verify(borderLayoutPiecework).setVisible(true);
    }

}