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
package com.qcadoo.mes.orders.hooks;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.ExpressionService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class OrderDetailsHooksTest {

    private static final String L_FORM = "form";

    private static final Long L_ID = 1L;

    private OrderDetailsHooks orderDetailsHooks;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private NumberGeneratorService numberGeneratorService;

    @Mock
    private ExpressionService expressionService;

    @Mock
    private ParameterService parameterService;

    @Mock
    private TechnologyServiceO technologyServiceO;

    @Mock
    private OrderService orderService;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FormComponent orderForm;

    @Mock
    private LookupComponent productLookup, technologyLookup, productionLineLookup;

    @Mock
    private FieldComponent defaultTechnologyField, plannedQuantityField, stateField, correctDateFromField, correctDateToField,
            commentDateFromField, commentDateToField, dateFromField, dateToField, effectiveDateFromField;

    @Mock
    private AwesomeDynamicListComponent reasonsDateFromField, reasonsDateToField;

    @Mock
    private DataDefinition orderDD, technologyDD;

    @Mock
    private Entity order, product, defaultTechnology, defaultProductionLine;

    @Mock
    private SearchCriteriaBuilder searchCriteriaBuilder;

    @Mock
    private SearchResult searchResult;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        orderDetailsHooks = new OrderDetailsHooks();

        setField(orderDetailsHooks, "dataDefinitionService", dataDefinitionService);
        setField(orderDetailsHooks, "numberGeneratorService", numberGeneratorService);
        setField(orderDetailsHooks, "expressionService", expressionService);
        setField(orderDetailsHooks, "parameterService", parameterService);
        setField(orderDetailsHooks, "technologyServiceO", technologyServiceO);
        setField(orderDetailsHooks, "orderService", orderService);

        PowerMockito.mockStatic(SearchRestrictions.class);

        given(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)).willReturn(orderDD);
        given(orderDD.get(L_ID)).willReturn(order);

        given(view.getComponentByReference(L_FORM)).willReturn(orderForm);
        given(orderForm.getEntityId()).willReturn(L_ID);

        given(view.getComponentByReference(OrderFields.STATE)).willReturn(stateField);

        given(view.getComponentByReference(OrderFields.CORRECTED_DATE_FROM)).willReturn(correctDateFromField);
        given(view.getComponentByReference(OrderFields.CORRECTED_DATE_TO)).willReturn(correctDateToField);
        given(view.getComponentByReference(OrderFields.REASON_TYPES_CORRECTION_DATE_FROM)).willReturn(reasonsDateFromField);
        given(view.getComponentByReference(OrderFields.REASON_TYPES_CORRECTION_DATE_TO)).willReturn(reasonsDateToField);
        given(view.getComponentByReference(OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_TO)).willReturn(commentDateToField);
        given(view.getComponentByReference(OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_FROM))
                .willReturn(commentDateFromField);
        given(view.getComponentByReference(OrderFields.DATE_FROM)).willReturn(dateFromField);
        given(view.getComponentByReference(OrderFields.DATE_TO)).willReturn(dateToField);
        given(view.getComponentByReference(OrderFields.EFFECTIVE_DATE_FROM)).willReturn(effectiveDateFromField);
    }

    @Test
    public void shouldntFillProductionLineIfFormIsSaved() {
        // given
        given(view.getComponentByReference(L_FORM)).willReturn(orderForm);
        given(orderForm.getEntityId()).willReturn(L_ID);

        given(view.getComponentByReference(OrderFields.PRODUCTION_LINE)).willReturn(productionLineLookup);

        given(orderService.getDefaultProductionLine()).willReturn(defaultProductionLine);

        given(defaultProductionLine.getId()).willReturn(L_ID);

        // when
        orderDetailsHooks.fillProductionLine(view);

        // then
        verify(productionLineLookup, never()).setFieldValue(L_ID);
    }

    @Test
    public void shouldFillProductionLineIfFormIsntSaved() {
        // given
        given(view.getComponentByReference(L_FORM)).willReturn(orderForm);
        given(orderForm.getEntityId()).willReturn(null);

        given(view.getComponentByReference(OrderFields.PRODUCTION_LINE)).willReturn(productionLineLookup);

        given(orderService.getDefaultProductionLine()).willReturn(defaultProductionLine);

        given(defaultProductionLine.getId()).willReturn(L_ID);

        // when
        orderDetailsHooks.fillProductionLine(view);

        // then
        verify(productionLineLookup).setFieldValue(L_ID);
    }

    @Test
    public void shouldSetAndDisableState() throws Exception {
        // given
        given(view.getComponentByReference(L_FORM)).willReturn(orderForm);
        given(orderForm.getEntityId()).willReturn(null);

        // when
        orderDetailsHooks.setAndDisableState(view);

        // then
        verify(stateField).setEnabled(false);
        verify(stateField).setFieldValue(OrderState.PENDING.getStringValue());
    }

    @Test
    public void shouldDisableState() throws Exception {
        // given
        given(view.getComponentByReference(L_FORM)).willReturn(orderForm);
        given(orderForm.getEntityId()).willReturn(L_ID);

        // when
        orderDetailsHooks.setAndDisableState(view);

        // then
        verify(stateField).setEnabled(false);
        verify(stateField, never()).setFieldValue(OrderState.PENDING.getStringValue());
    }

    @Test
    public void shouldGenerateOrderNumber() throws Exception {
        // given

        // when
        orderDetailsHooks.generateOrderNumber(view);

        // then
        verify(numberGeneratorService).generateAndInsertNumber(view, OrdersConstants.PLUGIN_IDENTIFIER,
                OrdersConstants.MODEL_ORDER, L_FORM, OrderFields.NUMBER);
    }

    @Test
    public void shouldNotFillDefaultTechnologyIfThereIsNoProduct() throws Exception {
        // given
        given(view.getComponentByReference(OrderFields.PRODUCT)).willReturn(productLookup);
        given(view.getComponentByReference(OrderFields.DEFAULT_TECHNOLOGY)).willReturn(defaultTechnologyField);

        given(productLookup.getEntity()).willReturn(null);

        // when
        orderDetailsHooks.fillDefaultTechnology(view);

        // then
        verify(defaultTechnologyField, never()).setFieldValue(anyString());
    }

    @Test
    public void shouldNotFillDefaultTechnologyIfThereIsNoDefaultTechnology() throws Exception {
        // given
        given(view.getComponentByReference(OrderFields.PRODUCT)).willReturn(productLookup);
        given(view.getComponentByReference(OrderFields.DEFAULT_TECHNOLOGY)).willReturn(defaultTechnologyField);

        given(productLookup.getEntity()).willReturn(product);

        given(technologyServiceO.getDefaultTechnology(product)).willReturn(null);

        // when
        orderDetailsHooks.fillDefaultTechnology(view);

        // then
        verify(defaultTechnologyField, never()).setFieldValue(Mockito.anyString());
    }

    @Test
    public void shouldFillDefaultTechnology() throws Exception {
        // given
        given(view.getComponentByReference(OrderFields.PRODUCT)).willReturn(productLookup);
        given(view.getComponentByReference(OrderFields.DEFAULT_TECHNOLOGY)).willReturn(defaultTechnologyField);

        given(productLookup.getEntity()).willReturn(product);

        given(technologyServiceO.getDefaultTechnology(product)).willReturn(defaultTechnology);
        given(defaultTechnology.getId()).willReturn(1L);

        // when
        orderDetailsHooks.fillDefaultTechnology(view);

        // then
        verify(defaultTechnologyField).setFieldValue(anyString());
    }

    @Test
    public void shouldDisableTechnologyIfThereIsNoProduct() throws Exception {
        // given
        given(view.getComponentByReference(OrderFields.PRODUCT)).willReturn(productLookup);
        given(view.getComponentByReference(OrderFields.DEFAULT_TECHNOLOGY)).willReturn(defaultTechnologyField);
        given(view.getComponentByReference(OrderFields.TECHNOLOGY_PROTOTYPE)).willReturn(technologyLookup);

        given(productLookup.getEntity()).willReturn(null);

        // when
        orderDetailsHooks.disableTechnologiesIfProductDoesNotAny(view);

        // then
        verify(defaultTechnologyField).setEnabled(false);
        verify(technologyLookup).setRequired(false);
    }

    @Test
    public void shouldDisableTechnologyIfProductHasNoTechnologies() throws Exception {
        // given
        given(view.getComponentByReference(OrderFields.PRODUCT)).willReturn(productLookup);
        given(view.getComponentByReference(OrderFields.DEFAULT_TECHNOLOGY)).willReturn(defaultTechnologyField);
        given(view.getComponentByReference(OrderFields.TECHNOLOGY_PROTOTYPE)).willReturn(technologyLookup);

        given(productLookup.getEntity()).willReturn(product);

        given(dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY))
                .willReturn(technologyDD);

        given(technologyDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(1)).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getTotalNumberOfEntities()).willReturn(0);

        // when
        orderDetailsHooks.disableTechnologiesIfProductDoesNotAny(view);

        // then
        verify(defaultTechnologyField).setEnabled(false);
        verify(technologyLookup).setRequired(false);
    }

    @Test
    public void shouldSetTechnologyAndPlannedQuantityAsRequired() throws Exception {
        // given
        given(view.getComponentByReference(OrderFields.PRODUCT)).willReturn(productLookup);
        given(view.getComponentByReference(OrderFields.DEFAULT_TECHNOLOGY)).willReturn(defaultTechnologyField);
        given(view.getComponentByReference(OrderFields.TECHNOLOGY_PROTOTYPE)).willReturn(technologyLookup);

        given(productLookup.getEntity()).willReturn(product);

        given(dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY))
                .willReturn(technologyDD);

        given(technologyDD.find()).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.add(Mockito.any(SearchCriterion.class))).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.setMaxResults(1)).willReturn(searchCriteriaBuilder);
        given(searchCriteriaBuilder.list()).willReturn(searchResult);
        given(searchResult.getTotalNumberOfEntities()).willReturn(1);

        // when
        orderDetailsHooks.disableTechnologiesIfProductDoesNotAny(view);

        // then
        verify(defaultTechnologyField).setEnabled(false);
        verify(technologyLookup).setRequired(true);
    }

    @Test
    public void shouldNotDisableFormIfOrderIsNotDone() throws Exception {
        // given
        given(view.getComponentByReference(L_FORM)).willReturn(orderForm);
        given(orderForm.getEntityId()).willReturn(L_ID);

        given(orderService.getOrder(L_ID)).willReturn(order);

        given(order.getStringField(OrderFields.STATE)).willReturn(OrderState.PENDING.getStringValue());

        // when
        orderDetailsHooks.disableFieldOrderForm(view);

        // then
        verify(orderForm).setFormEnabled(true);
    }

    @Test
    public void shouldNotDisableFormForDoneOrder() throws Exception {
        // given
        given(view.getComponentByReference(L_FORM)).willReturn(orderForm);
        given(orderForm.getEntityId()).willReturn(L_ID);

        given(orderService.getOrder(L_ID)).willReturn(order);

        given(order.getStringField(OrderFields.STATE)).willReturn(OrderState.COMPLETED.getStringValue());
        given(order.isValid()).willReturn(true);

        // when
        orderDetailsHooks.disableFieldOrderForm(view);

        // then
        verify(orderForm).setFormEnabled(false);
    }

    @Test
    public void shouldCheckEnabledFieldWhenOrderStateIsAccepted() throws Exception {
        // given
        given(order.getStringField(OrderFields.STATE)).willReturn(OrderState.ACCEPTED.getStringValue());

        // when
        orderDetailsHooks.changedEnabledFieldForSpecificOrderState(view);

        // then
        Mockito.verify(correctDateFromField).setEnabled(true);
        Mockito.verify(correctDateToField).setEnabled(true);
        Mockito.verify(commentDateFromField).setEnabled(true);
        Mockito.verify(commentDateToField).setEnabled(true);
        Mockito.verify(reasonsDateFromField).setEnabled(true);
        Mockito.verify(reasonsDateToField).setEnabled(true);
        Mockito.verify(dateFromField).setEnabled(true);
        Mockito.verify(dateToField).setEnabled(true);
    }

}
