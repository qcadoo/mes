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
package com.qcadoo.mes.deliveriesMinState.listeners;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class DeliveryDetailsListenersDMS {

    @Autowired
    NumberService numberService;

    @Autowired
    private DeliveriesService deliveriesService;

    public void fillPrices(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        GridComponent orderedProductsGrid = (GridComponent) view.getComponentByReference(DeliveriesService.L_ORDERED_PRODUCTS);
        List<Entity> orderedProducts = deliveriesService.getSelectedOrderedProducts(orderedProductsGrid);
        orderedProducts.forEach(op -> {
            Entity product = op.getBelongsToField(OrderedProductFields.PRODUCT);
            BigDecimal lastPurchaseCost = product.getDecimalField(ProductFieldsCNFP.LAST_PURCHASE_COST);
            op.setField(OrderedProductFields.PRICE_PER_UNIT, lastPurchaseCost);
            op.getDataDefinition().save(op);
        });
        orderedProductsGrid.reloadEntities();

        BigDecimal totalPrice = orderedProducts.stream()
                .filter(op -> op.getDecimalField(OrderedProductFields.TOTAL_PRICE) != null)
                .map(op -> op.getDecimalField(OrderedProductFields.TOTAL_PRICE)).reduce(BigDecimal.ZERO, BigDecimal::add);
        FieldComponent totalPriceComponent = (FieldComponent) view.getComponentByReference("orderedProductsCumulatedTotalPrice");
        totalPriceComponent.setFieldValue(numberService.formatWithMinimumFractionDigits(totalPrice, 0));
        totalPriceComponent.requestComponentUpdateState();
    }
}
