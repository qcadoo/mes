/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.deliveries.deliveriesColumnExtension;

import static com.qcadoo.mes.basic.constants.ProductFields.NAME;
import static com.qcadoo.mes.basic.constants.ProductFields.NUMBER;
import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.DAMAGED_QUANTITY;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.DELIVERED_QUANTITY;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.ORDERED_QUANTITY;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.PRODUCT;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.print.DeliveryColumnFiller;
import com.qcadoo.mes.deliveries.print.DeliveryProduct;
import com.qcadoo.mes.deliveries.print.OrderColumnFiller;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Component
public class DeliveriesColumnFiller implements DeliveryColumnFiller, OrderColumnFiller {

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private NumberService numberService;

    @Override
    public Map<Entity, Map<String, String>> getDeliveryProductsColumnValues(
            final Map<Entity, DeliveryProduct> productWithDeliveryProducts) {
        Map<Entity, Map<String, String>> values = new HashMap<Entity, Map<String, String>>();

        for (Entry<Entity, DeliveryProduct> productWithDeliveryProduct : productWithDeliveryProducts.entrySet()) {
            Entity product = productWithDeliveryProduct.getKey();
            DeliveryProduct deliveryProduct = productWithDeliveryProduct.getValue();

            if (!values.containsKey(product)) {
                values.put(product, new HashMap<String, String>());
            }

            fillProductNumber(values, product);
            fillProductName(values, product);
            fillProductUnit(values, product);

            fillOrderedQuantity(values, product, deliveryProduct);
            fillDeliveredQuantity(values, product, deliveryProduct);
            fillDamagedQuantity(values, product, deliveryProduct);
        }

        return values;
    }

    @Override
    public Map<Entity, Map<String, String>> getOrderedProductsColumnValues(final List<Entity> orderedProducts) {
        Map<Entity, Map<String, String>> values = new HashMap<Entity, Map<String, String>>();

        for (Entity orderedProduct : orderedProducts) {
            Entity product = orderedProduct.getBelongsToField(PRODUCT);

            if (!values.containsKey(product)) {
                values.put(product, new HashMap<String, String>());
            }

            fillProductNumber(values, product);
            fillProductName(values, product);
            fillProductUnit(values, product);

            fillOrderedQuantity(values, product, orderedProduct);
        }

        return values;
    }

    private void fillProductNumber(final Map<Entity, Map<String, String>> values, final Entity product) {
        values.get(product).put("productNumber", product.getStringField(NUMBER));
    }

    private void fillProductName(final Map<Entity, Map<String, String>> values, final Entity product) {
        values.get(product).put("productName", product.getStringField(NAME));
    }

    private void fillProductUnit(final Map<Entity, Map<String, String>> values, final Entity product) {
        values.get(product).put("productUnit", product.getStringField(UNIT));
    }

    private void fillOrderedQuantity(final Map<Entity, Map<String, String>> values, final Entity product,
            final DeliveryProduct deliveryProduct) {
        BigDecimal orderedQuantity = null;

        if (deliveryProduct.getOrderedProductId() == null) {
            orderedQuantity = BigDecimal.ZERO;
        } else {
            Entity orderedProduct = deliveriesService.getOrderedProduct(deliveryProduct.getOrderedProductId());

            if (orderedProduct == null) {
                orderedQuantity = BigDecimal.ZERO;
            } else {
                orderedQuantity = orderedProduct.getDecimalField(ORDERED_QUANTITY);
            }
        }

        values.get(product).put("orderedQuantity", numberService.format(orderedQuantity));
    }

    private void fillDeliveredQuantity(final Map<Entity, Map<String, String>> values, final Entity product,
            final DeliveryProduct deliveryProduct) {
        BigDecimal deliveredQuantity = null;

        if (deliveryProduct.getDeliveredProductId() == null) {
            deliveredQuantity = BigDecimal.ZERO;
        } else {
            Entity deliveredProduct = deliveriesService.getDeliveredProduct(deliveryProduct.getDeliveredProductId());

            if (deliveredProduct == null) {
                deliveredQuantity = BigDecimal.ZERO;
            } else {
                deliveredQuantity = deliveredProduct.getDecimalField(DELIVERED_QUANTITY);
            }
        }

        values.get(product).put("deliveredQuantity", numberService.format(deliveredQuantity));
    }

    private void fillDamagedQuantity(final Map<Entity, Map<String, String>> values, final Entity product,
            final DeliveryProduct deliveryProduct) {
        BigDecimal damagedQuantity = null;

        if (deliveryProduct.getDeliveredProductId() == null) {
            damagedQuantity = BigDecimal.ZERO;
        } else {
            Entity deliveredProduct = deliveriesService.getDeliveredProduct(deliveryProduct.getDeliveredProductId());

            if (deliveredProduct == null) {
                damagedQuantity = BigDecimal.ZERO;
            } else {
                damagedQuantity = deliveredProduct.getDecimalField(DAMAGED_QUANTITY);
            }
        }

        values.get(product).put("damagedQuantity", numberService.format(damagedQuantity));
    }

    private void fillOrderedQuantity(final Map<Entity, Map<String, String>> values, final Entity product,
            final Entity orderedProduct) {
        BigDecimal orderedQuantity = null;

        if (orderedProduct == null) {
            orderedQuantity = BigDecimal.ZERO;
        } else {
            orderedQuantity = orderedProduct.getDecimalField(ORDERED_QUANTITY);
        }

        values.get(product).put("orderedQuantity", numberService.format(orderedQuantity));
    }

}
