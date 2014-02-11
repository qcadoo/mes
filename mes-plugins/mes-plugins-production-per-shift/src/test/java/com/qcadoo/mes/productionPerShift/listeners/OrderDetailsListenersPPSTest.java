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
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public class OrderDetailsListenersPPSTest {

    private OrderDetailsListenersPPS orderDetailsListenersPPS;

    @Mock
    private PPSHelper ppsHelper;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private ComponentState state;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderDetailsListenersPPS = new OrderDetailsListenersPPS();

        ReflectionTestUtils.setField(orderDetailsListenersPPS, "ppsHelper", ppsHelper);
    }

    @Test
    public void shouldRedirectToProductionPerShiftView() {
        // given
        Long givenOrderId = 1L;
        Long expectedPpsId = 50L;

        given(state.getFieldValue()).willReturn(givenOrderId);
        given(ppsHelper.getPpsIdForOrder(givenOrderId)).willReturn(expectedPpsId);

        // when
        orderDetailsListenersPPS.redirectToProductionPerShift(view, state, new String[] {});

        // then
        verifyRedirectToPpsDetails(expectedPpsId);
    }

    @Test
    public void shouldRedirectToJustCreatedProductionPerShiftView() {
        // given
        Long givenOrderId = 1L;
        Long expectedPpsId = 50L;

        given(state.getFieldValue()).willReturn(givenOrderId);
        given(ppsHelper.getPpsIdForOrder(givenOrderId)).willReturn(null);
        given(ppsHelper.createPpsForOrderAndReturnId(givenOrderId)).willReturn(expectedPpsId);

        // when
        orderDetailsListenersPPS.redirectToProductionPerShift(view, state, new String[] {});

        // then
        verifyRedirectToPpsDetails(expectedPpsId);
    }

    private void verifyRedirectToPpsDetails(final Long expectedPpsId) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", expectedPpsId);

        verify(view).redirectTo("../page/productionPerShift/productionPerShiftDetails.html", false, true, parameters);
    }

    @Test
    public void shouldThrowExceptionIfProductionPerShiftCanNotBeSaved() {
        // given
        Long givenOrderId = 1L;

        given(state.getFieldValue()).willReturn(givenOrderId);
        given(ppsHelper.getPpsIdForOrder(givenOrderId)).willReturn(null);
        given(ppsHelper.createPpsForOrderAndReturnId(givenOrderId)).willReturn(null);

        // when & then
        try {
            orderDetailsListenersPPS.redirectToProductionPerShift(view, state, new String[] {});
            Assert.fail();
        } catch (NullPointerException ex) {
            // test passed
        }
    }

}
