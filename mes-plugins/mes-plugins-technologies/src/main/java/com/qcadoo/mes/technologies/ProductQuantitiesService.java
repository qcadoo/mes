/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
package com.qcadoo.mes.technologies;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.qcadoo.model.api.Entity;

public interface ProductQuantitiesService {

    /**
     * 
     * @param orders
     *            List of orders
     * @return Map with operationProductComponents (in or out) as the keys and its quantities as the values. Be aware that
     *         products that are the same, but are related to different operations are here as different entries.
     */
    Map<Entity, BigDecimal> getProductComponentQuantities(final List<Entity> orders);

    /**
     * 
     * @param orders
     *            List of orders
     * @param operationRuns
     *            Method takes an empty map and puts here info on how many certain operations (operationComponents) have to be
     *            run.
     * @return Map with operationProductComponents (in or out) as the keys and its quantities as the values. Be aware that
     *         products that are the same, but are related to different operations are here as different entries.
     */
    Map<Entity, BigDecimal> getProductComponentQuantities(final List<Entity> orders, Map<Entity, BigDecimal> operationRuns);

    /**
     * 
     * @param technology
     *            Given technology
     * @param operationRuns
     *            Method takes an empty map and puts here info on how many certain operations (operationComponents) have to be
     *            run.
     * @return Map with operationProductComponents (in or out) as the keys and its quantities as the values. Be aware that
     *         products that are the same, but are related to different operations are here as different entries.
     */
    Map<Entity, BigDecimal> getProductComponentQuantities(final Entity technology, final BigDecimal givenQty,
            Map<Entity, BigDecimal> operationRuns);

    /**
     * 
     * @param technology
     *            Given technology
     * @param givenQty
     *            How many products, that are outcomes of this technology, we want.
     * @return Map with product as the key and its quantity as the value. This time keys are products, so they are aggregated.
     */
    Map<Entity, BigDecimal> getNeededProductQuantities(final Entity technology, final BigDecimal givenQty,
            final boolean onlyComponents);

    /**
     * 
     * @param orders
     *            Given list of orders
     * @param onlyComponents
     *            A flag that indicates if we want only components or intermediates too
     * @return Map of products and their quantities (products that occur in multiple operations or even in multiple orders are
     *         aggregated)
     */
    Map<Entity, BigDecimal> getNeededProductQuantities(final List<Entity> orders, final boolean onlyComponents);

    /**
     * 
     * @param orders
     *            Given list of orders
     * @return Map of output products and their quantities (products that occur in multiple operations or even in multiple orders
     *         are aggregated)
     */
    Map<Entity, BigDecimal> getOutputProductQuantities(final List<Entity> orders);

    /**
     * 
     * @param orders
     *            Given list of orders
     * @param onlyComponents
     *            A flag that indicates if we want only components or intermediates too
     * @param operationRuns
     *            Method takes an empty map and puts here info on how many times certain operation (operationComponent) has to be
     *            run.
     * @return Map of products and their quantities (products that occur in multiple operations or even in multiple orders are
     *         aggregated)
     */
    Map<Entity, BigDecimal> getNeededProductQuantities(final List<Entity> orders, boolean onlyComponents,
            Map<Entity, BigDecimal> operationRuns);

    /**
     * 
     * @param components
     *            List of components that have order as belongsTo relation
     * @param onlyComponents
     *            A flag that indicates if we want only components or intermediates too
     * @return Map of products and their quantities (products that occur in multiple operations or even in multiple orders are
     *         aggregated)
     */
    Map<Entity, BigDecimal> getNeededProductQuantitiesForComponents(final List<Entity> components, final boolean onlyComponents);
}
