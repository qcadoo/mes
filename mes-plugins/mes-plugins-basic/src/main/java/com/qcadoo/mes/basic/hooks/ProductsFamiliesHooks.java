/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.basic.hooks;

import static com.qcadoo.mes.basic.constants.ProductFields.PARENT;
import static com.qcadoo.mes.basic.constants.ProductFields.PRODUCT_FAMILY_CHILDREN_TREE;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.tree.ProductsFamiliesTreeService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class ProductsFamiliesHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductsFamiliesTreeService productsFamiliesTreeService;

    private static CustomRestriction customRestriction = new CustomRestriction() {

        @Override
        public void addRestriction(final SearchCriteriaBuilder searchBuilder) {
            searchBuilder.add(SearchRestrictions.isNull(PARENT)).add(
                    SearchRestrictions.eq(ProductFields.ENTITY_TYPE, ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue()));
        }

    };

    public final void addDiscriminatorRestrictionToProductsFamilies(final ViewDefinitionState view) {
        GridComponent grid = (GridComponent) view.getComponentByReference("parents");
        grid.setCustomRestriction(customRestriction);
    }

    public void generateTreeWhenIdIsSet(final ViewDefinitionState view) {
        FormComponent form = getForm(view);
        try {
            Entity formEntity = generateFormEntity(view);
            if (formEntity == null) {
                return;
            }
            form.setEntity(formEntity);
            view.getComponentByReference(PRODUCT_FAMILY_CHILDREN_TREE).setEnabled(true);
        } catch (FormValidationException e) {
            form.addMessage(e.getMessage(), MessageType.FAILURE);
        }

    }

    private Entity generateFormEntity(final ViewDefinitionState view) {
        DataDefinition dd = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);

        FormComponent form = getForm(view);
        Entity formEntity = form.getEntity();
        GridComponent parentsGrid = (GridComponent) view.getComponentByReference("parents");
        if (parentsGrid.getSelectedEntities().isEmpty()) {
            return null;
        }
        Long productId = parentsGrid.getSelectedEntities().get(0).getId();

        if (productId == null) {
            throw new FormValidationException("basic.productFamiliesTree.noProductSelected");
        }

        Entity product = dd.get(productId);

        List<Entity> tree = productsFamiliesTreeService.getHierarchyProductsTree(product);
        EntityTree entityTree = EntityTreeUtilsService.getDetachedEntityTree(tree);
        formEntity.setId(productId);
        formEntity.setField(PRODUCT_FAMILY_CHILDREN_TREE, entityTree);

        return formEntity;
    }

    private FormComponent getForm(final ViewDefinitionState view) {
        return (FormComponent) view.getComponentByReference("form");
    }

    private static class FormValidationException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        FormValidationException(final String msg) {
            super(msg);
        }
    }

}
