/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.technologies.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.technologies.tree.ProductStructureTreeService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.TreeComponent;

@Service
public class TechnologyDetailsListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductStructureTreeService productStructureTreeService;

    private static final String OUT_PRODUCTS_REFERENCE = "outProducts";

    private static final String IN_PRODUCTS_REFERENCE = "inProducts";

    private static final String TECHNOLOGY_TREE_REFERENCE = "technologyTree";

    public void setGridEditable(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        setGridEditable(view);
    }

    public void setGridEditable(final ViewDefinitionState view) {
        final TreeComponent technologyTree = (TreeComponent) view.getComponentByReference(TECHNOLOGY_TREE_REFERENCE);
        final boolean gridsShouldBeEnabled = technologyTree.getSelectedEntityId() != null;
        for (String componentReference : Sets.newHashSet(OUT_PRODUCTS_REFERENCE, IN_PRODUCTS_REFERENCE)) {
            view.getComponentByReference(componentReference).setEnabled(gridsShouldBeEnabled);
        }
    }

    public void downloadAtachment(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent grid = (GridComponent) view.getComponentByReference("technologyAttachments");
        if (grid.getSelectedEntitiesIds() == null || grid.getSelectedEntitiesIds().size() == 0) {
            state.addMessage("technologies.technologyDetails.window.ribbon.atachments.nonSelectedAtachment", MessageType.INFO);
            return;
        }
        StringBuffer redirectUrl = new StringBuffer();
        redirectUrl.append("/rest/techologies/getAttachment.html");
        boolean isFirstParam = true;
        for (Long confectionProtocolId : grid.getSelectedEntitiesIds()) {
            if (isFirstParam) {
                redirectUrl.append("?");
                isFirstParam = false;
            } else {
                redirectUrl.append("&");
            }
            redirectUrl.append("id=");
            redirectUrl.append(confectionProtocolId);
        }
        view.redirectTo(redirectUrl.toString(), true, false);
    }

    public void generateProductStructure(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        FormComponent productStructureForm = (FormComponent) view.getComponentByReference("productStructureForm");
        Entity technology = form.getEntity();

        EntityTree generatedTree = productStructureTreeService.generateProductStructureTree(view, technology);

        technology.setField("productStructureTree", generatedTree);
        productStructureForm.setEntity(technology);

    }
}
