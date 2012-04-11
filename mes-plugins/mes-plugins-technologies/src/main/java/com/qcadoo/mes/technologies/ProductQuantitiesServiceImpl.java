/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.NumberService;

@Service
public class ProductQuantitiesServiceImpl implements ProductQuantitiesService {

    private static final String OPERATION_PRODUCT_IN_COMPONENTS_L = "operationProductInComponents";

    private static final String OPERATION_PRODUCT_OUT_COMPONENTS_L = "operationProductOutComponents";

    private static final String REFERENCE_TECHNOLOGY_L = "referenceTechnology";

    private static final String PRODUCT_L = "product";

    private static final String IN_PRODUCT = "operationProductInComponent";

    private static final String OUT_PRODUCT = "operationProductOutComponent";

    @Autowired
    private NumberService numberService;

    public Map<Entity, BigDecimal> getProductComponentQuantities(final List<Entity> orders) {
        Map<Entity, BigDecimal> operationMultipliers = new HashMap<Entity, BigDecimal>();
        return getProductComponentQuantities(orders, operationMultipliers);
    }

    public Map<Entity, BigDecimal> getProductComponentQuantities(final List<Entity> orders,
            final Map<Entity, BigDecimal> operationRuns) {
        Map<Entity, BigDecimal> productComponentQuantities = new HashMap<Entity, BigDecimal>();

        Set<Entity> nonComponents = new HashSet<Entity>();

        getAllQuantitiesForOrders(orders, false, productComponentQuantities, operationRuns, nonComponents);
        return productComponentQuantities;
    }

    public Map<Entity, BigDecimal> getProductComponentQuantities(final Entity technology, final BigDecimal givenQty,
            final Map<Entity, BigDecimal> operationRuns) {
        Map<Entity, BigDecimal> productComponentQuantities = new HashMap<Entity, BigDecimal>();

        Set<Entity> nonComponents = new HashSet<Entity>();

        fillMapWithQuantitiesForTechnology(technology, givenQty, productComponentQuantities, nonComponents, operationRuns);

        return productComponentQuantities;
    }

    public Map<Entity, BigDecimal> getNeededProductQuantities(final Entity technology, final BigDecimal givenQty,
            final boolean onlyComponents) {
        Map<Entity, BigDecimal> productComponentQuantities = new HashMap<Entity, BigDecimal>();
        Map<Entity, BigDecimal> operationMultipliers = new HashMap<Entity, BigDecimal>();
        Set<Entity> nonComponents = new HashSet<Entity>();

        fillMapWithQuantitiesForTechnology(technology, givenQty, productComponentQuantities, nonComponents, operationMultipliers);

        return getProducts(productComponentQuantities, nonComponents, onlyComponents, IN_PRODUCT);
    }

    public Map<Entity, BigDecimal> getNeededProductQuantities(final List<Entity> orders, final boolean onlyComponents) {
        Map<Entity, BigDecimal> productComponentQuantities = new HashMap<Entity, BigDecimal>();
        Map<Entity, BigDecimal> operationMultipliers = new HashMap<Entity, BigDecimal>();
        Set<Entity> nonComponents = new HashSet<Entity>();

        getAllQuantitiesForOrders(orders, onlyComponents, productComponentQuantities, operationMultipliers, nonComponents);

        return getProducts(productComponentQuantities, nonComponents, onlyComponents, IN_PRODUCT);
    }

    public Map<Entity, BigDecimal> getOutputProductQuantities(final List<Entity> orders) {
        Map<Entity, BigDecimal> productComponentQuantities = new HashMap<Entity, BigDecimal>();
        Map<Entity, BigDecimal> operationMultipliers = new HashMap<Entity, BigDecimal>();
        Set<Entity> nonComponents = new HashSet<Entity>();

        getAllQuantitiesForOrders(orders, false, productComponentQuantities, operationMultipliers, nonComponents);

        return getProducts(productComponentQuantities, nonComponents, false, OUT_PRODUCT);
    }

    public Map<Entity, BigDecimal> getNeededProductQuantities(final List<Entity> orders, final boolean onlyComponents,
            final Map<Entity, BigDecimal> operationRuns) {
        Map<Entity, BigDecimal> productComponentQuantities = new HashMap<Entity, BigDecimal>();
        Set<Entity> nonComponents = new HashSet<Entity>();

        getAllQuantitiesForOrders(orders, onlyComponents, productComponentQuantities, operationRuns, nonComponents);

        return getProducts(productComponentQuantities, nonComponents, onlyComponents, IN_PRODUCT);
    }

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
            final Map<Entity, BigDecimal> operationRuns) {
        for (Entity operationComponent : tree) {
            if (REFERENCE_TECHNOLOGY_L.equals(operationComponent.getStringField("entityType"))) {
                Entity refTech = operationComponent.getBelongsToField(REFERENCE_TECHNOLOGY_L);
                EntityTree refTree = refTech.getTreeField("operationComponents");
                preloadProductQuantitiesAndOperationRuns(refTree, productQuantities, operationRuns);
                continue;
            }

            for (Entity productComponent : operationComponent.getHasManyField(OPERATION_PRODUCT_IN_COMPONENTS_L)) {
                BigDecimal neededQty = (BigDecimal) productComponent.getField("quantity");
                productQuantities.put(productComponent, neededQty);
            }

            for (Entity productComponent : operationComponent.getHasManyField(OPERATION_PRODUCT_OUT_COMPONENTS_L)) {
                BigDecimal neededQty = (BigDecimal) productComponent.getField("quantity");
                productQuantities.put(productComponent, neededQty);
            }

            operationRuns.put(operationComponent, BigDecimal.ONE);
        }
    }

    private void multiplyQuantities(final BigDecimal needed, final BigDecimal actual, final Entity operationComponent,
            final Map<Entity, BigDecimal> mapWithQuantities, final Map<Entity, BigDecimal> operationRuns) {
        BigDecimal multiplier = needed.divide(actual, numberService.getMathContext());

        if (!operationComponent.getBooleanField("areProductQuantitiesDivisible")) {
            multiplier = multiplier.setScale(0, RoundingMode.CEILING); // It's intentional to round up the operation runs
        }

        BigDecimal runs = multiplier;

        if (!operationComponent.getBooleanField("isTjDivisible")) {
            runs = multiplier.setScale(0, RoundingMode.CEILING);
        }

        operationRuns.put(operationComponent, runs);

        for (Entity currentOut : operationComponent.getHasManyField(OPERATION_PRODUCT_OUT_COMPONENTS_L)) {
            BigDecimal currentOutQty = mapWithQuantities.get(currentOut);
            mapWithQuantities.put(currentOut, currentOutQty.multiply(multiplier, numberService.getMathContext()));
        }

        for (Entity currentIn : operationComponent.getHasManyField(OPERATION_PRODUCT_IN_COMPONENTS_L)) {
            BigDecimal currentInQuantity = mapWithQuantities.get(currentIn);
            mapWithQuantities.put(currentIn, currentInQuantity.multiply(multiplier, numberService.getMathContext()));
        }

    }

    private void traverse(final Entity operationComponent, final Entity previousOperationComponent,
            final Map<Entity, BigDecimal> productQuantities, final Set<Entity> nonComponents, final BigDecimal plannedQty,
            final Entity technology, final Map<Entity, BigDecimal> operationRuns) {
        if (REFERENCE_TECHNOLOGY_L.equals(operationComponent.getStringField("entityType"))) {
            Entity refTech = operationComponent.getBelongsToField(REFERENCE_TECHNOLOGY_L);
            EntityTree refTree = refTech.getTreeField("operationComponents");
            traverse(refTree.getRoot(), previousOperationComponent, productQuantities, nonComponents, plannedQty, refTech,
                    operationRuns);
            return;
        }

        if (previousOperationComponent == null) {
            Entity outProduct = technology.getBelongsToField(PRODUCT_L);

            for (Entity out : operationComponent.getHasManyField(OPERATION_PRODUCT_OUT_COMPONENTS_L)) {
                if (out.getBelongsToField(PRODUCT_L).getId().equals(outProduct.getId())) {

                    BigDecimal outQuantity = productQuantities.get(out);

                    multiplyQuantities(plannedQty, outQuantity, operationComponent, productQuantities, operationRuns);

                    break;
                }
            }
        } else {
            for (Entity in : previousOperationComponent.getHasManyField(OPERATION_PRODUCT_IN_COMPONENTS_L)) {
                boolean isntComponent = false;

                for (Entity out : operationComponent.getHasManyField(OPERATION_PRODUCT_OUT_COMPONENTS_L)) {
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
