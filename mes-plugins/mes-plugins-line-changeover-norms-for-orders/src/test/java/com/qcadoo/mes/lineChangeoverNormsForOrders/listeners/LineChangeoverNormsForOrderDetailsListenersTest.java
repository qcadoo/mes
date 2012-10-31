/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.ORDER;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.OWN_LINE_CHANGEOVER;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.OWN_LINE_CHANGEOVER_DURATION;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.PREVIOUS_ORDER;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Maps;
import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

public class LineChangeoverNormsForOrderDetailsListenersTest {

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    private static final long L_ID = 1L;

    private static final long L_PREVIOUS_ORDER_ID = 1L;

    private static final long L_ORDER_ID = 2L;

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
    private FieldComponent previousOrderField, orderField, lineChangeoverNormField, previousOrderTechnologyGroupNumberField,
            technologyGroupNumberField, previousOrderTechnologyNumberField, technologyNumberField;

    @Mock
    private Entity previousOrderDB, orderDB;

    private Map<String, Object> parameters = Maps.newHashMap();

    private Map<String, String> filters = Maps.newHashMap();

    private Map<String, Object> gridOptions = Maps.newHashMap();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        lineChangeoverNormsForOrderDetailsListeners = new LineChangeoverNormsForOrderDetailsListeners();

        setField(lineChangeoverNormsForOrderDetailsListeners, "orderService", orderService);
        setField(lineChangeoverNormsForOrderDetailsListeners, "lineChangeoverNormsForOrdersService",
                lineChangeoverNormsForOrdersService);
    }

    @Test
    public void shouldntShowPreviousOrderIfPreviousOrderIdIsNull() {
        // given
        given(view.getComponentByReference(PREVIOUS_ORDER)).willReturn(previousOrderField);

        given(previousOrderField.getFieldValue()).willReturn(null);

        String url = "../page/orders/orderDetails.html";

        // when
        lineChangeoverNormsForOrderDetailsListeners.showPreviousOrder(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldShowPreviousOrderIfPreviousOrderIdIsntNull() {
        // given
        given(view.getComponentByReference(PREVIOUS_ORDER)).willReturn(previousOrderField);

        given(previousOrderField.getFieldValue()).willReturn(L_ID);

        parameters.put("form.id", L_ID);

        String url = "../page/orders/orderDetails.html";

        // when
        lineChangeoverNormsForOrderDetailsListeners.showPreviousOrder(view, null, null);

        // then
        verify(view).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldntShowBestFittingLineChangeoverNormIfLineChangeoverIdIsNull() {
        // given
        given(view.getComponentByReference(LINE_CHANGEOVER_NORM)).willReturn(lineChangeoverNormField);

        given(lineChangeoverNormField.getFieldValue()).willReturn(null);

        String url = "../page/lineChangeoverNorms/lineChangeoverNormsDetails.html";

        // when
        lineChangeoverNormsForOrderDetailsListeners.showBestFittingLineChangeoverNorm(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldshowBestFittingLineChangeoverNormIfLineChangeoverNormIdIsntNull() {
        // given
        given(view.getComponentByReference(LINE_CHANGEOVER_NORM)).willReturn(lineChangeoverNormField);

        given(lineChangeoverNormField.getFieldValue()).willReturn(L_ID);

        parameters.put("form.id", L_ID);

        String url = "../page/lineChangeoverNorms/lineChangeoverNormsDetails.html";

        // when
        lineChangeoverNormsForOrderDetailsListeners.showBestFittingLineChangeoverNorm(view, null, null);

        // then
        verify(view).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldntShowLineChangeoverNormForGroupIfTechnologyGroupNumbersAreEmpty() {
        // given
        given(view.getComponentByReference("previousOrderTechnologyGroupNumber")).willReturn(
                previousOrderTechnologyGroupNumberField);
        given(view.getComponentByReference("technologyGroupNumber")).willReturn(technologyGroupNumberField);

        given(previousOrderTechnologyGroupNumberField.getFieldValue()).willReturn(null);
        given(technologyGroupNumberField.getFieldValue()).willReturn(null);

        String url = "../page/lineChangeoverNorms/lineChangeoverNormsList.html";

        // when
        lineChangeoverNormsForOrderDetailsListeners.showLineChangeoverNormForGroup(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldShowLineChangeoverNormForGroupIfTechnologyGroupNumbersArentEmpty() {
        // given
        given(view.getComponentByReference("previousOrderTechnologyGroupNumber")).willReturn(
                previousOrderTechnologyGroupNumberField);
        given(view.getComponentByReference("technologyGroupNumber")).willReturn(technologyGroupNumberField);

        given(previousOrderTechnologyGroupNumberField.getFieldValue()).willReturn(L_TECHNOLOGY_GROUP_NUMBER);
        given(technologyGroupNumberField.getFieldValue()).willReturn(L_TECHNOLOGY_GROUP_NUMBER);

        filters.put("fromTechnologyGroup", L_TECHNOLOGY_GROUP_NUMBER);
        filters.put("toTechnologyGroup", L_TECHNOLOGY_GROUP_NUMBER);

        gridOptions.put(L_FILTERS, filters);

        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "technology.lineChangeoverNorms");

        String url = "../page/lineChangeoverNorms/lineChangeoverNormsList.html";

        // when
        lineChangeoverNormsForOrderDetailsListeners.showLineChangeoverNormForGroup(view, null, null);

        // then
        verify(view).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldntShowLineChangeoverNormForTechnologyIfTechnologyNumbersAreEmpty() {
        // given
        given(view.getComponentByReference("previousOrderTechnologyNumber")).willReturn(previousOrderTechnologyNumberField);
        given(view.getComponentByReference("technologyNumber")).willReturn(technologyNumberField);

        given(previousOrderTechnologyNumberField.getFieldValue()).willReturn(null);
        given(technologyNumberField.getFieldValue()).willReturn(null);

        String url = "../page/lineChangeoverNorms/lineChangeoverNormsList.html";

        // when
        lineChangeoverNormsForOrderDetailsListeners.showLineChangeoverNormForTechnology(view, null, null);

        // then
        verify(view, never()).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldShowLineChangeoverNormForTechnologyIfTechnologyNumbersArentEmpty() {
        // given
        given(view.getComponentByReference("previousOrderTechnologyNumber")).willReturn(previousOrderTechnologyNumberField);
        given(view.getComponentByReference("technologyNumber")).willReturn(technologyNumberField);

        given(previousOrderTechnologyNumberField.getFieldValue()).willReturn(L_TECHNOLOGY_NUMBER);
        given(technologyNumberField.getFieldValue()).willReturn(L_TECHNOLOGY_NUMBER);

        filters.put("fromTechnology", L_TECHNOLOGY_GROUP_NUMBER);
        filters.put("toTechnology", L_TECHNOLOGY_GROUP_NUMBER);

        gridOptions.put(L_FILTERS, filters);

        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "technology.lineChangeoverNorms");

        String url = "../page/lineChangeoverNorms/lineChangeoverNormsList.html";

        // when
        lineChangeoverNormsForOrderDetailsListeners.showLineChangeoverNormForTechnology(view, null, null);

        // then
        verify(view).redirectTo(url, false, true, parameters);
    }

    @Test
    public void shouldntAddMessageWhenCheckIfOrderHasCorrectStateAndIsPreviousIfOrdersAreNull() {
        // given
        given(view.getComponentByReference(PREVIOUS_ORDER)).willReturn(previousOrderField);
        given(view.getComponentByReference(ORDER)).willReturn(orderField);

        given(previousOrderField.getFieldValue()).willReturn(null);
        given(orderField.getFieldValue()).willReturn(null);

        // when
        lineChangeoverNormsForOrderDetailsListeners.checkIfOrderHasCorrectStateAndIsPrevious(view, null, null);

        // then
        verify(previousOrderField, never()).addMessage(Mockito.anyString(), Mockito.eq(ComponentState.MessageType.FAILURE));
    }

    @Test
    public void shouldntAddMessageWhenCheckIfOrderHasCorrectStateAndIsPreviousIfOrdersArentNullAndPreviousOrderIsntCorrectAndPrevious() {
        // given
        given(view.getComponentByReference(PREVIOUS_ORDER)).willReturn(previousOrderField);
        given(view.getComponentByReference(ORDER)).willReturn(orderField);

        given(previousOrderField.getFieldValue()).willReturn(L_PREVIOUS_ORDER_ID);
        given(orderField.getFieldValue()).willReturn(L_ORDER_ID);

        given(lineChangeoverNormsForOrdersService.getOrderFromDB(L_PREVIOUS_ORDER_ID)).willReturn(previousOrderDB);
        given(lineChangeoverNormsForOrdersService.getOrderFromDB(L_ORDER_ID)).willReturn(orderDB);

        given(lineChangeoverNormsForOrdersService.checkIfOrderHasCorrectStateAndIsPrevious(previousOrderDB, orderDB)).willReturn(
                true);

        // when
        lineChangeoverNormsForOrderDetailsListeners.checkIfOrderHasCorrectStateAndIsPrevious(view, null, null);

        // then
        verify(previousOrderField, never()).addMessage(Mockito.anyString(), Mockito.eq(ComponentState.MessageType.FAILURE));
    }

    @Test
    public void shouldAddMessageWhenCheckIfOrderHasCorrectStateAndIsPreviousIfOrdersArentNullAndPreviousOrderIsCorrectAndPrevious() {
        // given
        given(view.getComponentByReference(PREVIOUS_ORDER)).willReturn(previousOrderField);
        given(view.getComponentByReference(ORDER)).willReturn(orderField);

        given(previousOrderField.getFieldValue()).willReturn(L_PREVIOUS_ORDER_ID);
        given(orderField.getFieldValue()).willReturn(L_ORDER_ID);

        given(lineChangeoverNormsForOrdersService.getOrderFromDB(L_PREVIOUS_ORDER_ID)).willReturn(previousOrderDB);
        given(lineChangeoverNormsForOrdersService.getOrderFromDB(L_ORDER_ID)).willReturn(orderDB);

        given(lineChangeoverNormsForOrdersService.checkIfOrderHasCorrectStateAndIsPrevious(previousOrderDB, orderDB)).willReturn(
                false);

        // when
        lineChangeoverNormsForOrderDetailsListeners.checkIfOrderHasCorrectStateAndIsPrevious(view, null, null);

        // then
        verify(previousOrderField).addMessage(Mockito.anyString(), Mockito.eq(ComponentState.MessageType.FAILURE));
    }

    @Test
    public final void shouldFillPreviousOrderForm() {
        // given

        // when
        lineChangeoverNormsForOrderDetailsListeners.fillPreviousOrderForm(view, null, null);

        // then
        verify(lineChangeoverNormsForOrdersService).fillOrderForm(view, PREVIOUS_ORDER_FIELDS);
    }

    @Test
    public void shouldShowOwnLineChangeoverDurationField() {
        // given

        // when
        lineChangeoverNormsForOrderDetailsListeners.showOwnLineChangeoverDurationField(view, null, null);

        // then
        verify(orderService).changeFieldState(view, OWN_LINE_CHANGEOVER, OWN_LINE_CHANGEOVER_DURATION);
    }

}
