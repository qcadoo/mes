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

    private static final String PRODUCT_LITERAL = "product";

    /**
     * 
     * @param orders
     *            List of orders
     * @return Map with operationProductComponents (in or out) as the keys and its quantities as the values. Be aware that
     *         products that are the same, but are related to different operations are here as different entries.
     */
    public Map<Entity, BigDecimal> getProductComponentQuantities(final List<Entity> orders) {
        Map<Entity, BigDecimal> productQuantities = new HashMap<Entity, BigDecimal>();
        Set<Entity> nonComponents = new HashSet<Entity>();

        for (Entity order : orders) {
            BigDecimal plannedQty = (BigDecimal) order.getField("plannedQuantity");

            Entity technology = order.getBelongsToField("technology");

            if (technology == null) {
                throw new IllegalStateException("Order doesn't contain technology.");
            }

            fillMapWithQuantitiesForTechnology(technology, plannedQty, productQuantities, nonComponents);
        }

        return productQuantities;
    }

    /**
     * 
     * @param technology
     *            Given technology
     * @param givenQty
     *            How many products, that are outcomes of this technology, we want.
     * @return Map with product as the key and its quantity as the value. This time keys are products, so they are aggregated.
     */
    public Map<Entity, BigDecimal> getNeededProductQuantities(Entity technology, BigDecimal givenQty, boolean onlyComponents) {
        Map<Entity, BigDecimal> productComponentQuantities = new HashMap<Entity, BigDecimal>();
        Set<Entity> nonComponents = new HashSet<Entity>();

        fillMapWithQuantitiesForTechnology(technology, givenQty, productComponentQuantities, nonComponents);

        Map<Entity, BigDecimal> productQuantities = new HashMap<Entity, BigDecimal>();

        for (Entry<Entity, BigDecimal> productComponentQuantity : productComponentQuantities.entrySet()) {
            if ("operationProductInComponent".equals(productComponentQuantity.getKey().getDataDefinition().getName())) {
                if (onlyComponents && nonComponents.contains(productComponentQuantity.getKey())) {
                    continue;
                }

                Entity product = productComponentQuantity.getKey().getBelongsToField(PRODUCT_LITERAL);
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

    /**
     * 
     * @param orders
     *            Given list of orders
     * @param onlyComponents
     *            A flag that indicates if we want only components or intermediates too
     * @return Map of products and their quantities (products that occur in multiple operations or even in multiple orders are
     *         aggregated)
     */
    public Map<Entity, BigDecimal> getNeededProductQuantities(List<Entity> orders, boolean onlyComponents) {
        Map<Entity, BigDecimal> productComponentQuantities = new HashMap<Entity, BigDecimal>();

        Set<Entity> nonComponents = new HashSet<Entity>();

        for (Entity order : orders) {
            BigDecimal plannedQty = (BigDecimal) order.getField("plannedQuantity");

            Entity technology = order.getBelongsToField("technology");

            if (technology == null) {
                throw new IllegalStateException("Order doesn't contain technology.");
            }

            fillMapWithQuantitiesForTechnology(technology, plannedQty, productComponentQuantities, nonComponents);
        }

        Map<Entity, BigDecimal> productQuantities = new HashMap<Entity, BigDecimal>();

        for (Entry<Entity, BigDecimal> productComponentQuantity : productComponentQuantities.entrySet()) {
            if ("operationProductInComponent".equals(productComponentQuantity.getKey().getDataDefinition().getName())) {
                if (onlyComponents && nonComponents.contains(productComponentQuantity.getKey())) {
                    continue;
                }

                Entity product = productComponentQuantity.getKey().getBelongsToField(PRODUCT_LITERAL);
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

    private void fillMapWithQuantitiesForTechnology(Entity technology, BigDecimal givenQty,
            Map<Entity, BigDecimal> productComponentQuantities, Set<Entity> nonComponents) {
        EntityTree tree = technology.getTreeField("operationComponents");

        calculateQuantitiesForNormalAlgorithm(tree, productComponentQuantities, nonComponents, givenQty, technology);
    }

    private void preloadProductQuantities(final EntityTree tree, Map<Entity, BigDecimal> productQuantities) {
        for (Entity operationComponent : tree) {
            for (Entity productComponent : operationComponent.getHasManyField("operationProductInComponents")) {
                BigDecimal neededQty = (BigDecimal) productComponent.getField("quantity");
                productQuantities.put(productComponent, neededQty);
            }

            for (Entity productComponent : operationComponent.getHasManyField("operationProductOutComponents")) {
                BigDecimal neededQty = (BigDecimal) productComponent.getField("quantity");
                productQuantities.put(productComponent, neededQty);
            }
        }
    }

    private void calculateQuantitiesForNormalAlgorithm(final EntityTree tree, Map<Entity, BigDecimal> productQuantities,
            Set<Entity> notComponents, BigDecimal plannedQty, Entity technology) {
        preloadProductQuantities(tree, productQuantities);

        Entity root = tree.getRoot();
        traverse(root, null, productQuantities, notComponents, plannedQty, technology);
    }

    private void traverse(Entity operationComponent, final Entity previousOperationComponent,
            final Map<Entity, BigDecimal> productQuantities, final Set<Entity> nonComponents, final BigDecimal plannedQty,
            final Entity technology) {
        if (previousOperationComponent == null) {
            Entity outProduct = technology.getBelongsToField(PRODUCT_LITERAL);

            for (Entity out : operationComponent.getHasManyField("operationProductOutComponents")) {
                if (out.getBelongsToField(PRODUCT_LITERAL).getId().equals(outProduct.getId())) {
                    BigDecimal outQuantity = productQuantities.get(out);

                    BigDecimal multiplier = plannedQty.divide(outQuantity);

                    productQuantities.put(out, outQuantity.multiply(multiplier));

                    for (Entity currentIn : operationComponent.getHasManyField("operationProductInComponents")) {
                        BigDecimal currentInQuantity = productQuantities.get(currentIn);
                        productQuantities.put(currentIn, currentInQuantity.multiply(multiplier));
                    }

                    break;
                }
            }
        } else {
            for (Entity in : previousOperationComponent.getHasManyField("operationProductInComponents")) {
                boolean isntComponent = false;

                for (Entity out : operationComponent.getHasManyField("operationProductOutComponents")) {
                    if (out.getBelongsToField(PRODUCT_LITERAL).getId().equals(in.getBelongsToField(PRODUCT_LITERAL).getId())) {
                        isntComponent = true;

                        BigDecimal outQuantity = productQuantities.get(out);
                        BigDecimal inQuantity = productQuantities.get(in);

                        BigDecimal multiplier = inQuantity.divide(outQuantity);

                        productQuantities.put(out, outQuantity.multiply(multiplier));

                        for (Entity currentIn : operationComponent.getHasManyField("operationProductInComponents")) {
                            BigDecimal currentInQuantity = productQuantities.get(currentIn);
                            productQuantities.put(currentIn, currentInQuantity.multiply(multiplier));
                        }

                        break;
                    }
                }

                if (isntComponent) {
                    nonComponents.add(in);
                }
            }

        }

        for (Entity child : operationComponent.getHasManyField("children")) {
            traverse(child, operationComponent, productQuantities, nonComponents, plannedQty, technology);
        }
    }
}
