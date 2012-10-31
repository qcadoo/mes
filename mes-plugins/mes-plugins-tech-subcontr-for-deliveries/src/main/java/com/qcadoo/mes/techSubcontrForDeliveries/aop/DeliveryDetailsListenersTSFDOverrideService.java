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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.techSubcontrForDeliveries.constants.DeliveredProductFieldsTSFD;
import com.qcadoo.mes.techSubcontrForDeliveries.constants.OrderedProductFieldsTSFD;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class DeliveryDetailsListenersTSFDOverrideService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void fillFieldWithOffer(final Entity delivery) {
        final List<Entity> deliveredProductList = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);
        for (Entity deliveredProduct : deliveredProductList) {
            final Entity orderedProduct = getOrderProduct(deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT),
                    delivery);
            deliveredProduct.setField(DeliveredProductFieldsTSFD.OPERATION,
                    orderedProduct.getBelongsToField(OrderedProductFieldsTSFD.OPERATION));
        }
        delivery.getDataDefinition().save(delivery);
    }

    private Entity getOrderProduct(final Entity product, final Entity delivery) {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_ORDERED_PRODUCT).find()
                .add(SearchRestrictions.belongsTo("product", product))
                .add(SearchRestrictions.belongsTo(OrderedProductFields.DELIVERY, delivery)).uniqueResult();
    }

}