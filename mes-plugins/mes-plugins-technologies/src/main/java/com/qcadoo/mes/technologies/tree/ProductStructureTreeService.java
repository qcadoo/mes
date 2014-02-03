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
import com.qcadoo.view.api.ViewDefinitionState;

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

    private void addChild(final List<Entity> tree, final Entity child, final Entity parent) {
        child.setField("parent", parent);
        child.setId((long) tree.size() + 1);
        child.setField(L_NUMBER, child.getId());
        child.setField("priority", 1);
        child.setField("entityType", "productStructureTreeNode");

        tree.add(child);
    }

    private Entity findOperationForProductAndTechnology(final Entity product, final Entity technology) {
        DataDefinition operationComponentsDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);
        List<Entity> operations = operationComponentsDD.find().add(SearchRestrictions.belongsTo(L_TECHNOLOGY, technology)).list()
                .getEntities();
        for (Entity operation : operations) {
            EntityList outProducts = operation.getHasManyField(L_OPERATION_PRODUCT_OUT_COMPONENTS);
            for (Entity productComponent : outProducts) {
                if (productComponent.getBelongsToField(L_PRODUCT).equals(product)) {
                    return operation;
                }
            }
        }
        return null;
    }

    private Entity findTechnologyForProduct(final Entity product) {
        DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);
        List<Entity> technologiesForProduct = technologyDD.find().add(SearchRestrictions.belongsTo(L_PRODUCT, product)).list()
                .getEntities();
        Entity result = null;
        for (Entity technology : technologiesForProduct) {
            boolean isMaster = technology.getBooleanField("master");
            if (isMaster) {
                return technology;
            } else {
                if (result != null) {
                    if (result.getStringField(L_NUMBER).compareTo(technology.getStringField(L_NUMBER)) > 0) {
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
        for (Entity productComponent : outProducts) {
            if (productComponent.getBelongsToField(L_PRODUCT).equals(product)) {
                return productComponent.getDecimalField(L_QUANTITY);
            }
        }
        EntityList inProducts = operation.getHasManyField(L_OPERATION_PRODUCT_IN_COMPONENTS);
        for (Entity productComponent : inProducts) {
            if (productComponent.getBelongsToField(L_PRODUCT).equals(product)) {
                return productComponent.getDecimalField(L_QUANTITY);
            }
        }
        return null;
    }

    private void generateTreeForSubproducts(final Entity operation, final Entity technology, final List<Entity> tree,
            final Entity parent) {
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
                Entity operationForTechnology = findOperationForProductAndTechnology(product, subTechnology);
                BigDecimal quantityForTechnology = findQuantityOfProductInOperation(product, operationForTechnology);

                child.setField(L_TECHNOLOGY, technology);
                child.setField(L_OPERATION, operationForTechnology);
                child.setField(L_PRODUCT, product);
                child.setField(L_QUANTITY, quantityForTechnology);
                addChild(tree, child, parent);

                generateTreeForSubproducts(operationForTechnology, subTechnology, tree, child);
            } else {
                child.setField(L_TECHNOLOGY, technology);
                child.setField(L_PRODUCT, product);
                child.setField(L_OPERATION, operation);
                child.setField(L_QUANTITY, quantity);
                addChild(tree, child, parent);
                if (subOperation != null) {
                    generateTreeForSubproducts(subOperation, technology, tree, child);
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
        addChild(productStructureList, root, null);
        generateTreeForSubproducts(operation, technology, productStructureList, root);
        EntityTree productStructureTree = EntityTreeUtilsService.getDetachedEntityTree(productStructureList);

        return productStructureTree;
    }
}
