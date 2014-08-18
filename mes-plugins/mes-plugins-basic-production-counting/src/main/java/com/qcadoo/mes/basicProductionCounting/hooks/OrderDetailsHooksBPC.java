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
package com.qcadoo.mes.basicProductionCounting.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Predicate;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.orders.util.OrderDetailsRibbonHelper;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class OrderDetailsHooksBPC {

    private static final Predicate<Entity> NOT_DECLINED_OR_PENDING = new Predicate<Entity>() {

        @Override
        public boolean apply(final Entity order) {
            if (order == null) {
                return false;
            }
            OrderState orderState = OrderState.of(order);
            return orderState != OrderState.DECLINED && orderState != OrderState.PENDING;
        }
    };

    @Autowired
    private OrderDetailsRibbonHelper orderDetailsRibbonHelper;

    public void disabledButtonForAppropriateState(final ViewDefinitionState view) {
        orderDetailsRibbonHelper.setButtonEnabled(view, BasicProductionCountingConstants.VIEW_RIBBON_ACTION_ITEM_GROUP,
                BasicProductionCountingConstants.VIEW_RIBBON_ACTION_ITEM_NAME, NOT_DECLINED_OR_PENDING);
    }

}
