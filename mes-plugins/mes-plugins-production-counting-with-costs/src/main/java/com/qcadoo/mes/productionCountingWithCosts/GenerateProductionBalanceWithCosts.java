/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
import static com.qcadoo.mes.costCalculation.constants.CostCalculationFields.TOTAL_PIECEWORK_COSTS;
import static com.qcadoo.mes.costCalculation.constants.SourceOfMaterialCosts.CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT;
import static com.qcadoo.mes.costCalculation.constants.SourceOfMaterialCosts.FROM_ORDERS_MATERIAL_COSTS;
import static com.qcadoo.mes.costNormsForOperation.constants.CalculationOperationComponentFields.LABOR_HOURLY_COST;
import static com.qcadoo.mes.costNormsForOperation.constants.CalculationOperationComponentFields.MACHINE_HOURLY_COST;
import static com.qcadoo.mes.costNormsForOperation.constants.CalculationOperationComponentFields.OPERATION_COST;
import static com.qcadoo.mes.costNormsForOperation.constants.CalculationOperationComponentFields.PIECES;
import static com.qcadoo.mes.orders.constants.OrderFields.PLANNED_QUANTITY;
import static com.qcadoo.mes.orders.constants.OrderFields.PRODUCTION_LINE;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.BalanceOperationProductInComponentFields.USED_QUANTITY;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_PIECEWORK;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_PRODUCTION_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.DATE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.MODEL_PRODUCTION_BALANCE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.EXECUTED_OPERATION_CYCLES;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.LABOR_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.MACHINE_TIME;
import static com.qcadoo.mes.productionCountingWithCosts.constants.OperationCostComponentFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT;
import static com.qcadoo.mes.productionCountingWithCosts.constants.OperationPieceworkCostComponentFields.CYCLES_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.OperationPieceworkCostComponentFields.CYCLES_COSTS_BALANCE;
import static com.qcadoo.mes.productionCountingWithCosts.constants.OperationPieceworkCostComponentFields.PLANNED_CYCLES_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.AVERAGE_LABOR_HOURLY_COST;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.AVERAGE_MACHINE_HOURLY_COST;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.BALANCE_TECHNICAL_PRODUCTION_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.CALCULATE_MATERIAL_COSTS_MODE;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.COMPONENTS_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.GENERATED_WITH_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.LABOR_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.LABOR_COSTS_BALANCE;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.MACHINE_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.MACHINE_COSTS_BALANCE;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.OPERATION_COST_COMPONENTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.OPERATION_PIECEWORK_COST_COMPONENTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.PLANNED_COMPONENTS_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.PLANNED_LABOR_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.PLANNED_MACHINE_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.QUANTITY;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.REGISTERED_TOTAL_TECHNICALPRODUCTION_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.SOURCE_OF_MATERIAL_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.TECHNOLOGY;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.TOTAL_OVERHEAD;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.TOTAL_TECHNICAL_PRODUCTION_COSTS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.TOTAL_TECHNICAL_PRODUCTION_COST_PER_UNIT;
import static com.qcadoo.mes.productionCountingWithCosts.constants.TechnologyInstOperProductInCompFields.BALANCE;
import static com.qcadoo.mes.productionCountingWithCosts.constants.TechnologyInstOperProductInCompFields.PLANNED_COST;
import static com.qcadoo.mes.productionCountingWithCosts.constants.TechnologyInstOperProductInCompFields.PRODUCT;
import static com.qcadoo.mes.productionCountingWithCosts.constants.TechnologyInstOperProductInCompFields.REGISTERED_COST;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
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
import com.qcadoo.mes.basic.CompanyService;
import com.qcadoo.mes.costCalculation.CostCalculationService;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costNormsForMaterials.ProductsCostCalculationService;
import com.qcadoo.mes.costNormsForOperation.constants.CostNormsForOperationConstants;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.internal.ProductionBalanceService;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC;
import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionCountingWithCostsConstants;
import com.qcadoo.mes.productionCountingWithCosts.materials.RegisteredMaterialCostHelper;
import com.qcadoo.mes.productionCountingWithCosts.operations.RegisteredProductionCostHelper;
import com.qcadoo.mes.productionCountingWithCosts.pdf.ProductionBalanceWithCostsPdfService;
import com.qcadoo.mes.productionCountingWithCosts.util.DecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginUtils;

@Service
public class GenerateProductionBalanceWithCosts implements Observer {

    private static final String L_PLANNED_MACHINE_TIME = "plannedMachineTime";

    private static final String L_PLANNED_LABOR_TIME = "plannedLaborTime";

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
    private RegisteredMaterialCostHelper registeredMaterialCostHelper;

    @Autowired
    private RegisteredProductionCostHelper registeredProductionCostHelper;

    @Autowired
    private CompanyService companyService;

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

        Entity company = companyService.getCompany();

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
        Entity productionLine = order.getBelongsToField(PRODUCTION_LINE);

        BigDecimal quantity = order.getDecimalField(PLANNED_QUANTITY);

        productionBalance.setField(QUANTITY, quantity);
        productionBalance.setField(TECHNOLOGY, technology);
        productionBalance.setField(PRODUCTION_LINE, productionLine);

        // FIXME MAKU beware of side effects - order of below computations matter!
        costCalculationService.calculateOperationsAndProductsCosts(productionBalance);
        final BigDecimal productionCosts = costCalculationService.calculateProductionCost(productionBalance);
        final BigDecimal doneQuantity = order.getDecimalField(OrderFields.DONE_QUANTITY);
        costCalculationService.calculateTotalCosts(productionBalance, productionCosts, doneQuantity);

        BigDecimal totalTechnicalProductionCosts = productionBalance.getDecimalField(TOTAL_TECHNICAL_PRODUCTION_COSTS);
        BigDecimal perUnit = totalTechnicalProductionCosts.divide(quantity, numberService.getMathContext());

        productionBalance.setField(TOTAL_TECHNICAL_PRODUCTION_COST_PER_UNIT, numberService.setScale(perUnit));

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private void fillFieldsAndGrids(final Entity productionBalance) {
        Entity order = productionBalance.getBelongsToField(MODEL_ORDER);

        if ((order == null) || productionBalanceService.isTypeOfProductionRecordingBasic(order)) {
            return;
        }

        List<Entity> productionRecords = productionBalanceService.getProductionRecordsFromDB(order);

        Map<Long, Entity> productionRecordsWithRegisteredTimes = productionBalanceService.groupProductionRecordsRegisteredTimes(
                productionBalance, productionRecords);

        Map<Entity, BigDecimal> productWithCosts = getPlannedProductsWithCosts(productionBalance, order);

        fillMaterialValues(productionBalance, productWithCosts);
        fillTechnologyInstOperProductInComps(productionBalance, productWithCosts);

        if (productionBalanceService.isCalculateOperationCostModeHourly(productionBalance)
                && order.getBooleanField(REGISTER_PRODUCTION_TIME)) {
            Map<Long, Map<String, Integer>> productionRecordsWithPlannedTimes = productionBalanceService
                    .fillProductionRecordsWithPlannedTimes(productionBalance, productionRecords);

            if (productionBalanceService.isTypeOfProductionRecordingForEach(order)) {
                fillCostValues(productionBalance, productionRecordsWithRegisteredTimes, productionRecordsWithPlannedTimes);
                fillOperationCostComponents(productionBalance, productionRecordsWithRegisteredTimes,
                        productionRecordsWithPlannedTimes);
            } else if (productionBalanceService.isTypeOfProductionRecordingCumulated(order)) {
                fillCostValues(productionBalance, productionRecordsWithRegisteredTimes, productionRecordsWithPlannedTimes);
            }
        } else if (productionBalanceService.isCalculateOperationCostModePiecework(productionBalance)
                && order.getBooleanField(REGISTER_PIECEWORK)) {
            fillPieceworkCostValues(productionBalance, productionRecordsWithRegisteredTimes);
            fillOperationPieceworkCostComponents(productionBalance, productionRecordsWithRegisteredTimes);
        }

        sumarizeCostValues(productionBalance, order);
    }

    private void fillMaterialValues(final Entity productionBalance, final Map<Entity, BigDecimal> productWithCosts) {
        if (productionBalance == null) {
            return;
        }

        BigDecimal componentsCosts = BigDecimal.ZERO;

        for (Entry<Entity, BigDecimal> productWithCost : productWithCosts.entrySet()) {
            Entity product = productWithCost.getKey();
            Entity balanceOperationProductInComponent = getBalanceOperationProductInComponentFromDB(productionBalance, product);

            if (balanceOperationProductInComponent != null) {
                BigDecimal registeredQuantity = balanceOperationProductInComponent.getDecimalField(USED_QUANTITY);

                BigDecimal productRegisteredCost = BigDecimal.ZERO;

                if (registeredQuantity != null) {
                    productRegisteredCost = getRegisteredProductWithCost(
                            productionBalance,
                            productsCostCalculationService.getAppropriateCostNormForProduct(product,
                                    productionBalance.getBelongsToField(ORDER),
                                    productionBalance.getStringField(SOURCE_OF_MATERIAL_COSTS)), registeredQuantity);
                }
                componentsCosts = componentsCosts.add(productRegisteredCost, numberService.getMathContext());
            }
        }

        final BigDecimal plannedComponentsCosts = DecimalUtils.nullToZero(productionBalance
                .getDecimalField(CostCalculationFields.TOTAL_MATERIAL_COSTS));
        BigDecimal componentsCostsBalance = componentsCosts.subtract(plannedComponentsCosts, numberService.getMathContext());

        productionBalance.setField(PLANNED_COMPONENTS_COSTS, numberService.setScale(plannedComponentsCosts));
        productionBalance.setField(COMPONENTS_COSTS, numberService.setScale(componentsCosts));
        productionBalance.setField(ProductionBalanceFieldsPCWC.COMPONENTS_COSTS_BALANCE,
                numberService.setScale(componentsCostsBalance));

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private void fillTechnologyInstOperProductInComps(final Entity productionBalance,
            final Map<Entity, BigDecimal> productWithCosts) {
        if (productionBalance == null) {
            return;
        }

        List<Entity> technologyInstOperProductInComps = Lists.newArrayList();

        for (Entry<Entity, BigDecimal> productWithCost : productWithCosts.entrySet()) {
            Entity product = productWithCost.getKey();
            BigDecimal productCost = productWithCost.getValue();

            Entity balanceOperationProductInComponent = getBalanceOperationProductInComponentFromDB(productionBalance, product);

            if (balanceOperationProductInComponent != null) {
                Entity technologyInstOperProductInComp = dataDefinitionService.get(
                        ProductionCountingWithCostsConstants.PLUGIN_IDENTIFIER,
                        ProductionCountingWithCostsConstants.MODEL_TECHNOLOGY_INST_OPER_PRODUCT_IN_COMP).create();

                BigDecimal registeredQuantity = balanceOperationProductInComponent.getDecimalField(USED_QUANTITY);

                BigDecimal productRegisteredCost = BigDecimal.ZERO;

                if (registeredQuantity != null) {
                    productRegisteredCost = getRegisteredProductWithCost(
                            productionBalance,
                            productsCostCalculationService.getAppropriateCostNormForProduct(product,
                                    productionBalance.getBelongsToField(ORDER),
                                    productionBalance.getStringField(SOURCE_OF_MATERIAL_COSTS)), registeredQuantity);
                }

                BigDecimal balance = productRegisteredCost.subtract(productCost, numberService.getMathContext());

                technologyInstOperProductInComp.setField(PRODUCT, product);

                technologyInstOperProductInComp.setField(PLANNED_COST, numberService.setScale(productCost));
                technologyInstOperProductInComp.setField(REGISTERED_COST, numberService.setScale(productRegisteredCost));
                technologyInstOperProductInComp.setField(BALANCE, numberService.setScale(balance));

                technologyInstOperProductInComps.add(technologyInstOperProductInComp);
            }
        }

        productionBalance.setField(TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS, technologyInstOperProductInComps);

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private void fillCostValues(final Entity productionBalance, final Map<Long, Entity> productionRecordsWithRegisteredTimes,
            final Map<Long, Map<String, Integer>> productionRecordsWithPlannedTimes) {
        if (productionBalance == null) {
            return;
        }
        Map<String, BigDecimal> costs = new HashMap<String, BigDecimal>();
        Entity order = productionBalance.getBelongsToField(ORDER);

        if (!productionRecordsWithPlannedTimes.isEmpty()) {
            if (productionBalanceService.isTypeOfProductionRecordingForEach(order)) {
                costs = costValueForTypeOfProductionRecordingForEach(productionBalance, productionRecordsWithRegisteredTimes);
            } else if (productionBalanceService.isTypeOfProductionRecordingCumulated(order)) {
                costs = costValueForTypeOfProductionRecordingCumulated(productionBalance, productionRecordsWithRegisteredTimes);
            }
        }

        final BigDecimal plannedMachineCosts = DecimalUtils.nullToZero(productionBalance
                .getDecimalField(CostCalculationFields.TOTAL_MACHINE_HOURLY_COSTS));
        final BigDecimal plannedLaborCosts = DecimalUtils.nullToZero(productionBalance
                .getDecimalField(CostCalculationFields.TOTAL_LABOR_HOURLY_COSTS));
        final BigDecimal machineCosts = costs.get("machineCosts");
        final BigDecimal laborCosts = costs.get("laborCosts");

        final BigDecimal machineCostsBalance = machineCosts.subtract(plannedMachineCosts, numberService.getMathContext());
        final BigDecimal laborCostsBalance = laborCosts.subtract(plannedLaborCosts, numberService.getMathContext());

        productionBalance.setField(PLANNED_MACHINE_COSTS, numberService.setScale(plannedMachineCosts));
        productionBalance.setField(MACHINE_COSTS, numberService.setScale(machineCosts));
        productionBalance.setField(MACHINE_COSTS_BALANCE, numberService.setScale(machineCostsBalance));

        productionBalance.setField(PLANNED_LABOR_COSTS, numberService.setScale(plannedLaborCosts));
        productionBalance.setField(LABOR_COSTS, numberService.setScale(laborCosts));
        productionBalance.setField(LABOR_COSTS_BALANCE, numberService.setScale(laborCostsBalance));

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private Map<String, BigDecimal> costValueForTypeOfProductionRecordingForEach(final Entity productionBalance,
            final Map<Long, Entity> productionRecordsWithRegisteredTimes) {
        Map<String, BigDecimal> costsValues = new HashMap<String, BigDecimal>();
        BigDecimal machineCosts = BigDecimal.ZERO;

        BigDecimal laborCosts = BigDecimal.ZERO;
        for (Map.Entry<Long, Entity> productionRecordWithRegisteredTimesEntry : productionRecordsWithRegisteredTimes.entrySet()) {
            Entity productionRecordWithRegisteredTimes = productionRecordWithRegisteredTimesEntry.getValue();

            Entity calculationOperationComponent = getCalculationOperationComponent(productionBalance,
                    productionRecordWithRegisteredTimes);

            if (calculationOperationComponent != null) {
                BigDecimal milisecondsInHour = BigDecimal.valueOf(3600);

                BigDecimal machineHourlyCost = getNotNullBigDecimal(calculationOperationComponent
                        .getDecimalField(MACHINE_HOURLY_COST));

                Integer machineTime = (Integer) productionRecordWithRegisteredTimes.getField(MACHINE_TIME);
                BigDecimal machineTimeHours = BigDecimal.valueOf(machineTime).divide(milisecondsInHour,
                        numberService.getMathContext());

                machineCosts = machineCosts.add(machineHourlyCost.multiply(machineTimeHours, numberService.getMathContext()),
                        numberService.getMathContext());

                BigDecimal laborHourlyCost = getNotNullBigDecimal(calculationOperationComponent
                        .getDecimalField(LABOR_HOURLY_COST));

                Integer laborTime = (Integer) productionRecordWithRegisteredTimes.getField(LABOR_TIME);
                BigDecimal laborTimeHours = BigDecimal.valueOf(laborTime).divide(milisecondsInHour,
                        numberService.getMathContext());

                laborCosts = laborCosts.add(laborHourlyCost.multiply(laborTimeHours, numberService.getMathContext()),
                        numberService.getMathContext());
            }
        }

        costsValues.put("machineCosts", machineCosts);
        costsValues.put("laborCosts", laborCosts);

        return costsValues;
    }

    private Map<String, BigDecimal> costValueForTypeOfProductionRecordingCumulated(final Entity productionBalance,
            final Map<Long, Entity> productionRecordsWithRegisteredTimes) {
        Map<String, BigDecimal> costsValues = new HashMap<String, BigDecimal>();
        BigDecimal machineCosts = BigDecimal.ZERO;

        BigDecimal laborCosts = BigDecimal.ZERO;
        for (Map.Entry<Long, Entity> productionRecordWithRegisteredTimesEntry : productionRecordsWithRegisteredTimes.entrySet()) {
            Entity productionRecordWithRegisteredTimes = productionRecordWithRegisteredTimesEntry.getValue();

            BigDecimal milisecondsInHour = BigDecimal.valueOf(3600);

            BigDecimal averageMachineHourlyCost = getNotNullBigDecimal(productionBalance
                    .getDecimalField(AVERAGE_MACHINE_HOURLY_COST));

            Integer machineTime = (Integer) productionRecordWithRegisteredTimes.getField(MACHINE_TIME);
            BigDecimal machineTimeHours = BigDecimal.valueOf(machineTime).divide(milisecondsInHour,
                    numberService.getMathContext());

            machineCosts = machineCosts.add(averageMachineHourlyCost.multiply(machineTimeHours, numberService.getMathContext()),
                    numberService.getMathContext());

            BigDecimal averageLaborHourlyCost = getNotNullBigDecimal(productionBalance.getDecimalField(AVERAGE_LABOR_HOURLY_COST));

            Integer laborTime = (Integer) productionRecordWithRegisteredTimes.getField(LABOR_TIME);
            BigDecimal laborTimeHours = BigDecimal.valueOf(laborTime).divide(milisecondsInHour, numberService.getMathContext());

            laborCosts = laborCosts.add(averageLaborHourlyCost.multiply(laborTimeHours, numberService.getMathContext()),
                    numberService.getMathContext());
        }

        costsValues.put("machineCosts", machineCosts);
        costsValues.put("laborCosts", laborCosts);

        return costsValues;
    }

    private void fillOperationCostComponents(final Entity productionBalance,
            final Map<Long, Entity> productionRecordsWithRegisteredTimes,
            final Map<Long, Map<String, Integer>> productionRecordsWithPlannedTimes) {
        if (productionBalance == null) {
            return;
        }

        List<Entity> operationCostComponents = Lists.newArrayList();

        if (!productionRecordsWithPlannedTimes.isEmpty()) {
            for (Map.Entry<Long, Entity> productionRecordWithRegisteredTimesEntry : productionRecordsWithRegisteredTimes
                    .entrySet()) {
                Long technologyInstanceOperationComponentId = productionRecordWithRegisteredTimesEntry.getKey();
                Entity productionRecordWithRegisteredTimes = productionRecordWithRegisteredTimesEntry.getValue();

                Entity calculationOperationComponent = getCalculationOperationComponent(productionBalance,
                        productionRecordWithRegisteredTimes);

                if (calculationOperationComponent != null) {
                    Entity operationCostComponent = dataDefinitionService.get(
                            ProductionCountingWithCostsConstants.PLUGIN_IDENTIFIER,
                            ProductionCountingWithCostsConstants.MODEL_OPERATION_COST_COMPONENT).create();

                    BigDecimal milisecondsInHour = BigDecimal.valueOf(3600);

                    BigDecimal machineHourlyCost = getNotNullBigDecimal(calculationOperationComponent
                            .getDecimalField(MACHINE_HOURLY_COST));

                    Integer plannedMachineTime = productionRecordsWithPlannedTimes.get(technologyInstanceOperationComponentId)
                            .get(L_PLANNED_MACHINE_TIME);
                    BigDecimal plannedMachineTimeHours = BigDecimal.valueOf(plannedMachineTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    BigDecimal plannedMachineCosts = machineHourlyCost.multiply(plannedMachineTimeHours,
                            numberService.getMathContext());

                    Integer machineTime = (Integer) productionRecordWithRegisteredTimes.getField(MACHINE_TIME);
                    BigDecimal machineTimeHours = BigDecimal.valueOf(machineTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    BigDecimal machineCosts = machineHourlyCost.multiply(machineTimeHours, numberService.getMathContext());

                    BigDecimal machineCostsBalance = machineCosts.subtract(plannedMachineCosts, numberService.getMathContext());

                    BigDecimal laborHourlyCost = getNotNullBigDecimal(calculationOperationComponent
                            .getDecimalField(LABOR_HOURLY_COST));

                    Integer plannedLaborTime = productionRecordsWithPlannedTimes.get(technologyInstanceOperationComponentId).get(
                            L_PLANNED_LABOR_TIME);
                    BigDecimal plannedLaborTimeHours = BigDecimal.valueOf(plannedLaborTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    BigDecimal plannedLaborCosts = laborHourlyCost
                            .multiply(plannedLaborTimeHours, numberService.getMathContext());

                    Integer laborTime = (Integer) productionRecordWithRegisteredTimes.getField(LABOR_TIME);
                    BigDecimal laborTimeHours = BigDecimal.valueOf(laborTime).divide(milisecondsInHour,
                            numberService.getMathContext());

                    BigDecimal laborCosts = laborHourlyCost.multiply(laborTimeHours, numberService.getMathContext());

                    BigDecimal laborCostsBalance = laborCosts.subtract(plannedLaborCosts, numberService.getMathContext());

                    operationCostComponent.setField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT,
                            productionRecordWithRegisteredTimes.getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT));

                    operationCostComponent.setField(PLANNED_MACHINE_COSTS, numberService.setScale(plannedMachineCosts));
                    operationCostComponent.setField(MACHINE_COSTS, numberService.setScale(machineCosts));
                    operationCostComponent.setField(MACHINE_COSTS_BALANCE, numberService.setScale(machineCostsBalance));

                    operationCostComponent.setField(PLANNED_LABOR_COSTS, numberService.setScale(plannedLaborCosts));
                    operationCostComponent.setField(LABOR_COSTS, numberService.setScale(laborCosts));
                    operationCostComponent.setField(LABOR_COSTS_BALANCE, numberService.setScale(laborCostsBalance));

                    operationCostComponents.add(operationCostComponent);
                }
            }

        }

        productionBalance.setField(OPERATION_COST_COMPONENTS, operationCostComponents);

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private void fillPieceworkCostValues(final Entity productionBalance,
            final Map<Long, Entity> productionRecordsWithRegisteredTimes) {
        if (productionBalance == null) {
            return;
        }

        BigDecimal cyclesCosts = BigDecimal.ZERO;

        for (Map.Entry<Long, Entity> productionRecordWithRegisteredTimesEntry : productionRecordsWithRegisteredTimes.entrySet()) {
            Entity productionRecordWithRegisteredTimes = productionRecordWithRegisteredTimesEntry.getValue();

            Entity calculationOperationComponent = getCalculationOperationComponent(productionBalance,
                    productionRecordWithRegisteredTimes);

            if (calculationOperationComponent != null) {
                final BigDecimal pieces = convertNullToOne(calculationOperationComponent.getDecimalField(PIECES));

                final BigDecimal cost = getNotNullBigDecimal(calculationOperationComponent.getDecimalField(OPERATION_COST))
                        .divide(pieces, numberService.getMathContext());

                if (productionRecordWithRegisteredTimes.getField(EXECUTED_OPERATION_CYCLES) != null) {
                    cyclesCosts = cyclesCosts.add(cost.multiply(
                            productionRecordWithRegisteredTimes.getDecimalField(EXECUTED_OPERATION_CYCLES),
                            numberService.getMathContext()), numberService.getMathContext());
                }
            }
        }

        final BigDecimal plannedCyclesCosts = getNotNullBigDecimal(productionBalance.getDecimalField(TOTAL_PIECEWORK_COSTS));
        final BigDecimal cyclesCostsBalance = cyclesCosts.subtract(plannedCyclesCosts, numberService.getMathContext());

        productionBalance.setField(PLANNED_CYCLES_COSTS, numberService.setScale(plannedCyclesCosts));
        productionBalance.setField(CYCLES_COSTS, numberService.setScale(cyclesCosts));
        productionBalance.setField(CYCLES_COSTS_BALANCE, numberService.setScale(cyclesCostsBalance));

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private void fillOperationPieceworkCostComponents(final Entity productionBalance,
            final Map<Long, Entity> productionRecordsWithRegisteredTimes) {
        if (productionBalance == null) {
            return;
        }

        List<Entity> operationPieceworkCostComponents = Lists.newArrayList();

        for (Map.Entry<Long, Entity> productionRecordWithRegisteredTimesEntry : productionRecordsWithRegisteredTimes.entrySet()) {
            Entity productionRecordWithRegisteredTimes = productionRecordWithRegisteredTimesEntry.getValue();

            Entity calculationOperationComponent = getCalculationOperationComponent(productionBalance,
                    productionRecordWithRegisteredTimes);

            if (calculationOperationComponent != null) {
                final BigDecimal plannedCyclesCosts = getNotNullBigDecimal(calculationOperationComponent
                        .getDecimalField(OPERATION_COST));
                final BigDecimal pieces = convertNullToOne(calculationOperationComponent.getDecimalField(PIECES));

                final BigDecimal cost = getNotNullBigDecimal(calculationOperationComponent.getDecimalField(OPERATION_COST))
                        .divide(pieces, numberService.getMathContext());

                BigDecimal cyclesCosts = BigDecimal.ZERO;

                if (productionRecordWithRegisteredTimes.getField(EXECUTED_OPERATION_CYCLES) != null) {
                    cyclesCosts = cost.multiply(productionRecordWithRegisteredTimes.getDecimalField(EXECUTED_OPERATION_CYCLES),
                            numberService.getMathContext());
                }

                BigDecimal cyclesCostsBalance = cyclesCosts.subtract(plannedCyclesCosts, numberService.getMathContext());

                Entity operationPieceworkCostComponent = dataDefinitionService.get(
                        ProductionCountingWithCostsConstants.PLUGIN_IDENTIFIER,
                        ProductionCountingWithCostsConstants.MODEL_OPERATION_PIECEWORK_COST_COMPONENT).create();

                operationPieceworkCostComponent.setField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT,
                        productionRecordWithRegisteredTimes.getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT));

                operationPieceworkCostComponent.setField(PLANNED_CYCLES_COSTS, numberService.setScale(plannedCyclesCosts));
                operationPieceworkCostComponent.setField(CYCLES_COSTS, numberService.setScale(cyclesCosts));
                operationPieceworkCostComponent.setField(CYCLES_COSTS_BALANCE, numberService.setScale(cyclesCostsBalance));

                operationPieceworkCostComponents.add(operationPieceworkCostComponent);
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
                productionBalance.getDecimalField(COMPONENTS_COSTS), numberService.getMathContext());

        if (productionBalanceService.isCalculateOperationCostModeHourly(productionBalance)
                && order.getBooleanField(REGISTER_PRODUCTION_TIME)) {
            registeredTotalTechnicalProductionCosts = registeredTotalTechnicalProductionCosts.add(
                    productionBalance.getDecimalField(MACHINE_COSTS), numberService.getMathContext());
            registeredTotalTechnicalProductionCosts = registeredTotalTechnicalProductionCosts.add(
                    productionBalance.getDecimalField(LABOR_COSTS), numberService.getMathContext());
        } else if (productionBalanceService.isCalculateOperationCostModePiecework(productionBalance)
                && order.getBooleanField(REGISTER_PIECEWORK)) {
            registeredTotalTechnicalProductionCosts = registeredTotalTechnicalProductionCosts.add(
                    productionBalance.getDecimalField(CYCLES_COSTS), numberService.getMathContext());
        }

        BigDecimal balanceTechnicalProductionCosts = registeredTotalTechnicalProductionCosts.subtract(
                productionBalance.getDecimalField(TOTAL_TECHNICAL_PRODUCTION_COSTS), numberService.getMathContext());

        productionBalance.setField(REGISTERED_TOTAL_TECHNICALPRODUCTION_COSTS,
                numberService.setScale(registeredTotalTechnicalProductionCosts));

        productionBalance.setField(BALANCE_TECHNICAL_PRODUCTION_COSTS, numberService.setScale(balanceTechnicalProductionCosts));

        registeredMaterialCostHelper.countRegisteredMaterialMarginValue(productionBalance);
        registeredProductionCostHelper.countRegisteredProductionMarginValue(productionBalance);
        costCalculationService.calculateTotalOverhead(productionBalance);

        BigDecimal totalCosts = registeredTotalTechnicalProductionCosts.add(
                DecimalUtils.nullToZero(productionBalance.getDecimalField(TOTAL_OVERHEAD)), numberService.getMathContext());
        productionBalance.setField(ProductionBalanceFieldsPCWC.TOTAL_COSTS, numberService.setScale(totalCosts));

        final BigDecimal doneQuantity = order.getDecimalField(OrderFields.DONE_QUANTITY);

        if (doneQuantity != null && BigDecimal.ZERO.compareTo(doneQuantity) != 0) {
            final BigDecimal totalCostPerUnit = totalCosts.divide(doneQuantity, numberService.getMathContext());
            final BigDecimal registeredTotalTechnicalProductionCostPerUnit = registeredTotalTechnicalProductionCosts.divide(
                    doneQuantity, numberService.getMathContext());

            productionBalance.setField(ProductionBalanceFieldsPCWC.TOTAL_COST_PER_UNIT, numberService.setScale(totalCostPerUnit));
            productionBalance.setField(ProductionBalanceFieldsPCWC.REGISTERED_TOTAL_TECHNICAL_PRODUCTION_COST_PER_UNIT,
                    numberService.setScale(registeredTotalTechnicalProductionCostPerUnit));

            final BigDecimal balanceTechnicalProductionCostPerUnit = registeredTotalTechnicalProductionCostPerUnit.subtract(
                    productionBalance.getDecimalField(ProductionBalanceFieldsPCWC.TOTAL_TECHNICAL_PRODUCTION_COST_PER_UNIT),
                    numberService.getMathContext());
            productionBalance.setField(ProductionBalanceFieldsPCWC.BALANCE_TECHNICAL_PRODUCTION_COST_PER_UNIT,
                    numberService.setScale(balanceTechnicalProductionCostPerUnit));
        } else {
            productionBalance.setField(ProductionBalanceFieldsPCWC.TOTAL_COST_PER_UNIT, null);
            productionBalance.setField(ProductionBalanceFieldsPCWC.REGISTERED_TOTAL_TECHNICAL_PRODUCTION_COST_PER_UNIT, null);
            productionBalance.setField(ProductionBalanceFieldsPCWC.BALANCE_TECHNICAL_PRODUCTION_COST_PER_UNIT, null);
        }

        productionBalance.getDataDefinition().save(productionBalance);
    }

    private Map<Entity, BigDecimal> getPlannedProductsWithCosts(final Entity productionBalance, final Entity order) {
        BigDecimal givenQty = productionBalance.getDecimalField(QUANTITY);

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
        if (operatonTimeComponent.getBelongsToField(MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT) == null) {
            return null;
        } else {
            return dataDefinitionService
                    .get(CostNormsForOperationConstants.PLUGIN_IDENTIFIER,
                            CostNormsForOperationConstants.MODEL_CALCULATION_OPERATION_COMPONENT)
                    .find()
                    .add(SearchRestrictions.belongsTo(MODEL_PRODUCTION_BALANCE, productionBalance))
                    .add(SearchRestrictions.belongsTo(MODEL_TECHNOLOGY_OPERATION_COMPONENT,
                            operatonTimeComponent.getBelongsToField(MODEL_TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)
                                    .getBelongsToField(MODEL_TECHNOLOGY_OPERATION_COMPONENT))).setMaxResults(1).uniqueResult();
        }
    }

    private static BigDecimal getNotNullBigDecimal(final BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value;
    }

    private static BigDecimal convertNullToOne(final BigDecimal value) {
        if (value == null) {
            return BigDecimal.ONE;
        }
        return value;
    }

}
