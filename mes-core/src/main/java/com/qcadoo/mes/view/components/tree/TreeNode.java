package com.qcadoo.mes.view.components.tree;

import java.util.LinkedList;
import java.util.List;

public class TreeNode {

    private Long id;

    private String label;

    private List<TreeNode> children;

    public TreeNode() {

    }

    public TreeNode(Long id, String label) {
        this();
        this.id = id;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public void addChild(TreeNode child) {
        if (children == null) {
            children = new LinkedList<TreeNode>();
        }
        children.add(child);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
