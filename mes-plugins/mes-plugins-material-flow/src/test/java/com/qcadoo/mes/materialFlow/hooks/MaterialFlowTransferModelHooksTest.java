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
package com.qcadoo.mes.materialFlow.hooks;

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

public class MaterialFlowTransferModelHooksTest {

    private TransferModelHooks materialFlowTransferModelHooks;

    @Mock
    private Entity transfer, transformation;

    @Mock
    private Entity stockAreasTo, stockAreasFrom, staff;

    @Mock
    private DataDefinition dd;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        materialFlowTransferModelHooks = new TransferModelHooks();
    }

    @Test
    public void shouldCopyProductionDataFromBelongingTransformation() {
        // given
        given(transfer.getBelongsToField("transformationsProduction")).willReturn(transformation);
        given(transformation.getField("time")).willReturn("1234");
        given(transformation.getBelongsToField("stockAreasTo")).willReturn(stockAreasTo);
        given(transformation.getBelongsToField("stockAreasFrom")).willReturn(stockAreasFrom);
        given(transformation.getBelongsToField("staff")).willReturn(staff);

        // when
        materialFlowTransferModelHooks.copyProductionOrConsumptionDataFromBelongingTransformation(dd, transfer);

        // then
        verify(transfer).setField("type", "Production");
        verify(transfer).setField("time", "1234");
        verify(transfer).setField("stockAreasTo", stockAreasTo);
        verify(transfer).setField("staff", staff);
    }

    @Test
    public void shouldCopyConsumptionDataFromBelongingTransformation() {
        // given
        given(transfer.getBelongsToField("transformationsConsumption")).willReturn(transformation);
        given(transformation.getField("time")).willReturn("1234");
        given(transformation.getBelongsToField("stockAreasTo")).willReturn(stockAreasTo);
        given(transformation.getBelongsToField("stockAreasFrom")).willReturn(stockAreasFrom);
        given(transformation.getBelongsToField("staff")).willReturn(staff);

        // when
        materialFlowTransferModelHooks.copyProductionOrConsumptionDataFromBelongingTransformation(dd, transfer);

        // then
        verify(transfer).setField("type", "Consumption");
        verify(transfer).setField("time", "1234");
        verify(transfer).setField("stockAreasFrom", stockAreasFrom);
        verify(transfer).setField("staff", staff);
    }

    @Test
    public void shouldNotTriggerCopyingWhenSavingPlainTransfer() {
        // when
        materialFlowTransferModelHooks.copyProductionOrConsumptionDataFromBelongingTransformation(dd, transfer);

        // then
        verify(transfer, never()).setField(anyString(), any());
    }
}
