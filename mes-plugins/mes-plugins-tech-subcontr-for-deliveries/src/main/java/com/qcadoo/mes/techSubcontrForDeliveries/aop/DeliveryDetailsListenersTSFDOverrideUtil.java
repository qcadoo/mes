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
package com.qcadoo.mes.techSubcontrForDeliveries.aop;

import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.PRODUCT;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DELIVERED_PRODUCTS;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.ORDERED_PRODUCTS;
import static com.qcadoo.mes.techSubcontrForDeliveries.constants.DeliveredProductFieldsTSFD.OPERATION;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class DeliveryDetailsListenersTSFDOverrideUtil {

    public void fillDeliveredProductOperation(final Entity delivery) {
        List<Entity> deliveredProducts = delivery.getHasManyField(DELIVERED_PRODUCTS);

        for (Entity deliveredProduct : deliveredProducts) {
            Entity product = deliveredProduct.getBelongsToField(PRODUCT);

            Entity orderedProduct = getOrderProduct(delivery, product);

            deliveredProduct.setField(OPERATION, orderedProduct.getBelongsToField(OPERATION));
        }

        delivery.getDataDefinition().save(delivery);
    }

    private Entity getOrderProduct(final Entity delivery, final Entity product) {
        return delivery.getHasManyField(ORDERED_PRODUCTS).find().add(SearchRestrictions.belongsTo(PRODUCT, product))
                .setMaxResults(1).uniqueResult();
    }

}
