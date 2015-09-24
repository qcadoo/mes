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
package com.qcadoo.mes.workPlans.hooks;

import static com.qcadoo.testing.model.EntityTestUtils.mockEntity;
import static com.qcadoo.testing.model.EntityTestUtils.stubBooleanField;
import static com.qcadoo.testing.model.EntityTestUtils.stubHasManyField;
import static com.qcadoo.testing.model.EntityTestUtils.stubStringField;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.workPlans.constants.WorkPlanFields;
import com.qcadoo.mes.workPlans.view.WorkPlansListView;
import com.qcadoo.model.api.Entity;

public class WorkPlansListHooksTest {

    private WorkPlansListHooks workPlansListHooks;

    @Mock
    private WorkPlansListView workPlansListView;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        workPlansListHooks = new WorkPlansListHooks();
        stubGridSelectedEntities();
    }

    private void stubGridSelectedEntities(final Entity... workPlanEntities) {
        given(workPlansListView.getSelectedWorkPlans()).willAnswer(new Answer<List<Entity>>() {

            @Override
            public List<Entity> answer(final InvocationOnMock invocation) throws Throwable {
                return ImmutableList.copyOf(workPlanEntities);
            }
        });
    }

    private Entity mockWorkPlanEntity(final boolean isGenerated, final OrderState... belongingOrderStates) {
        Entity workPlan = mockEntity();
        stubBooleanField(workPlan, WorkPlanFields.GENERATED, isGenerated);
        List<Entity> orders = FluentIterable.from(Arrays.asList(belongingOrderStates)).transform(MOCK_ORDER_WITH_STATE).toList();
        stubHasManyField(workPlan, WorkPlanFields.ORDERS, orders);
        return workPlan;
    }

    private static final Function<OrderState, Entity> MOCK_ORDER_WITH_STATE = new Function<OrderState, Entity>() {

        @Override
        public Entity apply(final OrderState orderState) {
            Entity order = mockEntity();
            stubStringField(order, OrderFields.STATE, orderState.getStringValue());
            return order;
        }
    };

    @Test
    public final void shouldEnableDeleteButton() {
        // given
        stubGridSelectedEntities(mockWorkPlanEntity(false, OrderState.PENDING, OrderState.PENDING),
                mockWorkPlanEntity(false, OrderState.ACCEPTED, OrderState.COMPLETED),
                mockWorkPlanEntity(true, OrderState.ACCEPTED, OrderState.IN_PROGRESS));

        // when
        workPlansListHooks.setGridGenerateButtonState(workPlansListView);

        // then
        verify(workPlansListView).setUpDeleteButton(true, null);
    }

    @Test
    public final void shouldDisableDeleteButtonWhenNoneEntityIsSelected() {
        // given
        stubGridSelectedEntities();

        // when
        workPlansListHooks.setGridGenerateButtonState(workPlansListView);

        // then
        verify(workPlansListView).setUpDeleteButton(false, null);
    }

    @Test
    public final void shouldDisableDeleteButtonWhenAnyOfWorkPlansIsGeneratedAndRelatesToAtLeastOneCompletedOrder() {
        // given
        stubGridSelectedEntities(mockWorkPlanEntity(false, OrderState.PENDING, OrderState.PENDING, OrderState.COMPLETED),
                mockWorkPlanEntity(true, OrderState.ABANDONED, OrderState.COMPLETED));

        // when
        workPlansListHooks.setGridGenerateButtonState(workPlansListView);

        // then
        verify(workPlansListView).setUpDeleteButton(false, "orders.ribbon.message.selectedRecordCannotBeDeleted");
    }

}
