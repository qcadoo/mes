package com.qcadoo.mes.basic.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newLinkedList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;

@Service
public class TreeNumberingService {
    
    private static Logger LOG = LoggerFactory.getLogger(TreeNumberingService.class);
    
    public void assignNumberToEntityTreeNodes(final EntityTree tree, final String numberFieldName) {
        if(tree != null && tree.getRoot() != null) {
            debug("null or incomplete EntityTree - " + tree);
            return;
        }
        checkArgument(numberFieldName != null, "given number field name is null");
        if (tree.size() == 0) {
            return;
        }
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
    
    private static final class EntityTreeNodeComparator implements Comparator<EntityTreeNode> {
        @Override
        public int compare(EntityTreeNode n1, EntityTreeNode n2) {
            Integer p1 = (Integer) n1.getField("priority");
            Integer p2 = (Integer) n2.getField("priority");
            return p1.compareTo(p2);
        }
        
    }
    
    private void debug(final String msg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(msg);
        }
    }
}