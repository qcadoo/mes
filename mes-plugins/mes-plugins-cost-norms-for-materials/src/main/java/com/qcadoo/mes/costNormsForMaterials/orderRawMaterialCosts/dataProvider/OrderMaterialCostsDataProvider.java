/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.dataProvider;

import java.util.List;

import com.google.common.base.Optional;
import com.qcadoo.model.api.Entity;

/**
 * Order's material costs data provider
 * 
 * @since 1.4
 */
public interface OrderMaterialCostsDataProvider {

    /**
     * Find order's material costs entities that match given criteria
     * 
     * @param criteria
     *            search criteria
     * @return order's material costs entities matching given criteria
     * @since 1.4
     */
    List<Entity> findAll(final OrderMaterialCostsCriteria criteria);

    /**
     * Find first order's material costs entity that match given criteria
     *
     * @param criteria
     *            search criteria
     * @return order's material costs entity matching given criteria
     * @since 1.4
     */
    Optional<Entity> find(final OrderMaterialCostsCriteria criteria);

    /**
     * Find order's material costs entity that belongs to given order and product
     *
     * @param orderId
     *            id of an order
     * @param productId
     *            id of a product
     * @return order's material costs entity belonging to given order and product
     * @since 1.4
     */
    Optional<Entity> find(final Long orderId, final Long productId);
}
