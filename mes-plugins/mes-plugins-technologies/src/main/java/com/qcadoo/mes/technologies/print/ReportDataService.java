/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.1
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

import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.FIELD_ENTITY_TYPE;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_BASIC_PRODUCT;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.OPERATION_COMPONENTS;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.OPERATION_PRODUCT_IN_COMPONENTS;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.OPERATION_PRODUCT_OUT_COMPONENTS;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.REFERENCE_TECHNOLOGY;

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

@Service
public class ReportDataService {

    private static final String COMPONENT_QUANTITY_ALGORITHM = "02perTechnology";

    private static final String OPERATION_NODE_ENTITY_TYPE = "operation";

    @Autowired
    private TechnologyService technologyService;

    public final void countQuantityForProductsIn(final Map<Entity, BigDecimal> products, final Entity technology,
            final BigDecimal plannedQuantity, final Boolean onlyComponents) {
        EntityTree operationComponents = technology.getTreeField(OPERATION_COMPONENTS);
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

    private void countQuntityComponentPerTechnology(final Map<Entity, BigDecimal> products,
            final List<Entity> operationComponents, final boolean onlyComponents, final BigDecimal plannedQuantity,
            final Entity technology) {
        for (Entity operationComponent : operationComponents) {
            if (OPERATION_NODE_ENTITY_TYPE.equals(operationComponent.getField(FIELD_ENTITY_TYPE))) {
                List<Entity> operationProductComponents = operationComponent.getHasManyField(OPERATION_PRODUCT_IN_COMPONENTS);
                for (Entity operationProductComponent : operationProductComponents) {
                    Entity product = (Entity) operationProductComponent.getField(MODEL_BASIC_PRODUCT);
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
                countQuntityComponentPerTechnology(products, operationComponent.getBelongsToField(REFERENCE_TECHNOLOGY)
                        .getTreeField(OPERATION_COMPONENTS), onlyComponents, plannedQuantity, technology);
            }
        }
    }

    private boolean countQuntityComponentPerOutProducts(final Map<Entity, BigDecimal> products, final EntityTreeNode node,
            final boolean onlyComponents, final BigDecimal plannedQuantity, final Entity technology) {
        if (OPERATION_NODE_ENTITY_TYPE.equals(node.getField(FIELD_ENTITY_TYPE))) {
            List<Entity> operationProductInComponents = node.getHasManyField(OPERATION_PRODUCT_IN_COMPONENTS);
            if (operationProductInComponents.size() == 0) {
                return false;
            }
            Entity productOutComponent = checkOutProducts(node, technology);
            if (productOutComponent == null) {
                return false;
            }
            for (Entity operationProductInComponent : operationProductInComponents) {
                Entity product = (Entity) operationProductInComponent.getField(MODEL_BASIC_PRODUCT);
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
            EntityTreeNode rootNode = node.getBelongsToField(REFERENCE_TECHNOLOGY).getTreeField(OPERATION_COMPONENTS).getRoot();
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

    private Entity checkOutProducts(final Entity operationComponent, final Entity technology) {
        List<Entity> operationProductOutComponents = operationComponent.getHasManyField(OPERATION_PRODUCT_OUT_COMPONENTS);
        Entity productOutComponent = null;
        if (operationProductOutComponents.size() == 0) {
            return null;
        } else {
            int productCount = 0;

            for (Entity operationProductOutComponent : operationProductOutComponents) {
                Entity product = (Entity) operationProductOutComponent.getField(MODEL_BASIC_PRODUCT);
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

    private EntityTreeNode findPreviousOperation(final EntityTreeNode node, final Entity product, final Entity technology) {
        for (EntityTreeNode operationComponent : node.getChildren()) {
            List<Entity> operationProductOutComponents = new ArrayList<Entity>();
            if (OPERATION_NODE_ENTITY_TYPE.equals(operationComponent.getField(FIELD_ENTITY_TYPE))) {
                operationProductOutComponents = operationComponent.getHasManyField(OPERATION_PRODUCT_OUT_COMPONENTS);
            } else {
                EntityTreeNode rootNode = operationComponent.getBelongsToField(REFERENCE_TECHNOLOGY)
                        .getTreeField(OPERATION_COMPONENTS).getRoot();
                if (rootNode != null) {
                    operationProductOutComponents = rootNode.getHasManyField(OPERATION_PRODUCT_OUT_COMPONENTS);
                }
            }

            for (Entity operationProductOutComponent : operationProductOutComponents) {
                Entity productOut = (Entity) operationProductOutComponent.getField(MODEL_BASIC_PRODUCT);

                if (!(technologyService.getProductType(productOut, technology).equals(TechnologyService.WASTE))
                        && productOut.getField("number").equals(product.getField("number"))) {
                    return operationComponent;
                }
            }
        }
        return null;
    }

}
