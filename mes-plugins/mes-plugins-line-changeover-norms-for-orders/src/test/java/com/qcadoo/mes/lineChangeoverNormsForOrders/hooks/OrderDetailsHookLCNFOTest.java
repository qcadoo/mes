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
package com.qcadoo.mes.lineChangeoverNormsForOrders.hooks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.base.Predicate;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.mes.orders.util.OrderDetailsRibbonHelper;
import com.qcadoo.model.api.Entity;
import com.qcadoo.testing.model.EntityTestUtils;
import com.qcadoo.view.api.ViewDefinitionState;

public class OrderDetailsHookLCNFOTest {

    private OrderDetailsHooksLCNFO orderDetailsHooksLCNFO;

    @Mock
    private OrderDetailsRibbonHelper orderDetailsRibbonHelper;

    @Mock
    private Entity order, technologyPrototype;

    @Mock
    private ViewDefinitionState view;

    @Captor
    private ArgumentCaptor<Predicate<Entity>> predicateCaptor;

    @Before
    public void init() {
        orderDetailsHooksLCNFO = new OrderDetailsHooksLCNFO();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(orderDetailsHooksLCNFO, "orderDetailsRibbonHelper", orderDetailsRibbonHelper);
    }

    private void stubOrderType(final OrderType type) {
        EntityTestUtils.stubStringField(order, OrderFields.ORDER_TYPE, type.getStringValue());
    }

    private void stubTechnologyPrototype(final Entity technology) {
        EntityTestUtils.stubBelongsToField(order, OrderFields.TECHNOLOGY_PROTOTYPE, technology);
    }

    @Test
    public final void shouldDelegateToOrderDetailsRibbonHelperAndUseValidPredicate() {
        // when
        orderDetailsHooksLCNFO.onBeforeRender(view);

        // then
        verify(orderDetailsRibbonHelper).setButtonEnabled(any(ViewDefinitionState.class), eq("changeover"), eq("showChangeover"),
                predicateCaptor.capture());
        Predicate<Entity> predicate = predicateCaptor.getValue();

        assertFalse(predicate.apply(null));

        stubOrderType(OrderType.WITH_OWN_TECHNOLOGY);
        stubTechnologyPrototype(null);
        assertFalse(predicate.apply(order));

        stubOrderType(OrderType.WITH_OWN_TECHNOLOGY);
        stubTechnologyPrototype(technologyPrototype);
        assertFalse(predicate.apply(order));

        stubOrderType(OrderType.WITH_PATTERN_TECHNOLOGY);
        stubTechnologyPrototype(null);
        assertFalse(predicate.apply(order));

        stubOrderType(OrderType.WITH_PATTERN_TECHNOLOGY);
        stubTechnologyPrototype(technologyPrototype);
        assertTrue(predicate.apply(order));
    }

}
