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
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentEntityType;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.mes.technologies.dto.ProductQuantitiesHolder;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.NumberService;

@Service
public class ProductQuantitiesServiceImpl implements ProductQuantitiesService {

    private static final String L_ORDER = "order";

    private static final String L_TECHNOLOGY = "technology";

    private static final String L_PLANNED_QUANTITY = "plannedQuantity";

    private static final String L_PRODUCT = "product";

    private static final String L_QUANTITY = "quantity";

    @Autowired
    private NumberService numberService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public ProductQuantitiesHolder getProductComponentQuantities(final Entity technology, final BigDecimal givenQuantity) {
        Map<Long, BigDecimal> operationRuns = Maps.newHashMap();
        OperationProductComponentWithQuantityContainer productQuantities = getProductComponentQuantities(technology,
                givenQuantity, operationRuns);
        // return new ProductQuantitiesContainer(operationRuns, productQuantities);

        return null;
    }

    @Override
    public OperationProductComponentWithQuantityContainer getProductComponentQuantities(final Entity technology,
            final BigDecimal givenQuantity, final Map<Long, BigDecimal> operationRuns) {
        Set<OperationProductComponentHolder> nonComponents = Sets.newHashSet();

        return getProductComponentWithQuantitiesForTechnology(technology, givenQuantity, operationRuns, nonComponents);
    }

    @Override
    public OperationProductComponentWithQuantityContainer getProductComponentQuantities(final List<Entity> orders) {
        return getProductComponentQuantities(orders, false);
    }

    private OperationProductComponentWithQuantityContainer getProductComponentQuantities(final List<Entity> orders,
            final boolean onTheFly) {
        Map<Long, BigDecimal> operationRuns = Maps.newHashMap();

        return getProductComponentQuantities(orders, operationRuns, onTheFly);
    }

    @Override
    public OperationProductComponentWithQuantityContainer getProductComponentQuantities(final List<Entity> orders,
            final Map<Long, BigDecimal> operationRuns) {
        return getProductComponentQuantities(orders, operationRuns, false);
    }

    private OperationProductComponentWithQuantityContainer getProductComponentQuantities(final List<Entity> orders,
            final Map<Long, BigDecimal> operationRuns, final boolean onTheFly) {
        Set<OperationProductComponentHolder> nonComponents = Sets.newHashSet();

        return getProductComponentWithQuantitiesForOrders(orders, operationRuns, nonComponents, onTheFly);
    }

    @Override
    public OperationProductComponentWithQuantityContainer getProductComponentQuantitiesWithoutNonComponents(
            final List<Entity> orders) {
        return getProductComponentQuantitiesWithoutNonComponents(orders, false);
    }

    private OperationProductComponentWithQuantityContainer getProductComponentQuantitiesWithoutNonComponents(
            final List<Entity> orders, final boolean onTheFly) {
        Map<Long, BigDecimal> operationRuns = Maps.newHashMap();
        Set<OperationProductComponentHolder> nonComponents = Sets.newHashSet();

        OperationProductComponentWithQuantityContainer productComponentWithQuantities = getProductComponentWithQuantitiesForOrders(
                orders, operationRuns, nonComponents, onTheFly);

        return getProductComponentWithQuantitiesWithoutNonComponents(productComponentWithQuantities, nonComponents);
    }

    private OperationProductComponentWithQuantityContainer getProductComponentWithQuantitiesWithoutNonComponents(
            final OperationProductComponentWithQuantityContainer productComponentWithQuantities,
            final Set<OperationProductComponentHolder> nonComponents) {
        for (OperationProductComponentHolder nonComponent : nonComponents) {
            productComponentWithQuantities.remove(nonComponent);
        }

        return productComponentWithQuantities;
    }

    @Override
    public Map<Long, BigDecimal> getNeededProductQuantities(final Entity technology, final BigDecimal givenQuantity,
            final MrpAlgorithm mrpAlgorithm) {
        Map<Long, BigDecimal> operationRuns = Maps.newHashMap();
        Set<OperationProductComponentHolder> nonComponents = Sets.newHashSet();

        OperationProductComponentWithQuantityContainer productComponentWithQuantities = getProductComponentWithQuantitiesForTechnology(
                technology, givenQuantity, operationRuns, nonComponents);

        return getProductWithQuantities(productComponentWithQuantities, nonComponents, mrpAlgorithm,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);
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
        Set<OperationProductComponentHolder> nonComponents = Sets.newHashSet();

        OperationProductComponentWithQuantityContainer productComponentWithQuantities = getProductComponentWithQuantitiesForOrders(
                orders, operationRuns, nonComponents, onTheFly);

        return getProductWithQuantities(productComponentWithQuantities, nonComponents, mrpAlgorithm,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);
    }

    @Override
    public Map<Long, BigDecimal> getNeededProductQuantitiesForComponents(final List<Entity> components,
            final MrpAlgorithm mrpAlgorithm) {
        return getNeededProductQuantitiesForComponents(components, mrpAlgorithm, false);
    }

    private Map<Long, BigDecimal> getNeededProductQuantitiesForComponents(final List<Entity> components,
            final MrpAlgorithm mrpAlgorithm, final boolean onTheFly) {
        Map<Long, BigDecimal> operationRuns = Maps.newHashMap();
        Set<OperationProductComponentHolder> nonComponents = Sets.newHashSet();

        OperationProductComponentWithQuantityContainer productComponentWithQuantities = getProductComponentWithQuantitiesForOrders(
                getOrdersFromComponents(components), operationRuns, nonComponents, onTheFly);

        return getProductWithQuantities(productComponentWithQuantities, nonComponents, mrpAlgorithm,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);
    }

    @Override
    public OperationProductComponentWithQuantityContainer getProductComponentWithQuantitiesForTechnology(final Entity technology,
            final BigDecimal givenQuantity, final Map<Long, BigDecimal> operationRuns,
            final Set<OperationProductComponentHolder> nonComponents) {
        OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer = new OperationProductComponentWithQuantityContainer();

        EntityTree operationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
        Entity root = operationComponents.getRoot();

        preloadProductQuantitiesAndOperationRuns(operationComponents, operationProductComponentWithQuantityContainer,
                operationRuns);
        traverseProductQuantitiesAndOperationRuns(technology, givenQuantity, root, null,
                operationProductComponentWithQuantityContainer, nonComponents, operationRuns);

        return operationProductComponentWithQuantityContainer;
    }

    private OperationProductComponentWithQuantityContainer getProductComponentWithQuantitiesForOrders(final List<Entity> orders,
            final Map<Long, BigDecimal> operationRuns, final Set<OperationProductComponentHolder> nonComponents,
            final boolean onTheFly) {
        Map<Long, OperationProductComponentWithQuantityContainer> productComponentWithQuantitiesForOrders = Maps.newHashMap();

        for (Entity order : orders) {
            BigDecimal plannedQuantity = order.getDecimalField(L_PLANNED_QUANTITY);

            Entity technology = order.getBelongsToField(L_TECHNOLOGY);

            if (technology == null) {
                throw new IllegalStateException("Order doesn't contain technology.");
            }

            productComponentWithQuantitiesForOrders.put(order.getId(),
                    getProductComponentWithQuantitiesForTechnology(technology, plannedQuantity, operationRuns, nonComponents));
        }

        return groupOperationProductComponentWithQuantities(productComponentWithQuantitiesForOrders);
    }

    @Override
    public OperationProductComponentWithQuantityContainer groupOperationProductComponentWithQuantities(
            final Map<Long, OperationProductComponentWithQuantityContainer> operationProductComponentWithQuantityContainerForOrders) {
        OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer = new OperationProductComponentWithQuantityContainer();

        for (Entry<Long, OperationProductComponentWithQuantityContainer> operationProductComponentWithQuantityContainerForOrder : operationProductComponentWithQuantityContainerForOrders
                .entrySet()) {

            for (Entry<OperationProductComponentHolder, BigDecimal> productComponentWithQuantity : operationProductComponentWithQuantityContainerForOrder
                    .getValue().asMap().entrySet()) {
                OperationProductComponentHolder operationProductComponentHolder = productComponentWithQuantity.getKey();
                BigDecimal quantity = productComponentWithQuantity.getValue();

                if (operationProductComponentWithQuantityContainer.containsKey(operationProductComponentHolder)) {
                    BigDecimal addedQuantity = operationProductComponentWithQuantityContainer
                            .get(operationProductComponentHolder);

                    quantity = quantity.add(addedQuantity, numberService.getMathContext());

                    operationProductComponentWithQuantityContainer.put(operationProductComponentHolder, quantity);
                } else {
                    operationProductComponentWithQuantityContainer.put(operationProductComponentHolder, quantity);
                }
            }
        }

        return operationProductComponentWithQuantityContainer;
    }

    private void preloadProductQuantitiesAndOperationRuns(final EntityTree operationComponents,
            final OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer,
            final Map<Long, BigDecimal> operationRuns) {
        for (Entity operationComponent : operationComponents) {
            if (isTechnologyOperationComponentEntityTypeReferenceTechnology(operationComponent)) {
                Entity referenceTechnology = operationComponent
                        .getBelongsToField(TechnologyOperationComponentFields.REFERENCETECHNOLOGY);
                EntityTree referenceOperationComponents = referenceTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

                preloadProductQuantitiesAndOperationRuns(referenceOperationComponents,
                        operationProductComponentWithQuantityContainer, operationRuns);

                continue;
            }

            preloadOperationProductComponentQuantity(
                    operationComponent.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS),
                    operationProductComponentWithQuantityContainer);
            preloadOperationProductComponentQuantity(
                    operationComponent.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS),
                    operationProductComponentWithQuantityContainer);

            operationRuns.put(operationComponent.getId(), BigDecimal.ONE);
        }
    }

    private void preloadOperationProductComponentQuantity(final List<Entity> operationProductComponents,
            final OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer) {
        for (Entity operationProductComponent : operationProductComponents) {
            BigDecimal neededQuantity = operationProductComponent.getDecimalField(L_QUANTITY);

            operationProductComponentWithQuantityContainer.put(operationProductComponent, neededQuantity);
        }
    }

    private void traverseProductQuantitiesAndOperationRuns(final Entity technology, final BigDecimal givenQuantity,
            final Entity operationComponent, final Entity previousOperationComponent,
            final OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer,
            final Set<OperationProductComponentHolder> nonComponents, final Map<Long, BigDecimal> operationRuns) {
        if (isTechnologyOperationComponentEntityTypeReferenceTechnology(operationComponent)) {
            Entity referenceTechnology = operationComponent
                    .getBelongsToField(TechnologyOperationComponentFields.REFERENCETECHNOLOGY);
            EntityTree referenceOperationComponent = referenceTechnology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

            traverseProductQuantitiesAndOperationRuns(referenceTechnology, givenQuantity, referenceOperationComponent.getRoot(),
                    previousOperationComponent, operationProductComponentWithQuantityContainer, nonComponents, operationRuns);

            return;
        }

        if (previousOperationComponent == null) {
            Entity technologyProduct = technology.getBelongsToField(TechnologyFields.PRODUCT);

            for (Entity operationProductOutComponent : operationComponent
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS)) {
                if (operationProductOutComponent.getBelongsToField(OperationProductOutComponentFields.PRODUCT).getId()
                        .equals(technologyProduct.getId())) {
                    BigDecimal outQuantity = operationProductComponentWithQuantityContainer.get(operationProductOutComponent);

                    multiplyProductQuantitiesAndAddOperationRuns(operationComponent, givenQuantity, outQuantity,
                            operationProductComponentWithQuantityContainer, operationRuns);

                    break;
                }
            }
        } else {
            for (Entity operationProductInComponent : previousOperationComponent
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS)) {
                boolean isntComponent = false;

                for (Entity operationProductOutComponent : operationComponent
                        .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS)) {
                    if (operationProductOutComponent
                            .getBelongsToField(OperationProductOutComponentFields.PRODUCT)
                            .getId()
                            .equals(operationProductInComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT)
                                    .getId())) {
                        isntComponent = true;

                        BigDecimal outQuantity = operationProductComponentWithQuantityContainer.get(operationProductOutComponent);
                        BigDecimal inQuantity = operationProductComponentWithQuantityContainer.get(operationProductInComponent);

                        multiplyProductQuantitiesAndAddOperationRuns(operationComponent, inQuantity, outQuantity,
                                operationProductComponentWithQuantityContainer, operationRuns);

                        break;
                    }
                }

                if (isntComponent) {
                    nonComponents.add(new OperationProductComponentHolder(operationProductInComponent));
                }
            }
        }

        for (Entity child : operationComponent.getHasManyField(TechnologyOperationComponentFields.CHILDREN)) {
            traverseProductQuantitiesAndOperationRuns(technology, givenQuantity, child, operationComponent,
                    operationProductComponentWithQuantityContainer, nonComponents, operationRuns);
        }
    }

    private void multiplyProductQuantitiesAndAddOperationRuns(final Entity operationComponent, final BigDecimal needed,
            final BigDecimal actual,
            final OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer,
            final Map<Long, BigDecimal> operationRuns) {
        BigDecimal multiplier = needed.divide(actual, numberService.getMathContext());

        if (!operationComponent.getBooleanField(TechnologyOperationComponentFields.ARE_PRODUCT_QUANTITIES_DIVISIBLE)) {
            // It's intentional to round up the operation runs
            multiplier = multiplier.setScale(0, RoundingMode.CEILING);
        }

        BigDecimal runs = multiplier;

        if (!operationComponent.getBooleanField(TechnologyOperationComponentFields.IS_TJ_DIVISIBLE)) {
            runs = multiplier.setScale(0, RoundingMode.CEILING);
        }

        operationRuns.put(operationComponent.getId(), runs);

        multiplyOperationProductComponentQuantities(
                operationComponent.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS),
                multiplier, operationProductComponentWithQuantityContainer);
        multiplyOperationProductComponentQuantities(
                operationComponent.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS),
                multiplier, operationProductComponentWithQuantityContainer);
    }

    private void multiplyOperationProductComponentQuantities(final List<Entity> operationProductComponents,
            final BigDecimal multiplier,
            final OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer) {
        for (Entity operationProductComponent : operationProductComponents) {
            BigDecimal addedQuantity = operationProductComponentWithQuantityContainer.get(operationProductComponent);
            BigDecimal quantity = addedQuantity.multiply(multiplier, numberService.getMathContext());

            operationProductComponentWithQuantityContainer.put(operationProductComponent, quantity);
        }
    }

    private boolean isTechnologyOperationComponentEntityTypeReferenceTechnology(Entity operationComponent) {
        return TechnologyOperationComponentEntityType.REFERENCE_TECHNOLOGY.getStringValue().equals(
                operationComponent.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE));
    }

    @Override
    public OperationProductComponentWithQuantityContainer getProductComponentWithQuantities(final List<Entity> orders,
            final Map<Long, BigDecimal> operationRuns, final Set<OperationProductComponentHolder> nonComponents) {
        return getProductComponentWithQuantitiesForOrders(orders, operationRuns, nonComponents, false);
    }

    private List<Entity> getOrdersFromComponents(final List<Entity> components) {
        List<Entity> orders = Lists.newArrayList();

        for (Entity component : components) {
            Entity order = component.getBelongsToField(L_ORDER);

            if (order == null) {
                throw new IllegalStateException(
                        "Given component doesn't point to an order using getBelongsToField(\"order\") relation");
            }

            orders.add(order);
        }

        return orders;
    }

    private Map<Long, BigDecimal> getProductWithQuantities(
            final OperationProductComponentWithQuantityContainer productComponentWithQuantities,
            final Set<OperationProductComponentHolder> nonComponents, final MrpAlgorithm mrpAlgorithm,
            final String operationProductComponentModelName) {
        OperationProductComponentWithQuantityContainer allWithSameEntityType = productComponentWithQuantities
                .getAllWithSameEntityType(operationProductComponentModelName);

        if (mrpAlgorithm.equals(MrpAlgorithm.ALL_PRODUCTS_IN)) {
            return getProductWithoutSubcontractingProduct(allWithSameEntityType, nonComponents, false);
        } else {
            return getProductWithoutSubcontractingProduct(allWithSameEntityType, nonComponents, true);
        }
    }

    private Map<Long, BigDecimal> getProductWithoutSubcontractingProduct(
            final OperationProductComponentWithQuantityContainer productComponentWithQuantities,
            final Set<OperationProductComponentHolder> nonComponents, final boolean onlyComponents) {
        Map<Long, BigDecimal> productWithQuantities = Maps.newHashMap();

        for (Entry<OperationProductComponentHolder, BigDecimal> productComponentWithQuantity : productComponentWithQuantities
                .asMap().entrySet()) {
            OperationProductComponentHolder operationProductComponentHolder = productComponentWithQuantity.getKey();

            if (onlyComponents && nonComponents.contains(operationProductComponentHolder)) {
                continue;
            }

            addProductQuantitiesToList(productComponentWithQuantity, productWithQuantities);
        }

        return productWithQuantities;
    }

    @Override
    public void addProductQuantitiesToList(final Entry<OperationProductComponentHolder, BigDecimal> productComponentWithQuantity,
            final Map<Long, BigDecimal> productWithQuantities) {
        Entity operationProductComponent = productComponentWithQuantity.getKey().getEntity();

        if (operationProductComponent != null) {
            Entity product = operationProductComponent.getBelongsToField(L_PRODUCT);
            BigDecimal newQuantity = productComponentWithQuantity.getValue();

            BigDecimal oldQuantity = productWithQuantities.get(product.getId());
            if (oldQuantity != null) {
                newQuantity = newQuantity.add(oldQuantity);
            }

            productWithQuantities.put(product.getId(), newQuantity);
        }
    }

    @Override
    public Entity getOutputProductsFromOperationComponent(final Entity operationComponent) {
        final List<Entity> operationProductOutComponents = operationComponent
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);

        if (operationProductOutComponents.isEmpty()) {
            return null;
        }

        final Entity parentOperation = operationComponent.getBelongsToField(TechnologyOperationComponentFields.PARENT);

        if (parentOperation == null) {
            return operationProductOutComponents.get(0);
        } else {
            final List<Entity> parentOperationProductInComponents = parentOperation
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);

            for (Entity operationProductOutComponent : operationProductOutComponents) {
                if (findProductParentOperation(operationProductOutComponent, parentOperationProductInComponents)) {
                    return operationProductOutComponent;
                }
            }

            return null;
        }
    }

    private boolean findProductParentOperation(final Entity operationProductOutComponent,
            final List<Entity> parentOperationProductInComponents) {
        for (Entity parentOperationProductInComponent : parentOperationProductInComponents) {
            Entity parentProduct = parentOperationProductInComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);
            Entity currentProduct = operationProductOutComponent.getBelongsToField(OperationProductOutComponentFields.PRODUCT);

            if (parentProduct.getId().equals(currentProduct.getId())) {
                return true;
            }
        }

        return false;
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
