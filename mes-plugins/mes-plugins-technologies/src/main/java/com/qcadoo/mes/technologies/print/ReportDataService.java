/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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
package com.qcadoo.mes.technologies.print;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.report.api.Pair;

@Service
public class ReportDataService {

    private static final String COMPONENT_QUANTITY_ALGORITHM = "02perTechnology";

    private static final String OPERATION_NODE_ENTITY_TYPE = "operation";

    @Autowired
    private TechnologyService technologyService;

    public final void countQuantityForProductsIn(final Map<Entity, BigDecimal> products, final Entity technology,
            final BigDecimal plannedQuantity, final Boolean onlyComponents) {
        EntityTree operationComponents = technology.getTreeField("operationComponents");
        if (COMPONENT_QUANTITY_ALGORITHM.equals(technology.getField("componentQuantityAlgorithm"))) {
            countQuntityComponentPerTechnology(products, operationComponents, onlyComponents, plannedQuantity, technology);
        } else {
            Map<Entity, BigDecimal> orderProducts = new HashMap<Entity, BigDecimal>();
            EntityTreeNode rootNode = operationComponents.getRoot();
            if (rootNode != null) {
                boolean success = countQuntityComponentPerOutProducts(orderProducts, rootNode, onlyComponents, plannedQuantity,
                        technology);
                if (success) {
                    for (Entry<Entity, BigDecimal> entry : orderProducts.entrySet()) {
                        if (!onlyComponents
                                || technologyService.getProductType(entry.getKey(), technology).equals(
                                        TechnologyService.COMPONENT)) {
                            if (products.containsKey(entry.getKey())) {
                                products.put(entry.getKey(), products.get(entry.getKey()).add(entry.getValue()));
                            } else {
                                products.put(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                }
            }
        }
    }

    public final Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> prepareOperationSeries(
            final Entity entity, final String type) {
        Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations = new HashMap<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>>();
        List<Entity> orders = entity.getHasManyField("orders");
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            Entity technology = (Entity) order.getField("technology");
            if (technology != null) {
                EntityTree operationComponents = technology.getTreeField("operationComponents");
                if (COMPONENT_QUANTITY_ALGORITHM.equals(technology.getField("componentQuantityAlgorithm"))) {
                    aggregateTreeDataPerTechnology(operationComponents, operations, type, order,
                            (BigDecimal) order.getField("plannedQuantity"), technology);
                } else {
                    Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> orderOperations = new HashMap<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>>();
                    EntityTreeNode rootNode = operationComponents.getRoot();
                    if (rootNode != null) {
                        boolean success = aggregateTreeDataPerOutProducts(rootNode, orderOperations, type, order,
                                (BigDecimal) order.getField("plannedQuantity"), technology);
                        if (success) {
                            concatenateOperationsList(operations, orderOperations);
                        }
                    }
                }
            }
        }
        return operations;
    }

    private void concatenateOperationsList(
            final Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations,
            final Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> orderOperations) {
        if (operations.size() == 0) {
            operations.putAll(orderOperations);
        } else {
            for (Entry<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> entry : orderOperations
                    .entrySet()) {
                if (operations.containsKey(entry.getKey())) {
                    Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> products = operations
                            .get(entry.getKey());
                    products.putAll(entry.getValue());
                    operations.put(entry.getKey(), products);
                } else {
                    operations.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private void aggregateTreeDataPerTechnology(final List<Entity> operationComponents,
            final Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations,
            final String type, final Entity order, final BigDecimal plannedQuantity, Entity technology) {
        Entity entityKey = null;
        if ("product".equals(type)) {
            Entity product = (Entity) order.getField("product");
            entityKey = product;
        }

        for (Entity operationComponent : operationComponents) {
            if (OPERATION_NODE_ENTITY_TYPE.equals(operationComponent.getField("entityType"))) {
                Entity operation = (Entity) operationComponent.getField("operation");

                if ("machine".equals(type)) {
                    Object machine = operation.getField("machine");
                    if (machine != null) {
                        entityKey = (Entity) machine;
                    } else {
                        entityKey = null;
                    }
                } else if ("worker".equals(type)) {
                    Object worker = operation.getField("staff");
                    if (worker != null) {
                        entityKey = (Entity) worker;
                    } else {
                        entityKey = null;
                    }
                }
                Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> operationMap = null;
                if (operations.containsKey(entityKey)) {
                    operationMap = operations.get(entityKey);
                } else {
                    operationMap = new HashMap<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>();
                }
                Pair<Entity, Entity> pair = Pair.of(operationComponent, order);
                Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>> mapPair = Pair.of(
                        prepareInProductsPerTechnology(operationComponent, plannedQuantity),
                        prepareOutProductsPerTechnology(operationComponent, plannedQuantity, technology));
                operationMap.put(pair, mapPair);
                operations.put(entityKey, operationMap);
            } else {
                aggregateTreeDataPerTechnology(
                        operationComponent.getBelongsToField("referenceTechnology").getTreeField("operationComponents"),
                        operations, type, order, plannedQuantity, technology);
            }
        }
    }

    private Map<Entity, BigDecimal> prepareOutProductsPerTechnology(final Entity operationComponent,
            final BigDecimal plannedQuantity, Entity technology) {
        List<Entity> operationProductOutComponents = operationComponent.getHasManyField("operationProductOutComponents");
        Map<Entity, BigDecimal> productsOutMap = new HashMap<Entity, BigDecimal>();

        for (Entity operationProductOutComponent : operationProductOutComponents) {
            Entity product = (Entity) operationProductOutComponent.getField("product");
            if (!technologyService.getProductType(product, technology).equals(TechnologyService.WASTE)) {
                BigDecimal quantity = ((BigDecimal) operationProductOutComponent.getField("quantity")).multiply(plannedQuantity,
                        MathContext.DECIMAL128);
                productsOutMap.put(product, quantity);
            }
        }
        return productsOutMap;
    }

    private boolean aggregateTreeDataPerOutProducts(final EntityTreeNode node,
            final Map<Entity, Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>> operations,
            final String type, final Entity order, final BigDecimal plannedQuantity, Entity technology) {
        if (OPERATION_NODE_ENTITY_TYPE.equals(node.getField("entityType"))) {
            Entity entityKey = null;
            Entity operation = (Entity) node.getField("operation");
            List<Entity> operationProductInComponents = node.getHasManyField("operationProductInComponents");
            if (operationProductInComponents.size() == 0) {
                return false;
            }
            Entity productOutComponent = checkOutProducts(node, technology);
            if (productOutComponent == null) {
                return false;
            }

            if ("product".equals(type)) {
                Entity product = (Entity) order.getField("product");
                entityKey = product;
            } else if ("machine".equals(type)) {
                Object machine = operation.getField("machine");
                if (machine != null) {
                    entityKey = (Entity) machine;
                } else {
                    entityKey = null;
                }
            } else if ("worker".equals(type)) {
                Object worker = operation.getField("staff");
                if (worker != null) {
                    entityKey = (Entity) worker;
                } else {
                    entityKey = null;
                }
            }
            Map<Entity, BigDecimal> productsInMap = new HashMap<Entity, BigDecimal>();
            for (Entity operationProductInComponent : operationProductInComponents) {
                Entity product = (Entity) operationProductInComponent.getField("product");
                BigDecimal quantity = ((BigDecimal) operationProductInComponent.getField("quantity")).multiply(plannedQuantity,
                        MathContext.DECIMAL128).divide((BigDecimal) productOutComponent.getField("quantity"),
                        MathContext.DECIMAL128);
                EntityTreeNode prevOperation = findPreviousOperation(node, product, technology);
                if (prevOperation != null) {
                    boolean success = aggregateTreeDataPerOutProducts(prevOperation, operations, type, order, quantity,
                            technology);
                    if (!success) {
                        return false;
                    }
                }
                productsInMap.put(product, quantity);
            }
            Map<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>> operationMap = null;
            if (operations.containsKey(entityKey)) {
                operationMap = operations.get(entityKey);
            } else {
                operationMap = new HashMap<Pair<Entity, Entity>, Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>>>();
            }
            Pair<Entity, Entity> pair = Pair.of((Entity) node, order);
            Pair<Map<Entity, BigDecimal>, Map<Entity, BigDecimal>> mapPair = Pair.of(productsInMap,
                    prepareOutProducts(node, plannedQuantity, technology));
            operationMap.put(pair, mapPair);
            operations.put(entityKey, operationMap);
        } else {
            EntityTreeNode rootNode = node.getBelongsToField("referenceTechnology").getTreeField("operationComponents").getRoot();
            if (rootNode != null) {
                boolean success = aggregateTreeDataPerOutProducts(rootNode, operations, type, order, plannedQuantity, technology);
                if (!success) {
                    return false;
                }
            }
        }
        return true;
    }

    private Map<Entity, BigDecimal> prepareInProductsPerTechnology(final Entity operationComponent,
            final BigDecimal plannedQuantity) {
        List<Entity> operationProductInComponents = operationComponent.getHasManyField("operationProductInComponents");
        Map<Entity, BigDecimal> productsInMap = new HashMap<Entity, BigDecimal>();
        for (Entity operationProductInComponent : operationProductInComponents) {
            Entity product = (Entity) operationProductInComponent.getField("product");
            BigDecimal quantity = ((BigDecimal) operationProductInComponent.getField("quantity")).multiply(plannedQuantity,
                    MathContext.DECIMAL128);
            productsInMap.put(product, quantity);
        }
        return productsInMap;
    }

    private Map<Entity, BigDecimal> prepareOutProducts(final EntityTreeNode node, final BigDecimal plannedQuantity,
            Entity technology) {
        Map<Entity, BigDecimal> productsOutMap = new HashMap<Entity, BigDecimal>();
        List<Entity> operationProductOutComponents = node.getHasManyField("operationProductOutComponents");

        for (Entity operationProductOutComponent : operationProductOutComponents) {
            Entity product = (Entity) operationProductOutComponent.getField("product");
            if (!(technologyService.getProductType(product, technology).equals(TechnologyService.WASTE))) {
                productsOutMap.put(product, plannedQuantity);
            }
        }
        return productsOutMap;
    }

    private void countQuntityComponentPerTechnology(final Map<Entity, BigDecimal> products,
            final List<Entity> operationComponents, final boolean onlyComponents, final BigDecimal plannedQuantity,
            Entity technology) {
        for (Entity operationComponent : operationComponents) {
            if (OPERATION_NODE_ENTITY_TYPE.equals(operationComponent.getField("entityType"))) {
                List<Entity> operationProductComponents = operationComponent.getHasManyField("operationProductInComponents");
                for (Entity operationProductComponent : operationProductComponents) {
                    Entity product = (Entity) operationProductComponent.getField("product");
                    if (!onlyComponents
                            || technologyService.getProductType(product, technology).equals(TechnologyService.COMPONENT)) {
                        if (products.containsKey(product)) {
                            BigDecimal quantity = products.get(product);
                            quantity = ((BigDecimal) operationProductComponent.getField("quantity")).multiply(plannedQuantity,
                                    MathContext.DECIMAL128).add(quantity);
                            products.put(product, quantity);
                        } else {
                            products.put(product, ((BigDecimal) operationProductComponent.getField("quantity")).multiply(
                                    plannedQuantity, MathContext.DECIMAL128));
                        }
                    }
                }
            } else {
                countQuntityComponentPerTechnology(products, operationComponent.getBelongsToField("referenceTechnology")
                        .getTreeField("operationComponents"), onlyComponents, plannedQuantity, technology);
            }
        }
    }

    private boolean countQuntityComponentPerOutProducts(final Map<Entity, BigDecimal> products, final EntityTreeNode node,
            final boolean onlyComponents, final BigDecimal plannedQuantity, Entity technology) {
        if (OPERATION_NODE_ENTITY_TYPE.equals(node.getField("entityType"))) {
            List<Entity> operationProductInComponents = node.getHasManyField("operationProductInComponents");
            if (operationProductInComponents.size() == 0) {
                return false;
            }
            Entity productOutComponent = checkOutProducts(node, technology);
            if (productOutComponent == null) {
                return false;
            }
            for (Entity operationProductInComponent : operationProductInComponents) {
                Entity product = (Entity) operationProductInComponent.getField("product");
                BigDecimal quantity = ((BigDecimal) operationProductInComponent.getField("quantity")).multiply(plannedQuantity,
                        MathContext.DECIMAL128).divide((BigDecimal) productOutComponent.getField("quantity"),
                        MathContext.DECIMAL128);
                EntityTreeNode prevOperation = findPreviousOperation(node, product, technology);
                if (prevOperation != null) {
                    boolean success = countQuntityComponentPerOutProducts(products, prevOperation, onlyComponents, quantity,
                            technology);
                    if (!success) {
                        return false;
                    }
                }
                if (products.containsKey(product)) {
                    products.put(product, products.get(product).add(quantity));
                } else {
                    products.put(product, quantity);
                }
            }
        } else {
            EntityTreeNode rootNode = node.getBelongsToField("referenceTechnology").getTreeField("operationComponents").getRoot();
            if (rootNode != null) {
                boolean success = countQuntityComponentPerOutProducts(products, rootNode, onlyComponents, plannedQuantity,
                        technology);
                if (!success) {
                    return false;
                }
            }
        }
        return true;
    }

    private Entity checkOutProducts(final Entity operationComponent, Entity technology) {
        List<Entity> operationProductOutComponents = operationComponent.getHasManyField("operationProductOutComponents");
        Entity productOutComponent = null;
        if (operationProductOutComponents.size() == 0) {
            return null;
        } else {
            int productCount = 0;

            for (Entity operationProductOutComponent : operationProductOutComponents) {
                Entity product = (Entity) operationProductOutComponent.getField("product");
                if (!(technologyService.getProductType(product, technology).equals(TechnologyService.WASTE))) {
                    productOutComponent = operationProductOutComponent;
                    productCount++;
                }
            }
            if (productCount != 1) {
                return null;
            }
        }
        return productOutComponent;
    }

    private EntityTreeNode findPreviousOperation(final EntityTreeNode node, final Entity product, Entity technology) {
        for (EntityTreeNode operationComponent : node.getChildren()) {
            List<Entity> operationProductOutComponents = new ArrayList<Entity>();
            if (OPERATION_NODE_ENTITY_TYPE.equals(operationComponent.getField("entityType"))) {
                operationProductOutComponents = operationComponent.getHasManyField("operationProductOutComponents");
            } else {
                EntityTreeNode rootNode = operationComponent.getBelongsToField("referenceTechnology")
                        .getTreeField("operationComponents").getRoot();
                if (rootNode != null) {
                    operationProductOutComponents = rootNode.getHasManyField("operationProductOutComponents");
                }
            }

            for (Entity operationProductOutComponent : operationProductOutComponents) {
                Entity productOut = (Entity) operationProductOutComponent.getField("product");

                if (!(technologyService.getProductType(productOut, technology).equals(TechnologyService.WASTE))
                        && productOut.getField("number").equals(product.getField("number"))) {
                    return operationComponent;
                }
            }
        }
        return null;
    }

}
