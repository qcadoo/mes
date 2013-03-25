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
package com.qcadoo.mes.productionPerShift.listeners;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Maps;
import com.qcadoo.mes.productionPerShift.PPSHelper;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public class ProductionPerShiftListenersTest {

    private ProductionPerShiftListeners productionPerShiftListeners;

    @Mock
    private ViewDefinitionState viewState;

    @Mock
    private ComponentState componentState;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private PPSHelper helper;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        productionPerShiftListeners = new ProductionPerShiftListeners();

        ReflectionTestUtils.setField(productionPerShiftListeners, "helper", helper);
        ReflectionTestUtils.setField(productionPerShiftListeners, "dataDefinitionService", dataDefinitionService);
    }

    @Test
    public void shouldRedirectToProductionPerShiftView() {
        // given
        Long givenOrderId = 1L;
        Long expectedPpsId = 50L;

        given(componentState.getFieldValue()).willReturn(givenOrderId);
        given(helper.getPpsIdForOrder(givenOrderId)).willReturn(expectedPpsId);

        // when
        productionPerShiftListeners.redirectToProductionPerShift(viewState, componentState, new String[] {});

        // then
        verifyRedirectToPpsDetails(expectedPpsId);
    }

    @Test
    public void shouldRedirectToJustCreatedProductionPerShiftView() {
        // given
        Long givenOrderId = 1L;
        Long expectedPpsId = 50L;

        given(componentState.getFieldValue()).willReturn(givenOrderId);
        given(helper.getPpsIdForOrder(givenOrderId)).willReturn(null);
        given(helper.createPpsForOrderAndReturnId(givenOrderId)).willReturn(expectedPpsId);

        // when
        productionPerShiftListeners.redirectToProductionPerShift(viewState, componentState, new String[] {});

        // then
        verifyRedirectToPpsDetails(expectedPpsId);
    }

    private void verifyRedirectToPpsDetails(final Long expectedPpsId) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", expectedPpsId);

        verify(viewState).redirectTo("../page/productionPerShift/productionPerShiftDetails.html", false, true, parameters);
    }

    @Test
    public void shouldThrowExceptionIfProductionPerShiftCanNotBeSaved() {
        // given
        Long givenOrderId = 1L;

        given(componentState.getFieldValue()).willReturn(givenOrderId);
        given(helper.getPpsIdForOrder(givenOrderId)).willReturn(null);
        given(helper.createPpsForOrderAndReturnId(givenOrderId)).willReturn(null);

        // when & then
        try {
            productionPerShiftListeners.redirectToProductionPerShift(viewState, componentState, new String[] {});
            Assert.fail();
        } catch (NullPointerException ex) {
            // test passed
        }

    }
}
