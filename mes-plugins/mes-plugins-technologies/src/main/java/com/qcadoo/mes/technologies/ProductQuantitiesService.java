/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

@Service
public class ProductQuantitiesService {

    private static final String PRODUCT_L = "product";

    private static final String IN_PRODUCT = "operationProductInComponent";

    private static final String OUT_PRODUCT = "operationProductOutComponent";

    /**
     * 
     * @param orders
     *            List of orders
     * @return Map with operationProductComponents (in or out) as the keys and its quantities as the values. Be aware that
     *         products that are the same, but are related to different operations are here as different entries.
     */
    public Map<Entity, BigDecimal> getProductComponentQuantities(final List<Entity> orders) {
        Map<Entity, BigDecimal> operationMultipliers = new HashMap<Entity, BigDecimal>();
        return getProductComponentQuantities(orders, operationMultipliers);
    }

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
    public Map<Entity, BigDecimal> getProductComponentQuantities(final List<Entity> orders, Map<Entity, BigDecimal> operationRuns) {
        Map<Entity, BigDecimal> productComponentQuantities = new HashMap<Entity, BigDecimal>();

        Set<Entity> nonComponents = new HashSet<Entity>();

        getAllQuantitiesForOrders(orders, false, productComponentQuantities, operationRuns, nonComponents);
        return productComponentQuantities;
    }

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
    public Map<Entity, BigDecimal> getProductComponentQuantities(final Entity technology, final BigDecimal givenQty,
            Map<Entity, BigDecimal> operationRuns) {
        Map<Entity, BigDecimal> productComponentQuantities = new HashMap<Entity, BigDecimal>();

        Set<Entity> nonComponents = new HashSet<Entity>();

        fillMapWithQuantitiesForTechnology(technology, givenQty, productComponentQuantities, nonComponents, operationRuns);

        return productComponentQuantities;
    }

    /**
     * 
     * @param technology
     *            Given technology
     * @param givenQty
     *            How many products, that are outcomes of this technology, we want.
     * @return Map with product as the key and its quantity as the value. This time keys are products, so they are aggregated.
     */
    public Map<Entity, BigDecimal> getNeededProductQuantities(final Entity technology, final BigDecimal givenQty,
            final boolean onlyComponents) {
        Map<Entity, BigDecimal> productComponentQuantities = new HashMap<Entity, BigDecimal>();
        Map<Entity, BigDecimal> operationMultipliers = new HashMap<Entity, BigDecimal>();
        Set<Entity> nonComponents = new HashSet<Entity>();

        fillMapWithQuantitiesForTechnology(technology, givenQty, productComponentQuantities, nonComponents, operationMultipliers);

        return getProducts(productComponentQuantities, nonComponents, onlyComponents, IN_PRODUCT);
    }

    /**
     * 
     * @param orders
     *            Given list of orders
     * @param onlyComponents
     *            A flag that indicates if we want only components or intermediates too
     * @return Map of products and their quantities (products that occur in multiple operations or even in multiple orders are
     *         aggregated)
     */
    public Map<Entity, BigDecimal> getNeededProductQuantities(final List<Entity> orders, final boolean onlyComponents) {
        Map<Entity, BigDecimal> productComponentQuantities = new HashMap<Entity, BigDecimal>();
        Map<Entity, BigDecimal> operationMultipliers = new HashMap<Entity, BigDecimal>();
        Set<Entity> nonComponents = new HashSet<Entity>();

        getAllQuantitiesForOrders(orders, onlyComponents, productComponentQuantities, operationMultipliers, nonComponents);

        return getProducts(productComponentQuantities, nonComponents, onlyComponents, IN_PRODUCT);
    }

    /**
     * 
     * @param orders
     *            Given list of orders
     * @return Map of output products and their quantities (products that occur in multiple operations or even in multiple orders
     *         are aggregated)
     */
    public Map<Entity, BigDecimal> getOutputProductQuantities(final List<Entity> orders) {
        Map<Entity, BigDecimal> productComponentQuantities = new HashMap<Entity, BigDecimal>();
        Map<Entity, BigDecimal> operationMultipliers = new HashMap<Entity, BigDecimal>();
        Set<Entity> nonComponents = new HashSet<Entity>();

        getAllQuantitiesForOrders(orders, false, productComponentQuantities, operationMultipliers, nonComponents);

        return getProducts(productComponentQuantities, nonComponents, false, OUT_PRODUCT);
    }

    /**
     * 
     * @param orders
     *            Given list of orders
     * @param onlyComponents
     *            A flag that indicates if we want only components or intermediates too
     * @param operationRuns
     *            Method takes an empty map and puts here info on how many certain operations (operationComponents) have to be
     *            run.
     * @return Map of products and their quantities (products that occur in multiple operations or even in multiple orders are
     *         aggregated)
     */
    public Map<Entity, BigDecimal> getNeededProductQuantities(final List<Entity> orders, boolean onlyComponents,
            Map<Entity, BigDecimal> operationRuns) {
        Map<Entity, BigDecimal> productComponentQuantities = new HashMap<Entity, BigDecimal>();
        Set<Entity> nonComponents = new HashSet<Entity>();

        getAllQuantitiesForOrders(orders, onlyComponents, productComponentQuantities, operationRuns, nonComponents);

        return getProducts(productComponentQuantities, nonComponents, onlyComponents, IN_PRODUCT);
    }

    /**
     * 
     * @param components
     *            List of components that have order as belongsTo relation
     * @param onlyComponents
     *            A flag that indicates if we want only components or intermediates too
     * @return Map of products and their quantities (products that occur in multiple operations or even in multiple orders are
     *         aggregated)
     */
    public Map<Entity, BigDecimal> getNeededProductQuantitiesForComponents(final List<Entity> components,
            final boolean onlyComponents) {
        Map<Entity, BigDecimal> productComponentQuantities = new HashMap<Entity, BigDecimal>();
        Map<Entity, BigDecimal> operationMultipliers = new HashMap<Entity, BigDecimal>();

        Set<Entity> nonComponents = new HashSet<Entity>();

        for (Entity component : components) {
            Entity order = component.getBelongsToField("order");

            if (order == null) {
                throw new IllegalStateException(
                        "Given component doesn't point to an order using getBelongsToField(\"order\") relation");
            }

            BigDecimal plannedQty = (BigDecimal) order.getField("plannedQuantity");

            Entity technology = order.getBelongsToField("technology");

            if (technology == null) {
                throw new IllegalStateException("Order doesn't contain technology.");
            }

            fillMapWithQuantitiesForTechnology(technology, plannedQty, productComponentQuantities, nonComponents,
                    operationMultipliers);
        }

        return getProducts(productComponentQuantities, nonComponents, onlyComponents, IN_PRODUCT);
    }

    private void getAllQuantitiesForOrders(final List<Entity> orders, final boolean onlyComponents,
            final Map<Entity, BigDecimal> productComponentQuantities, final Map<Entity, BigDecimal> operationRuns,
            final Set<Entity> nonComponents) {
        for (Entity order : orders) {
            BigDecimal plannedQty = (BigDecimal) order.getField("plannedQuantity");

            Entity technology = order.getBelongsToField("technology");

            if (technology == null) {
                throw new IllegalStateException("Order doesn't contain technology.");
            }

            fillMapWithQuantitiesForTechnology(technology, plannedQty, productComponentQuantities, nonComponents, operationRuns);
        }
    }

    private Map<Entity, BigDecimal> getProducts(final Map<Entity, BigDecimal> productComponentQuantities,
            final Set<Entity> nonComponents, final boolean onlyComponents, final String type) {
        Map<Entity, BigDecimal> productQuantities = new HashMap<Entity, BigDecimal>();

        for (Entry<Entity, BigDecimal> productComponentQuantity : productComponentQuantities.entrySet()) {
            if (type.equals(productComponentQuantity.getKey().getDataDefinition().getName())) {
                if (onlyComponents && nonComponents.contains(productComponentQuantity.getKey())) {
                    continue;
                }

                Entity product = productComponentQuantity.getKey().getBelongsToField(PRODUCT_L);
                BigDecimal newQty = productComponentQuantity.getValue();

                BigDecimal oldQty = productQuantities.get(product);
                if (oldQty != null) {
                    newQty = newQty.add(oldQty);

                }
                productQuantities.put(product, newQty);
            }
        }

        return productQuantities;
    }

    private void fillMapWithQuantitiesForTechnology(final Entity technology, final BigDecimal givenQty,
            final Map<Entity, BigDecimal> productComponentQuantities, final Set<Entity> nonComponents,
            final Map<Entity, BigDecimal> operationRuns) {
        EntityTree tree = technology.getTreeField("operationComponents");

        preloadProductQuantitiesAndOperationRuns(tree, productComponentQuantities, operationRuns);

        Entity root = tree.getRoot();
        traverse(root, null, productComponentQuantities, nonComponents, givenQty, technology, operationRuns);
    }

    private void preloadProductQuantitiesAndOperationRuns(final EntityTree tree, final Map<Entity, BigDecimal> productQuantities,
            Map<Entity, BigDecimal> operationRuns) {
        for (Entity operationComponent : tree) {
            if ("referenceTechnology".equals(operationComponent.getStringField("entityType"))) {
                Entity refTech = operationComponent.getBelongsToField("referenceTechnology");
                EntityTree refTree = refTech.getTreeField("operationComponents");
                preloadProductQuantitiesAndOperationRuns(refTree, productQuantities, operationRuns);
                continue;
            }

            for (Entity productComponent : operationComponent.getHasManyField("operationProductInComponents")) {
                BigDecimal neededQty = (BigDecimal) productComponent.getField("quantity");
                productQuantities.put(productComponent, neededQty);
            }

            for (Entity productComponent : operationComponent.getHasManyField("operationProductOutComponents")) {
                BigDecimal neededQty = (BigDecimal) productComponent.getField("quantity");
                productQuantities.put(productComponent, neededQty);
            }

            operationRuns.put(operationComponent, new BigDecimal(1));
        }
    }

    private void multiplyQuantities(final BigDecimal needed, final BigDecimal actual, final Entity operationComponent,
            final Map<Entity, BigDecimal> mapWithQuantities, final Map<Entity, BigDecimal> operationRuns) {
        BigDecimal multiplier = needed.divide(actual, 0, RoundingMode.CEILING);

        operationRuns.put(operationComponent, multiplier);

        for (Entity currentOut : operationComponent.getHasManyField("operationProductOutComponents")) {
            BigDecimal currentOutQty = mapWithQuantities.get(currentOut);
            mapWithQuantities.put(currentOut, currentOutQty.multiply(multiplier));
        }

        for (Entity currentIn : operationComponent.getHasManyField("operationProductInComponents")) {
            BigDecimal currentInQuantity = mapWithQuantities.get(currentIn);
            mapWithQuantities.put(currentIn, currentInQuantity.multiply(multiplier));
        }

    }

    private void traverse(final Entity operationComponent, final Entity previousOperationComponent,
            final Map<Entity, BigDecimal> productQuantities, final Set<Entity> nonComponents, final BigDecimal plannedQty,
            final Entity technology, final Map<Entity, BigDecimal> operationRuns) {
        if ("referenceTechnology".equals(operationComponent.getStringField("entityType"))) {
            Entity refTech = operationComponent.getBelongsToField("referenceTechnology");
            EntityTree refTree = refTech.getTreeField("operationComponents");
            traverse(refTree.getRoot(), previousOperationComponent, productQuantities, nonComponents, plannedQty, refTech,
                    operationRuns);
            return;
        }

        if (previousOperationComponent == null) {
            Entity outProduct = technology.getBelongsToField(PRODUCT_L);

            for (Entity out : operationComponent.getHasManyField("operationProductOutComponents")) {
                if (out.getBelongsToField(PRODUCT_L).getId().equals(outProduct.getId())) {

                    BigDecimal outQuantity = productQuantities.get(out);

                    multiplyQuantities(plannedQty, outQuantity, operationComponent, productQuantities, operationRuns);

                    break;
                }
            }
        } else {
            for (Entity in : previousOperationComponent.getHasManyField("operationProductInComponents")) {
                boolean isntComponent = false;

                for (Entity out : operationComponent.getHasManyField("operationProductOutComponents")) {
                    if (out.getBelongsToField(PRODUCT_L).getId().equals(in.getBelongsToField(PRODUCT_L).getId())) {
                        isntComponent = true;

                        BigDecimal outQuantity = productQuantities.get(out);
                        BigDecimal inQuantity = productQuantities.get(in);

                        multiplyQuantities(inQuantity, outQuantity, operationComponent, productQuantities, operationRuns);

                        break;
                    }
                }

                if (isntComponent) {
                    nonComponents.add(in);
                }
            }

        }

        for (Entity child : operationComponent.getHasManyField("children")) {
            traverse(child, operationComponent, productQuantities, nonComponents, plannedQty, technology, operationRuns);
        }
    }
}
