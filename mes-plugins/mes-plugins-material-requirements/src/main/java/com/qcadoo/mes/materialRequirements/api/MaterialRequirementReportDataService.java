/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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
package com.qcadoo.mes.materialRequirements.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.qcadoo.model.api.Entity;

/**
 * Service for preparing data to material requirement reports.
 * 
 * @since 0.4.1
 * 
 */
public interface MaterialRequirementReportDataService {

    /**
     * Return map of products with it's quantity for orders entities.
     * 
     * @param orders
     *            list of orders
     * @param onlyComponents
     *            get only this products which are components (related to technology they are used in)
     * @return map of products with it quantity
     */
    Map<Entity, BigDecimal> getQuantitiesForOrdersTechnologyProducts(final List<Entity> orders, final Boolean onlyComponents);

    Map<Entity, BigDecimal> getQuantitiesForMaterialRequirementProducts(final List<Entity> materialRequirementComponents,
            final Boolean onlyComponents);

}
