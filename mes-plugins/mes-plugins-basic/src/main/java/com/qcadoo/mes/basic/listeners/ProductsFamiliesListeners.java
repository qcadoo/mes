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
package com.qcadoo.mes.basic.listeners;

import static com.qcadoo.mes.basic.constants.ProductFields.PRODUCT_FAMILY_CHILDREN_TREE;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.hooks.ProductsFamiliesHooks;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.TreeComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class ProductsFamiliesListeners {

    @Autowired
    private ProductsFamiliesHooks productsFamiliesHooks;

    public final void generateHierarchyTree(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        productsFamiliesHooks.generateTreeWhenIdIsSet(view);
        disableEditButtonAfterGenerating(view);
    }

    private void disableEditButtonAfterGenerating(final ViewDefinitionState view) {
        TreeComponent tree = (TreeComponent) view.getComponentByReference(PRODUCT_FAMILY_CHILDREN_TREE);
        boolean enabled = false;
        if ((tree != null) && (tree.getSelectedEntityId() != null)) {
            enabled = true;
        }
        setEditButtonEnabled(view, enabled);
    }

    public final void editSelectedProduct(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        TreeComponent tree = (TreeComponent) view.getComponentByReference(PRODUCT_FAMILY_CHILDREN_TREE);
        Long productId = tree.getSelectedEntityId();

        if (productId == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", productId);

        String url = "../page/basic/productDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void enabledEditButton(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        setEditButtonEnabled(view, true);
    }

    private void setEditButtonEnabled(final ViewDefinitionState view, final boolean isEnabled) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonGroup edit = (RibbonGroup) window.getRibbon().getGroupByName("edit");
        RibbonActionItem editSelectedProductFromTree = edit.getItemByName("editSelectedProduct");
        editSelectedProductFromTree.setEnabled(isEnabled);
        editSelectedProductFromTree.requestUpdate(true);
    }

}
