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
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.NumberService;

@Service
public class ProductQuantitiesServiceImpl implements ProductQuantitiesService {

    @Autowired
    private NumberService numberService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public Map<Long, BigDecimal> getProductComponentQuantities(final Entity technology, final BigDecimal givenQuantity,
            final Map<Long, BigDecimal> operationRuns) {
        Set<Long> nonComponents = Sets.newHashSet();

        return getProductComponentWithQuantitiesForTechnology(technology, givenQuantity, operationRuns, nonComponents);
    }

    @Override
    public Map<Long, BigDecimal> getProductComponentQuantities(final List<Entity> orders) {
        return getProductComponentQuantities(orders, false);
    }

    private Map<Long, BigDecimal> getProductComponentQuantities(final List<Entity> orders, final boolean onTheFly) {
        Map<Long, BigDecimal> operationRuns = Maps.newHashMap();

        return getProductComponentQuantities(orders, operationRuns, false);
    }

    @Override
    public Map<Long, BigDecimal> getProductComponentQuantities(final List<Entity> orders,
            final Map<Long, BigDecimal> operationRuns) {
        return getProductComponentQuantities(orders, operationRuns, false);
    }

    private Map<Long, BigDecimal> getProductComponentQuantities(final List<Entity> orders,
            final Map<Long, BigDecimal> operationRuns, final boolean onTheFly) {
        Set<Long> nonComponents = Sets.newHashSet();

        return getProductComponentWithQuantitiesForOrders(orders, operationRuns, nonComponents, onTheFly);
    }

    @Override
    public Map<Long, BigDecimal> getProductComponentQuantitiesWithoutNonComponents(final List<Entity> orders) {
        return getProductComponentQuantitiesWithoutNonComponents(orders, false);
    }

    private Map<Long, BigDecimal> getProductComponentQuantitiesWithoutNonComponents(final List<Entity> orders,
            final boolean onTheFly) {
        Map<Long, BigDecimal> operationRuns = Maps.newHashMap();
        Set<Long> nonComponents = Sets.newHashSet();

        Map<Long, BigDecimal> productComponentWithQuantities = getProductComponentWithQuantitiesForOrders(orders, operationRuns,
                nonComponents, onTheFly);

        return getProductComponentWithQuantitiesWithoutNonComponents(productComponentWithQuantities, nonComponents);
    }

    @Override
    public Map<Long, BigDecimal> getNeededProductQuantities(final Entity technology, final BigDecimal givenQuantity,
            final MrpAlgorithm mrpAlgorithm) {
        Map<Long, BigDecimal> operationRuns = Maps.newHashMap();
        Set<Long> nonComponents = Sets.newHashSet();

        Map<Long, BigDecimal> productComponentWithQuantities = getProductComponentWithQuantitiesForTechnology(technology,
                givenQuantity, operationRuns, nonComponents);

        return getProductWithQuantities(productComponentWithQuantities, nonComponents, mrpAlgorithm,
                MODEL_OPERATION_PRODUCT_IN_COMPONENT);
    }

    @Override
    public Map<Long, BigDecimal> getNeededProductQuantities(final List<Entity> orders, final MrpAlgorithm mrpAlgorithm) {
        return getNeededProductQuantities(orders, mrpAlgorithm, false);
    }

    @Override
    public Map<Long, BigDecimal> getNeededProductQuantities(final List<Entity> orders, final MrpAlgorithm mrpAlgorithm,
            final boolean onTheFly) {
        Map<Long, BigDecimal> operationRuns = Maps.newHashMap();

        return getNeededProductQuantities(orders, mrpAlgorithm, operationRuns, onTheFly);
    }

    @Override
    public Map<Long, BigDecimal> getNeededProductQuantities(final List<Entity> orders, final MrpAlgorithm mrpAlgorithm,
            final Map<Long, BigDecimal> operationRuns) {
        return getNeededProductQuantities(orders, mrpAlgorithm, operationRuns, false);
    }

    private Map<Long, BigDecimal> getNeededProductQuantities(final List<Entity> orders, final MrpAlgorithm mrpAlgorithm,
            final Map<Long, BigDecimal> operationRuns, final boolean onTheFly) {
        Set<Long> nonComponents = Sets.newHashSet();

        Map<Long, BigDecimal> productComponentWithQuantities = getProductComponentWithQuantitiesForOrders(orders, operationRuns,
                nonComponents, onTheFly);

        return getProductWithQuantities(productComponentWithQuantities, nonComponents, mrpAlgorithm,
                MODEL_OPERATION_PRODUCT_IN_COMPONENT);
    }

    @Override
    public Map<Long, BigDecimal> getNeededProductQuantitiesForComponents(final List<Entity> components,
            final MrpAlgorithm mrpAlgorithm) {
        return getNeededProductQuantitiesForComponents(components, mrpAlgorithm, false);
    }

    private Map<Long, BigDecimal> getNeededProductQuantitiesForComponents(final List<Entity> components,
            final MrpAlgorithm mrpAlgorithm, final boolean onTheFly) {
        Map<Long, BigDecimal> operationRuns = Maps.newHashMap();
        Set<Long> nonComponents = Sets.newHashSet();

        Map<Long, BigDecimal> productComponentWithQuantities = getProductComponentWithQuantitiesForOrders(
                getOrdersFromComponents(components), operationRuns, nonComponents, onTheFly);

        return getProductWithQuantities(productComponentWithQuantities, nonComponents, mrpAlgorithm,
                MODEL_OPERATION_PRODUCT_IN_COMPONENT);
    }

    @Override
    public Map<Long, BigDecimal> getProductComponentWithQuantitiesForTechnology(final Entity technology,
            final BigDecimal givenQuantity, final Map<Long, BigDecimal> operationRuns, final Set<Long> nonComponents) {
        Map<Long, BigDecimal> productComponentWithQuantities = Maps.newHashMap();

        EntityTree operationComponents = technology.getTreeField(OPERATION_COMPONENTS);
        Entity root = operationComponents.getRoot();

        preloadProductQuantitiesAndOperationRuns(operationComponents, productComponentWithQuantities, operationRuns);
        traverseProductQuantitiesAndOperationRuns(technology, givenQuantity, root, null, productComponentWithQuantities,
                nonComponents, operationRuns);

        return productComponentWithQuantities;
    }

    private Map<Long, BigDecimal> getProductComponentWithQuantitiesForOrders(final List<Entity> orders,
            final Map<Long, BigDecimal> operationRuns, final Set<Long> nonComponents, final boolean onTheFly) {
        Map<Long, Map<Long, BigDecimal>> productComponentWithQuantitiesForOrders = Maps.newHashMap();

        for (Entity order : orders) {
            BigDecimal plannedQuantity = (BigDecimal) order.getField("plannedQuantity");

            Entity technology = order.getBelongsToField("technology");

            if (technology == null) {
                throw new IllegalStateException("Order doesn't contain technology.");
            }

            productComponentWithQuantitiesForOrders.put(order.getId(),
                    getProductComponentWithQuantitiesForTechnology(technology, plannedQuantity, operationRuns, nonComponents));
        }

        return groupProductComponentWithQuantities(productComponentWithQuantitiesForOrders);
    }

    @Override
    public Map<Long, BigDecimal> getProductComponentWithQuantities(final List<Entity> orders,
            final Map<Long, BigDecimal> operationRuns, final Set<Long> nonComponents) {
        Map<Long, Map<Long, BigDecimal>> productComponentWithQuantitiesForOrders = Maps.newHashMap();

        for (Entity order : orders) {
            BigDecimal plannedQuantity = (BigDecimal) order.getField("plannedQuantity");

            Entity technology = order.getBelongsToField("technology");

            if (technology == null) {
                throw new IllegalStateException("Order doesn't contain technology.");
            }

            productComponentWithQuantitiesForOrders.put(order.getId(),
                    getProductComponentWithQuantitiesForTechnology(technology, plannedQuantity, operationRuns, nonComponents));
        }

        return groupProductComponentWithQuantities(productComponentWithQuantitiesForOrders);
    }

    @Override
    public Map<Long, BigDecimal> groupProductComponentWithQuantities(
            final Map<Long, Map<Long, BigDecimal>> productComponentWithQuantitiesForOrders) {
        Map<Long, BigDecimal> productComponentWithQuantities = Maps.newHashMap();

        for (Entry<Long, Map<Long, BigDecimal>> productComponentWithQuantitiesForOrder : productComponentWithQuantitiesForOrders
                .entrySet()) {

            for (Entry<Long, BigDecimal> productComponentWithQuantity : productComponentWithQuantitiesForOrder.getValue()
                    .entrySet()) {
                Entity operationProductComponent = getOperationProductComponent(productComponentWithQuantity.getKey());

                if (operationProductComponent != null) {
                    BigDecimal quantity = productComponentWithQuantity.getValue();

                    if (productComponentWithQuantities.containsKey(operationProductComponent.getId())) {
                        BigDecimal addedQuantity = productComponentWithQuantities.get(operationProductComponent.getId());

                        quantity = quantity.add(addedQuantity, numberService.getMathContext());

                        productComponentWithQuantities.put(operationProductComponent.getId(), quantity);
                    } else {
                        productComponentWithQuantities.put(operationProductComponent.getId(), quantity);
                    }
                }
            }
        }

        return productComponentWithQuantities;
    }

    private void preloadProductQuantitiesAndOperationRuns(final EntityTree operationComponents,
            final Map<Long, BigDecimal> productComponentWithQuantities, final Map<Long, BigDecimal> operationRuns) {
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

            operationRuns.put(operationComponent.getId(), BigDecimal.ONE);
        }
    }

    private void preloadOperationProductComponentQuantity(final List<Entity> operationProductComponents,
            final Map<Long, BigDecimal> productComponentWithQuantities) {
        for (Entity operationProductComponent : operationProductComponents) {
            BigDecimal neededQuantity = operationProductComponent.getDecimalField(QUANTITY);

            productComponentWithQuantities.put(operationProductComponent.getId(), neededQuantity);
        }
    }

    private void traverseProductQuantitiesAndOperationRuns(final Entity technology, final BigDecimal givenQuantity,
            final Entity operationComponent, final Entity previousOperationComponent,
            final Map<Long, BigDecimal> productComponentWithQuantities, final Set<Long> nonComponents,
            final Map<Long, BigDecimal> operationRuns) {
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
                    BigDecimal outQuantity = productComponentWithQuantities.get(operationProductOutComponent.getId());

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

                        BigDecimal outQuantity = productComponentWithQuantities.get(operationProductOutComponent.getId());
                        BigDecimal inQuantity = productComponentWithQuantities.get(operationProductInComponent.getId());

                        multiplyProductQuantitiesAndAddOperationRuns(operationComponent, inQuantity, outQuantity,
                                productComponentWithQuantities, operationRuns);

                        break;
                    }
                }

                if (isntComponent) {
                    nonComponents.add(operationProductInComponent.getId());
                }
            }
        }

        for (Entity child : operationComponent.getHasManyField(CHILDREN)) {
            traverseProductQuantitiesAndOperationRuns(technology, givenQuantity, child, operationComponent,
                    productComponentWithQuantities, nonComponents, operationRuns);
        }
    }

    private void multiplyProductQuantitiesAndAddOperationRuns(final Entity operationComponent, final BigDecimal needed,
            final BigDecimal actual, final Map<Long, BigDecimal> productComponentWithQuantities,
            final Map<Long, BigDecimal> operationRuns) {
        BigDecimal multiplier = needed.divide(actual, numberService.getMathContext());

        if (!operationComponent.getBooleanField(ARE_PRODUCT_QUANTITIES_DIVISIBLE)) {
            // It's intentional to round up the operation runs
            multiplier = multiplier.setScale(0, RoundingMode.CEILING);
        }

        BigDecimal runs = multiplier;

        if (!operationComponent.getBooleanField(IS_TJ_DIVISIBLE)) {
            runs = multiplier.setScale(0, RoundingMode.CEILING);
        }

        operationRuns.put(operationComponent.getId(), runs);

        multiplyOperationProductComponentQuantities(operationComponent.getHasManyField(OPERATION_PRODUCT_IN_COMPONENTS),
                multiplier, productComponentWithQuantities);
        multiplyOperationProductComponentQuantities(operationComponent.getHasManyField(OPERATION_PRODUCT_OUT_COMPONENTS),
                multiplier, productComponentWithQuantities);
    }

    private void multiplyOperationProductComponentQuantities(final List<Entity> operationProductComponents,
            final BigDecimal multiplier, final Map<Long, BigDecimal> productComponentWithQuantities) {
        for (Entity operationProductComponent : operationProductComponents) {
            BigDecimal addedQuantity = productComponentWithQuantities.get(operationProductComponent.getId());
            BigDecimal quantity = addedQuantity.multiply(multiplier, numberService.getMathContext());

            productComponentWithQuantities.put(operationProductComponent.getId(), quantity);
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

    private Map<Long, BigDecimal> getProductWithQuantities(final Map<Long, BigDecimal> productComponentWithQuantities,
            final Set<Long> nonComponents, final MrpAlgorithm mrpAlgorithm, final String operationProductComponentModelName) {
        if (mrpAlgorithm.equals(MrpAlgorithm.ALL_PRODUCTS_IN)) {
            return getProductWithoutSubcontractingProduct(productComponentWithQuantities, nonComponents, false,
                    operationProductComponentModelName);
        } else {
            return getProductWithoutSubcontractingProduct(productComponentWithQuantities, nonComponents, true,
                    operationProductComponentModelName);
        }
    }

    private Map<Long, BigDecimal> getProductWithoutSubcontractingProduct(
            final Map<Long, BigDecimal> productComponentWithQuantities, final Set<Long> nonComponents,
            final boolean onlyComponents, final String operationProductComponentModelName) {
        Map<Long, BigDecimal> productWithQuantities = Maps.newHashMap();

        for (Entry<Long, BigDecimal> productComponentWithQuantity : productComponentWithQuantities.entrySet()) {
            Entity operationProductComponent = getOperationProductComponent(productComponentWithQuantity.getKey());

            if (operationProductComponent != null) {
                if (operationProductComponentModelName.equals(operationProductComponent.getDataDefinition().getName())) {
                    if (onlyComponents && nonComponents.contains(productComponentWithQuantity.getKey())) {
                        continue;
                    }

                    addProductQuantitiesToList(productComponentWithQuantity, productWithQuantities);
                }
            }
        }

        return productWithQuantities;
    }

    @Override
    public void addProductQuantitiesToList(final Entry<Long, BigDecimal> productComponentWithQuantity,
            final Map<Long, BigDecimal> productWithQuantities) {
        Entity operationProductComponent = getOperationProductComponent(productComponentWithQuantity.getKey());

        if (operationProductComponent != null) {
            Entity product = operationProductComponent.getBelongsToField(PRODUCT);
            BigDecimal newQuantity = productComponentWithQuantity.getValue();

            BigDecimal oldQuantity = productWithQuantities.get(product.getId());
            if (oldQuantity != null) {
                newQuantity = newQuantity.add(oldQuantity);
            }

            productWithQuantities.put(product.getId(), newQuantity);
        }
    }

    private Map<Long, BigDecimal> getProductComponentWithQuantitiesWithoutNonComponents(
            final Map<Long, BigDecimal> productComponentWithQuantities, final Set<Long> nonComponents) {
        for (Long nonComponent : nonComponents) {
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

    @Override
    public Entity getOperationProductComponent(final Long operationProductComponentId) {
        Entity operationProductInComponent = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT).get(operationProductComponentId);

        Entity operationProductOutComponent = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT).get(operationProductComponentId);

        if (operationProductInComponent != null) {
            return operationProductInComponent;
        } else if (operationProductOutComponent != null) {
            return operationProductOutComponent;
        } else {
            return null;
        }
    }

    @Override
    public Entity getTechnologyOperationComponent(final Long technologyOperationComponentId) {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(technologyOperationComponentId);
    }

    @Override
    public Entity getProduct(final Long productId) {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(productId);
    }

    @Override
    public Map<Entity, BigDecimal> convertOperationsRunsFromProductQuantities(
            final Map<Long, BigDecimal> operationRunsFromProductionQuantities) {
        final Map<Entity, BigDecimal> operationRuns = Maps.newHashMap();

        for (Entry<Long, BigDecimal> operationRunsFromProductionQuantity : operationRunsFromProductionQuantities.entrySet()) {
            Entity technologyOperatonComponent = getTechnologyOperationComponent(operationRunsFromProductionQuantity.getKey());

            operationRuns.put(technologyOperatonComponent, operationRunsFromProductionQuantity.getValue());
        }

        return operationRuns;
    }

}
