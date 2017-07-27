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
package com.qcadoo.mes.technologies.tree;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
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

    private static final String L_DIVISION = "division";

    private static final String L_PARENT = "parent";

    private static final String L_MATERIAL = "material";

    private static final String ENTITY_TYPE = "entityType";

    private static final String L_TECHNOLOGY_GROUP = "technologyGroup";

    private static final String L_STANDARD_PERFORMANCE_TECHNOLOGY = "standardPerformanceTechnology";

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

    private Entity findOperationForProductWithinChildren(final Entity product, final Entity toc) {
        List<Entity> operations = toc.getHasManyField(TechnologyOperationComponentFields.CHILDREN);
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
                .add(SearchRestrictions.isNull(TechnologyFields.TECHNOLOGY_TYPE))
                .add(SearchRestrictions.isNull(TechnologyFields.TECHNOLOGY_GROUP))
                .add(SearchRestrictions.and(
                        SearchRestrictions.belongsTo(L_PRODUCT, product),
                        SearchRestrictions.or(SearchRestrictions.eq("state", "02accepted"),
                                SearchRestrictions.eq("state", "05checked")))).list().getEntities();
        Entity result = null;
        for (Entity technology : technologiesForProduct) {
            boolean isMaster = technology.getBooleanField("master");
            if (isMaster) {
                return technology;
            } else if (result != null) {
                if (result.getStringField(L_NUMBER).compareTo(technology.getStringField(L_NUMBER)) < 0) {
                    result = technology;
                }
            } else {
                result = technology;
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
            Entity subOperation = findOperationForProductWithinChildren(product, operation);
            BigDecimal quantity = findQuantityOfProductInOperation(product, operation);
            Entity subTechnology = findTechnologyForProduct(product);

            if (subTechnology != null) {
                if (!usedTechnologies.contains(subTechnology.getId())) {
                    if (subOperation == null) {
                        Entity operationForTechnology = findOperationForProductAndTechnology(product, subTechnology);
                        Entity technologyGroup = subTechnology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP);
                        BigDecimal standardPerformanceTechnology = subTechnology
                                .getDecimalField(TechnologyFields.STANDARD_PERFORMANCE_TECHNOLOGY);

                        child.setField(L_TECHNOLOGY, subTechnology);
                        child.setField(L_OPERATION, operationForTechnology);
                        child.setField(L_PRODUCT, product);
                        child.setField(L_QUANTITY, quantity);
                        child.setField(L_DIVISION,
                                operationForTechnology.getBelongsToField(TechnologyOperationComponentFields.DIVISION));
                        child.setField(L_TECHNOLOGY_GROUP, technologyGroup);
                        child.setField(L_STANDARD_PERFORMANCE_TECHNOLOGY, standardPerformanceTechnology);
                        addChild(tree, child, parent, L_COMPONENT);
                        usedTechnologies.add(subTechnology.getId());
                        generateTreeForSubproducts(operationForTechnology, subTechnology, tree, child, view, usedTechnologies);
                    } else {

                        child.setField(L_TECHNOLOGY, technology);
                        child.setField(L_PRODUCT, product);
                        child.setField(L_QUANTITY, quantity);
                        child.setField(L_OPERATION, subOperation);
                        child.setField(L_DIVISION, subOperation.getBelongsToField(TechnologyOperationComponentFields.DIVISION));
                        addChild(tree, child, parent, L_INTERMEDIATE);
                        if (view != null) {
                            FormComponent productStructureForm = (FormComponent) view
                                    .getComponentByReference("productStructureForm");
                            productStructureForm
                                    .addMessage(
                                            "technologies.technologyDetails.window.productStructure.productStructureForm.technologyAndOperationExists",
                                            MessageType.INFO, false,
                                            product.getStringField("number") + " " + product.getStringField("name"));
                        }
                        generateTreeForSubproducts(subOperation, technology, tree, child, view, usedTechnologies);
                    }
                } else if (view != null) {
                    FormComponent productStructureForm = (FormComponent) view.getComponentByReference("productStructureForm");
                    productStructureForm
                            .addMessage(
                                    "technologies.technologyDetails.window.productStructure.productStructureForm.duplicateProductForTechnology",
                                    MessageType.INFO, false,
                                    product.getStringField("number") + " " + product.getStringField("name"));
                }
            } else {
                Entity technologyGroup = technology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP);
                BigDecimal standardPerformanceTechnology = technology
                        .getDecimalField(TechnologyFields.STANDARD_PERFORMANCE_TECHNOLOGY);
                child.setField(L_TECHNOLOGY, technology);
                child.setField(L_PRODUCT, product);
                child.setField(L_QUANTITY, quantity);
                child.setField(L_TECHNOLOGY_GROUP, technologyGroup);
                child.setField(L_STANDARD_PERFORMANCE_TECHNOLOGY, standardPerformanceTechnology);

                if (subOperation != null) {
                    child.setField(L_OPERATION, subOperation);
                    child.setField(L_DIVISION, subOperation.getBelongsToField(TechnologyOperationComponentFields.DIVISION));

                    addChild(tree, child, parent, L_INTERMEDIATE);
                    generateTreeForSubproducts(subOperation, technology, tree, child, view, usedTechnologies);
                } else {
                    child.setField(L_OPERATION, operation);
                    child.setField(L_DIVISION, operation.getBelongsToField(TechnologyOperationComponentFields.DIVISION));
                    child.setField(L_TECHNOLOGY_GROUP, technologyGroup);
                    child.setField(L_STANDARD_PERFORMANCE_TECHNOLOGY, standardPerformanceTechnology);

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
        Entity technologyGroup = technology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP);
        BigDecimal standardPerformanceTechnology = technology.getDecimalField(TechnologyFields.STANDARD_PERFORMANCE_TECHNOLOGY);
        root.setField(L_TECHNOLOGY, technology);
        root.setField(L_PRODUCT, product);
        root.setField(L_OPERATION, operation);
        root.setField(L_QUANTITY, quantity);
        root.setField(L_DIVISION, operation.getBelongsToField(TechnologyOperationComponentFields.DIVISION));
        root.setField(L_TECHNOLOGY_GROUP, technologyGroup);
        root.setField(L_STANDARD_PERFORMANCE_TECHNOLOGY, standardPerformanceTechnology);

        List<Entity> productStructureList = new ArrayList<>();
        addChild(productStructureList, root, null, L_FINAL_PRODUCT);

        List<Long> usedTechnologies = new ArrayList<>();
        usedTechnologies.add(technology.getId());

        generateTreeForSubproducts(operation, technology, productStructureList, root, view, usedTechnologies);
        EntityTree productStructureTree = EntityTreeUtilsService.getDetachedEntityTree(productStructureList);

        return productStructureTree;
    }

    public EntityTree getOperationComponentsFromTechnology(final Entity technology) {

        EntityTree productStructureTree = generateProductStructureTree(null, technology);
        return transformProductStructureTreeToTOCTree(productStructureTree);
    }

    private EntityTree transformProductStructureTreeToTOCTree(final EntityTree productStructureTree) {
        List<Entity> tocTree = Lists.newArrayList();
        DataDefinition tocDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
        Entity parent = null;
        Entity root = productStructureTree.getRoot();
        Long rootTocID = root.getBelongsToField(L_OPERATION).getId();
        addChildTOC(tocTree, tocDD.get(rootTocID), parent, root.getBelongsToField(L_PRODUCT), L_FINAL_PRODUCT);
        for (Entity node : productStructureTree) {
            String entityType = node.getStringField(ENTITY_TYPE);
            if (!entityType.equals(L_MATERIAL) && !entityType.equals(L_FINAL_PRODUCT)) {
                Long tocId = node.getBelongsToField(L_OPERATION).getId();
                Entity toc = tocDD.get(tocId);
                Long parentId = node.getBelongsToField(L_PARENT) != null ? node.getBelongsToField(L_PARENT)
                        .getBelongsToField(L_OPERATION).getId() : node.getBelongsToField(L_OPERATION).getId();
                parent = getEntityById(tocTree, parentId);
                addChildTOC(tocTree, toc, parent, node.getBelongsToField(L_PRODUCT), entityType);
            }
        }
        return EntityTreeUtilsService.getDetachedEntityTree(tocTree);
    }

    private Entity getEntityById(final List<Entity> tree, final Long id) {
        for (Entity entity : tree) {
            if (entity.getId().equals(id)) {
                return entity;
            }
        }
        return null;
    }

    private Entity addChildTOC(final List<Entity> tree, final Entity child, final Entity parent, final Entity product, String type) {
        child.setField(TechnologyOperationComponentFields.PARENT, parent);
        child.setField(TechnologyOperationComponentFields.PRIORITY, 1);
        child.setField(TechnologyOperationComponentFields.TYPE_FROM_STRUCTURE_TREE, type);
        child.setField(TechnologyOperationComponentFields.PRODUCT_FROM_STRUCTURE_TREE, product);
        if (parent != null) {
            List<Entity> children = Lists.newArrayList();
            EntityList tocChildren = parent.getHasManyField(TechnologyOperationComponentFields.CHILDREN);
            if (!tocChildren.isEmpty()) {
                children = Lists.newArrayList(tocChildren);
            }
            if (!tocChildren.stream().filter(e -> e.getId().equals(child.getId())).findAny().isPresent()) {
                children.add(child);
            }
            parent.setField(TechnologyOperationComponentFields.CHILDREN, children);
        }
        tree.add(child);
        return child;
    }
}
