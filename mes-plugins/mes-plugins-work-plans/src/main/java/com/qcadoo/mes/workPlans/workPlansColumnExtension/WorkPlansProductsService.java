package com.qcadoo.mes.workPlans.workPlansColumnExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

@Service
public class WorkPlansProductsService {

    public Map<Entity, BigDecimal> getProductQuantities(List<Entity> orders) {
        Map<Entity, BigDecimal> productQuantities = new HashMap<Entity, BigDecimal>();

        for (Entity order : orders) {
            BigDecimal plannedQty = (BigDecimal) order.getField("plannedQuantity");

            Entity technology = order.getBelongsToField("technology");

            if (technology == null) {
                throw new IllegalStateException("Order doesn't contain technology.");
            }

            EntityTree tree = technology.getTreeField("operationComponents");

            String algorithm = technology.getStringField("componentQuantityAlgorithm");

            if ("01perProductOut".equals(algorithm)) {
                calculateQuantitiesForNormalAlgorithm(tree, productQuantities, plannedQty, technology);
            } else if ("02perTechnology".equals(algorithm)) {
                calculateQuantitiesForSimpleAlgorithm(tree, productQuantities, plannedQty);
            } else {
                throw new IllegalStateException(
                        "technology's componentQuantityAlgorithm isn't 01perProductOut nor 02perTechnology");
            }
        }

        return productQuantities;
    }

    void preloadProductQuantities(EntityTree tree, Map<Entity, BigDecimal> productQuantities, BigDecimal plannedQty) {
        for (Entity operationComponent : tree) {
            for (Entity productComponent : operationComponent.getHasManyField("operationProductInComponents")) {
                BigDecimal neededQty = (BigDecimal) productComponent.getField("quantity");
                productQuantities.put(productComponent, neededQty);
            }

            for (Entity productComponent : operationComponent.getHasManyField("operationProductOutComponents")) {
                BigDecimal neededQty = (BigDecimal) productComponent.getField("quantity");
                productQuantities.put(productComponent, neededQty);
            }
        }
    }

    void calculateQuantitiesForSimpleAlgorithm(EntityTree tree, Map<Entity, BigDecimal> productQuantities, BigDecimal plannedQty) {
        for (Entity operationComponent : tree) {
            for (Entity productComponent : operationComponent.getHasManyField("operationProductInComponents")) {
                BigDecimal neededQty = (BigDecimal) productComponent.getField("quantity");
                productQuantities.put(productComponent, neededQty.multiply(plannedQty));
            }

            for (Entity productComponent : operationComponent.getHasManyField("operationProductOutComponents")) {
                BigDecimal neededQty = (BigDecimal) productComponent.getField("quantity");
                productQuantities.put(productComponent, neededQty.multiply(plannedQty));
            }
        }
    }

    void calculateQuantitiesForNormalAlgorithm(EntityTree tree, Map<Entity, BigDecimal> productQuantities, BigDecimal plannedQty,
            Entity technology) {
        preloadProductQuantities(tree, productQuantities, plannedQty);

        Entity root = tree.getRoot();
        traverse(root, null, productQuantities, plannedQty, technology);
    }

    void traverse(Entity operationComponent, Entity previousOperationComponent, Map<Entity, BigDecimal> productQuantities,
            BigDecimal plannedQty, Entity technology) {
        if (previousOperationComponent == null) {
            Entity outProduct = technology.getBelongsToField("product");

            for (Entity out : operationComponent.getHasManyField("operationProductOutComponents")) {
                if (out.getBelongsToField("product").getId().equals(outProduct.getId())) {
                    BigDecimal outQuantity = productQuantities.get(out);

                    BigDecimal multiplier = plannedQty.divide(outQuantity);

                    productQuantities.put(out, outQuantity.multiply(multiplier));

                    for (Entity currentIn : operationComponent.getHasManyField("operationProductInComponents")) {
                        BigDecimal currentInQuantity = productQuantities.get(currentIn);
                        productQuantities.put(currentIn, currentInQuantity.multiply(multiplier));
                    }

                    break;
                }
            }
        } else {
            for (Entity out : operationComponent.getHasManyField("operationProductOutComponents")) {
                for (Entity in : previousOperationComponent.getHasManyField("operationProductInComponents")) {
                    if (out.getBelongsToField("product").getId().equals(in.getBelongsToField("product").getId())) {
                        BigDecimal outQuantity = productQuantities.get(out);
                        BigDecimal inQuantity = productQuantities.get(in);

                        BigDecimal multiplier = inQuantity.divide(outQuantity);

                        productQuantities.put(out, outQuantity.multiply(multiplier));

                        for (Entity currentIn : operationComponent.getHasManyField("operationProductInComponents")) {
                            BigDecimal currentInQuantity = productQuantities.get(currentIn);
                            productQuantities.put(currentIn, currentInQuantity.multiply(multiplier));
                        }

                        break;
                    }
                }
            }
        }

        for (Entity child : operationComponent.getHasManyField("children")) {
            traverse(child, operationComponent, productQuantities, plannedQty, technology);
        }
    }
}
