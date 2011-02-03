/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.view.components.tree;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class TreeNode {

    private Long id;

    private String label;

    private List<TreeNode> children;

    private final TreeDataType dataType;

    public TreeNode(final Long id, final String label, final TreeDataType dataType) {
        this.id = id;
        this.label = label;
        this.dataType = dataType;
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

    public JSONObject toJson() throws JSONException {
        JSONObject jsonNode = new JSONObject();
        jsonNode.put("id", id);
        jsonNode.put("label", label);
        jsonNode.put("dataType", dataType.toJson());

        if (children != null) {
            JSONArray childrenArray = new JSONArray();
            for (TreeNode kid : children) {
                childrenArray.put(kid.toJson());
            }
            jsonNode.put("children", childrenArray);
        }
        return jsonNode;
    }

    @Override
    public String toString() {
        return toStringWithTabs(0);
    }

    public String toStringWithTabs(int tabs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tabs; i++) {
            sb.append("    ");
        }
        sb.append("[" + id + "] " + label + "\n");
        if (children != null) {
            for (TreeNode kid : children) {
                sb.append(kid.toStringWithTabs(tabs + 1));
            }
        }
        return sb.toString();
    }

}
