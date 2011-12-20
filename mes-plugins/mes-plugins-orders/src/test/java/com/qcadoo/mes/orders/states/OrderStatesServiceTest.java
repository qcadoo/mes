/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.1
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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

public class OrderStatesServiceTest {

    private OrderStatesService orderStatesService;

    private ViewDefinitionState view;

    private ComponentState state;

    private FormComponent form;

    private Entity order;

    private DataDefinition dataDefinition;

    private FieldComponent stateFromField, externalSynchronizedState, externalNumber;

    private FieldComponent field;

    private Entity orderFromDB;

    private Locale locale;

    private TranslationService translationService;

    private DataDefinitionService dataDefinitionService;

    private DataDefinition dataDefinitionForBasic;

    private SearchResult searchResult;

    private SearchCriteriaBuilder searchCriteriaBuilder;

    private GridComponent grid;

    @Before
    public void init() {
        orderStatesService = new OrderStatesService();

        order = mock(Entity.class);
        view = mock(ViewDefinitionState.class);
        form = mock(FormComponent.class);
        state = mock(ComponentState.class);
        dataDefinition = mock(DataDefinition.class);
        stateFromField = mock(FieldComponent.class);
        externalSynchronizedState = mock(FieldComponent.class);
        externalNumber = mock(FieldComponent.class);
        dataDefinitionService = mock(DataDefinitionService.class);
        translationService = mock(TranslationService.class);
        field = mock(FieldComponent.class);
        dataDefinition = mock(DataDefinition.class);
        dataDefinitionForBasic = mock(DataDefinition.class);
        orderFromDB = mock(Entity.class);
        searchResult = mock(SearchResult.class);
        searchCriteriaBuilder = mock(SearchCriteriaBuilder.class);
        grid = mock(GridComponent.class);
        // Set<Long> ordersId = mock(Set.class);
        Iterator<Long> iterator = mock(Iterator.class);

        when(order.getDataDefinition()).thenReturn(dataDefinition);
        when(view.getComponentByReference("form")).thenReturn(form);
        when(view.getComponentByReference("state")).thenReturn(stateFromField);
        when(view.getComponentByReference("externalSynchronized")).thenReturn(externalSynchronizedState);
        when(view.getComponentByReference("externalNumber")).thenReturn(externalNumber);
        when(externalNumber.getFieldValue()).thenReturn("1");
        when(form.getEntity()).thenReturn(order);

        when(dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER))
                .thenReturn(dataDefinition);
        when(dataDefinition.get(order.getId())).thenReturn(orderFromDB);
        when(orderFromDB.getStringField("state")).thenReturn("state");
        when(dataDefinition.get(Mockito.anyLong())).thenReturn(orderFromDB);
        when(orderFromDB.getDataDefinition()).thenReturn(dataDefinition);

        when(view.getLocale()).thenReturn(locale);
        when(translationService.translate("orders.order.orderStates.fieldRequired", view.getLocale())).thenReturn("translate");
        when(dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER)).thenReturn(
                dataDefinitionForBasic);
        when(view.getComponentByReference("grid")).thenReturn(grid);
        Set<Long> ordersId = new HashSet<Long>();
        ordersId.add(1L);
        when(grid.getSelectedEntitiesIds()).thenReturn(ordersId);

        for (String reference : Arrays.asList("product", "plannedQuantity", "dateTo", "dateFrom", "defaultTechnology",
                "technology")) {
            when(view.getComponentByReference(reference)).thenReturn(field);
        }
        setField(orderStatesService, "dataDefinitionService", dataDefinitionService);
        setField(orderStatesService, "translationService", translationService);
    }

    @Test
    public void shouldChangeOrderStateToAccepted() throws Exception {
        // when

        orderStatesService.changeOrderStateToAccepted(view, state, new String[0]);
    }

    @Test
    public void shouldChangeOrderStateToInProgressFromAccepted() throws Exception {
        // given
        given(order.getStringField("state")).willReturn(OrderStates.ACCEPTED.getStringValue());
        given(view.getComponentByReference("externalSynchronized")).willReturn(externalSynchronizedState);
        when(view.getComponentByReference("doneQuantity")).thenReturn(field);
        // when
        orderStatesService.changeOrderStateToInProgress(view, state, new String[0]);

    }

    @Test
    public void shouldChangeOrderStateToInProgressFromInterrupted() throws Exception {
        // given
        given(order.getStringField("state")).willReturn(OrderStates.INTERRUPTED.getStringValue());
        given(view.getComponentByReference("externalSynchronized")).willReturn(externalSynchronizedState);
        when(view.getComponentByReference("doneQuantity")).thenReturn(field);
        // when
        orderStatesService.changeOrderStateToInProgress(view, state, new String[0]);
    }

    @Test
    public void shouldChangeOrderStateToCompleted() throws Exception {
        // given

        // when
        when(dataDefinitionForBasic.find()).thenReturn(searchCriteriaBuilder);
        when(searchCriteriaBuilder.setMaxResults(1)).thenReturn(searchCriteriaBuilder);
        when(searchCriteriaBuilder.list()).thenReturn(searchResult);
        orderStatesService.changeOrderStateToCompleted(view, state, new String[0]);
    }

    @Test
    public void shouldChangeOrderStateToDeclinedFromPending() throws Exception {
        // given
        given(order.getStringField("state")).willReturn(OrderStates.PENDING.getStringValue());
        // when
        orderStatesService.changeOrderStateToDeclined(view, state, new String[0]);
    }

    @Test
    public void shouldChangeOrderStateToDeclinedFromAccepted() throws Exception {
        // given
        given(order.getStringField("state")).willReturn(OrderStates.ACCEPTED.getStringValue());
        // when
        orderStatesService.changeOrderStateToDeclined(view, state, new String[0]);
    }

    @Test
    public void shouldChangeOrderStateToAbandonedFromInProgress() throws Exception {
        // given
        given(order.getStringField("state")).willReturn(OrderStates.IN_PROGRESS.getStringValue());
        // when
        orderStatesService.changeOrderStateToAbandoned(view, state, new String[0]);
    }

    @Test
    public void shouldChangeOrderStateToAbandonedFromInterrupted() throws Exception {
        // given
        given(order.getStringField("state")).willReturn(OrderStates.INTERRUPTED.getStringValue());
        // when
        orderStatesService.changeOrderStateToAbandoned(view, state, new String[0]);
    }

    @Test
    public void shouldChangeOrderStateToInterrupted() throws Exception {
        // when
        orderStatesService.changeOrderStateToInterrupted(view, state, new String[0]);
    }

    @Test
    public void shouldChangeOrderStateToAcceptedForGrid() throws Exception {
        // when
        orderStatesService.changeOrderStateToAcceptedForGrid(view, state, new String[0]);
    }

    @Test
    public void shouldChangeOrderStateToInProgressForGrid() throws Exception {
        // when
        orderStatesService.changeOrderStateToInProgressForGrid(view, state, new String[0]);
        // then
    }

    @Test
    public void shouldChangeOrderStateToCompletedForGrid() throws Exception {
        // when
        when(dataDefinitionForBasic.find()).thenReturn(searchCriteriaBuilder);
        when(searchCriteriaBuilder.setMaxResults(1)).thenReturn(searchCriteriaBuilder);
        when(searchCriteriaBuilder.list()).thenReturn(searchResult);
        orderStatesService.changeOrderStateToCompletedForGrid(view, state, new String[0]);
    }

    @Test
    public void shouldChangeOrderStateToDeclinedForGrid() throws Exception {
        // when
        orderStatesService.changeOrderStateToDeclinedForGrid(view, state, new String[0]);
    }

    @Test
    public void shouldChangeOrderStateToAbandonedForGrid() throws Exception {
        // when
        orderStatesService.changeOrderStateToAbandonedForGrid(view, state, new String[0]);
    }

    @Test
    public void shouldChangeOrderStateToInterruptedForGrid() throws Exception {
        // when
        orderStatesService.changeOrderStateToInterruptedForGrid(view, state, new String[0]);
    }

}
