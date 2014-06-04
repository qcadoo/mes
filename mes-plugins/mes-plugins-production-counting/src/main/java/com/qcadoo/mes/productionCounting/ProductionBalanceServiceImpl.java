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
package com.qcadoo.mes.productionCounting;

import static java.util.Arrays.asList;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.constants.OperationPieceworkComponentFields;
import com.qcadoo.mes.productionCounting.constants.OperationTimeComponentFields;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.IntegerUtils;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ProductionBalanceServiceImpl implements ProductionBalanceService {

    private static final String L_PRODUCT = "product";

    private static final String L_PLANNED_QUANTITY = "plannedQuantity";

    private static final String L_USED_QUANTITY = "usedQuantity";

    private static final String L_BALANCE = "balance";

    private static final String L_PLANNED_MACHINE_TIME = "plannedMachineTime";

    private static final String L_PLANNED_LABOR_TIME = "plannedLaborTime";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

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

            Map<Entity, OperationWorkTime> operationsWorkTime = operationWorkTimeService.estimateOperationsWorkTimeForOrder(
                    order, operationRuns, productionBalance.getBooleanField(ProductionBalanceFields.INCLUDE_TPZ),
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

    @Override
    public void fillFieldsAndGrids(final Entity productionBalance) {
        Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);

        if ((order == null)
                || productionCountingService.isTypeOfProductionRecordingBasic(order
                        .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            return;
        }

        List<Entity> productionTrackings = productionCountingService.getProductionTrackingsForOrder(order);

        Map<Long, Entity> productionTrackingsWithRegisteredTimes = groupProductionTrackingsRegisteredTimes(productionBalance,
                productionTrackings);

        if (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT)) {
            fillBalanceOperationProductComponents(productionBalance, productionTrackings,
                    ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS,
                    ProductionBalanceFields.BALANCE_OPERATION_PRODUCT_IN_COMPONENTS,
                    ProductionCountingConstants.MODEL_BALANCE_OPERATION_PRODUCT_IN_COMPONENT);
        }

        if (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT)) {
            fillBalanceOperationProductComponents(productionBalance, productionTrackings,
                    ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS,
                    ProductionBalanceFields.BALANCE_OPERATION_PRODUCT_OUT_COMPONENTS,
                    ProductionCountingConstants.MODEL_BALANCE_OPERATION_PRODUCT_OUT_COMPONENT);
        }

        if (productionCountingService.isCalculateOperationCostModeHourly(productionBalance
                .getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE))
                && order.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME)) {
            Map<Long, Map<String, Integer>> productionTrackingsWithPlannedTimes = fillProductionTrackingsWithPlannedTimes(
                    productionBalance, productionTrackings);

            calculatePlannedTimeValues(productionBalance);
            fillTimeValues(productionBalance, productionTrackingsWithRegisteredTimes, productionTrackingsWithPlannedTimes);

            if (productionCountingService.isTypeOfProductionRecordingForEach(order
                    .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
                fillOperationTimeComponents(productionBalance, productionTrackingsWithRegisteredTimes,
                        productionTrackingsWithPlannedTimes);
            }
        } else if (productionCountingService.isCalculateOperationCostModePiecework(productionBalance
                .getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE))
                && order.getBooleanField(OrderFieldsPC.REGISTER_PIECEWORK)) {
            fillOperationPieceworkComponents(productionBalance, productionTrackingsWithRegisteredTimes);
        }
    }

    private void fillBalanceOperationProductComponents(final Entity productionBalance, final List<Entity> productionTrackings,
            final String trackingOperationProductComponentsModel, final String balanceOperationProductComponentsModel,
            final String balanceOperationProductComponentModel) {
        if (productionBalance == null) {
            return;
        }

        Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);

        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        Map<Long, Entity> balanceOperationProductComponents = Maps.newHashMap();
        Set<Long> addedTechnologyOperationComponents = Sets.newHashSet();

        boolean shouldAddPlannedQuantity = true;

        for (Entity productionTracking : productionTrackings) {
            List<Entity> trackingOperationProductComponents = productionTracking
                    .getHasManyField(trackingOperationProductComponentsModel);

            Entity technologyOperationComponent = productionTracking
                    .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

            if (productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)) {
                if (addedTechnologyOperationComponents.contains(technologyOperationComponent.getId())) {
                    shouldAddPlannedQuantity = false;
                } else {
                    shouldAddPlannedQuantity = true;
                }
            }

            if (trackingOperationProductComponents != null) {
                for (Entity trackingOperationProductComponent : trackingOperationProductComponents) {
                    Entity product = trackingOperationProductComponent.getBelongsToField(L_PRODUCT);

                    if (product != null) {
                        Long productId = product.getId();

                        if (balanceOperationProductComponents.containsKey(productId)) {
                            updateBalanceOperationComponent(balanceOperationProductComponents, trackingOperationProductComponent,
                                    productId, shouldAddPlannedQuantity);
                        } else {
                            addBalanceOperationComponent(balanceOperationProductComponents,
                                    balanceOperationProductComponentModel, trackingOperationProductComponent, productId);
                        }
                    }
                }
            }

            if (productionCountingService.isTypeOfProductionRecordingCumulated(typeOfProductionRecording)) {
                shouldAddPlannedQuantity = false;
            } else {
                addedTechnologyOperationComponents.add(technologyOperationComponent.getId());
            }
        }

        productionBalance.setField(balanceOperationProductComponentsModel,
                Lists.newArrayList(balanceOperationProductComponents.values()));
    }

    private void addBalanceOperationComponent(final Map<Long, Entity> balanceOperationProductComponents,
            final String balanceOperationProductComponentModel, final Entity trackingOperationProductComponent,
            final Long productId) {
        BigDecimal plannedQuantity = BigDecimalUtils.convertNullToZero(trackingOperationProductComponent
                .getDecimalField(L_PLANNED_QUANTITY));
        BigDecimal usedQuantity = BigDecimalUtils.convertNullToZero(trackingOperationProductComponent
                .getDecimalField(L_USED_QUANTITY));

        BigDecimal balance = usedQuantity.subtract(plannedQuantity, numberService.getMathContext());

        Entity balanceOperationProductComponent = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                balanceOperationProductComponentModel).create();

        balanceOperationProductComponent.setField(L_PRODUCT, trackingOperationProductComponent.getField(L_PRODUCT));

        balanceOperationProductComponent.setField(L_PLANNED_QUANTITY, numberService.setScale(plannedQuantity));
        balanceOperationProductComponent.setField(L_USED_QUANTITY, numberService.setScale(usedQuantity));
        balanceOperationProductComponent.setField(L_BALANCE, numberService.setScale(balance));

        balanceOperationProductComponents.put(productId, balanceOperationProductComponent);
    }

    private void updateBalanceOperationComponent(final Map<Long, Entity> balanceOperationProductComponents,
            final Entity trackingOperationProductComponent, final Long productId, final boolean shouldAddPlannedQuantity) {
        Entity addedBalanceOperationProductInComponent = balanceOperationProductComponents.get(productId);

        BigDecimal plannedQuantity = addedBalanceOperationProductInComponent.getDecimalField(L_PLANNED_QUANTITY);
        BigDecimal usedQuantity = addedBalanceOperationProductInComponent.getDecimalField(L_USED_QUANTITY);

        if (shouldAddPlannedQuantity) {
            plannedQuantity = plannedQuantity.add(
                    BigDecimalUtils.convertNullToZero(trackingOperationProductComponent.getDecimalField(L_PLANNED_QUANTITY)),
                    numberService.getMathContext());
        }

        usedQuantity = usedQuantity.add(
                BigDecimalUtils.convertNullToZero(trackingOperationProductComponent.getDecimalField(L_USED_QUANTITY)),
                numberService.getMathContext());

        BigDecimal balance = usedQuantity.subtract(plannedQuantity, numberService.getMathContext());

        addedBalanceOperationProductInComponent.setField(L_PLANNED_QUANTITY, numberService.setScale(plannedQuantity));
        addedBalanceOperationProductInComponent.setField(L_USED_QUANTITY, numberService.setScale(usedQuantity));
        addedBalanceOperationProductInComponent.setField(L_BALANCE, numberService.setScale(balance));

        balanceOperationProductComponents.put(productId, addedBalanceOperationProductInComponent);
    }

    private void calculatePlannedTimeValues(final Entity productionBalance) {
        final Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);
        final boolean includeTpz = productionBalance.getBooleanField(ProductionBalanceFields.INCLUDE_TPZ);
        final boolean includeAdditionalTime = productionBalance.getBooleanField(ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME);
        final Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);

        // TODO LUPO fix problem with operationRuns
        final Map<Long, BigDecimal> operationRuns = Maps.newHashMap();

        productQuantitiesService.getNeededProductQuantities(Lists.newArrayList(order), MrpAlgorithm.ONLY_COMPONENTS,
                operationRuns);

        final OperationWorkTime operationWorkTime = operationWorkTimeService.estimateTotalWorkTimeForOrder(order, operationRuns,
                includeTpz, includeAdditionalTime, productionLine, false);

        final int plannedMachineTime = operationWorkTime.getMachineWorkTime();
        final int plannedLaborTime = operationWorkTime.getLaborWorkTime();

        productionBalance.setField(ProductionBalanceFields.PLANNED_LABOR_TIME, plannedLaborTime);
        productionBalance.setField(ProductionBalanceFields.PLANNED_MACHINE_TIME, plannedMachineTime);
    }

    private void fillTimeValues(final Entity productionBalance, final Map<Long, Entity> productionTrackingsWithRegisteredTimes,
            final Map<Long, Map<String, Integer>> productionTrackingsWithPlannedTimes) {
        if (productionBalance == null) {
            return;
        }

        Integer machineTime = 0;
        Integer laborTime = 0;

        if (!productionTrackingsWithPlannedTimes.isEmpty()) {
            for (Entry<Long, Entity> productionTrackingWithRegisteredTimes : productionTrackingsWithRegisteredTimes.entrySet()) {
                Entity productionTracking = productionTrackingWithRegisteredTimes.getValue();
                machineTime += productionTracking.getIntegerField(ProductionTrackingFields.MACHINE_TIME);
                laborTime += productionTracking.getIntegerField(ProductionTrackingFields.LABOR_TIME);
            }
        }

        final int machineTimeBalance = machineTime
                - productionBalance.getIntegerField(ProductionBalanceFields.PLANNED_MACHINE_TIME);
        final int laborTimeBalance = laborTime - productionBalance.getIntegerField(ProductionBalanceFields.PLANNED_LABOR_TIME);

        productionBalance.setField(ProductionBalanceFields.MACHINE_TIME, machineTime);
        productionBalance.setField(ProductionBalanceFields.MACHINE_TIME_BALANCE, machineTimeBalance);

        productionBalance.setField(ProductionBalanceFields.LABOR_TIME, laborTime);
        productionBalance.setField(ProductionBalanceFields.LABOR_TIME_BALANCE, laborTimeBalance);
    }

    private void fillOperationTimeComponents(final Entity productionBalance,
            final Map<Long, Entity> productionTrackingsWithRegisteredTimes,
            final Map<Long, Map<String, Integer>> productionTrackingsWithPlannedTimes) {
        if (productionBalance == null) {
            return;
        }

        List<Entity> operationTimeComponents = Lists.newArrayList();

        if (!productionTrackingsWithPlannedTimes.isEmpty()) {
            for (Entry<Long, Entity> productionTrackingWithRegisteredTimes : productionTrackingsWithRegisteredTimes.entrySet()) {
                Entity productionTracking = productionTrackingWithRegisteredTimes.getValue();

                Entity technologyOperationComponent = productionTracking
                        .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

                if (technologyOperationComponent != null) {
                    Long technologyOperationComponentId = technologyOperationComponent.getId();

                    if (productionTrackingsWithPlannedTimes.containsKey(technologyOperationComponentId)) {
                        Integer plannedMachineTime = productionTrackingsWithPlannedTimes.get(technologyOperationComponentId).get(
                                L_PLANNED_MACHINE_TIME);
                        Integer machineTime = productionTracking.getIntegerField(ProductionTrackingFields.MACHINE_TIME);

                        Integer machineTimeBalance = machineTime - plannedMachineTime;

                        Integer plannedLaborTime = productionTrackingsWithPlannedTimes.get(technologyOperationComponentId).get(
                                L_PLANNED_LABOR_TIME);
                        Integer laborTime = productionTracking.getIntegerField(ProductionTrackingFields.LABOR_TIME);

                        Integer laborTimeBalance = laborTime - plannedLaborTime;

                        Entity operationTimeComponent = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                                ProductionCountingConstants.MODEL_OPERATION_TIME_COMPONENT).create();

                        operationTimeComponent.setField(OperationPieceworkComponentFields.TECHNOLOGY_OPERATION_COMPONENT,
                                technologyOperationComponent);

                        operationTimeComponent.setField(OperationTimeComponentFields.PLANNED_MACHINE_TIME, plannedMachineTime);
                        operationTimeComponent.setField(OperationTimeComponentFields.MACHINE_TIME, machineTime);
                        operationTimeComponent.setField(OperationTimeComponentFields.MACHINE_TIME_BALANCE, machineTimeBalance);

                        operationTimeComponent.setField(OperationTimeComponentFields.PLANNED_LABOR_TIME, plannedLaborTime);
                        operationTimeComponent.setField(OperationTimeComponentFields.LABOR_TIME, laborTime);
                        operationTimeComponent.setField(OperationTimeComponentFields.LABOR_TIME_BALANCE, laborTimeBalance);

                        operationTimeComponents.add(operationTimeComponent);
                    }
                }
            }
        }

        productionBalance.setField(ProductionBalanceFields.OPERATION_TIME_COMPONENTS, operationTimeComponents);
    }

    private void fillOperationPieceworkComponents(final Entity productionBalance,
            final Map<Long, Entity> productionTrackingsWithRegisteredTimes) {
        if (productionBalance == null) {
            return;
        }

        List<Entity> operationPieceworkComponents = Lists.newArrayList();

        Map<Long, BigDecimal> operationRuns = Maps.newHashMap();

        OperationProductComponentWithQuantityContainer productComponents = productQuantitiesService
                .getProductComponentQuantities(asList(productionBalance.getBelongsToField(ProductionBalanceFields.ORDER)),
                        operationRuns);

        if (!productComponents.asMap().isEmpty()) {
            for (Entry<Long, Entity> productionTrackingWithRegisteredTimes : productionTrackingsWithRegisteredTimes.entrySet()) {
                Entity productionTracking = productionTrackingWithRegisteredTimes.getValue();

                Entity technologyOperationComponent = productionTracking
                        .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

                if (technologyOperationComponent != null) {
                    Long technologyOperationComponentId = technologyOperationComponent.getId();

                    if (operationRuns.containsKey(technologyOperationComponentId)) {
                        BigDecimal plannedCycles = operationRuns.get(technologyOperationComponentId);

                        BigDecimal cycles = productionTracking
                                .getDecimalField(ProductionTrackingFields.EXECUTED_OPERATION_CYCLES);

                        BigDecimal cyclesBalance = cycles.subtract(plannedCycles, numberService.getMathContext());

                        Entity operationPieceworkComponent = dataDefinitionService.get(
                                ProductionCountingConstants.PLUGIN_IDENTIFIER,
                                ProductionCountingConstants.MODEL_OPERATION_PIECEWORK_COMPONENT).create();

                        operationPieceworkComponent.setField(OperationPieceworkComponentFields.TECHNOLOGY_OPERATION_COMPONENT,
                                technologyOperationComponent);

                        operationPieceworkComponent.setField(OperationPieceworkComponentFields.PLANNED_CYCLES,
                                numberService.setScale(plannedCycles));
                        operationPieceworkComponent.setField(OperationPieceworkComponentFields.CYCLES,
                                numberService.setScale(cycles));
                        operationPieceworkComponent.setField(OperationPieceworkComponentFields.CYCLES_BALANCE,
                                numberService.setScale(cyclesBalance));

                        operationPieceworkComponents.add(operationPieceworkComponent);
                    }
                }
            }
        }

        productionBalance.setField(ProductionBalanceFields.OPERATION_PIECEWORK_COMPONENTS, operationPieceworkComponents);
    }

}
