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
package com.qcadoo.mes.productionCounting.hooks.helpers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
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

        return fromProduct(product, role);
    }

    public Entity fromOperationProductComponentHolder(final OperationProductComponentHolder operationProductComponentHolder) {
        String modelName = operationProductComponentHolder.getEntityType().getStringValue();
        ProductionCountingQuantityRole role = null;

        if (TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT.equals(modelName)) {
            role = ProductionCountingQuantityRole.USED;
        } else if (TechnologiesConstants.MODEL_OPERATION_PRODUCT_OUT_COMPONENT.equals(modelName)) {
            role = ProductionCountingQuantityRole.PRODUCED;
        } else {
            throw new IllegalArgumentException(String.format("Unsupported operation component type: %s", modelName));
        }

        Entity product = operationProductComponentHolder.getProduct();

        return fromProduct(product, role);
    }

    public Entity fromOperationProductComponent(final Entity operationProductComponent) {
        String modelName = operationProductComponent.getDataDefinition().getName();
        String productFieldName = null;
        ProductionCountingQuantityRole role = null;

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

        return fromProduct(product, role);
    }

    public Entity fromProduct(final Entity product, final ProductionCountingQuantityRole role) {
        String modelName = null;
        String productFieldName = null;

        if (role == ProductionCountingQuantityRole.PRODUCED) {
            modelName = ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT;
            productFieldName = TrackingOperationProductOutComponentFields.PRODUCT;
        } else if (role == ProductionCountingQuantityRole.USED) {
            modelName = ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT;
            productFieldName = TrackingOperationProductInComponentFields.PRODUCT;
        } else {
            throw new IllegalArgumentException(String.format("Unsupported product role: %s", role));
        }

        DataDefinition dataDefinition = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, modelName);
        Entity trackingOperationProductComponent = dataDefinition.create();
        trackingOperationProductComponent.setField(productFieldName, product);

        return trackingOperationProductComponent;
    }

}
