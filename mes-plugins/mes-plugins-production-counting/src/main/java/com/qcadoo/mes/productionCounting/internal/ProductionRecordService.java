package com.qcadoo.mes.productionCounting.internal;

import static com.google.common.collect.Lists.newArrayList;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_RECORD_OPERATION_PRODUCT_OUT_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_RECORDING_TYPE_CUMULATED;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_RECORDING_TYPE_FOREACH;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_RECORDING_TYPE_NONE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_REGISTER_IN_PRODUCTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_REGISTER_OUT_PRODUCTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PLUGIN_IDENTIFIER;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class ProductionRecordService {

    @Autowired
    DataDefinitionService dataDefinitionService;

    @Autowired
    NumberGeneratorService numberGeneratorService;

    @Autowired
    SecurityService securityService;

    public void generateData(final DataDefinition dd, final Entity entity) {
        entity.setField("number", numberGeneratorService.generateNumber(PLUGIN_IDENTIFIER, entity.getDataDefinition().getName()));
        entity.setField("creationTime", new Date());
        entity.setField("worker", securityService.getCurrentUserName());
    }

    public void checkTypeOfProductionRecording(final DataDefinition dd, final Entity entity) {
        Entity order = entity.getBelongsToField("order");
        String typeOfProductionRecording = order.getStringField("typeOfProductionRecording");
        if (typeOfProductionRecording == null || PARAM_RECORDING_TYPE_NONE.equals(typeOfProductionRecording)) {
            entity.addError(dd.getField("order"), "productionCounting.validate.global.error.productionRecord.orderError");
        }
    }

    public void allowedPartial(final DataDefinition dd, final Entity entity) {
        Entity order = entity.getBelongsToField("order");
        Boolean allowedPartial = getBooleanValue(order.getField("allowedPartial"));
        Boolean isFinal = getBooleanValue(order.getField("isFinal"));
        if (!isFinal && allowedPartial) {
            entity.addError(dd.getField("order"),
                    "productionCounting.validate.global.error.productionRecord.orderError.allowedPartial");
        }
    }

    public void checkFinal(final DataDefinition dd, final Entity entity) {

    }

    public Boolean checkIfOrderIsStarted(final DataDefinition dd, final Entity entity) {
        String orderState = entity.getBelongsToField("order").getStringField("state");
        if (orderState == null || "01new".equals(orderState) || "02accepted".equals(orderState)) {
            entity.addError(dd.getField("order"), "productionCounting.record.messages.error.orderIsNotStarted");
            return false;
        }
        return true;
    }
    
    public void copyProductInAndOut(final DataDefinition dd, final Entity productionRecord) {
        Entity order = productionRecord.getBelongsToField("order");
        String typeOfProductionRecording = order.getStringField("typeOfProductionRecording");
        List<Entity> operationComponents = null;

        Boolean registerInput = getBooleanValue(order.getField(PARAM_REGISTER_IN_PRODUCTS));
        Boolean registerOutput = getBooleanValue(order.getField(PARAM_REGISTER_OUT_PRODUCTS));
        
        if (!registerInput && !registerOutput) {
            return;
        }
        
        for (String fieldName : newArrayList("recordOperationProductInComponents", "recordOperationProductOutComponents")) {
            if (productionRecord.getHasManyField(fieldName) != null) {
                return;
            }
        }

        if (PARAM_RECORDING_TYPE_CUMULATED.equals(typeOfProductionRecording)) {
            operationComponents = order.getTreeField("orderOperationComponents");
        } else if (PARAM_RECORDING_TYPE_FOREACH.equals(typeOfProductionRecording)) {
            operationComponents = newArrayList(productionRecord.getBelongsToField("orderOperationComponent"));
        }

        if (registerInput) {
            copyOperationProductComponents(operationComponents, productionRecord, MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT);
        }
        if (registerOutput) {
            copyOperationProductComponents(operationComponents, productionRecord, MODEL_RECORD_OPERATION_PRODUCT_OUT_COMPONENT);
        }
    }

    // TODO products list should be distinct?
    private void copyOperationProductComponents(final List<Entity> orderOperations, final Entity productionRecord,
            final String modelName) {
        if (orderOperations == null || orderOperations.size() == 0) {
            return;
        }

        DataDefinition recordProductDD = dataDefinitionService.get(PLUGIN_IDENTIFIER, modelName);
        List<Entity> products = newArrayList();
        String technologyProductFieldName = "operationProductOutComponents";
        String recordProductFieldName = "recordOperationProductOutComponents";

        if (MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT.equals(modelName)) {
            technologyProductFieldName = "operationProductInComponents";
            recordProductFieldName = "recordOperationProductInComponents";
        }

        for (Entity orderOperation : orderOperations) {
            List<Entity> technologyProducts = orderOperation.getBelongsToField("technologyOperationComponent").getHasManyField(
                    technologyProductFieldName);
            if (technologyProducts == null) {
                continue;
            }

            for (Entity technologyProduct : technologyProducts) {
                Entity recordProduct = recordProductDD.create();
                recordProduct.setField("product", technologyProduct.getField("product"));
                recordProduct.setField("plannedQuantity", technologyProduct.getField("quantity"));
                products.add(recordProduct);
            }
        }
        productionRecord.setField(recordProductFieldName, products);
    }
    
    public static Boolean getBooleanValue(Object fieldValue) {
        return fieldValue != null && fieldValue instanceof Boolean && (Boolean) fieldValue; 
    }
}
