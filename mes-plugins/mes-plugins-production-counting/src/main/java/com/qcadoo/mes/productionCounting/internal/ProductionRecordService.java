package com.qcadoo.mes.productionCounting.internal;

import static com.google.common.collect.Lists.newArrayList;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_PRODUCTION_RECORD;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_RECORD_OPERATION_PRODUCT_OUT_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_RECORDING_TYPE_CUMULATED;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_RECORDING_TYPE_FOREACH;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_RECORDING_TYPE_NONE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_REGISTER_IN_PRODUCTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_REGISTER_OUT_PRODUCTS;
import static java.math.BigDecimal.ROUND_UP;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
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

    private final static String CUMULATE = "02cumulated";

    private final static String FOR_EACH_OPERATION = "03forEach";

    public void generateData(final DataDefinition dd, final Entity entity) {
        if (entity.getField("number") == null) {
            entity.setField("number", numberGeneratorService.generateNumber(ProductionCountingConstants.PLUGIN_IDENTIFIER, entity
                    .getDataDefinition().getName()));
        }
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
        Boolean isFinal = getBooleanValue(entity.getField("isFinal"));
        if (!isFinal && !allowedPartial) {
            entity.addError(dd.getField("order"),
                    "productionCounting.validate.global.error.productionRecord.orderError.allowedPartial");
        }
    }

    public void checkExistsFinalProductionRecording(final DataDefinition dd, final Entity entity) {
        if (entity.getId() != null) {
            return;
        }
        Entity order = entity.getBelongsToField("order");
        String typeOfProductionRecording = order.getStringField("typeOfProductionRecording");

        if (CUMULATE.equals(typeOfProductionRecording)) {
            List<Entity> productionRecordings = dataDefinitionService
                    .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, MODEL_PRODUCTION_RECORD).find()
                    .add(SearchRestrictions.belongsTo("order", order)).add(SearchRestrictions.eq("isFinal", true)).list()
                    .getEntities();
            if (productionRecordings.size() == 0) {
                entity.addError(dd.getField("order"), "productionCounting.record.messages.error.final");
                return;
            }
        } else if (FOR_EACH_OPERATION.equals(typeOfProductionRecording)) {
            Entity operation = entity.getBelongsToField("orderOperationComponent");
            List<Entity> productionRecordings = dataDefinitionService
                    .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, MODEL_PRODUCTION_RECORD).find()
                    .add(SearchRestrictions.belongsTo("order", order))
                    .add(SearchRestrictions.belongsTo("orderOperationComponent", operation))
                    .add(SearchRestrictions.eq("isFinal", true)).list().getEntities();
            if (productionRecordings.size() != 0) {
                entity.addError(dd.getField("order"), "productionCounting.record.messages.error.finalExists");
                return;
            }
        }
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

    private void copyOperationProductComponents(final List<Entity> orderOperations, final Entity productionRecord,
            final String modelName) {
        if (orderOperations == null || orderOperations.size() == 0) {
            return;
        }

        DataDefinition recordProductDD = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, modelName);
        List<Entity> products = newArrayList();
        Map<Entity, BigDecimal> productQuantityMap = Maps.newHashMap();
        String technologyProductFieldName = "operationProductOutComponents";
        String recordProductFieldName = "recordOperationProductOutComponents";

        if (ProductionCountingConstants.MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT.equals(modelName)) {
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
                BigDecimal plannedQuantity = getBigDecimal(technologyProduct.getField("quantity"));
                if (productQuantityMap.containsKey(technologyProduct)) {
                    productQuantityMap.put(technologyProduct, productQuantityMap.get(technologyProduct).add(plannedQuantity));
                } else {
                    productQuantityMap.put(technologyProduct, plannedQuantity);
                }
            }
        }

        for (Map.Entry<Entity, BigDecimal> technologyEntry : productQuantityMap.entrySet()) {
            Entity recordProduct = recordProductDD.create();
            recordProduct.setField("product", technologyEntry.getKey().getField("product"));
            recordProduct.setField("plannedQuantity", technologyEntry.getValue());
            products.add(recordProduct);
        }

        productionRecord.setField(recordProductFieldName, products);
    }

    public void countPlannedTime(final DataDefinition dataDefinition, final Entity productionCounting) {
        Entity order = productionCounting.getBelongsToField("order");
        String typeOfProductionRecording = order.getStringField("typeOfProductionRecording");
        if (CUMULATE.equals(typeOfProductionRecording)) {
            List<Entity> operationComponents = order.getTreeField("orderOperationComponents");
            countPlannedTimeForCumulated(productionCounting, operationComponents);

        } else if (FOR_EACH_OPERATION.equals(typeOfProductionRecording)) {
            Entity orderOperationComponent = productionCounting.getBelongsToField("orderOperationComponent");
            countPlannedTimeForEachOperation(productionCounting, orderOperationComponent);
        }
    }

    private void countPlannedTimeForEachOperation(final Entity productionCounting, final Entity orderOperationComponent) {
        BigDecimal tpz = getBigDecimal(orderOperationComponent.getField("tpz"));
        BigDecimal tj = getBigDecimal(orderOperationComponent.getField("tj"));
        BigDecimal productionInOneCycle = getBigDecimal(orderOperationComponent.getField("productionInOneCycle"));
        BigDecimal machineUtilization = getBigDecimal(orderOperationComponent.getField("machineUtilization"));
        BigDecimal laborUtilization = getBigDecimal(orderOperationComponent.getField("laborUtilization"));
        BigDecimal plannedTime = (tj.multiply(productionInOneCycle)).add(tpz);
        BigDecimal plannedMachineTime = plannedTime.multiply(machineUtilization);
        BigDecimal plannedLaborTime = plannedTime.multiply(laborUtilization);

        productionCounting.setField("plannedTime", plannedTime.setScale(0, ROUND_UP).intValue());
        productionCounting.setField("plannedMachineTime", plannedMachineTime.setScale(0, ROUND_UP).intValue());
        productionCounting.setField("plannedLaborTime", plannedLaborTime.setScale(0, ROUND_UP).intValue());
    }

    private void countPlannedTimeForCumulated(final Entity productionCounting, final List<Entity> orderOperationComponents) {
        BigDecimal plannedTime = BigDecimal.ZERO;
        BigDecimal plannedMachineTime = BigDecimal.ZERO;
        BigDecimal plannedLaborTime = BigDecimal.ZERO;
        for (Entity orderOperationComponent : orderOperationComponents) {
            BigDecimal tpz = getBigDecimal(orderOperationComponent.getField("tpz"));
            BigDecimal tj = getBigDecimal(orderOperationComponent.getField("tj"));
            BigDecimal productionInOneCycle = getBigDecimal(orderOperationComponent.getField("productionInOneCycle"));
            BigDecimal machineUtilization = getBigDecimal(orderOperationComponent.getField("machineUtilization"));
            BigDecimal laborUtilization = getBigDecimal(orderOperationComponent.getField("laborUtilization"));
            plannedTime = (tj.multiply(productionInOneCycle)).add(tpz);
            plannedMachineTime = plannedTime.multiply(machineUtilization);
            plannedLaborTime = plannedTime.multiply(laborUtilization);
        }
        productionCounting.setField("plannedTime", plannedTime.setScale(0, ROUND_UP).intValue());
        productionCounting.setField("plannedMachineTime", plannedMachineTime.setScale(0, ROUND_UP).intValue());
        productionCounting.setField("plannedLaborTime", plannedLaborTime.setScale(0, ROUND_UP).intValue());
    }

    public static BigDecimal getBigDecimal(final Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return BigDecimal.valueOf(Double.valueOf(value.toString()));
    }

    public static Boolean getBooleanValue(Object fieldValue) {
        return fieldValue != null && fieldValue instanceof Boolean && (Boolean) fieldValue;
    }
}
