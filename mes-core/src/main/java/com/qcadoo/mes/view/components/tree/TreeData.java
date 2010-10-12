package com.qcadoo.mes.view.components.tree;

import java.util.LinkedList;
import java.util.List;

public class TreeData {

    private TreeNode rootNode;

    private final String contextFieldName;

    private final Long contextId;

    private List<Long> openedNodes;

    private Long selectedEntityId;

    public TreeNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(TreeNode rootNode) {
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

    public TreeData(TreeNode rootNode, String contextFieldName, Long contextId) {
        super();
        this.rootNode = rootNode;
        this.contextFieldName = contextFieldName;
        this.contextId = contextId;
    }

    public Long getSelectedEntityId() {
        return selectedEntityId;
    }

    public void setSelectedEntityId(Long selectedEntityId) {
        this.selectedEntityId = selectedEntityId;
    }

    public List<Long> getOpenedNodes() {
        return openedNodes;
    }

    public void setOpenedNodes(List<Long> openedNodes) {
        this.openedNodes = openedNodes;
    }

    public void addOpenedNode(Long nodeId) {
        if (openedNodes == null) {
            openedNodes = new LinkedList<Long>();
        }
        openedNodes.add(nodeId);
    }

}
