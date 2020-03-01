/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.supplyNegotiations.listeners;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.supplyNegotiations.SupplyNegotiationsService;
import com.qcadoo.mes.supplyNegotiations.constants.OfferProductFields;
import com.qcadoo.mes.supplyNegotiations.constants.OrderedProductFieldsSN;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class DeliveryDetailsListenersSN {

    private static final String L_ORDERED_PRODUCTS_CUMULATED_TOTAL_PRICE = "orderedProductsCumulatedTotalPrice";

    @Autowired
    private NumberService numberService;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private SupplyNegotiationsService supplyNegotiationsService;

    public void fillPrices(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        GridComponent orderedProductsGrid = (GridComponent) view.getComponentByReference(DeliveryFields.ORDERED_PRODUCTS);

        List<Entity> orderedProducts = deliveriesService.getSelectedOrderedProducts(orderedProductsGrid);

        orderedProducts.forEach(orderedProduct -> {
            Entity product = orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT);
            Entity supplier = orderedProduct.getBelongsToField(OrderedProductFields.DELIVERY)
                    .getBelongsToField(DeliveryFields.SUPPLIER);

            if (Objects.isNull(supplier)) {
                return;
            }

            Entity offerProduct = supplyNegotiationsService.getLastOfferProduct(supplier, product);

            if (Objects.nonNull(offerProduct)) {
                orderedProduct.setField(OrderedProductFields.PRICE_PER_UNIT,
                        offerProduct.getDecimalField(OfferProductFields.PRICE_PER_UNIT));
                orderedProduct.setField(OrderedProductFieldsSN.OFFER, offerProduct.getBelongsToField(OfferProductFields.OFFER));
                orderedProduct.getDataDefinition().save(orderedProduct);
            }
        });

        orderedProductsGrid.reloadEntities();

        BigDecimal totalPrice = orderedProducts.stream()
                .filter(orderedProduct -> Objects.nonNull(orderedProduct.getDecimalField(OrderedProductFields.TOTAL_PRICE)))
                .map(orderedProduct -> orderedProduct.getDecimalField(OrderedProductFields.TOTAL_PRICE))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        FieldComponent totalPriceComponent = (FieldComponent) view
                .getComponentByReference(L_ORDERED_PRODUCTS_CUMULATED_TOTAL_PRICE);

        totalPriceComponent.setFieldValue(numberService.formatWithMinimumFractionDigits(totalPrice, 0));
        totalPriceComponent.requestComponentUpdateState();
    }

}
