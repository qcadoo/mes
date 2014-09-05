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
package com.qcadoo.mes.technologies.tree;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductStructureTreeService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private static final String L_TECHNOLOGY = "technology";

    private static final String L_OPERATION = "operation";

    private static final String L_PRODUCT = "product";

    private static final String L_QUANTITY = "quantity";

    private static final String L_NUMBER = "number";

    private static final String L_OPERATION_PRODUCT_OUT_COMPONENTS = "operationProductOutComponents";

    private static final String L_OPERATION_PRODUCT_IN_COMPONENTS = "operationProductInComponents";

    private static final String L_FINAL_PRODUCT = "finalProduct";

    private static final String L_INTERMEDIATE = "intermediate";

    private static final String L_COMPONENT = "component";

    private static final String L_MATERIAL = "material";

    private void addChild(final List<Entity> tree, final Entity child, final Entity parent, final String entityType) {
        child.setField("parent", parent);
        child.setId((long) tree.size() + 1);
        child.setField(L_NUMBER, child.getId());
        child.setField("priority", 1);
        child.setField("entityType", entityType);

        tree.add(child);
    }

    private Entity findOperationForProductAndTechnology(final Entity product, final Entity technology) {
        DataDefinition operationComponentsDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
        List<Entity> operations = operationComponentsDD.find().add(SearchRestrictions.belongsTo(L_TECHNOLOGY, technology)).list()
                .getEntities();
        for (Entity operation : operations) {

            Entity isResult = operation.getHasManyField(L_OPERATION_PRODUCT_OUT_COMPONENTS).find()
                    .add(SearchRestrictions.belongsTo(L_PRODUCT, product)).setMaxResults(1).uniqueResult();
            if (isResult != null) {
                return operation;
            }
        }
        return null;
    }

    private Entity findTechnologyForProduct(final Entity product) {
        DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);
        List<Entity> technologiesForProduct = technologyDD
                .find()
                .add(SearchRestrictions.and(
                        SearchRestrictions.belongsTo(L_PRODUCT, product),
                        SearchRestrictions.or(SearchRestrictions.eq("state", "02accepted"),
                                SearchRestrictions.eq("state", "05checked")))).list().getEntities();
        Entity result = null;
        for (Entity technology : technologiesForProduct) {
            boolean isMaster = technology.getBooleanField("master");
            if (isMaster) {
                return technology;
            } else {
                if (result != null) {
                    if (result.getStringField(L_NUMBER).compareTo(technology.getStringField(L_NUMBER)) < 0) {
                        result = technology;
                    }
                } else {
                    result = technology;
                }
            }
        }
        return result;
    }

    private BigDecimal findQuantityOfProductInOperation(final Entity product, final Entity operation) {
        EntityList outProducts = operation.getHasManyField(L_OPERATION_PRODUCT_OUT_COMPONENTS);

        Entity productComponent = outProducts.find().add(SearchRestrictions.belongsTo(L_PRODUCT, product)).setMaxResults(1)
                .uniqueResult();
        if (productComponent != null) {
            return productComponent.getDecimalField(L_QUANTITY);
        }
        EntityList inProducts = operation.getHasManyField(L_OPERATION_PRODUCT_IN_COMPONENTS);
        productComponent = inProducts.find().add(SearchRestrictions.belongsTo(L_PRODUCT, product)).setMaxResults(1)
                .uniqueResult();
        if (productComponent != null) {
            return productComponent.getDecimalField(L_QUANTITY);
        }
        return null;
    }

    private void generateTreeForSubproducts(final Entity operation, final Entity technology, final List<Entity> tree,
            final Entity parent, final ViewDefinitionState view, final List<Long> usedTechnologies) {
        EntityList productInComponents = operation.getHasManyField(L_OPERATION_PRODUCT_IN_COMPONENTS);
        DataDefinition treeNodeDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_PRODUCT_STRUCTURE_TREE_NODE);
        for (Entity productInComp : productInComponents) {
            Entity child = treeNodeDD.create();
            Entity product = productInComp.getBelongsToField(L_PRODUCT);
            Entity subOperation = findOperationForProductAndTechnology(product, technology);
            BigDecimal quantity = findQuantityOfProductInOperation(product, operation);
            Entity subTechnology = findTechnologyForProduct(product);

            if (subTechnology != null) {
                // if (subTechnology.getId() != technology.getId()) {
                if (!usedTechnologies.contains(subTechnology.getId())) {
                    if (subOperation == null) {
                        Entity operationForTechnology = findOperationForProductAndTechnology(product, subTechnology);
                        BigDecimal quantityForTechnology = findQuantityOfProductInOperation(product, operationForTechnology);

                        child.setField(L_TECHNOLOGY, subTechnology);
                        child.setField(L_OPERATION, operationForTechnology);
                        child.setField(L_PRODUCT, product);
                        child.setField(L_QUANTITY, quantityForTechnology);
                        addChild(tree, child, parent, L_COMPONENT);
                        usedTechnologies.add(subTechnology.getId());
                        generateTreeForSubproducts(operationForTechnology, subTechnology, tree, child, view, usedTechnologies);
                    } else {

                        child.setField(L_TECHNOLOGY, technology);
                        child.setField(L_PRODUCT, product);
                        child.setField(L_QUANTITY, quantity);
                        child.setField(L_OPERATION, subOperation);
                        addChild(tree, child, parent, L_INTERMEDIATE);

                        FormComponent productStructureForm = (FormComponent) view.getComponentByReference("productStructureForm");
                        productStructureForm
                                .addMessage(
                                        "technologies.technologyDetails.window.productStructure.productStructureForm.technologyAndOperationExists",
                                        MessageType.INFO, false,
                                        product.getStringField("number") + " " + product.getStringField("name"));
                        generateTreeForSubproducts(subOperation, technology, tree, child, view, usedTechnologies);
                    }
                } else {
                    FormComponent productStructureForm = (FormComponent) view.getComponentByReference("productStructureForm");
                    productStructureForm
                            .addMessage(
                                    "technologies.technologyDetails.window.productStructure.productStructureForm.duplicateProductForTechnology",
                                    MessageType.INFO, false,
                                    product.getStringField("number") + " " + product.getStringField("name"));
                }
            } else {
                child.setField(L_TECHNOLOGY, technology);
                child.setField(L_PRODUCT, product);
                child.setField(L_QUANTITY, quantity);

                if (subOperation != null) {
                    child.setField(L_OPERATION, subOperation);
                    addChild(tree, child, parent, L_INTERMEDIATE);
                    generateTreeForSubproducts(subOperation, technology, tree, child, view, usedTechnologies);
                } else {
                    child.setField(L_OPERATION, operation);
                    addChild(tree, child, parent, L_MATERIAL);
                }
            }
        }

    }

    public EntityTree generateProductStructureTree(final ViewDefinitionState view, final Entity technology) {
        DataDefinition treeNodeDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_PRODUCT_STRUCTURE_TREE_NODE);
        Entity root = treeNodeDD.create();
        Entity product = technology.getBelongsToField(L_PRODUCT);
        Entity operation = findOperationForProductAndTechnology(product, technology);
        BigDecimal quantity = findQuantityOfProductInOperation(product, operation);
        root.setField(L_TECHNOLOGY, technology);
        root.setField(L_PRODUCT, product);
        root.setField(L_OPERATION, operation);
        root.setField(L_QUANTITY, quantity);
        List<Entity> productStructureList = new ArrayList<Entity>();
        addChild(productStructureList, root, null, L_FINAL_PRODUCT);

        List<Long> usedTechnologies = new ArrayList<Long>();
        usedTechnologies.add(technology.getId());

        generateTreeForSubproducts(operation, technology, productStructureList, root, view, usedTechnologies);
        EntityTree productStructureTree = EntityTreeUtilsService.getDetachedEntityTree(productStructureList);

        return productStructureTree;
    }
}
