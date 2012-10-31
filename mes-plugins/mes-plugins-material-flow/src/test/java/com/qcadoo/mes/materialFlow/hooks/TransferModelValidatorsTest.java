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
package com.qcadoo.mes.materialFlow.hooks;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_FROM;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.LOCATION_TO;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TYPE;
import static com.qcadoo.mes.materialFlow.constants.TransferType.TRANSPORT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

public class TransferModelValidatorsTest {

    private TransferModelValidators transferModelValidators;

    @Mock
    private DataDefinition transferDD;

    @Mock
    private Entity transfer, locationFrom, locationTo;

    @Mock
    private Date time;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        transferModelValidators = new TransferModelValidators();
    }

    @Test
    public void shouldReturnFalseAndAddErrorWhenValidateTransferAndAllFieldsAreNull() {
        // given
        given(transfer.getStringField(TYPE)).willReturn(null);
        given(transfer.getField(TIME)).willReturn(null);
        given(transfer.getBelongsToField(LOCATION_FROM)).willReturn(null);
        given(transfer.getBelongsToField(LOCATION_TO)).willReturn(null);

        // when
        boolean result = transferModelValidators.validateTransfer(transferDD, transfer);

        // then
        assertFalse(result);

        verify(transfer, times(4)).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnFalseAndAddErrorWhenValidateTransferAndTypeIsNull() {
        // given
        given(transfer.getStringField(TYPE)).willReturn(null);
        given(transfer.getField(TIME)).willReturn(time);
        given(transfer.getBelongsToField(LOCATION_FROM)).willReturn(locationFrom);
        given(transfer.getBelongsToField(LOCATION_TO)).willReturn(locationTo);

        // when
        boolean result = transferModelValidators.validateTransfer(transferDD, transfer);

        // then
        assertFalse(result);

        verify(transfer, times(1)).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnFalseAndAddErrorWhenValidateTransferAndTimeIsNull() {
        // given
        given(transfer.getStringField(TYPE)).willReturn(TRANSPORT.getStringValue());
        given(transfer.getField(TIME)).willReturn(null);
        given(transfer.getBelongsToField(LOCATION_FROM)).willReturn(locationFrom);
        given(transfer.getBelongsToField(LOCATION_TO)).willReturn(locationTo);

        // when
        boolean result = transferModelValidators.validateTransfer(transferDD, transfer);

        // then
        assertFalse(result);

        verify(transfer, times(1)).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnFalseAndAddErrorWhenValidateTransferAndLocationsAreNull() {
        // given
        given(transfer.getStringField(TYPE)).willReturn(TRANSPORT.getStringValue());
        given(transfer.getField(TIME)).willReturn(time);
        given(transfer.getBelongsToField(LOCATION_FROM)).willReturn(null);
        given(transfer.getBelongsToField(LOCATION_TO)).willReturn(null);

        // when
        boolean result = transferModelValidators.validateTransfer(transferDD, transfer);

        // then
        assertFalse(result);

        verify(transfer, times(2)).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

    @Test
    public void shouldReturnTrueWhenValidateTransferAndAllFieldsArentNull() {
        // given
        given(transfer.getStringField(TYPE)).willReturn(TRANSPORT.getStringValue());
        given(transfer.getField(TIME)).willReturn(time);
        given(transfer.getBelongsToField(LOCATION_FROM)).willReturn(locationFrom);
        given(transfer.getBelongsToField(LOCATION_TO)).willReturn(locationTo);

        // when
        boolean result = transferModelValidators.validateTransfer(transferDD, transfer);

        // then
        assertTrue(result);

        verify(transfer, never()).addError(Mockito.any(FieldDefinition.class), Mockito.anyString());
    }

}
