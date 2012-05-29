/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
package com.qcadoo.mes.technologies.tree;

import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.NODE_NUMBER;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;

@Service
public class TechnologyTreeValidationServiceImpl implements TechnologyTreeValidationService {

    private static final String L_PRODUCT = "product";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public final Map<String, Set<String>> checkConsumingManyProductsFromOneSubOp(final EntityTree technologyTree) {
        final Map<String, Set<String>> parentToChildsMap = Maps.newHashMap();
        if (technologyTree != null && !technologyTree.isEmpty()) {
            final EntityTreeNode rootNode = technologyTree.getRoot();
            collectChildrenProducingManyParentInputs(parentToChildsMap, rootNode);
        }
        return parentToChildsMap;
    }

    @Override
    public Map<String, Set<Entity>> checkConsumingTheSameProductFromManySubOperations(EntityTree technologyTree) {
        Map<String, Set<Entity>> parentToProductsMap = Maps.newHashMap();

        if (technologyTree != null && !technologyTree.isEmpty()) {
            final EntityTreeNode rootNode = technologyTree.getRoot();
            collectChildrenProducingTheSameParentInputs(parentToProductsMap, rootNode);
        }

        return parentToProductsMap;
    }

    private void collectChildrenProducingTheSameParentInputs(final Map<String, Set<Entity>> parentToProductsMap,
            final EntityTreeNode parentOperation) {
        final Set<Long> parentInProdIds = getProductIdsFromOperationComponent(parentOperation, OPERATION_PRODUCT_IN_COMPONENTS);

        Map<String, Set<Long>> intersections = Maps.newHashMap();

        for (EntityTreeNode subOperation : parentOperation.getChildren()) {
            final Set<Long> childOutProdIds = getProductIdsFromOperationComponent(subOperation, OPERATION_PRODUCT_OUT_COMPONENTS);

            Set<Long> intersection = Sets.intersection(parentInProdIds, childOutProdIds);
            intersections.put(subOperation.getStringField("nodeNumber"), intersection);
        }
        if (intersections.isEmpty()) {
            return;
        }
        for (Entry<String, Set<Long>> entry : intersections.entrySet()) {
            for (Entry<String, Set<Long>> entry1 : intersections.entrySet()) {
                if (entry.getKey() == null || entry1.getKey() == null) {
                    continue;
                }
                if (entry.getKey().equals(entry1.getKey())) {
                    continue;
                }

                Set<Long> commonProds = Sets.intersection(entry.getValue(), entry1.getValue());
                if (!commonProds.isEmpty()) {
                    appendProductsToMap(parentToProductsMap, parentOperation, commonProds);
                }
            }
        }

        for (EntityTreeNode subOperation : parentOperation.getChildren()) {
            collectChildrenProducingTheSameParentInputs(parentToProductsMap, subOperation);
        }
    }

    private void appendProductsToMap(final Map<String, Set<Entity>> parentToProductsMap, final EntityTreeNode parent,
            final Set<Long> commonProds) {
        DataDefinition dd = dataDefinitionService.get("basic", "product");
        String nodeNumber = parent.getStringField("nodeNumber");
        Set<Entity> productsSet = parentToProductsMap.get(nodeNumber);
        if (productsSet == null) {
            productsSet = Sets.newHashSet();
        }

        for (Long prodId : commonProds) {
            productsSet.add(dd.get(prodId));
        }

        parentToProductsMap.put(nodeNumber, productsSet);
    }

    private void collectChildrenProducingManyParentInputs(final Map<String, Set<String>> parentToChildsMap,
            final EntityTreeNode parentOperation) {
        final Set<Long> parentInProdIds = getProductIdsFromOperationComponent(parentOperation, OPERATION_PRODUCT_IN_COMPONENTS);
        for (EntityTreeNode subOperation : parentOperation.getChildren()) {
            final Set<Long> childOutProdIds = getProductIdsFromOperationComponent(subOperation, OPERATION_PRODUCT_OUT_COMPONENTS);
            if (hasMoreThanOneCommonProduct(parentInProdIds, childOutProdIds)) {
                appendNodeNumbersToMap(parentToChildsMap, parentOperation, subOperation);
            }
            collectChildrenProducingManyParentInputs(parentToChildsMap, subOperation);
        }
    }

    private void appendNodeNumbersToMap(final Map<String, Set<String>> map, final EntityTreeNode parent,
            final EntityTreeNode child) {
        final String parentNodeNumber = parent.getStringField(NODE_NUMBER);
        final String childNodeNumber = child.getStringField(NODE_NUMBER);

        if (map.containsKey(parentNodeNumber)) {
            final Set<String> subOpsNodeNums = map.get(parentNodeNumber);
            subOpsNodeNums.add(childNodeNumber);
            map.put(parentNodeNumber, subOpsNodeNums);
        } else {
            map.put(parentNodeNumber, Sets.newHashSet(childNodeNumber));
        }
    }

    private Set<Long> getProductIdsFromOperationComponent(final Entity opComponent, final String productsFieldName) {
        final Set<Long> productIds = Sets.newHashSet();
        for (Entity productComponent : opComponent.getHasManyField(productsFieldName)) {
            final Entity product = productComponent.getBelongsToField(L_PRODUCT);
            productIds.add(product.getId());
        }
        return productIds;
    }

    private boolean hasMoreThanOneCommonProduct(final Set<Long> parentInProdIds, final Set<Long> childInProdIds) {
        final Set<Long> prodIdsIntersect = Sets.intersection(parentInProdIds, childInProdIds);
        return prodIdsIntersect.size() > 1;
    }
}
