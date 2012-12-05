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
package com.qcadoo.mes.catNumbersInDeliveries.hooks;

import static com.qcadoo.mes.catNumbersInDeliveries.contants.OrderedProductFieldsCNID.PRODUCT_CATALOG_NUMBER;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DELIVERED_PRODUCTS;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.ORDERED_PRODUCTS;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.SUPPLIER;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.PRODUCT;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.catNumbersInDeliveries.CatNumbersInDeliveriesService;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class DeliveryHooksCNID {

    @Autowired
    private CatNumbersInDeliveriesService catNumbersInDeliveriesService;

    @Autowired
    private DeliveriesService deliveriesService;

    public void updateOrderedProductsCatalogNumbers(final DataDefinition deliveryDD, final Entity delivery) {
        Entity supplier = delivery.getBelongsToField(SUPPLIER);

        if ((delivery.getId() != null) && hasSupplierChanged(delivery.getId(), supplier)) {
            List<Entity> orderedProducts = delivery.getHasManyField(ORDERED_PRODUCTS);

            if (orderedProducts != null) {
                for (Entity orderedProduct : orderedProducts) {
                    Entity product = orderedProduct.getBelongsToField(PRODUCT);

                    Entity productCatalogNumber = catNumbersInDeliveriesService.getProductCatalogNumber(product, supplier);

                    if (productCatalogNumber != null) {
                        orderedProduct.setField(PRODUCT_CATALOG_NUMBER, productCatalogNumber);

                        orderedProduct.getDataDefinition().save(orderedProduct);
                    }
                }
            }
        }
    }

    public void updateDeliveredProductsCatalogNumbers(final DataDefinition deliveryDD, final Entity delivery) {
        Entity supplier = delivery.getBelongsToField(SUPPLIER);

        if ((delivery.getId() != null) && hasSupplierChanged(delivery.getId(), supplier)) {
            List<Entity> deliveredProducts = delivery.getHasManyField(DELIVERED_PRODUCTS);

            if (deliveredProducts != null) {
                for (Entity deliveredProduct : deliveredProducts) {
                    Entity product = deliveredProduct.getBelongsToField(PRODUCT);

                    Entity productCatalogNumber = catNumbersInDeliveriesService.getProductCatalogNumber(product, supplier);

                    if (productCatalogNumber != null) {
                        deliveredProduct.setField(PRODUCT_CATALOG_NUMBER, productCatalogNumber);

                        deliveredProduct.getDataDefinition().save(deliveredProduct);
                    }
                }
            }
        }
    }

    private boolean hasSupplierChanged(final Long deliveryId, final Entity supplier) {
        Entity existingDelivery = deliveriesService.getDelivery(deliveryId);

        Entity existingSupplier = existingDelivery.getBelongsToField(SUPPLIER);

        return !existingSupplier.equals(supplier);
    }

}
