/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.productionCounting;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.constants.ProductionRecordFields;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.IntegerUtils;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ProductionBalanceServiceImpl implements ProductionBalanceService {

    private static final String L_PLANNED_MACHINE_TIME = "plannedMachineTime";

    private static final String L_PLANNED_LABOR_TIME = "plannedLaborTime";

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Override
    public Map<Long, Entity> groupProductionRecordsRegisteredTimes(final Entity productionBalance,
            final List<Entity> productionRecords) {
        Map<Long, Entity> groupedProductionRecords = Maps.newHashMap();

        if ((productionBalance != null) && (productionRecords != null)) {
            Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);

            String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

            for (Entity productionRecord : productionRecords) {
                Entity technologyInstanceOperationComponent = productionRecord
                        .getBelongsToField(ProductionRecordFields.TECHNOLOGY_OPERATION_COMPONENT);

                if (productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)) {
                    Long technologyInstanceOperationComponentId = technologyInstanceOperationComponent.getId();

                    if (groupedProductionRecords.containsKey(technologyInstanceOperationComponentId)) {
                        updateProductionRecordRegisteredTimes(groupedProductionRecords, productionRecord,
                                technologyInstanceOperationComponentId);
                    } else {
                        addProductionRecordRegisteredTimes(groupedProductionRecords, productionRecord,
                                technologyInstanceOperationComponentId);
                    }
                } else if (productionCountingService.isTypeOfProductionRecordingCumulated(typeOfProductionRecording)) {
                    if (groupedProductionRecords.isEmpty()) {
                        addProductionRecordRegisteredTimes(groupedProductionRecords, productionRecord, 0L);
                    } else {
                        updateProductionRecordRegisteredTimes(groupedProductionRecords, productionRecord, 0L);
                    }
                }
            }
        }

        return groupedProductionRecords;
    }

    private void addProductionRecordRegisteredTimes(final Map<Long, Entity> groupedProductionRecords,
            final Entity productionRecord, final Long technologyInstanceOperationComponentId) {

        Integer machineTime = IntegerUtils.convertNullToZero(productionRecord
                .getIntegerField(ProductionRecordFields.MACHINE_TIME));
        Integer laborTime = IntegerUtils.convertNullToZero(productionRecord.getIntegerField(ProductionRecordFields.LABOR_TIME));
        BigDecimal executedOperationCycles = BigDecimalUtils.convertNullToZero(productionRecord
                .getDecimalField(ProductionRecordFields.EXECUTED_OPERATION_CYCLES));

        productionRecord.setField(ProductionRecordFields.MACHINE_TIME, machineTime);
        productionRecord.setField(ProductionRecordFields.LABOR_TIME, laborTime);
        productionRecord.setField(ProductionRecordFields.EXECUTED_OPERATION_CYCLES,
                numberService.setScale(executedOperationCycles));

        groupedProductionRecords.put(technologyInstanceOperationComponentId, productionRecord);
    }

    private void updateProductionRecordRegisteredTimes(final Map<Long, Entity> groupedProductionRecords,
            final Entity productionRecord, final Long technologyInstanceOperationComponentId) {
        Entity addedProductionRecord = groupedProductionRecords.get(technologyInstanceOperationComponentId);

        Integer machineTime = addedProductionRecord.getIntegerField(ProductionRecordFields.MACHINE_TIME);
        Integer laborTime = addedProductionRecord.getIntegerField(ProductionRecordFields.LABOR_TIME);
        BigDecimal executedOperationCycles = addedProductionRecord
                .getDecimalField(ProductionRecordFields.EXECUTED_OPERATION_CYCLES);

        machineTime += IntegerUtils.convertNullToZero(productionRecord.getIntegerField(ProductionRecordFields.MACHINE_TIME));
        laborTime += IntegerUtils.convertNullToZero(productionRecord.getIntegerField(ProductionRecordFields.LABOR_TIME));
        executedOperationCycles = executedOperationCycles.add(BigDecimalUtils.convertNullToZero(productionRecord
                .getDecimalField(ProductionRecordFields.EXECUTED_OPERATION_CYCLES)), numberService.getMathContext());

        addedProductionRecord.setField(ProductionRecordFields.MACHINE_TIME, machineTime);
        addedProductionRecord.setField(ProductionRecordFields.LABOR_TIME, laborTime);
        addedProductionRecord.setField(ProductionRecordFields.EXECUTED_OPERATION_CYCLES,
                numberService.setScale(executedOperationCycles));

        groupedProductionRecords.put(technologyInstanceOperationComponentId, addedProductionRecord);
    }

    @Override
    public Map<Long, Map<String, Integer>> fillProductionRecordsWithPlannedTimes(final Entity productionBalance,
            final List<Entity> productionRecords) {
        Map<Long, Map<String, Integer>> productionRecordsWithPlannedTimes = Maps.newHashMap();

        if ((productionBalance != null) && (productionRecords != null)) {
            Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);

            String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

            for (Entity productionRecord : productionRecords) {
                Map<String, Integer> plannedTimes = countPlannedTimes(productionBalance, productionRecord);

                if (!plannedTimes.isEmpty()) {
                    Entity technologyInstanceOperationComponent = productionRecord
                            .getBelongsToField(ProductionRecordFields.TECHNOLOGY_OPERATION_COMPONENT);

                    if (productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)) {
                        Long technologyInstanceOperationComponentId = technologyInstanceOperationComponent.getId();

                        if (!productionRecordsWithPlannedTimes.containsKey(technologyInstanceOperationComponentId)) {
                            addProductionRecordWithPlannedTimes(productionRecordsWithPlannedTimes, plannedTimes,
                                    technologyInstanceOperationComponentId);
                        }
                    } else if (productionCountingService.isTypeOfProductionRecordingCumulated(typeOfProductionRecording)
                            && productionRecordsWithPlannedTimes.isEmpty()) {
                        addProductionRecordWithPlannedTimes(productionRecordsWithPlannedTimes, plannedTimes, 0L);
                    }
                }
            }
        }

        return productionRecordsWithPlannedTimes;
    }

    private void addProductionRecordWithPlannedTimes(final Map<Long, Map<String, Integer>> productionRecordsWithPlannedTimes,
            final Map<String, Integer> plannedTimes, final Long technologyInstanceOperationComponentId) {
        productionRecordsWithPlannedTimes.put(technologyInstanceOperationComponentId, plannedTimes);
    }

    private Map<String, Integer> countPlannedTimes(final Entity productionBalance, final Entity productionRecord) {
        Map<String, Integer> plannedTimes = Maps.newHashMap();

        if ((productionBalance != null) && (productionRecord != null)) {
            Entity order = productionRecord.getBelongsToField(OrdersConstants.MODEL_ORDER);

            if ((order == null) || !order.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME)) {
                return plannedTimes;
            }

            plannedTimes.put(L_PLANNED_MACHINE_TIME, 0);
            plannedTimes.put(L_PLANNED_LABOR_TIME, 0);

            // TODO LUPO fix problem with operationRuns
            final Map<Long, BigDecimal> operationRunsFromProductionQuantities = Maps.newHashMap();

            productQuantitiesService.getProductComponentQuantities(order.getBelongsToField(OrderFields.TECHNOLOGY),
                    order.getDecimalField(OrderFields.PLANNED_QUANTITY), operationRunsFromProductionQuantities);

            final Map<Entity, BigDecimal> operationRuns = productQuantitiesService
                    .convertOperationsRunsFromProductQuantities(operationRunsFromProductionQuantities);

            Map<Entity, OperationWorkTime> operationLaborAndMachineWorkTime = operationWorkTimeService
                    .estimateOperationsWorkTimeForOrder(order, operationRuns,
                            productionBalance.getBooleanField(ProductionBalanceFields.INCLUDE_TPZ),
                            productionBalance.getBooleanField(ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME),
                            order.getBelongsToField(OrderFields.PRODUCTION_LINE), false);

            String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

            if (productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)) {
                countTimeOperation(plannedTimes, operationLaborAndMachineWorkTime.get(productionRecord
                        .getBelongsToField(ProductionRecordFields.TECHNOLOGY_OPERATION_COMPONENT)));
            } else if (productionCountingService.isTypeOfProductionRecordingCumulated(typeOfProductionRecording)) {
                EntityTree technologyInstanceOperationComponents = order
                        .getTreeField(OrderFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS);

                for (Entity technologyInstanceOperationComponent : technologyInstanceOperationComponents) {
                    countTimeOperation(plannedTimes, operationLaborAndMachineWorkTime.get(technologyInstanceOperationComponent));
                }
            }
        }

        return plannedTimes;
    }

    private void countTimeOperation(final Map<String, Integer> plannedTimes, final OperationWorkTime durationOfOperation) {
        Integer plannedMachineTime = IntegerUtils.convertNullToZero(plannedTimes.get(L_PLANNED_MACHINE_TIME))
                + IntegerUtils.convertNullToZero(durationOfOperation.getMachineWorkTime());
        Integer plannedLaborTime = IntegerUtils.convertNullToZero(plannedTimes.get(L_PLANNED_LABOR_TIME))
                + IntegerUtils.convertNullToZero(durationOfOperation.getLaborWorkTime());

        plannedTimes.put(L_PLANNED_MACHINE_TIME, plannedMachineTime);
        plannedTimes.put(L_PLANNED_LABOR_TIME, plannedLaborTime);
    }

    @Override
    public void disableCheckboxes(final ViewDefinitionState view) {
        FieldComponent calculateOperationCostsModeField = (FieldComponent) view
                .getComponentByReference(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE);

        FieldComponent includeTPZ = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.INCLUDE_TPZ);
        FieldComponent includeAdditionalTime = (FieldComponent) view
                .getComponentByReference(ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME);

        String calculateOperationCostsMode = (String) calculateOperationCostsModeField.getFieldValue();

        if (productionCountingService.isCalculateOperationCostModePiecework(calculateOperationCostsMode)) {
            includeTPZ.setFieldValue(false);
            includeTPZ.setEnabled(false);
            includeTPZ.requestComponentUpdateState();

            includeAdditionalTime.setFieldValue(false);
            includeAdditionalTime.setEnabled(false);
            includeAdditionalTime.requestComponentUpdateState();
        } else {
            includeTPZ.setEnabled(true);
            includeTPZ.requestComponentUpdateState();

            includeAdditionalTime.setEnabled(true);
            includeAdditionalTime.requestComponentUpdateState();
        }
    }

}
