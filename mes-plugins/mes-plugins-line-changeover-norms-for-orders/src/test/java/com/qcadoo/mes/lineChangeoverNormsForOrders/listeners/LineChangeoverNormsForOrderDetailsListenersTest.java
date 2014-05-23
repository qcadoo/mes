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
package com.qcadoo.mes.lineChangeoverNormsForOrders.listeners;

import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.LineChangeoverNormsForOrdersConstants.PREVIOUS_ORDER_FIELDS;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.LINE_CHANGEOVER_NORM;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.OWN_LINE_CHANGEOVER;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.OWN_LINE_CHANGEOVER_DURATION;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.PREVIOUS_ORDER;
import static com.qcadoo.testing.model.EntityTestUtils.stubId;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

public class LineChangeoverNormsForOrderDetailsListenersTest {

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    private static final long L_ID = 1L;

    private static final String L_TECHNOLOGY_GROUP_NUMBER = "000001";

    private static final String L_TECHNOLOGY_NUMBER = "000001";

    private LineChangeoverNormsForOrderDetailsListeners lineChangeoverNormsForOrderDetailsListeners;

    @Mock
    private OrderService orderService;

    @Mock
    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private Entity entityWithId;

    @Mock
    private FieldComponent previousOrderTechnologyGroupNumberField, technologyGroupNumberField,
            previousOrderTechnologyNumberField, technologyNumberField;

    @Mock
    private LookupComponent orderLookup, previousOrderLookup, lineChangeoverNormLookup;

    @Captor
    private ArgumentCaptor<Map<String, Object>> parametersCaptor;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        lineChangeoverNormsForOrderDetailsListeners = new LineChangeoverNormsForOrderDetailsListeners();

        setField(lineChangeoverNormsForOrderDetailsListeners, "orderService", orderService);
        setField(lineChangeoverNormsForOrderDetailsListeners, "lineChangeoverNormsForOrdersService",
                lineChangeoverNormsForOrdersService);

        given(view.getComponentByReference("order")).willReturn(orderLookup);
        given(view.getComponentByReference(PREVIOUS_ORDER)).willReturn(previousOrderLookup);
        given(view.getComponentByReference(LINE_CHANGEOVER_NORM)).willReturn(lineChangeoverNormLookup);

        given(view.getComponentByReference("previousOrderTechnologyNumber")).willReturn(previousOrderTechnologyNumberField);
        given(view.getComponentByReference("technologyNumber")).willReturn(technologyNumberField);

        given(view.getComponentByReference("previousOrderTechnologyGroupNumber")).willReturn(
                previousOrderTechnologyGroupNumberField);
        given(view.getComponentByReference("technologyGroupNumber")).willReturn(technologyGroupNumberField);

        stubId(entityWithId, L_ID);
    }

    private void stubLookup(final LookupComponent lookup, final Entity entity) {
        given(lookup.isEmpty()).willReturn(entity == null);
        given(lookup.getEntity()).willReturn(entity);
        if (entity == null) {
            given(lookup.getFieldValue()).willReturn(null);
        } else {
            Long id = entity.getId();
            given(lookup.getFieldValue()).willReturn(id);
        }
    }

    @Test
    public void shouldNotRedirectToPreviousOrderIfPreviousOrderIsNull() {
        // given
        stubLookup(previousOrderLookup, null);

        // when
        lineChangeoverNormsForOrderDetailsListeners.showPreviousOrder(view, null, null);

        // then
        verify(view, never()).redirectTo(anyString(), anyBoolean(), anyBoolean(), anyMap());
    }

    @Test
    public void shouldRedirectToPreviousOrderIfPreviousOrderIsNotNull() {
        // given
        stubLookup(previousOrderLookup, entityWithId);

        // when
        lineChangeoverNormsForOrderDetailsListeners.showPreviousOrder(view, null, null);

        // then
        String url = "../page/orders/orderDetails.html";
        verify(view).redirectTo(eq(url), anyBoolean(), anyBoolean(), parametersCaptor.capture());
        Map<String, Object> usedParameters = parametersCaptor.getValue();
        assertEquals(L_ID, usedParameters.get("form.id"));
    }

    @Test
    public void shouldNotRedirectToBestFittingLineChangeoverNormIfLineChangeoverIsNull() {
        // given
        stubLookup(lineChangeoverNormLookup, null);

        // when
        lineChangeoverNormsForOrderDetailsListeners.showBestFittingLineChangeoverNorm(view, null, null);

        // then
        verify(view, never()).redirectTo(anyString(), anyBoolean(), anyBoolean(), anyMap());
    }

    @Test
    public void shouldRedirectToBestFittingLineChangeoverNormIfLineChangeoverNormIsNotNull() {
        // given
        stubLookup(lineChangeoverNormLookup, entityWithId);

        // when
        lineChangeoverNormsForOrderDetailsListeners.showBestFittingLineChangeoverNorm(view, null, null);

        // then
        String url = "../page/lineChangeoverNorms/lineChangeoverNormsDetails.html";
        verify(view).redirectTo(eq(url), anyBoolean(), anyBoolean(), parametersCaptor.capture());
        Map<String, Object> usedParameters = parametersCaptor.getValue();
        assertEquals(L_ID, usedParameters.get("form.id"));
    }

    @Test
    public void shouldNotRedirectToLineChangeoverNormForGroupIfTechnologyGroupNumbersAreEmpty() {
        // given
        given(previousOrderTechnologyGroupNumberField.getFieldValue()).willReturn(null);
        given(technologyGroupNumberField.getFieldValue()).willReturn(null);

        // when
        lineChangeoverNormsForOrderDetailsListeners.showLineChangeoverNormForGroup(view, null, null);

        // then
        verify(view, never()).redirectTo(anyString(), anyBoolean(), anyBoolean(), anyMap());
    }

    @Test
    public void shouldRedirectToLineChangeoverNormForGroupIfTechnologyGroupNumbersAreNotEmpty() {
        // given
        given(previousOrderTechnologyGroupNumberField.getFieldValue()).willReturn(L_TECHNOLOGY_GROUP_NUMBER);
        given(technologyGroupNumberField.getFieldValue()).willReturn(L_TECHNOLOGY_GROUP_NUMBER);

        // when
        lineChangeoverNormsForOrderDetailsListeners.showLineChangeoverNormForGroup(view, null, null);

        // then
        String url = "../page/lineChangeoverNorms/lineChangeoverNormsList.html";
        verify(view).redirectTo(eq(url), anyBoolean(), anyBoolean(), parametersCaptor.capture());
        Map<String, Object> usedParameters = parametersCaptor.getValue();
        assertEquals(2, usedParameters.size());
        assertEquals("technology.lineChangeoverNorms", usedParameters.get(L_WINDOW_ACTIVE_MENU));

        Map<String, Object> gridOptions = (Map<String, Object>) usedParameters.get(L_GRID_OPTIONS);
        assertEquals(1, gridOptions.size());

        Map<String, Object> filters = (Map<String, Object>) gridOptions.get(L_FILTERS);
        assertEquals(2, filters.size());
        assertEquals(L_TECHNOLOGY_GROUP_NUMBER, filters.get("fromTechnologyGroup"));
        assertEquals(L_TECHNOLOGY_GROUP_NUMBER, filters.get("toTechnologyGroup"));
    }

    @Test
    public void shouldNotRedirectToLineChangeoverNormsForTechnologyIfTechnologyNumbersAreEmpty() {
        // given
        given(previousOrderTechnologyNumberField.getFieldValue()).willReturn(null);
        given(technologyNumberField.getFieldValue()).willReturn(null);

        // when
        lineChangeoverNormsForOrderDetailsListeners.showLineChangeoverNormForTechnology(view, null, null);

        // then
        verify(view, never()).redirectTo(anyString(), anyBoolean(), anyBoolean(), anyMap());
    }

    @Test
    public void shouldRedirectToLineChangeoverNormsForTechnologyIfTechnologyNumbersAreNotEmpty() {
        // given
        given(previousOrderTechnologyNumberField.getFieldValue()).willReturn(L_TECHNOLOGY_NUMBER);
        given(technologyNumberField.getFieldValue()).willReturn(L_TECHNOLOGY_NUMBER);

        // when
        lineChangeoverNormsForOrderDetailsListeners.showLineChangeoverNormForTechnology(view, null, null);

        // then
        String url = "../page/lineChangeoverNorms/lineChangeoverNormsList.html";
        verify(view).redirectTo(eq(url), anyBoolean(), anyBoolean(), parametersCaptor.capture());
        Map<String, Object> usedParameters = parametersCaptor.getValue();
        assertEquals(2, usedParameters.size());
        assertEquals("technology.lineChangeoverNorms", usedParameters.get(L_WINDOW_ACTIVE_MENU));

        Map<String, Object> gridOptions = (Map<String, Object>) usedParameters.get(L_GRID_OPTIONS);
        assertEquals(1, gridOptions.size());

        Map<String, Object> filters = (Map<String, Object>) gridOptions.get(L_FILTERS);
        assertEquals(2, filters.size());
        assertEquals(L_TECHNOLOGY_GROUP_NUMBER, filters.get("fromTechnology"));
        assertEquals(L_TECHNOLOGY_GROUP_NUMBER, filters.get("toTechnology"));
    }

    @Test
    public void shouldNotAddMessageWhenOrdersAreNotSpecified() {
        // given
        stubLookup(previousOrderLookup, null);
        stubLookup(orderLookup, null);

        given(lineChangeoverNormsForOrdersService.previousOrderEndsBeforeOrIsWithdrawed(null, null)).willReturn(true);

        // when
        lineChangeoverNormsForOrderDetailsListeners.checkIfOrderHasCorrectStateAndIsPrevious(view, null, null);

        // then
        verify(previousOrderLookup, never()).addMessage(anyString(), eq(ComponentState.MessageType.FAILURE));
    }

    @Test
    public void shouldNotAddMessageWhenPreviousOrderIsCorrect() {
        // given
        stubLookup(previousOrderLookup, entityWithId);
        stubLookup(orderLookup, entityWithId);

        given(lineChangeoverNormsForOrdersService.previousOrderEndsBeforeOrIsWithdrawed(entityWithId, entityWithId)).willReturn(
                true);

        // when
        lineChangeoverNormsForOrderDetailsListeners.checkIfOrderHasCorrectStateAndIsPrevious(view, null, null);

        // then
        verify(previousOrderLookup, never()).addMessage(anyString(), eq(ComponentState.MessageType.FAILURE));
    }

    @Test
    public void shouldAddMessageWhenPreviousOrderIsIncorrect() {
        // given
        stubLookup(previousOrderLookup, entityWithId);
        stubLookup(orderLookup, entityWithId);

        given(lineChangeoverNormsForOrdersService.previousOrderEndsBeforeOrIsWithdrawed(entityWithId, entityWithId)).willReturn(
                false);

        // when
        lineChangeoverNormsForOrderDetailsListeners.checkIfOrderHasCorrectStateAndIsPrevious(view, null, null);

        // then
        verify(previousOrderLookup).addMessage(anyString(), eq(ComponentState.MessageType.FAILURE));
    }

    @Test
    public final void shouldFillPreviousOrderForm() {
        // when
        lineChangeoverNormsForOrderDetailsListeners.fillPreviousOrderForm(view, null, null);

        // then
        verify(lineChangeoverNormsForOrdersService).fillOrderForm(view, PREVIOUS_ORDER_FIELDS);
    }

    @Test
    public void shouldShowOwnLineChangeoverDurationField() {
        // when
        lineChangeoverNormsForOrderDetailsListeners.showOwnLineChangeoverDurationField(view, null, null);

        // then
        verify(orderService).changeFieldState(view, OWN_LINE_CHANGEOVER, OWN_LINE_CHANGEOVER_DURATION);
    }

}
