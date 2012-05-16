package com.qcadoo.mes.technologies.tree;

import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.NODE_NUMBER;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS;
import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;

@Service
public class TechnologyTreeValidationServiceImpl implements TechnologyTreeValidationService {

    private static final String L_PRODUCT = "product";

    @Override
    public final Map<String, Set<String>> checkConsumingManyProductsFromOneSubOp(final EntityTree technologyTree) {
        final Map<String, Set<String>> parentToChildsMap = Maps.newHashMap();
        if (technologyTree != null && !technologyTree.isEmpty()) {
            final EntityTreeNode rootNode = technologyTree.getRoot();
            collectChildrenProducingManyParentInputs(parentToChildsMap, rootNode);
        }
        return parentToChildsMap;
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
