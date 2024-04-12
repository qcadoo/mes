/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.masterOrders.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.testing.model.EntityListMock;
import junit.framework.Assert;
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

import java.util.List;

import static com.qcadoo.testing.model.EntityTestUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class MasterOrderHooksTest {

    private static final Long MASTER_ORDER_ID = 1L;

    private MasterOrderHooks masterOrderHooks;

    @Mock
    private Entity masterOrder, order1, order2, customer, parameter;

    @Mock
    private ParameterService parameterService;

    @Captor
    private ArgumentCaptor<List<Entity>> entityListCaptor;

    @Before
    public void init() {
        masterOrderHooks = new MasterOrderHooks();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(masterOrderHooks, "parameterService", parameterService);

        PowerMockito.mockStatic(SearchRestrictions.class);

        given(parameterService.getParameter()).willReturn(parameter);
    }

    @Test
    public final void shouldSetExternalSynchronized() {
        // when
        masterOrderHooks.setExternalSynchronizedField(masterOrder);
        // then
        verify(masterOrder).setField(MasterOrderFields.EXTERNAL_SYNCHRONIZED, true);

    }

    @Test
    public final void shouldThrowExceptionWhenMasterOrderIsNotSave() {
        // given
        stubId(masterOrder, null);

        try {
            // when
            masterOrderHooks.changedDeadlineAndInOrder(masterOrder);
            Assert.fail();
        } catch (IllegalArgumentException ignored) {
            // then
            verify(masterOrder, never()).setField(MasterOrderFields.ORDERS, Lists.newArrayList());
        }
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

    private static EntityList mockEntityList(final List<Entity> entities) {
        return EntityListMock.create(entities);
    }
}
