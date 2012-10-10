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
package com.qcadoo.mes.deliveries;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class DeliveriesServiceImpl implements DeliveriesService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public Entity getDelivery(final Long deliveryId) {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY).get(
                deliveryId);
    }

    @Override
    public Entity getOrderedProduct(final Long deliveredProductId) {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_ORDERED_PRODUCT).get(
                deliveredProductId);
    }

    @Override
    public Entity getDeliveredProduct(final Long deliveredProductId) {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERED_PRODUCT).get(
                deliveredProductId);
    }

    @Override
    public List<Entity> getColumnsForDeliveries() {
        List<Entity> columnForDeliveries = dataDefinitionService
                .get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_COLUMN_FOR_DELIVERIES).find().list()
                .getEntities();

        return columnForDeliveries;
    }

    @Override
    public List<Entity> getColumnsForOrders() {
        List<Entity> columnForDeliveries = dataDefinitionService
                .get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_COLUMN_FOR_ORDERS).find().list()
                .getEntities();

        return columnForDeliveries;
    }

}
