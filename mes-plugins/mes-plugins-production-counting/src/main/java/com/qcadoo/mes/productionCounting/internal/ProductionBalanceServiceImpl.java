/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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

import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_PRODUCT;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.CalculateOperationCostsMode.HOURLY;
import static com.qcadoo.mes.productionCounting.internal.constants.CalculateOperationCostsMode.PIECEWORK;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.BALANCE_OPERATION_PRODUCT_IN_COMPONENTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.BALANCE_OPERATION_PRODUCT_OUT_COMPONENTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.DATE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.FILE_NAME;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.GENERATED;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.LABOR_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.LABOR_TIME_BALANCE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.MACHINE_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.MACHINE_TIME_BALANCE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.OPERATION_PIECEWORK_COMPONENTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.OPERATION_TIME_COMPONENTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.PLANNED_LABOR_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.PLANNED_MACHINE_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.RECORDS_NUMBER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.WORKER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_BALANCE_OPERATION_PRODUCT_IN_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_PRODUCTION_RECORD;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_REGISTER_IN_PRODUCTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_REGISTER_OUT_PRODUCTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_REGISTER_PIECEWORK;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_REGISTER_PRODUCTION_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.BALANCE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.PLANNED_QUANTITY;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.RECORD_OPERATION_PRODUCT_IN_COMPONENTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.RECORD_OPERATION_PRODUCT_OUT_COMPONENTS;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.STATE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.USED_QUANTITY;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.BASIC;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.CUMULATED;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.FOR_EACH;
import static com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStates.ACCEPTED;
import static com.qcadoo.mes.productionScheduling.constants.ProductionSchedulingConstants.MODEL_ORDER_OPERATION_COMPONENT;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.DocumentException;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.print.ProductionBalancePdfService;
import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductInOutComparator;
import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductionRecordOperationComparator;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductionBalanceServiceImpl implements ProductionBalanceService {

    private static final String L_PLANNED_QUANTITY = "plannedQuantity";

    private static final String L_USED_QUANTITY = "usedQuantity";

    private static final String L_BALANCE = "balance";

    private static final String L_PLANNED_MACHINE_TIME = "plannedMachineTime";

    private static final String L_MACHINE_TIME = "machineTime";

    private static final String L_MACHINE_TIME_BALANCE = "machineTimeBalance";

    private static final String L_PLANNED_LABOR_TIME = "plannedLaborTime";

    private static final String L_LABOR_TIME = "laborTime";

    private static final String L_LABOR_TIME_BALANCE = "laborTimeBalance";

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

        if (!order.getBooleanField(PARAM_REGISTER_PRODUCTION_TIME)
                && HOURLY.getStringValue().equals(productionBalance.getField(CALCULATE_OPERATION_COST_MODE))) {
            productionBalance.addError(productionBalanceDD.getField(MODEL_ORDER),
                    "productionCounting.productionBalance.report.error.orderWithoutRegisterProductionTime");

            return false;
        } else if (!order.getBooleanField(PARAM_REGISTER_PIECEWORK)
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

        if (!state.isHasError() && (state instanceof FormComponent)) {
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

        if (order.getBooleanField(PARAM_REGISTER_IN_PRODUCTS)) {
            fillBalanceOperationProductInComponents(productionBalance, order);
        }

        if (order.getBooleanField(PARAM_REGISTER_OUT_PRODUCTS)) {
            fillBalanceOperationProductOutComponents(productionBalance, order);
        }

        if (HOURLY.getStringValue().equals(productionBalance.getStringField(CALCULATE_OPERATION_COST_MODE))
                && order.getBooleanField(PARAM_REGISTER_PRODUCTION_TIME)) {
            if (FOR_EACH.getStringValue().equals(order.getStringField(TYPE_OF_PRODUCTION_RECORDING))) {
                fillTimeValues(productionBalance, order);
                fillOperationTimeComponents(productionBalance, order);
            } else if (CUMULATED.getStringValue().equals(order.getStringField(TYPE_OF_PRODUCTION_RECORDING))) {
                fillTimeValues(productionBalance, order);
            }
        } else if (PIECEWORK.getStringValue().equals(productionBalance.getStringField(CALCULATE_OPERATION_COST_MODE))
                && order.getBooleanField(PARAM_REGISTER_PIECEWORK)) {
            fillOperationPieceworkComponents(productionBalance, order);
        }
    }

    private void fillBalanceOperationProductInComponents(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        List<Entity> balanceOperationProductInComponents = Lists.newArrayList();

        List<Entity> productionRecords = getProductionRecordsFromDB(order);

        for (Entity productionRecord : productionRecords) {
            List<Entity> recordOperationProductInComponents = productionRecord
                    .getHasManyField(RECORD_OPERATION_PRODUCT_IN_COMPONENTS);

            if (recordOperationProductInComponents != null) {
                for (Entity recordOperationProductInComponent : recordOperationProductInComponents) {
                    Entity balanceOperationProductInComponent = dataDefinitionService.get(
                            ProductionCountingConstants.PLUGIN_IDENTIFIER, MODEL_BALANCE_OPERATION_PRODUCT_IN_COMPONENT).create();

                    balanceOperationProductInComponent.setField(MODEL_PRODUCTION_RECORD,
                            recordOperationProductInComponent.getField(MODEL_PRODUCTION_RECORD));
                    balanceOperationProductInComponent.setField(MODEL_PRODUCT,
                            recordOperationProductInComponent.getField(PRODUCT));
                    balanceOperationProductInComponent.setField(L_USED_QUANTITY,
                            recordOperationProductInComponent.getField(USED_QUANTITY));
                    balanceOperationProductInComponent.setField(L_PLANNED_QUANTITY,
                            recordOperationProductInComponent.getField(PLANNED_QUANTITY));
                    balanceOperationProductInComponent.setField(L_BALANCE, recordOperationProductInComponent.getField(BALANCE));

                    balanceOperationProductInComponents.add(balanceOperationProductInComponent);
                }
            }
        }

        if (!balanceOperationProductInComponents.isEmpty()) {
            Collections.sort(balanceOperationProductInComponents, new EntityProductInOutComparator());

            productionBalance.setField(BALANCE_OPERATION_PRODUCT_IN_COMPONENTS,
                    productionBalanceReportDataService.groupProductInOutComponentsByProduct(balanceOperationProductInComponents));

            productionBalance.getDataDefinition().save(productionBalance);
        }
    }

    private void fillBalanceOperationProductOutComponents(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        List<Entity> balanceOperationProductOutComponents = Lists.newArrayList();

        List<Entity> productionRecordList = getProductionRecordsFromDB(order);

        for (Entity productionRecord : productionRecordList) {
            List<Entity> recordOperationProductOutComponents = productionRecord
                    .getHasManyField(RECORD_OPERATION_PRODUCT_OUT_COMPONENTS);

            if (recordOperationProductOutComponents != null) {
                for (Entity recordOperationProductOutComponent : recordOperationProductOutComponents) {
                    Entity balanceOperationProductOutComponent = dataDefinitionService.get(
                            ProductionCountingConstants.PLUGIN_IDENTIFIER,
                            ProductionCountingConstants.MODEL_BALANCE_OPERATION_PRODUCT_OUT_COMPONENT).create();

                    balanceOperationProductOutComponent.setField(MODEL_PRODUCTION_RECORD,
                            recordOperationProductOutComponent.getField(MODEL_PRODUCTION_RECORD));
                    balanceOperationProductOutComponent.setField(MODEL_PRODUCT,
                            recordOperationProductOutComponent.getField(PRODUCT));
                    balanceOperationProductOutComponent.setField(L_USED_QUANTITY,
                            recordOperationProductOutComponent.getField(USED_QUANTITY));
                    balanceOperationProductOutComponent.setField(L_PLANNED_QUANTITY,
                            recordOperationProductOutComponent.getField(PLANNED_QUANTITY));
                    balanceOperationProductOutComponent.setField(L_BALANCE, recordOperationProductOutComponent.getField(BALANCE));

                    balanceOperationProductOutComponents.add(balanceOperationProductOutComponent);
                }
            }
        }

        if (!balanceOperationProductOutComponents.isEmpty()) {
            Collections.sort(balanceOperationProductOutComponents, new EntityProductInOutComparator());

            productionBalance
                    .setField(BALANCE_OPERATION_PRODUCT_OUT_COMPONENTS, productionBalanceReportDataService
                            .groupProductInOutComponentsByProduct(balanceOperationProductOutComponents));

            productionBalance.getDataDefinition().save(productionBalance);
        }
    }

    private void fillTimeValues(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        BigDecimal plannedMachineTime = BigDecimal.ZERO;
        BigDecimal machineTime = BigDecimal.ZERO;

        BigDecimal laborTime = BigDecimal.ZERO;
        BigDecimal plannedLaborTime = BigDecimal.ZERO;

        List<Entity> productionRecordsList = getProductionRecordsFromDB(order);

        if (!productionRecordsList.isEmpty()) {
            for (Entity productionRecord : productionRecordsList) {
                plannedMachineTime = plannedMachineTime
                        .add(new BigDecimal((Integer) productionRecord.getField(PLANNED_MACHINE_TIME)),
                                numberService.getMathContext());
                plannedLaborTime = plannedLaborTime.add(new BigDecimal((Integer) productionRecord.getField(PLANNED_LABOR_TIME)),
                        numberService.getMathContext());

                machineTime = machineTime.add(new BigDecimal((Integer) productionRecord.getField(MACHINE_TIME)),
                        numberService.getMathContext());
                laborTime = laborTime.add(new BigDecimal((Integer) productionRecord.getField(LABOR_TIME)),
                        numberService.getMathContext());
            }
        }

        BigDecimal machineTimeBalance = machineTime.subtract(plannedMachineTime, numberService.getMathContext());
        BigDecimal laborTimeBalance = laborTime.subtract(plannedLaborTime, numberService.getMathContext());

        productionBalance.setField(PLANNED_MACHINE_TIME, plannedMachineTime);
        productionBalance.setField(MACHINE_TIME, machineTime);
        productionBalance.setField(MACHINE_TIME_BALANCE, machineTimeBalance);

        productionBalance.setField(PLANNED_LABOR_TIME, plannedLaborTime);
        productionBalance.setField(LABOR_TIME, laborTime);
        productionBalance.setField(LABOR_TIME_BALANCE, laborTimeBalance);

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private void fillOperationTimeComponents(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        List<Entity> operationTimeComponents = Lists.newArrayList();

        List<Entity> productionRecordsList = getProductionRecordsFromDB(order);

        if (!productionRecordsList.isEmpty()) {
            Collections.sort(productionRecordsList, new EntityProductionRecordOperationComparator());

            List<Entity> groupedProductionRecords = productionBalanceReportDataService
                    .groupProductionRecordsByOperation(productionRecordsList);

            if (!groupedProductionRecords.isEmpty()) {
                for (Entity productionRecord : groupedProductionRecords) {
                    Entity operationTimeComponent = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                            ProductionCountingConstants.MODEL_OPERATION_TIME_COMPONENT).create();

                    operationTimeComponent.setField(MODEL_ORDER_OPERATION_COMPONENT,
                            productionRecord.getBelongsToField(MODEL_ORDER_OPERATION_COMPONENT));

                    operationTimeComponent.setField(L_PLANNED_MACHINE_TIME, productionRecord.getField(PLANNED_MACHINE_TIME));
                    operationTimeComponent.setField(L_MACHINE_TIME, productionRecord.getField(MACHINE_TIME));
                    operationTimeComponent.setField(L_MACHINE_TIME_BALANCE, productionRecord.getField(MACHINE_TIME_BALANCE));

                    operationTimeComponent.setField(L_PLANNED_LABOR_TIME, productionRecord.getField(PLANNED_LABOR_TIME));
                    operationTimeComponent.setField(L_LABOR_TIME, productionRecord.getField(LABOR_TIME));
                    operationTimeComponent.setField(L_LABOR_TIME_BALANCE, productionRecord.getField(LABOR_TIME_BALANCE));

                    operationTimeComponents.add(operationTimeComponent);
                }
            }

        }
        productionBalance.setField(OPERATION_TIME_COMPONENTS, operationTimeComponents);

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private void fillOperationPieceworkComponents(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        List<Entity> operationPieceworkComponents = Lists.newArrayList();

        List<Entity> productionRecordsList = getProductionRecordsFromDB(order);

        Map<Entity, BigDecimal> operationRuns = Maps.newHashMap();

        Map<Entity, BigDecimal> productComponents = productQuantitiesService.getProductComponentQuantities(asList(order),
                operationRuns);

        if (!productionRecordsList.isEmpty() && !productComponents.isEmpty()) {
            Collections.sort(productionRecordsList, new EntityProductionRecordOperationComparator());

            List<Entity> groupedProductionRecordsList = productionBalanceReportDataService
                    .groupProductionRecordsByOperation(productionRecordsList);

            if (!groupedProductionRecordsList.isEmpty()) {
                for (Entity productionRecord : groupedProductionRecordsList) {
                    Entity operationPieceworkComponent = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                            ProductionCountingConstants.MODEL_OPERATION_PIECEWORK_COMPONENT).create();

                    operationPieceworkComponent.setField(MODEL_ORDER_OPERATION_COMPONENT,
                            productionRecord.getBelongsToField(MODEL_ORDER_OPERATION_COMPONENT));

                    Entity orderOperationComponent = productionRecord.getBelongsToField("orderOperationComponent");
                    Entity proxyTechnologyOperationComponent = orderOperationComponent
                            .getBelongsToField("technologyOperationComponent");
                    Long technologyOperationComponentId = proxyTechnologyOperationComponent.getId();

                    Entity technologyOperationComponent = getTechnologyOperationComponentFromDB(technologyOperationComponentId);

                    if ((technologyOperationComponent != null) && operationRuns.containsKey(technologyOperationComponent)) {
                        BigDecimal plannedCycles = operationRuns.get(technologyOperationComponent);

                        BigDecimal cycles = BigDecimal.ZERO;

                        if (productionRecord.getField("executedOperationCycles") != null) {
                            cycles = (BigDecimal) productionRecord.getField("executedOperationCycles");
                        }

                        BigDecimal cyclesBalance = cycles.subtract(plannedCycles, numberService.getMathContext());

                        operationPieceworkComponent.setField("plannedCycles", numberService.setScale(plannedCycles));
                        operationPieceworkComponent.setField("cycles", numberService.setScale(cycles));
                        operationPieceworkComponent.setField("cyclesBalance", numberService.setScale(cyclesBalance));

                        operationPieceworkComponents.add(operationPieceworkComponent);
                    }
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
