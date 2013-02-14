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
package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.STAFF;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TRANSFORMATIONS_CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TRANSFORMATIONS_PRODUCTION;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;
import static com.qcadoo.mes.materialFlow.constants.TransferType.CONSUMPTION;
import static com.qcadoo.mes.materialFlow.constants.TransferType.PRODUCTION;
import static com.qcadoo.mes.materialFlow.constants.TransformationsFields.LOCATION_FROM;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class TransferModelHooksTest {

    private TransferModelHooks materialFlowTransferModelHooks;

    @Mock
    private Entity transfer, transformation;

    @Mock
    private Entity locationTo, locationFrom, staff;

    @Mock
    private DataDefinition transferDD;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        materialFlowTransferModelHooks = new TransferModelHooks();
    }

    @Test
    public void shouldCopyProductionDataFromBelongingTransformation() {
        // given
        given(transfer.getBelongsToField(TRANSFORMATIONS_PRODUCTION)).willReturn(transformation);
        given(transformation.getField(TIME)).willReturn("1234");
        given(transformation.getBelongsToField(LOCATION_FROM)).willReturn(locationFrom);
        given(transformation.getBelongsToField(LOCATION_TO)).willReturn(locationTo);
        given(transformation.getBelongsToField(STAFF)).willReturn(staff);

        // when
        materialFlowTransferModelHooks.copyProductionOrConsumptionDataFromBelongingTransformation(transferDD, transfer);

        // then
        verify(transfer).setField(TYPE, PRODUCTION.getStringValue());
        verify(transfer).setField(TIME, "1234");
        verify(transfer).setField(LOCATION_TO, locationTo);
        verify(transfer).setField(STAFF, staff);
    }

    @Test
    public void shouldCopyConsumptionDataFromBelongingTransformation() {
        // given
        given(transfer.getBelongsToField(TRANSFORMATIONS_CONSUMPTION)).willReturn(transformation);
        given(transformation.getField(TIME)).willReturn("1234");
        given(transformation.getBelongsToField(LOCATION_TO)).willReturn(locationTo);
        given(transformation.getBelongsToField(LOCATION_FROM)).willReturn(locationFrom);
        given(transformation.getBelongsToField(STAFF)).willReturn(staff);

        // when
        materialFlowTransferModelHooks.copyProductionOrConsumptionDataFromBelongingTransformation(transferDD, transfer);

        // then
        verify(transfer).setField(TYPE, CONSUMPTION.getStringValue());
        verify(transfer).setField(TIME, "1234");
        verify(transfer).setField(LOCATION_FROM, locationFrom);
        verify(transfer).setField(STAFF, staff);
    }

    @Test
    public void shouldNotTriggerCopyingWhenSavingPlainTransfer() {
        // when
        materialFlowTransferModelHooks.copyProductionOrConsumptionDataFromBelongingTransformation(transferDD, transfer);

        // then
        verify(transfer, never()).setField(anyString(), any());
    }
}
