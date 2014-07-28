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
package com.qcadoo.mes.productionPerShift.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.util.OrderDetailsRibbonHelper;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class OrderDetailsHooksPPS {

    private static final Predicate<Entity> HAS_DEFINED_PLANNED_START_DATE = new Predicate<Entity>() {

        @Override
        public boolean apply(final Entity order) {
            return order.getDateField(OrderFields.DATE_FROM) != null;
        }
    };

    @Autowired
    private OrderDetailsRibbonHelper orderDetailsRibbonHelper;

    public void onBeforeRender(final ViewDefinitionState view) {
        Predicate<Entity> predicate = Predicates.and(HAS_DEFINED_PLANNED_START_DATE,
                OrderDetailsRibbonHelper.HAS_CHECKED_OR_ACCEPTED_TECHNOLOGY);
        orderDetailsRibbonHelper.setButtonEnabled(view, "orderProgressPlans", "productionPerShift", predicate,
                Optional.of("orders.ribbon.message.mustChangeTechnologyState"));
    }

}
