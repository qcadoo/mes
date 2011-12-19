/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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
package com.qcadoo.mes.costNormsForOperation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.costNormsForOperation.constants.CostNormsForOperationConstants.MODEL_CALCULATION_OPERATION_COMPONENT;
import static com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants.LABOR_HOURLY_COST;
import static com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants.MACHINE_HOURLY_COST;
import static com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants.PIECEWORK;
import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.math.BigDecimal.ROUND_UP;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.costNormsForOperation.constants.CostNormsForOperationConstants;
import com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants;
import com.qcadoo.mes.productionScheduling.constants.ProductionSchedulingConstants;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;

@Service
public class OperationsCostCalculationServiceImpl implements OperationsCostCalculationService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyService technologyService;

    private static final Logger LOG = LoggerFactory.getLogger(OperationsCostCalculationServiceImpl.class);

    private static final Set<String> PATH_COST_KEYS = Sets.newHashSet(LABOR_HOURLY_COST, MACHINE_HOURLY_COST);

    @Override
    public void calculateOperationsCost(final Entity costCalculation) {
        checkArgument(costCalculation != null, "costCalculation entity is null");
        checkArgument("costCalculation".equals(costCalculation.getDataDefinition().getName()), "unsupported entity type");

        DataDefinition costCalculationDD = dataDefinitionService.get("costCalculation", "costCalculation");

        OperationsCostCalculationConstants mode = getOperationModeFromField(costCalculation
                .getField("calculateOperationCostsMode"));
        BigDecimal quantity = getBigDecimal(costCalculation.getField("quantity"));
        Boolean includeTPZ = getBooleanFromField(costCalculation.getField("includeTPZ"));
        BigDecimal margin = getBigDecimal(costCalculation.getField("productionCostMargin"));

        copyTechnologyTree(costCalculation);

        Entity yetAnotherCostCalculation = costCalculationDD.save(costCalculation);
        Entity newCostCalculation = costCalculationDD.get(yetAnotherCostCalculation.getId());
        EntityTree operationComponents = newCostCalculation.getTreeField("calculationOperationComponents");

        checkArgument(operationComponents != null, "given operation components is null");

        if (mode == PIECEWORK) {
            BigDecimal totalPieceworkCost = estimateCostCalculationForPieceWork(operationComponents.getRoot(), margin, quantity);
            costCalculation.setField("totalPieceworkCosts", totalPieceworkCost);
        } else {
            Map<String, BigDecimal> hourlyResultsMap = estimateCostCalculationForHourly(operationComponents.getRoot(), margin,
                    quantity, includeTPZ);
            costCalculation.setField("totalMachineHourlyCosts", hourlyResultsMap.get(MACHINE_HOURLY_COST).setScale(3, ROUND_UP));
            costCalculation.setField("totalLaborHourlyCosts", hourlyResultsMap.get(LABOR_HOURLY_COST).setScale(3, ROUND_UP));
        }

        costCalculation.setField("calculationOperationComponents", operationComponents);
    }

    private Map<String, BigDecimal> estimateCostCalculationForHourly(final EntityTreeNode operationComponent,
            final BigDecimal margin, final BigDecimal plannedQuantity, final Boolean includeTPZ) {
        checkArgument(operationComponent != null, "given operationComponent is null");

        BigDecimal hourlyMachineCost = getBigDecimal(operationComponent.getField(MACHINE_HOURLY_COST));
        BigDecimal hourlyLaborCost = getBigDecimal(operationComponent.getField(LABOR_HOURLY_COST));
        BigDecimal numOfProducts = countNumberOfOutputProducts(operationComponent
                .getBelongsToField("technologyOperationComponent"));
        BigDecimal prodInOneCycle = getBigDecimal(operationComponent.getField("productionInOneCycle"));
        Map<String, BigDecimal> resultsMap = Maps.newHashMapWithExpectedSize(PATH_COST_KEYS.size());
        Map<String, BigDecimal> unitResultsMap;

        for (String key : PATH_COST_KEYS) {
            resultsMap.put(key, BigDecimal.ZERO);
        }

        for (EntityTreeNode child : operationComponent.getChildren()) {
            unitResultsMap = estimateCostCalculationForHourly(child, margin, plannedQuantity, includeTPZ);
            for (String key : PATH_COST_KEYS) {
                BigDecimal unitOperationCost = resultsMap.get(key).add(unitResultsMap.get(key));
                resultsMap.put(key, unitOperationCost);
            }
        }
        BigDecimal quantityOfComponents = numOfProducts.multiply(plannedQuantity);
        BigDecimal numOfCycles = quantityOfComponents.divide(prodInOneCycle).setScale(0, ROUND_UP);
        BigDecimal tj = getBigDecimal(operationComponent.getField("tj"));
        BigDecimal machineUtilization = getBigDecimal(operationComponent.getField("machineUtilization"));
        BigDecimal laborUtilization = getBigDecimal(operationComponent.getField("laborUtilization"));
        BigDecimal additionalTime = getBigDecimal(operationComponent.getField("timeNextOperation"));
        BigDecimal duration = (tj.multiply(numOfCycles)).add(additionalTime);
        if (includeTPZ) {
            duration = duration.add(getBigDecimal(operationComponent.getField("tpz")));
        }
        BigDecimal durationInHours = duration.divide(BigDecimal.valueOf(3600), 5, ROUND_HALF_UP);
        BigDecimal durationMachine = durationInHours.multiply(machineUtilization);
        BigDecimal durationLabor = durationInHours.multiply(laborUtilization);
        BigDecimal operationMachineCost = durationMachine.multiply(hourlyMachineCost);
        BigDecimal operationLaborCost = durationLabor.multiply(hourlyLaborCost);
        BigDecimal operationCost = operationMachineCost.add(operationLaborCost);
        BigDecimal operationMarginCost = operationCost.multiply(margin.divide(BigDecimal.valueOf(100), 5, ROUND_HALF_UP));

        operationComponent.setField("operationCost", operationCost.setScale(3, ROUND_UP));
        operationComponent.setField("operationMarginCost", operationMarginCost.setScale(3, ROUND_UP));
        operationComponent.setField("totalOperationCost", operationCost.add(operationMarginCost).setScale(3, ROUND_UP));
        operationComponent.setField("duration", duration.setScale(0, ROUND_UP).longValue());

        checkArgument(operationComponent.getDataDefinition().save(operationComponent).isValid(), "invalid operationComponent");

        // add child operations cost values to total cost of operation
        resultsMap.put(MACHINE_HOURLY_COST, resultsMap.get(MACHINE_HOURLY_COST).add(operationMachineCost));
        resultsMap.put(LABOR_HOURLY_COST, resultsMap.get(LABOR_HOURLY_COST).add(operationLaborCost));

        return resultsMap;
    }

    private BigDecimal countNumberOfOutputProducts(final Entity givenTechnologyOperation) {
        if (givenTechnologyOperation == null) {
            return BigDecimal.ZERO;
        }

        EntityList outProductsTree = givenTechnologyOperation.getHasManyField("operationProductOutComponents");
        Entity technology = givenTechnologyOperation.getBelongsToField("technology");
        for (Entity outProduct : outProductsTree) {
            Entity product = outProduct.getBelongsToField("product");
            if (!(technologyService.getProductType(product, technology).equals(TechnologyService.WASTE))) {
                return getBigDecimal(outProduct.getField("quantity"));
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal estimateCostCalculationForPieceWork(final EntityTreeNode operationComponent, final BigDecimal margin,
            final BigDecimal plannedQuantity) {

        BigDecimal pathCost = BigDecimal.ZERO;

        for (EntityTreeNode child : operationComponent.getChildren()) {
            pathCost = pathCost.add(estimateCostCalculationForPieceWork(child, margin, plannedQuantity));
        }

        BigDecimal pieceworkCost = getBigDecimal(operationComponent.getField("pieceworkCost"));
        BigDecimal numberOfOperations = getBigDecimal(operationComponent.getField("numberOfOperations"));
        BigDecimal numOfProducts = countNumberOfOutputProducts(operationComponent
                .getBelongsToField("technologyOperationComponent"));
        BigDecimal pieces = numOfProducts.multiply(plannedQuantity);
        BigDecimal pieceworkCostPerOperation = pieceworkCost.divide(numberOfOperations, 5, ROUND_HALF_UP);
        BigDecimal operationCost = pieces.multiply(pieceworkCostPerOperation);
        BigDecimal operationMarginCost = operationCost.multiply(margin.divide(BigDecimal.valueOf(100), 5, ROUND_HALF_UP));

        operationComponent.setField("pieces", pieces.setScale(3, ROUND_UP));
        operationComponent.setField("operationCost", operationCost.setScale(3, ROUND_UP));
        operationComponent.setField("operationMarginCost", operationMarginCost.setScale(3, ROUND_UP));
        operationComponent.setField("totalOperationCost", operationCost.add(operationMarginCost).setScale(3, ROUND_UP));

        checkArgument(operationComponent.getDataDefinition().save(operationComponent).isValid(), "invalid operationComponent");

        return operationCost.add(pathCost);
    }

    @Transactional
    private void copyTechnologyTree(final Entity costCalculation) {
        EntityTree sourceOperationComponents;

        deleteOperationsTreeIfExists(costCalculation);

        if (costCalculation.getBelongsToField("order") != null) {
            sourceOperationComponents = costCalculation.getBelongsToField("order").getTreeField("orderOperationComponents");
        } else {
            sourceOperationComponents = costCalculation.getBelongsToField("technology").getTreeField("operationComponents");
        }
        createTechnologyInstanceForCalculation(sourceOperationComponents, costCalculation);
    }

    public void createTechnologyInstanceForCalculation(final EntityTree sourceTree, final Entity costCalculation) {
        checkArgument(sourceTree != null, "source is null");
        DataDefinition calculationOperationComponentDD = dataDefinitionService.get(
                CostNormsForOperationConstants.PLUGIN_IDENTIFIER, MODEL_CALCULATION_OPERATION_COMPONENT);

        // drop old operation components tree
        EntityTree oldCalculationOperationComponents = costCalculation.getTreeField("calculationOperationComponents");
        if (oldCalculationOperationComponents != null && oldCalculationOperationComponents.getRoot() != null) {
            calculationOperationComponentDD.delete(oldCalculationOperationComponents.getRoot().getId());
        }

        costCalculation.setField("calculationOperationComponents", Collections.singletonList(createCalculationOperationComponent(
                sourceTree.getRoot(), null, calculationOperationComponentDD, costCalculation)));
    }

    private Entity createCalculationOperationComponent(final EntityTreeNode sourceTreeNode, final Entity parent,
            final DataDefinition calculationOperationComponentDD, final Entity costCalculation) {
        Entity calculationOperationComponent = calculationOperationComponentDD.create();

        calculationOperationComponent.setField("parent", parent);
        calculationOperationComponent.setField("costCalculation", costCalculation);

        if ("operation".equals(sourceTreeNode.getField("entityType"))) {
            createOrCopyCalculationOperationComponent(sourceTreeNode, calculationOperationComponentDD,
                    calculationOperationComponent, costCalculation);
        } else {
            Entity referenceTechnology = sourceTreeNode.getBelongsToField("referenceTechnology");
            createOrCopyCalculationOperationComponent(referenceTechnology.getTreeField("operationComponents").getRoot(),
                    calculationOperationComponentDD, calculationOperationComponent, costCalculation);
        }

        return calculationOperationComponent;
    }

    private void createOrCopyCalculationOperationComponent(final EntityTreeNode operationComponent,
            final DataDefinition calculationOperationComponentDD, final Entity calculationOperationComponent,
            final Entity costCalculation) {
        DataDefinition sourceDD = operationComponent.getDataDefinition();

        for (String fieldName : Arrays.asList("priority", "nodeNumber", "tpz", "tj", "productionInOneCycle", "countMachine",
                "timeNextOperation", "operationOffSet", "effectiveOperationRealizationTime", "effectiveDateFrom",
                "effectiveDateTo", "pieceworkCost", "laborHourlyCost", "machineHourlyCost", "numberOfOperations",
                "totalOperationCost", "laborUtilization", "machineUtilization")) {
            calculationOperationComponent.setField(fieldName, operationComponent.getField(fieldName));
        }

        calculationOperationComponent.setField("operation", operationComponent.getBelongsToField("operation"));

        calculationOperationComponent.setField("countRealized",
                operationComponent.getField("countRealized") != null ? operationComponent.getField("countRealized") : "01all");

        if (TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT.equals(sourceDD.getName())) {
            calculationOperationComponent.setField("technologyOperationComponent", operationComponent);
        } else if (ProductionSchedulingConstants.MODEL_ORDER_OPERATION_COMPONENT.equals(sourceDD.getName())) {
            calculationOperationComponent.setField("technologyOperationComponent",
                    operationComponent.getBelongsToField("technologyOperationComponent"));
        }

        calculationOperationComponent.setField("entityType", "operation");

        List<Entity> newOrderOperationComponents = new ArrayList<Entity>();

        for (EntityTreeNode child : operationComponent.getChildren()) {
            newOrderOperationComponents.add(createCalculationOperationComponent(child, calculationOperationComponent,
                    calculationOperationComponentDD, costCalculation));
        }

        calculationOperationComponent.setField("children", newOrderOperationComponents);
    }

    private void deleteOperationsTreeIfExists(final Entity costCalculation) {
        Entity yetAnotherCostCalculation = costCalculation.getDataDefinition().get(costCalculation.getId());
        EntityTree existingOperationsTree = yetAnotherCostCalculation.getTreeField("calculationOperationComponents");

        if (existingOperationsTree == null || existingOperationsTree.getRoot() == null) {
            return;
        }

        debug("existing calculation operation components tree will be removed..");
        EntityTreeNode existingOperationsTreeRoot = existingOperationsTree.getRoot();
        existingOperationsTreeRoot.getDataDefinition().delete(existingOperationsTreeRoot.getId());
    }

    private BigDecimal getBigDecimal(final Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        // MAKU - using BigDecimal.valueOf(Double) instead of new BigDecimal(String) to prevent issue described at
        // https://forums.oracle.com/forums/thread.jspa?threadID=2251030
        return BigDecimal.valueOf(Double.valueOf(value.toString()));
    }

    private Boolean getBooleanFromField(final Object value) {
        if ("1".equals(value) || "true".equals(value)) {
            return true;
        }
        return false;
    }

    private OperationsCostCalculationConstants getOperationModeFromField(final Object value) {
        checkArgument(value != null, "field value is null");
        String strValue = value.toString();
        return OperationsCostCalculationConstants.valueOf(strValue.toUpperCase());
    }

    private void debug(final String message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(message);
        }
    }

}
