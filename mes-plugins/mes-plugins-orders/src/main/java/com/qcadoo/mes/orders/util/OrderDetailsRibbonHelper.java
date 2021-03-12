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
package com.qcadoo.mes.orders.util;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableSet;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OrderDetailsRibbonHelper {

    private static final Set<TechnologyState> SUPPORTED_TECHNOLOGY_STATES = ImmutableSet.of(TechnologyState.ACCEPTED,
            TechnologyState.CHECKED);

    public static final Predicate<Entity> HAS_CHECKED_OR_ACCEPTED_TECHNOLOGY = order -> {
        if (Objects.isNull(order)) {
            return false;
        }

        Entity orderTechnology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        return Objects.nonNull(orderTechnology) && SUPPORTED_TECHNOLOGY_STATES.contains(TechnologyState.of(orderTechnology));
    };

    public static final Predicate<Entity> DIFFERENT_STATE_THAN_PENDING = order -> {
        if (Objects.isNull(order)) {
            return false;
        }

        return !OrderStateStringValues.PENDING.equals(order.getStringField(OrderFields.STATE));
    };

    public static final String FOR_EACH = "03forEach";

    public static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    public static final Predicate<Entity> CAN_NOT_GENERATE_OPERATIONAL_TASKS = order -> {
        if (Objects.isNull(order)) {
            return false;
        }

        if (!OrderStateStringValues.ACCEPTED.equals(order.getStringField(OrderFields.STATE))) {
            return false;
        }

        if (!FOR_EACH.equals(order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING))) {
            return false;
        }

        return order.getHasManyField(OrderFields.OPERATIONAL_TASKS).isEmpty();
    };

    @Autowired
    private OrderService orderService;

    public void setButtonEnabled(final ViewDefinitionState view, final String ribbonGroupName, final String ribbonItemName,
            final Predicate<Entity> predicate) {
        setButtonEnabled(view, ribbonGroupName, ribbonItemName, predicate, Optional.<String> empty());
    }

    public void setButtonEnabled(final ViewDefinitionState view, final String ribbonGroupName, final String ribbonItemName,
            final Predicate<Entity> predicate, final Optional<String> message) {
        RibbonActionItem ribbonItem = getRibbonItem(view, ribbonGroupName, ribbonItemName);

        Entity order = getOrderEntity(view);

        boolean enabled = Objects.nonNull(order) && predicate.test(order);

        if (Objects.isNull(ribbonItem)) {
            return;
        }

        ribbonItem.setEnabled(enabled);

        if (!enabled && message.isPresent()) {
            ribbonItem.setMessage(message.get());
        }

        ribbonItem.requestUpdate(true);
    }

    public Entity getOrderEntity(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (Objects.isNull(orderForm)) {
            return null;
        }

        Long orderId = orderForm.getEntityId();

        if (Objects.isNull(orderId)) {
            return null;
        }

        return orderService.getOrder(orderId);
    }

    public RibbonActionItem getRibbonItem(final ViewDefinitionState view, final String ribbonGroupName,
            final String ribbonItemName) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        Ribbon ribbon = window.getRibbon();
        RibbonGroup ribbonGroup = ribbon.getGroupByName(ribbonGroupName);

        if (Objects.isNull(ribbonGroup)) {
            return null;
        }

        return ribbonGroup.getItemByName(ribbonItemName);
    }

}
