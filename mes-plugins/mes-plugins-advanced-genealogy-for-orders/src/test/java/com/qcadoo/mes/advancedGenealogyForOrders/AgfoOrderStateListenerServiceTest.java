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
package com.qcadoo.mes.advancedGenealogyForOrders;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public class AgfoOrderStateListenerServiceTest {

    private AgfoOrderStateListenerService advancedGenealogyForOrdersOrderStateListener;

    @Mock
    private Entity order, trackingRecord;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private StateChangeContext stateChangeContext;

    private static EntityList mockEntityListIterator(final List<Entity> list) {
        EntityList entityList = mock(EntityList.class);
        when(entityList.iterator()).thenReturn(list.iterator());
        return entityList;
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        advancedGenealogyForOrdersOrderStateListener = new AgfoOrderStateListenerService();

        given(order.getDataDefinition()).willReturn(dataDefinition);
        given(order.getId()).willReturn(1L);
        given(dataDefinition.get(1L)).willReturn(order);

        EntityList trackingRecords = mockEntityListIterator(asList(trackingRecord));
        given(order.getHasManyField("trackingRecords")).willReturn(trackingRecords);

        given(stateChangeContext.getOwner()).willReturn(order);
    }

    @Test
    public void shouldNotAddErrorsUponAcceptingIfTrackingRecordTreatmentIsntUnchangablePlanAfterOrderAccept() {
        // given
        given(order.getStringField("trackingRecordTreatment")).willReturn("01duringProduction");

        // when
        advancedGenealogyForOrdersOrderStateListener.onAccepted(stateChangeContext);

        // then
        verify(stateChangeContext, never()).addMessage(Mockito.anyString(), Mockito.any(StateMessageType.class));
        verify(stateChangeContext, never()).addMessage(Mockito.anyString(), Mockito.any(StateMessageType.class),
                Mockito.any(String[].class));
    }

    @Test
    public void shouldNotAddErrorsUponAcceptingIfTrackingRecordTreatmentIsUnchangablePlanAfterOrderAcceptAndIfIfItHasAcceptedTrackingRecords() {
        // given
        given(order.getStringField("trackingRecordTreatment")).willReturn("02unchangablePlanAfterOrderAccept");
        given(trackingRecord.getStringField("state")).willReturn("02accepted");

        // when
        advancedGenealogyForOrdersOrderStateListener.onAccepted(stateChangeContext);

        // then
        verify(stateChangeContext, never()).addMessage(Mockito.anyString(), Mockito.any(StateMessageType.class));
        verify(stateChangeContext, never()).addMessage(Mockito.anyString(), Mockito.any(StateMessageType.class),
                Mockito.any(String[].class));
    }

    @Test
    public void shouldAddErrorsUponAcceptingIfTrackingRecordTreatmentIsUnchangablePlanAfterOrderAcceptAndItDoesntHaveAcceptedTrackingRecords() {
        // given
        given(order.getStringField("trackingRecordTreatment")).willReturn("02unchangablePlanAfterOrderAccept");
        given(trackingRecord.getStringField("state")).willReturn("01draft");

        // when
        advancedGenealogyForOrdersOrderStateListener.onAccepted(stateChangeContext);

        // then
        verify(stateChangeContext).addMessage(Mockito.anyString(), Mockito.any(StateMessageType.class));
        verify(stateChangeContext).addMessage(Mockito.eq("orders.order.trackingRecords.atLeastOneDraftOrNoneAcceptedError"),
                Mockito.any(StateMessageType.class));
    }

    @Test
    public void shouldNotAddErrorsUponStartingIfTrackingRecordTreatmentIsntUnchangablePlanAfterOrderStart() {
        // given
        given(order.getStringField("trackingRecordTreatment")).willReturn("01duringProduction");

        // when
        advancedGenealogyForOrdersOrderStateListener.onInProgress(stateChangeContext);

        // then
        verify(stateChangeContext, never()).addMessage(Mockito.anyString(), Mockito.any(StateMessageType.class));
        verify(stateChangeContext, never()).addMessage(Mockito.anyString(), Mockito.any(StateMessageType.class),
                Mockito.any(String[].class));
    }

    @Test
    public void shouldNotAddErrorsUponStartingIfTrackingRecordTreatmentIsUnchangablePlanAfterOrderStartAndIfIfItHasAcceptedTrackingRecords() {
        // given
        given(order.getStringField("trackingRecordTreatment")).willReturn("03unchangablePlanAfterOrderStart");
        given(trackingRecord.getStringField("state")).willReturn("02accepted");

        // when
        advancedGenealogyForOrdersOrderStateListener.onInProgress(stateChangeContext);

        // then
        verify(stateChangeContext, never()).addMessage(Mockito.anyString(), Mockito.any(StateMessageType.class));
        verify(stateChangeContext, never()).addMessage(Mockito.anyString(), Mockito.any(StateMessageType.class),
                Mockito.any(String[].class));
    }

    @Test
    public void shouldAddErrorsUponStartingingIfTrackingRecordTreatmentIsUnchangablePlanAfterOrderStartAndItDoesntHaveAcceptedTrackingRecords() {
        // given
        given(order.getStringField("trackingRecordTreatment")).willReturn("03unchangablePlanAfterOrderStart");
        given(trackingRecord.getStringField("state")).willReturn("01draft");

        // when
        advancedGenealogyForOrdersOrderStateListener.onInProgress(stateChangeContext);

        // then
        verify(stateChangeContext).addMessage(Mockito.anyString(), Mockito.any(StateMessageType.class));
        verify(stateChangeContext).addMessage(Mockito.eq("orders.order.trackingRecords.atLeastOneDraftOrNoneAcceptedError"),
                Mockito.any(StateMessageType.class));
    }
}
