/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
    @Ignore
    // TODO ALBR
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
    @Ignore
    // TODO ALBR
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
}
