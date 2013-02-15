/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
import java.util.Map.Entry;

import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.Entity;

public interface ProductQuantitiesService {

    /**
     * 
     * @param technology
     *            Given technology
     * 
     * @param givenQuantity
     *            How many products, that are outcomes of this technology, we want.
     * 
     * @param operationRuns
     *            Method takes an empty map and puts here info on how many certain operations (operationComponents) have to be
     *            run.
     * 
     * @return Map with operationProductComponents (in or out) as the keys and its quantities as the values. Be aware that
     *         products that are the same, but are related to different operations are here as different entries.
     */
    Map<Entity, BigDecimal> getProductComponentQuantities(final Entity technology, final BigDecimal givenQuantity,
            Map<Entity, BigDecimal> operationRuns);

    /**
     * 
     * @param orders
     *            List of orders
     * 
     * @return Map with operationProductComponents (in or out) as the keys and its quantities as the values. Be aware that
     *         products that are the same, but are related to different operations are here as different entries.
     */
    Map<Entity, BigDecimal> getProductComponentQuantities(final List<Entity> orders);

    /**
     * 
     * @param orders
     *            List of orders
     * 
     * @param operationRuns
     *            Method takes an empty map and puts here info on how many certain operations (operationComponents) have to be
     *            run.
     * 
     * @return Map with operationProductComponents (in or out) as the keys and its quantities as the values. Be aware that
     *         products that are the same, but are related to different operations are here as different entries.
     */
    Map<Entity, BigDecimal> getProductComponentQuantities(final List<Entity> orders, Map<Entity, BigDecimal> operationRuns);

    /**
     * 
     * @param orders
     *            Given list of orders
     * 
     * @return Map of products and their quantities (products that occur in multiple operations or even in multiple orders are
     *         aggregated)
     */
    Map<Entity, BigDecimal> getProductComponentQuantitiesWithoutNonComponents(final List<Entity> orders);

    /**
     * 
     * @param technology
     *            Given technology
     * 
     * @param givenQuantity
     *            How many products, that are outcomes of this technology, we want.
     * 
     * @param mrpAlgorithm
     *            MRP Algorithm
     * 
     * @return Map with product as the key and its quantity as the value. This time keys are products, so they are aggregated.
     */
    Map<Entity, BigDecimal> getNeededProductQuantities(final Entity technology, final BigDecimal givenQuantity,
            final MrpAlgorithm mrpAlgorithm);

    /**
     * 
     * @param orders
     *            Given list of orders
     * 
     * @param mrpAlgorithm
     *            MRP Algorithm
     * 
     * @return Map of products and their quantities (products that occur in multiple operations or even in multiple orders are
     *         aggregated)
     */
    Map<Entity, BigDecimal> getNeededProductQuantities(final List<Entity> orders, final MrpAlgorithm mrpAlgorithm);

    /**
     * 
     * @param orders
     *            Given list of orders
     * 
     * @param mrpAlgorithm
     *            MRP Algorithm
     * 
     * @param operationRuns
     *            Method takes an empty map and puts here info on how many times certain operation (operationComponent) has to be
     *            run.
     * 
     * @return Map of products and their quantities (products that occur in multiple operations or even in multiple orders are
     *         aggregated)
     */
    Map<Entity, BigDecimal> getNeededProductQuantities(final List<Entity> orders, MrpAlgorithm mrpAlgorithm,
            Map<Entity, BigDecimal> operationRuns);

    /**
     * 
     * @param components
     *            List of components that have order as belongsTo relation
     * 
     * @param mrpAlgorithm
     *            MRP Algorithm
     * 
     * @return Map of products and their quantities (products that occur in multiple operations or even in multiple orders are
     *         aggregated)
     */
    Map<Entity, BigDecimal> getNeededProductQuantitiesForComponents(final List<Entity> components, final MrpAlgorithm mrpAlgorithm);

    /**
     * 
     * @param productComponentQuantity
     *            Product Component Quantity
     * 
     * @param productQuantities
     *            Product Quantities
     */
    void addProductQuantitiesToList(final Entry<Entity, BigDecimal> productComponentQuantity,
            final Map<Entity, BigDecimal> productQuantities);

    /**
     * 
     * @param operationComponent
     *            Operation Component
     * 
     * @return
     */
    Entity getOutputProductsFromOperationComponent(final Entity operationComponent);

}
