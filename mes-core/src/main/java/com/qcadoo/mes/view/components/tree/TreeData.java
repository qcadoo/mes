package com.qcadoo.mes.view.components.tree;

public class TreeData {

    private TreeNode rootNode;

    private final String contextFieldName;

    private final Long contextId;

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

}
