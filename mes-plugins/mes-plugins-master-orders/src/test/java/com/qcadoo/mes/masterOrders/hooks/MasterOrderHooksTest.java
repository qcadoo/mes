/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.masterOrders.hooks;

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.EntityTestUtils.stubBelongsToField;
import static com.qcadoo.testing.model.EntityTestUtils.stubDateField;
import static com.qcadoo.testing.model.EntityTestUtils.stubHasManyField;
import static com.qcadoo.testing.model.EntityTestUtils.stubId;
import static com.qcadoo.testing.model.EntityTestUtils.stubStringField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderType;
import com.qcadoo.mes.masterOrders.util.MasterOrderOrdersDataProvider;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.testing.model.EntityListMock;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class MasterOrderHooksTest {

    private static final Long MASTER_ORDER_ID = 1L;

    private MasterOrderHooks masterOrderHooks;

    @Mock
    private DataDefinition masterOrderDD, orderDD;

    @Mock
    private Entity masterOrder, product, order1, order2, customer;

    @Mock
    private MasterOrderOrdersDataProvider masterOrderOrdersDataProvider;

    @Captor
    private ArgumentCaptor<List<Entity>> entityListCaptor;

    @Before
    public void init() {
        masterOrderHooks = new MasterOrderHooks();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(masterOrderHooks, "masterOrderOrdersDataProvider", masterOrderOrdersDataProvider);

        PowerMockito.mockStatic(SearchRestrictions.class);
    }

    @Test
    public final void shouldReturnWhenMasterOrderDoesnotSave() {
        // given
        stubId(masterOrder, null);
        // when
        masterOrderHooks.calculateCumulativeQuantityFromOrders(masterOrder);
        // then
        verify(masterOrder, never()).setField(MasterOrderFields.CUMULATED_ORDER_QUANTITY, BigDecimal.ONE);
    }

    @Test
    public final void shouldReturnWhenMasterOrderTypeIsIncorrect() {
        // given
        stubId(masterOrder, MASTER_ORDER_ID);
        stubMasterOrderType(MasterOrderType.UNDEFINED);
        // when
        masterOrderHooks.calculateCumulativeQuantityFromOrders(masterOrder);
        // then
        verify(masterOrder, never()).setField(MasterOrderFields.CUMULATED_ORDER_QUANTITY, BigDecimal.ONE);
    }

    @Test
    public final void shouldSetCumulatedQuantity() {
        // given
        stubId(masterOrder, MASTER_ORDER_ID);
        MasterOrderType masterOrderType = MasterOrderType.ONE_PRODUCT;
        stubMasterOrderType(masterOrderType);
        stubBelongsToField(masterOrder, MasterOrderFields.PRODUCT, product);

        BigDecimal quantitiesSum = BigDecimal.valueOf(20L);
        stubOrdersPlannedQuantitiesSum(masterOrder, quantitiesSum);

        // when
        masterOrderHooks.calculateCumulativeQuantityFromOrders(masterOrder);

        // then
        verify(masterOrder).setField(MasterOrderFields.CUMULATED_ORDER_QUANTITY, new BigDecimal(20));
    }

    @Test
    public final void shouldSetZeroWhenOrderDoesnotExists() {
        // given
        stubId(masterOrder, MASTER_ORDER_ID);
        stubMasterOrderType(MasterOrderType.ONE_PRODUCT);
        stubBelongsToField(masterOrder, MasterOrderFields.PRODUCT, product);

        stubOrdersPlannedQuantitiesSum(masterOrder, BigDecimal.ZERO);

        // when
        masterOrderHooks.calculateCumulativeQuantityFromOrders(masterOrder);

        // then
        verify(masterOrder).setField(MasterOrderFields.CUMULATED_ORDER_QUANTITY, BigDecimal.ZERO);

    }

    @Test
    public final void shouldSetExternalSynchronized() {
        // when
        masterOrderHooks.setExternalSynchronizedField(masterOrder);
        // then
        verify(masterOrder).setField(MasterOrderFields.EXTERNAL_SYNCHRONIZED, true);

    }

    @Test
    public final void shouldReturnWhenMasterOrderIsNotSave() {
        // given
        stubId(masterOrder, null);
        // when
        masterOrderHooks.changedDeadlineAndInOrder(masterOrder);
        // then
        verify(masterOrder, never()).setField(MasterOrderFields.ORDERS, Lists.newArrayList());
    }

    @Test
    public final void shouldReturnWhenDeadlineIsNull() {
        // given
        stubId(masterOrder, MASTER_ORDER_ID);
        stubDateField(masterOrder, MasterOrderFields.DEADLINE, null);
        stubDateField(masterOrder, MasterOrderFields.COMPANY, null);
        // when
        masterOrderHooks.changedDeadlineAndInOrder(masterOrder);
        // then
        verify(masterOrder, never()).setField(MasterOrderFields.ORDERS, Lists.newArrayList());

    }

    @Test
    public final void shouldSetDeadline() {
        // given
        DateTime now = DateTime.now();

        stubId(masterOrder, MASTER_ORDER_ID);
        stubDateField(masterOrder, MasterOrderFields.DEADLINE, now.toDate());

        stubStringField(order1, OrderFields.STATE, OrderState.PENDING.getStringValue());
        stubStringField(order2, OrderFields.STATE, OrderState.IN_PROGRESS.getStringValue());

        stubDateField(order1, OrderFields.DEADLINE, now.plusHours(6).toDate());

        EntityList orders = mockEntityList(Lists.newArrayList(order1, order2));
        stubHasManyField(masterOrder, MasterOrderFields.ORDERS, orders);

        // when
        masterOrderHooks.changedDeadlineAndInOrder(masterOrder);
        // then
        verify(masterOrder).setField(eq(MasterOrderFields.ORDERS), entityListCaptor.capture());
        List<Entity> actualOrders = entityListCaptor.getValue();
        assertEquals(2, actualOrders.size());
        assertTrue(actualOrders.contains(order1));
        assertTrue(actualOrders.contains(order2));

    }

    @Test
    public final void shouldReturnWhenMasterOrderDoesNotHaveId() {
        // given
        given(masterOrder.getId()).willReturn(null);
        // when
        masterOrderHooks.changedDeadlineAndInOrder(masterOrder);
        // then
        verify(masterOrder, never()).setField(MasterOrderFields.ORDERS, Lists.newArrayList());
    }

    @Test
    public final void shouldReturnWhenDeadlineInMasterOrderIsNull() {
        // given
        stubId(masterOrder, MASTER_ORDER_ID);
        given(masterOrder.getBelongsToField(MasterOrderFields.COMPANY)).willReturn(null);
        // when
        masterOrderHooks.changedDeadlineAndInOrder(masterOrder);
        // then
        verify(masterOrder, never()).setField(MasterOrderFields.ORDERS, Lists.newArrayList());

    }

    @Test
    public final void shouldSetCustomer() {
        // given
        stubId(masterOrder, MASTER_ORDER_ID);
        stubBelongsToField(masterOrder, MasterOrderFields.COMPANY, customer);

        stubStringField(order1, OrderFields.STATE, OrderState.PENDING.getStringValue());
        stubStringField(order2, OrderFields.STATE, OrderState.IN_PROGRESS.getStringValue());
        Entity yetAnotherCustomer = mockEntity();
        stubBelongsToField(order1, OrderFields.COMPANY, yetAnotherCustomer);

        EntityList orders = mockEntityList(Lists.newArrayList(order1, order2));
        given(masterOrder.getHasManyField(MasterOrderFields.ORDERS)).willReturn(orders);

        // when
        masterOrderHooks.changedDeadlineAndInOrder(masterOrder);

        // then
        verify(masterOrder).setField(eq(MasterOrderFields.ORDERS), entityListCaptor.capture());
        List<Entity> actualOrders = entityListCaptor.getValue();
        assertEquals(2, actualOrders.size());
        assertTrue(actualOrders.contains(order1));
        assertTrue(actualOrders.contains(order2));
    }

    private void stubMasterOrderType(final MasterOrderType masterOrderType) {
        stubStringField(masterOrder, MasterOrderFields.MASTER_ORDER_TYPE, masterOrderType.getStringValue());
    }

    private void stubOrdersPlannedQuantitiesSum(final Entity masterOrder, final BigDecimal quantitiesSum) {
        given(masterOrderOrdersDataProvider.sumBelongingOrdersPlannedQuantities(eq(masterOrder), any(Entity.class))).willReturn(
                quantitiesSum);
    }

    private static EntityList mockEntityList(final List<Entity> entities) {
        return EntityListMock.create(entities);
    }
}
