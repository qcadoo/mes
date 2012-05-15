/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.5
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

import static com.qcadoo.mes.orders.constants.OrderFields.PRODUCTION_LINE;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.BalanceOperationProductInComponentFields.BALANCE;
import static com.qcadoo.mes.productionCounting.internal.constants.BalanceOperationProductInComponentFields.PLANNED_QUANTITY;
import static com.qcadoo.mes.productionCounting.internal.constants.BalanceOperationProductInComponentFields.PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.BalanceOperationProductInComponentFields.PRODUCTION_RECORD;
import static com.qcadoo.mes.productionCounting.internal.constants.BalanceOperationProductInComponentFields.USED_QUANTITY;
import static com.qcadoo.mes.productionCounting.internal.constants.CalculateOperationCostsMode.HOURLY;
import static com.qcadoo.mes.productionCounting.internal.constants.CalculateOperationCostsMode.PIECEWORK;
import static com.qcadoo.mes.productionCounting.internal.constants.OperationPieceworkComponentFields.CYCLES;
import static com.qcadoo.mes.productionCounting.internal.constants.OperationPieceworkComponentFields.CYCLES_BALANCE;
import static com.qcadoo.mes.productionCounting.internal.constants.OperationPieceworkComponentFields.PLANNED_CYCLES;
import static com.qcadoo.mes.productionCounting.internal.constants.OperationPieceworkComponentFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.OperationTimeComponentFields.LABOR_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.OperationTimeComponentFields.LABOR_TIME_BALANCE;
import static com.qcadoo.mes.productionCounting.internal.constants.OperationTimeComponentFields.MACHINE_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.OperationTimeComponentFields.MACHINE_TIME_BALANCE;
import static com.qcadoo.mes.productionCounting.internal.constants.OperationTimeComponentFields.PLANNED_LABOR_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.OperationTimeComponentFields.PLANNED_MACHINE_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_PIECEWORK;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_PRODUCTION_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.BALANCE_OPERATION_PRODUCT_IN_COMPONENTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.BALANCE_OPERATION_PRODUCT_OUT_COMPONENTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.DATE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.FILE_NAME;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.GENERATED;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.INCLUDE_TPZ;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.OPERATION_PIECEWORK_COMPONENTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.OPERATION_TIME_COMPONENTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.RECORDS_NUMBER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.WORKER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_BALANCE_OPERATION_PRODUCT_IN_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_BALANCE_OPERATION_PRODUCT_OUT_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.EXECUTED_OPERATION_CYCLES;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.RECORD_OPERATION_PRODUCT_IN_COMPONENTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.RECORD_OPERATION_PRODUCT_OUT_COMPONENTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.STATE;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.BASIC;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.CUMULATED;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.FOR_EACH;
import static com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStates.ACCEPTED;
import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.LABOR_UTILIZATION;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.MACHINE_UTILIZATION;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.operationTimeCalculations.OrderRealizationTimeService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields;
import com.qcadoo.mes.productionCounting.internal.print.ProductionBalancePdfService;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.aop.Monitorable;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ProductionBalanceServiceImpl implements ProductionBalanceService {

    private static final String L_PLANNED_MACHINE_TIME = "plannedMachineTime";

    private static final String L_PLANNED_LABOR_TIME = "plannedLaborTime";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ProductionBalancePdfService productionBalancePdfService;

    @Autowired
    private ProductionBalanceReportDataService productionBalanceReportDataService;

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
    private OrderRealizationTimeService orderRealizationTimeService;

    public void updateRecordsNumber(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        Entity order = productionBalance.getBelongsToField(MODEL_ORDER);

        if ((order != null) && !checkIfTypeOfProductionRecordingIsBasic(order)) {
            Integer recordsNumber = dataDefinitionService
                    .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD)
                    .find().add(SearchRestrictions.eq(STATE, ACCEPTED.getStringValue()))
                    .add(SearchRestrictions.belongsTo(ORDER, order)).list().getEntities().size();

            productionBalance.setField(RECORDS_NUMBER, recordsNumber);
        }
    }

    public void clearGeneratedOnCopy(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        productionBalance.setField(FILE_NAME, null);
        productionBalance.setField(GENERATED, false);
        productionBalance.setField(DATE, null);
        productionBalance.setField(WORKER, null);
    }

    public boolean validateOrder(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        Entity order = productionBalance.getBelongsToField(MODEL_ORDER);

        if ((order == null) || checkIfTypeOfProductionRecordingIsBasic(order)) {
            productionBalance.addError(productionBalanceDD.getField(MODEL_ORDER),
                    "productionCounting.productionBalance.report.error.orderWithoutRecordingType");

            return false;
        }

        if (!order.getBooleanField(REGISTER_PRODUCTION_TIME)
                && HOURLY.getStringValue().equals(productionBalance.getField(CALCULATE_OPERATION_COST_MODE))) {
            productionBalance.addError(productionBalanceDD.getField(MODEL_ORDER),
                    "productionCounting.productionBalance.report.error.orderWithoutRegisterProductionTime");

            return false;
        } else if (!order.getBooleanField(REGISTER_PIECEWORK)
                && PIECEWORK.getStringValue().equals(productionBalance.getField(CALCULATE_OPERATION_COST_MODE))) {
            productionBalance.addError(productionBalanceDD.getField(MODEL_ORDER),
                    "productionCounting.productionBalance.report.error.orderWithoutRegisterPiecework");

            return false;
        }

        List<Entity> productionRecordList = getProductionRecordsFromDB(order);

        if (productionRecordList.isEmpty()) {
            productionBalance.addError(productionBalanceDD.getField(MODEL_ORDER),
                    "productionCounting.productionBalance.report.error.orderWithoutProductionRecords");

            return false;
        }

        return true;
    }

    @Transactional
    public void generateProductionBalance(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        state.performEvent(viewDefinitionState, "save", new String[0]);

        if (!state.isHasError()) {
            Entity productionBalance = getProductionBalanceFromDB((Long) state.getFieldValue());

            if (productionBalance == null) {
                state.addMessage("qcadooView.message.entityNotFound", MessageType.FAILURE);
                return;
            } else if (StringUtils.hasText(productionBalance.getStringField(FILE_NAME))) {
                state.addMessage("productionCounting.productionBalance.report.error.documentsWasGenerated", MessageType.FAILURE);
                return;
            }

            if (!productionBalance.getBooleanField(GENERATED)) {
                fillReportValues(productionBalance);

                fillFieldsAndGrids(productionBalance);
            }

            productionBalance = getProductionBalanceFromDB((Long) state.getFieldValue());

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

    public void printProductionBalance(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        reportService.printGeneratedReport(viewDefinitionState, state, new String[] { args[0],
                ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_BALANCE, args[1] });
    }

    private void fillReportValues(final Entity productionBalance) {
        productionBalance.setField(GENERATED, true);
        productionBalance.setField(DATE,
                new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, LocaleContextHolder.getLocale()).format(new Date()));
        productionBalance.setField(WORKER, securityService.getCurrentUserName());
    }

    private void fillFieldsAndGrids(final Entity productionBalance) {
        Entity order = productionBalance.getBelongsToField(ORDER);

        if ((order == null) || checkIfTypeOfProductionRecordingIsBasic(order)) {
            return;
        }

        List<Entity> productionRecords = getProductionRecordsFromDB(order);

        List<Entity> groupedProductionRecords = productionBalanceReportDataService
                .groupProductionRecordsByOperation(productionRecords);

        if (order.getBooleanField(REGISTER_QUANTITY_IN_PRODUCT)) {
            fillBalanceOperationProductComponents(productionBalance, productionRecords, RECORD_OPERATION_PRODUCT_IN_COMPONENTS,
                    BALANCE_OPERATION_PRODUCT_IN_COMPONENTS, MODEL_BALANCE_OPERATION_PRODUCT_IN_COMPONENT);
        }

        if (order.getBooleanField(REGISTER_QUANTITY_OUT_PRODUCT)) {
            fillBalanceOperationProductComponents(productionBalance, productionRecords, RECORD_OPERATION_PRODUCT_OUT_COMPONENTS,
                    BALANCE_OPERATION_PRODUCT_OUT_COMPONENTS, MODEL_BALANCE_OPERATION_PRODUCT_OUT_COMPONENT);
        }

        if (HOURLY.getStringValue().equals(productionBalance.getStringField(CALCULATE_OPERATION_COST_MODE))
                && order.getBooleanField(REGISTER_PRODUCTION_TIME)) {
            Map<Long, Map<String, Integer>> productionRecordsWithPlannedTimes = fillProductionRecordsWithPlannedTimes(
                    productionBalance, productionRecords);

            if (FOR_EACH.getStringValue().equals(order.getStringField(TYPE_OF_PRODUCTION_RECORDING))) {
                fillTimeValues(productionBalance, groupedProductionRecords, productionRecordsWithPlannedTimes);
                fillOperationTimeComponents(productionBalance, groupedProductionRecords, productionRecordsWithPlannedTimes);
            } else if (CUMULATED.getStringValue().equals(order.getStringField(TYPE_OF_PRODUCTION_RECORDING))) {
                fillTimeValues(productionBalance, groupedProductionRecords, productionRecordsWithPlannedTimes);
            }
        } else if (PIECEWORK.getStringValue().equals(productionBalance.getStringField(CALCULATE_OPERATION_COST_MODE))
                && order.getBooleanField(REGISTER_PIECEWORK)) {
            fillOperationPieceworkComponents(productionBalance, groupedProductionRecords);
        }
    }

    private void fillBalanceOperationProductComponents(final Entity productionBalance, final List<Entity> productionRecords,
            final String recordOperationProductComponentsModel, final String balanceOperationProductComponentsModel,
            final String balanceOperationProductComponentModel) {
        if (productionBalance == null) {
            return;
        }

        Map<Long, Entity> balanceOperationProductComponents = Maps.newHashMap();
        Set<Long> addedTechnologyInstanceOperationComponents = Sets.newHashSet();

        boolean shouldAdd = true;

        for (Entity productionRecord : productionRecords) {
            List<Entity> recordOperationProductComponents = productionRecord
                    .getHasManyField(recordOperationProductComponentsModel);

            Entity technologyInstanceOperationComponent = productionRecord
                    .getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);

            if (technologyInstanceOperationComponent != null) {
                if (addedTechnologyInstanceOperationComponents.contains(technologyInstanceOperationComponent.getId())) {
                    shouldAdd = false;
                } else {
                    shouldAdd = true;
                }
            }

            if (recordOperationProductComponents != null) {
                for (Entity recordOperationProductComponent : recordOperationProductComponents) {
                    addBalanceOperationProductComponent(balanceOperationProductComponents, balanceOperationProductComponentModel,
                            productionRecord, recordOperationProductComponent, shouldAdd);
                }
            }

            if (technologyInstanceOperationComponent == null) {
                shouldAdd = false;
            } else {
                addedTechnologyInstanceOperationComponents.add(technologyInstanceOperationComponent.getId());
            }
        }

        productionBalance.setField(balanceOperationProductComponentsModel,
                Lists.newArrayList(balanceOperationProductComponents.values()));

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private void addBalanceOperationProductComponent(Map<Long, Entity> balanceOperationProductComponents,
            final String balanceOperationProductComponentModel, final Entity productionRecord,
            final Entity recordOperationProductComponent, boolean shouldAdd) {
        Long productId = recordOperationProductComponent.getBelongsToField(PRODUCT).getId();

        if (balanceOperationProductComponents.containsKey(productId)) {
            Entity balanceOperationProductInComponent = balanceOperationProductComponents.get(productId);

            BigDecimal plannedQuantity = balanceOperationProductInComponent.getDecimalField(PLANNED_QUANTITY);
            BigDecimal usedQuantity = balanceOperationProductInComponent.getDecimalField(USED_QUANTITY);

            if (shouldAdd) {
                plannedQuantity = plannedQuantity.add(recordOperationProductComponent.getDecimalField(PLANNED_QUANTITY),
                        numberService.getMathContext());
            }

            if (recordOperationProductComponent.getField(USED_QUANTITY) != null) {
                usedQuantity = (usedQuantity == null) ? recordOperationProductComponent.getDecimalField(USED_QUANTITY)
                        : usedQuantity.add(recordOperationProductComponent.getDecimalField(USED_QUANTITY),
                                numberService.getMathContext());
            }

            BigDecimal balance = (usedQuantity == null) ? null : usedQuantity.subtract(plannedQuantity,
                    numberService.getMathContext());

            balanceOperationProductInComponent.setField(PLANNED_QUANTITY, plannedQuantity);
            balanceOperationProductInComponent.setField(USED_QUANTITY, usedQuantity);

            balanceOperationProductInComponent.setField(BALANCE, balance);

            balanceOperationProductComponents.put(productId, balanceOperationProductInComponent);
        } else {
            Entity balanceOperationProductComponent = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                    balanceOperationProductComponentModel).create();

            balanceOperationProductComponent.setField(PRODUCTION_RECORD,
                    recordOperationProductComponent.getField(PRODUCTION_RECORD));
            balanceOperationProductComponent.setField(PRODUCT, recordOperationProductComponent.getField(PRODUCT));
            balanceOperationProductComponent.setField(PLANNED_QUANTITY,
                    recordOperationProductComponent.getField(PLANNED_QUANTITY));
            balanceOperationProductComponent.setField(USED_QUANTITY, recordOperationProductComponent.getField(USED_QUANTITY));
            balanceOperationProductComponent.setField(BALANCE, recordOperationProductComponent.getField(BALANCE));

            balanceOperationProductComponents.put(productId, balanceOperationProductComponent);
        }
    }

    private void fillTimeValues(final Entity productionBalance, final List<Entity> productionRecords,
            final Map<Long, Map<String, Integer>> productionRecordsWithPlannedTimes) {
        if (productionBalance == null) {
            return;
        }

        Integer plannedMachineTime = 0;
        Integer machineTime = 0;

        Integer laborTime = 0;
        Integer plannedLaborTime = 0;

        if (!productionRecords.isEmpty() || !productionRecordsWithPlannedTimes.isEmpty()) {
            for (Entity productionRecord : productionRecords) {
                plannedMachineTime += productionRecordsWithPlannedTimes.get(productionRecord.getId()).get(L_PLANNED_MACHINE_TIME);
                machineTime += (Integer) productionRecord.getField(MACHINE_TIME);

                plannedLaborTime += productionRecordsWithPlannedTimes.get(productionRecord.getId()).get(L_PLANNED_LABOR_TIME);
                laborTime += (Integer) productionRecord.getField(LABOR_TIME);
            }
        }

        Integer machineTimeBalance = machineTime - plannedMachineTime;
        Integer laborTimeBalance = laborTime - plannedLaborTime;

        productionBalance.setField(PLANNED_MACHINE_TIME, plannedMachineTime);
        productionBalance.setField(MACHINE_TIME, machineTime);
        productionBalance.setField(MACHINE_TIME_BALANCE, machineTimeBalance);

        productionBalance.setField(PLANNED_LABOR_TIME, plannedLaborTime);
        productionBalance.setField(LABOR_TIME, laborTime);
        productionBalance.setField(LABOR_TIME_BALANCE, laborTimeBalance);

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private void fillOperationTimeComponents(final Entity productionBalance, final List<Entity> productionRecords,
            final Map<Long, Map<String, Integer>> productionRecordsWithPlannedTimes) {
        if (productionBalance == null) {
            return;
        }

        List<Entity> operationTimeComponents = Lists.newArrayList();

        if (!productionRecords.isEmpty() || !productionRecordsWithPlannedTimes.isEmpty()) {
            for (Entity productionRecord : productionRecords) {
                Entity operationTimeComponent = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                        ProductionCountingConstants.MODEL_OPERATION_TIME_COMPONENT).create();

                Integer plannedMachineTime = productionRecordsWithPlannedTimes.get(productionRecord.getId()).get(
                        L_PLANNED_MACHINE_TIME);
                Integer machineTime = (Integer) productionRecord.getField(MACHINE_TIME);

                Integer machineTimeBalance = machineTime - plannedMachineTime;

                Integer plannedLaborTime = productionRecordsWithPlannedTimes.get(productionRecord.getId()).get(
                        L_PLANNED_LABOR_TIME);
                Integer laborTime = (Integer) productionRecord.getField(LABOR_TIME);

                Integer laborTimeBalance = laborTime - plannedLaborTime;

                operationTimeComponent.setField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT,
                        productionRecord.getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT));

                operationTimeComponent.setField(PLANNED_MACHINE_TIME, plannedMachineTime);
                operationTimeComponent.setField(MACHINE_TIME, machineTime);
                operationTimeComponent.setField(MACHINE_TIME_BALANCE, machineTimeBalance);

                operationTimeComponent.setField(PLANNED_LABOR_TIME, plannedLaborTime);
                operationTimeComponent.setField(LABOR_TIME, laborTime);
                operationTimeComponent.setField(LABOR_TIME_BALANCE, laborTimeBalance);

                operationTimeComponents.add(operationTimeComponent);
            }

        }

        productionBalance.setField(OPERATION_TIME_COMPONENTS, operationTimeComponents);

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private void fillOperationPieceworkComponents(final Entity productionBalance, final List<Entity> productionRecords) {
        if (productionBalance == null) {
            return;
        }

        List<Entity> operationPieceworkComponents = Lists.newArrayList();

        Map<Entity, BigDecimal> operationRuns = Maps.newHashMap();

        Map<Entity, BigDecimal> productComponents = productQuantitiesService.getProductComponentQuantities(
                asList(productionBalance.getBelongsToField(ORDER)), operationRuns);

        if (!productionRecords.isEmpty() && !productComponents.isEmpty()) {
            for (Entity productionRecord : productionRecords) {
                Entity operationPieceworkComponent = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                        ProductionCountingConstants.MODEL_OPERATION_PIECEWORK_COMPONENT).create();

                operationPieceworkComponent.setField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT,
                        productionRecord.getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT));

                Entity technologyInstanceOperationComponent = productionRecord
                        .getBelongsToField(ProductionRecordFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);
                Entity proxyTechnologyOperationComponent = technologyInstanceOperationComponent
                        .getBelongsToField(TECHNOLOGY_OPERATION_COMPONENT);
                Long technologyOperationComponentId = proxyTechnologyOperationComponent.getId();

                Entity technologyOperationComponent = getTechnologyOperationComponentFromDB(technologyOperationComponentId);

                if ((technologyOperationComponent != null) && operationRuns.containsKey(technologyOperationComponent)) {
                    BigDecimal plannedCycles = operationRuns.get(technologyOperationComponent);

                    BigDecimal cycles = productionRecord.getDecimalField(EXECUTED_OPERATION_CYCLES);

                    BigDecimal cyclesBalance = cycles.subtract(plannedCycles, numberService.getMathContext());

                    operationPieceworkComponent.setField(PLANNED_CYCLES, numberService.setScale(plannedCycles));
                    operationPieceworkComponent.setField(CYCLES, numberService.setScale(cycles));
                    operationPieceworkComponent.setField(CYCLES_BALANCE, numberService.setScale(cyclesBalance));

                    operationPieceworkComponents.add(operationPieceworkComponent);
                }
            }
        }

        productionBalance.setField(OPERATION_PIECEWORK_COMPONENTS, operationPieceworkComponents);

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private Entity getTechnologyOperationComponentFromDB(final Long technologyOperationComponentId) {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(technologyOperationComponentId);
    }

    public void generateProductionBalanceDocuments(final Entity productionBalance, final Locale locale) throws IOException,
            DocumentException {

        String localePrefix = "productionCounting.productionBalance.report.fileName";

        Entity productionBalanceWithFileName = fileService.updateReportFileName(productionBalance, DATE, localePrefix);

        Entity company = getCompanyFromDB();

        try {
            productionBalancePdfService.generateDocument(productionBalanceWithFileName, company, locale);

            generateProductionBalance.notifyObserversThatTheBalanceIsBeingGenerated(productionBalance);
        } catch (IOException e) {
            throw new IllegalStateException("Problem with saving productionBalance report");
        } catch (DocumentException e) {
            throw new IllegalStateException("Problem with generating productionBalance report");
        }
    }

    @Monitorable
    public Map<Long, Map<String, Integer>> fillProductionRecordsWithPlannedTimes(final Entity productionBalance,
            final List<Entity> productionRecords) {
        Map<Long, Map<String, Integer>> productionRecordsWithPlannedTimes = Maps.newHashMap();

        if (!productionRecords.isEmpty()) {
            for (Entity productionRecord : productionRecords) {
                Map<String, Integer> plannedTimes = countPlannedTimes(productionBalance, productionRecord);

                if (!plannedTimes.isEmpty()) {
                    if (productionRecordsWithPlannedTimes.containsKey(productionRecord.getId())) {
                        Map<String, Integer> productionRecordPlannedTimes = productionRecordsWithPlannedTimes
                                .get(productionRecord.getId());

                        Integer plannedMachineTime = productionRecordPlannedTimes.get(L_PLANNED_MACHINE_TIME)
                                + plannedTimes.get(L_PLANNED_MACHINE_TIME);

                        Integer plannedLaborTime = productionRecordPlannedTimes.get(L_PLANNED_LABOR_TIME)
                                + plannedTimes.get(L_PLANNED_LABOR_TIME);

                        productionRecordPlannedTimes.put(L_PLANNED_MACHINE_TIME, plannedMachineTime);
                        productionRecordPlannedTimes.put(L_PLANNED_LABOR_TIME, plannedLaborTime);

                        productionRecordsWithPlannedTimes.put(productionRecord.getId(), productionRecordPlannedTimes);
                    } else {
                        productionRecordsWithPlannedTimes.put(productionRecord.getId(), plannedTimes);
                    }
                }
            }
        }

        return productionRecordsWithPlannedTimes;
    }

    @Monitorable
    private Map<String, Integer> countPlannedTimes(final Entity productionBalance, final Entity productionRecord) {
        Map<String, Integer> plannedTimes = Maps.newHashMap();

        Entity order = productionRecord.getBelongsToField(OrdersConstants.MODEL_ORDER);

        if (order == null || !order.getBooleanField(REGISTER_PRODUCTION_TIME)) {
            return plannedTimes;
        }

        plannedTimes.put(L_PLANNED_MACHINE_TIME, 0);
        plannedTimes.put(L_PLANNED_LABOR_TIME, 0);

        String typeOfProductionRecording = order.getStringField(TYPE_OF_PRODUCTION_RECORDING);

        Map<Entity, Integer> durationOperation = orderRealizationTimeService.estimateOperationTimeConsumptions(order,
                (BigDecimal) order.getField(ProductionRecordFields.PLANNED_QUANTITY),
                productionBalance.getBooleanField(INCLUDE_TPZ), productionBalance.getBooleanField(INCLUDE_ADDITIONAL_TIME),
                order.getBelongsToField(PRODUCTION_LINE));

        if (FOR_EACH.getStringValue().equals(typeOfProductionRecording)) {
            countTimeOperation(productionRecord.getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT), plannedTimes,
                    durationOperation.get(productionRecord.getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)
                            .getBelongsToField(TECHNOLOGY_OPERATION_COMPONENT)));
        } else if (CUMULATED.getStringValue().equals(typeOfProductionRecording)) {
            EntityTree technologyInstanceOperationComponents = order.getTreeField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENTS);

            for (Entity technologyInstanceOperationComponent : technologyInstanceOperationComponents) {
                countTimeOperation(technologyInstanceOperationComponent, plannedTimes,
                        durationOperation.get(technologyInstanceOperationComponent
                                .getBelongsToField(TECHNOLOGY_OPERATION_COMPONENT)));
            }
        }

        return plannedTimes;
    }

    private void countTimeOperation(final Entity technologyInstanceOperationComponent, final Map<String, Integer> plannedTimes,
            final Integer durationOfOperation) {
        BigDecimal durationOfOperationComponent = BigDecimal.valueOf(durationOfOperation);

        Integer plannedMachineTime = plannedTimes.get(L_PLANNED_MACHINE_TIME)
                + numberService.setScale(
                        durationOfOperationComponent.multiply(
                                getBigDecimal(technologyInstanceOperationComponent.getDecimalField(MACHINE_UTILIZATION)),
                                numberService.getMathContext())).intValue();

        Integer plannedLaborTime = plannedTimes.get(L_PLANNED_LABOR_TIME)
                + numberService.setScale(
                        durationOfOperationComponent.multiply(
                                getBigDecimal(technologyInstanceOperationComponent.getField(LABOR_UTILIZATION)),
                                numberService.getMathContext())).intValue();

        plannedTimes.put(L_PLANNED_MACHINE_TIME, plannedMachineTime);
        plannedTimes.put(L_PLANNED_LABOR_TIME, plannedLaborTime);
    }

    private static BigDecimal getBigDecimal(final Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return BigDecimal.valueOf(Double.valueOf(value.toString()));
    }

    public boolean checkIfTypeOfProductionRecordingIsBasic(final Entity order) {
        return BASIC.getStringValue().equals(order.getStringField(TYPE_OF_PRODUCTION_RECORDING));
    }

    public List<Entity> getProductionRecordsFromDB(final Entity order) {
        return dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq(STATE, ACCEPTED.getStringValue())).add(SearchRestrictions.belongsTo(ORDER, order))
                .list().getEntities();
    }

    public Entity getProductionBalanceFromDB(final Long productionBalanceId) {
        return dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_BALANCE).get(productionBalanceId);
    }

    public Entity getOrderFromDB(final Long orderId) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);
    }

    public Entity getCompanyFromDB() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .add(SearchRestrictions.eq("owner", true)).setMaxResults(1).uniqueResult();
    }
}
