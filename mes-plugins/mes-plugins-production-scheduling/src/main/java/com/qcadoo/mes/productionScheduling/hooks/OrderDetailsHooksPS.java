/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.productionScheduling.hooks;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableSet;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class OrderDetailsHooksPS {

    private static final Set<TechnologyState> SUPPORTED_TECHNOLOGY_STATES = ImmutableSet.of(TechnologyState.ACCEPTED,
            TechnologyState.CHECKED);

    private static final String L_OPERATION_DURATION = "operationDuration";

    public void disabledButtonOperationDuration(final ViewDefinitionState view) {
        RibbonActionItem operationDurationButton = getOperationDurationButton(view);
        Entity order = getOrderEntity(view);

        if (operationDurationButton == null || order == null) {
            return;
        }

        Entity orderTechnology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        boolean enabled = orderTechnology != null;
        if (OrderState.of(order) == OrderState.PENDING) {
            enabled = enabled && SUPPORTED_TECHNOLOGY_STATES.contains(TechnologyState.of(orderTechnology));
        }
        operationDurationButton.setEnabled(enabled);
        operationDurationButton.requestUpdate(true);
    }

    private RibbonActionItem getOperationDurationButton(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        Ribbon ribbon = window.getRibbon();
        RibbonGroup group = ribbon.getGroupByName(L_OPERATION_DURATION);
        if (group == null) {
            return null;
        }
        return group.getItemByName(L_OPERATION_DURATION);
    }

    private Entity getOrderEntity(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form == null) {
            return null;
        }
        return form.getPersistedEntityWithIncludedFormValues();
    }

}
