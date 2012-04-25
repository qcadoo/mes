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
package com.qcadoo.mes.productionCountingWithCosts;

import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_PRODUCT;
import static com.qcadoo.mes.costCalculation.constants.SourceOfMaterialCosts.CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT;
import static com.qcadoo.mes.costCalculation.constants.SourceOfMaterialCosts.FROM_ORDERS_MATERIAL_COSTS;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.CalculateOperationCostsMode.HOURLY;
import static com.qcadoo.mes.productionCounting.internal.constants.CalculateOperationCostsMode.PIECEWORK;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.DATE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_PRODUCTION_BALANCE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_REGISTER_PIECEWORK;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_REGISTER_PRODUCTION_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.CUMULATED;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.FOR_EACH;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.AVERAGE_LABOR_HOURLY_COST;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.AVERAGE_MACHINE_HOURLY_COST;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.BALANCE_TECHNICAL_PRODUCTION_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.BALANCE_TECHNICAL_PRODUCTION_COST_PER_UNIT;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.CALCULATE_MATERIAL_COSTS_MODE;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.COMPONENTS_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.CYCLES_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.CYCLES_COSTS_BALANCE;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.GENERATED_WITH_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.LABOR_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.MACHINE_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.OPERATION_COST_COMPONENTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.OPERATION_PIECEWORK_COST_COMPONENTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.PLANNED_COMPONENTS_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.PLANNED_CYCLES_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.QUANTITY;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.REGISTERED_TOTAL_TECHNICALPRODUCTION_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.REGISTERED_TOTAL_TECHNICAL_PRODUCTION_COST_PER_UNIT;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.SOURCE_OF_MATERIAL_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.TECHNOLOGY;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.TOTAL_OVERHEAD;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.TOTAL_TECHNICAL_PRODUCTION_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.TOTAL_TECHNICAL_PRODUCTION_COST_PER_UNIT;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lowagie.text.DocumentException;
import com.qcadoo.mes.costCalculation.CostCalculationService;
import com.qcadoo.mes.costNormsForMaterials.ProductsCostCalculationService;
import com.qcadoo.mes.costNormsForOperation.constants.CostNormsForOperationConstants;
import com.qcadoo.mes.productionCounting.internal.ProductionBalanceReportDataService;
import com.qcadoo.mes.productionCounting.internal.ProductionBalanceService;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductionRecordOperationComparator;
import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC;
import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionCountingWithCostsConstants;
import com.qcadoo.mes.productionCountingWithCosts.pdf.ProductionBalanceWithCostsPdfService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginUtils;

@Service
public class GenerateProductionBalanceWithCosts implements Observer {

    private static final String L_PRODUCTION_LINE = "productionLine";

    private static final String L_LABOR_HOURLY_COST = "laborHourlyCost";

    private static final String L_MACHINE_HOURLY_COST = "machineHourlyCost";

    private static final String L_BALANCE = "balance";

    private static final String L_REGISTERED_COST = "registeredCost";

    private static final String L_PLANNED_COST = "plannedCost";

    private static final String L_EXECUTED_OPERATION_CYCLES = "executedOperationCycles";

    private static final String L_PIECES = "pieces";

    private static final String L_OPERATION_COST = "operationCost";

    private static final String L_PLANNED_QUANTITY = "plannedQuantity";

    private static final String L_USED_QUANTITY = "usedQuantity";

    private static final String L_PLANNED_MACHINE_TIME = "plannedMachineTime";

    private static final String L_MACHINE_TIME = "machineTime";

    private static final String L_PLANNED_LABOR_TIME = "plannedLaborTime";

    private static final String L_LABOR_TIME = "laborTime";

    private static final String L_LABOR_COSTS_BALANCE = "laborCostsBalance";

    private static final String L_LABOR_COSTS = "laborCosts";

    private static final String L_PLANNED_LABOR_COSTS = "plannedLaborCosts";

    private static final String L_MACHINE_COSTS_BALANCE = "machineCostsBalance";

    private static final String L_MACHINE_COSTS = "machineCosts";

    private static final String L_PLANNED_MACHINE_COSTS = "plannedMachineCosts";

    @Autowired
    private NumberService numberService;

    @Autowired
    private CostCalculationService costCalculationService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ProductionBalanceWithCostsPdfService productionBalanceWithCostsPdfService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionBalanceService productionBalanceService;

    @Autowired
    private ProductsCostCalculationService productsCostCalculationService;

    @Autowired
    private ProductionBalanceReportDataService productionBalanceReportDataService;

    @Override
    public void update(final Observable arg0, final Object arg1) {
        // FIXME mici, well, since those observers are registered on plugin startup
        // they are registered for all tenants. Checking if the plugin is enabled seems like a viable workaround.
        // This problem also applies to other listeners, across the system, that are implemented using observer pattern.
        if (PluginUtils.isEnabled(ProductionCountingWithCostsConstants.PLUGIN_IDENTIFIER)) {
            Entity balance = (Entity) arg1;

            doTheCostsPart(balance);
            fillFieldsAndGrids(balance);
            generateBalanceWithCostsReport(balance);
        }
    }

    void generateBalanceWithCostsReport(final Entity productionBalance) {
        Locale locale = LocaleContextHolder.getLocale();

        String localePrefix = "productionCounting.productionBalanceWithCosts.report.fileName";

        Entity productionBalanceWithFileName = fileService.updateReportFileName(productionBalance, DATE, localePrefix);

        String localePrefixToMatch = localePrefix;

        Entity company = productionBalanceService.getCompanyFromDB();

        try {
            productionBalanceWithCostsPdfService.generateDocument(productionBalanceWithFileName, company, locale,
                    localePrefixToMatch);

            productionBalance.setField(GENERATED_WITH_COSTS, Boolean.TRUE);

            productionBalance.getDataDefinition().save(productionBalance);
        } catch (IOException e) {
            throw new IllegalStateException("Problem with saving productionBalanceWithCosts report");
        } catch (DocumentException e) {
            throw new IllegalStateException("Problem with generating productionBalanceWithCosts report");
        }
    }

    void doTheCostsPart(final Entity productionBalance) {
        Entity order = productionBalance.getBelongsToField(ORDER);
        Entity technology = order.getBelongsToField(TECHNOLOGY);
        Entity productionLine = order.getBelongsToField(L_PRODUCTION_LINE);

        BigDecimal quantity = (BigDecimal) order.getField(L_PLANNED_QUANTITY);
        productionBalance.setField(QUANTITY, quantity);
        productionBalance.setField(TECHNOLOGY, technology);
        productionBalance.setField(L_PRODUCTION_LINE, productionLine);
        costCalculationService.calculateTotalCost(productionBalance);

        BigDecimal totalTechnicalProductionCosts = (BigDecimal) productionBalance.getField(TOTAL_TECHNICAL_PRODUCTION_COSTS);
        BigDecimal perUnit = totalTechnicalProductionCosts.divide(quantity, numberService.getMathContext());
        productionBalance.setField(TOTAL_TECHNICAL_PRODUCTION_COST_PER_UNIT, numberService.setScale(perUnit));

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private void fillFieldsAndGrids(final Entity productionBalance) {
        Entity order = productionBalance.getBelongsToField(MODEL_ORDER);

        if ((order == null) || productionBalanceService.checkIfTypeOfProductionRecordingIsBasic(order)) {
            return;
        }

        fillMaterialValues(productionBalance, order);
        fillTechnologyInstOperProductInComps(productionBalance, order);

        if (HOURLY.getStringValue().equals(productionBalance.getStringField(CALCULATE_OPERATION_COST_MODE))
                && order.getBooleanField(PARAM_REGISTER_PRODUCTION_TIME)) {
            if (FOR_EACH.getStringValue().equals(order.getStringField(TYPE_OF_PRODUCTION_RECORDING))) {
                fillCostValues(productionBalance, order);
                fillOperationCostComponents(productionBalance, order);
            } else if (CUMULATED.getStringValue().equals(order.getStringField(TYPE_OF_PRODUCTION_RECORDING))) {
                fillCostValues(productionBalance, order);
            }
        } else if (PIECEWORK.getStringValue().equals(productionBalance.getStringField(CALCULATE_OPERATION_COST_MODE))
                && order.getBooleanField(PARAM_REGISTER_PIECEWORK)) {
            fillPieceworkCostValues(productionBalance, order);
            fillOperationPieceworkCostComponents(productionBalance, order);
        }

        sumarizeCostValues(productionBalance, order);
    }

    private void fillMaterialValues(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        BigDecimal plannedComponentsCosts = BigDecimal.ZERO;
        BigDecimal componentsCosts = BigDecimal.ZERO;

        Map<Entity, BigDecimal> productWithCosts = getPlannedProductsWithCosts(productionBalance, order);

        if (!productWithCosts.isEmpty()) {
            for (Entry<Entity, BigDecimal> productWithCost : productWithCosts.entrySet()) {
                Entity product = productWithCost.getKey();
                BigDecimal productCost = productWithCost.getValue();

                Entity balanceOperationProductInComponent = getBalanceOperationProductInComponentFromDB(productionBalance,
                        product);

                if (balanceOperationProductInComponent != null) {
                    BigDecimal registeredQuantity = (BigDecimal) balanceOperationProductInComponent.getField(L_USED_QUANTITY);

                    BigDecimal productRegisteredCost = BigDecimal.ZERO;

                    if (registeredQuantity != null) {
                        productRegisteredCost = getRegisteredProductWithCost(
                                productionBalance,
                                productsCostCalculationService.getAppropriateCostNormForProduct(product, order,
                                        productionBalance.getStringField(SOURCE_OF_MATERIAL_COSTS)), registeredQuantity);
                    }

                    plannedComponentsCosts = plannedComponentsCosts.add(productCost, numberService.getMathContext());

                    componentsCosts = componentsCosts.add(productRegisteredCost, numberService.getMathContext());
                }
            }

            BigDecimal componentsCostsBalance = componentsCosts.subtract(plannedComponentsCosts, numberService.getMathContext());

            productionBalance.setField(PLANNED_COMPONENTS_COSTS, numberService.setScale(plannedComponentsCosts));
            productionBalance.setField(COMPONENTS_COSTS, numberService.setScale(componentsCosts));

            productionBalance.setField(ProductionBalanceFieldsPCWC.COMPONENTS_COSTS_BALANCE,
                    numberService.setScale(componentsCostsBalance));

            productionBalance.getDataDefinition().save(productionBalance);
        }
    }

    private void fillTechnologyInstOperProductInComps(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        List<Entity> technologyInstOperProductInComps = Lists.newArrayList();

        Map<Entity, BigDecimal> productWithCosts = getPlannedProductsWithCosts(productionBalance, order);

        if (!productWithCosts.isEmpty()) {
            for (Entry<Entity, BigDecimal> productWithCost : productWithCosts.entrySet()) {
                Entity product = productWithCost.getKey();
                BigDecimal productCost = productWithCost.getValue();

                Entity balanceOperationProductInComponent = getBalanceOperationProductInComponentFromDB(productionBalance,
                        product);

                if (balanceOperationProductInComponent != null) {
                    Entity technologyInstOperProductInComp = dataDefinitionService.get(
                            ProductionCountingWithCostsConstants.PLUGIN_IDENTIFIER,
                            ProductionCountingWithCostsConstants.MODEL_TECHNOLOGY_INST_OPER_PRODUCT_IN_COMP).create();

                    BigDecimal registeredQuantity = (BigDecimal) balanceOperationProductInComponent.getField(L_USED_QUANTITY);

                    BigDecimal productRegisteredCost = BigDecimal.ZERO;

                    if (registeredQuantity != null) {
                        productRegisteredCost = getRegisteredProductWithCost(
                                productionBalance,
                                productsCostCalculationService.getAppropriateCostNormForProduct(product, order,
                                        productionBalance.getStringField(SOURCE_OF_MATERIAL_COSTS)), registeredQuantity);
                    }

                    BigDecimal balance = productRegisteredCost.subtract(productCost, numberService.getMathContext());

                    technologyInstOperProductInComp.setField(MODEL_PRODUCT, product);
                    technologyInstOperProductInComp.setField("plannedCost", numberService.setScale(productCost));
                    technologyInstOperProductInComp.setField("registeredCost", numberService.setScale(productRegisteredCost));
                    technologyInstOperProductInComp.setField("balance", numberService.setScale(balance));
                    technologyInstOperProductInComps.add(technologyInstOperProductInComp);
                }
            }

            productionBalance.setField(TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS, technologyInstOperProductInComps);

            productionBalance.getDataDefinition().save(productionBalance);
        }
    }

    private void fillCostValues(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        BigDecimal plannedMachineCosts = BigDecimal.ZERO;
        BigDecimal machineCosts = BigDecimal.ZERO;

        BigDecimal laborCosts = BigDecimal.ZERO;
        BigDecimal plannedLaborCosts = BigDecimal.ZERO;

        List<Entity> productionRecordsList = productionBalanceService.getProductionRecordsFromDB(order);

        if (!productionRecordsList.isEmpty()) {
            if (FOR_EACH.getStringValue().equals(order.getStringField(TYPE_OF_PRODUCTION_RECORDING))) {
                Collections.sort(productionRecordsList, new EntityProductionRecordOperationComparator());

                List<Entity> groupedProductionRecordsList = productionBalanceReportDataService
                        .groupProductionRecordsByOperation(productionRecordsList);

                for (Entity productionRecord : groupedProductionRecordsList) {
                    Entity calculationOperationComponent = getCalculationOperationComponent(productionBalance, productionRecord);

                    if (calculationOperationComponent != null) {
                        BigDecimal milisecondsInHour = BigDecimal.valueOf(3600);

                        BigDecimal machineHourlyCost = (BigDecimal) calculationOperationComponent.getField(L_MACHINE_HOURLY_COST);

                        Integer plannedMachineTime = (Integer) productionRecord.getField(L_PLANNED_MACHINE_TIME);
                        BigDecimal plannedMachineTimeHours = BigDecimal.valueOf(plannedMachineTime).divide(milisecondsInHour,
                                numberService.getMathContext());

                        plannedMachineCosts = plannedMachineCosts.add(
                                (BigDecimal) machineHourlyCost.multiply(plannedMachineTimeHours), numberService.getMathContext());

                        Integer machineTime = (Integer) productionRecord.getField(L_MACHINE_TIME);
                        BigDecimal machineTimeHours = BigDecimal.valueOf(machineTime).divide(milisecondsInHour,
                                numberService.getMathContext());

                        machineCosts = machineCosts.add((BigDecimal) machineHourlyCost.multiply(machineTimeHours),
                                numberService.getMathContext());

                        BigDecimal laborHourlyCost = (BigDecimal) calculationOperationComponent.getField(L_LABOR_HOURLY_COST);

                        Integer plannedLaborTime = (Integer) productionRecord.getField(L_PLANNED_LABOR_TIME);
                        BigDecimal plannedLaborTimeHours = BigDecimal.valueOf(plannedLaborTime).divide(milisecondsInHour,
                                numberService.getMathContext());

                        plannedLaborCosts = plannedLaborCosts.add((BigDecimal) laborHourlyCost.multiply(plannedLaborTimeHours),
                                numberService.getMathContext());

                        Integer laborTime = (Integer) productionRecord.getField(L_LABOR_TIME);
                        BigDecimal laborTimeHours = BigDecimal.valueOf(laborTime).divide(milisecondsInHour,
                                numberService.getMathContext());

                        laborCosts = laborCosts.add((BigDecimal) laborHourlyCost.multiply(laborTimeHours),
                                numberService.getMathContext());
                    }
                }
            } else if (CUMULATED.getStringValue().equals(order.getStringField(TYPE_OF_PRODUCTION_RECORDING))) {
                for (Entity productionRecord : productionRecordsList) {
                    BigDecimal milisecondsInHour = BigDecimal.valueOf(3600);

                    BigDecimal averageMachineHourlyCost = (BigDecimal) productionBalance.getField(AVERAGE_MACHINE_HOURLY_COST);

                    if (averageMachineHourlyCost == null) {
                        averageMachineHourlyCost = BigDecimal.ONE;
                    }

                    Integer plannedMachineTime = (Integer) productionRecord.getField(L_PLANNED_MACHINE_TIME);
                    BigDecimal plannedMachineTimeHours = BigDecimal.valueOf(plannedMachineTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    plannedMachineCosts = plannedMachineCosts.add(
                            (BigDecimal) averageMachineHourlyCost.multiply(plannedMachineTimeHours),
                            numberService.getMathContext());

                    Integer machineTime = (Integer) productionRecord.getField(L_MACHINE_TIME);
                    BigDecimal machineTimeHours = BigDecimal.valueOf(machineTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    machineCosts = machineCosts.add((BigDecimal) averageMachineHourlyCost.multiply(machineTimeHours),
                            numberService.getMathContext());

                    BigDecimal averageLaborHourlyCost = (BigDecimal) productionBalance.getField(AVERAGE_LABOR_HOURLY_COST);

                    if (averageLaborHourlyCost == null) {
                        averageLaborHourlyCost = BigDecimal.ONE;
                    }

                    Integer plannedLaborTime = (Integer) productionRecord.getField(L_PLANNED_LABOR_TIME);
                    BigDecimal plannedLaborTimeHours = BigDecimal.valueOf(plannedLaborTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    plannedLaborCosts = plannedLaborCosts.add(
                            (BigDecimal) averageLaborHourlyCost.multiply(plannedLaborTimeHours), numberService.getMathContext());

                    Integer laborTime = (Integer) productionRecord.getField(L_LABOR_TIME);
                    BigDecimal laborTimeHours = BigDecimal.valueOf(laborTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    laborCosts = laborCosts.add((BigDecimal) averageLaborHourlyCost.multiply(laborTimeHours),
                            numberService.getMathContext());
                }
            }
        }

        BigDecimal machineCostsBalance = machineCosts.subtract(plannedMachineCosts, numberService.getMathContext());
        BigDecimal laborCostsBalance = laborCosts.subtract(plannedLaborCosts, numberService.getMathContext());

        productionBalance.setField(L_PLANNED_MACHINE_COSTS, numberService.setScale(plannedMachineCosts));
        productionBalance.setField(L_MACHINE_COSTS, numberService.setScale(machineCosts));
        productionBalance.setField(L_MACHINE_COSTS_BALANCE, numberService.setScale(machineCostsBalance));

        productionBalance.setField(L_PLANNED_LABOR_COSTS, numberService.setScale(plannedLaborCosts));
        productionBalance.setField(L_LABOR_COSTS, numberService.setScale(laborCosts));
        productionBalance.setField(L_LABOR_COSTS_BALANCE, numberService.setScale(laborCostsBalance));

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private void fillOperationCostComponents(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        List<Entity> operationCostComponents = Lists.newArrayList();

        List<Entity> productionRecordsList = productionBalanceService.getProductionRecordsFromDB(order);

        if (!productionRecordsList.isEmpty()) {
            Collections.sort(productionRecordsList, new EntityProductionRecordOperationComparator());

            List<Entity> groupedProductionRecordsList = productionBalanceReportDataService
                    .groupProductionRecordsByOperation(productionRecordsList);

            for (Entity productionRecord : groupedProductionRecordsList) {
                Entity calculationOperationComponent = getCalculationOperationComponent(productionBalance, productionRecord);

                if (calculationOperationComponent != null) {
                    Entity operationCostComponent = dataDefinitionService.get(
                            ProductionCountingWithCostsConstants.PLUGIN_IDENTIFIER,
                            ProductionCountingWithCostsConstants.MODEL_OPERATION_COST_COMPONENT).create();

                    BigDecimal milisecondsInHour = BigDecimal.valueOf(3600);

                    BigDecimal machineHourlyCost = (BigDecimal) calculationOperationComponent.getField(L_MACHINE_HOURLY_COST);

                    Integer plannedMachineTime = (Integer) productionRecord.getField(L_PLANNED_MACHINE_TIME);
                    BigDecimal plannedMachineTimeHours = BigDecimal.valueOf(plannedMachineTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    BigDecimal plannedMachineCosts = (BigDecimal) machineHourlyCost.multiply(plannedMachineTimeHours);

                    Integer machineTime = (Integer) productionRecord.getField(L_MACHINE_TIME);
                    BigDecimal machineTimeHours = BigDecimal.valueOf(machineTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    BigDecimal machineCosts = (BigDecimal) machineHourlyCost.multiply(machineTimeHours);

                    BigDecimal machineCostsBalance = machineCosts.subtract(plannedMachineCosts, numberService.getMathContext());

                    BigDecimal laborHourlyCost = (BigDecimal) calculationOperationComponent.getField(L_LABOR_HOURLY_COST);

                    Integer plannedLaborTime = (Integer) productionRecord.getField(L_PLANNED_LABOR_TIME);
                    BigDecimal plannedLaborTimeHours = BigDecimal.valueOf(plannedLaborTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    BigDecimal plannedLaborCosts = (BigDecimal) laborHourlyCost.multiply(plannedLaborTimeHours);

                    Integer laborTime = (Integer) productionRecord.getField(L_LABOR_TIME);
                    BigDecimal laborTimeHours = BigDecimal.valueOf(laborTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    BigDecimal laborCosts = (BigDecimal) laborHourlyCost.multiply(laborTimeHours);

                    BigDecimal laborCostsBalance = laborCosts.subtract(plannedLaborCosts, numberService.getMathContext());

                    operationCostComponent.setField(MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT,
                            productionRecord.getBelongsToField(MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT));

                    operationCostComponent.setField(L_PLANNED_MACHINE_COSTS, numberService.setScale(plannedMachineCosts));
                    operationCostComponent.setField(L_MACHINE_COSTS, numberService.setScale(machineCosts));
                    operationCostComponent.setField(L_MACHINE_COSTS_BALANCE, numberService.setScale(machineCostsBalance));

                    operationCostComponent.setField(L_PLANNED_LABOR_COSTS, numberService.setScale(plannedLaborCosts));
                    operationCostComponent.setField(L_LABOR_COSTS, numberService.setScale(laborCosts));
                    operationCostComponent.setField(L_LABOR_COSTS_BALANCE, numberService.setScale(laborCostsBalance));

                    operationCostComponents.add(operationCostComponent);
                }
            }

        }

        productionBalance.setField(OPERATION_COST_COMPONENTS, operationCostComponents);

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private void fillPieceworkCostValues(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        BigDecimal plannedCyclesCosts = BigDecimal.ZERO;
        BigDecimal cyclesCosts = BigDecimal.ZERO;

        List<Entity> productionRecordsList = productionBalanceService.getProductionRecordsFromDB(order);

        List<Entity> groupedProductionRecordsList = productionBalanceReportDataService
                .groupProductionRecordsByOperation(productionRecordsList);

        if (!groupedProductionRecordsList.isEmpty()) {
            for (Entity productionRecord : groupedProductionRecordsList) {
                Entity calculationOperationComponent = getCalculationOperationComponent(productionBalance, productionRecord);

                if (calculationOperationComponent != null) {
                    plannedCyclesCosts = plannedCyclesCosts
                            .add((BigDecimal) calculationOperationComponent.getField(L_OPERATION_COST),
                                    numberService.getMathContext());
                    BigDecimal pieces = (BigDecimal) calculationOperationComponent.getField(L_PIECES);

                    BigDecimal cost = ((BigDecimal) calculationOperationComponent.getField(L_OPERATION_COST)).divide(pieces,
                            numberService.getMathContext());

                    if (productionRecord.getField(L_EXECUTED_OPERATION_CYCLES) != null) {
                        cyclesCosts = cyclesCosts.add(
                                cost.multiply((BigDecimal) productionRecord.getField(L_EXECUTED_OPERATION_CYCLES),
                                        numberService.getMathContext()), numberService.getMathContext());
                    }
                }
            }
        }

        BigDecimal cyclesCostsBalance = cyclesCosts.subtract(plannedCyclesCosts, numberService.getMathContext());

        productionBalance.setField(PLANNED_CYCLES_COSTS, numberService.setScale(plannedCyclesCosts));
        productionBalance.setField(CYCLES_COSTS, numberService.setScale(cyclesCosts));
        productionBalance.setField(CYCLES_COSTS_BALANCE, numberService.setScale(cyclesCostsBalance));

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private void fillOperationPieceworkCostComponents(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }

        List<Entity> operationPieceworkCostComponents = Lists.newArrayList();

        List<Entity> productionRecordsList = productionBalanceService.getProductionRecordsFromDB(order);

        if (!productionRecordsList.isEmpty()) {

            Collections.sort(productionRecordsList, new EntityProductionRecordOperationComparator());

            List<Entity> groupedProductionRecordsList = productionBalanceReportDataService
                    .groupProductionRecordsByOperation(productionRecordsList);

            if (!groupedProductionRecordsList.isEmpty()) {
                for (Entity productionRecord : groupedProductionRecordsList) {
                    Entity calculationOperationComponent = getCalculationOperationComponent(productionBalance, productionRecord);

                    if (calculationOperationComponent != null) {
                        BigDecimal plannedCyclesCosts = (BigDecimal) calculationOperationComponent.getField(L_OPERATION_COST);
                        BigDecimal pieces = (BigDecimal) calculationOperationComponent.getField(L_PIECES);

                        BigDecimal cost = ((BigDecimal) calculationOperationComponent.getField(L_OPERATION_COST)).divide(pieces,
                                numberService.getMathContext());

                        BigDecimal cyclesCosts = BigDecimal.ZERO;

                        if (productionRecord.getField(L_EXECUTED_OPERATION_CYCLES) != null) {
                            cyclesCosts = cost.multiply((BigDecimal) productionRecord.getField(L_EXECUTED_OPERATION_CYCLES),
                                    numberService.getMathContext());
                        }

                        BigDecimal cyclesCostsBalance = cyclesCosts.subtract(plannedCyclesCosts, numberService.getMathContext());

                        Entity operationPieceworkCostComponent = dataDefinitionService.get(
                                ProductionCountingWithCostsConstants.PLUGIN_IDENTIFIER,
                                ProductionCountingWithCostsConstants.MODEL_OPERATION_PIECEWORK_COST_COMPONENT).create();

                        operationPieceworkCostComponent.setField(MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT,
                                productionRecord.getBelongsToField(MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT));

                        operationPieceworkCostComponent
                                .setField("plannedCyclesCosts", numberService.setScale(plannedCyclesCosts));
                        operationPieceworkCostComponent.setField("cyclesCosts", numberService.setScale(cyclesCosts));
                        operationPieceworkCostComponent
                                .setField("cyclesCostsBalance", numberService.setScale(cyclesCostsBalance));

                        operationPieceworkCostComponents.add(operationPieceworkCostComponent);
                    }
                }
            }

        }

        productionBalance.setField(OPERATION_PIECEWORK_COST_COMPONENTS, operationPieceworkCostComponents);

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private void sumarizeCostValues(final Entity productionBalance, final Entity order) {
        if ((productionBalance == null) || (order == null)) {
            return;
        }
        BigDecimal registeredTotalTechnicalProductionCosts = BigDecimal.ZERO;

        registeredTotalTechnicalProductionCosts = registeredTotalTechnicalProductionCosts.add(
                (BigDecimal) productionBalance.getField(COMPONENTS_COSTS), numberService.getMathContext());

        if (HOURLY.getStringValue().equals(productionBalance.getStringField(CALCULATE_OPERATION_COST_MODE))
                && order.getBooleanField(PARAM_REGISTER_PRODUCTION_TIME)) {
            registeredTotalTechnicalProductionCosts = registeredTotalTechnicalProductionCosts.add(
                    (BigDecimal) productionBalance.getField(MACHINE_COSTS), numberService.getMathContext());
            registeredTotalTechnicalProductionCosts = registeredTotalTechnicalProductionCosts.add(
                    (BigDecimal) productionBalance.getField(LABOR_COSTS), numberService.getMathContext());
        } else if (PIECEWORK.getStringValue().equals(productionBalance.getStringField(CALCULATE_OPERATION_COST_MODE))
                && order.getBooleanField(PARAM_REGISTER_PIECEWORK)) {
            registeredTotalTechnicalProductionCosts = registeredTotalTechnicalProductionCosts.add(
                    (BigDecimal) productionBalance.getField(CYCLES_COSTS), numberService.getMathContext());
        }

        BigDecimal registeredTotalTechnicalProductionCostPerUnit = registeredTotalTechnicalProductionCosts.divide(
                (BigDecimal) productionBalance.getField(QUANTITY), numberService.getMathContext());

        BigDecimal balanceTechnicalProductionCosts = registeredTotalTechnicalProductionCosts.subtract(
                (BigDecimal) productionBalance.getField(TOTAL_TECHNICAL_PRODUCTION_COSTS), numberService.getMathContext());

        BigDecimal balanceTechnicalProductionCostPerUnit = registeredTotalTechnicalProductionCostPerUnit
                .subtract((BigDecimal) productionBalance.getField(TOTAL_TECHNICAL_PRODUCTION_COST_PER_UNIT),
                        numberService.getMathContext());

        productionBalance.setField(REGISTERED_TOTAL_TECHNICALPRODUCTION_COSTS,
                numberService.setScale(registeredTotalTechnicalProductionCosts));
        productionBalance.setField(REGISTERED_TOTAL_TECHNICAL_PRODUCTION_COST_PER_UNIT,
                numberService.setScale(registeredTotalTechnicalProductionCostPerUnit));
        productionBalance.setField(BALANCE_TECHNICAL_PRODUCTION_COSTS, numberService.setScale(balanceTechnicalProductionCosts));
        productionBalance.setField(BALANCE_TECHNICAL_PRODUCTION_COST_PER_UNIT,
                numberService.setScale(balanceTechnicalProductionCostPerUnit));

        BigDecimal totalCosts = registeredTotalTechnicalProductionCosts.add(
                (BigDecimal) productionBalance.getField(TOTAL_OVERHEAD), numberService.getMathContext());
        BigDecimal totalCostPerUnit = totalCosts.divide((BigDecimal) productionBalance.getField(QUANTITY),
                numberService.getMathContext());

        productionBalance.setField(ProductionBalanceFieldsPCWC.TOTAL_COSTS, numberService.setScale(totalCosts));
        productionBalance.setField(ProductionBalanceFieldsPCWC.TOTAL_COST_PER_UNIT, numberService.setScale(totalCostPerUnit));

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private Map<Entity, BigDecimal> getPlannedProductsWithCosts(final Entity productionBalance, final Entity order) {
        BigDecimal givenQty = (BigDecimal) productionBalance.getField(QUANTITY);

        String sourceOfMaterialCosts = productionBalance.getStringField(SOURCE_OF_MATERIAL_COSTS);

        String calculateMaterialCostsMode = productionBalance.getStringField(CALCULATE_MATERIAL_COSTS_MODE);

        if (FROM_ORDERS_MATERIAL_COSTS.getStringValue().equals(sourceOfMaterialCosts)) {
            return productsCostCalculationService.getProductWithCostForPlannedQuantities(
                    productionBalance.getBelongsToField(TECHNOLOGY), givenQty, calculateMaterialCostsMode, order);
        } else if (CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT.getStringValue().equals(sourceOfMaterialCosts)) {
            return productsCostCalculationService.getProductWithCostForPlannedQuantities(
                    productionBalance.getBelongsToField(TECHNOLOGY), givenQty, calculateMaterialCostsMode);
        }

        return Maps.newHashMap();
    }

    private BigDecimal getRegisteredProductWithCost(final Entity productionBalance, final Entity product,
            final BigDecimal registeredQuantity) {
        String calculateMaterialCostsMode = productionBalance.getStringField(CALCULATE_MATERIAL_COSTS_MODE);

        return productsCostCalculationService.calculateProductCostForGivenQuantity(product, registeredQuantity,
                calculateMaterialCostsMode);

    }

    private Entity getBalanceOperationProductInComponentFromDB(final Entity productionBalance, final Entity product) {
        return dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                        ProductionCountingConstants.MODEL_BALANCE_OPERATION_PRODUCT_IN_COMPONENT).find()
                .add(SearchRestrictions.belongsTo(MODEL_PRODUCTION_BALANCE, productionBalance))
                .add(SearchRestrictions.belongsTo(MODEL_PRODUCT, product)).setMaxResults(1).uniqueResult();
    }

    private Entity getCalculationOperationComponent(final Entity productionBalance, final Entity operatonTimeComponent) {
        return dataDefinitionService
                .get(CostNormsForOperationConstants.PLUGIN_IDENTIFIER,
                        CostNormsForOperationConstants.MODEL_CALCULATION_OPERATION_COMPONENT)
                .find()
                .add(SearchRestrictions.belongsTo(MODEL_PRODUCTION_BALANCE, productionBalance))
                .add(SearchRestrictions.belongsTo(
                        MODEL_TECHNOLOGY_OPERATION_COMPONENT,
                        operatonTimeComponent.getBelongsToField(MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT).getBelongsToField(
                                MODEL_TECHNOLOGY_OPERATION_COMPONENT))).setMaxResults(1).uniqueResult();
    }

}
