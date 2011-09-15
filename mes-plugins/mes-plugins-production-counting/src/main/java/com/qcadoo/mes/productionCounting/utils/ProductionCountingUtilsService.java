package com.qcadoo.mes.productionCounting.utils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newLinkedList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;

// This Service will be moved into numberGeneratorService
@Service
public class ProductionCountingUtilsService {
    // TODO MAKU perform some tests
    public void generateNumbersForEntityTree(final EntityTree tree, final String numberFieldName) {
        checkArgument(tree != null, "given EntityTree is null");
        checkArgument(numberFieldName != null, "given number field name is null");
        generateNumberForEntityTreeNode(tree.getRoot(), numberFieldName, "1.");
    }
    
    private void generateNumberForEntityTreeNode(final EntityTreeNode node, final String numberFieldName, final String prefix) {
        node.setField(numberFieldName, prefix);
        List<EntityTreeNode> childNodes = newLinkedList(node.getChildren());
        
        if (childNodes.size() == 0) {
            return;
        }
        
        Collections.sort(childNodes, new EntityTreeNodeComparator());
        Integer nestedOrderNumber = 1;
        
        for (EntityTreeNode childNode : childNodes) {
            generateNumberForEntityTreeNode(childNode, numberFieldName, prefix + nestedOrderNumber + ".");
            nestedOrderNumber++;
        }
    }
    
    private static class EntityTreeNodeComparator implements Comparator<EntityTreeNode> {
        @Override
        public int compare(EntityTreeNode n1, EntityTreeNode n2) {
            Integer p1 = (Integer) n1.getField("priority");
            Integer p2 = (Integer) n2.getField("priority");
            return p1.compareTo(p2);
        }
        
    }
}
