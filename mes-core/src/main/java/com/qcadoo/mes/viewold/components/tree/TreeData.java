/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.viewold.components.tree;

import java.util.LinkedList;
import java.util.List;

/**
 * View value of TreeComponent.
 * 
 * @see com.qcadoo.mes.viewold.components.TreeComponent
 * @see com.qcadoo.mes.viewold.ViewValue
 */
public final class TreeData {

    private TreeNode rootNode;

    private final String contextFieldName;

    private final Long contextId;

    private List<Long> openedNodes;

    private Long selectedEntityId;

    public TreeNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(final TreeNode rootNode) {
        this.rootNode = rootNode;
    }

    public String getContextFieldName() {
        return contextFieldName;
    }

    public Long getContextId() {
        return contextId;
    }

    public TreeData() {
        this(null, null, null);
    }

    public TreeData(final TreeNode rootNode, final String contextFieldName, final Long contextId) {
        super();
        this.rootNode = rootNode;
        this.contextFieldName = contextFieldName;
        this.contextId = contextId;
    }

    public Long getSelectedEntityId() {
        return selectedEntityId;
    }

    public void setSelectedEntityId(final Long selectedEntityId) {
        this.selectedEntityId = selectedEntityId;
    }

    public List<Long> getOpenedNodes() {
        return openedNodes;
    }

    public void setOpenedNodes(final List<Long> openedNodes) {
        this.openedNodes = openedNodes;
    }

    public void addOpenedNode(final Long nodeId) {
        if (openedNodes == null) {
            openedNodes = new LinkedList<Long>();
        }
        openedNodes.add(nodeId);
    }

}
