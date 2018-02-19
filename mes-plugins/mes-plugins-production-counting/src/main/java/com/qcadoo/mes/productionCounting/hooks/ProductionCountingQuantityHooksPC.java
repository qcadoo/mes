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
package com.qcadoo.mes.productionCounting.hooks;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.GlobalTypeOfMaterial;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.productionCounting.SetTechnologyInComponentsService;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingQuantityFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingQuantitySetComponentFields;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingState;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductionCountingQuantityHooksPC {

    private static final String L_TRACKING_OPERATION_IN_QUANTITY_QUERY = "SELECT '' AS nullResultProtector, t.usedQuantity AS usedQuantity FROM #productionCounting_productionTracking pt, #productionCounting_trackingOperationProductInComponent t WHERE t.productionTracking.id = pt.id AND pt.id = %s AND t.product.id = %s";

    @Autowired
    private SetTechnologyInComponentsService setTechnologyInComponentsService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean onDelete(final DataDefinition productionCountingQuantityDD, final Entity productionCountingQuantity) {
        Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);
        Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);

        List<Entity> productionTrackings = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_TRACKING).find()
                .add(SearchRestrictions.belongsTo(ProductionTrackingFields.ORDER, order)).list().getEntities();
        for (Entity tracking : productionTrackings) {
            String state = tracking.getStringField(ProductionTrackingFields.STATE);
            if (!ProductionTrackingState.DECLINED.getStringValue().equals(state)) {
                Entity trackingInComponent = getTrackingOperationProductInComponent(tracking, product);
                if (trackingInComponent != null
                        && trackingInComponent.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY) != null) {
                    productionCountingQuantity.addGlobalError("productionCounting.productionCountingQuantity.onDelete.error");
                    return false;
                }
            }
        }
        return true;
    }

    private Entity getTrackingOperationProductInComponent(final Entity productionTracking, final Entity product) {
        return productionTracking.getDataDefinition()
                .find(String.format(L_TRACKING_OPERATION_IN_QUANTITY_QUERY, productionTracking.getId(), product.getId()))
                .setMaxResults(1).uniqueResult();
    }

    public void onCreate(final DataDefinition productionCountingQuantityDD, final Entity productionCountingQuantity) {
        String typeOfMaterial = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);
        String role = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.ROLE);

        if (ProductionCountingQuantityRole.USED.getStringValue().equals(role)
                && ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue().equals(typeOfMaterial)) {

            Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
            if (product != null) {
                Optional<Entity> maybeTechnology = setTechnologyInComponentsService.getSetProductTechnology(product);
                maybeTechnology.ifPresent(entity -> generateProductionCountingQuantities(productionCountingQuantityDD,
                        productionCountingQuantity, entity));
            }
        }
    }

    public void onSave(final DataDefinition productionCountingQuantityDD, final Entity productionCountingQuantity) {
        recalculateProductionCountingQuantities(productionCountingQuantity);
    }

    private void generateProductionCountingQuantities(DataDefinition productionCountingQuantityDD,
            Entity productionCountingQuantity, Entity technology) {
        List<Entity> productionCountingQuantitySetComponents = new ArrayList<>();

        EntityTree operationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
        List<EntityTreeNode> children = operationComponents.getRoot().getChildren();

        Entity productOutComponent = operationComponents.getRoot()
                .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS).get(0);
        BigDecimal outQuantity = productOutComponent.getDecimalField(OperationProductOutComponentFields.QUANTITY);

        BigDecimal plannedQuantity = productionCountingQuantity
                .getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);

        for (Entity technologyOperationComponent : children) {
            Entity operationProductOutComponent = technologyOperationComponent
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS).get(0);

            Entity productFromComponent = operationProductOutComponent
                    .getBelongsToField(OperationProductOutComponentFields.PRODUCT);
            GlobalTypeOfMaterial productGlobalTypeOfMaterial = GlobalTypeOfMaterial
                    .parseString(productFromComponent.getStringField(ProductFields.GLOBAL_TYPE_OF_MATERIAL));

            if (productGlobalTypeOfMaterial == GlobalTypeOfMaterial.INTERMEDIATE) {
                Entity productionCountingQuantitySetComponent = getProductionCountingQuantitySetComponentsDD().create();

                Optional<Entity> operationProductInComponent = getInComponentFromParent(technologyOperationComponent,
                        productFromComponent);
                BigDecimal plannedQuantityFromProduct;
                if (operationProductInComponent.isPresent()) {
                    plannedQuantityFromProduct = operationProductInComponent.get()
                            .getDecimalField(OperationProductInComponentFields.QUANTITY);
                } else {
                    plannedQuantityFromProduct = operationProductOutComponent
                            .getDecimalField(OperationProductOutComponentFields.QUANTITY);
                }

                BigDecimal quantityFromSets = plannedQuantityFromProduct.multiply(plannedQuantity).divide(outQuantity,
                        RoundingMode.HALF_UP);
                productionCountingQuantitySetComponent.setField(ProductionCountingQuantitySetComponentFields.QUANTITY_FROM_SETS,
                        quantityFromSets);
                productionCountingQuantitySetComponent.setField(ProductionCountingQuantitySetComponentFields.PRODUCT,
                        productFromComponent);
                productionCountingQuantitySetComponent.setField(
                        ProductionCountingQuantitySetComponentFields.PRODUCTION_COUNTING_QUANTITY, productionCountingQuantity);
                productionCountingQuantitySetComponent.setField(
                        ProductionCountingQuantitySetComponentFields.PLANNED_QUANTITY_FROM_PRODUCT, plannedQuantityFromProduct);
                productionCountingQuantitySetComponent.setField(ProductionCountingQuantitySetComponentFields.OUT_QUANTITY,
                        outQuantity);

                productionCountingQuantitySetComponents.add(productionCountingQuantitySetComponent);
            }
        }
        productionCountingQuantity.setField(ProductionCountingQuantityFieldsPC.PRODUCTION_COUNTING_QUANTITY_SET_COMPONENTS,
                productionCountingQuantitySetComponents);
    }

    private Optional<Entity> getInComponentFromParent(final Entity toc, final Entity product) {
        Optional<Entity> opicForProduct = Optional.empty();
        Entity parent = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT)
                .get(toc.getId()).getBelongsToField(TechnologyOperationComponentFields.PARENT);
        if (parent != null) {
            List<Entity> opics = parent.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);
            opicForProduct = opics.stream().filter(
                    opic -> opic.getBelongsToField(OperationProductInComponentFields.PRODUCT).getId().equals(product.getId()))
                    .findFirst();
            if (opicForProduct.isPresent()) {
                return opicForProduct;
            }
        }
        return opicForProduct;
    }

    private void recalculateProductionCountingQuantities(Entity productionCountingQuantity) {
        List<Entity> productionCountingQuantitySetComponents = productionCountingQuantity
                .getHasManyField(ProductionCountingQuantityFieldsPC.PRODUCTION_COUNTING_QUANTITY_SET_COMPONENTS);

        if (productionCountingQuantitySetComponents == null) {
            return;
        }

        BigDecimal plannedQuantity = productionCountingQuantity
                .getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);

        for (Entity productionCountingQuantitySetComponent : productionCountingQuantitySetComponents) {

            BigDecimal plannedQuantityFromProduct = productionCountingQuantitySetComponent
                    .getDecimalField(ProductionCountingQuantitySetComponentFields.PLANNED_QUANTITY_FROM_PRODUCT);
            BigDecimal outQuantity = productionCountingQuantitySetComponent
                    .getDecimalField(ProductionCountingQuantitySetComponentFields.OUT_QUANTITY);

            if (plannedQuantityFromProduct != null && outQuantity != null) {
                BigDecimal quantityFromSets = plannedQuantityFromProduct.multiply(plannedQuantity).divide(outQuantity,
                        RoundingMode.HALF_UP);
                productionCountingQuantitySetComponent.setField(ProductionCountingQuantitySetComponentFields.QUANTITY_FROM_SETS,
                        quantityFromSets);

                productionCountingQuantitySetComponent.getDataDefinition().save(productionCountingQuantitySetComponent);
            }
        }
    }

    private DataDefinition getProductionCountingQuantitySetComponentsDD() {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY_SET_COMPONENT);
    }
}
