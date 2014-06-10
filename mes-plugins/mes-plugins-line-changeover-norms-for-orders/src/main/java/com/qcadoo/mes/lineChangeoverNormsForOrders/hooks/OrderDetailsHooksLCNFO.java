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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Predicate;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.mes.orders.util.OrderDetailsRibbonHelper;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class OrderDetailsHooksLCNFO {

    private static final Predicate<Entity> HAS_PATTERN_TECHNOLOGY = new Predicate<Entity>() {

        @Override
        public boolean apply(final Entity order) {
            if (order == null) {
                return false;
            }
            OrderType orderType = OrderType.of(order);
            if (orderType != OrderType.WITH_PATTERN_TECHNOLOGY) {
                return false;
            }
            Entity patternTechnology = order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);
            return patternTechnology != null;
        }
    };

    @Autowired
    private OrderDetailsRibbonHelper orderDetailsRibbonHelper;

    public void onBeforeRender(final ViewDefinitionState view) {
        enableOrDisableChangeoverButton(view);
    }

    private void enableOrDisableChangeoverButton(final ViewDefinitionState view) {
        orderDetailsRibbonHelper.setButtonEnabled(view, "changeover", "showChangeover", HAS_PATTERN_TECHNOLOGY);
    }

}
