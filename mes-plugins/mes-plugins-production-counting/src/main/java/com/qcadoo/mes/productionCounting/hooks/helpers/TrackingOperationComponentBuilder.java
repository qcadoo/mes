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
package com.qcadoo.mes.productionCounting.hooks.helpers;

import java.util.List;
import java.util.Objects;

import com.qcadoo.model.api.EntityList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingAttributeValueFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.productionCounting.constants.ProdOutResourceAttrValFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class TrackingOperationComponentBuilder {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public List<Entity> build(final Iterable<Entity> productionCountingQuantities) {
        List<Entity> trackingOperationProductComponents = Lists.newArrayList();

        for (Entity productionCountingQuantity : productionCountingQuantities) {
            trackingOperationProductComponents.add(fromProductionCountingQuantity(productionCountingQuantity));
        }

        return trackingOperationProductComponents;
    }

    public Entity fromProductionCountingQuantity(final Entity productionCountingQuantity) {
        Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
        String roleString = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.ROLE);

        ProductionCountingQuantityRole role = ProductionCountingQuantityRole.parseString(roleString);

        String typeOfMaterial = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);
        return fromProduct(product, role, typeOfMaterial);
    }

    public Entity fromOperationProductComponentHolder(final OperationProductComponentHolder operationProductComponentHolder) {
        String modelName = operationProductComponentHolder.getEntityType().getStringValue();
        ProductionCountingQuantityRole role;

        String typeOfMaterial = operationProductComponentHolder.getProductMaterialType().getStringValue();
        if (TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT.equals(modelName)) {
            role = ProductionCountingQuantityRole.USED;
        } else if (TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT.equals(modelName)) {
            role = ProductionCountingQuantityRole.PRODUCED;
        } else {
            throw new IllegalArgumentException(String.format("Unsupported operation component type: %s", modelName));
        }

        Entity product = operationProductComponentHolder.getProduct();
        Entity operationProductComponent = fromProduct(product, role, typeOfMaterial);

        Long productionCountingQuantityId = operationProductComponentHolder.getProductionCountingQuantityId();
        if (Objects.nonNull(productionCountingQuantityId)) {
            Entity productionCountingQuantity = dataDefinitionService
                    .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                            BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY)
                    .get(productionCountingQuantityId);
            if (TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT.equals(modelName)) {
                Entity replacement = productionCountingQuantity
                        .getBelongsToField(ProductionCountingQuantityFields.REPLACEMENT_TO);
                if (Objects.nonNull(replacement)) {
                    operationProductComponent.setField(TrackingOperationProductInComponentFields.REPLACEMENT_TO,
                            replacement.getId());
                }

            } else {
                moveAttributesFromPCQ(operationProductComponent, productionCountingQuantity);
            }
        }
        return operationProductComponent;
    }

    private void moveAttributesFromPCQ(Entity operationProductComponent, Entity productionCountingQuantity) {
        List<Entity> productionCountingAttributeValues = productionCountingQuantity
                .getHasManyField(ProductionCountingQuantityFields.PRODUCTION_COUNTING_ATTRIBUTE_VALUES);
        List<Entity> prodOutResourceAttrVals = Lists.newArrayList();
        for (Entity productionCountingAttributeValue : productionCountingAttributeValues) {
            Entity prodOutResourceAttrVal = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                    ProductionCountingConstants.MODEL_PROD_OUT_RESOURCE_ATTR_VAL).create();
            prodOutResourceAttrVal.setField(ProdOutResourceAttrValFields.ATTRIBUTE,
                    productionCountingAttributeValue.getField(ProductionCountingAttributeValueFields.ATTRIBUTE));
            prodOutResourceAttrVal.setField(ProdOutResourceAttrValFields.ATTRIBUTE_VALUE,
                    productionCountingAttributeValue.getField(ProductionCountingAttributeValueFields.ATTRIBUTE_VALUE));
            prodOutResourceAttrVal.setField(ProdOutResourceAttrValFields.VALUE,
                    productionCountingAttributeValue.getField(ProductionCountingAttributeValueFields.VALUE));
            prodOutResourceAttrVals.add(prodOutResourceAttrVal);
        }
        operationProductComponent.setField(TrackingOperationProductOutComponentFields.PROD_OUT_RESOURCE_ATTR_VALS,
                prodOutResourceAttrVals);
    }

    public Entity fromOperationProductComponent(final Entity operationProductComponent) {
        String modelName = operationProductComponent.getDataDefinition().getName();
        String productFieldName;
        ProductionCountingQuantityRole role;

        if (TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT.equals(modelName)) {
            productFieldName = OperationProductInComponentFields.PRODUCT;
            role = ProductionCountingQuantityRole.USED;
        } else if (TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT.equals(modelName)) {
            productFieldName = OperationProductOutComponentFields.PRODUCT;
            role = ProductionCountingQuantityRole.PRODUCED;
        } else {
            throw new IllegalArgumentException(String.format("Unsupported operation component type: %s", modelName));
        }

        Entity product = operationProductComponent.getBelongsToField(productFieldName);

        return fromProduct(product, role, null);
    }

    public Entity fromProduct(final Entity product, final ProductionCountingQuantityRole role, final String typeOfMaterial) {
        String modelName;
        String productFieldName;

        String typeOfMaterialFieldName;
        if (role == ProductionCountingQuantityRole.PRODUCED) {
            modelName = ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT;
            productFieldName = TrackingOperationProductOutComponentFields.PRODUCT;
            typeOfMaterialFieldName = TrackingOperationProductOutComponentFields.TYPE_OF_MATERIAL;
        } else if (role == ProductionCountingQuantityRole.USED) {
            modelName = ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT;
            productFieldName = TrackingOperationProductInComponentFields.PRODUCT;
            typeOfMaterialFieldName = TrackingOperationProductInComponentFields.TYPE_OF_MATERIAL;
        } else {
            throw new IllegalArgumentException(String.format("Unsupported product role: %s", role));
        }

        DataDefinition dataDefinition = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, modelName);
        Entity trackingOperationProductComponent = dataDefinition.create();
        trackingOperationProductComponent.setField(productFieldName, product);
        trackingOperationProductComponent.setField(typeOfMaterialFieldName, typeOfMaterial);

        return trackingOperationProductComponent;
    }

}
