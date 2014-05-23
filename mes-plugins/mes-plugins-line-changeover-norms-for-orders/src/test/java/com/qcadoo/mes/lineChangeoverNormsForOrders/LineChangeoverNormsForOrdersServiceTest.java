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
package com.qcadoo.mes.lineChangeoverNormsForOrders;

import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.LineChangeoverNormsForOrdersConstants.ORDER_FIELDS;
import static com.qcadoo.mes.lineChangeoverNormsForOrders.constants.LineChangeoverNormsForOrdersConstants.PREVIOUS_ORDER_FIELDS;
import static com.qcadoo.testing.model.EntityTestUtils.stubBelongsToField;
import static com.qcadoo.testing.model.EntityTestUtils.stubDateField;
import static com.qcadoo.testing.model.EntityTestUtils.stubStringField;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyGroupFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

public class LineChangeoverNormsForOrdersServiceTest {

    private LineChangeoverNormsForOrdersService lineChangeoverNormsForOrdersService;

    private static final DateTime DATE_TIME_EARLY = new DateTime(2000, 1, 1, 0, 0);

    private static final DateTime DATE_TIME_LATE = new DateTime(2014, 1, 1, 0, 0);

    private static final String NOT_EMPTY_NUMBER = "000001";

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private TranslationService translationService;

    @Mock
    private Entity previousOrder, order, technologyPrototype, technologyGroup, productionLine;

    @Mock
    private ViewDefinitionState view;

    @Mock
    private FieldComponent technologyNumberField, technologyGroupNumberField, dateToFromField, dateIsField;

    @Mock
    private LookupComponent orderLookup;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        lineChangeoverNormsForOrdersService = new LineChangeoverNormsForOrdersServiceImpl();

        setField(lineChangeoverNormsForOrdersService, "dataDefinitionService", dataDefinitionService);
        setField(lineChangeoverNormsForOrdersService, "translationService", translationService);

        stubDateField(order, OrderFields.EFFECTIVE_DATE_FROM, null);
        stubDateField(order, OrderFields.CORRECTED_DATE_FROM, null);
        stubDateField(order, OrderFields.DATE_FROM, null);

        stubDateField(order, OrderFields.EFFECTIVE_DATE_TO, null);
        stubDateField(order, OrderFields.CORRECTED_DATE_TO, null);
        stubDateField(order, OrderFields.DATE_TO, null);

        stubStringField(technologyPrototype, TechnologyFields.NUMBER, NOT_EMPTY_NUMBER);
        stubStringField(technologyGroup, TechnologyGroupFields.NUMBER, NOT_EMPTY_NUMBER);

        given(view.getComponentByReference(PREVIOUS_ORDER_FIELDS.get(0))).willReturn(orderLookup);
        given(view.getComponentByReference(PREVIOUS_ORDER_FIELDS.get(1))).willReturn(technologyNumberField);
        given(view.getComponentByReference(PREVIOUS_ORDER_FIELDS.get(2))).willReturn(technologyGroupNumberField);
        given(view.getComponentByReference(PREVIOUS_ORDER_FIELDS.get(3))).willReturn(dateToFromField);
        given(view.getComponentByReference(PREVIOUS_ORDER_FIELDS.get(4))).willReturn(dateIsField);

        given(view.getComponentByReference(ORDER_FIELDS.get(0))).willReturn(orderLookup);
        given(view.getComponentByReference(ORDER_FIELDS.get(1))).willReturn(technologyNumberField);
        given(view.getComponentByReference(ORDER_FIELDS.get(2))).willReturn(technologyGroupNumberField);
        given(view.getComponentByReference(ORDER_FIELDS.get(3))).willReturn(dateToFromField);
        given(view.getComponentByReference(ORDER_FIELDS.get(4))).willReturn(dateIsField);
    }

    private void stubOrderLookupEntity(final Entity order) {
        given(orderLookup.isEmpty()).willReturn(order == null);
        given(orderLookup.getEntity()).willReturn(order);
    }

    @Test
    public void shouldNotFillOrderForm() {
        // given
        stubOrderLookupEntity(null);

        // when
        lineChangeoverNormsForOrdersService.fillOrderForm(view, ORDER_FIELDS);

        // then
        verify(orderLookup, never()).setFieldValue(Mockito.any());
        verify(technologyNumberField, never()).setFieldValue(Mockito.any());
        verify(technologyGroupNumberField, never()).setFieldValue(Mockito.any());
        verify(dateToFromField, never()).setFieldValue(Mockito.any());
        verify(dateIsField, never()).setFieldValue(Mockito.any());
    }

    @Test
    public void shouldNotFillOrderFormIfOrderIsNull() {
        // given
        stubOrderLookupEntity(null);

        // when
        lineChangeoverNormsForOrdersService.fillOrderForm(view, ORDER_FIELDS);

        // then
        verify(orderLookup, never()).setFieldValue(Mockito.any());
        verify(technologyNumberField, never()).setFieldValue(Mockito.any());
        verify(technologyGroupNumberField, never()).setFieldValue(Mockito.any());
        verify(dateToFromField, never()).setFieldValue(Mockito.any());
        verify(dateIsField, never()).setFieldValue(Mockito.any());
    }

    @Test
    public void shouldFillOrderFormIfFormIsNotNullAndTechnologyIsNull() {
        // given
        stubOrderLookupEntity(order);
        stubBelongsToField(order, OrderFields.TECHNOLOGY_PROTOTYPE, null);

        // when
        lineChangeoverNormsForOrdersService.fillOrderForm(view, ORDER_FIELDS);

        // then
        verify(orderLookup).setFieldValue(Mockito.notNull());
        verify(technologyNumberField).setFieldValue(null);
        verify(technologyNumberField, never()).setFieldValue(Mockito.notNull());
        verify(technologyGroupNumberField).setFieldValue(null);
        verify(technologyGroupNumberField, never()).setFieldValue(Mockito.notNull());
        verify(dateToFromField).setFieldValue(null);
        verify(dateIsField).setFieldValue(null);
    }

    @Test
    public void shouldFillOrderFormIfFormIsNotNullAndTechnologyIsNotNullAndTechnologyGroupIsNull() {
        // given
        stubOrderLookupEntity(order);
        stubBelongsToField(order, OrderFields.TECHNOLOGY_PROTOTYPE, technologyPrototype);
        stubBelongsToField(technologyPrototype, TechnologyFields.TECHNOLOGY_GROUP, null);

        // when
        lineChangeoverNormsForOrdersService.fillOrderForm(view, ORDER_FIELDS);

        // then
        verify(orderLookup).setFieldValue(Mockito.any());
        verify(technologyNumberField).setFieldValue(NOT_EMPTY_NUMBER);
        verify(technologyGroupNumberField).setFieldValue(null);
        verify(technologyGroupNumberField, never()).setFieldValue(Mockito.notNull());
        verify(dateToFromField).setFieldValue(null);
        verify(dateIsField).setFieldValue(null);
    }

    @Test
    public void shouldFillOrderFormIfFormIsNotNullAndTechnologyIsNotNullAndTechnologyGroupIsNotNull() {
        // given
        stubOrderLookupEntity(order);
        stubBelongsToField(order, OrderFields.TECHNOLOGY_PROTOTYPE, technologyPrototype);
        stubBelongsToField(technologyPrototype, TechnologyFields.TECHNOLOGY_GROUP, technologyGroup);

        // when
        lineChangeoverNormsForOrdersService.fillOrderForm(view, ORDER_FIELDS);

        // then
        verify(orderLookup).setFieldValue(Mockito.any());
        verify(technologyNumberField).setFieldValue(Mockito.any());
        verify(technologyGroupNumberField).setFieldValue(Mockito.any());
        verify(dateToFromField).setFieldValue(null);
        verify(dateIsField).setFieldValue(null);
    }

    @Test
    public void shouldFillOrderFormIfFormIsNotNullAndTechnologyIsNotNullAndTechnologyGroupIsNotNullAndEffectiveDateFromIsNotNull() {
        // given
        stubOrderLookupEntity(order);
        stubBelongsToField(order, OrderFields.TECHNOLOGY_PROTOTYPE, technologyPrototype);
        stubBelongsToField(technologyPrototype, TechnologyFields.TECHNOLOGY_GROUP, technologyGroup);

        stubDateField(order, OrderFields.EFFECTIVE_DATE_FROM, DATE_TIME_EARLY.toDate());

        // when
        lineChangeoverNormsForOrdersService.fillOrderForm(view, ORDER_FIELDS);

        // then
        verify(orderLookup).setFieldValue(Mockito.any());
        verify(technologyNumberField).setFieldValue(Mockito.any());
        verify(technologyGroupNumberField).setFieldValue(Mockito.any());
        verify(dateToFromField).setFieldValue(Mockito.any());
        verify(dateIsField).setFieldValue(Mockito.any());
    }

    @Test
    public void shouldFillOrderFormIfFormIsNotNullAndTechnologyIsNotNullAndTechnologyGroupIsNotNullAndCorrectedDateFromIsNotNull() {
        // given
        stubOrderLookupEntity(order);
        stubBelongsToField(order, OrderFields.TECHNOLOGY_PROTOTYPE, technologyPrototype);
        stubBelongsToField(technologyPrototype, TechnologyFields.TECHNOLOGY_GROUP, technologyGroup);

        stubDateField(order, OrderFields.CORRECTED_DATE_FROM, DATE_TIME_EARLY.toDate());

        // when
        lineChangeoverNormsForOrdersService.fillOrderForm(view, ORDER_FIELDS);

        // then
        verify(orderLookup).setFieldValue(Mockito.any());
        verify(technologyNumberField).setFieldValue(Mockito.any());
        verify(technologyGroupNumberField).setFieldValue(Mockito.any());
        verify(dateToFromField).setFieldValue(Mockito.any());
        verify(dateIsField).setFieldValue(Mockito.any());
    }

    @Test
    public void shouldFillOrderFormIfFormIsNotNullAndTechnologyIsNotNullAndTechnologyGroupIsNotNullAndDateFromIsNotNull() {
        // given
        stubOrderLookupEntity(order);
        stubBelongsToField(order, OrderFields.TECHNOLOGY_PROTOTYPE, technologyPrototype);
        stubBelongsToField(technologyPrototype, TechnologyFields.TECHNOLOGY_GROUP, technologyGroup);

        stubDateField(order, OrderFields.DATE_FROM, DATE_TIME_EARLY.toDate());

        // when
        lineChangeoverNormsForOrdersService.fillOrderForm(view, ORDER_FIELDS);

        // then
        verify(orderLookup).setFieldValue(Mockito.any());
        verify(technologyNumberField).setFieldValue(Mockito.any());
        verify(technologyGroupNumberField).setFieldValue(Mockito.any());
        verify(dateToFromField).setFieldValue(Mockito.any());
        verify(dateIsField).setFieldValue(Mockito.any());
    }

    @Test
    public void shouldFillPreviousOrderFormIfFormIsNotNullAndTechnologyIsNotNullAndTechnologyGroupIsNotNullAndEffectiveDateToIsNotNull() {
        // given
        stubOrderLookupEntity(order);
        stubBelongsToField(order, OrderFields.TECHNOLOGY_PROTOTYPE, technologyPrototype);
        stubBelongsToField(technologyPrototype, TechnologyFields.TECHNOLOGY_GROUP, technologyGroup);

        stubDateField(order, OrderFields.EFFECTIVE_DATE_TO, DATE_TIME_LATE.toDate());

        // when
        lineChangeoverNormsForOrdersService.fillOrderForm(view, PREVIOUS_ORDER_FIELDS);

        // then
        verify(orderLookup).setFieldValue(Mockito.any());
        verify(technologyNumberField).setFieldValue(Mockito.any());
        verify(technologyGroupNumberField).setFieldValue(Mockito.any());
        verify(dateToFromField).setFieldValue(Mockito.any());
        verify(dateIsField).setFieldValue(Mockito.any());
    }

    @Test
    public void shouldFillPreviousOrderFormIfFormIsNotNullAndTechnologyIsNotNullAndTechnologyGroupIsNotNullAndCorrectedDateToIsNotNull() {
        // given
        stubOrderLookupEntity(order);
        stubBelongsToField(order, OrderFields.TECHNOLOGY_PROTOTYPE, technologyPrototype);
        stubBelongsToField(technologyPrototype, TechnologyFields.TECHNOLOGY_GROUP, technologyGroup);

        stubDateField(order, OrderFields.CORRECTED_DATE_TO, DATE_TIME_LATE.toDate());

        // when
        lineChangeoverNormsForOrdersService.fillOrderForm(view, PREVIOUS_ORDER_FIELDS);

        // then
        verify(orderLookup).setFieldValue(Mockito.any());
        verify(technologyNumberField).setFieldValue(Mockito.any());
        verify(technologyGroupNumberField).setFieldValue(Mockito.any());
        verify(dateToFromField).setFieldValue(Mockito.any());
        verify(dateIsField).setFieldValue(Mockito.any());
    }

    @Test
    public void shouldFillPreviousOrderFormIfFormIsNotNullAndTechnologyIsNotNullAndTechnologyGroupIsNotNullAndDateToIsNotNull() {
        // given
        stubOrderLookupEntity(order);
        stubBelongsToField(order, OrderFields.TECHNOLOGY_PROTOTYPE, technologyPrototype);
        stubBelongsToField(technologyPrototype, TechnologyFields.TECHNOLOGY_GROUP, technologyGroup);

        stubDateField(order, OrderFields.DATE_TO, DATE_TIME_LATE.toDate());

        // when
        lineChangeoverNormsForOrdersService.fillOrderForm(view, PREVIOUS_ORDER_FIELDS);

        // then
        verify(orderLookup).setFieldValue(Mockito.any());
        verify(technologyNumberField).setFieldValue(Mockito.any());
        verify(technologyGroupNumberField).setFieldValue(Mockito.any());
        verify(dateToFromField).setFieldValue(Mockito.any());
        verify(dateIsField).setFieldValue(Mockito.any());
    }

    @Test
    public void shouldReturnTrueWhenCheckIfOrderHasCorrectStateAndIsPreviousIfOrdersAreNull() {
        // when
        boolean result = lineChangeoverNormsForOrdersService.previousOrderEndsBeforeOrIsWithdrawed(null, null);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueWhenOrdersAreNotNullAndPreviousOrderIsCorrectAndPrevious() {
        // given
        stubStringField(previousOrder, OrderFields.STATE, OrderStateStringValues.ACCEPTED);
        stubDateField(previousOrder, OrderFields.DATE_TO, DATE_TIME_EARLY.toDate());
        stubDateField(order, OrderFields.DATE_FROM, DATE_TIME_LATE.toDate());

        // when
        boolean result = lineChangeoverNormsForOrdersService.previousOrderEndsBeforeOrIsWithdrawed(previousOrder, order);

        // then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenOrdersAreNotNullAndPreviousOrderIsNotCorrectAndPrevious() {
        // given
        stubStringField(previousOrder, OrderFields.STATE, OrderStateStringValues.ACCEPTED);
        stubDateField(previousOrder, OrderFields.DATE_TO, DATE_TIME_LATE.toDate());
        stubDateField(order, OrderFields.DATE_FROM, DATE_TIME_EARLY.toDate());

        // when
        boolean result = lineChangeoverNormsForOrdersService.previousOrderEndsBeforeOrIsWithdrawed(previousOrder, order);

        // then
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseWhenOrdersAreNotNullAndPreviousOrderDateToIsAndOrderDateFromIsNull() {
        // given
        stubStringField(previousOrder, OrderFields.STATE, OrderStateStringValues.ACCEPTED);
        stubDateField(previousOrder, OrderFields.DATE_TO, null);
        stubDateField(order, OrderFields.DATE_FROM, null);

        // when
        boolean result = lineChangeoverNormsForOrdersService.previousOrderEndsBeforeOrIsWithdrawed(previousOrder, order);

        // then
        assertFalse(result);
    }

}
