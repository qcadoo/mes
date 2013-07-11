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
import static com.qcadoo.mes.costNormsForOperation.constants.CalculationOperationComponentFields.LABOR_HOURLY_COST;
import static com.qcadoo.mes.costNormsForOperation.constants.CalculationOperationComponentFields.MACHINE_HOURLY_COST;
import static com.qcadoo.mes.technologies.constants.TechnologyFields.OPERATION_COMPONENTS;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.costNormsForOperation.constants.CalculateOperationCostMode;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.operationTimeCalculations.dto.OperationTimes;
import com.qcadoo.mes.operationTimeCalculations.dto.OperationTimesContainer;
import com.qcadoo.mes.productionLines.ProductionLinesService;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.dto.ProductQuantitiesHolder;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.NumberService;

@Service
public class OperationsCostCalculationServiceImpl implements OperationsCostCalculationService {

    private static final String L_TOTAL_OPERATION_COST = "totalOperationCost";

    private static final String L_PIECES = "pieces";

    private static final String L_OPERATION_MARGIN_COST = "operationMarginCost";

    private static final String L_OPERATION_COST = "operationCost";

    private static final String L_PRODUCTION_LINE = "productionLine";

    private static final String L_QUANTITY = "quantity";

    private static final String L_TECHNOLOGY = "technology";

    private static final String L_ORDER = "order";

    private static final String L_CALCULATION_OPERATION_COMPONENTS = "calculationOperationComponents";

    private static final String L_COST_CALCULATION = "costCalculation";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductionLinesService productionLinesService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private OperationCostCalculationTreeBuilder operationCostCalculationTreeBuilder;

    private static final Set<String> PATH_COST_KEYS = Sets.newHashSet(LABOR_HOURLY_COST, MACHINE_HOURLY_COST);

    @Override
    public void calculateOperationsCost(final Entity entity) {
        checkArgument(entity != null, "entity is null");
        String modelName = entity.getDataDefinition().getName();
        checkArgument(L_COST_CALCULATION.equals(modelName) || "productionBalance".equals(modelName), "unsupported entity type");

        DataDefinition costCalculationDD = entity.getDataDefinition();

        CalculateOperationCostMode mode = CalculateOperationCostMode.parseString(entity
                .getStringField("calculateOperationCostsMode"));
        BigDecimal quantity = BigDecimalUtils.convertNullToZero(entity.getField(L_QUANTITY));
        BigDecimal margin = BigDecimalUtils.convertNullToZero(entity.getField("productionCostMargin"));

        Entity costCalculation = operationCostCalculationTreeBuilder.copyTechnologyTree(entity);

        Entity yetAnotherCostCalculation = costCalculationDD.save(costCalculation);
        Entity newCostCalculation = costCalculationDD.get(yetAnotherCostCalculation.getId());
        EntityTree calculationOperationComponents = newCostCalculation.getTreeField(L_CALCULATION_OPERATION_COMPONENTS);

        checkArgument(calculationOperationComponents != null, "given operation components is null");

        Entity technology = entity.getBelongsToField(L_TECHNOLOGY);
        Entity order = entity.getBelongsToField(L_ORDER);
        if (order != null) {
            Entity technologyFromOrder = order.getBelongsToField(L_TECHNOLOGY);
            technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyFromOrder.getId());
        }

        ProductQuantitiesHolder productQuantitiesAndOperationRuns = productQuantitiesService.getProductComponentQuantities(
                technology, quantity);

        if (CalculateOperationCostMode.PIECEWORK.equals(mode)) {
            if (calculationOperationComponents.isEmpty()) {
                entity.addError(entity.getDataDefinition().getField(L_ORDER), "costCalculation.lackOfTreeComponents");
                entity.addError(entity.getDataDefinition().getField(L_TECHNOLOGY), "costCalculation.lackOfTreeComponents");
                return;
            }
            BigDecimal totalPieceworkCost = estimateCostCalculationForPieceWork(calculationOperationComponents.getRoot(),
                    productQuantitiesAndOperationRuns.getOperationRuns(), margin, quantity);
            entity.setField("totalPieceworkCosts", numberService.setScale(totalPieceworkCost));
        } else if (CalculateOperationCostMode.HOURLY.equals(mode)) {
            Entity productionLine = entity.getBelongsToField(L_PRODUCTION_LINE);
            Boolean includeTPZ = entity.getBooleanField("includeTPZ");
            Boolean includeAdditionalTime = entity.getBooleanField("includeAdditionalTime");
            Map<Long, Integer> workstations = getWorkstationsMapsForOperationsComponent(costCalculation, productionLine);

            // Map<Entity, OperationWorkTime> realizationTimes = operationWorkTimeService.estimateOperationsWorkTime(
            // calculationOperationComponents, productQuantitiesService
            // .convertOperationsRunsFromProductQuantities(productQuantitiesAndOperationRuns.getOperationRuns()),
            // includeTPZ, includeAdditionalTime, workstations, true);
            OperationTimesContainer operationTimes = new OperationTimesContainer();
            operationTimes = operationWorkTimeService.estimateOperationsWorkTimes(calculationOperationComponents,
                    productQuantitiesAndOperationRuns.getOperationRuns(), includeTPZ, includeAdditionalTime, workstations, true);
            // FIXME MAKU
            // for (Entry<Entity, OperationWorkTime> operationAndTimeEntry : realizationTimes.entrySet()) {
            // operationTimes.add(operationAndTimeEntry.getKey(), operationAndTimeEntry.getValue());
            // }

            Map<String, BigDecimal> hourlyResultsMap = estimateCostCalculationForHourly(calculationOperationComponents.getRoot(),
                    margin, quantity, operationTimes);
            entity.setField("totalMachineHourlyCosts", numberService.setScale(hourlyResultsMap.get(MACHINE_HOURLY_COST)));
            entity.setField("totalLaborHourlyCosts", numberService.setScale(hourlyResultsMap.get(LABOR_HOURLY_COST)));
        } else {
            throw new IllegalStateException("Unsupported calculateOperationCostMode");
        }

        entity.setField(L_CALCULATION_OPERATION_COMPONENTS, calculationOperationComponents);
    }

    @Override
    public Map<String, BigDecimal> estimateCostCalculationForHourly(final EntityTreeNode calcOperComp, final BigDecimal margin,
            final BigDecimal plannedQuantity, final OperationTimesContainer realizationTimes) {
        checkArgument(calcOperComp != null, "given operationComponent is empty");
        Map<String, BigDecimal> resultsMap = Maps.newHashMapWithExpectedSize(PATH_COST_KEYS.size());
        MathContext mc = numberService.getMathContext();

        for (String key : PATH_COST_KEYS) {
            resultsMap.put(key, BigDecimal.ZERO);
        }

        for (EntityTreeNode child : calcOperComp.getChildren()) {
            Map<String, BigDecimal> unitResultsMap = estimateCostCalculationForHourly(child, margin, plannedQuantity,
                    realizationTimes);
            for (String key : PATH_COST_KEYS) {
                BigDecimal unitOperationCost = resultsMap.get(key).add(unitResultsMap.get(key), numberService.getMathContext());
                resultsMap.put(key, unitOperationCost);
            }
        }

        OperationTimes operationTimes = realizationTimes.get(calcOperComp.getId());
        Map<String, BigDecimal> costs = estimateHourlyCostCalculationSingleOperations(operationTimes, margin);
        savedGeneratedValues(costs, calcOperComp, true, operationTimes.getTimes(), null);

        resultsMap.put(MACHINE_HOURLY_COST, resultsMap.get(MACHINE_HOURLY_COST).add(costs.get("operationMachineCost"), mc));
        resultsMap.put(LABOR_HOURLY_COST, resultsMap.get(LABOR_HOURLY_COST).add(costs.get("operationLaborCost"), mc));
        return resultsMap;
    }

    private Map<String, BigDecimal> estimateHourlyCostCalculationSingleOperations(final OperationTimes operationTimes,
            final BigDecimal margin) {

        MathContext mc = numberService.getMathContext();

        Entity calcOperComp = operationTimes.getOperation();
        OperationWorkTime times = operationTimes.getTimes();

        Map<String, BigDecimal> results = new HashMap<String, BigDecimal>();
        BigDecimal hourlyMachineCost = BigDecimalUtils.convertNullToZero(calcOperComp.getField(MACHINE_HOURLY_COST));
        BigDecimal hourlyLaborCost = BigDecimalUtils.convertNullToZero(calcOperComp.getField(LABOR_HOURLY_COST));

        BigDecimal durationMachine = BigDecimal.valueOf(times.getMachineWorkTime());
        BigDecimal durationLabor = BigDecimal.valueOf(times.getLaborWorkTime());

        BigDecimal durationMachineInHours = durationMachine.divide(BigDecimal.valueOf(3600), mc);
        BigDecimal durationLaborInHours = durationLabor.divide(BigDecimal.valueOf(3600), mc);

        BigDecimal operationMachineCost = durationMachineInHours.multiply(hourlyMachineCost, mc);
        BigDecimal operationLaborCost = durationLaborInHours.multiply(hourlyLaborCost, mc);

        BigDecimal operationMachineCostIncludeMargin = operationMachineCost.add(operationMachineCost.multiply(
                margin.divide(BigDecimal.valueOf(100), numberService.getMathContext()), numberService.getMathContext()), mc);

        BigDecimal operationLaborCostIncludeMargin = operationLaborCost.add(
                operationLaborCost.multiply(margin.divide(BigDecimal.valueOf(100), numberService.getMathContext()), mc), mc);

        BigDecimal operationCost = operationMachineCost.add(operationLaborCost, mc);
        BigDecimal operationMarginCost = operationCost.multiply(
                margin.divide(BigDecimal.valueOf(100), numberService.getMathContext()), mc);

        results.put(L_OPERATION_COST, numberService.setScale(operationCost));
        results.put(L_OPERATION_MARGIN_COST, numberService.setScale(operationMarginCost));
        results.put("operationMachineCost", numberService.setScale(operationMachineCost));
        results.put("operationLaborCost", numberService.setScale(operationLaborCost));
        results.put("operationMachineCostIncludeMargin", numberService.setScale(operationMachineCostIncludeMargin));
        results.put("operationLaborCostIncludeMargin", numberService.setScale(operationLaborCostIncludeMargin));
        return results;

    }

    private void savedGeneratedValues(final Map<String, BigDecimal> costs, final Entity calcOperComp, boolean hourlyCosts,
            final OperationWorkTime times, final BigDecimal operationRuns) {
        if (hourlyCosts) {
            calcOperComp.setField("duration", new BigDecimal(times.getDuration(), numberService.getMathContext()));
            calcOperComp.setField("totalMachineOperationCost", costs.get("operationMachineCost"));
            calcOperComp.setField("totalLaborOperationCost", costs.get("operationLaborCost"));
            calcOperComp.setField("totalLaborOperationCostWithMargin", costs.get("operationLaborCostIncludeMargin"));
            calcOperComp.setField("totalMachineOperationCostWithMargin", costs.get("operationMachineCostIncludeMargin"));
        } else {
            calcOperComp.setField(L_PIECES, numberService.setScale(operationRuns));
        }
        BigDecimal operationCost = costs.get(L_OPERATION_COST);
        BigDecimal operationMarginCost = costs.get(L_OPERATION_MARGIN_COST);
        calcOperComp.setField(L_OPERATION_COST, numberService.setScale(operationCost));
        calcOperComp.setField(L_OPERATION_MARGIN_COST, numberService.setScale(operationMarginCost));
        calcOperComp.setField(L_TOTAL_OPERATION_COST,
                numberService.setScale(operationCost.add(operationMarginCost, numberService.getMathContext())));

        calcOperComp.getDataDefinition().save(calcOperComp);
    }

    @Override
    public BigDecimal estimateCostCalculationForPieceWork(final EntityTreeNode operationComponent,
            final Map<Long, BigDecimal> operationRunsMap, final BigDecimal margin, final BigDecimal plannedQuantity) {

        BigDecimal totalPieceworkCost = BigDecimal.ZERO;

        for (EntityTreeNode child : operationComponent.getChildren()) {
            totalPieceworkCost = totalPieceworkCost.add(
                    estimateCostCalculationForPieceWork(child, operationRunsMap, margin, plannedQuantity),
                    numberService.getMathContext());
        }
        // FIXME MAKU unnecessary mapping of whole entity - we need only their id! We can increase performance by replacing line
        // below by a query projection
        Entity techOperComp = operationComponent.getBelongsToField("technologyOperationComponent");

        BigDecimal operationRuns = operationRunsMap.get(techOperComp.getId());
        Map<String, BigDecimal> costs = estimatePieceworkCostCalculationSingleOperations(operationComponent, operationRuns,
                margin);
        totalPieceworkCost = totalPieceworkCost.add(costs.get(L_OPERATION_COST));
        savedGeneratedValues(costs, operationComponent, false, null, operationRuns);
        return totalPieceworkCost;
    }

    private Map<String, BigDecimal> estimatePieceworkCostCalculationSingleOperations(final EntityTreeNode calcOperComp,
            final BigDecimal operationRuns, final BigDecimal margin) {
        Map<String, BigDecimal> results = new HashMap<String, BigDecimal>();

        BigDecimal pieceworkCost = BigDecimalUtils.convertNullToZero(calcOperComp.getField("pieceworkCost"));
        BigDecimal numberOfOperations = BigDecimalUtils.convertNullToOne(calcOperComp.getField("numberOfOperations"));

        BigDecimal pieceworkCostPerOperation = pieceworkCost.divide(numberOfOperations, numberService.getMathContext());
        BigDecimal operationCost = operationRuns.multiply(pieceworkCostPerOperation, numberService.getMathContext());
        BigDecimal operationMarginCost = operationCost.multiply(margin.divide(BigDecimal.valueOf(100),
                numberService.getMathContext()));
        BigDecimal totalOperationCost = numberService.setScale(operationCost.add(operationMarginCost,
                numberService.getMathContext()));

        results.put(L_OPERATION_COST, numberService.setScale(operationCost));
        results.put(L_OPERATION_MARGIN_COST, numberService.setScale(operationMarginCost));
        results.put(L_PIECES, numberService.setScale(operationRuns));
        results.put(L_TOTAL_OPERATION_COST, totalOperationCost);

        return results;
    }

    private Map<Long, Integer> getWorkstationsMapsForOperationsComponent(final Entity costCalculation, final Entity productionLine) {
        Entity order = costCalculation.getBelongsToField(L_ORDER);
        if (order == null) {
            return getWorkstationsFromTechnology(costCalculation.getBelongsToField("technology"), productionLine);
        } else {
            return getWorkstationsFromOrder(order);
        }
    }

    private Map<Long, Integer> getWorkstationsFromTechnology(final Entity technology, final Entity productionLine) {
        Map<Long, Integer> workstations = new HashMap<Long, Integer>();
        for (Entity operComp : technology.getHasManyField(OPERATION_COMPONENTS)) {
            workstations.put(operComp.getId(), productionLinesService.getWorkstationTypesCount(operComp, productionLine));
        }
        return workstations;
    }

    private Map<Long, Integer> getWorkstationsFromOrder(final Entity order) {
        Map<Long, Integer> workstations = new HashMap<Long, Integer>();
        for (Entity operComp : order.getBelongsToField("technology").getHasManyField("operationComponents")) {
            workstations
                    .put(operComp.getId(),
                            getIntegerValue(operComp.getBelongsToField("techOperCompWorkstation").getField(
                                    "quantityOfWorkstationTypes")));
        }
        return workstations;
    }

    private Integer getIntegerValue(final Object value) {
        return value == null ? Integer.valueOf(0) : (Integer) value;
    }
}
