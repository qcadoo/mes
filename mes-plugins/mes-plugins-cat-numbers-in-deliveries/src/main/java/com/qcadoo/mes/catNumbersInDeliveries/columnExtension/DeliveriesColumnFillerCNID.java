/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.catNumbersInDeliveries.columnExtension;

import static com.qcadoo.mes.catNumbersInDeliveries.contants.OrderedProductFieldsCNID.PRODUCT_CATALOG_NUMBER;
import static com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumberFields.CATALOG_NUMBER;

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

@Component
public class DeliveriesColumnFillerCNID implements DeliveryColumnFiller, OrderColumnFiller {

    @Autowired
    private DeliveriesService deliveriesService;

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

            fillCatalogNumber(values, product, deliveryProduct);
        }

        return values;
    }

    @Override
    public Map<Entity, Map<String, String>> getOrderedProductsColumnValues(final List<Entity> orderedProducts) {
        Map<Entity, Map<String, String>> values = new HashMap<Entity, Map<String, String>>();

        for (Entity orderedProduct : orderedProducts) {
            if (!values.containsKey(orderedProduct)) {
                values.put(orderedProduct, new HashMap<String, String>());
            }

            fillCatalogNumber(values, orderedProduct);
        }

        return values;
    }

    private void fillCatalogNumber(final Map<Entity, Map<String, String>> values, final Entity product,
            final DeliveryProduct deliveryProduct) {
        String catalogNumber = null;

        if (deliveryProduct.getDeliveredProductId() != null) {
            Entity deliveredProduct = deliveriesService.getDeliveredProduct(deliveryProduct.getDeliveredProductId());

            if (deliveredProduct == null) {
                catalogNumber = "";
            } else {
                Entity productCatalogNumber = deliveredProduct.getBelongsToField(PRODUCT_CATALOG_NUMBER);

                if (productCatalogNumber == null) {
                    catalogNumber = "";
                } else {
                    catalogNumber = productCatalogNumber.getStringField(CATALOG_NUMBER);
                }
            }
        } else if (deliveryProduct.getOrderedProductId() != null) {
            Entity orderedProduct = deliveriesService.getOrderedProduct(deliveryProduct.getOrderedProductId());

            if (orderedProduct == null) {
                catalogNumber = "";
            } else {
                Entity productCatalogNumber = orderedProduct.getBelongsToField(PRODUCT_CATALOG_NUMBER);

                if (productCatalogNumber == null) {
                    catalogNumber = "";
                } else {
                    catalogNumber = productCatalogNumber.getStringField(CATALOG_NUMBER);
                }
            }
        } else {
            catalogNumber = "";
        }

        values.get(product).put("catalogNumber", catalogNumber);
    }

    private void fillCatalogNumber(final Map<Entity, Map<String, String>> values, final Entity orderedProduct) {
        String catalogNumber = null;

        if (orderedProduct == null) {
            catalogNumber = "";
        } else {
            Entity productCatalogNumber = orderedProduct.getBelongsToField(PRODUCT_CATALOG_NUMBER);

            if (productCatalogNumber == null) {
                catalogNumber = "";
            } else {
                catalogNumber = productCatalogNumber.getStringField(CATALOG_NUMBER);
            }
        }

        values.get(orderedProduct).put("catalogNumber", catalogNumber);
    }

}
