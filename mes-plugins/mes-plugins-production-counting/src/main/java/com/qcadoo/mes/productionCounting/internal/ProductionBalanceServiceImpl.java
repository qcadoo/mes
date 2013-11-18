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
package com.qcadoo.mes.productionCounting.internal;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lowagie.text.DocumentException;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.internal.constants.CalculateOperationCostsMode;
import com.qcadoo.mes.productionCounting.internal.constants.OperationPieceworkComponentFields;
import com.qcadoo.mes.productionCounting.internal.constants.OperationTimeComponentFields;
import com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields;
import com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording;
import com.qcadoo.mes.productionCounting.internal.print.ProductionBalancePdfService;
import com.qcadoo.mes.productionCounting.states.constants.ProductionRecordState;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.IntegerUtils;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ProductionBalanceServiceImpl implements ProductionBalanceService {

    private static final String L_BALANCE = "balance";

    private static final String L_USED_QUANTITY = "usedQuantity";

    private static final String L_PLANNED_QUANTITY = "plannedQuantity";

    private static final String L_PRODUCT = "product";

    private static final String L_PLANNED_MACHINE_TIME = "plannedMachineTime";

    private static final String L_PLANNED_LABOR_TIME = "plannedLaborTime";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ProductionBalancePdfService productionBalancePdfService;

    @Autowired
    private ProductionCountingGenerateProductionBalance generateProductionBalance;

    @Autowired
    private FileService fileService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Override
    public void updateRecordsNumber(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);

        if ((order != null) && !isTypeOfProductionRecordingBasic(order)) {
            int recordsNumber = dataDefinitionService
                    .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD)
                    .find()
                    .add(SearchRestrictions.eq(ProductionRecordFields.STATE, ProductionRecordState.ACCEPTED.getStringValue()))
                    .add(SearchRestrictions.belongsTo(ProductionRecordFields.ORDER, order)).list().getEntities().size();

            productionBalance.setField(ProductionBalanceFields.RECORDS_NUMBER, recordsNumber);
        }
    }

    @Override
    public void clearGeneratedOnCopy(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        productionBalance.setField(ProductionBalanceFields.FILE_NAME, null);
        productionBalance.setField(ProductionBalanceFields.GENERATED, false);
        productionBalance.setField(ProductionBalanceFields.DATE, null);
        productionBalance.setField(ProductionBalanceFields.WORKER, null);
    }

    @Override
    public boolean validateOrder(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);

        if ((order == null) || isTypeOfProductionRecordingBasic(order)) {
            productionBalance.addError(productionBalanceDD.getField(ProductionBalanceFields.ORDER),
                    "productionCounting.productionBalance.report.error.orderWithoutRecordingType");

            return false;
        }

        if (!order.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME)
                && isCalculateOperationCostModeHourly(productionBalance)) {
            productionBalance.addError(productionBalanceDD.getField(ProductionBalanceFields.ORDER),
                    "productionCounting.productionBalance.report.error.orderWithoutRegisterProductionTime");

            return false;
        } else if (!order.getBooleanField(OrderFieldsPC.REGISTER_PIECEWORK)
                && isCalculateOperationCostModePiecework(productionBalance)) {
            productionBalance.addError(productionBalanceDD.getField(ProductionBalanceFields.ORDER),
                    "productionCounting.productionBalance.report.error.orderWithoutRegisterPiecework");

            return false;
        }

        List<Entity> productionRecordList = getProductionRecordsFromDB(order);

        if (productionRecordList.isEmpty()) {
            productionBalance.addError(productionBalanceDD.getField(ProductionBalanceFields.ORDER),
                    "productionCounting.productionBalance.report.error.orderWithoutProductionRecords");

            return false;
        }

        return true;
    }

    @Override
    public void printProductionBalance(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        reportService.printGeneratedReport(viewDefinitionState, state, new String[] { args[0],
                ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_BALANCE, args[1] });
    }

    @Transactional
    @Override
    public void generateProductionBalance(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        state.performEvent(viewDefinitionState, "save", new String[0]);

        if (!state.isHasError()) {
            Entity productionBalance = getProductionBalanceFromDB((Long) state.getFieldValue());

            if (productionBalance == null) {
                state.addMessage("qcadooView.message.entityNotFound", MessageType.FAILURE);
                return;
            } else if (StringUtils.hasText(productionBalance.getStringField(ProductionBalanceFields.FILE_NAME))) {
                state.addMessage("productionCounting.productionBalance.report.error.documentsWasGenerated", MessageType.FAILURE);
                return;
            }

            if (!productionBalance.getBooleanField(ProductionBalanceFields.GENERATED)) {
                fillReportValues(productionBalance);

                fillFieldsAndGrids(productionBalance);
            }

            checkOrderDoneQuantity(state, productionBalance);

            try {
                generateProductionBalanceDocuments(productionBalance, state.getLocale());

                state.performEvent(viewDefinitionState, "reset", new String[0]);

                state.addMessage(
                        "productionCounting.productionBalanceDetails.window.mainTab.productionBalanceDetails.generatedMessage",
                        MessageType.SUCCESS);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (DocumentException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    private void checkOrderDoneQuantity(final ComponentState componentState, final Entity productionBalance) {
        final Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);
        final BigDecimal doneQuantityFromOrder = order.getDecimalField(OrderFields.DONE_QUANTITY);
        if (doneQuantityFromOrder == null || BigDecimal.ZERO.compareTo(doneQuantityFromOrder) == 0) {
            componentState
                    .addMessage("productionRecord.productionBalance.report.info.orderWithoutDoneQuantity", MessageType.INFO);
        }
    }

    @Override
    public void generateProductionBalanceDocuments(final Entity productionBalance, final Locale locale) throws IOException,
            DocumentException {

        String localePrefix = "productionCounting.productionBalance.report.fileName";

        Entity productionBalanceWithFileName = fileService.updateReportFileName(productionBalance, ProductionBalanceFields.DATE,
                localePrefix);

        try {
            productionBalancePdfService.generateDocument(productionBalanceWithFileName, locale);

            generateProductionBalance.notifyObserversThatTheBalanceIsBeingGenerated(productionBalance);
        } catch (IOException e) {
            throw new IllegalStateException("Problem with saving productionBalance report");
        } catch (DocumentException e) {
            throw new IllegalStateException("Problem with generating productionBalance report");
        }
    }

    private void fillReportValues(final Entity productionBalance) {
        productionBalance.setField(ProductionBalanceFields.GENERATED, true);
        productionBalance.setField(ProductionBalanceFields.DATE, new Date());
        productionBalance.setField(ProductionBalanceFields.WORKER, securityService.getCurrentUserName());
    }

    @Override
    public void fillFieldsAndGrids(final Entity productionBalance) {
        Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);

        if ((order == null) || isTypeOfProductionRecordingBasic(order)) {
            return;
        }

        List<Entity> productionRecords = getProductionRecordsFromDB(order);

        Map<Long, Entity> productionRecordsWithRegisteredTimes = groupProductionRecordsRegisteredTimes(productionBalance,
                productionRecords);

        if (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT)) {
            fillBalanceOperationProductComponents(productionBalance, productionRecords,
                    ProductionRecordFields.RECORD_OPERATION_PRODUCT_IN_COMPONENTS,
                    ProductionBalanceFields.BALANCE_OPERATION_PRODUCT_IN_COMPONENTS,
                    ProductionCountingConstants.MODEL_BALANCE_OPERATION_PRODUCT_IN_COMPONENT);
        }

        if (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT)) {
            fillBalanceOperationProductComponents(productionBalance, productionRecords,
                    ProductionRecordFields.RECORD_OPERATION_PRODUCT_OUT_COMPONENTS,
                    ProductionBalanceFields.BALANCE_OPERATION_PRODUCT_OUT_COMPONENTS,
                    ProductionCountingConstants.MODEL_BALANCE_OPERATION_PRODUCT_OUT_COMPONENT);
        }

        if (isCalculateOperationCostModeHourly(productionBalance)
                && order.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME)) {
            Map<Long, Map<String, Integer>> productionRecordsWithPlannedTimes = fillProductionRecordsWithPlannedTimes(
                    productionBalance, productionRecords);

            calculatePlannedTimeValues(productionBalance);
            fillTimeValues(productionBalance, productionRecordsWithRegisteredTimes, productionRecordsWithPlannedTimes);

            if (isTypeOfProductionRecordingForEach(order)) {
                fillOperationTimeComponents(productionBalance, productionRecordsWithRegisteredTimes,
                        productionRecordsWithPlannedTimes);
            }
        } else if (isCalculateOperationCostModePiecework(productionBalance)
                && order.getBooleanField(OrderFieldsPC.REGISTER_PIECEWORK)) {
            fillOperationPieceworkComponents(productionBalance, productionRecordsWithRegisteredTimes);
        }
    }

    @Override
    public Map<Long, Entity> groupProductionRecordsRegisteredTimes(final Entity productionBalance,
            final List<Entity> productionRecords) {
        Map<Long, Entity> groupedProductionRecords = Maps.newHashMap();

        if ((productionBalance != null) && (productionRecords != null)) {
            Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);

            for (Entity productionRecord : productionRecords) {
                Entity technologyInstanceOperationComponent = productionRecord
                        .getBelongsToField(ProductionRecordFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);

                if (isTypeOfProductionRecordingForEach(order)) {
                    Long technologyInstanceOperationComponentId = technologyInstanceOperationComponent.getId();

                    if (groupedProductionRecords.containsKey(technologyInstanceOperationComponentId)) {
                        updateProductionRecordRegisteredTimes(groupedProductionRecords, productionRecord,
                                technologyInstanceOperationComponentId);
                    } else {
                        addProductionRecordRegisteredTimes(groupedProductionRecords, productionRecord,
                                technologyInstanceOperationComponentId);
                    }
                } else if (isTypeOfProductionRecordingCumulated(order)) {
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

    private void fillBalanceOperationProductComponents(final Entity productionBalance, final List<Entity> productionRecords,
            final String recordOperationProductComponentsModel, final String balanceOperationProductComponentsModel,
            final String balanceOperationProductComponentModel) {
        if (productionBalance == null) {
            return;
        }

        Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);

        Map<Long, Entity> balanceOperationProductComponents = Maps.newHashMap();
        Set<Long> addedTechnologyInstanceOperationComponents = Sets.newHashSet();

        boolean shouldAddPlannedQuantity = true;

        for (Entity productionRecord : productionRecords) {
            List<Entity> recordOperationProductComponents = productionRecord
                    .getHasManyField(recordOperationProductComponentsModel);

            Entity technologyInstanceOperationComponent = productionRecord
                    .getBelongsToField(ProductionRecordFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);

            if (isTypeOfProductionRecordingForEach(order)) {
                Long technologyInstanceOperationComponentId = technologyInstanceOperationComponent.getId();

                shouldAddPlannedQuantity = !addedTechnologyInstanceOperationComponents
                        .contains(technologyInstanceOperationComponentId);
            }

            if (recordOperationProductComponents != null) {
                for (Entity recordOperationProductComponent : recordOperationProductComponents) {
                    Entity product = recordOperationProductComponent.getBelongsToField(L_PRODUCT);

                    if (product != null) {
                        Long productId = product.getId();

                        if (balanceOperationProductComponents.containsKey(productId)) {
                            updateBalanceOperationComponent(balanceOperationProductComponents, recordOperationProductComponent,
                                    productId, shouldAddPlannedQuantity);
                        } else {
                            addBalanceOperationComponent(balanceOperationProductComponents,
                                    balanceOperationProductComponentModel, recordOperationProductComponent, productId);
                        }
                    }
                }
            }

            if (isTypeOfProductionRecordingCumulated(order)) {
                shouldAddPlannedQuantity = false;
            } else {
                addedTechnologyInstanceOperationComponents.add(technologyInstanceOperationComponent.getId());
            }
        }

        productionBalance.setField(balanceOperationProductComponentsModel,
                Lists.newArrayList(balanceOperationProductComponents.values()));
    }

    private void addBalanceOperationComponent(final Map<Long, Entity> balanceOperationProductComponents,
            final String balanceOperationProductComponentModel, final Entity recordOperationProductComponent, final Long productId) {
        Entity balanceOperationProductComponent = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                balanceOperationProductComponentModel).create();

        BigDecimal plannedQuantity = BigDecimalUtils.convertNullToZero(recordOperationProductComponent
                .getDecimalField(L_PLANNED_QUANTITY));
        BigDecimal usedQuantity = BigDecimalUtils.convertNullToZero(recordOperationProductComponent
                .getDecimalField(L_USED_QUANTITY));

        BigDecimal balance = usedQuantity.subtract(plannedQuantity, numberService.getMathContext());

        balanceOperationProductComponent.setField(L_PRODUCT, recordOperationProductComponent.getBelongsToField(L_PRODUCT));

        balanceOperationProductComponent.setField(L_PLANNED_QUANTITY, numberService.setScale(plannedQuantity));
        balanceOperationProductComponent.setField(L_USED_QUANTITY, numberService.setScale(usedQuantity));
        balanceOperationProductComponent.setField(L_BALANCE, numberService.setScale(balance));

        balanceOperationProductComponents.put(productId, balanceOperationProductComponent);
    }

    private void updateBalanceOperationComponent(final Map<Long, Entity> balanceOperationProductComponents,
            final Entity recordOperationProductComponent, final Long productId, final boolean shouldAddPlannedQuantity) {
        Entity addedBalanceOperationProductInComponent = balanceOperationProductComponents.get(productId);

        BigDecimal plannedQuantity = addedBalanceOperationProductInComponent.getDecimalField(L_PLANNED_QUANTITY);
        BigDecimal usedQuantity = addedBalanceOperationProductInComponent.getDecimalField(L_USED_QUANTITY);

        if (shouldAddPlannedQuantity) {
            plannedQuantity = plannedQuantity.add(
                    BigDecimalUtils.convertNullToZero(recordOperationProductComponent.getDecimalField(L_PLANNED_QUANTITY)),
                    numberService.getMathContext());
        }

        usedQuantity = usedQuantity.add(
                BigDecimalUtils.convertNullToZero(recordOperationProductComponent.getDecimalField(L_USED_QUANTITY)),
                numberService.getMathContext());

        BigDecimal balance = usedQuantity.subtract(plannedQuantity, numberService.getMathContext());

        addedBalanceOperationProductInComponent.setField(L_PLANNED_QUANTITY, numberService.setScale(plannedQuantity));
        addedBalanceOperationProductInComponent.setField(L_USED_QUANTITY, numberService.setScale(usedQuantity));
        addedBalanceOperationProductInComponent.setField(L_BALANCE, numberService.setScale(balance));

        balanceOperationProductComponents.put(productId, addedBalanceOperationProductInComponent);
    }

    private void fillTimeValues(final Entity productionBalance, final Map<Long, Entity> productionRecordsWithRegisteredTimes,
            final Map<Long, Map<String, Integer>> productionRecordsWithPlannedTimes) {
        if (productionBalance == null) {
            return;
        }

        Integer machineTime = 0;
        Integer laborTime = 0;

        if (!productionRecordsWithPlannedTimes.isEmpty()) {
            for (Map.Entry<Long, Entity> productionRecordWithRegisteredTimesEntry : productionRecordsWithRegisteredTimes
                    .entrySet()) {
                Entity productionRecordWithRegisteredTimes = productionRecordWithRegisteredTimesEntry.getValue();
                machineTime += productionRecordWithRegisteredTimes.getIntegerField(ProductionRecordFields.MACHINE_TIME);
                laborTime += productionRecordWithRegisteredTimes.getIntegerField(ProductionRecordFields.LABOR_TIME);
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

    private void calculatePlannedTimeValues(final Entity productionBalance) {
        final Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);
        final boolean includeTpz = productionBalance.getBooleanField(ProductionBalanceFields.INCLUDE_TPZ);
        final boolean includeAdditionalTime = productionBalance.getBooleanField(ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME);
        final Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);
        final Map<Entity, BigDecimal> operationRuns = Maps.newHashMap();

        productQuantitiesService.getNeededProductQuantities(Lists.newArrayList(order), MrpAlgorithm.ONLY_COMPONENTS,
                operationRuns);

        final OperationWorkTime operationWorkTime = operationWorkTimeService.estimateTotalWorkTimeForOrder(order, operationRuns,
                includeTpz, includeAdditionalTime, productionLine, false);

        final int plannedMachineTime = operationWorkTime.getMachineWorkTime();
        final int plannedLaborTime = operationWorkTime.getLaborWorkTime();

        productionBalance.setField(ProductionBalanceFields.PLANNED_LABOR_TIME, plannedLaborTime);
        productionBalance.setField(ProductionBalanceFields.PLANNED_MACHINE_TIME, plannedMachineTime);
    }

    private void fillOperationTimeComponents(final Entity productionBalance,
            final Map<Long, Entity> productionRecordsWithRegisteredTimes,
            final Map<Long, Map<String, Integer>> productionRecordsWithPlannedTimes) {
        if (productionBalance == null) {
            return;
        }

        List<Entity> operationTimeComponents = Lists.newArrayList();

        if (!productionRecordsWithPlannedTimes.isEmpty()) {
            for (Map.Entry<Long, Entity> productionRecordWithRegisteredTimesEntry : productionRecordsWithRegisteredTimes
                    .entrySet()) {
                Long technologyInstanceOperationComponentId = productionRecordWithRegisteredTimesEntry.getKey();
                Entity productionRecordWithRegisteredTimes = productionRecordWithRegisteredTimesEntry.getValue();

                Entity operationTimeComponent = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                        ProductionCountingConstants.MODEL_OPERATION_TIME_COMPONENT).create();

                Integer plannedMachineTime = productionRecordsWithPlannedTimes.get(technologyInstanceOperationComponentId).get(
                        L_PLANNED_MACHINE_TIME);
                Integer machineTime = productionRecordWithRegisteredTimes.getIntegerField(ProductionBalanceFields.MACHINE_TIME);

                Integer machineTimeBalance = machineTime - plannedMachineTime;

                Integer plannedLaborTime = productionRecordsWithPlannedTimes.get(technologyInstanceOperationComponentId).get(
                        L_PLANNED_LABOR_TIME);
                Integer laborTime = productionRecordWithRegisteredTimes.getIntegerField(ProductionRecordFields.LABOR_TIME);

                Integer laborTimeBalance = laborTime - plannedLaborTime;

                operationTimeComponent.setField(OperationTimeComponentFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT,
                        productionRecordWithRegisteredTimes
                                .getBelongsToField(ProductionRecordFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT));

                operationTimeComponent.setField(OperationTimeComponentFields.PLANNED_MACHINE_TIME, plannedMachineTime);
                operationTimeComponent.setField(OperationTimeComponentFields.MACHINE_TIME, machineTime);
                operationTimeComponent.setField(OperationTimeComponentFields.MACHINE_TIME_BALANCE, machineTimeBalance);

                operationTimeComponent.setField(OperationTimeComponentFields.PLANNED_LABOR_TIME, plannedLaborTime);
                operationTimeComponent.setField(OperationTimeComponentFields.LABOR_TIME, laborTime);
                operationTimeComponent.setField(OperationTimeComponentFields.LABOR_TIME_BALANCE, laborTimeBalance);

                operationTimeComponents.add(operationTimeComponent);
            }

        }

        productionBalance.setField(ProductionBalanceFields.OPERATION_TIME_COMPONENTS, operationTimeComponents);
    }

    private void fillOperationPieceworkComponents(final Entity productionBalance,
            final Map<Long, Entity> productionRecordsWithRegisteredTimes) {
        if (productionBalance == null) {
            return;
        }

        List<Entity> operationPieceworkComponents = Lists.newArrayList();

        Map<Entity, BigDecimal> operationRuns = Maps.newHashMap();

        Map<Entity, BigDecimal> productComponents = productQuantitiesService.getProductComponentQuantities(
                asList(productionBalance.getBelongsToField(ProductionBalanceFields.ORDER)), operationRuns);

        if (!productComponents.isEmpty()) {
            for (Map.Entry<Long, Entity> productionRecordWithRegisteredTimesEntry : productionRecordsWithRegisteredTimes
                    .entrySet()) {
                Entity productionRecordWithRegisteredTimes = productionRecordWithRegisteredTimesEntry.getValue();

                if (productionRecordWithRegisteredTimes
                        .getBelongsToField(ProductionRecordFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT) != null) {
                    Entity operationPieceworkComponent = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                            ProductionCountingConstants.MODEL_OPERATION_PIECEWORK_COMPONENT).create();

                    operationPieceworkComponent.setField(
                            OperationPieceworkComponentFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT,
                            productionRecordWithRegisteredTimes
                                    .getBelongsToField(ProductionRecordFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT));

                    Entity technologyInstanceOperationComponent = productionRecordWithRegisteredTimes
                            .getBelongsToField(ProductionRecordFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);

                    Entity proxyTechnologyOperationComponent = technologyInstanceOperationComponent
                            .getBelongsToField(TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT);
                    Long technologyOperationComponentId = proxyTechnologyOperationComponent.getId();

                    Entity technologyOperationComponent = getTechnologyOperationComponentFromDB(technologyOperationComponentId);

                    if ((technologyOperationComponent != null) && operationRuns.containsKey(technologyOperationComponent)) {
                        BigDecimal plannedCycles = operationRuns.get(technologyOperationComponent);

                        BigDecimal cycles = productionRecordWithRegisteredTimes
                                .getDecimalField(ProductionRecordFields.EXECUTED_OPERATION_CYCLES);

                        BigDecimal cyclesBalance = cycles.subtract(plannedCycles, numberService.getMathContext());

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

    @Override
    public Map<Long, Map<String, Integer>> fillProductionRecordsWithPlannedTimes(final Entity productionBalance,
            final List<Entity> productionRecords) {
        Map<Long, Map<String, Integer>> productionRecordsWithPlannedTimes = Maps.newHashMap();

        if ((productionBalance != null) && (productionRecords != null)) {
            Entity order = productionBalance.getBelongsToField(ProductionBalanceFields.ORDER);

            for (Entity productionRecord : productionRecords) {
                Map<String, Integer> plannedTimes = countPlannedTimes(productionBalance, productionRecord);

                if (!plannedTimes.isEmpty()) {
                    Entity technologyInstanceOperationComponent = productionRecord
                            .getBelongsToField(ProductionRecordFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);

                    if (isTypeOfProductionRecordingForEach(order)) {
                        Long technologyInstanceOperationComponentId = technologyInstanceOperationComponent.getId();

                        if (!productionRecordsWithPlannedTimes.containsKey(technologyInstanceOperationComponentId)) {
                            addProductionRecordWithPlannedTimes(productionRecordsWithPlannedTimes, plannedTimes,
                                    technologyInstanceOperationComponentId);
                        }
                    } else if (isTypeOfProductionRecordingCumulated(order) && productionRecordsWithPlannedTimes.isEmpty()) {
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
            Map<Entity, BigDecimal> operationRuns = new HashMap<Entity, BigDecimal>();

            productQuantitiesService.getProductComponentQuantities(order.getBelongsToField(OrderFields.TECHNOLOGY),
                    order.getDecimalField(OrderFields.PLANNED_QUANTITY), operationRuns);

            Map<Entity, OperationWorkTime> operationLaborAndMachineWorkTime = operationWorkTimeService
                    .estimateOperationsWorkTimeForOrder(order, operationRuns,
                            productionBalance.getBooleanField(ProductionBalanceFields.INCLUDE_TPZ),
                            productionBalance.getBooleanField(ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME),
                            order.getBelongsToField(OrderFields.PRODUCTION_LINE), false);

            if (isTypeOfProductionRecordingForEach(order)) {
                countTimeOperation(plannedTimes, operationLaborAndMachineWorkTime.get(productionRecord
                        .getBelongsToField(ProductionRecordFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)));
            } else if (isTypeOfProductionRecordingCumulated(order)) {
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
        Integer plannedMachineTime = plannedTimes.get(L_PLANNED_MACHINE_TIME) + durationOfOperation.getMachineWorkTime();
        Integer plannedLaborTime = plannedTimes.get(L_PLANNED_LABOR_TIME) + durationOfOperation.getLaborWorkTime();

        plannedTimes.put(L_PLANNED_MACHINE_TIME, plannedMachineTime);
        plannedTimes.put(L_PLANNED_LABOR_TIME, plannedLaborTime);
    }

    @Override
    public Entity getProductionBalanceFromDB(final Long productionBalanceId) {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_BALANCE).get(productionBalanceId);
    }

    @Override
    public List<Entity> getProductionRecordsFromDB(final Entity order) {
        return dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq(ProductionRecordFields.STATE, ProductionRecordState.ACCEPTED.getStringValue()))
                .add(SearchRestrictions.belongsTo(ProductionRecordFields.ORDER, order)).list().getEntities();
    }

    @Override
    public Entity getTechnologyOperationComponentFromDB(final Long technologyOperationComponentId) {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(technologyOperationComponentId);
    }

    @Override
    public Entity getOrderFromDB(final Long orderId) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);
    }

    @Override
    public boolean isCalculateOperationCostModeHourly(final Entity productionBalance) {
        return CalculateOperationCostsMode.HOURLY.getStringValue().equals(
                productionBalance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE));
    }

    @Override
    public boolean isCalculateOperationCostModePiecework(final Entity productionBalance) {
        return CalculateOperationCostsMode.PIECEWORK.getStringValue().equals(
                productionBalance.getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE));
    }

    @Override
    public boolean isTypeOfProductionRecordingBasic(final Entity order) {
        return TypeOfProductionRecording.BASIC.getStringValue().equals(
                order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING));
    }

    @Override
    public boolean isTypeOfProductionRecordingForEach(final Entity order) {
        return TypeOfProductionRecording.FOR_EACH.getStringValue().equals(
                order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING));
    }

    @Override
    public boolean isTypeOfProductionRecordingCumulated(final Entity order) {
        return TypeOfProductionRecording.CUMULATED.getStringValue().equals(
                order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING));
    }

}
