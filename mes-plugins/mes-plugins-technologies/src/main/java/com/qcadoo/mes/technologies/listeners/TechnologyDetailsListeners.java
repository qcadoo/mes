/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.tree.ProductStructureTreeService;
import com.qcadoo.mes.technologies.tree.RemoveTOCService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.TreeComponent;
import com.qcadoo.view.api.components.WindowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TechnologyDetailsListeners {

    private static final Logger LOG = LoggerFactory.getLogger(TechnologyDetailsListeners.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductStructureTreeService productStructureTreeService;

    @Autowired
    private RemoveTOCService removeTOCService;

    private static final String OUT_PRODUCTS_REFERENCE = "outProducts";

    private static final String IN_PRODUCTS_REFERENCE = "inProducts";

    private static final String TECHNOLOGY_TREE_REFERENCE = "technologyTree";

    public void setGridEditable(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        setGridEditable(view);
    }

    public void removeOnlySelectedOperation(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        final TreeComponent technologyTree = (TreeComponent) view.getComponentByReference(TECHNOLOGY_TREE_REFERENCE);
        final Long selectedEntityId = technologyTree.getSelectedEntityId();
        Entity selectedOperation = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)
                .get(selectedEntityId);
        boolean removed = removeTOCService.removeOnlySelectedOperation(selectedOperation, view);
        if (removed) {
            FormComponent form = (FormComponent) view.getComponentByReference("form");

            form.performEvent(view, "reset");

            view.addMessage("technologies.technologyDetails.window.treeTab.technologyTree.success",
                    ComponentState.MessageType.SUCCESS);
        }
    }

    public void setGridEditable(final ViewDefinitionState view) {
        final TreeComponent technologyTree = (TreeComponent) view.getComponentByReference(TECHNOLOGY_TREE_REFERENCE);
        final boolean gridsShouldBeEnabled = technologyTree.getSelectedEntityId() != null;
        for (String componentReference : Sets.newHashSet(OUT_PRODUCTS_REFERENCE, IN_PRODUCTS_REFERENCE)) {
            view.getComponentByReference(componentReference).setEnabled(gridsShouldBeEnabled);
        }
    }

    public void generateProductStructure(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        FormComponent productStructureForm = (FormComponent) view.getComponentByReference("productStructureForm");
        Entity technology = form.getEntity();
        Entity productTechnology = technology.copy();
        EntityTree generatedTree = productStructureTreeService.generateProductStructureTree(view, technology);

        productTechnology.setField("productStructureTree", generatedTree);
        productStructureForm.setEntity(productTechnology);
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        window.setActiveTab("productStructure");
    }

    public void fillProducts(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity technology = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY).get(form.getEntityId());
        List<Entity> tocs = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);
        DataDefinition opicDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);
        List<Entity> operationsWithManyOutProducts = Lists.newArrayList();
        for (Entity toc : tocs) {
            List<Entity> outComponents = toc.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);
            Entity parent = toc.getBelongsToField(TechnologyOperationComponentFields.PARENT);
            if (outComponents.size() == 1 && Objects.nonNull(parent)) {
                Entity opoc = outComponents.get(0);
                Entity opic = opicDD.create();
                opic.setField(OperationProductInComponentFields.QUANTITY,
                        opoc.getField(OperationProductOutComponentFields.QUANTITY));
                opic.setField(OperationProductInComponentFields.PRODUCT,
                        opoc.getBelongsToField(OperationProductOutComponentFields.PRODUCT));
                opic.setField(OperationProductInComponentFields.OPERATION_COMPONENT, parent);
                opicDD.save(opic);
            } else if (outComponents.size() > 1) {
                operationsWithManyOutProducts.add(toc.getBelongsToField(TechnologyOperationComponentFields.OPERATION));
            }
        }
        if (!operationsWithManyOutProducts.isEmpty()) {
            state.addMessage("technologies.technologyDetails.window.tooManyOutProductsInOperation", MessageType.INFO,
                    operationsWithManyOutProducts.stream().map(o -> o.getStringField(OperationFields.NUMBER))
                            .collect(Collectors.joining(", ")));
        }
    }
}
