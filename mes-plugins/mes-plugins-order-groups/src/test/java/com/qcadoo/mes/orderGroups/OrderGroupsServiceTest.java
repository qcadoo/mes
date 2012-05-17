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
package com.qcadoo.mes.orderGroups;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

public class OrderGroupsServiceTest {

    private OrderGroupsService orderGroupsService;

    private Entity order = null;

    private Entity orderGroup = null;

    private EntityList ordersList = null;

    private final long now = new Date().getTime();

    private DataDefinition dataDefinition = null;

    private ViewDefinitionState view = null;

    private ComponentState numberComponent = null;

    private ComponentState nameComponent = null;

    private NumberGeneratorService numberGeneratorService = null;

    private TranslationService translationService = null;

    @Before
    public final void init() {
        orderGroupsService = new OrderGroupsService();

        mockModel();
        mockView();

        ReflectionTestUtils.setField(orderGroupsService, "translationService", translationService);
        ReflectionTestUtils.setField(orderGroupsService, "numberGeneratorService", numberGeneratorService);
    }

    private void mockView() {
        numberComponent = mock(ComponentState.class);
        nameComponent = mock(ComponentState.class);
        view = mock(ViewDefinitionState.class);
        numberGeneratorService = mock(NumberGeneratorService.class);
        translationService = mock(TranslationService.class);
        when(view.getComponentByReference("number")).thenReturn(numberComponent);
        when(view.getComponentByReference("name")).thenReturn(nameComponent);
    }

    @SuppressWarnings("unchecked")
    private void mockModel() {
        orderGroup = mock(Entity.class);
        order = mock(Entity.class);
        ordersList = mock(EntityList.class);
        Iterator<Entity> ordersListIterator = mock(Iterator.class);
        dataDefinition = mock(DataDefinition.class);

        when(order.getBelongsToField("orderGroup")).thenReturn(orderGroup);
        when(order.getDataDefinition()).thenReturn(dataDefinition);
        when(orderGroup.getDataDefinition()).thenReturn(dataDefinition);
        when(ordersListIterator.hasNext()).thenReturn(true, true, true, false);
        when(ordersListIterator.next()).thenReturn(order, order, order);
        when(ordersList.iterator()).thenReturn(ordersListIterator);
        when(ordersList.size()).thenReturn(3);
        when(ordersList.isEmpty()).thenReturn(false);
        when(orderGroup.getHasManyField("orders")).thenReturn(ordersList);
    }

    @Test
    public final void shouldReturnTrueIfBounaryContainsGivenDate() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 10, 20);
        mockEntityDateRange(order, 11, 19);

        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(orderGroup, ordersList, "", orderGroup);

        // then
        assertTrue(result);
    }

    @Test
    public final void shouldReturnTrueIfLowerBounaryIsNotSetAndUpperContainsEndDate() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 0, 20);
        mockEntityDateRange(order, 1, 19);

        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(orderGroup, ordersList, "", orderGroup);

        // then
        assertTrue(result);
    }

    @Test
    public final void shouldReturnTrueIfUpperBounaryIsNotSetAndLowerContainsStartDate() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 10, 0);
        mockEntityDateRange(order, 11, 30);

        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(orderGroup, ordersList, "", orderGroup);

        // then
        assertTrue(result);
    }

    @Test
    public final void shouldReturnFalseIfUpperBounaryIsExceeded() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 10, 20);
        mockEntityDateRange(order, 11, 30);

        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(orderGroup, ordersList, "", orderGroup);

        // then
        assertFalse(result);
    }

    @Test
    public final void shouldReturnFalseIfLowerBounaryIsExceeded() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 10, 20);
        mockEntityDateRange(order, 9, 19);

        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(orderGroup, ordersList, "", orderGroup);

        // then
        assertFalse(result);
    }

    @Test
    public final void shouldReturnTrueIfBoundariesAreEquals() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 10, 20);
        mockEntityDateRange(order, 10, 20);

        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(orderGroup, ordersList, "", orderGroup);

        // then
        assertTrue(result);
    }

    @Test
    public final void shouldReturnTrueIfBoundariesIsTheSameOneDay() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 10, 10);
        mockEntityDateRange(order, 10, 10);

        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(orderGroup, ordersList, "", orderGroup);

        // then
        assertTrue(result);
    }

    @Test
    public final void shouldReturnFalseIfBounariesAreExceeded() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 10, 20);
        mockEntityDateRange(order, 1, 30);

        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(orderGroup, ordersList, "", orderGroup);

        // then
        assertFalse(result);
    }

    @Test
    public final void shouldReturnTrueIfGroupDateBoundariesAreCorrect() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 10, 20);
        mockEntityDateRange(order, 14, 16);

        // when
        boolean result = orderGroupsService.validateDates(dataDefinition, orderGroup);

        // then
        assertTrue(result);
    }

    @Test
    public final void shouldReturnTrueIfGroupIsNull() throws Exception {
        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(null, ordersList, "", orderGroup);

        // then
        assertTrue(result);
    }

    @Test
    public final void shouldReturnTrueIfOrdersIsEmpty() throws Exception {
        // given
        when(ordersList.size()).thenReturn(0);
        when(ordersList.isEmpty()).thenReturn(true);

        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(orderGroup, ordersList, "", orderGroup);

        // then
        assertTrue(result);
    }

    @Test
    public final void shouldReturnFalseIfGroupDateBoundariesAreIncorrect() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 50, 10);
        mockEntityDateRange(order, 14, 16);

        // when
        boolean result = orderGroupsService.validateDates(dataDefinition, orderGroup);

        // then
        assertFalse(result);
    }

    @Test
    public final void shouldFireValidatorAndReturnFalse() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 10, 20);
        mockEntityDateRange(order, 9, 46);

        // when
        boolean result = orderGroupsService.validateOrderDate(dataDefinition, order);

        // then
        assertFalse(result);
    }

    @Test
    public final void shouldFireValidatorAndReturnTrue() throws Exception {
        // given
        mockEntityDateRange(orderGroup, 10, 40);
        mockEntityDateRange(order, 14, 16);

        // when
        boolean result = orderGroupsService.validateOrderDate(dataDefinition, order);

        // then
        assertTrue(result);
    }

    @Test
    public final void shouldReturnTrueIfOrdersIsNull() throws Exception {
        // when
        boolean result = orderGroupsService.checkOrderGroupDateBoundary(orderGroup, null, "", orderGroup);

        // then
        Assert.assertTrue(result);
    }

    private void mockEntityDateRange(final Entity entity, final int daysFrom, final int daysTo) {
        when(entity.getField("dateFrom")).thenReturn(getDateWithTimeInterval(daysFrom));
        when(entity.getField("dateTo")).thenReturn(getDateWithTimeInterval(daysTo));
    }

    private Object getDateWithTimeInterval(final int additionalDays) {
        Calendar cal = Calendar.getInstance();
        if (additionalDays == 0) {
            return null;
        } else {
            cal.setTimeInMillis(now);
            cal.add(Calendar.DAY_OF_MONTH, additionalDays);
        }
        return cal.getTime();
    }

    @Test
    public final void shouldNotGenerateNumberAndNameForAlreadyNumberedGroup() throws Exception {
        // given
        when(numberComponent.getFieldValue()).thenReturn("I was been here ...");
        when(nameComponent.getFieldValue()).thenReturn("... and also here :)");

        // when
        orderGroupsService.generateNumberAndName(view);

        // then
        Mockito.verify(numberGeneratorService, Mockito.never()).generateAndInsertNumber(
                (ViewDefinitionState) Mockito.anyObject(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.eq("number"));
        Mockito.verify(nameComponent, Mockito.never()).setFieldValue(Mockito.anyObject());
    }

    @Test
    public final void shouldGenerateNumberAndNameIfDoesNotExists() throws Exception {
        // given
        when(numberComponent.getFieldValue()).thenReturn("");
        when(nameComponent.getFieldValue()).thenReturn("");

        // when
        orderGroupsService.generateNumberAndName(view);

        // then
        Mockito.verify(numberGeneratorService, Mockito.atLeastOnce()).generateAndInsertNumber(
                (ViewDefinitionState) Mockito.anyObject(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.eq("number"));
        Mockito.verify(nameComponent, Mockito.atLeastOnce()).setFieldValue(Mockito.anyObject());
    }

    @Test
    public final void shouldGenerateOnlyNumberIfNameAlreadyExists() throws Exception {
        // given
        when(numberComponent.getFieldValue()).thenReturn("");
        when(nameComponent.getFieldValue()).thenReturn("Qcadoo Framework RLZ!");

        // when
        orderGroupsService.generateNumberAndName(view);

        // then
        Mockito.verify(numberGeneratorService, Mockito.atLeastOnce()).generateAndInsertNumber(
                (ViewDefinitionState) Mockito.anyObject(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.eq("number"));
        Mockito.verify(nameComponent, Mockito.never()).setFieldValue(Mockito.anyObject());
    }

    @Ignore
    @Test
    public final void shouldChangeOrderGroupName() throws Exception {
        // given
        final String groupName = "any group name";
        when(orderGroup.getStringField("name")).thenReturn(groupName);
        ArgumentCaptor<String> argCaptor = ArgumentCaptor.forClass(String.class);

        // when
        orderGroupsService.updateOrderGroupName(dataDefinition, order);

        // then
        Mockito.verify(order, Mockito.times(1)).setField(Mockito.eq("orderGroupName"), argCaptor.capture());
        Assert.assertEquals(groupName, argCaptor.getValue());
    }

    @Test
    public final void shouldSetOrderGroupNameToNullIfOrderIsNotAssociatedWithAnyGroup() throws Exception {
        // given
        DataDefinition orderDataDefinition = mock(DataDefinition.class);
        when(order.getBelongsToField("orderGroup")).thenReturn(null);
        ArgumentCaptor<String> argCaptor = ArgumentCaptor.forClass(String.class);

        // when
        orderGroupsService.updateOrderGroupName(orderDataDefinition, order);

        // then
        Mockito.verify(order, Mockito.times(1)).setField(Mockito.eq("orderGroupName"), argCaptor.capture());
        Assert.assertEquals(null, argCaptor.getValue());
    }

    @Test
    public final void shouldSaveBelongingOrdersWhenSavingGroup() throws Exception {
        // given
        when(orderGroup.getHasManyField("orders")).thenReturn(ordersList);
        when(order.getDataDefinition()).thenReturn(dataDefinition);

        // when
        orderGroupsService.updateBelongingOrdersOrderGroupName(dataDefinition, orderGroup);

        // then
        Mockito.verify(dataDefinition, Mockito.times(ordersList.size())).save(order);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public final void shouldRedirectToOrdersList() throws Exception {
        // given
        ViewDefinitionState view = mock(ViewDefinitionState.class);
        FormComponent form = mock(FormComponent.class);

        when(view.getComponentByReference("form")).thenReturn(form);
        when(form.getEntity()).thenReturn(orderGroup);
        when(orderGroup.getStringField("name")).thenReturn("Some group name");

        // when
        orderGroupsService.showInOrdersList(view, form, null);

        // then
        Mockito.verify(view).redirectTo(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean(),
                (Map) Mockito.anyObject());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public final void shouldNotRedirectToOrdersListIfFormEntityIsNull() throws Exception {
        // given
        ViewDefinitionState view = mock(ViewDefinitionState.class);
        FormComponent form = mock(FormComponent.class);

        when(view.getComponentByReference("form")).thenReturn(form);

        // when
        orderGroupsService.showInOrdersList(view, form, null);

        // then
        Mockito.verify(view, Mockito.never()).redirectTo(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean(),
                (Map) Mockito.anyObject());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public final void shouldNotRedirectToOrdersListIfGroupNameIsNull() throws Exception {
        // given
        ViewDefinitionState view = mock(ViewDefinitionState.class);
        FormComponent form = mock(FormComponent.class);

        when(view.getComponentByReference("form")).thenReturn(form);
        when(form.getEntity()).thenReturn(orderGroup);

        // when
        orderGroupsService.showInOrdersList(view, form, null);

        // then
        Mockito.verify(view, Mockito.never()).redirectTo(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean(),
                (Map) Mockito.anyObject());
    }
}
