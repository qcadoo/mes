/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
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
        final Map<String, Set<String>> parentToChildrenMap = Maps.newHashMap();

        if (Objects.nonNull(technologyTree) && !technologyTree.isEmpty()) {
            final EntityTreeNode rootNode = technologyTree.getRoot();

            collectChildrenProducingManyParentInputs(parentToChildrenMap, rootNode);
        }

        return parentToChildrenMap;
    }

    @Override
    public Map<String, Set<Entity>> checkConsumingTheSameProductFromManySubOperations(final EntityTree technologyTree) {
        Map<String, Set<Entity>> parentToProductsMap = Maps.newHashMap();

        if (Objects.nonNull(technologyTree) && !technologyTree.isEmpty()) {
            final EntityTreeNode rootNode = technologyTree.getRoot();

            collectChildrenProducingTheSameParentInputs(parentToProductsMap, rootNode);
        }

        return parentToProductsMap;
    }

    private void collectChildrenProducingTheSameParentInputs(final Map<String, Set<Entity>> parentToProductsMap,
            final EntityTreeNode parentOperation) {
        final Set<Long> parentInProductIds = getProductIdsFromOperationComponent(parentOperation,
                TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);

        Map<Long, Set<Long>> intersections = Maps.newHashMap();

        for (EntityTreeNode subOperation : parentOperation.getChildren()) {
            final Set<Long> childOutProductIds = getProductIdsFromOperationComponent(subOperation,
                    TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);

            Set<Long> intersection = Sets.intersection(parentInProductIds, childOutProductIds);

            intersections.put(subOperation.getId(), intersection);
        }

        for (Entry<Long, Set<Long>> entry : intersections.entrySet()) {
            for (Entry<Long, Set<Long>> entry1 : intersections.entrySet()) {
                if (entry.getKey().equals(entry1.getKey())) {
                    continue;
                }

                Set<Long> commonProducts = Sets.intersection(entry.getValue(), entry1.getValue());

                if (!commonProducts.isEmpty()) {
                    appendProductsToMap(parentToProductsMap, parentOperation, commonProducts);
                }
            }
        }

        for (EntityTreeNode subOperation : parentOperation.getChildren()) {
            collectChildrenProducingTheSameParentInputs(parentToProductsMap, subOperation);
        }
    }

    private void appendProductsToMap(final Map<String, Set<Entity>> parentToProductsMap, final EntityTreeNode parent,
            final Set<Long> commonProducts) {
        DataDefinition productDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);

        String nodeNumber = parent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER);

        Set<Entity> productsSet = parentToProductsMap.get(nodeNumber);

        if (Objects.isNull(productsSet)) {
            productsSet = Sets.newHashSet();
        }

        for (Long prodId : commonProducts) {
            productsSet.add(productDD.get(prodId));
        }

        parentToProductsMap.put(nodeNumber, productsSet);
    }

    private void collectChildrenProducingManyParentInputs(final Map<String, Set<String>> parentToChildrenMap,
            final EntityTreeNode parentOperation) {
        final Set<Long> parentInProductIds = getProductIdsFromOperationComponent(parentOperation,
                TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);

        for (EntityTreeNode subOperation : parentOperation.getChildren()) {
            final Set<Long> childOutProductIds = getProductIdsFromOperationComponent(subOperation,
                    TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);

            if (hasMoreThanOneCommonProduct(parentInProductIds, childOutProductIds)) {
                appendNodeNumbersToMap(parentToChildrenMap, parentOperation, subOperation);
            }

            collectChildrenProducingManyParentInputs(parentToChildrenMap, subOperation);
        }
    }

    private void appendNodeNumbersToMap(final Map<String, Set<String>> parentToChildrenMap, final EntityTreeNode parent,
            final EntityTreeNode child) {
        final String parentNodeNumber = parent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER);
        final String childNodeNumber = child.getStringField(TechnologyOperationComponentFields.NODE_NUMBER);

        if (parentToChildrenMap.containsKey(parentNodeNumber)) {
            final Set<String> subOpsNodeNumbers = parentToChildrenMap.get(parentNodeNumber);

            subOpsNodeNumbers.add(childNodeNumber);

            parentToChildrenMap.put(parentNodeNumber, subOpsNodeNumbers);
        } else {
            parentToChildrenMap.put(parentNodeNumber, Sets.newHashSet(childNodeNumber));
        }
    }

    private Set<Long> getProductIdsFromOperationComponent(final Entity operationComponent, final String productsFieldName) {
        final Set<Long> productIds = Sets.newHashSet();

        for (Entity operationProductComponent : operationComponent.getHasManyField(productsFieldName)) {
            final Entity product = operationProductComponent.getBelongsToField(L_PRODUCT);

            if (Objects.nonNull(product)) {
                productIds.add(product.getId());
            }
        }

        return productIds;
    }

    private boolean hasMoreThanOneCommonProduct(final Set<Long> parentInProductIds, final Set<Long> childInProductIds) {
        final Set<Long> productIdsIntersect = Sets.intersection(parentInProductIds, childInProductIds);

        return productIdsIntersect.size() > 1;
    }

}
