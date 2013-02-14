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
package com.qcadoo.mes.lineChangeoverNormsForOrders.hooks;

import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.LineChangeoverNormsForOrdersConstants.ORDER_FIELDS;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.LineChangeoverNormsForOrdersConstants.PREVIOUS_ORDER_FIELDS;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.LINE_CHANGEOVER_NORM;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.ORDER;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.OWN_LINE_CHANGEOVER;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.OWN_LINE_CHANGEOVER_DURATION;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.OrderFieldsLCNFO.PREVIOUS_ORDER;
import static com.qcadoo.mes.orders.constants.OrderFields.PRODUCTION_LINE;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.lineChangeoverNorms.ChangeoverNormsSearchService;
import com.qcadoo.mes.lineChangeoverNorms.ChangeoverNormsService;
import com.qcadoo.mes.lineChangeoverNormsForOrders.LineChangeoverNormsForOrdersService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.internal.components.window.WindowComponentState;

public class LineChangeoverNormsForOrderDetailsViewHooksTest {

    private LineChangeoverNormsForOrderDetailsViewHooks lineChangeoverNormsForOrderDetailsViewHooks;

    private static final String L_FORM = "form";

    private static final long L_ID = 1L;

    private static final String L_PREVIOUS_ORDER = "000001";

    private static final String L_LINE_CHANGEOVER_NORM = "000001";

    private static final String L_PREVIOUS_ORDER_TECHNOLOGY_GROUP_NUMBER = "000001";

    private static final String L_TECHNOLOGY_GROUP_NUMBER = "000002";

    private static final String L_PREVIOUS_ORDER_TECHNOLOGY_NUMBER = "000001";

    private static final String L_TECHNOLOGY_NUMBER = "000002";

    private static final long L_PREVIOUS_ORDER_ID = 1L;

    private static final long L_ORDER_ID = 2L;

    private static final long L_FROM_TECHNOLOGY_ID = 1L;

    private static final long L_TO_TECHNOLOGY_ID = 2L;

    @Mock
    private OrderService orderService;

    @Mock
    private ChangeoverNormsService changeoverNormsService;

    @Mock
    private ChangeoverNormsSearchService changeoverNormsSearchService;

    @Mock
    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent orderForm;

    @Mock
    private FieldComponent previousOrderField, orderField, productionLineField, lineChangeoverNormField,
            lineChangeoverNormDurationField, previousOrderTechnologyGroupNumberField, technologyGroupNumberField,
            previousOrderTechnologyNumberField, technologyNumberField;

    @Mock
    private Entity previousOrder, order, fromTechnologyGroup, toTechnologyGroup, fromTechnology, toTechnology, productionLine,
            lineChangeoverNorm, lineChangeoverNormForGroup, lineChangeoverNormForTechnology;

    @Mock
    private WindowComponentState window;

    @Mock
    private Ribbon ribbon;

    @Mock
    private RibbonGroup orders, lineChangeoverNorms;

    @Mock
    private RibbonActionItem showPreviousOrder, showBestFittingLineChangeoverNorm, showLineChangeoverNormForGroup,
            showLineChangeoverNormForTechnology;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        lineChangeoverNormsForOrderDetailsViewHooks = new LineChangeoverNormsForOrderDetailsViewHooks();

        setField(lineChangeoverNormsForOrderDetailsViewHooks, "orderService", orderService);
        setField(lineChangeoverNormsForOrderDetailsViewHooks, "changeoverNormsService", changeoverNormsService);
        setField(lineChangeoverNormsForOrderDetailsViewHooks, "changeoverNormsSearchService", changeoverNormsSearchService);
        setField(lineChangeoverNormsForOrderDetailsViewHooks, "lineChangeoverNormsForOrdersService",
                lineChangeoverNormsForOrdersService);
    }

    @Test
    public void shouldntFillOrderFormsIfOrderFormEntityIdIsNull() {
        // given
        given(view.getComponentByReference(L_FORM)).willReturn(orderForm);

        given(view.getComponentByReference(PREVIOUS_ORDER)).willReturn(previousOrderField);
        given(view.getComponentByReference(ORDER)).willReturn(orderField);

        given(orderForm.getEntityId()).willReturn(null);

        // when
        lineChangeoverNormsForOrderDetailsViewHooks.fillOrderForms(view);

        // then
        verify(orderField, never()).setFieldValue(Mockito.any());
        verify(previousOrderField, never()).setFieldValue(Mockito.any());

        verify(lineChangeoverNormsForOrdersService, never()).fillOrderForm(view, ORDER_FIELDS);
        verify(lineChangeoverNormsForOrdersService, never()).fillOrderForm(view, PREVIOUS_ORDER_FIELDS);
    }

    @Test
    public void shouldntFillOrderFormsIfOrderFormEntityIdIsntNullAndOrderIsNull() {
        // given
        given(view.getComponentByReference(L_FORM)).willReturn(orderForm);

        given(view.getComponentByReference(PREVIOUS_ORDER)).willReturn(previousOrderField);
        given(view.getComponentByReference(ORDER)).willReturn(orderField);

        given(orderForm.getEntityId()).willReturn(L_ID);

        given(lineChangeoverNormsForOrdersService.getOrderFromDB(L_ID)).willReturn(null);

        // when
        lineChangeoverNormsForOrderDetailsViewHooks.fillOrderForms(view);

        // then
        verify(orderField, never()).setFieldValue(Mockito.any());
        verify(previousOrderField, never()).setFieldValue(Mockito.any());

        verify(lineChangeoverNormsForOrdersService, never()).fillOrderForm(view, ORDER_FIELDS);
        verify(lineChangeoverNormsForOrdersService, never()).fillOrderForm(view, PREVIOUS_ORDER_FIELDS);
    }

    @Test
    public void shouldFillOrderFormsIfOrderFormEntityIdIsntNullAndOrderIsntNullAndPreviousOrderIdIsntNull() {
        // given
        given(view.getComponentByReference(L_FORM)).willReturn(orderForm);

        given(view.getComponentByReference(PREVIOUS_ORDER)).willReturn(previousOrderField);
        given(view.getComponentByReference(ORDER)).willReturn(orderField);

        given(orderForm.getEntityId()).willReturn(L_ID);

        given(lineChangeoverNormsForOrdersService.getOrderFromDB(L_ID)).willReturn(order);

        given(order.getId()).willReturn(L_ORDER_ID);

        given(previousOrderField.getFieldValue()).willReturn(L_PREVIOUS_ORDER_ID);

        // when
        lineChangeoverNormsForOrderDetailsViewHooks.fillOrderForms(view);

        // then
        verify(orderField).setFieldValue(Mockito.any());
        verify(previousOrderField, never()).setFieldValue(Mockito.any());

        verify(lineChangeoverNormsForOrdersService).fillOrderForm(view, ORDER_FIELDS);
        verify(lineChangeoverNormsForOrdersService, never()).fillOrderForm(view, PREVIOUS_ORDER_FIELDS);
    }

    @Test
    public void shouldFillOrderFormsIfOrderFormEntityIdIsntNullAndOrderIsntNullAndPreviousOrderIsNull() {
        // given
        given(view.getComponentByReference(L_FORM)).willReturn(orderForm);

        given(view.getComponentByReference(PREVIOUS_ORDER)).willReturn(previousOrderField);
        given(view.getComponentByReference(ORDER)).willReturn(orderField);

        given(orderForm.getEntityId()).willReturn(L_ID);

        given(lineChangeoverNormsForOrdersService.getOrderFromDB(L_ID)).willReturn(order);

        given(order.getId()).willReturn(L_ORDER_ID);

        given(previousOrderField.getFieldValue()).willReturn(null);

        given(lineChangeoverNormsForOrdersService.getPreviousOrderFromDB(order)).willReturn(null);

        // when
        lineChangeoverNormsForOrderDetailsViewHooks.fillOrderForms(view);

        // then
        verify(orderField).setFieldValue(Mockito.any());
        verify(previousOrderField, never()).setFieldValue(Mockito.any());

        verify(lineChangeoverNormsForOrdersService).fillOrderForm(view, ORDER_FIELDS);
        verify(lineChangeoverNormsForOrdersService, never()).fillOrderForm(view, PREVIOUS_ORDER_FIELDS);
    }

    @Test
    public void shouldFillOrderFormsIfOrderFormEntityIdIsntNullAndOrderIsntNullAndPreviousOrderIsntNull() {
        // given
        given(view.getComponentByReference(L_FORM)).willReturn(orderForm);

        given(view.getComponentByReference(PREVIOUS_ORDER)).willReturn(previousOrderField);
        given(view.getComponentByReference(ORDER)).willReturn(orderField);

        given(orderForm.getEntityId()).willReturn(L_ID);

        given(lineChangeoverNormsForOrdersService.getOrderFromDB(L_ID)).willReturn(order);

        given(order.getId()).willReturn(L_ORDER_ID);

        given(previousOrderField.getFieldValue()).willReturn(null);

        given(lineChangeoverNormsForOrdersService.getPreviousOrderFromDB(order)).willReturn(previousOrder);

        // when
        lineChangeoverNormsForOrderDetailsViewHooks.fillOrderForms(view);

        // then
        verify(orderField).setFieldValue(Mockito.any());
        verify(previousOrderField).setFieldValue(Mockito.any());

        verify(lineChangeoverNormsForOrdersService).fillOrderForm(view, ORDER_FIELDS);
        verify(lineChangeoverNormsForOrdersService).fillOrderForm(view, PREVIOUS_ORDER_FIELDS);
    }

    @Test
    public void shouldntFillLineChangeoverNormIfOrderFieldsAreNull() {
        // given
        given(view.getComponentByReference(PREVIOUS_ORDER)).willReturn(previousOrderField);
        given(view.getComponentByReference(ORDER)).willReturn(orderField);

        given(view.getComponentByReference("lineChangeoverNorm")).willReturn(lineChangeoverNormField);
        given(view.getComponentByReference("lineChangeoverNormDuration")).willReturn(lineChangeoverNormDurationField);

        given(previousOrderField.getFieldValue()).willReturn(null);
        given(orderField.getFieldValue()).willReturn(null);

        // when
        lineChangeoverNormsForOrderDetailsViewHooks.fillLineChangeoverNorm(view);

        // then
        verify(lineChangeoverNormField, never()).setFieldValue(Mockito.any());
        verify(lineChangeoverNormDurationField, never()).setFieldValue(Mockito.any());
    }

    @Test
    public void shouldntFillLineChangeoverNormIfOrderFieldsAreNullAndOrdersAreNull() {
        // given
        given(view.getComponentByReference(PREVIOUS_ORDER)).willReturn(previousOrderField);
        given(view.getComponentByReference(ORDER)).willReturn(orderField);

        given(view.getComponentByReference("lineChangeoverNorm")).willReturn(lineChangeoverNormField);
        given(view.getComponentByReference("lineChangeoverNormDuration")).willReturn(lineChangeoverNormDurationField);

        given(previousOrderField.getFieldValue()).willReturn(L_PREVIOUS_ORDER_ID);
        given(orderField.getFieldValue()).willReturn(L_ORDER_ID);

        given(lineChangeoverNormsForOrdersService.getOrderFromDB(L_PREVIOUS_ORDER_ID)).willReturn(null);
        given(lineChangeoverNormsForOrdersService.getOrderFromDB(L_ORDER_ID)).willReturn(null);

        // when
        lineChangeoverNormsForOrderDetailsViewHooks.fillLineChangeoverNorm(view);

        // then
        verify(lineChangeoverNormField, never()).setFieldValue(Mockito.any());
        verify(lineChangeoverNormDurationField, never()).setFieldValue(Mockito.any());
    }

    @Test
    public void shouldntFillLineChangeoverNormIfOrderFieldsAreNullAndOrdersArentNullAndTechnologiesAreNull() {
        // given
        given(view.getComponentByReference(PREVIOUS_ORDER)).willReturn(previousOrderField);
        given(view.getComponentByReference(ORDER)).willReturn(orderField);

        given(view.getComponentByReference("lineChangeoverNorm")).willReturn(lineChangeoverNormField);
        given(view.getComponentByReference("lineChangeoverNormDuration")).willReturn(lineChangeoverNormDurationField);

        given(previousOrderField.getFieldValue()).willReturn(L_PREVIOUS_ORDER_ID);
        given(orderField.getFieldValue()).willReturn(L_ORDER_ID);

        given(lineChangeoverNormsForOrdersService.getOrderFromDB(L_PREVIOUS_ORDER_ID)).willReturn(previousOrder);
        given(lineChangeoverNormsForOrdersService.getOrderFromDB(L_ORDER_ID)).willReturn(order);

        given(previousOrder.getBelongsToField(TECHNOLOGY)).willReturn(null);
        given(order.getBelongsToField(TECHNOLOGY)).willReturn(null);

        // when
        lineChangeoverNormsForOrderDetailsViewHooks.fillLineChangeoverNorm(view);

        // then
        verify(lineChangeoverNormField, never()).setFieldValue(Mockito.any());
        verify(lineChangeoverNormDurationField, never()).setFieldValue(Mockito.any());
    }

    @Test
    public void shouldntFillLineChangeoverNormIfOrderFieldsAreNullAndOrdersArentNullAndTechnologiesArentNullAndLineChangeoverNormIsNull() {
        // given
        given(view.getComponentByReference(PREVIOUS_ORDER)).willReturn(previousOrderField);
        given(view.getComponentByReference(ORDER)).willReturn(orderField);

        given(view.getComponentByReference("lineChangeoverNorm")).willReturn(lineChangeoverNormField);
        given(view.getComponentByReference("lineChangeoverNormDuration")).willReturn(lineChangeoverNormDurationField);

        given(previousOrderField.getFieldValue()).willReturn(L_PREVIOUS_ORDER_ID);
        given(orderField.getFieldValue()).willReturn(L_ORDER_ID);

        given(lineChangeoverNormsForOrdersService.getOrderFromDB(L_PREVIOUS_ORDER_ID)).willReturn(previousOrder);
        given(lineChangeoverNormsForOrdersService.getOrderFromDB(L_ORDER_ID)).willReturn(order);

        given(previousOrder.getBelongsToField(TECHNOLOGY)).willReturn(fromTechnology);
        given(order.getBelongsToField(TECHNOLOGY)).willReturn(toTechnology);

        given(fromTechnology.getId()).willReturn(L_FROM_TECHNOLOGY_ID);
        given(toTechnology.getId()).willReturn(L_TO_TECHNOLOGY_ID);

        given(lineChangeoverNormsForOrdersService.getTechnologyFromDB(L_FROM_TECHNOLOGY_ID)).willReturn(fromTechnology);
        given(lineChangeoverNormsForOrdersService.getTechnologyFromDB(L_TO_TECHNOLOGY_ID)).willReturn(toTechnology);

        given(order.getBelongsToField(PRODUCTION_LINE)).willReturn(productionLine);

        given(changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology, productionLine)).willReturn(null);

        // when
        lineChangeoverNormsForOrderDetailsViewHooks.fillLineChangeoverNorm(view);

        // then
        verify(lineChangeoverNormField).setFieldValue(null);
        verify(lineChangeoverNormDurationField).setFieldValue(null);
    }

    @Test
    public void shouldFillLineChangeoverNormIfOrderFieldsAreNullAndOrdersArentNullAndTechnologiesArentNullAndLineChangeoverNormIsntNull() {
        // given
        given(view.getComponentByReference(PREVIOUS_ORDER)).willReturn(previousOrderField);
        given(view.getComponentByReference(ORDER)).willReturn(orderField);

        given(view.getComponentByReference("lineChangeoverNorm")).willReturn(lineChangeoverNormField);
        given(view.getComponentByReference("lineChangeoverNormDuration")).willReturn(lineChangeoverNormDurationField);

        given(previousOrderField.getFieldValue()).willReturn(L_PREVIOUS_ORDER_ID);
        given(orderField.getFieldValue()).willReturn(L_ORDER_ID);

        given(lineChangeoverNormsForOrdersService.getOrderFromDB(L_PREVIOUS_ORDER_ID)).willReturn(previousOrder);
        given(lineChangeoverNormsForOrdersService.getOrderFromDB(L_ORDER_ID)).willReturn(order);

        given(previousOrder.getBelongsToField(TECHNOLOGY)).willReturn(fromTechnology);
        given(order.getBelongsToField(TECHNOLOGY)).willReturn(toTechnology);

        given(fromTechnology.getId()).willReturn(L_FROM_TECHNOLOGY_ID);
        given(toTechnology.getId()).willReturn(L_TO_TECHNOLOGY_ID);

        given(lineChangeoverNormsForOrdersService.getTechnologyFromDB(L_FROM_TECHNOLOGY_ID)).willReturn(fromTechnology);
        given(lineChangeoverNormsForOrdersService.getTechnologyFromDB(L_TO_TECHNOLOGY_ID)).willReturn(toTechnology);

        given(order.getBelongsToField(PRODUCTION_LINE)).willReturn(productionLine);

        given(changeoverNormsService.getMatchingChangeoverNorms(fromTechnology, toTechnology, productionLine)).willReturn(
                lineChangeoverNorm);

        // when
        lineChangeoverNormsForOrderDetailsViewHooks.fillLineChangeoverNorm(view);

        // then
        verify(lineChangeoverNormField).setFieldValue(Mockito.any());
        verify(lineChangeoverNormDurationField).setFieldValue(Mockito.any());
    }

    @Test
    public void shouldntUpdateRibbonState() {
        // given
        given(view.getComponentByReference(PRODUCTION_LINE)).willReturn(productionLineField);

        given(view.getComponentByReference(PREVIOUS_ORDER)).willReturn(previousOrderField);

        given(view.getComponentByReference(LINE_CHANGEOVER_NORM)).willReturn(lineChangeoverNormField);

        given(view.getComponentByReference("previousOrderTechnologyGroupNumber")).willReturn(
                previousOrderTechnologyGroupNumberField);
        given(view.getComponentByReference("technologyGroupNumber")).willReturn(technologyGroupNumberField);

        given(view.getComponentByReference("previousOrderTechnologyNumber")).willReturn(previousOrderTechnologyNumberField);
        given(view.getComponentByReference("technologyNumber")).willReturn(technologyNumberField);

        given(view.getComponentByReference("window")).willReturn((ComponentState) window);

        given(window.getRibbon()).willReturn(ribbon);

        given(ribbon.getGroupByName("orders")).willReturn(orders);
        given(ribbon.getGroupByName("lineChangeoverNorms")).willReturn(lineChangeoverNorms);

        given(orders.getItemByName("showPreviousOrder")).willReturn(showPreviousOrder);

        given(lineChangeoverNorms.getItemByName("showBestFittingLineChangeoverNorm")).willReturn(
                showBestFittingLineChangeoverNorm);
        given(lineChangeoverNorms.getItemByName("showLineChangeoverNormForGroup")).willReturn(showLineChangeoverNormForGroup);
        given(lineChangeoverNorms.getItemByName("showLineChangeoverNormForTechnology")).willReturn(
                showLineChangeoverNormForTechnology);

        given(productionLineField.getFieldValue()).willReturn(L_ID);
        given(lineChangeoverNormsForOrdersService.getProductionLineFromDB(L_ID)).willReturn(productionLine);

        given(previousOrderField.getFieldValue()).willReturn(null);
        given(lineChangeoverNormField.getFieldValue()).willReturn(null);
        given(previousOrderTechnologyGroupNumberField.getFieldValue()).willReturn(null);
        given(technologyGroupNumberField.getFieldValue()).willReturn(null);
        given(previousOrderTechnologyNumberField.getFieldValue()).willReturn(null);
        given(technologyNumberField.getFieldValue()).willReturn(null);

        // when
        lineChangeoverNormsForOrderDetailsViewHooks.updateRibbonState(view);

        // then
        verify(showPreviousOrder).setEnabled(false);
        verify(showBestFittingLineChangeoverNorm).setEnabled(false);
        verify(showLineChangeoverNormForGroup).setEnabled(false);
        verify(showLineChangeoverNormForTechnology).setEnabled(false);
    }

    @Test
    public void shouldntUpdateRibbonStateIfSearchLineChangeoverNormIsNull() {
        // given
        given(view.getComponentByReference(PRODUCTION_LINE)).willReturn(productionLineField);

        given(view.getComponentByReference(PREVIOUS_ORDER)).willReturn(previousOrderField);

        given(view.getComponentByReference(LINE_CHANGEOVER_NORM)).willReturn(lineChangeoverNormField);

        given(view.getComponentByReference("previousOrderTechnologyGroupNumber")).willReturn(
                previousOrderTechnologyGroupNumberField);
        given(view.getComponentByReference("technologyGroupNumber")).willReturn(technologyGroupNumberField);

        given(view.getComponentByReference("previousOrderTechnologyNumber")).willReturn(previousOrderTechnologyNumberField);
        given(view.getComponentByReference("technologyNumber")).willReturn(technologyNumberField);

        given(view.getComponentByReference("window")).willReturn((ComponentState) window);

        given(window.getRibbon()).willReturn(ribbon);

        given(ribbon.getGroupByName("orders")).willReturn(orders);
        given(ribbon.getGroupByName("lineChangeoverNorms")).willReturn(lineChangeoverNorms);

        given(orders.getItemByName("showPreviousOrder")).willReturn(showPreviousOrder);

        given(lineChangeoverNorms.getItemByName("showBestFittingLineChangeoverNorm")).willReturn(
                showBestFittingLineChangeoverNorm);
        given(lineChangeoverNorms.getItemByName("showLineChangeoverNormForGroup")).willReturn(showLineChangeoverNormForGroup);
        given(lineChangeoverNorms.getItemByName("showLineChangeoverNormForTechnology")).willReturn(
                showLineChangeoverNormForTechnology);

        given(productionLineField.getFieldValue()).willReturn(L_ID);
        given(lineChangeoverNormsForOrdersService.getProductionLineFromDB(L_ID)).willReturn(null);

        given(previousOrderField.getFieldValue()).willReturn(null);
        given(lineChangeoverNormField.getFieldValue()).willReturn(null);
        given(previousOrderTechnologyGroupNumberField.getFieldValue()).willReturn(L_PREVIOUS_ORDER_TECHNOLOGY_GROUP_NUMBER);
        given(technologyGroupNumberField.getFieldValue()).willReturn(L_TECHNOLOGY_GROUP_NUMBER);
        given(previousOrderTechnologyNumberField.getFieldValue()).willReturn(L_PREVIOUS_ORDER_TECHNOLOGY_NUMBER);
        given(technologyNumberField.getFieldValue()).willReturn(L_TECHNOLOGY_NUMBER);

        given(lineChangeoverNormsForOrdersService.getTechnologyGroupByNumberFromDB(L_PREVIOUS_ORDER_TECHNOLOGY_GROUP_NUMBER))
                .willReturn(null);
        given(lineChangeoverNormsForOrdersService.getTechnologyGroupByNumberFromDB(L_TECHNOLOGY_GROUP_NUMBER)).willReturn(null);

        given(lineChangeoverNormsForOrdersService.getTechnologyByNumberFromDB(L_PREVIOUS_ORDER_TECHNOLOGY_NUMBER)).willReturn(
                null);
        given(lineChangeoverNormsForOrdersService.getTechnologyByNumberFromDB(L_TECHNOLOGY_NUMBER)).willReturn(null);

        given(changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyGroupWithLine(null, null, null))
                .willReturn(null);

        given(changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyWithLine(null, null, null))
                .willReturn(null);

        // when
        lineChangeoverNormsForOrderDetailsViewHooks.updateRibbonState(view);

        // then
        verify(showPreviousOrder).setEnabled(false);
        verify(showBestFittingLineChangeoverNorm).setEnabled(false);
        verify(showLineChangeoverNormForGroup).setEnabled(false);
        verify(showLineChangeoverNormForTechnology).setEnabled(false);
    }

    @Test
    public void shouldUpdateRibbonState() {
        // given
        given(view.getComponentByReference(PRODUCTION_LINE)).willReturn(productionLineField);

        given(view.getComponentByReference(PREVIOUS_ORDER)).willReturn(previousOrderField);

        given(view.getComponentByReference(LINE_CHANGEOVER_NORM)).willReturn(lineChangeoverNormField);

        given(view.getComponentByReference("previousOrderTechnologyGroupNumber")).willReturn(
                previousOrderTechnologyGroupNumberField);
        given(view.getComponentByReference("technologyGroupNumber")).willReturn(technologyGroupNumberField);

        given(view.getComponentByReference("previousOrderTechnologyNumber")).willReturn(previousOrderTechnologyNumberField);
        given(view.getComponentByReference("technologyNumber")).willReturn(technologyNumberField);

        given(view.getComponentByReference("window")).willReturn((ComponentState) window);

        given(window.getRibbon()).willReturn(ribbon);

        given(ribbon.getGroupByName("orders")).willReturn(orders);
        given(ribbon.getGroupByName("lineChangeoverNorms")).willReturn(lineChangeoverNorms);

        given(orders.getItemByName("showPreviousOrder")).willReturn(showPreviousOrder);

        given(lineChangeoverNorms.getItemByName("showBestFittingLineChangeoverNorm")).willReturn(
                showBestFittingLineChangeoverNorm);
        given(lineChangeoverNorms.getItemByName("showLineChangeoverNormForGroup")).willReturn(showLineChangeoverNormForGroup);
        given(lineChangeoverNorms.getItemByName("showLineChangeoverNormForTechnology")).willReturn(
                showLineChangeoverNormForTechnology);

        given(productionLineField.getFieldValue()).willReturn(L_ID);
        given(lineChangeoverNormsForOrdersService.getProductionLineFromDB(L_ID)).willReturn(productionLine);

        given(previousOrderField.getFieldValue()).willReturn(L_PREVIOUS_ORDER);
        given(lineChangeoverNormField.getFieldValue()).willReturn(L_LINE_CHANGEOVER_NORM);
        given(previousOrderTechnologyGroupNumberField.getFieldValue()).willReturn(L_PREVIOUS_ORDER_TECHNOLOGY_GROUP_NUMBER);
        given(technologyGroupNumberField.getFieldValue()).willReturn(L_TECHNOLOGY_GROUP_NUMBER);
        given(previousOrderTechnologyNumberField.getFieldValue()).willReturn(L_PREVIOUS_ORDER_TECHNOLOGY_NUMBER);
        given(technologyNumberField.getFieldValue()).willReturn(L_TECHNOLOGY_NUMBER);

        given(lineChangeoverNormsForOrdersService.getTechnologyGroupByNumberFromDB(L_PREVIOUS_ORDER_TECHNOLOGY_GROUP_NUMBER))
                .willReturn(fromTechnologyGroup);
        given(lineChangeoverNormsForOrdersService.getTechnologyGroupByNumberFromDB(L_TECHNOLOGY_GROUP_NUMBER)).willReturn(
                toTechnologyGroup);

        given(lineChangeoverNormsForOrdersService.getTechnologyByNumberFromDB(L_PREVIOUS_ORDER_TECHNOLOGY_NUMBER)).willReturn(
                fromTechnology);
        given(lineChangeoverNormsForOrdersService.getTechnologyByNumberFromDB(L_TECHNOLOGY_NUMBER)).willReturn(toTechnology);

        given(
                changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyGroupWithLine(fromTechnologyGroup,
                        toTechnologyGroup, productionLine)).willReturn(lineChangeoverNormForGroup);

        given(
                changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyWithLine(fromTechnology, toTechnology,
                        productionLine)).willReturn(lineChangeoverNormForTechnology);

        // when
        lineChangeoverNormsForOrderDetailsViewHooks.updateRibbonState(view);

        // then
        verify(showPreviousOrder).setEnabled(true);
        verify(showBestFittingLineChangeoverNorm).setEnabled(true);
        verify(showLineChangeoverNormForGroup).setEnabled(true);
        verify(showLineChangeoverNormForTechnology).setEnabled(true);
    }

    @Test
    public void shouldShowOwnLineChangeoverDurationField() {
        // given

        // when
        lineChangeoverNormsForOrderDetailsViewHooks.showOwnLineChangeoverDurationField(view);

        // then
        verify(orderService).changeFieldState(view, OWN_LINE_CHANGEOVER, OWN_LINE_CHANGEOVER_DURATION);
    }

}
