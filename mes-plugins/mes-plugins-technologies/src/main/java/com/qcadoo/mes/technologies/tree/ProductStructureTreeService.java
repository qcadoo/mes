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
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.ProductBySizeGroupFields;
import com.qcadoo.mes.technologies.constants.ProductStructureTreeNodeFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateChangeFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.JoinType;
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

    public static final String L_COMPONENT = "component";

    public static final String L_MATERIAL = "material";

    private static final String L_PRODUCT_BY_SIZE_GROUP = "productBySizeGroup";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyService technologyService;

    private Entity addChild(final List<Entity> tree, final Entity child, final Entity parent, final String entityType) {
        child.setField(ProductStructureTreeNodeFields.PARENT, parent);
        child.setField(ProductStructureTreeNodeFields.NUMBER, (long) tree.size() + 1);
        child.setField(ProductStructureTreeNodeFields.PRIORITY, 1);
        child.setField(ProductStructureTreeNodeFields.ENTITY_TYPE, entityType);

        Entity savedChild = child.getDataDefinition().save(child);

        tree.add(savedChild);

        return savedChild;
    }

    public Entity findOperationForProductAndTechnology(final Entity product, final Entity technology) {
        Entity operationProductOutComponent = getOperationProductOutComponentDD().find()
                .createAlias(OperationProductOutComponentFields.OPERATION_COMPONENT, "c", JoinType.INNER)
                .add(SearchRestrictions.belongsTo("c." + TechnologyOperationComponentFields.TECHNOLOGY, technology))
                .add(SearchRestrictions.belongsTo(OperationProductInComponentFields.PRODUCT, product)).setMaxResults(1)
                .uniqueResult();

        if (Objects.nonNull(operationProductOutComponent)) {
            return operationProductOutComponent.getBelongsToField(OperationProductOutComponentFields.OPERATION_COMPONENT);
        } else {
            return null;
        }
    }

    public Entity findOperationForProductWithinChildren(final Entity product, final Entity toc) {
        Entity operationProductOutComponent = getOperationProductOutComponentDD().find()
                .createAlias(OperationProductOutComponentFields.OPERATION_COMPONENT, "c", JoinType.INNER)
                .add(SearchRestrictions.belongsTo("c." + TechnologyOperationComponentFields.PARENT, toc))
                .add(SearchRestrictions.belongsTo(OperationProductInComponentFields.PRODUCT, product)).setMaxResults(1)
                .uniqueResult();

        if (Objects.nonNull(operationProductOutComponent)) {
            return operationProductOutComponent.getBelongsToField(OperationProductOutComponentFields.OPERATION_COMPONENT);
        } else {
            return null;
        }
    }

    public Entity findTechnologyForProduct(final Entity product) {
        return getTechnologyDD().find()
                .add(SearchRestrictions.eq(TechnologyFields.ACTIVE, true))
                .add(SearchRestrictions.belongsTo(ProductStructureTreeNodeFields.PRODUCT, product))
                .add(SearchRestrictions.or(SearchRestrictions.eq(TechnologyFields.STATE, TechnologyStateStringValues.ACCEPTED),
                        SearchRestrictions.eq(TechnologyFields.STATE, TechnologyStateStringValues.CHECKED)))
                .addOrder(SearchOrders.desc(TechnologyFields.MASTER)).addOrder(SearchOrders.asc(TechnologyFields.NUMBER))
                .setMaxResults(1).uniqueResult();
    }

    private BigDecimal findQuantityOfProductInOperation(final Entity technologyInputProductType, final Entity product,
            final Entity operation) {
        EntityList operationProductOutComponents = operation
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);

        Entity operationProductOutComponent = operationProductOutComponents.find()
                .add(SearchRestrictions.belongsTo(OperationProductOutComponentFields.PRODUCT, product)).setMaxResults(1)
                .uniqueResult();

        if (Objects.nonNull(operationProductOutComponent)) {
            return operationProductOutComponent.getDecimalField(ProductStructureTreeNodeFields.QUANTITY);
        }

        EntityList operationProductInComponents = operation
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);

        operationProductOutComponent = operationProductInComponents.find()
                .add(SearchRestrictions.belongsTo(OperationProductInComponentFields.TECHNOLOGY_INPUT_PRODUCT_TYPE,
                        technologyInputProductType))
                .add(SearchRestrictions.belongsTo(OperationProductInComponentFields.PRODUCT, product)).setMaxResults(1)
                .uniqueResult();

        if (Objects.nonNull(operationProductOutComponent)) {
            return operationProductOutComponent.getDecimalField(ProductStructureTreeNodeFields.QUANTITY);
        }

        return null;
    }

    public EntityTree generateProductStructureTree(final ViewDefinitionState view, final Entity technology) {
        Entity product = technology.getBelongsToField(TechnologyFields.PRODUCT);
        Entity operation = findOperationForProductAndTechnology(product, technology);
        Entity technologyFromDB = technology.getDataDefinition().get(technology.getId());
        EntityTree tree = technologyFromDB.getTreeField(TechnologyFields.PRODUCT_STRUCTURE_TREE);

        if (Objects.nonNull(tree.getRoot())) {
            Date productStructureCreateDate = tree.getRoot().getDateField(ProductStructureTreeNodeFields.CREATE_DATE);

            List<Entity> treeEntities = tree.find().list().getEntities();

            Entity technologyStateChange = getLastTechnologyStateChange(technologyFromDB);

            if (productStructureCreateDate.before(technologyStateChange.getDateField(TechnologyStateChangeFields.DATE_AND_TIME))
                    || checkSubTechnologiesSubstitution(treeEntities)
                    || checkIfSubTechnologiesChanged(operation, productStructureCreateDate)) {
                deleteProductStructureTree(treeEntities);
            } else {
                return tree;
            }
        }

        Entity root = getProductStructureTreeNodeDD().create();

        BigDecimal quantity = findQuantityOfProductInOperation(null, product, operation);
        Entity technologyGroup = technology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP);
        BigDecimal standardPerformance = technologyService.getStandardPerformance(technology).orElse(null);

        root.setField(ProductStructureTreeNodeFields.TECHNOLOGY, technology);
        root.setField(ProductStructureTreeNodeFields.MAIN_TECHNOLOGY, technology);
        root.setField(ProductStructureTreeNodeFields.PRODUCT, product);
        root.setField(ProductStructureTreeNodeFields.OPERATION, operation);
        root.setField(ProductStructureTreeNodeFields.QUANTITY, quantity);
        root.setField(ProductStructureTreeNodeFields.DIVISION,
                operation.getBelongsToField(TechnologyOperationComponentFields.DIVISION));
        root.setField(ProductStructureTreeNodeFields.TECHNOLOGY_GROUP, technologyGroup);
        root.setField(ProductStructureTreeNodeFields.STANDARD_PERFORMANCE, standardPerformance);

        List<Entity> productStructureList = Lists.newArrayList();

        root = addChild(productStructureList, root, null, L_FINAL_PRODUCT);

        generateTreeForSubProducts(operation, technology, productStructureList, root, view, technology);

        technologyFromDB = technology.getDataDefinition().get(technology.getId());

        return technologyFromDB.getTreeField(TechnologyFields.PRODUCT_STRUCTURE_TREE);
    }

    private boolean checkIfSubTechnologiesChanged(final Entity operation, final Date productStructureCreateDate) {
        for (Entity operationProductInComponent : operation
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS)) {
            Entity product = operationProductInComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);
            Entity subOperation = findOperationForProductWithinChildren(product, operation);
            Entity subTechnology = findTechnologyForProduct(product);

            if (Objects.nonNull(subTechnology)) {
                Entity technologyStateChange = getLastTechnologyStateChange(subTechnology);

                if (productStructureCreateDate
                        .before(technologyStateChange.getDateField(TechnologyStateChangeFields.DATE_AND_TIME))) {
                    return true;
                }

                if (Objects.isNull(subOperation)) {
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
            } else if (Objects.nonNull(subOperation)) {
                boolean changed = checkIfSubTechnologiesChanged(subOperation, productStructureCreateDate);

                if (changed) {
                    return true;
                }
            }
        }

        return false;
    }

    private void deleteProductStructureTree(final List<Entity> treeEntities) {
        for (Entity entity : treeEntities) {
            entity.getDataDefinition().delete(entity.getId());
        }
    }

    private boolean checkSubTechnologiesSubstitution(final List<Entity> treeEntities) {
        for (Entity entity : treeEntities) {
            String entityType = entity.getStringField(ProductStructureTreeNodeFields.ENTITY_TYPE);

            if (entityType.equals(L_INTERMEDIATE) || entityType.equals(L_FINAL_PRODUCT)) {
                continue;
            }

            Entity product = entity.getBelongsToField(ProductStructureTreeNodeFields.PRODUCT);
            Entity newTechnology = findTechnologyForProduct(product);

            if (entityType.equals(L_MATERIAL) && Objects.nonNull(newTechnology)) {
                return true;
            } else if (entityType.equals(L_COMPONENT)) {
                Entity oldTechnology = entity.getBelongsToField(ProductStructureTreeNodeFields.TECHNOLOGY);

                if (Objects.nonNull(oldTechnology) && Objects.isNull(newTechnology)
                        || Objects.nonNull(oldTechnology) && !oldTechnology.getId().equals(newTechnology.getId())) {
                    return true;
                }
            }
        }

        return false;
    }

    private void generateTreeForSubProducts(final Entity operation, final Entity technology, final List<Entity> tree,
            final Entity parent, final ViewDefinitionState view, final Entity mainTechnology) {
        EntityList operationProductInComponents = operation
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);

        for (Entity operationProductInComponent : operationProductInComponents) {
            Entity child = getProductStructureTreeNodeDD().create();

            Entity technologyInputProductType = operationProductInComponent
                    .getBelongsToField(OperationProductInComponentFields.TECHNOLOGY_INPUT_PRODUCT_TYPE);
            Entity product = operationProductInComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);
            String unit = operationProductInComponent.getStringField(OperationProductInComponentFields.GIVEN_UNIT);

            Entity subOperation = findOperationForProductWithinChildren(product, operation);
            BigDecimal quantity = findQuantityOfProductInOperation(technologyInputProductType, product, operation);
            Entity subTechnology = findTechnologyForProduct(product);

            if (Objects.nonNull(subTechnology)) {
                if (Objects.isNull(subOperation)) {
                    Entity operationForTechnology = findOperationForProductAndTechnology(product, subTechnology);
                    Entity technologyGroup = subTechnology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP);
                    BigDecimal standardPerformance =technologyService.getStandardPerformance(subTechnology).orElse(null);

                    child.setField(ProductStructureTreeNodeFields.TECHNOLOGY, subTechnology);
                    child.setField(ProductStructureTreeNodeFields.MAIN_TECHNOLOGY, mainTechnology);
                    child.setField(ProductStructureTreeNodeFields.OPERATION, operationForTechnology);
                    child.setField(ProductStructureTreeNodeFields.PRODUCT, product);
                    child.setField(ProductStructureTreeNodeFields.QUANTITY, quantity);
                    child.setField(ProductStructureTreeNodeFields.DIVISION,
                            operationForTechnology.getBelongsToField(TechnologyOperationComponentFields.DIVISION));
                    child.setField(ProductStructureTreeNodeFields.TECHNOLOGY_GROUP, technologyGroup);
                    child.setField(ProductStructureTreeNodeFields.STANDARD_PERFORMANCE, standardPerformance);

                    child = addChild(tree, child, parent, L_COMPONENT);

                    generateTreeForSubProducts(operationForTechnology, subTechnology, tree, child, view, mainTechnology);
                } else {
                    child.setField(ProductStructureTreeNodeFields.TECHNOLOGY, technology);
                    child.setField(ProductStructureTreeNodeFields.MAIN_TECHNOLOGY, mainTechnology);
                    child.setField(ProductStructureTreeNodeFields.PRODUCT, product);
                    child.setField(ProductStructureTreeNodeFields.QUANTITY, quantity);
                    child.setField(ProductStructureTreeNodeFields.OPERATION, subOperation);
                    child.setField(ProductStructureTreeNodeFields.DIVISION,
                            subOperation.getBelongsToField(TechnologyOperationComponentFields.DIVISION));

                    child = addChild(tree, child, parent, L_INTERMEDIATE);

                    if (Objects.nonNull(view)) {
                        FormComponent productStructureForm = (FormComponent) view.getComponentByReference("productStructureForm");

                        if (Objects.nonNull(productStructureForm)) {
                            productStructureForm.addMessage(
                                    "technologies.technologyDetails.window.productStructure.productStructureForm.technologyAndOperationExists",
                                    MessageType.INFO, false, product.getStringField(ProductFields.NUMBER) + " "
                                            + product.getStringField(ProductFields.NAME));
                        }
                    }

                    generateTreeForSubProducts(subOperation, technology, tree, child, view, mainTechnology);
                }
            } else {
                Entity technologyGroup = technology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP);
                BigDecimal standardPerformance = technologyService.getStandardPerformance(technology).orElse(null);

                child.setField(ProductStructureTreeNodeFields.TECHNOLOGY, technology);
                child.setField(ProductStructureTreeNodeFields.MAIN_TECHNOLOGY, mainTechnology);
                child.setField(ProductStructureTreeNodeFields.PRODUCT, product);
                child.setField(ProductStructureTreeNodeFields.QUANTITY, quantity);
                child.setField(ProductStructureTreeNodeFields.TECHNOLOGY_GROUP, technologyGroup);
                child.setField(ProductStructureTreeNodeFields.STANDARD_PERFORMANCE, standardPerformance);
                child.setField(ProductStructureTreeNodeFields.UNIT, unit);

                if (Objects.nonNull(subOperation)) {
                    child.setField(ProductStructureTreeNodeFields.OPERATION, subOperation);
                    child.setField(ProductStructureTreeNodeFields.DIVISION,
                            subOperation.getBelongsToField(TechnologyOperationComponentFields.DIVISION));

                    child = addChild(tree, child, parent, L_INTERMEDIATE);

                    generateTreeForSubProducts(subOperation, technology, tree, child, view, mainTechnology);
                } else {
                    boolean differentProductsInDifferentSizes = operationProductInComponent
                            .getBooleanField(OperationProductInComponentFields.DIFFERENT_PRODUCTS_IN_DIFFERENT_SIZES);
                    boolean variousQuantitiesInProductsBySize = operationProductInComponent
                            .getBooleanField(OperationProductInComponentFields.VARIOUS_QUANTITIES_IN_PRODUCTS_BY_SIZE);

                    child.setField(ProductStructureTreeNodeFields.TECHNOLOGY_INPUT_PRODUCT_TYPE, technologyInputProductType);
                    child.setField(ProductStructureTreeNodeFields.DIFFERENT_PRODUCTS_IN_DIFFERENT_SIZES,
                            differentProductsInDifferentSizes);
                    child.setField(ProductStructureTreeNodeFields.VARIOUS_QUANTITIES_IN_PRODUCTS_BY_SIZE,
                            variousQuantitiesInProductsBySize);
                    child.setField(ProductStructureTreeNodeFields.OPERATION, operation);
                    child.setField(ProductStructureTreeNodeFields.DIVISION,
                            operation.getBelongsToField(TechnologyOperationComponentFields.DIVISION));

                    child = addChild(tree, child, parent, L_MATERIAL);

                    if (differentProductsInDifferentSizes) {
                        generateTreeForProductBySizeGroups(operationProductInComponent, operation,
                                variousQuantitiesInProductsBySize, technology, tree, child, view, mainTechnology);
                    }
                }
            }
        }
    }

    private void generateTreeForProductBySizeGroups(final Entity operationProductInComponent, final Entity operation,
            final boolean variousQuantitiesInProductsBySize, final Entity technology, final List<Entity> tree,
            final Entity parent, final ViewDefinitionState view, final Entity mainTechnology) {
        List<Entity> productBySizeGroups = operationProductInComponent
                .getHasManyField(OperationProductInComponentFields.PRODUCT_BY_SIZE_GROUPS);

        for (Entity productBySizeGroup : productBySizeGroups) {
            Entity child = getProductStructureTreeNodeDD().create();

            Entity product = productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.PRODUCT);
            Entity sizeGroup = productBySizeGroup.getBelongsToField(ProductBySizeGroupFields.SIZE_GROUP);
            BigDecimal quantity = productBySizeGroup.getDecimalField(ProductBySizeGroupFields.QUANTITY);
            String unit = product.getStringField(ProductFields.UNIT);

            child.setField(ProductStructureTreeNodeFields.TECHNOLOGY, technology);
            child.setField(ProductStructureTreeNodeFields.MAIN_TECHNOLOGY, mainTechnology);
            child.setField(ProductStructureTreeNodeFields.PRODUCT, product);
            child.setField(ProductStructureTreeNodeFields.SIZE_GROUP, sizeGroup);
            child.setField(ProductStructureTreeNodeFields.QUANTITY, quantity);
            child.setField(ProductStructureTreeNodeFields.UNIT, unit);
            child.setField(ProductStructureTreeNodeFields.OPERATION, operation);
            child.setField(ProductStructureTreeNodeFields.VARIOUS_QUANTITIES_IN_PRODUCTS_BY_SIZE,
                    variousQuantitiesInProductsBySize);

            addChild(tree, child, parent, L_PRODUCT_BY_SIZE_GROUP);
        }
    }

    public EntityTree getOperationComponentsFromTechnology(final Entity technology) {
        EntityTree productStructureTree = generateProductStructureTree(null, technology);

        return transformProductStructureTreeToTOCTree(productStructureTree);
    }

    private EntityTree transformProductStructureTreeToTOCTree(final EntityTree productStructureTree) {
        List<Entity> tocTree = Lists.newArrayList();

        DataDefinition tocDD = getTechnologyOperationComponentDD();

        Entity root = productStructureTree.getRoot();

        Long rootTocID = root.getBelongsToField(ProductStructureTreeNodeFields.OPERATION).getId();

        addChildTOC(tocTree, tocDD.get(rootTocID), null, root.getBelongsToField(ProductStructureTreeNodeFields.PRODUCT),
                L_FINAL_PRODUCT);

        addTocChildes(tocTree, tocDD, root);

        return EntityTreeUtilsService.getDetachedEntityTree(tocTree);
    }

    private void addTocChildes(final List<Entity> tocTree, final DataDefinition tocDD, final Entity root) {
        Entity parent;

        for (Entity node : root.getHasManyField(TechnologyOperationComponentFields.CHILDREN)) {
            String entityType = node.getStringField(ProductStructureTreeNodeFields.ENTITY_TYPE);

            if (!entityType.equals(L_MATERIAL) && !entityType.equals(L_FINAL_PRODUCT)
                    && !entityType.equals(L_PRODUCT_BY_SIZE_GROUP)) {
                Long tocId = node.getBelongsToField(ProductStructureTreeNodeFields.OPERATION).getId();
                Entity toc = tocDD.get(tocId);
                Long parentId = Objects.nonNull(node.getBelongsToField(ProductStructureTreeNodeFields.PARENT))
                        ? node.getBelongsToField(ProductStructureTreeNodeFields.PARENT)
                                .getBelongsToField(ProductStructureTreeNodeFields.OPERATION).getId()
                        : node.getBelongsToField(ProductStructureTreeNodeFields.OPERATION).getId();

                parent = getEntityById(tocTree, parentId);

                addChildTOC(tocTree, toc, parent, node.getBelongsToField(ProductStructureTreeNodeFields.PRODUCT), entityType);
            }

            addTocChildes(tocTree, tocDD, node);
        }
    }

    private Entity getEntityById(final List<Entity> tree, final Long id) {
        for (Entity entity : tree) {
            if (entity.getId().equals(id)) {
                return entity;
            }
        }

        return null;
    }

    private void addChildTOC(final List<Entity> tree, final Entity child, final Entity parent, final Entity product,
            final String type) {
        child.setField(TechnologyOperationComponentFields.PARENT, parent);
        child.setField(TechnologyOperationComponentFields.PRIORITY, 1);
        child.setField(TechnologyOperationComponentFields.TYPE_FROM_STRUCTURE_TREE, type);
        child.setField(TechnologyOperationComponentFields.PRODUCT_FROM_STRUCTURE_TREE, product);

        if (Objects.nonNull(parent)) {
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
    }

    public Entity getLastTechnologyStateChange(final Entity technology) {
        return technology.getHasManyField(TechnologyFields.STATE_CHANGES).find()
                .add(SearchRestrictions.eq(TechnologyStateChangeFields.STATUS, StateChangeStatus.SUCCESSFUL.getStringValue()))
                .addOrder(SearchOrders.desc(TechnologyStateChangeFields.DATE_AND_TIME)).setMaxResults(1).uniqueResult();
    }

    public Entity getTechnologyAcceptStateChange(final Entity technology) {
        return technology.getHasManyField(TechnologyFields.STATE_CHANGES).find()
                .add(SearchRestrictions.eq(TechnologyStateChangeFields.TARGET_STATE, TechnologyStateStringValues.ACCEPTED))
                .add(SearchRestrictions.eq(TechnologyStateChangeFields.STATUS, StateChangeStatus.SUCCESSFUL.getStringValue()))
                .addOrder(SearchOrders.desc(TechnologyStateChangeFields.DATE_AND_TIME)).setMaxResults(1).uniqueResult();
    }

    public Entity getTechnologyOutdatedStateChange(final Entity technology) {
        return technology.getHasManyField(TechnologyFields.STATE_CHANGES).find()
                .add(SearchRestrictions.eq(TechnologyStateChangeFields.TARGET_STATE, TechnologyStateStringValues.OUTDATED))
                .add(SearchRestrictions.eq(TechnologyStateChangeFields.STATUS, StateChangeStatus.SUCCESSFUL.getStringValue()))
                .addOrder(SearchOrders.desc(TechnologyStateChangeFields.DATE_AND_TIME)).setMaxResults(1).uniqueResult();
    }

    private DataDefinition getTechnologyDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);
    }

    private DataDefinition getOperationProductOutComponentDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT);
    }

    private DataDefinition getTechnologyOperationComponentDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
    }

    private DataDefinition getProductStructureTreeNodeDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_PRODUCT_STRUCTURE_TREE_NODE);
    }

}
