/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.costCalculation.print;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.constants.PieceRateFields;
import com.qcadoo.mes.basic.constants.PieceRateItemFields;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.constants.SourceOfOperationCosts;
import com.qcadoo.mes.costNormsForOperation.constants.CalculationOperationComponentFields;
import com.qcadoo.mes.costNormsForOperation.constants.TechnologyOperationComponentFieldsCNFO;
import com.qcadoo.mes.operationCostCalculations.OperationCostCalculationTreeBuilder;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.operationTimeCalculations.dto.OperationTimes;
import com.qcadoo.mes.operationTimeCalculations.dto.OperationTimesContainer;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.ProductQuantitiesWithComponentsService;
import com.qcadoo.mes.technologies.dto.ProductQuantitiesHolder;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginManager;

@Service
public class OperationsCostCalculationServiceImpl implements OperationsCostCalculationService {

    public static final String ORDERS_FOR_SUBPRODUCTS_GENERATION = "ordersForSubproductsGeneration";

    private static final Set<String> L_COST_KEYS = Sets.newHashSet(CalculationOperationComponentFields.LABOR_HOURLY_COST,
            CalculationOperationComponentFields.MACHINE_HOURLY_COST);

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private ProductQuantitiesWithComponentsService productQuantitiesWithComponentsService;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Autowired
    private OperationCostCalculationTreeBuilder operationCostCalculationTreeBuilder;

    @Override
    public BigDecimal calculateOperationsCost(final Entity costCalculation, final Entity technology) {
        DataDefinition costCalculationDataDefinition = costCalculation.getDataDefinition();

        BigDecimal quantity = costCalculation.getDecimalField(CostCalculationFields.QUANTITY);

        ProductQuantitiesHolder productQuantitiesAndOperationRuns = getProductQuantitiesAndOperationRuns(technology, quantity,
                costCalculation);

        Entity copyCostCalculation = operationCostCalculationTreeBuilder.copyTechnologyTree(costCalculation, technology);

        Entity yetAnotherCostCalculation = costCalculationDataDefinition.save(copyCostCalculation);
        Entity newCostCalculation = costCalculationDataDefinition.get(yetAnotherCostCalculation.getId());

        EntityTree calculationOperationComponents = newCostCalculation
                .getTreeField(CostCalculationFields.CALCULATION_OPERATION_COMPONENTS);

        checkArgument(calculationOperationComponents != null, "given operation components is null");

        List<Entity> tocs = calculationOperationComponents.stream()
                .map(e -> e.getBelongsToField(CalculationOperationComponentFields.TECHNOLOGY_OPERATION_COMPONENT))
                .collect(Collectors.toList());
        OperationTimesContainer operationTimes = operationWorkTimeService.estimateOperationsWorkTimes(tocs,
                productQuantitiesAndOperationRuns.getOperationRuns(),
                costCalculation.getBooleanField(CostCalculationFields.INCLUDE_TPZ),
                costCalculation.getBooleanField(CostCalculationFields.INCLUDE_ADDITIONAL_TIME), true);

        boolean hourlyCostFromOperation = !SourceOfOperationCosts.PARAMETERS.getStringValue()
                .equals(costCalculation.getStringField(CostCalculationFields.SOURCE_OF_OPERATION_COSTS));
        Map<String, BigDecimal> resultsMap = estimateCostCalculationForHourly(calculationOperationComponents.getRoot(),
                operationTimes, hourlyCostFromOperation, costCalculation, productQuantitiesAndOperationRuns.getOperationRuns());

        costCalculation.setField(CostCalculationFields.CALCULATION_OPERATION_COMPONENTS, calculationOperationComponents);
        return BigDecimalUtils
                .convertNullToZero(numberService
                        .setScaleWithDefaultMathContext(resultsMap.get(CalculationOperationComponentFields.MACHINE_HOURLY_COST)))
                .add(BigDecimalUtils.convertNullToZero(numberService
                                .setScaleWithDefaultMathContext(resultsMap.get(CalculationOperationComponentFields.LABOR_HOURLY_COST))),
                        numberService.getMathContext());
    }

    private ProductQuantitiesHolder getProductQuantitiesAndOperationRuns(final Entity technology, final BigDecimal quantity,
                                                                         final Entity costCalculation) {
        boolean includeComponents = costCalculation.getBooleanField(CostCalculationFields.INCLUDE_COMPONENTS);
        if (pluginManager.isPluginEnabled(ORDERS_FOR_SUBPRODUCTS_GENERATION) && includeComponents) {
            return productQuantitiesWithComponentsService.getProductComponentQuantities(technology, quantity);
        }
        return productQuantitiesService.getProductComponentQuantities(technology, quantity);
    }

    private Map<String, BigDecimal> estimateCostCalculationForHourly(final EntityTreeNode calculationOperationComponent,
                                                                     final OperationTimesContainer realizationTimes, final boolean hourlyCostFromOperation,
                                                                     Entity costCalculation, Map<Long, BigDecimal> operationRuns) {
        checkArgument(calculationOperationComponent != null, "given operationComponent is empty");

        Map<String, BigDecimal> costs = Maps.newHashMapWithExpectedSize(L_COST_KEYS.size());

        MathContext mathContext = numberService.getMathContext();

        for (String costKey : L_COST_KEYS) {
            costs.put(costKey, BigDecimal.ZERO);
        }

        for (EntityTreeNode child : calculationOperationComponent.getChildren()) {
            Map<String, BigDecimal> unitCosts = estimateCostCalculationForHourly(child, realizationTimes, hourlyCostFromOperation,
                    costCalculation, operationRuns);

            for (String costKey : L_COST_KEYS) {
                BigDecimal unitCost = costs.get(costKey).add(unitCosts.get(costKey), mathContext);

                costs.put(costKey, numberService.setScaleWithDefaultMathContext(unitCost));
            }
        }

        Entity toc = calculationOperationComponent.getBelongsToField(
                CalculationOperationComponentFields.TECHNOLOGY_OPERATION_COMPONENT);
        if (!toc.getBooleanField(TechnologyOperationComponentFieldsTNFO.PIECEWORK_PRODUCTION)) {
            OperationTimes operationTimes = realizationTimes.get(toc.getId());
            Map<String, BigDecimal> costsForSingleOperation = estimateHourlyCostCalculationForSingleOperation(operationTimes,
                    hourlyCostFromOperation, costCalculation);
            saveGeneratedValues(costsForSingleOperation, calculationOperationComponent, operationTimes.getTimes());

            costs.put(CalculationOperationComponentFields.MACHINE_HOURLY_COST,
                    costs.get(CalculationOperationComponentFields.MACHINE_HOURLY_COST).add(
                            costsForSingleOperation.get(CalculationOperationComponentFields.TOTAL_MACHINE_OPERATION_COST),
                            mathContext));
            costs.put(CalculationOperationComponentFields.LABOR_HOURLY_COST,
                    costs.get(CalculationOperationComponentFields.LABOR_HOURLY_COST).add(
                            costsForSingleOperation.get(CalculationOperationComponentFields.TOTAL_LABOR_OPERATION_COST),
                            mathContext));
        } else {
            costs.put(CalculationOperationComponentFields.LABOR_HOURLY_COST,
                    costs.get(CalculationOperationComponentFields.LABOR_HOURLY_COST).add(
                            BigDecimalUtils.convertNullToZero(operationRuns.get(toc.getId()))
                                    .multiply(getCurrentRate(toc.getBelongsToField(TechnologyOperationComponentFieldsCNFO.PIECE_RATE)), numberService.getMathContext()),
                            mathContext));
        }

        return costs;
    }

    @Override
    public BigDecimal getCurrentRate(Entity pieceRate) {
        Entity pieceRateItem = pieceRate.getHasManyField(PieceRateFields.PIECE_RATE_ITEMS)
                .find().addOrder(SearchOrders.desc(PieceRateItemFields.DATE_FROM))
                .add(SearchRestrictions.le(PieceRateItemFields.DATE_FROM, new Date())).setMaxResults(1).uniqueResult();
        BigDecimal currentRate = BigDecimal.ZERO;
        if (!Objects.isNull(pieceRateItem)) {
            currentRate = pieceRateItem.getDecimalField(PieceRateItemFields.ACTUAL_RATE);
        }
        return currentRate;
    }

    private Map<String, BigDecimal> estimateHourlyCostCalculationForSingleOperation(final OperationTimes operationTimes,
                                                                                    boolean hourlyCostFromOperation, Entity costCalculation) {
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
            machineHourlyCost = BigDecimalUtils.convertNullToZero(costCalculation.getDecimalField("averageMachineHourlyCost"));
            laborHourlyCost = BigDecimalUtils.convertNullToZero(costCalculation.getDecimalField("averageLaborHourlyCost"));
        }

        BigDecimal durationMachine = BigDecimal.valueOf(operationWorkTimes.getMachineWorkTime());
        BigDecimal durationLabor = BigDecimal.valueOf(operationWorkTimes.getLaborWorkTime());

        BigDecimal durationMachineInHours = durationMachine.divide(BigDecimal.valueOf(3600), mathContext);
        BigDecimal durationLaborInHours = durationLabor.divide(BigDecimal.valueOf(3600), mathContext);

        BigDecimal operationMachineCost = durationMachineInHours.multiply(machineHourlyCost, mathContext);
        BigDecimal operationLaborCost = durationLaborInHours.multiply(laborHourlyCost, mathContext)
                .multiply(BigDecimal.valueOf(technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.MIN_STAFF)), mathContext);

        BigDecimal operationCost = operationMachineCost.add(operationLaborCost, mathContext);

        costs.put(CalculationOperationComponentFields.MACHINE_HOURLY_COST,
                numberService.setScaleWithDefaultMathContext(machineHourlyCost));
        costs.put(CalculationOperationComponentFields.LABOR_HOURLY_COST,
                numberService.setScaleWithDefaultMathContext(laborHourlyCost));
        costs.put(CalculationOperationComponentFields.TOTAL_MACHINE_OPERATION_COST,
                numberService.setScaleWithDefaultMathContext(operationMachineCost));
        costs.put(CalculationOperationComponentFields.TOTAL_LABOR_OPERATION_COST,
                numberService.setScaleWithDefaultMathContext(operationLaborCost));
        costs.put(CalculationOperationComponentFields.OPERATION_COST,
                numberService.setScaleWithDefaultMathContext(operationCost));

        return costs;
    }

    private void saveGeneratedValues(final Map<String, BigDecimal> costs, final Entity calculationOperationComponent,
                                     final OperationWorkTime operationWorkTimes) {
        calculationOperationComponent.setField(CalculationOperationComponentFields.DURATION,
                new BigDecimal(operationWorkTimes.getDuration(), numberService.getMathContext()));
        calculationOperationComponent.setField(CalculationOperationComponentFields.MACHINE_WORK_TIME,
                operationWorkTimes.getMachineWorkTime());
        calculationOperationComponent.setField(CalculationOperationComponentFields.LABOR_WORK_TIME,
                operationWorkTimes.getLaborWorkTime());
        calculationOperationComponent.setField(CalculationOperationComponentFields.MACHINE_HOURLY_COST,
                costs.get(CalculationOperationComponentFields.MACHINE_HOURLY_COST));
        calculationOperationComponent.setField(CalculationOperationComponentFields.LABOR_HOURLY_COST,
                costs.get(CalculationOperationComponentFields.LABOR_HOURLY_COST));
        calculationOperationComponent.setField(CalculationOperationComponentFields.TOTAL_MACHINE_OPERATION_COST,
                costs.get(CalculationOperationComponentFields.TOTAL_MACHINE_OPERATION_COST));
        calculationOperationComponent.setField(CalculationOperationComponentFields.TOTAL_LABOR_OPERATION_COST,
                costs.get(CalculationOperationComponentFields.TOTAL_LABOR_OPERATION_COST));
        calculationOperationComponent.setField(CalculationOperationComponentFields.OPERATION_COST,
                numberService.setScaleWithDefaultMathContext(costs.get(CalculationOperationComponentFields.OPERATION_COST)));

    }

}
