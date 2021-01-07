/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.costNormsForOperation.constants.CalculationOperationComponentFields;
import com.qcadoo.mes.costNormsForOperation.constants.TechnologyOperationComponentFieldsCNFO;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.operationTimeCalculations.dto.OperationTimes;
import com.qcadoo.mes.operationTimeCalculations.dto.OperationTimesContainer;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.ProductQuantitiesHolder;
import com.qcadoo.model.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

@Service
public class OperationsCostCalculationServiceImpl implements OperationsCostCalculationService {

    private static final String L_CALCULATION_OPERATION_COMPONENTS = "calculationOperationComponents";

    private static final String L_QUANTITY = "quantity";

    private static final String L_PRODUCTION_COST_MARGIN = "productionCostMargin";

    private static final String L_INCLUDE_ADDITIONAL_TIME = "includeAdditionalTime";

    private static final String L_INCLUDE_TPZ = "includeTPZ";

    private static final String L_TOTAL_LABOR_HOURLY_COSTS = "totalLaborHourlyCosts";

    private static final String L_TOTAL_MACHINE_HOURLY_COSTS = "totalMachineHourlyCosts";

    private static final String L_MACHINE_HOURLY_COST = "machineHourlyCost";

    private static final String L_LABOR_HOURLY_COST = "laborHourlyCost";

    private static final String L_OPERATION_MACHINE_COST = "operationMachineCost";

    private static final String L_OPERATION_LABOR_COST = "operationLaborCost";

    private static final String L_OPERATION_COST = "operationCost";

    private static final String L_OPERATION_MARGIN_COST = "operationMarginCost";

    private static final String L_TOTAL_LABOR_OPERATION_COST_WITH_MARGIN = "totalLaborOperationCostWithMargin";

    private static final String L_TOTAL_MACHINE_OPERATION_COST_WITH_MARGIN = "totalMachineOperationCostWithMargin";

    private static final Set<String> L_COST_KEYS = Sets.newHashSet(CalculationOperationComponentFields.LABOR_HOURLY_COST,
            CalculationOperationComponentFields.MACHINE_HOURLY_COST);

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Autowired
    private OperationCostCalculationTreeBuilder operationCostCalculationTreeBuilder;

    @Autowired
    private ParameterService parameterService;

    @Override
    public void calculateOperationsCost(final Entity costCalculation, boolean hourlyCostFromOperation, final Entity technology) {
        checkArgument(costCalculation != null, "entity is null");

        DataDefinition costCalculationOrProductionBalanceDD = costCalculation.getDataDefinition();

        BigDecimal quantity = BigDecimalUtils.convertNullToZero(costCalculation.getDecimalField(L_QUANTITY));
        BigDecimal productionCostMargin = BigDecimalUtils
                .convertNullToZero(costCalculation.getDecimalField(L_PRODUCTION_COST_MARGIN));

        ProductQuantitiesHolder productQuantitiesAndOperationRuns = getProductQuantitiesAndOperationRuns(technology, quantity,
                costCalculation);

        Entity copyCostCalculationOrProductionBalance = operationCostCalculationTreeBuilder.copyTechnologyTree(costCalculation,
                technology);

        Entity yetAnotherCostCalculationOrProductionBalance = costCalculationOrProductionBalanceDD
                .save(copyCostCalculationOrProductionBalance);
        Entity newCostCalculationOrProductionBalance = costCalculationOrProductionBalanceDD
                .get(yetAnotherCostCalculationOrProductionBalance.getId());

        EntityTree calculationOperationComponents = newCostCalculationOrProductionBalance
                .getTreeField(L_CALCULATION_OPERATION_COMPONENTS);

        checkArgument(calculationOperationComponents != null, "given operation components is null");

        boolean includeTPZ = costCalculation.getBooleanField(L_INCLUDE_TPZ);
        boolean includeAdditionalTime = costCalculation.getBooleanField(L_INCLUDE_ADDITIONAL_TIME);

        Map<Long, Integer> workstations = getWorkstationsFromTechnology(technology);

        List<Entity> tocs = calculationOperationComponents.stream().map(e -> e.getBelongsToField("technologyOperationComponent"))
                .collect(Collectors.toList());
        OperationTimesContainer operationTimes = operationWorkTimeService.estimateOperationsWorkTimes(tocs,
                productQuantitiesAndOperationRuns.getOperationRuns(), includeTPZ, includeAdditionalTime, workstations, true);

        Map<String, BigDecimal> resultsMap = estimateCostCalculationForHourly(calculationOperationComponents.getRoot(),
                productionCostMargin, quantity, operationTimes, hourlyCostFromOperation);

        costCalculation.setField(L_TOTAL_MACHINE_HOURLY_COSTS, numberService
                .setScaleWithDefaultMathContext(resultsMap.get(CalculationOperationComponentFields.MACHINE_HOURLY_COST)));
        costCalculation.setField(L_TOTAL_LABOR_HOURLY_COSTS, numberService
                .setScaleWithDefaultMathContext(resultsMap.get(CalculationOperationComponentFields.LABOR_HOURLY_COST)));

        costCalculation.setField(L_CALCULATION_OPERATION_COMPONENTS, calculationOperationComponents);
    }

    private ProductQuantitiesHolder getProductQuantitiesAndOperationRuns(final Entity technology, final BigDecimal quantity,
            final Entity costCalculationOrProductionBalance) {
        return productQuantitiesService.getProductComponentQuantities(technology, quantity);
    }

    @Override
    public Map<String, BigDecimal> estimateCostCalculationForHourly(final EntityTreeNode calculationOperationComponent,
            final BigDecimal productionCostMargin, final BigDecimal plannedQuantity,
            final OperationTimesContainer realizationTimes, final boolean hourlyCostFromOperation) {
        checkArgument(calculationOperationComponent != null, "given operationComponent is empty");

        Map<String, BigDecimal> costs = Maps.newHashMapWithExpectedSize(L_COST_KEYS.size());

        MathContext mathContext = numberService.getMathContext();

        for (String costKey : L_COST_KEYS) {
            costs.put(costKey, BigDecimal.ZERO);
        }

        for (EntityTreeNode child : calculationOperationComponent.getChildren()) {
            Map<String, BigDecimal> unitCosts = estimateCostCalculationForHourly(child, productionCostMargin, plannedQuantity,
                    realizationTimes, hourlyCostFromOperation);

            for (String costKey : L_COST_KEYS) {
                BigDecimal unitCost = costs.get(costKey).add(unitCosts.get(costKey), mathContext);

                costs.put(costKey, numberService.setScaleWithDefaultMathContext(unitCost));
            }
        }

        OperationTimes operationTimes = realizationTimes
                .get(calculationOperationComponent.getBelongsToField("technologyOperationComponent").getId());
        Map<String, BigDecimal> costsForSingleOperation = estimateHourlyCostCalculationForSingleOperation(operationTimes,
                productionCostMargin, hourlyCostFromOperation);
        saveGeneratedValues(costsForSingleOperation, calculationOperationComponent, true, operationTimes.getTimes(), null);

        costs.put(L_MACHINE_HOURLY_COST,
                costs.get(L_MACHINE_HOURLY_COST).add(costsForSingleOperation.get(L_OPERATION_MACHINE_COST), mathContext));
        costs.put(L_LABOR_HOURLY_COST,
                costs.get(L_LABOR_HOURLY_COST).add(costsForSingleOperation.get(L_OPERATION_LABOR_COST), mathContext));

        return costs;
    }

    private Map<String, BigDecimal> estimateHourlyCostCalculationForSingleOperation(final OperationTimes operationTimes,
            final BigDecimal productionCostMargin, boolean hourlyCostFromOperation) {
        Map<String, BigDecimal> costs = Maps.newHashMap();

        MathContext mathContext = numberService.getMathContext();

        Entity technologyOperationComponent = operationTimes.getOperation();

        OperationWorkTime operationWorkTimes = operationTimes.getTimes();

        BigDecimal machineHourlyCost;
        BigDecimal laborHourlyCost;
        if (hourlyCostFromOperation) {
            machineHourlyCost = BigDecimalUtils.convertNullToZero(
                    technologyOperationComponent.getField(TechnologyOperationComponentFieldsCNFO.MACHINE_HOURLY_COST));
            laborHourlyCost = BigDecimalUtils.convertNullToZero(
                    technologyOperationComponent.getField(TechnologyOperationComponentFieldsCNFO.LABOR_HOURLY_COST));
        } else {
            machineHourlyCost = BigDecimalUtils
                    .convertNullToZero(parameterService.getParameter().getDecimalField("averageMachineHourlyCostPB"));
            laborHourlyCost = BigDecimalUtils
                    .convertNullToZero(parameterService.getParameter().getDecimalField("averageLaborHourlyCostPB"));
        }

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
        BigDecimal operationMarginCost = operationCost.multiply(productionCostMargin.divide(BigDecimal.valueOf(100), mathContext),
                mathContext);

        costs.put(L_MACHINE_HOURLY_COST, numberService.setScaleWithDefaultMathContext(machineHourlyCost));
        costs.put(L_LABOR_HOURLY_COST, numberService.setScaleWithDefaultMathContext(laborHourlyCost));
        costs.put(L_OPERATION_MACHINE_COST, numberService.setScaleWithDefaultMathContext(operationMachineCost));
        costs.put(L_OPERATION_LABOR_COST, numberService.setScaleWithDefaultMathContext(operationLaborCost));
        costs.put(L_OPERATION_COST, numberService.setScaleWithDefaultMathContext(operationCost));
        costs.put(L_OPERATION_MARGIN_COST, numberService.setScaleWithDefaultMathContext(operationMarginCost));
        costs.put(L_TOTAL_MACHINE_OPERATION_COST_WITH_MARGIN,
                numberService.setScaleWithDefaultMathContext(totalMachineOperationCostWithMargin));
        costs.put(L_TOTAL_LABOR_OPERATION_COST_WITH_MARGIN,
                numberService.setScaleWithDefaultMathContext(totalLaborOperationCostWithMargin));

        return costs;
    }

    private void saveGeneratedValues(final Map<String, BigDecimal> costs, final Entity calculationOperationComponent,
            boolean areHourly, final OperationWorkTime operationWorkTimes, final BigDecimal operationRuns) {
        if (areHourly) {
            calculationOperationComponent.setField(CalculationOperationComponentFields.DURATION,
                    new BigDecimal(operationWorkTimes.getDuration(), numberService.getMathContext()));
            calculationOperationComponent.setField(CalculationOperationComponentFields.MACHINE_WORK_TIME,
                    operationWorkTimes.getMachineWorkTime());
            calculationOperationComponent.setField(CalculationOperationComponentFields.LABOR_WORK_TIME,
                    operationWorkTimes.getLaborWorkTime());
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
                    numberService.setScaleWithDefaultMathContext(operationRuns));
        }

        BigDecimal operationCost = costs.get(L_OPERATION_COST);
        BigDecimal operationMarginCost = costs.get(L_OPERATION_MARGIN_COST);

        calculationOperationComponent.setField(CalculationOperationComponentFields.OPERATION_COST,
                numberService.setScaleWithDefaultMathContext(operationCost));
        calculationOperationComponent.setField(CalculationOperationComponentFields.OPERATION_MARGIN_COST,
                numberService.setScaleWithDefaultMathContext(operationMarginCost));
        calculationOperationComponent.setField(CalculationOperationComponentFields.TOTAL_OPERATION_COST, numberService
                .setScaleWithDefaultMathContext(operationCost.add(operationMarginCost, numberService.getMathContext())));

        calculationOperationComponent.getDataDefinition().save(calculationOperationComponent);
    }

    private Map<Long, Integer> getWorkstationsFromTechnology(final Entity technology) {
        Map<Long, Integer> workstations = Maps.newHashMap();
        for (Entity operComp : technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS)) {
            workstations.put(operComp.getId(), IntegerUtils
                    .convertNullToZero(operComp.getIntegerField(TechnologyOperationComponentFields.QUANTITY_OF_WORKSTATIONS)));
        }
        return workstations;
    }

}
