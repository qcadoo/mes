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
package com.qcadoo.mes.operationCostCalculations;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.costNormsForOperation.constants.CalculateOperationCostMode;
import com.qcadoo.mes.costNormsForOperation.constants.CalculationOperationComponentFields;
import com.qcadoo.mes.costNormsForOperation.constants.TechnologyOperationComponentFieldsCNFO;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.operationTimeCalculations.dto.OperationTimes;
import com.qcadoo.mes.operationTimeCalculations.dto.OperationTimesContainer;
import com.qcadoo.mes.productionLines.ProductionLinesService;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.ProductQuantitiesHolder;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.IntegerUtils;
import com.qcadoo.model.api.NumberService;

@Service
public class OperationsCostCalculationServiceImpl implements OperationsCostCalculationService {

    private static final String L_PRODUCTION_BALANCE = "productionBalance";

    private static final String L_COST_CALCULATION = "costCalculation";

    private static final String L_CALCULATION_OPERATION_COMPONENTS = "calculationOperationComponents";

    private static final String L_ORDER = "order";

    private static final String L_TECHNOLOGY = "technology";

    private static final String L_QUANTITY = "quantity";

    private static final String L_PRODUCTION_COST_MARGIN = "productionCostMargin";

    private static final String L_CALCULATE_OPERATION_COSTS_MODE = "calculateOperationCostsMode";

    private static final String L_PRODUCTION_LINE = "productionLine";

    private static final String L_INCLUDE_ADDITIONAL_TIME = "includeAdditionalTime";

    private static final String L_INCLUDE_TPZ = "includeTPZ";

    private static final String L_TOTAL_LABOR_HOURLY_COSTS = "totalLaborHourlyCosts";

    private static final String L_TOTAL_MACHINE_HOURLY_COSTS = "totalMachineHourlyCosts";

    private static final String L_TOTAL_PIECEWORK_COSTS = "totalPieceworkCosts";

    private static final String L_MACHINE_HOURLY_COST = "machineHourlyCost";

    private static final String L_LABOR_HOURLY_COST = "laborHourlyCost";

    private static final String L_OPERATION_MACHINE_COST = "operationMachineCost";

    private static final String L_OPERATION_LABOR_COST = "operationLaborCost";

    private static final String L_OPERATION_COST = "operationCost";

    private static final String L_OPERATION_MARGIN_COST = "operationMarginCost";

    private static final String L_TOTAL_OPERATION_COST = "totalOperationCost";

    private static final String L_PIECES = "pieces";

    private static final String L_TOTAL_LABOR_OPERATION_COST_WITH_MARGIN = "totalLaborOperationCostWithMargin";

    private static final String L_TOTAL_MACHINE_OPERATION_COST_WITH_MARGIN = "totalMachineOperationCostWithMargin";

    private static final Set<String> L_COST_KEYS = Sets.newHashSet(CalculationOperationComponentFields.LABOR_HOURLY_COST,
            CalculationOperationComponentFields.MACHINE_HOURLY_COST);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private ProductionLinesService productionLinesService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Autowired
    private OperationCostCalculationTreeBuilder operationCostCalculationTreeBuilder;

    @Autowired
    private ParameterService parameterService;

    @Override
    public void calculateOperationsCost(final Entity costCalculationOrProductionBalance) {
        checkArgument(costCalculationOrProductionBalance != null, "entity is null");
        String modelName = costCalculationOrProductionBalance.getDataDefinition().getName();
        checkArgument(L_COST_CALCULATION.equals(modelName) || L_PRODUCTION_BALANCE.equals(modelName), "unsupported entity type");

        DataDefinition costCalculationOrProductionBalanceDD = costCalculationOrProductionBalance.getDataDefinition();

        Entity copyCostCalculationOrProductionBalance = operationCostCalculationTreeBuilder
                .copyTechnologyTree(costCalculationOrProductionBalance);

        Entity yetAnotherCostCalculationOrProductionBalance = costCalculationOrProductionBalanceDD
                .save(copyCostCalculationOrProductionBalance);
        Entity newCostCalculationOrProductionBalance = costCalculationOrProductionBalanceDD
                .get(yetAnotherCostCalculationOrProductionBalance.getId());

        EntityTree calculationOperationComponents = newCostCalculationOrProductionBalance
                .getTreeField(L_CALCULATION_OPERATION_COMPONENTS);

        checkArgument(calculationOperationComponents != null, "given operation components is null");

        Entity order = costCalculationOrProductionBalance.getBelongsToField(L_ORDER);
        Entity technology = costCalculationOrProductionBalance.getBelongsToField(L_TECHNOLOGY);
        BigDecimal quantity = BigDecimalUtils.convertNullToZero(costCalculationOrProductionBalance.getDecimalField(L_QUANTITY));
        BigDecimal productionCostMargin = BigDecimalUtils.convertNullToZero(costCalculationOrProductionBalance
                .getDecimalField(L_PRODUCTION_COST_MARGIN));

        CalculateOperationCostMode calculateOperationCostMode = CalculateOperationCostMode
                .parseString(costCalculationOrProductionBalance.getStringField(L_CALCULATE_OPERATION_COSTS_MODE));

        if (order != null) {
            Entity technologyFromOrder = order.getBelongsToField(L_TECHNOLOGY);

            technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyFromOrder.getId());
        }

        ProductQuantitiesHolder productQuantitiesAndOperationRuns = productQuantitiesService.getProductComponentQuantities(
                technology, quantity);

        if (CalculateOperationCostMode.PIECEWORK.equals(calculateOperationCostMode)) {
            if (calculationOperationComponents.isEmpty()) {
                costCalculationOrProductionBalance.addError(costCalculationOrProductionBalanceDD.getField(L_ORDER),
                        "costCalculation.lackOfTreeComponents");
                costCalculationOrProductionBalance.addError(costCalculationOrProductionBalanceDD.getField(L_TECHNOLOGY),
                        "costCalculation.lackOfTreeComponents");
                return;
            }

            BigDecimal totalPieceworkCost = estimateCostCalculationForPieceWork(calculationOperationComponents.getRoot(),
                    productionCostMargin, quantity, productQuantitiesAndOperationRuns.getOperationRuns());

            costCalculationOrProductionBalance.setField(L_TOTAL_PIECEWORK_COSTS, numberService.setScale(totalPieceworkCost));
        } else if (CalculateOperationCostMode.HOURLY.equals(calculateOperationCostMode)) {
            Entity productionLine = costCalculationOrProductionBalance.getBelongsToField(L_PRODUCTION_LINE);

            Boolean includeTPZ = costCalculationOrProductionBalance.getBooleanField(L_INCLUDE_TPZ);
            Boolean includeAdditionalTime = costCalculationOrProductionBalance.getBooleanField(L_INCLUDE_ADDITIONAL_TIME);

            Map<Long, Integer> workstations = getWorkstationsMapsForOperationsComponent(copyCostCalculationOrProductionBalance,
                    productionLine);

            OperationTimesContainer operationTimes = operationWorkTimeService.estimateOperationsWorkTimes(
                    calculationOperationComponents, productQuantitiesAndOperationRuns.getOperationRuns(), includeTPZ,
                    includeAdditionalTime, workstations, true);

            Map<String, BigDecimal> resultsMap = estimateCostCalculationForHourly(calculationOperationComponents.getRoot(),
                    productionCostMargin, quantity, operationTimes);

            costCalculationOrProductionBalance.setField(L_TOTAL_MACHINE_HOURLY_COSTS,
                    numberService.setScale(resultsMap.get(CalculationOperationComponentFields.MACHINE_HOURLY_COST)));
            costCalculationOrProductionBalance.setField(L_TOTAL_LABOR_HOURLY_COSTS,
                    numberService.setScale(resultsMap.get(CalculationOperationComponentFields.LABOR_HOURLY_COST)));
        } else {
            throw new IllegalStateException("Unsupported calculateOperationCostMode");
        }

        costCalculationOrProductionBalance.setField(L_CALCULATION_OPERATION_COMPONENTS, calculationOperationComponents);
    }

    @Override
    public Map<String, BigDecimal> estimateCostCalculationForHourly(final EntityTreeNode calculationOperationComponent,
            final BigDecimal productionCostMargin, final BigDecimal plannedQuantity,
            final OperationTimesContainer realizationTimes) {
        checkArgument(calculationOperationComponent != null, "given operationComponent is empty");

        Map<String, BigDecimal> costs = Maps.newHashMapWithExpectedSize(L_COST_KEYS.size());

        MathContext mathContext = numberService.getMathContext();

        for (String costKey : L_COST_KEYS) {
            costs.put(costKey, BigDecimal.ZERO);
        }

        for (EntityTreeNode child : calculationOperationComponent.getChildren()) {
            Map<String, BigDecimal> unitCosts = estimateCostCalculationForHourly(child, productionCostMargin, plannedQuantity,
                    realizationTimes);

            for (String costKey : L_COST_KEYS) {
                BigDecimal unitCost = costs.get(costKey).add(unitCosts.get(costKey), mathContext);

                costs.put(costKey, numberService.setScale(unitCost));
            }
        }

        OperationTimes operationTimes = realizationTimes.get(calculationOperationComponent.getId());
        Map<String, BigDecimal> costsForSingleOperation = estimateHourlyCostCalculationForSingleOperation(operationTimes,
                productionCostMargin);
        saveGeneratedValues(costsForSingleOperation, calculationOperationComponent, true, operationTimes.getTimes(), null);

        costs.put(L_MACHINE_HOURLY_COST,
                costs.get(L_MACHINE_HOURLY_COST).add(costsForSingleOperation.get(L_OPERATION_MACHINE_COST), mathContext));
        costs.put(L_LABOR_HOURLY_COST,
                costs.get(L_LABOR_HOURLY_COST).add(costsForSingleOperation.get(L_OPERATION_LABOR_COST), mathContext));

        return costs;
    }

    private Map<String, BigDecimal> estimateHourlyCostCalculationForSingleOperation(final OperationTimes operationTimes,
            final BigDecimal productionCostMargin) {
        Map<String, BigDecimal> costs = Maps.newHashMap();

        MathContext mathContext = numberService.getMathContext();

        Entity calculationOperationComponent = operationTimes.getOperation();
        Entity technologyOperationComponent = calculationOperationComponent
                .getBelongsToField(CalculationOperationComponentFields.TECHNOLOGY_OPERATION_COMPONENT);

        OperationWorkTime operationWorkTimes = operationTimes.getTimes();

        BigDecimal machineHourlyCost = BigDecimalUtils.convertNullToZero(technologyOperationComponent
                .getField(TechnologyOperationComponentFieldsCNFO.MACHINE_HOURLY_COST));
        BigDecimal laborHourlyCost = BigDecimalUtils.convertNullToZero(technologyOperationComponent
                .getField(TechnologyOperationComponentFieldsCNFO.LABOR_HOURLY_COST));

        BigDecimal durationMachine = BigDecimal.valueOf(operationWorkTimes.getMachineWorkTime());
        BigDecimal durationLabor = BigDecimal.valueOf(operationWorkTimes.getLaborWorkTime());

        BigDecimal durationMachineInHours = durationMachine.divide(BigDecimal.valueOf(3600), mathContext);
        BigDecimal durationLaborInHours = durationLabor.divide(BigDecimal.valueOf(3600), mathContext);

        BigDecimal operationMachineCost = durationMachineInHours.multiply(machineHourlyCost, mathContext);
        BigDecimal operationLaborCost = durationLaborInHours.multiply(laborHourlyCost, mathContext);

        BigDecimal totalMachineOperationCostWithMargin = operationMachineCost.add(
                operationMachineCost.multiply(productionCostMargin.divide(BigDecimal.valueOf(100), mathContext), mathContext),
                mathContext);

        BigDecimal totalLaborOperationCostWithMargin = operationLaborCost.add(
                operationLaborCost.multiply(productionCostMargin.divide(BigDecimal.valueOf(100), mathContext), mathContext),
                mathContext);

        BigDecimal operationCost = operationMachineCost.add(operationLaborCost, mathContext);
        BigDecimal operationMarginCost = operationCost.multiply(
                productionCostMargin.divide(BigDecimal.valueOf(100), mathContext), mathContext);

        costs.put(L_MACHINE_HOURLY_COST, numberService.setScale(machineHourlyCost));
        costs.put(L_LABOR_HOURLY_COST, numberService.setScale(laborHourlyCost));
        costs.put(L_OPERATION_MACHINE_COST, numberService.setScale(operationMachineCost));
        costs.put(L_OPERATION_LABOR_COST, numberService.setScale(operationLaborCost));
        costs.put(L_OPERATION_COST, numberService.setScale(operationCost));
        costs.put(L_OPERATION_MARGIN_COST, numberService.setScale(operationMarginCost));
        costs.put(L_TOTAL_MACHINE_OPERATION_COST_WITH_MARGIN, numberService.setScale(totalMachineOperationCostWithMargin));
        costs.put(L_TOTAL_LABOR_OPERATION_COST_WITH_MARGIN, numberService.setScale(totalLaborOperationCostWithMargin));

        return costs;
    }

    @Override
    public BigDecimal estimateCostCalculationForPieceWork(final EntityTreeNode calculationOperationComponent,
            final BigDecimal productionCostMargin, final BigDecimal plannedQuantity, final Map<Long, BigDecimal> operationRuns) {

        BigDecimal cost = BigDecimal.ZERO;

        for (EntityTreeNode child : calculationOperationComponent.getChildren()) {
            cost = cost.add(estimateCostCalculationForPieceWork(child, productionCostMargin, plannedQuantity, operationRuns),
                    numberService.getMathContext());
        }

        // FIXME MAKU unnecessary mapping of whole entity - we need only their id! We can increase performance by replacing line
        // below by a query projection
        Entity technologyOperationComponent = calculationOperationComponent
                .getBelongsToField(CalculationOperationComponentFields.TECHNOLOGY_OPERATION_COMPONENT);

        BigDecimal operationRunsForOperation = operationRuns.get(technologyOperationComponent.getId());

        Map<String, BigDecimal> costsForSingleOperation = estimatePieceworkCostCalculationForSingleOperation(
                calculationOperationComponent, productionCostMargin, operationRunsForOperation);

        cost = cost.add(costsForSingleOperation.get(L_OPERATION_COST));
        saveGeneratedValues(costsForSingleOperation, calculationOperationComponent, false, null, operationRunsForOperation);

        return cost;
    }

    private Map<String, BigDecimal> estimatePieceworkCostCalculationForSingleOperation(
            final EntityTreeNode calculationOperationComponent, final BigDecimal productionCostMargin,
            final BigDecimal operationRuns) {
        Map<String, BigDecimal> costs = Maps.newHashMap();

        BigDecimal pieceworkCost = BigDecimalUtils.convertNullToZero(calculationOperationComponent
                .getDecimalField(CalculationOperationComponentFields.PIECEWORK_COST));
        BigDecimal numberOfOperations = BigDecimalUtils.convertNullToOne(calculationOperationComponent
                .getField(CalculationOperationComponentFields.NUMBER_OF_OPERATIONS));

        BigDecimal pieceworkCostPerOperation = pieceworkCost.divide(numberOfOperations, numberService.getMathContext());

        BigDecimal operationCost = operationRuns.multiply(pieceworkCostPerOperation, numberService.getMathContext());
        BigDecimal operationMarginCost = operationCost.multiply(productionCostMargin.divide(BigDecimal.valueOf(100),
                numberService.getMathContext()));
        BigDecimal totalOperationCost = numberService.setScale(operationCost.add(operationMarginCost,
                numberService.getMathContext()));

        costs.put(L_OPERATION_COST, numberService.setScale(operationCost));
        costs.put(L_OPERATION_MARGIN_COST, numberService.setScale(operationMarginCost));
        costs.put(L_PIECES, numberService.setScale(operationRuns));
        costs.put(L_TOTAL_OPERATION_COST, totalOperationCost);

        return costs;
    }

    private void saveGeneratedValues(final Map<String, BigDecimal> costs, final Entity calculationOperationComponent,
            boolean areHourly, final OperationWorkTime operationWorkTimes, final BigDecimal operationRuns) {
        if (areHourly) {
            calculationOperationComponent.setField(CalculationOperationComponentFields.DURATION, new BigDecimal(
                    operationWorkTimes.getDuration(), numberService.getMathContext()));
            calculationOperationComponent.setField(CalculationOperationComponentFields.MACHINE_HOURLY_COST,
                    costs.get(L_MACHINE_HOURLY_COST));
            calculationOperationComponent.setField(CalculationOperationComponentFields.LABOR_HOURLY_COST,
                    costs.get(L_LABOR_HOURLY_COST));
            calculationOperationComponent.setField(CalculationOperationComponentFields.TOTAL_MACHINE_OPERATION_COST,
                    costs.get(L_OPERATION_MACHINE_COST));
            calculationOperationComponent.setField(CalculationOperationComponentFields.TOTAL_LABOR_OPERATION_COST,
                    costs.get(L_OPERATION_LABOR_COST));
            calculationOperationComponent.setField(CalculationOperationComponentFields.TOTAL_MACHINE_OPERATION_COST_WITH_MARGIN,
                    costs.get(L_TOTAL_MACHINE_OPERATION_COST_WITH_MARGIN));
            calculationOperationComponent.setField(CalculationOperationComponentFields.TOTAL_LABOR_OPERATION_COST_WITH_MARGIN,
                    costs.get(L_TOTAL_LABOR_OPERATION_COST_WITH_MARGIN));
        } else {
            calculationOperationComponent.setField(CalculationOperationComponentFields.PIECES,
                    numberService.setScale(operationRuns));
        }

        BigDecimal operationCost = costs.get(L_OPERATION_COST);
        BigDecimal operationMarginCost = costs.get(L_OPERATION_MARGIN_COST);

        calculationOperationComponent.setField(CalculationOperationComponentFields.OPERATION_COST,
                numberService.setScale(operationCost));
        calculationOperationComponent.setField(CalculationOperationComponentFields.OPERATION_MARGIN_COST,
                numberService.setScale(operationMarginCost));
        calculationOperationComponent.setField(CalculationOperationComponentFields.TOTAL_OPERATION_COST,
                numberService.setScale(operationCost.add(operationMarginCost, numberService.getMathContext())));

        calculationOperationComponent.getDataDefinition().save(calculationOperationComponent);
    }

    private Map<Long, Integer> getWorkstationsMapsForOperationsComponent(final Entity costCalculationOrProductionBalance,
            final Entity productionLine) {
        Entity order = costCalculationOrProductionBalance.getBelongsToField(L_ORDER);
        if (order == null) {
            return getWorkstationsFromTechnology(costCalculationOrProductionBalance.getBelongsToField(L_TECHNOLOGY),
                    productionLine);
        } else {
            return getWorkstationsFromOrder(order);
        }
    }

    private Map<Long, Integer> getWorkstationsFromTechnology(final Entity technology, final Entity productionLine) {
        Map<Long, Integer> workstations = Maps.newHashMap();
        if (parameterService.getParameter().getBooleanField("workstationsQuantityFromProductionLine")) {
            for (Entity operComp : technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS)) {
                workstations.put(operComp.getId(), productionLinesService.getWorkstationTypesCount(operComp, productionLine));
            }
        } else {
            for (Entity operComp : technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS)) {
                workstations.put(operComp.getId(), IntegerUtils.convertNullToZero(operComp
                        .getIntegerField(TechnologyOperationComponentFields.QUANTITY_OF_WORKSTATIONS)));
            }
        }
        return workstations;
    }

    private Map<Long, Integer> getWorkstationsFromOrder(final Entity order) {
        Map<Long, Integer> workstations = Maps.newHashMap();

        for (Entity technologyOperationComponent : order.getBelongsToField(L_TECHNOLOGY).getHasManyField(
                TechnologyFields.OPERATION_COMPONENTS)) {
            workstations.put(technologyOperationComponent.getId(), IntegerUtils.convertNullToZero(technologyOperationComponent
                    .getIntegerField(TechnologyOperationComponentFields.QUANTITY_OF_WORKSTATIONS)));
        }

        return workstations;
    }

}
