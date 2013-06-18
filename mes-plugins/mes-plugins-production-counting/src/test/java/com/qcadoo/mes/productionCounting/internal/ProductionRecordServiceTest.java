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
package com.qcadoo.mes.productionCounting.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

public class ProductionRecordServiceTest {

    private ProductionRecordService productionRecordService;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity productionRecord;

    @Mock
    private Entity counting, operation;

    @Mock
    private FieldDefinition orderField, operationField;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        productionRecordService = new ProductionRecordService();

        given(dataDefinition.getField("order")).willReturn(orderField);
        given(dataDefinition.getField("technologyInstanceOperationComponent")).willReturn(operationField);

        given(productionRecord.getDataDefinition()).willReturn(dataDefinition);
    }

    @Test
    public void shouldNotAllowToAddProductionCountingWhenThereAreFinalOnesForNullOperation() {
        // given
        List<Entity> productionCountings = Arrays.asList(counting);
        given(counting.getBooleanField("lastRecord")).willReturn(true);
        given(productionRecord.getBelongsToField("technologyInstanceOperationComponent")).willReturn(null);

        // when
        boolean canIAdd = productionRecordService.willOrderAcceptOneMoreValidator(productionCountings, productionRecord,
                dataDefinition);

        // then
        assertFalse(canIAdd);
        verify(productionRecord).addError(orderField, "productionCounting.record.messages.error.final");
    }

    @Test
    public void shouldNotAllowToAddProductionCountingWhenThereAreFinalOnesForGivenOperation() {
        // given
        List<Entity> productionCountings = Arrays.asList(counting);
        given(counting.getBooleanField("lastRecord")).willReturn(true);

        given(productionRecord.getBelongsToField("technologyInstanceOperationComponent")).willReturn(operation);

        // when
        boolean canIAdd = productionRecordService.willOrderAcceptOneMoreValidator(productionCountings, productionRecord,
                dataDefinition);

        // then
        assertFalse(canIAdd);
        verify(productionRecord).addError(operationField, "productionCounting.record.messages.error.operationFinal");
    }

    @Test
    public final void shouldRecognizeIfOrderWasStarted() {
        mustNotBeStarted(null);
        mustNotBeStarted(OrderState.PENDING.getStringValue());
        mustNotBeStarted(OrderState.ACCEPTED.getStringValue());
        mustNotBeStarted(OrderState.DECLINED.getStringValue());
        mustNotBeStarted(OrderState.ABANDONED.getStringValue());

        mustBeStarted(OrderState.IN_PROGRESS.getStringValue());
        mustBeStarted(OrderState.COMPLETED.getStringValue());
        mustBeStarted(OrderState.INTERRUPTED.getStringValue());
    }

    private void mustBeStarted(final String state) {
        assertTrue(productionRecordService.isOrderStarted(state));
    }

    private void mustNotBeStarted(final String state) {
        assertFalse(productionRecordService.isOrderStarted(state));
    }
}
