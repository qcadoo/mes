/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.deliveries.listeners;

import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.PRODUCT;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DELIVERED_PRODUCTS;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.ORDERED_PRODUCTS;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.hooks.DeliveryDetailsHooks;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Component
public class DeliveryDetailsListeners {

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private DeliveryDetailsHooks deliveryDetailsHooks;

    public void fillBufferForSupplier(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveryDetailsHooks.fillBufferForSupplier(view);
    }

    public final void printDeliveryReport(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponent) {
            state.performEvent(viewDefinitionState, "save", args);

            if (!state.isHasError()) {
                viewDefinitionState.redirectTo("/deliveries/deliveryReport." + args[0] + "?id=" + state.getFieldValue(), true,
                        false);
            }
        } else {
            state.addMessage("deliveries.delivery.report.componentFormError", MessageType.FAILURE);
        }
    }

    public final void printOrderReport(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (state instanceof FormComponent) {
            state.performEvent(viewDefinitionState, "save", args);

            if (!state.isHasError()) {
                viewDefinitionState
                        .redirectTo("/deliveries/orderReport." + args[0] + "?id=" + state.getFieldValue(), true, false);
            }
        } else {
            state.addMessage("deliveries.order.report.componentFormError", MessageType.FAILURE);
        }
    }

    public final void copyOrderedProductToDelivered(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent deliveryForm = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Long deliveryId = deliveryForm.getEntityId();

        if (deliveryId == null) {
            return;
        }

        Entity delivery = deliveriesService.getDelivery(deliveryId);

        List<Entity> orderedProducts = delivery.getHasManyField(ORDERED_PRODUCTS);

        copyOrderedProductToDelivered(delivery, orderedProducts);
    }

    private void copyOrderedProductToDelivered(final Entity delivery, final List<Entity> orderedProducts) {
        // ALBR deliveredProduct has a validation so we have to delete all entities before save HM field in delivery
        delivery.setField(DELIVERED_PRODUCTS, Lists.newArrayList());
        delivery.getDataDefinition().save(delivery);
        delivery.setField(DELIVERED_PRODUCTS, Lists.newArrayList(createDeliveredProducts(orderedProducts)));

        delivery.getDataDefinition().save(delivery);
    }

    private List<Entity> createDeliveredProducts(final List<Entity> orderedProducts) {
        List<Entity> deliveredProducts = new ArrayList<Entity>();

        for (Entity orderedProduct : orderedProducts) {
            deliveredProducts.add(createDeliveredProduct(orderedProduct));
        }

        return deliveredProducts;
    }

    private Entity createDeliveredProduct(final Entity orderedProduct) {
        Entity deliveredProduct = deliveriesService.getDeliveredProductDD().create();

        deliveredProduct.setField(PRODUCT, orderedProduct.getBelongsToField(PRODUCT));

        return deliveredProduct;
    }

}
