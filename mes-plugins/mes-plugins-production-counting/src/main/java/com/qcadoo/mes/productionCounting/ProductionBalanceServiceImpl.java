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
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
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
    public Map<Long, Entity> groupProductionTrackingsRegisteredTimes(final Entity productionBalance,
            final List<Entity> productionTrackings) {
        Map<Long, Entity> groupedProductionTrackings = Maps.newHashMap();

        if ((productionBalance != null) && (productionTrackings != null)) {
            Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);

            String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

            for (Entity productionTracking : productionTrackings) {
                Entity technologyOperationComponent = productionTracking
                        .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

                if (productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)) {
                    Long technologyOperationComponentId = technologyOperationComponent.getId();

                    if (groupedProductionTrackings.containsKey(technologyOperationComponentId)) {
                        updateProductionTrackingRegisteredTimes(groupedProductionTrackings, productionTracking,
                                technologyOperationComponentId);
                    } else {
                        addProductionTrackingRegisteredTimes(groupedProductionTrackings, productionTracking,
                                technologyOperationComponentId);
                    }
                } else if (productionCountingService.isTypeOfProductionRecordingCumulated(typeOfProductionRecording)) {
                    if (groupedProductionTrackings.isEmpty()) {
                        addProductionTrackingRegisteredTimes(groupedProductionTrackings, productionTracking, 0L);
                    } else {
                        updateProductionTrackingRegisteredTimes(groupedProductionTrackings, productionTracking, 0L);
                    }
                }
            }
        }

        return groupedProductionTrackings;
    }

    private void addProductionTrackingRegisteredTimes(final Map<Long, Entity> groupedProductionTracking,
            final Entity productionTracking, final Long technologyOperationComponentId) {
        Integer machineTime = IntegerUtils.convertNullToZero(productionTracking
                .getIntegerField(ProductionTrackingFields.MACHINE_TIME));
        Integer laborTime = IntegerUtils.convertNullToZero(productionTracking
                .getIntegerField(ProductionTrackingFields.LABOR_TIME));
        BigDecimal executedOperationCycles = BigDecimalUtils.convertNullToZero(productionTracking
                .getDecimalField(ProductionTrackingFields.EXECUTED_OPERATION_CYCLES));

        productionTracking.setField(ProductionTrackingFields.MACHINE_TIME, machineTime);
        productionTracking.setField(ProductionTrackingFields.LABOR_TIME, laborTime);
        productionTracking.setField(ProductionTrackingFields.EXECUTED_OPERATION_CYCLES,
                numberService.setScale(executedOperationCycles));

        groupedProductionTracking.put(technologyOperationComponentId, productionTracking);
    }

    private void updateProductionTrackingRegisteredTimes(final Map<Long, Entity> groupedProductionTrackings,
            final Entity productionTracking, final Long technologyOperationComponentId) {
        Entity addedProductionTracking = groupedProductionTrackings.get(technologyOperationComponentId);

        Integer machineTime = addedProductionTracking.getIntegerField(ProductionTrackingFields.MACHINE_TIME);
        Integer laborTime = addedProductionTracking.getIntegerField(ProductionTrackingFields.LABOR_TIME);
        BigDecimal executedOperationCycles = addedProductionTracking
                .getDecimalField(ProductionTrackingFields.EXECUTED_OPERATION_CYCLES);

        machineTime += IntegerUtils.convertNullToZero(productionTracking.getIntegerField(ProductionTrackingFields.MACHINE_TIME));
        laborTime += IntegerUtils.convertNullToZero(productionTracking.getIntegerField(ProductionTrackingFields.LABOR_TIME));
        executedOperationCycles = executedOperationCycles.add(BigDecimalUtils.convertNullToZero(productionTracking
                .getDecimalField(ProductionTrackingFields.EXECUTED_OPERATION_CYCLES)), numberService.getMathContext());

        addedProductionTracking.setField(ProductionTrackingFields.MACHINE_TIME, machineTime);
        addedProductionTracking.setField(ProductionTrackingFields.LABOR_TIME, laborTime);
        addedProductionTracking.setField(ProductionTrackingFields.EXECUTED_OPERATION_CYCLES,
                numberService.setScale(executedOperationCycles));

        groupedProductionTrackings.put(technologyOperationComponentId, addedProductionTracking);
    }

    @Override
    public Map<Long, Map<String, Integer>> fillProductionTrackingsWithPlannedTimes(final Entity productionBalance,
            final List<Entity> productionTrackings) {
        Map<Long, Map<String, Integer>> productionTrackingsWithPlannedTimes = Maps.newHashMap();

        if ((productionBalance != null) && (productionTrackings != null)) {
            Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);

            String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

            for (Entity productionTracking : productionTrackings) {
                Map<String, Integer> plannedTimes = countPlannedTimes(productionBalance, productionTracking);

                if (!plannedTimes.isEmpty()) {
                    Entity technologyOperationComponent = productionTracking
                            .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

                    if (productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)) {
                        Long technologyOperationComponentId = technologyOperationComponent.getId();

                        if (!productionTrackingsWithPlannedTimes.containsKey(technologyOperationComponentId)) {
                            addProductionTrackingWithPlannedTimes(productionTrackingsWithPlannedTimes, plannedTimes,
                                    technologyOperationComponentId);
                        }
                    } else if (productionCountingService.isTypeOfProductionRecordingCumulated(typeOfProductionRecording)
                            && productionTrackingsWithPlannedTimes.isEmpty()) {
                        addProductionTrackingWithPlannedTimes(productionTrackingsWithPlannedTimes, plannedTimes, 0L);
                    }
                }
            }
        }

        return productionTrackingsWithPlannedTimes;
    }

    private void addProductionTrackingWithPlannedTimes(final Map<Long, Map<String, Integer>> productionTrackingsWithPlannedTimes,
            final Map<String, Integer> plannedTimes, final Long technologyOperationComponentId) {
        productionTrackingsWithPlannedTimes.put(technologyOperationComponentId, plannedTimes);
    }

    private Map<String, Integer> countPlannedTimes(final Entity productionBalance, final Entity productionTracking) {
        Map<String, Integer> plannedTimes = Maps.newHashMap();

        if ((productionBalance != null) && (productionTracking != null)) {
            Entity order = productionTracking.getBelongsToField(OrdersConstants.MODEL_ORDER);

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

            Map<Entity, OperationWorkTime> operationsWorkTime = operationWorkTimeService
                    .estimateOperationsWorkTimeForOrder(order, operationRuns,
                            productionBalance.getBooleanField(ProductionBalanceFields.INCLUDE_TPZ),
                            productionBalance.getBooleanField(ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME),
                            order.getBelongsToField(OrderFields.PRODUCTION_LINE), false);

            String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

            if (productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)) {
                countTimeOperation(plannedTimes, operationsWorkTime.get(productionTracking
                        .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT)));
            } else if (productionCountingService.isTypeOfProductionRecordingCumulated(typeOfProductionRecording)) {
                Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
                EntityTree technologyOperationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);

                for (Entity technologyOperationComponent : technologyOperationComponents) {
                    countTimeOperation(plannedTimes, operationsWorkTime.get(technologyOperationComponent));
                }
            }
        }

        return plannedTimes;
    }

    private void countTimeOperation(final Map<String, Integer> plannedTimes, final OperationWorkTime operationWorkTime) {
        Integer plannedMachineTime = IntegerUtils.convertNullToZero(plannedTimes.get(L_PLANNED_MACHINE_TIME))
                + IntegerUtils.convertNullToZero(operationWorkTime.getMachineWorkTime());
        Integer plannedLaborTime = IntegerUtils.convertNullToZero(plannedTimes.get(L_PLANNED_LABOR_TIME))
                + IntegerUtils.convertNullToZero(operationWorkTime.getLaborWorkTime());

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
