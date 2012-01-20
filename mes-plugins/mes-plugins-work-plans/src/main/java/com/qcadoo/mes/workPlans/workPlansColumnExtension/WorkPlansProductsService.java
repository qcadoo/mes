/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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

            calculateQuantitiesForNormalAlgorithm(tree, productQuantities, plannedQty, technology);
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
