/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.advancedGenealogyForOrders.hooks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

public class TrackingForOrdersModelValidatorsTest {

    private TrackingForOrdersModelValidators trackingForOrdersModelValidators;

    @Mock
    private Entity order, trackingRecord;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private FieldDefinition orderField;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        trackingForOrdersModelValidators = new TrackingForOrdersModelValidators();

        given(trackingRecord.getBelongsToField("order")).willReturn(order);
        given(trackingRecord.getStringField("entityType")).willReturn("02forOrder");
        given(dataDefinition.getField("order")).willReturn(orderField);
    }

    @Test
    public void shouldInvalidateTrackingRecordIfOrderIsAcceptedAndHasUnchangablePlanAfterAccept() {
        // given
        given(order.getStringField("trackingRecordTreatment")).willReturn("02unchangablePlanAfterOrderAccept");
        given(order.getStringField("state")).willReturn("02accepted");

        // when
        boolean isTrackingRecordValid = trackingForOrdersModelValidators.checkChoosenOrderState(dataDefinition, trackingRecord);

        // then
        assertFalse(isTrackingRecordValid);
        verify(trackingRecord).addError(orderField, "advancedGenealogyForOrders.error.orderAcceptedError");
    }

    @Test
    public void shouldInvalidateTrackingRecordIfOrderIsInProgressAndHasUnchangablePlanAfterStart() {
        // given
        given(order.getStringField("trackingRecordTreatment")).willReturn("03unchangablePlanAfterOrderStart");
        given(order.getStringField("state")).willReturn("03inProgress");

        // when
        boolean isTrackingRecordValid = trackingForOrdersModelValidators.checkChoosenOrderState(dataDefinition, trackingRecord);

        // then
        assertFalse(isTrackingRecordValid);
        verify(trackingRecord).addError(orderField, "advancedGenealogyForOrders.error.orderInProgressError");
    }

    @Test
    public void shouldValidateTrackingRecordIfTrackingRecordTreatmentIsSetToDuringProduction() {
        // given
        given(order.getStringField("trackingRecordTreatment")).willReturn("01duringProduction");
        given(order.getStringField("state")).willReturn("03inProgress");

        // when
        boolean isTrackingRecordValid = trackingForOrdersModelValidators.checkChoosenOrderState(dataDefinition, trackingRecord);

        // then
        assertTrue(isTrackingRecordValid);
    }
}
