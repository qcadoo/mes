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
package com.qcadoo.mes.orders.util;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class OrderDetailsRibbonHelper {

    private static final Set<TechnologyState> SUPPORTED_TECHNOLOGY_STATES = ImmutableSet.of(TechnologyState.ACCEPTED,
            TechnologyState.CHECKED);

    public static final Predicate<Entity> HAS_CHECKED_OR_ACCEPTED_TECHNOLOGY = new Predicate<Entity>() {

        @Override
        public boolean apply(final Entity order) {
            if (order == null) {
                return false;
            }
            Entity orderTechnology = order.getBelongsToField(OrderFields.TECHNOLOGY);
            return orderTechnology != null && SUPPORTED_TECHNOLOGY_STATES.contains(TechnologyState.of(orderTechnology));
        }
    };

    @Autowired
    private OrderService orderService;

    public void setButtonEnabled(final ViewDefinitionState view, final String ribbonGroupName, final String ribbonItemName,
            final Predicate<Entity> predicate) {
        RibbonActionItem ribbonItem = getRibbonItem(view, ribbonGroupName, ribbonItemName);
        Entity order = getOrderEntity(view);
        if (ribbonItem == null) {
            return;
        }
        ribbonItem.setEnabled(order != null && predicate.apply(order));
        ribbonItem.requestUpdate(true);
    }

    private Entity getOrderEntity(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form == null) {
            return null;
        }
        Long orderId = form.getEntityId();
        if (orderId == null) {
            return null;
        }
        return orderService.getOrder(orderId);
    }

    private RibbonActionItem getRibbonItem(final ViewDefinitionState view, final String ribbonGroupName,
            final String ribbonItemName) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        Ribbon ribbon = window.getRibbon();
        RibbonGroup ribbonGroup = ribbon.getGroupByName(ribbonGroupName);
        if (ribbonGroup == null) {
            return null;
        }
        return ribbonGroup.getItemByName(ribbonItemName);
    }

}
