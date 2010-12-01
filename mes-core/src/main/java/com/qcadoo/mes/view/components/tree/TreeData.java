/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
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

/**
 * View value of TreeComponent.
 * 
 * @see com.qcadoo.mes.view.components.TreeComponent
 * @see com.qcadoo.mes.view.ViewValue
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
