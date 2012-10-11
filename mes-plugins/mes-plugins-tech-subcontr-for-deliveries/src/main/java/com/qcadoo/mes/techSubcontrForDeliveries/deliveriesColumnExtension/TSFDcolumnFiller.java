/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.techSubcontrForDeliveries.deliveriesColumnExtension;

import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.PRODUCT;
import static com.qcadoo.mes.techSubcontrForDeliveries.constants.DeliveredProductFieldsTSFD.OPERATION;
import static com.qcadoo.mes.technologies.constants.OperationFields.NUMBER;

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
public class TSFDcolumnFiller implements DeliveryColumnFiller, OrderColumnFiller {

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

            fillOperationNumber(values, product, deliveryProduct);
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

            fillOperationNumber(values, product, orderedProduct);
        }

        return values;
    }

    private void fillOperationNumber(Map<Entity, Map<String, String>> values, final Entity product,
            final DeliveryProduct deliveryProduct) {
        String operationNumber = null;

        if (deliveryProduct.getDeliveredProductId() != null) {
            Entity deliveredProduct = deliveriesService.getDeliveredProduct(deliveryProduct.getDeliveredProductId());

            if (deliveredProduct == null) {
                operationNumber = "";
            } else {
                Entity operation = deliveredProduct.getBelongsToField(OPERATION);

                if (operation == null) {
                    operationNumber = "";
                } else {
                    operationNumber = operation.getStringField(NUMBER);
                }
            }
        } else if (deliveryProduct.getOrderedProductId() != null) {
            Entity orderedProduct = deliveriesService.getOrderedProduct(deliveryProduct.getOrderedProductId());

            if (orderedProduct == null) {
                operationNumber = "";
            } else {
                Entity operation = orderedProduct.getBelongsToField(OPERATION);

                if (operation == null) {
                    operationNumber = "";
                } else {
                    operationNumber = operation.getStringField(NUMBER);
                }
            }
        } else {
            operationNumber = "";
        }

        values.get(product).put("operationNumber", operationNumber);
    }

    private void fillOperationNumber(Map<Entity, Map<String, String>> values, final Entity product, final Entity orderedProduct) {
        String operationNumber = null;

        if (orderedProduct == null) {
            operationNumber = "";
        } else {
            Entity operation = orderedProduct.getBelongsToField(OPERATION);

            if (operation == null) {
                operationNumber = "";
            } else {
                operationNumber = operation.getStringField(NUMBER);
            }
        }

        values.get(product).put("operationNumber", operationNumber);
    }

}
