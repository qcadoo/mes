/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
import java.util.Set;

import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.mes.technologies.dto.ProductQuantitiesHolder;
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
    OperationProductComponentWithQuantityContainer getProductComponentQuantities(final Entity technology,
            final BigDecimal givenQuantity, Map<Long, BigDecimal> operationRuns);

    ProductQuantitiesHolder getProductComponentQuantities(final Entity technology, final BigDecimal givenQuantity);

    /**
     * 
     * @param orders
     *            List of orders
     * 
     * @return Map with operationProductComponents (in or out) as the keys and its quantities as the values. Be aware that
     *         products that are the same, but are related to different operations are here as different entries.
     */
    OperationProductComponentWithQuantityContainer getProductComponentQuantities(final List<Entity> orders);

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
    OperationProductComponentWithQuantityContainer getProductComponentQuantities(final List<Entity> orders,
            Map<Long, BigDecimal> operationRuns);

    /**
     * 
     * @param orders
     *            Given list of orders
     * 
     * @return Map of products and their quantities (products that occur in multiple operations or even in multiple orders are
     *         aggregated)
     */
    OperationProductComponentWithQuantityContainer getProductComponentQuantitiesWithoutNonComponents(final List<Entity> orders);

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
    Map<Long, BigDecimal> getNeededProductQuantities(final Entity technology, final BigDecimal givenQuantity,
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
    Map<Long, BigDecimal> getNeededProductQuantities(final List<Entity> orders, final MrpAlgorithm mrpAlgorithm);

    /**
     * 
     * @param orders
     *            Given list of orders
     * 
     * @param mrpAlgorithm
     *            MRP Algorithm
     * 
     * @param onTheFly
     *            onTheFly
     * 
     * @return Map of products and their quantities (products that occur in multiple operations or even in multiple orders are
     *         aggregated)
     */
    Map<Long, BigDecimal> getNeededProductQuantities(final List<Entity> orders, final MrpAlgorithm mrpAlgorithm,
            final boolean onTheFly);

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
    Map<Long, BigDecimal> getNeededProductQuantities(final List<Entity> orders, MrpAlgorithm mrpAlgorithm,
            Map<Long, BigDecimal> operationRuns);

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
    Map<Long, BigDecimal> getNeededProductQuantitiesForComponents(final List<Entity> components, final MrpAlgorithm mrpAlgorithm);

    /**
     * 
     * @param orders
     *            orders
     * 
     * @param operationRuns
     *            Method takes an empty map and puts here info on how many times certain operation (operationComponent) has to be
     *            run.
     * 
     * @param nonComponents
     *            non components
     * 
     * @return Map with operationProductComponents (in or out) as the keys and its quantities as the values. Be aware that
     *         products that are the same, but are related to different operations are here as different entries.
     * 
     */
    OperationProductComponentWithQuantityContainer getProductComponentWithQuantities(final List<Entity> orders,
            final Map<Long, BigDecimal> operationRuns, final Set<OperationProductComponentHolder> nonComponents);

    /**
     * 
     * @param productComponentQuantity
     *            Product Component Quantity
     * 
     * @param productQuantities
     *            Product Quantities
     */
    void addProductQuantitiesToList(final Entry<OperationProductComponentHolder, BigDecimal> productComponentQuantity,
            final Map<Long, BigDecimal> productQuantities);

    /**
     * 
     * @param operationComponent
     *            Operation Component
     * 
     * @return
     */
    Entity getOutputProductsFromOperationComponent(final Entity operationComponent);

    /**
     * 
     * @param technology
     * @param givenQuantity
     * @param operationRuns
     * @param nonComponents
     * @return
     */
    OperationProductComponentWithQuantityContainer getProductComponentWithQuantitiesForTechnology(final Entity technology,
            final BigDecimal givenQuantity, final Map<Long, BigDecimal> operationRuns,
            final Set<OperationProductComponentHolder> nonComponents);

    /**
     * 
     * @param productComponentWithQuantitiesForOrders
     * @return
     */
    OperationProductComponentWithQuantityContainer groupOperationProductComponentWithQuantities(
            final Map<Long, OperationProductComponentWithQuantityContainer> productComponentWithQuantitiesForOrders);

    /**
     * Gets technology operation component
     * 
     * @param technologyOperationComponentId
     * 
     * @return technology operation component
     */
    Entity getTechnologyOperationComponent(final Long technologyOperationComponentId);

    /**
     * Gets product
     * 
     * @param productId
     * 
     * @return product
     */
    Entity getProduct(final Long productId);

    /**
     * Covers operations runs from product quantities
     * 
     * @param operationRunsFromProductionQuantities
     * 
     * @return operations runs
     */
    Map<Entity, BigDecimal> convertOperationsRunsFromProductQuantities(
            final Map<Long, BigDecimal> operationRunsFromProductionQuantities);

}
