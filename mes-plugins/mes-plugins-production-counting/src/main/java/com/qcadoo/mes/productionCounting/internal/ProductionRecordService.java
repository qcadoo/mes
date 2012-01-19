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
package com.qcadoo.mes.productionCounting.internal;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_RECORD_OPERATION_PRODUCT_OUT_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_RECORDING_TYPE_BASIC;
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

import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStates;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class ProductionRecordService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    private static final String FIELD_ORDER = "order";

    private static final String FIELD_ORDER_OPERATION_COMPONENT = "orderOperationComponent";

    private static final String FIELD_TYPE_OF_PRODUCTION_RCORDING = "typeOfProductionRecording";

    @Autowired
    private SecurityService securityService;

    public void generateData(final DataDefinition dataDefinition, final Entity entity) {
        if (entity.getField("number") == null) {
            entity.setField("number", numberGeneratorService.generateNumber(ProductionCountingConstants.PLUGIN_IDENTIFIER, entity
                    .getDataDefinition().getName()));
        }
        entity.setField("creationTime", new Date());
        entity.setField("worker", securityService.getCurrentUserName());
    }

    public boolean checkTypeOfProductionRecording(final DataDefinition dataDefinition, final Entity entity) {
        final Entity order = entity.getBelongsToField(FIELD_ORDER);
        final String typeOfProductionRecording = order.getStringField(FIELD_TYPE_OF_PRODUCTION_RCORDING);
        return isValidTypeOfProductionRecording(entity, typeOfProductionRecording, dataDefinition);
    }

    public boolean isValidTypeOfProductionRecording(Entity entity, String typeOfProductionRecording, DataDefinition dd) {
        boolean validTypeOfRecording = true;
        if (typeOfProductionRecording == null || PARAM_RECORDING_TYPE_NONE.equals(typeOfProductionRecording)) {
            entity.addError(dd.getField(FIELD_ORDER), "productionCounting.validate.global.error.productionRecord.orderError");
            validTypeOfRecording = false;
        }
        if (PARAM_RECORDING_TYPE_BASIC.equals(typeOfProductionRecording)) {
            entity.addError(dd.getField(FIELD_ORDER),
                    "productionRecord.productionRecord.report.error.orderWithBasicProductionCounting");
            validTypeOfRecording = false;
        }
        return validTypeOfRecording;
    }

    // checkIfJustOneChoosen
    public boolean checkIfPartialIsAllowed(final DataDefinition dd, final Entity entity) {
        boolean isAllowed = true;
        final Entity order = entity.getBelongsToField(FIELD_ORDER);
        final Boolean justOne = getBooleanValue(order.getField("justOne"));
        final Boolean lastRecord = getBooleanValue(entity.getField("lastRecord"));
        if (!lastRecord && justOne) {
            entity.addError(dd.getField(FIELD_ORDER),
                    "productionCounting.validate.global.error.productionRecord.orderError.justOne");
            isAllowed = false;
        }
        return isAllowed;
    }

    public boolean checkIfExistsFinalRecord(final DataDefinition dataDefinition, final Entity entity) {
        boolean finalExist = true;
        if (entity.getId() == null) {

            final Entity order = entity.getBelongsToField(FIELD_ORDER);
            final String typeOfProductionRecording = order.getStringField(FIELD_TYPE_OF_PRODUCTION_RCORDING);

            final SearchCriteriaBuilder searchBuilder = dataDefinition.find();
            searchBuilder.add(SearchRestrictions.eq("state", ProductionCountingStates.ACCEPTED.getStringValue()));
            searchBuilder.add(SearchRestrictions.belongsTo(FIELD_ORDER, order));
            searchBuilder.add(SearchRestrictions.eq("lastRecord", true));

            if (PARAM_RECORDING_TYPE_FOREACH.equals(typeOfProductionRecording)) {
                searchBuilder.add(SearchRestrictions.belongsTo(FIELD_ORDER_OPERATION_COMPONENT,
                        entity.getBelongsToField("orderOperationComponents")));
            }
            if (searchBuilder.list().getTotalNumberOfEntities() != 0) {
                entity.addError(dataDefinition.getField(FIELD_ORDER), "productionCounting.record.messages.error.finalExists");
                finalExist = false;
            }
        }
        return finalExist;
    }

    public boolean checkIfOrderIsStarted(final DataDefinition dd, final Entity entity) {
        boolean isStarted = true;
        final String orderState = entity.getBelongsToField(FIELD_ORDER).getStringField("state");
        if (orderState == null || "01pending".equals(orderState) || "02accepted".equals(orderState)
                || "05declined".equals(orderState) || "07abandoned".equals(orderState)) {
            entity.addError(dd.getField(FIELD_ORDER), "productionCounting.record.messages.error.orderIsNotStarted");
            isStarted = false;
        }
        return isStarted;
    }

    public void copyProductsFromOrderOperation(final DataDefinition dd, final Entity productionRecord) {
        Entity order = productionRecord.getBelongsToField(FIELD_ORDER);
        if (order == null) {
            return;
        }
        String typeOfProductionRecording = order.getStringField(FIELD_TYPE_OF_PRODUCTION_RCORDING);
        if (typeOfProductionRecording == null) {
            return;
        }
        List<Entity> operationComponents = null;

        Boolean registerInput = getBooleanValue(order.getField(PARAM_REGISTER_IN_PRODUCTS));
        Boolean registerOutput = getBooleanValue(order.getField(PARAM_REGISTER_OUT_PRODUCTS));

        BigDecimal orderQuantity = getBigDecimal(order.getField("plannedQuantity"));

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
            operationComponents = newArrayList(productionRecord.getBelongsToField(FIELD_ORDER_OPERATION_COMPONENT));
        }

        if (registerInput) {
            copyOperationProductComponents(operationComponents, productionRecord, MODEL_RECORD_OPERATION_PRODUCT_IN_COMPONENT,
                    orderQuantity);
        }
        if (registerOutput) {
            copyOperationProductComponents(operationComponents, productionRecord, MODEL_RECORD_OPERATION_PRODUCT_OUT_COMPONENT,
                    orderQuantity);
        }
    }

    private void copyOperationProductComponents(final List<Entity> orderOperations, final Entity productionRecord,
            final String modelName, final BigDecimal orderQuantity) {
        if (checkIfOperationListIsEmpty(orderOperations)) {
            return;
        }

        DataDefinition recordProductDD = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER, modelName);
        Map<Long, Entity> recordProductsMap = newHashMap();
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
                Long productId = technologyProduct.getBelongsToField("product").getId();
                BigDecimal plannedQuantity = getBigDecimal(technologyProduct.getField("quantity"));
                BigDecimal newPlannedQuantity = null;
                Entity recordProduct = null;

                if (recordProductsMap.containsKey(productId)) {
                    recordProduct = recordProductsMap.get(productId);
                    newPlannedQuantity = getBigDecimal(recordProduct.getField("plannedQuantity")).add(plannedQuantity).multiply(
                            orderQuantity);
                } else {
                    recordProduct = recordProductDD.create();
                    recordProduct.setField("product", technologyProduct.getField("product"));
                    newPlannedQuantity = plannedQuantity.multiply(orderQuantity);
                }
                recordProduct.setField("plannedQuantity", newPlannedQuantity.setScale(3, BigDecimal.ROUND_HALF_UP));
                recordProductsMap.put(productId, recordProduct);
            }
        }
        productionRecord.setField(recordProductFieldName, newArrayList(recordProductsMap.values()));
    }

    public boolean checkIfOperationIsSet(final DataDefinition dd, final Entity productionRecord) {
        String recordingMode = productionRecord.getBelongsToField(FIELD_ORDER).getStringField(FIELD_TYPE_OF_PRODUCTION_RCORDING);
        Object orderOperation = productionRecord.getField(FIELD_ORDER_OPERATION_COMPONENT);

        if (PARAM_RECORDING_TYPE_FOREACH.equals(recordingMode) && orderOperation == null) {
            productionRecord.addError(dd.getField(FIELD_ORDER_OPERATION_COMPONENT),
                    "productionCounting.record.messages.error.operationIsNotSet");
            return false;
        }
        return true;
    }

    public void countPlannedTimeAndBalance(final DataDefinition dataDefinition, final Entity productionRecord) {
        Entity order = productionRecord.getBelongsToField(FIELD_ORDER);
        if (!getBooleanValue(order.getField("registerProductionTime"))) {
            return;
        }
        String typeOfProductionRecording = order.getStringField(FIELD_TYPE_OF_PRODUCTION_RCORDING);
        List<Entity> operationComponents = null;
        if (PARAM_RECORDING_TYPE_CUMULATED.equals(typeOfProductionRecording)) {
            operationComponents = order.getTreeField("orderOperationComponents");
        } else if (PARAM_RECORDING_TYPE_FOREACH.equals(typeOfProductionRecording)) {
            operationComponents = newArrayList(productionRecord.getBelongsToField(FIELD_ORDER_OPERATION_COMPONENT));
        }
        countPlannedTime(productionRecord, operationComponents);
        countTimeBalance(productionRecord);
    }

    private void countPlannedTime(final Entity productionRecord, final List<Entity> operationComponents) {
        if (checkIfOperationListIsEmpty(operationComponents)) {
            return;
        }
        BigDecimal plannedTime = BigDecimal.ZERO;
        BigDecimal plannedMachineTime = BigDecimal.ZERO;
        BigDecimal plannedLaborTime = BigDecimal.ZERO;

        for (Entity orderOperationComponent : operationComponents) {
            BigDecimal tj = getBigDecimal(orderOperationComponent.getField("tj"));
            if (tj == BigDecimal.ZERO) {
                continue;
            }
            BigDecimal tpz = getBigDecimal(orderOperationComponent.getField("tpz"));
            BigDecimal productionInOneCycle = getBigDecimal(orderOperationComponent.getField("productionInOneCycle"));
            BigDecimal machineUtilization = getBigDecimal(orderOperationComponent.getField("machineUtilization"));
            BigDecimal laborUtilization = getBigDecimal(orderOperationComponent.getField("laborUtilization"));
            plannedTime = plannedTime.add(tj.multiply(productionInOneCycle)).add(tpz);
            plannedMachineTime = plannedMachineTime.add(plannedTime.multiply(machineUtilization));
            plannedLaborTime = plannedLaborTime.add(plannedTime.multiply(laborUtilization));
        }
        productionRecord.setField("plannedTime", plannedTime.setScale(0, ROUND_UP).intValue());
        productionRecord.setField("plannedMachineTime", plannedMachineTime.setScale(0, ROUND_UP).intValue());
        productionRecord.setField("plannedLaborTime", plannedLaborTime.setScale(0, ROUND_UP).intValue());
    }

    private void countTimeBalance(final Entity productionRecord) {
        BigDecimal plannedMachineTime = getBigDecimal(productionRecord.getField("plannedMachineTime"));
        BigDecimal plannedLaborTime = getBigDecimal(productionRecord.getField("plannedLaborTime"));
        BigDecimal machineTime = getBigDecimal(productionRecord.getField("machineTime"));
        BigDecimal laborTime = getBigDecimal(productionRecord.getField("laborTime"));

        productionRecord
                .setField("machineTimeBalance", machineTime.subtract(plannedMachineTime).setScale(0, ROUND_UP).intValue());
        productionRecord.setField("laborTimeBalance", laborTime.subtract(plannedLaborTime).setScale(0, ROUND_UP).intValue());
    }

    private static boolean checkIfOperationListIsEmpty(final List<Entity> orderOperations) {
        return orderOperations == null || orderOperations.isEmpty() || orderOperations.get(0) == null;
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

    public static Integer getInteger(final Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return Integer.valueOf(value.toString());
    }

    public static Boolean getBooleanValue(final Object fieldValue) {
        return fieldValue instanceof Boolean && (Boolean) fieldValue;
    }
}
