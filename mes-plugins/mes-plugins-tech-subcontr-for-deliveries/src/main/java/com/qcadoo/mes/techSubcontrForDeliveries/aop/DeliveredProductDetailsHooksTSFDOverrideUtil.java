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

import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.DELIVERY;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.PRODUCT;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.ORDERED_QUANTITY;
import static com.qcadoo.mes.techSubcontrForDeliveries.constants.DeliveredProductFieldsTSFD.OPERATION;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.techSubcontrForDeliveries.constants.TechSubcontrForDeliveriesConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginStateResolver;

@Service
public class DeliveredProductDetailsHooksTSFDOverrideUtil {

    @Autowired
    private PluginStateResolver pluginStateResolver;

    @Autowired
    private DeliveriesService deliveriesService;

    public boolean shouldOverride(final Entity deliveredProduct) {
        return pluginStateResolver.isEnabled(TechSubcontrForDeliveriesConstants.PLUGIN_IDENTIFIER)
                && (deliveredProduct.getBelongsToField(OPERATION) != null);
    }

    public BigDecimal getOrderedProductQuantityWithOperation(final Entity deliveredProduct) {
        Entity delivery = deliveredProduct.getBelongsToField(DELIVERY);
        Entity product = deliveredProduct.getBelongsToField(PRODUCT);
        Entity operation = deliveredProduct.getBelongsToField(OPERATION);

        BigDecimal orderedQuantity = null;

        Entity orderedProduct = deliveriesService.getOrderedProductDD().find()
                .add(SearchRestrictions.belongsTo(DELIVERY, delivery)).add(SearchRestrictions.belongsTo(PRODUCT, product))
                .add(SearchRestrictions.belongsTo(OPERATION, operation)).setMaxResults(1).uniqueResult();

        if (orderedProduct != null) {
            orderedQuantity = orderedProduct.getDecimalField(ORDERED_QUANTITY);
        }

        return orderedQuantity;
    }

}
