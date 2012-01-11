package com.qcadoo.mes.workPlans.print;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

@Service
public class WorkPlanProductsService {

    public Map<Entity, Map<Entity, BigDecimal>> getProductQuantities(List<Entity> orders) {
        Map<Entity, Map<Entity, BigDecimal>> productQuantitiesPerOperation = new HashMap<Entity, Map<Entity, BigDecimal>>();
        for (Entity order : orders) {
            BigDecimal plannedQty = (BigDecimal) order.getField("plannedQuantity");

            Entity technology = order.getBelongsToField("technology");

            if (technology == null) {
                continue;
            }

            EntityTree tree = technology.getTreeField("operationComponents");

            Map<Entity, List<Entity>> productInComponents = preloadProductInComponents(tree);
            Map<Entity, List<Entity>> productOutComponents = preloadProductOutComponents(tree);

            String algorithm = technology.getStringField("componentQuantityAlgorithm");

            if ("01perProductOut".equals(algorithm)) {
                alterTreeForDetailedAlgorithm(tree, productInComponents, productOutComponents, technology, plannedQty);
            } else if ("02perTechnology".equals(algorithm)) {
                alterTreeForSimpleAlgorithm(tree, productInComponents, productOutComponents, plannedQty);
            } else {
                throw new IllegalStateException(
                        "technology's componentQuantityAlgorithm isn't 01perProductOut nor 02perTechnology");
            }

            getQuantities(productQuantitiesPerOperation, tree, productInComponents, productOutComponents, plannedQty);
        }

        return productQuantitiesPerOperation;
    }

    void getQuantities(Map<Entity, Map<Entity, BigDecimal>> productQuantitiesPerOperation, EntityTree tree,
            Map<Entity, List<Entity>> productInComponents, Map<Entity, List<Entity>> productOutComponents, BigDecimal plannedQty) {
        for (Entity operationComponent : tree) {
            Map<Entity, BigDecimal> productQuantities = new HashMap<Entity, BigDecimal>();

            for (Entity productComponent : productInComponents.get(operationComponent)) {
                BigDecimal neededQty = (BigDecimal) productComponent.getField("quantity");
                productQuantities.put(productComponent, neededQty);
            }

            for (Entity productComponent : productOutComponents.get(operationComponent)) {
                BigDecimal neededQty = (BigDecimal) productComponent.getField("quantity");
                productQuantities.put(productComponent, neededQty);
            }

            productQuantitiesPerOperation.put(operationComponent, productQuantities);
        }
    }

    Map<Entity, List<Entity>> preloadProductInComponents(EntityTree tree) {
        Map<Entity, List<Entity>> productComponents = new HashMap<Entity, List<Entity>>();

        for (Entity operationComponent : tree) {
            productComponents.put(operationComponent, operationComponent.getHasManyField("operationProductInComponents"));
        }

        return productComponents;
    }

    Map<Entity, List<Entity>> preloadProductOutComponents(EntityTree tree) {
        Map<Entity, List<Entity>> productComponents = new HashMap<Entity, List<Entity>>();

        for (Entity operationComponent : tree) {
            productComponents.put(operationComponent, operationComponent.getHasManyField("operationProductOutComponents"));
        }

        return productComponents;
    }

    void alterTreeForSimpleAlgorithm(EntityTree tree, Map<Entity, List<Entity>> productInComponents,
            Map<Entity, List<Entity>> productOutComponents, BigDecimal plannedQty) {
        for (Entity operationComponent : tree) {
            for (Entity productComponent : productInComponents.get(operationComponent)) {
                BigDecimal neededQty = (BigDecimal) productComponent.getField("quantity");
                productComponent.setField("quantity", neededQty.multiply(plannedQty));
            }

            for (Entity productComponent : productOutComponents.get(operationComponent)) {
                BigDecimal neededQty = (BigDecimal) productComponent.getField("quantity");
                productComponent.setField("quantity", neededQty.multiply(plannedQty));
            }
        }
    }

    void alterTreeForDetailedAlgorithm(EntityTree tree, Map<Entity, List<Entity>> productInComponents,
            Map<Entity, List<Entity>> productOutComponents, Entity technology, BigDecimal plannedQty) {
        Entity root = tree.getRoot();
        traverse(root, null, productInComponents, productOutComponents, technology, plannedQty);
    }

    void traverse(Entity operationComponent, Entity previousOperationComponent,
            Map<Entity, List<Entity>> preloadedProductInComponents, Map<Entity, List<Entity>> preloadedProductOutComponents,
            Entity technology, BigDecimal plannedQty) {
        if (previousOperationComponent == null) {
            Entity outProduct = technology.getBelongsToField("product");

            for (Entity out : preloadedProductOutComponents.get(operationComponent)) {
                if (out.getBelongsToField("product").getId().equals(outProduct.getId())) {
                    BigDecimal outQuantity = (BigDecimal) out.getField("quantity");

                    BigDecimal multiplier = plannedQty.divide(outQuantity);

                    out.setField("quantity", outQuantity.multiply(multiplier));

                    for (Entity currentIn : preloadedProductInComponents.get(operationComponent)) {
                        BigDecimal currentInQuantity = (BigDecimal) currentIn.getField("quantity");
                        currentIn.setField("quantity", currentInQuantity.multiply(multiplier));
                    }

                    break;
                }
            }
        } else {
            for (Entity out : preloadedProductOutComponents.get(operationComponent)) {
                for (Entity in : preloadedProductInComponents.get(previousOperationComponent)) {
                    if (out.getBelongsToField("product").getId().equals(in.getBelongsToField("product").getId())) {
                        BigDecimal outQuantity = (BigDecimal) out.getField("quantity");
                        BigDecimal inQuantity = (BigDecimal) in.getField("quantity");

                        BigDecimal multiplier = inQuantity.divide(outQuantity);

                        out.setField("quantity", outQuantity.multiply(multiplier));

                        for (Entity currentIn : preloadedProductInComponents.get(operationComponent)) {
                            BigDecimal currentInQuantity = (BigDecimal) currentIn.getField("quantity");
                            currentIn.setField("quantity", currentInQuantity.multiply(multiplier));
                        }

                        break;
                    }
                }
            }
        }

        for (Entity child : operationComponent.getHasManyField("children")) {
            traverse(child, operationComponent, preloadedProductInComponents, preloadedProductOutComponents, technology,
                    plannedQty);
        }
    }
}
