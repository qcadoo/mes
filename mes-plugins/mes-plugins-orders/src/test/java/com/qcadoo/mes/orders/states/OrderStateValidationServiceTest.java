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
package com.qcadoo.mes.orders.states;

import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.DEADLINE;
import static com.qcadoo.mes.orders.constants.OrderFields.DONE_QUANTITY;
import static com.qcadoo.mes.orders.constants.OrderFields.PRODUCTION_LINE;
import static com.qcadoo.mes.orders.constants.OrderFields.START_DATE;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.Entity;

public class OrderStateValidationServiceTest {

    private static final String L_MISSING_MESSAGE = "orders.order.orderStates.fieldRequired";

    private static final String L_PRODUCTION_LINE_DOESNT_SUPPORT_TECHNOLOGY = "orders.order.productionLine.error.productionLineDoesntSupportTechnology";

    private static final String L_TECHNOLOGY_WRONG_STATE = "orders.validate.technology.error.wrongState.accepted";

    private static final String L_WRONG_DEADLINE = "orders.validate.global.error.deadline";

    private OrderStateValidationService orderStateValidationService;

    @Mock
    private OrderService orderService;

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

        ReflectionTestUtils.setField(orderStateValidationService, "orderService", orderService);

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
        given(order.getField(DEADLINE)).willReturn(new Date());
        given(order.getField(START_DATE)).willReturn(new Date(System.currentTimeMillis() - 10000));

        given(orderService.checkIfProductionLineSupportsTechnology(order)).willReturn(true);

        // when
        orderStateValidationService.validationOnAccepted(stateChangeContext);

        // then
        verify(stateChangeContext, never()).addFieldValidationError(Mockito.anyString(), Mockito.eq(L_MISSING_MESSAGE));
        verify(stateChangeContext, never()).addFieldValidationError(TECHNOLOGY, L_TECHNOLOGY_WRONG_STATE);
        verify(stateChangeContext, never()).addFieldValidationError(PRODUCTION_LINE, L_PRODUCTION_LINE_DOESNT_SUPPORT_TECHNOLOGY);
        verify(stateChangeContext, never()).addFieldValidationError(DEADLINE, L_WRONG_DEADLINE);
    }

    @Test
    public void shouldPerformValidationInProgress() throws Exception {
        // given
        given(order.getField(Mockito.anyString())).willReturn("fieldValue");

        // when
        orderStateValidationService.validationOnInProgress(stateChangeContext);

        // then
        verify(stateChangeContext, never()).addFieldValidationError(Mockito.anyString(), Mockito.eq(L_MISSING_MESSAGE));
    }

    @Test
    public void shouldPerformValidationCompleted() throws Exception {
        // given
        given(order.getField(Mockito.anyString())).willReturn("fieldValue");

        // when
        orderStateValidationService.validationOnCompleted(stateChangeContext);

        // then
        verify(stateChangeContext, never()).addFieldValidationError(Mockito.anyString(), Mockito.eq(L_MISSING_MESSAGE));
    }

    @Test
    public void shouldPerformValidationAcceptedFail() throws Exception {
        // given
        given(order.getField(Mockito.anyString())).willReturn(null);
        given(orderService.checkIfProductionLineSupportsTechnology(order)).willReturn(true);
        stubTechnologyField(null);

        // when
        orderStateValidationService.validationOnAccepted(stateChangeContext);

        // then
        for (String field : Arrays.asList(DATE_TO, DATE_FROM, TECHNOLOGY)) {
            verify(stateChangeContext).addFieldValidationError(field, L_MISSING_MESSAGE);
        }
        verify(stateChangeContext, never()).addFieldValidationError(TECHNOLOGY, L_TECHNOLOGY_WRONG_STATE);
        verify(stateChangeContext, never()).addFieldValidationError(PRODUCTION_LINE, L_PRODUCTION_LINE_DOESNT_SUPPORT_TECHNOLOGY);
    }

    @Test
    public void shouldPerformValidationAcceptedFailOnTechnologyState() throws Exception {
        // given
        given(order.getField(Mockito.anyString())).willReturn("fieldValue");
        given(order.getField(DEADLINE)).willReturn(new Date());
        given(order.getField(START_DATE)).willReturn(new Date(System.currentTimeMillis() - 10000));
        given(orderService.checkIfProductionLineSupportsTechnology(order)).willReturn(true);
        stubTechnologyState(TechnologyState.DRAFT);

        // when
        orderStateValidationService.validationOnAccepted(stateChangeContext);

        // then
        verify(stateChangeContext, never()).addFieldValidationError(Mockito.anyString(), Mockito.eq(L_MISSING_MESSAGE));
        verify(stateChangeContext).addFieldValidationError(TECHNOLOGY, L_TECHNOLOGY_WRONG_STATE);
        verify(stateChangeContext, never()).addFieldValidationError(PRODUCTION_LINE, L_PRODUCTION_LINE_DOESNT_SUPPORT_TECHNOLOGY);
        verify(stateChangeContext, never()).addFieldValidationError(DEADLINE, L_WRONG_DEADLINE);
    }

    @Test
    public void shouldPerformValidationAcceptedFailOnProductionLine() throws Exception {
        // given
        given(order.getField(Mockito.anyString())).willReturn("fieldValue");
        given(order.getField(DEADLINE)).willReturn(new Date());
        given(order.getField(START_DATE)).willReturn(new Date(System.currentTimeMillis() - 10000));
        given(orderService.checkIfProductionLineSupportsTechnology(order)).willReturn(false);

        // when
        orderStateValidationService.validationOnAccepted(stateChangeContext);

        // then
        verify(stateChangeContext, never()).addFieldValidationError(Mockito.anyString(), Mockito.eq(L_MISSING_MESSAGE));
        verify(stateChangeContext, never()).addFieldValidationError(TECHNOLOGY, L_TECHNOLOGY_WRONG_STATE);
        verify(stateChangeContext).addFieldValidationError(PRODUCTION_LINE, L_PRODUCTION_LINE_DOESNT_SUPPORT_TECHNOLOGY);
        verify(stateChangeContext, never()).addFieldValidationError(DEADLINE, L_PRODUCTION_LINE_DOESNT_SUPPORT_TECHNOLOGY);
    }

    @Test
    public void shouldPerformValidationInProgressFail() throws Exception {
        // given
        given(order.getField(Mockito.anyString())).willReturn(null);
        stubTechnologyField(null);

        // when
        orderStateValidationService.validationOnInProgress(stateChangeContext);

        // then
        for (String field : Arrays.asList(DATE_TO, DATE_FROM, TECHNOLOGY)) {
            verify(stateChangeContext).addFieldValidationError(field, L_MISSING_MESSAGE);
        }
        verify(stateChangeContext, never()).addFieldValidationError(TECHNOLOGY, L_TECHNOLOGY_WRONG_STATE);
    }

    @Test
    public void shouldPerformValidationInProgresFailOnTechnologyState() throws Exception {
        // given
        given(order.getField(Mockito.anyString())).willReturn("fieldValue");
        stubTechnologyState(TechnologyState.DRAFT);

        // when
        orderStateValidationService.validationOnInProgress(stateChangeContext);

        // then
        verify(stateChangeContext, never()).addFieldValidationError(Mockito.anyString(), Mockito.eq(L_MISSING_MESSAGE));
        verify(stateChangeContext).addFieldValidationError(TECHNOLOGY, L_TECHNOLOGY_WRONG_STATE);
    }

    @Test
    public void shouldPerformValidationCompletedFail() throws Exception {
        // given
        given(order.getField(Mockito.anyString())).willReturn(null);
        stubTechnologyField(null);

        // when
        orderStateValidationService.validationOnCompleted(stateChangeContext);

        // then
        for (String field : Arrays.asList(DATE_TO, DATE_FROM, TECHNOLOGY, DONE_QUANTITY)) {
            verify(stateChangeContext).addFieldValidationError(field, L_MISSING_MESSAGE);
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