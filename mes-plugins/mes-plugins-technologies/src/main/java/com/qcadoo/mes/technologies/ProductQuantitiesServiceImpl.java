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

import static com.qcadoo.mes.technologies.constants.OperationProductInComponentFields.PRODUCT;
import static com.qcadoo.mes.technologies.constants.OperationProductInComponentFields.QUANTITY;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.OPERATION_COMPONENTS;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.ARE_PRODUCT_QUANTITIES_DIVISIBLE;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.CHILDREN;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.ENTITY_TYPE;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.IS_TJ_DIVISIBLE;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.PARENT;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.REFERENCETECHNOLOGY;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.NumberService;

@Service
public class ProductQuantitiesServiceImpl implements ProductQuantitiesService {

    @Autowired
    private NumberService numberService;

    @Override
    public Map<Entity, BigDecimal> getProductComponentQuantities(final Entity technology, final BigDecimal givenQuantity,
            final Map<Entity, BigDecimal> operationRuns) {
        Set<Entity> nonComponents = Sets.newHashSet();

        return getProductComponentWithQuantitiesForTechnology(technology, givenQuantity, nonComponents, operationRuns);
    }

    @Override
    public Map<Entity, BigDecimal> getProductComponentQuantities(final List<Entity> orders) {
        Map<Entity, BigDecimal> operationRuns = Maps.newHashMap();

        return getProductComponentQuantities(orders, operationRuns);
    }

    @Override
    public Map<Entity, BigDecimal> getProductComponentQuantities(final List<Entity> orders,
            final Map<Entity, BigDecimal> operationRuns) {
        Set<Entity> nonComponents = Sets.newHashSet();

        return getProductComponentWithQuantitiesForOrders(orders, operationRuns, nonComponents);
    }

    @Override
    public Map<Entity, BigDecimal> getProductComponentQuantitiesWithoutNonComponents(final List<Entity> orders) {
        Map<Entity, BigDecimal> operationRuns = Maps.newHashMap();
        Set<Entity> nonComponents = Sets.newHashSet();

        Map<Entity, BigDecimal> productComponentWithQuantities = getProductComponentWithQuantitiesForOrders(orders,
                operationRuns, nonComponents);

        return getProductComponentWithQuantitiesWithoutNonComponents(productComponentWithQuantities, nonComponents);
    }

    @Override
    public Map<Entity, BigDecimal> getNeededProductQuantities(final Entity technology, final BigDecimal givenQuantity,
            final MrpAlgorithm mrpAlgorithm) {
        Map<Entity, BigDecimal> operationRuns = Maps.newHashMap();
        Set<Entity> nonComponents = Sets.newHashSet();

        Map<Entity, BigDecimal> productComponentWithQuantities = getProductComponentWithQuantitiesForTechnology(technology,
                givenQuantity, nonComponents, operationRuns);

        return getProductWithQuantities(productComponentWithQuantities, nonComponents, mrpAlgorithm,
                MODEL_OPERATION_PRODUCT_IN_COMPONENT);
    }

    @Override
    public Map<Entity, BigDecimal> getNeededProductQuantities(final List<Entity> orders, final MrpAlgorithm mrpAlgorithm) {
        Map<Entity, BigDecimal> operationRuns = Maps.newHashMap();

        return getNeededProductQuantities(orders, mrpAlgorithm, operationRuns);
    }

    @Override
    public Map<Entity, BigDecimal> getNeededProductQuantities(final List<Entity> orders, final MrpAlgorithm mrpAlgorithm,
            final Map<Entity, BigDecimal> operationRuns) {
        Set<Entity> nonComponents = Sets.newHashSet();

        Map<Entity, BigDecimal> productComponentWithQuantities = getProductComponentWithQuantitiesForOrders(orders,
                operationRuns, nonComponents);

        return getProductWithQuantities(productComponentWithQuantities, nonComponents, mrpAlgorithm,
                MODEL_OPERATION_PRODUCT_IN_COMPONENT);
    }

    @Override
    public Map<Entity, BigDecimal> getNeededProductQuantitiesForComponents(final List<Entity> components,
            final MrpAlgorithm mrpAlgorithm) {
        Map<Entity, BigDecimal> operationRuns = Maps.newHashMap();
        Set<Entity> nonComponents = Sets.newHashSet();

        Map<Entity, BigDecimal> productComponentWithQuantities = getProductComponentWithQuantitiesForOrders(
                getOrdersFromComponents(components), operationRuns, nonComponents);

        return getProductWithQuantities(productComponentWithQuantities, nonComponents, mrpAlgorithm,
                MODEL_OPERATION_PRODUCT_IN_COMPONENT);
    }

    private Map<Entity, BigDecimal> getProductComponentWithQuantitiesForTechnology(final Entity technology,
            final BigDecimal givenQuantity, final Set<Entity> nonComponents, final Map<Entity, BigDecimal> operationRuns) {
        Map<Entity, BigDecimal> productComponentWithQuantities = Maps.newHashMap();

        EntityTree operationComponents = technology.getTreeField(OPERATION_COMPONENTS);
        Entity root = operationComponents.getRoot();

        preloadProductQuantitiesAndOperationRuns(operationComponents, productComponentWithQuantities, operationRuns);
        traverseProductQuantitiesAndOperationRuns(technology, givenQuantity, root, null, productComponentWithQuantities,
                nonComponents, operationRuns);

        return productComponentWithQuantities;
    }

    private Map<Entity, BigDecimal> getProductComponentWithQuantitiesForOrders(final List<Entity> orders,
            final Map<Entity, BigDecimal> operationRuns, final Set<Entity> nonComponents) {
        Map<Long, Map<Entity, BigDecimal>> productComponentWithQuantitiesForOrders = Maps.newHashMap();

        for (Entity order : orders) {
            BigDecimal plannedQuantity = (BigDecimal) order.getField("plannedQuantity");

            Entity technology = order.getBelongsToField("technology");

            if (technology == null) {
                throw new IllegalStateException("Order doesn't contain technology.");
            }

            productComponentWithQuantitiesForOrders.put(order.getId(),
                    getProductComponentWithQuantitiesForTechnology(technology, plannedQuantity, nonComponents, operationRuns));
        }

        return groupProductComponentWithQuantities(productComponentWithQuantitiesForOrders);
    }

    private Map<Entity, BigDecimal> groupProductComponentWithQuantities(
            final Map<Long, Map<Entity, BigDecimal>> productComponentWithQuantitiesForOrders) {
        Map<Entity, BigDecimal> productComponentWithQuantities = Maps.newHashMap();

        for (Entry<Long, Map<Entity, BigDecimal>> productComponentWithQuantitiesForOrder : productComponentWithQuantitiesForOrders
                .entrySet()) {

            for (Entry<Entity, BigDecimal> productComponentWithQuantity : productComponentWithQuantitiesForOrder.getValue()
                    .entrySet()) {
                Entity operationProductComponent = productComponentWithQuantity.getKey();
                BigDecimal quantity = productComponentWithQuantity.getValue();

                if (productComponentWithQuantities.containsKey(operationProductComponent)) {
                    BigDecimal addedQuantity = productComponentWithQuantities.get(operationProductComponent);

                    quantity = quantity.add(addedQuantity, numberService.getMathContext());

                    productComponentWithQuantities.put(operationProductComponent, quantity);
                } else {
                    productComponentWithQuantities.put(operationProductComponent, quantity);
                }
            }
        }

        return productComponentWithQuantities;
    }

    private void preloadProductQuantitiesAndOperationRuns(final EntityTree operationComponents,
            final Map<Entity, BigDecimal> productComponentWithQuantities, final Map<Entity, BigDecimal> operationRuns) {
        for (Entity operationComponent : operationComponents) {
            if ("referenceTechnology".equals(operationComponent.getStringField(ENTITY_TYPE))) {
                Entity referenceTechnology = operationComponent.getBelongsToField(REFERENCETECHNOLOGY);
                EntityTree referenceOperationComponents = referenceTechnology.getTreeField(OPERATION_COMPONENTS);

                preloadProductQuantitiesAndOperationRuns(referenceOperationComponents, productComponentWithQuantities,
                        operationRuns);

                continue;
            }

            preloadOperationProductComponentQuantity(operationComponent.getHasManyField(OPERATION_PRODUCT_IN_COMPONENTS),
                    productComponentWithQuantities);
            preloadOperationProductComponentQuantity(operationComponent.getHasManyField(OPERATION_PRODUCT_OUT_COMPONENTS),
                    productComponentWithQuantities);

            operationRuns.put(operationComponent, BigDecimal.ONE);
        }
    }

    private void preloadOperationProductComponentQuantity(final List<Entity> operationProductComponents,
            final Map<Entity, BigDecimal> productComponentWithQuantities) {
        for (Entity operationProductComponent : operationProductComponents) {
            BigDecimal neededQuantity = operationProductComponent.getDecimalField(QUANTITY);

            productComponentWithQuantities.put(operationProductComponent, neededQuantity);
        }
    }

    private void traverseProductQuantitiesAndOperationRuns(final Entity technology, final BigDecimal givenQuantity,
            final Entity operationComponent, final Entity previousOperationComponent,
            final Map<Entity, BigDecimal> productComponentWithQuantities, final Set<Entity> nonComponents,
            final Map<Entity, BigDecimal> operationRuns) {
        if ("referenceTechnology".equals(operationComponent.getStringField(ENTITY_TYPE))) {
            Entity referenceTechnology = operationComponent.getBelongsToField(REFERENCETECHNOLOGY);
            EntityTree referenceOperationComponent = referenceTechnology.getTreeField(OPERATION_COMPONENTS);

            traverseProductQuantitiesAndOperationRuns(referenceTechnology, givenQuantity, referenceOperationComponent.getRoot(),
                    previousOperationComponent, productComponentWithQuantities, nonComponents, operationRuns);

            return;
        }

        if (previousOperationComponent == null) {
            Entity technologyProduct = technology.getBelongsToField(PRODUCT);

            for (Entity operationProductOutComponent : operationComponent.getHasManyField(OPERATION_PRODUCT_OUT_COMPONENTS)) {
                if (operationProductOutComponent.getBelongsToField(PRODUCT).getId().equals(technologyProduct.getId())) {
                    BigDecimal outQuantity = productComponentWithQuantities.get(operationProductOutComponent);

                    multiplyProductQuantitiesAndAddOperationRuns(operationComponent, givenQuantity, outQuantity,
                            productComponentWithQuantities, operationRuns);

                    break;
                }
            }
        } else {
            for (Entity operationProductInComponent : previousOperationComponent.getHasManyField(OPERATION_PRODUCT_IN_COMPONENTS)) {
                boolean isntComponent = false;

                for (Entity operationProductOutComponent : operationComponent.getHasManyField(OPERATION_PRODUCT_OUT_COMPONENTS)) {
                    if (operationProductOutComponent.getBelongsToField(PRODUCT).getId()
                            .equals(operationProductInComponent.getBelongsToField(PRODUCT).getId())) {
                        isntComponent = true;

                        BigDecimal outQuantity = productComponentWithQuantities.get(operationProductOutComponent);
                        BigDecimal inQuantity = productComponentWithQuantities.get(operationProductInComponent);

                        multiplyProductQuantitiesAndAddOperationRuns(operationComponent, inQuantity, outQuantity,
                                productComponentWithQuantities, operationRuns);

                        break;
                    }
                }

                if (isntComponent) {
                    nonComponents.add(operationProductInComponent);
                }
            }
        }

        for (Entity child : operationComponent.getHasManyField(CHILDREN)) {
            traverseProductQuantitiesAndOperationRuns(technology, givenQuantity, child, operationComponent,
                    productComponentWithQuantities, nonComponents, operationRuns);
        }
    }

    private void multiplyProductQuantitiesAndAddOperationRuns(final Entity operationComponent, final BigDecimal needed,
            final BigDecimal actual, final Map<Entity, BigDecimal> productComponentWithQuantities,
            final Map<Entity, BigDecimal> operationRuns) {
        BigDecimal multiplier = needed.divide(actual, numberService.getMathContext());

        if (!operationComponent.getBooleanField(ARE_PRODUCT_QUANTITIES_DIVISIBLE)) {
            // It's intentional to round up the operation runs
            multiplier = multiplier.setScale(0, RoundingMode.CEILING);
        }

        BigDecimal runs = multiplier;

        if (!operationComponent.getBooleanField(IS_TJ_DIVISIBLE)) {
            runs = multiplier.setScale(0, RoundingMode.CEILING);
        }

        operationRuns.put(operationComponent, runs);

        multiplyOperationProductComponentQuantities(operationComponent.getHasManyField(OPERATION_PRODUCT_IN_COMPONENTS),
                multiplier, productComponentWithQuantities);
        multiplyOperationProductComponentQuantities(operationComponent.getHasManyField(OPERATION_PRODUCT_OUT_COMPONENTS),
                multiplier, productComponentWithQuantities);
    }

    private void multiplyOperationProductComponentQuantities(final List<Entity> operationProductComponents,
            final BigDecimal multiplier, final Map<Entity, BigDecimal> productComponentWithQuantities) {
        for (Entity operationProductComponent : operationProductComponents) {
            BigDecimal addedQuantity = productComponentWithQuantities.get(operationProductComponent);
            BigDecimal quantity = addedQuantity.multiply(multiplier, numberService.getMathContext());

            productComponentWithQuantities.put(operationProductComponent, quantity);
        }
    }

    private List<Entity> getOrdersFromComponents(final List<Entity> components) {
        List<Entity> orders = Lists.newArrayList();

        for (Entity component : components) {
            Entity order = component.getBelongsToField("order");

            if (order == null) {
                throw new IllegalStateException(
                        "Given component doesn't point to an order using getBelongsToField(\"order\") relation");
            }

            orders.add(order);
        }

        return orders;
    }

    private Map<Entity, BigDecimal> getProductWithQuantities(final Map<Entity, BigDecimal> productComponentWithQuantities,
            final Set<Entity> nonComponents, final MrpAlgorithm mrpAlgorithm, final String operationProductComponentModelName) {
        if (mrpAlgorithm.equals(MrpAlgorithm.ALL_PRODUCTS_IN)) {
            return getProductWithoutSubcontractingProduct(productComponentWithQuantities, nonComponents, false,
                    operationProductComponentModelName);
        } else {
            return getProductWithoutSubcontractingProduct(productComponentWithQuantities, nonComponents, true,
                    operationProductComponentModelName);
        }
    }

    private Map<Entity, BigDecimal> getProductWithoutSubcontractingProduct(
            final Map<Entity, BigDecimal> productComponentWithQuantities, final Set<Entity> nonComponents,
            final boolean onlyComponents, final String operationProductComponentModelName) {
        Map<Entity, BigDecimal> productWithQuantities = Maps.newHashMap();

        for (Entry<Entity, BigDecimal> productComponentWithQuantity : productComponentWithQuantities.entrySet()) {
            if (operationProductComponentModelName.equals(productComponentWithQuantity.getKey().getDataDefinition().getName())) {
                if (onlyComponents && nonComponents.contains(productComponentWithQuantity.getKey())) {
                    continue;
                }

                addProductQuantitiesToList(productComponentWithQuantity, productWithQuantities);
            }
        }

        return productWithQuantities;
    }

    @Override
    public void addProductQuantitiesToList(final Entry<Entity, BigDecimal> productComponentWithQuantity,
            final Map<Entity, BigDecimal> productWithQuantities) {
        Entity product = productComponentWithQuantity.getKey().getBelongsToField(PRODUCT);
        BigDecimal newQuantity = productComponentWithQuantity.getValue();

        BigDecimal oldQuantity = productWithQuantities.get(product);
        if (oldQuantity != null) {
            newQuantity = newQuantity.add(oldQuantity);
        }

        productWithQuantities.put(product, newQuantity);
    }

    private Map<Entity, BigDecimal> getProductComponentWithQuantitiesWithoutNonComponents(
            final Map<Entity, BigDecimal> productComponentWithQuantities, final Set<Entity> nonComponents) {
        for (Entity nonComponent : nonComponents) {
            productComponentWithQuantities.remove(nonComponent);
        }

        return productComponentWithQuantities;
    }

    @Override
    public Entity getOutputProductsFromOperationComponent(final Entity operationComponent) {
        final List<Entity> operationProductOutComponents = operationComponent.getHasManyField(OPERATION_PRODUCT_OUT_COMPONENTS);

        if (operationProductOutComponents.isEmpty()) {
            return null;
        }

        final Entity parentOperation = operationComponent.getBelongsToField(PARENT);

        if (parentOperation == null) {
            return operationProductOutComponents.get(0);
        } else {
            final List<Entity> parentOperationProductInComponents = parentOperation
                    .getHasManyField(OPERATION_PRODUCT_IN_COMPONENTS);

            for (Entity product : operationProductOutComponents) {
                if (findProductParentOperation(product, parentOperationProductInComponents)) {
                    return product;
                }
            }

            return null;
        }
    }

    private boolean findProductParentOperation(final Entity product, final List<Entity> parentProducts) {
        for (Entity parent : parentProducts) {
            Entity parentProduct = parent.getBelongsToField(PRODUCT);
            Entity currentProduct = product.getBelongsToField(PRODUCT);

            if (parentProduct.getId().equals(currentProduct.getId())) {
                return true;
            }
        }

        return false;
    }

}
