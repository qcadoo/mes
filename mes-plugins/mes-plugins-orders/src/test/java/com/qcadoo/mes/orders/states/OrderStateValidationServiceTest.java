/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
package com.qcadoo.mes.orders.states;

import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.DONE_QUANTITY;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyState;
import com.qcadoo.model.api.Entity;

public class OrderStateValidationServiceTest {

    private static final String MISSING_MESSAGE = "orders.order.orderStates.fieldRequired";

    private static final String TECHNOLOGY_WRONG_STATE = "orders.validate.technology.error.wrongState.accepted";

    private OrderStateValidationService orderStateValidationService;

    @Mock
    private Entity order;

    @Mock
    private Entity technology;

    @Mock
    private StateChangeContext stateChangeContext;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);
        orderStateValidationService = new OrderStateValidationService();
        given(stateChangeContext.getOwner()).willReturn(order);
        stubTechnologyField(technology);
        stubTechnologyState(TechnologyState.ACCEPTED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenEntityIsNullValidationAccepted() throws Exception {
        // when
        orderStateValidationService.validationOnAccepted(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenEntityIsNullValidationInProgress() throws Exception {
        // when
        orderStateValidationService.validationOnInProgress(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenEntityIsNullValidationCompleted() throws Exception {
        // when
        orderStateValidationService.validationOnCompleted(null);
    }

    @Test
    public void shouldPerformValidationAccepted() throws Exception {
        // given
        given(order.getField(Mockito.anyString())).willReturn("fieldValue");

        // when
        orderStateValidationService.validationOnAccepted(stateChangeContext);

        // then
        verify(stateChangeContext, Mockito.never()).addFieldValidationError(Mockito.eq(MISSING_MESSAGE), Mockito.anyString());
    }

    @Test
    public void shouldPerformValidationInProgress() throws Exception {
        // given
        given(order.getField(Mockito.anyString())).willReturn("fieldValue");

        // when
        orderStateValidationService.validationOnInProgress(stateChangeContext);

        // then
        verify(stateChangeContext, Mockito.never()).addFieldValidationError(Mockito.eq(MISSING_MESSAGE), Mockito.anyString());
    }

    @Test
    public void shouldPerformValidationCompleted() throws Exception {
        // given
        given(order.getField(Mockito.anyString())).willReturn("fieldValue");

        // when
        orderStateValidationService.validationOnCompleted(stateChangeContext);

        // then
        verify(stateChangeContext, Mockito.never()).addFieldValidationError(Mockito.eq(MISSING_MESSAGE), Mockito.anyString());
    }

    @Test
    public void shouldPerformValidationAcceptedFail() throws Exception {
        // given
        stubTechnologyField(null);

        // when
        orderStateValidationService.validationOnAccepted(stateChangeContext);

        // then
        for (String field : Arrays.asList(DATE_TO, DATE_FROM, TECHNOLOGY)) {
            verify(stateChangeContext).addFieldValidationError(MISSING_MESSAGE, field);
        }
        verify(stateChangeContext, never()).addFieldValidationError(TECHNOLOGY_WRONG_STATE, TECHNOLOGY);
    }

    @Test
    public void shouldPerformValidationAcceptedFailOnTechnologyState() throws Exception {
        // given
        stubTechnologyState(TechnologyState.DRAFT);

        // when
        orderStateValidationService.validationOnAccepted(stateChangeContext);

        // then
        for (String field : Arrays.asList(DATE_TO, DATE_FROM)) {
            verify(stateChangeContext).addFieldValidationError(MISSING_MESSAGE, field);
        }
        verify(stateChangeContext, never()).addFieldValidationError(MISSING_MESSAGE, TECHNOLOGY);
        verify(stateChangeContext).addFieldValidationError(TECHNOLOGY_WRONG_STATE, TECHNOLOGY);
    }

    @Test
    public void shouldPerformValidationInProgressFail() throws Exception {
        // given
        given(order.getField(Mockito.anyString())).willReturn(null);

        // when
        orderStateValidationService.validationOnInProgress(stateChangeContext);

        // then
        for (String field : Arrays.asList(DATE_TO, DATE_FROM, TECHNOLOGY)) {
            verify(stateChangeContext).addFieldValidationError(MISSING_MESSAGE, field);
        }
        verify(stateChangeContext, never()).addFieldValidationError(TECHNOLOGY_WRONG_STATE, TECHNOLOGY);
    }

    @Test
    public void shouldPerformValidationInProgresFailOnTechnologyState() throws Exception {
        // given
        stubTechnologyState(TechnologyState.DRAFT);

        // when
        orderStateValidationService.validationOnInProgress(stateChangeContext);

        // then
        for (String field : Arrays.asList(DATE_TO, DATE_FROM)) {
            verify(stateChangeContext).addFieldValidationError(MISSING_MESSAGE, field);
        }
        verify(stateChangeContext, never()).addFieldValidationError(MISSING_MESSAGE, TECHNOLOGY);
        verify(stateChangeContext).addFieldValidationError(TECHNOLOGY_WRONG_STATE, TECHNOLOGY);
    }

    @Test
    public void shouldPerformValidationCompletedFail() throws Exception {
        // given
        stubTechnologyField(null);

        // when
        orderStateValidationService.validationOnCompleted(stateChangeContext);

        // then
        for (String field : Arrays.asList(DATE_TO, DATE_FROM, TECHNOLOGY, DONE_QUANTITY)) {
            verify(stateChangeContext).addFieldValidationError(MISSING_MESSAGE, field);
        }
    }

    private void stubTechnologyField(final Entity value) {
        given(order.getBelongsToField(TECHNOLOGY)).willReturn(value);
        given(order.getField(TECHNOLOGY)).willReturn(value);
    }

    private void stubTechnologyState(final TechnologyState technologyState) {
        given(technology.getStringField(TechnologyFields.STATE)).willReturn(technologyState.getStringValue());
        given(technology.getField(TechnologyFields.STATE)).willReturn(technologyState.getStringValue());
    }
}