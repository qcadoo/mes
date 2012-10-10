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

import com.qcadoo.model.api.Entity;

public interface DeliveriesService {

    /**
     * Gets delivery
     * 
     * @param deliveryId
     * 
     * @return delivery
     */
    Entity getDelivery(final Long deliveryId);

    /**
     * Gets ordered product
     * 
     * @param deliveredProductId
     * 
     * @return ordered product
     */
    Entity getOrderedProduct(final Long deliveredProductId);

    /**
     * Gets delivered product
     * 
     * @param deliveredProductId
     * 
     * @return delivered product
     */
    Entity getDeliveredProduct(final Long deliveredProductId);

    /**
     * Gets list of columns for deliveries
     * 
     * @return list of columns for deliveries
     */
    List<Entity> getColumnsForDeliveries();

    /**
     * Gets list of columns for orders
     * 
     * @return list of columns for orders
     */
    List<Entity> getColumnsForOrders();

}
