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
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class TrackingOperationComponentBuilder {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public List<Entity> build(final Iterable<Entity> productionCountingQuantities) {
        List<Entity> trackingOps = Lists.newArrayList();
        for (Entity pcQuantity : productionCountingQuantities) {
            trackingOps.add(fromPcQuantity(pcQuantity));
        }
        return trackingOps;
    }

    public Entity fromPcQuantity(final Entity productionCountingQuantity) {
        Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
        String roleString = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.ROLE);
        ProductionCountingQuantityRole role = ProductionCountingQuantityRole.parseString(roleString);
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
        DataDefinition dataDef = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, modelName);
        Entity trackingOperationComp = dataDef.create();
        trackingOperationComp.setField(productFieldName, product);
        return trackingOperationComp;
    }

}
