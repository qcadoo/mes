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
package com.qcadoo.mes.supplyNegotiations.aop;

import static com.qcadoo.mes.deliveries.constants.ColumnForOrdersFields.IDENTIFIER;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.supplyNegotiations.constants.OfferFields;
import com.qcadoo.mes.supplyNegotiations.constants.OrderedProductFieldsSN;
import com.qcadoo.mes.supplyNegotiations.constants.SupplyNegotiationsConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginStateResolver;

@Service
public class OrderReportPdfSNOverrideUtil {

    @Autowired
    private PluginStateResolver pluginStateResolver;

    public boolean shouldOverride() {
        return pluginStateResolver.isEnabled(SupplyNegotiationsConstants.PLUGIN_IDENTIFIER);
    }

    public Map<String, Object> addFieldToSecondColumn(final Entity delivery, final Map<String, Object> result) {
        Map<String, Object> column = Maps.newLinkedHashMap(result);

        List<Entity> orderedProducts = delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS);

        Entity offer = checkIfShouldAddCell(orderedProducts);

        if (offer != null) {
            column.put("deliveries.order.report.columnHeader.offer", offer.getStringField(OfferFields.NUMBER));
        }

        return column;
    }

    public List<Entity> getOrderReportColumnsExecution(final List<Entity> columnsForOrders, final List<Entity> orderedProducts) {
        List<Entity> filteredColumnsForOrders = null;

        if (checkIfShouldAddCell(orderedProducts) == null) {
            filteredColumnsForOrders = Lists.newArrayList(columnsForOrders);
        } else {
            filteredColumnsForOrders = Lists.newArrayList();

            for (Entity columnForOrders : columnsForOrders) {
                String identifier = columnForOrders.getStringField(IDENTIFIER);

                if (!"offerNumber".equals(identifier)) {
                    filteredColumnsForOrders.add(columnForOrders);
                }
            }
        }

        return filteredColumnsForOrders;
    }

    private Entity checkIfShouldAddCell(final List<Entity> orderedProducts) {
        if (orderedProducts.isEmpty()) {
            return null;
        }

        Entity firstOrderedProduct = orderedProducts.get(0);
        Entity offer = firstOrderedProduct.getBelongsToField(OrderedProductFieldsSN.OFFER);

        if (firstOrderedProduct.getBelongsToField(OrderedProductFieldsSN.OFFER) == null) {
            return null;
        }

        Long offerId = offer.getId();

        for (Entity orderedProduct : orderedProducts) {
            Entity offerFromOrderedProduct = orderedProduct.getBelongsToField(OrderedProductFieldsSN.OFFER);

            if (offerFromOrderedProduct == null || !offerFromOrderedProduct.getId().equals(offerId)) {
                return null;
            }
        }

        return offer;
    }

}
