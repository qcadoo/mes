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
 * Node of ViewValue of TreeComponent.
 * 
 * @see com.qcadoo.mes.viewold.components.TreeComponent
 * @see com.qcadoo.mes.viewold.ViewValue
 */
public final class TreeNode {

    private Long id;

    private String label;

    private List<TreeNode> children;

    public TreeNode() {

    }

    public TreeNode(final Long id, final String label) {
        this();
        this.id = id;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(final List<TreeNode> children) {
        this.children = children;
    }

    public void addChild(final TreeNode child) {
        if (children == null) {
            children = new LinkedList<TreeNode>();
        }
        children.add(child);
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

}
