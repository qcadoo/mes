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
package com.qcadoo.mes.costCalculation;

import com.qcadoo.model.api.Entity;

import java.math.BigDecimal;

public interface CostCalculationService {

    /**
     * Calculates total cost
     * 
     * @param entity
     *            entity
     * @param technology
     *            technology
     *
     * @return entity
     */
    Entity calculateTotalCost(final Entity entity, final Entity technology);

    /**
     * Calculates total costs
     * 
     * @param entity
     *            entity
     * @param productionCosts
     *            production costs
     * @param quantity
     *            quantity
     */
    void calculateTotalCosts(final Entity entity, final BigDecimal productionCosts, final BigDecimal quantity);

    /**
     * Calculates producton cost
     * 
     * @param entity
     *            entity
     * 
     * @return productionCost
     */
    BigDecimal calculateProductionCost(final Entity entity);

    /**
     * Calculated total overhead
     * 
     * @param entity
     *            entity
     */
    void calculateTotalOverhead(final Entity entity);

    /**
     * Calculated sell price overhead
     * 
     * @param entity
     *            entity
     */
    public void calculateSellPriceOverhead(final Entity entity);

    /**
     * Calculated sell price
     * 
     * @param entity
     *            entity
     */
    public void calculateSellPrice(final Entity entity);

}
