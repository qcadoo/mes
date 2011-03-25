/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

package com.qcadoo.view.internal.components.tree;

import org.json.JSONException;
import org.json.JSONObject;

public class TreeDataType {

    private final String name;

    private String nodeLabelExpression;

    private String correspondingView;

    private String correspondingComponent;

    private boolean correspondingViewInModal = false;

    private String nodeIcon;

    private String newIcon;

    private boolean canHaveChildren = true;

    public TreeDataType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getNodeLabelExpression() {
        return nodeLabelExpression;
    }

    public String getCorrespondingView() {
        return correspondingView;
    }

    public String getCorrespondingComponent() {
        return correspondingComponent;
    }

    public void setNodeLabelExpression(final String nodeLabelExpression) {
        this.nodeLabelExpression = nodeLabelExpression;
    }

    public void setCorrespondingView(final String correspondingView) {
        this.correspondingView = correspondingView;
    }

    public void setCorrespondingComponent(final String correspondingComponent) {
        this.correspondingComponent = correspondingComponent;
    }

    public void setOption(final String type, final String value) {
        if ("nodeLabelExpression".equals(type)) {
            setNodeLabelExpression(value);
        } else if ("correspondingView".equals(type)) {
            setCorrespondingView(value);
        } else if ("correspondingComponent".equals(type)) {
            setCorrespondingComponent(value);
        } else if ("correspondingViewInModal".equals(type)) {
            setCorrespondingViewInModal(Boolean.parseBoolean(value));
        } else if ("nodeIcon".equals(type)) {
            setNodeIcon(value);
        } else if ("newIcon".equals(type)) {
            setNewIcon(value);
        } else if ("canHaveChildren".equals(type)) {
            setCanHaveChildren(Boolean.parseBoolean(value));
        } else {
            throw new IllegalStateException("Unknown tree 'dataType' option: " + type);
        }
    }

    public void validate() {
        if (newIcon == null) {
            newIcon = "newIcon16_dis.png";
        }
        if (nodeLabelExpression == null) {
            throw new IllegalStateException("Node 'dataType' of tree must contain 'nodeLabelExpression' option");
        }
    }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("correspondingView", correspondingView);
        obj.put("correspondingComponent", correspondingComponent);
        obj.put("correspondingViewInModal", correspondingViewInModal);
        obj.put("canHaveChildren", canHaveChildren);
        if (nodeIcon != null) {
            obj.put("nodeIcon", nodeIcon);
        }
        if (newIcon != null) {
            obj.put("newIcon", newIcon);
        }
        return obj;
    }

    public String getNodeIcon() {
        return nodeIcon;
    }

    public void setNodeIcon(String nodeIcon) {
        this.nodeIcon = nodeIcon;
    }

    public String getNewIcon() {
        return newIcon;
    }

    public void setNewIcon(String newIcon) {
        this.newIcon = newIcon;
    }

    public boolean isCanHaveChildren() {
        return canHaveChildren;
    }

    public void setCanHaveChildren(boolean canHaveChildren) {
        this.canHaveChildren = canHaveChildren;
    }

    public boolean isCorrespondingViewInModal() {
        return correspondingViewInModal;
    }

    public void setCorrespondingViewInModal(boolean correspondingViewInModal) {
        this.correspondingViewInModal = correspondingViewInModal;
    }
}
