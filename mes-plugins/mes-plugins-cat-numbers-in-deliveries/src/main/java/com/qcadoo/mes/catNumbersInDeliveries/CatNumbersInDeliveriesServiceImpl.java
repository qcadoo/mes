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
package com.qcadoo.mes.catNumbersInDeliveries;

import static com.qcadoo.mes.catNumbersInDeliveries.contants.DeliveredProductFieldsCNID.PRODUCT_CATALOG_NUMBER;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.DELIVERY;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.PRODUCT;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.SUPPLIER;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.productCatalogNumbers.ProductCatalogNumbersService;
import com.qcadoo.model.api.Entity;

@Service
public class CatNumbersInDeliveriesServiceImpl implements CatNumbersInDeliveriesService {

    @Autowired
    private ProductCatalogNumbersService productCatalogNumbersService;

    @Autowired
    private DeliveriesService deliveriesService;

    @Override
    public void updateProductCatalogNumber(final Entity deliveryProduct) {
        Entity delivery = deliveryProduct.getBelongsToField(DELIVERY);
        Entity supplier = delivery.getBelongsToField(SUPPLIER);

        Entity product = deliveryProduct.getBelongsToField(PRODUCT);

        Entity productCatalogNumber = productCatalogNumbersService.getProductCatalogNumber(product, supplier);

        if (productCatalogNumber != null) {
            deliveryProduct.setField(PRODUCT_CATALOG_NUMBER, productCatalogNumber);
        }
    }

    @Override
    public void updateProductsCatalogNumbers(final Entity delivery, final String productsName) {
        Entity supplier = delivery.getBelongsToField(SUPPLIER);

        if ((delivery.getId() != null) && hasSupplierChanged(delivery.getId(), supplier)) {
            List<Entity> deliveryProducts = delivery.getHasManyField(productsName);

            if (deliveryProducts != null) {
                for (Entity deliveryPoduct : deliveryProducts) {
                    Entity product = deliveryPoduct.getBelongsToField(PRODUCT);

                    Entity productCatalogNumber = productCatalogNumbersService.getProductCatalogNumber(product, supplier);

                    if (productCatalogNumber != null) {
                        deliveryPoduct.setField(PRODUCT_CATALOG_NUMBER, productCatalogNumber);

                        deliveryPoduct.getDataDefinition().save(deliveryPoduct);
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
