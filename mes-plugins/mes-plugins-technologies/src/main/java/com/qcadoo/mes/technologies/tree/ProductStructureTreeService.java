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
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.ProductStructureTreeNodeFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.utils.EntityTreeUtilsService;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductStructureTreeService {

    private static final String L_FINAL_PRODUCT = "finalProduct";

    private static final String L_INTERMEDIATE = "intermediate";

    private static final String L_COMPONENT = "component";

    private static final String L_MATERIAL = "material";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private Entity addChild(final List<Entity> tree, final Entity child, final Entity parent, final String entityType) {
        child.setField(ProductStructureTreeNodeFields.PARENT, parent);
        child.setField(ProductStructureTreeNodeFields.NUMBER, (long) tree.size() + 1);
        child.setField(ProductStructureTreeNodeFields.PRIORITY, 1);
        child.setField(ProductStructureTreeNodeFields.ENTITY_TYPE, entityType);

        Entity savedChild = child.getDataDefinition().save(child);

        tree.add(savedChild);
        return savedChild;
    }

    private Entity findOperationForProductAndTechnology(final Entity product, final Entity technology) {
        DataDefinition operationComponentsDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
        List<Entity> operations = operationComponentsDD.find()
                .add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology)).list()
                .getEntities();
        for (Entity operation : operations) {
            Entity isResult = operation.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS)
                    .find().add(SearchRestrictions.belongsTo(ProductStructureTreeNodeFields.PRODUCT, product)).setMaxResults(1)
                    .uniqueResult();
            if (isResult != null) {
                return operation;
            }
        }
        return null;
    }

    private Entity findOperationForProductWithinChildren(final Entity product, final Entity toc) {
        List<Entity> operations = toc.getHasManyField(TechnologyOperationComponentFields.CHILDREN);
        for (Entity operation : operations) {
            Entity isResult = operation.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS)
                    .find().add(SearchRestrictions.belongsTo(ProductStructureTreeNodeFields.PRODUCT, product)).setMaxResults(1)
                    .uniqueResult();
            if (isResult != null) {
                return operation;
            }
        }
        return null;
    }

    private Entity findTechnologyForProduct(final Entity product) {
        DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);
        return technologyDD.find().add(SearchRestrictions.isNull(TechnologyFields.TECHNOLOGY_TYPE))
                .add(SearchRestrictions.belongsTo(ProductStructureTreeNodeFields.PRODUCT, product))
                .add(SearchRestrictions.or(SearchRestrictions.eq("state", "02accepted"),
                        SearchRestrictions.eq("state", "05checked")))
                .addOrder(SearchOrders.desc(TechnologyFields.MASTER)).addOrder(SearchOrders.asc(TechnologyFields.NUMBER))
                .setMaxResults(1).uniqueResult();
    }

    private BigDecimal findQuantityOfProductInOperation(final Entity product, final Entity operation) {
        EntityList outProducts = operation.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);

        Entity productComponent = outProducts.find()
                .add(SearchRestrictions.belongsTo(ProductStructureTreeNodeFields.PRODUCT, product)).setMaxResults(1)
                .uniqueResult();
        if (productComponent != null) {
            return productComponent.getDecimalField(ProductStructureTreeNodeFields.QUANTITY);
        }
        EntityList inProducts = operation.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);
        productComponent = inProducts.find().add(SearchRestrictions.belongsTo(ProductStructureTreeNodeFields.PRODUCT, product))
                .setMaxResults(1).uniqueResult();
        if (productComponent != null) {
            return productComponent.getDecimalField(ProductStructureTreeNodeFields.QUANTITY);
        }
        return null;
    }

    private void generateTreeForSubproducts(final Entity operation, final Entity technology, final List<Entity> tree,
            final Entity parent, final ViewDefinitionState view, final List<Long> usedTechnologies, final Entity mainTechnology) {
        EntityList productInComponents = operation
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);
        DataDefinition treeNodeDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_PRODUCT_STRUCTURE_TREE_NODE);
        for (Entity productInComp : productInComponents) {
            Entity child = treeNodeDD.create();
            Entity product = productInComp.getBelongsToField(OperationProductInComponentFields.PRODUCT);
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

                        child.setField(ProductStructureTreeNodeFields.TECHNOLOGY, subTechnology);
                        child.setField(ProductStructureTreeNodeFields.MAIN_TECHNOLOGY, mainTechnology);
                        child.setField(ProductStructureTreeNodeFields.OPERATION, operationForTechnology);
                        child.setField(ProductStructureTreeNodeFields.PRODUCT, product);
                        child.setField(ProductStructureTreeNodeFields.QUANTITY, quantity);
                        child.setField(ProductStructureTreeNodeFields.DIVISION,
                                operationForTechnology.getBelongsToField(TechnologyOperationComponentFields.DIVISION));
                        child.setField(ProductStructureTreeNodeFields.TECHNOLOGY_GROUP, technologyGroup);
                        child.setField(ProductStructureTreeNodeFields.STANDARD_PERFORMANCE_TECHNOLOGY,
                                standardPerformanceTechnology);
                        child = addChild(tree, child, parent, L_COMPONENT);
                        usedTechnologies.add(subTechnology.getId());
                        generateTreeForSubproducts(operationForTechnology, subTechnology, tree, child, view, usedTechnologies,
                                mainTechnology);
                    } else {

                        child.setField(ProductStructureTreeNodeFields.TECHNOLOGY, technology);
                        child.setField(ProductStructureTreeNodeFields.MAIN_TECHNOLOGY, mainTechnology);
                        child.setField(ProductStructureTreeNodeFields.PRODUCT, product);
                        child.setField(ProductStructureTreeNodeFields.QUANTITY, quantity);
                        child.setField(ProductStructureTreeNodeFields.OPERATION, subOperation);
                        child.setField(ProductStructureTreeNodeFields.DIVISION,
                                subOperation.getBelongsToField(TechnologyOperationComponentFields.DIVISION));
                        child = addChild(tree, child, parent, L_INTERMEDIATE);
                        if (view != null) {
                            FormComponent productStructureForm = (FormComponent) view
                                    .getComponentByReference("productStructureForm");
                            productStructureForm.addMessage(
                                    "technologies.technologyDetails.window.productStructure.productStructureForm.technologyAndOperationExists",
                                    MessageType.INFO, false,
                                    product.getStringField("number") + " " + product.getStringField("name"));
                        }
                        generateTreeForSubproducts(subOperation, technology, tree, child, view, usedTechnologies, mainTechnology);
                    }
                } else if (view != null) {
                    FormComponent productStructureForm = (FormComponent) view.getComponentByReference("productStructureForm");
                    productStructureForm.addMessage(
                            "technologies.technologyDetails.window.productStructure.productStructureForm.duplicateProductForTechnology",
                            MessageType.INFO, false, product.getStringField("number") + " " + product.getStringField("name"));
                }
            } else {
                Entity technologyGroup = technology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP);
                BigDecimal standardPerformanceTechnology = technology
                        .getDecimalField(TechnologyFields.STANDARD_PERFORMANCE_TECHNOLOGY);
                child.setField(ProductStructureTreeNodeFields.TECHNOLOGY, technology);
                child.setField(ProductStructureTreeNodeFields.MAIN_TECHNOLOGY, mainTechnology);
                child.setField(ProductStructureTreeNodeFields.PRODUCT, product);
                child.setField(ProductStructureTreeNodeFields.QUANTITY, quantity);
                child.setField(ProductStructureTreeNodeFields.TECHNOLOGY_GROUP, technologyGroup);
                child.setField(ProductStructureTreeNodeFields.STANDARD_PERFORMANCE_TECHNOLOGY, standardPerformanceTechnology);

                if (subOperation != null) {
                    child.setField(ProductStructureTreeNodeFields.OPERATION, subOperation);
                    child.setField(ProductStructureTreeNodeFields.DIVISION,
                            subOperation.getBelongsToField(TechnologyOperationComponentFields.DIVISION));

                    child = addChild(tree, child, parent, L_INTERMEDIATE);
                    generateTreeForSubproducts(subOperation, technology, tree, child, view, usedTechnologies, mainTechnology);
                } else {
                    child.setField(ProductStructureTreeNodeFields.OPERATION, operation);
                    child.setField(ProductStructureTreeNodeFields.DIVISION,
                            operation.getBelongsToField(TechnologyOperationComponentFields.DIVISION));
                    child.setField(ProductStructureTreeNodeFields.TECHNOLOGY_GROUP, technologyGroup);
                    child.setField(ProductStructureTreeNodeFields.STANDARD_PERFORMANCE_TECHNOLOGY, standardPerformanceTechnology);

                    addChild(tree, child, parent, L_MATERIAL);
                }
            }
        }

    }

    public EntityTree generateProductStructureTree(final ViewDefinitionState view, final Entity technology) {
        Entity technologyFromDB = technology.getDataDefinition().get(technology.getId());
        EntityTree tree = technologyFromDB.getTreeField(TechnologyFields.PRODUCT_STRUCTURE_TREE);
        if (tree.getRoot() != null) {
            Date productStructureCreateDate = tree.getRoot().getDateField(ProductStructureTreeNodeFields.CREATE_DATE);
            Entity product = technology.getBelongsToField(TechnologyFields.PRODUCT);
            Entity operation = findOperationForProductAndTechnology(product, technology);
            List<Entity> treeEntities = tree.find().list().getEntities();
            if (checkSubTechnologiesSubstitution(treeEntities)
                    || checkIfSubTechnologiesChanged(operation, productStructureCreateDate)) {
                deleteProductStructureTree(treeEntities);
            } else {
                return tree;
            }
        }
        DataDefinition treeNodeDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_PRODUCT_STRUCTURE_TREE_NODE);
        Entity root = treeNodeDD.create();
        Entity product = technology.getBelongsToField(TechnologyFields.PRODUCT);
        Entity operation = findOperationForProductAndTechnology(product, technology);
        BigDecimal quantity = findQuantityOfProductInOperation(product, operation);
        Entity technologyGroup = technology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP);
        BigDecimal standardPerformanceTechnology = technology.getDecimalField(TechnologyFields.STANDARD_PERFORMANCE_TECHNOLOGY);
        root.setField(ProductStructureTreeNodeFields.TECHNOLOGY, technology);
        root.setField(ProductStructureTreeNodeFields.MAIN_TECHNOLOGY, technology);
        root.setField(ProductStructureTreeNodeFields.PRODUCT, product);
        root.setField(ProductStructureTreeNodeFields.OPERATION, operation);
        root.setField(ProductStructureTreeNodeFields.QUANTITY, quantity);
        root.setField(ProductStructureTreeNodeFields.DIVISION,
                operation.getBelongsToField(TechnologyOperationComponentFields.DIVISION));
        root.setField(ProductStructureTreeNodeFields.TECHNOLOGY_GROUP, technologyGroup);
        root.setField(ProductStructureTreeNodeFields.STANDARD_PERFORMANCE_TECHNOLOGY, standardPerformanceTechnology);

        List<Entity> productStructureList = new ArrayList<>();
        root = addChild(productStructureList, root, null, L_FINAL_PRODUCT);

        List<Long> usedTechnologies = new ArrayList<>();
        usedTechnologies.add(technology.getId());

        generateTreeForSubproducts(operation, technology, productStructureList, root, view, usedTechnologies, technology);

        return EntityTreeUtilsService.getDetachedEntityTree(productStructureList);
    }

    private void deleteProductStructureTree(List<Entity> treeEntities) {
        for (Entity entity : treeEntities) {
            entity.getDataDefinition().delete(entity.getId());
        }
    }

    private boolean checkSubTechnologiesSubstitution(List<Entity> treeEntities) {
        for (Entity entity : treeEntities) {
            String entityType = entity.getStringField(ProductStructureTreeNodeFields.ENTITY_TYPE);
            if (entityType.equals(L_INTERMEDIATE) || entityType.equals(L_FINAL_PRODUCT)) {
                continue;
            }
            Entity product = entity.getBelongsToField(ProductStructureTreeNodeFields.PRODUCT);
            Entity newTechnology = findTechnologyForProduct(product);
            if (entityType.equals(L_MATERIAL) && newTechnology != null) {
                return true;
            } else if (entityType.equals(L_COMPONENT)) {
                Entity oldTechnology = entity.getBelongsToField(ProductStructureTreeNodeFields.TECHNOLOGY);
                if (oldTechnology != null && newTechnology == null
                        || oldTechnology != null && !oldTechnology.getId().equals(newTechnology.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkIfSubTechnologiesChanged(Entity operation, Date productStructureCreateDate) {
        for (Entity productInComp : operation
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS)) {
            Entity product = productInComp.getBelongsToField(OperationProductInComponentFields.PRODUCT);
            Entity subOperation = findOperationForProductWithinChildren(product, operation);
            Entity subTechnology = findTechnologyForProduct(product);

            if (subTechnology != null) {
                Entity technologyStateChange = subTechnology.getHasManyField(TechnologyFields.STATE_CHANGES).find()
                        .add(SearchRestrictions.eq("status", StateChangeStatus.SUCCESSFUL.getStringValue()))
                        .addOrder(SearchOrders.desc("dateAndTime")).setMaxResults(1).uniqueResult();
                if (productStructureCreateDate.before(technologyStateChange.getDateField("dateAndTime"))) {
                    return true;
                }
                if (subOperation == null) {
                    Entity operationForTechnology = findOperationForProductAndTechnology(product, subTechnology);
                    boolean changed = checkIfSubTechnologiesChanged(operationForTechnology, productStructureCreateDate);
                    if (changed) {
                        return true;
                    }
                } else {
                    boolean changed = checkIfSubTechnologiesChanged(subOperation, productStructureCreateDate);
                    if (changed) {
                        return true;
                    }
                }
            } else if (subOperation != null) {
                boolean changed = checkIfSubTechnologiesChanged(subOperation, productStructureCreateDate);
                if (changed) {
                    return true;
                }
            }
        }
        return false;
    }

    public EntityTree getOperationComponentsFromTechnology(final Entity technology) {
        EntityTree productStructureTree = generateProductStructureTree(null, technology);
        return transformProductStructureTreeToTOCTree(productStructureTree);
    }

    private EntityTree transformProductStructureTreeToTOCTree(final EntityTree productStructureTree) {
        List<Entity> tocTree = Lists.newArrayList();
        DataDefinition tocDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
        Entity root = productStructureTree.getRoot();
        Long rootTocID = root.getBelongsToField(ProductStructureTreeNodeFields.OPERATION).getId();
        addChildTOC(tocTree, tocDD.get(rootTocID), null, root.getBelongsToField(ProductStructureTreeNodeFields.PRODUCT),
                L_FINAL_PRODUCT);
        Entity parent;
        for (Entity node : productStructureTree) {
            String entityType = node.getStringField(ProductStructureTreeNodeFields.ENTITY_TYPE);
            if (!entityType.equals(L_MATERIAL) && !entityType.equals(L_FINAL_PRODUCT)) {
                Long tocId = node.getBelongsToField(ProductStructureTreeNodeFields.OPERATION).getId();
                Entity toc = tocDD.get(tocId);
                Long parentId = node.getBelongsToField(ProductStructureTreeNodeFields.PARENT) != null
                        ? node.getBelongsToField(ProductStructureTreeNodeFields.PARENT)
                                .getBelongsToField(ProductStructureTreeNodeFields.OPERATION).getId()
                        : node.getBelongsToField(ProductStructureTreeNodeFields.OPERATION).getId();
                parent = getEntityById(tocTree, parentId);
                addChildTOC(tocTree, toc, parent, node.getBelongsToField(ProductStructureTreeNodeFields.PRODUCT), entityType);
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

    private Entity addChildTOC(final List<Entity> tree, final Entity child, final Entity parent, final Entity product,
            String type) {
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
            if (tocChildren.stream().noneMatch(e -> e.getId().equals(child.getId()))) {
                children.add(child);
            }
            parent.setField(TechnologyOperationComponentFields.CHILDREN, children);
        }
        tree.add(child);
        return child;
    }
}
