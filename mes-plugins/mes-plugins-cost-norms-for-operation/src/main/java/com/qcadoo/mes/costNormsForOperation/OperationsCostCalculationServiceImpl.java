/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
import static java.math.BigDecimal.ROUND_UP;
import static java.util.Arrays.asList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
import com.qcadoo.mes.productionScheduling.OrderRealizationTimeService;
import com.qcadoo.mes.productionScheduling.constants.ProductionSchedulingConstants;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.NumberService;

@Service
public class OperationsCostCalculationServiceImpl implements OperationsCostCalculationService {

    private static final String QUANTITY_FIELD = "quantity";

    private static final String PRODUCT_FIELD = "product";

    private static final String TECHNOLOGY_FIELD = "technology";

    private static final String ORDER_L = "order";

    private static final String OPERATION_FIELD = "operation";

    private static final String ENTITY_TYPE_FIELD = "entityType";

    private static final String PRODUCTION_IN_ONE_CYCLE_FIELD = "productionInOneCycle";

    private static final String TECHNOLOGY_OPERATION_COMPONENT_FIELD = "technologyOperationComponent";

    private static final String CALCULATION_OPERATION_COMPONENTS_FIELD = "calculationOperationComponents";

    private static final String COST_CALCULATION_FIELD = "costCalculation";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private OrderRealizationTimeService orderRealizationTimeService;

    @Autowired
    private NumberService numberService;

    private static final Logger LOG = LoggerFactory.getLogger(OperationsCostCalculationServiceImpl.class);

    private static final Set<String> PATH_COST_KEYS = Sets.newHashSet(LABOR_HOURLY_COST, MACHINE_HOURLY_COST);

    @Override
    public void calculateOperationsCost(final Entity costCalculation) {
        checkArgument(costCalculation != null, "costCalculation entity is null");
        checkArgument(COST_CALCULATION_FIELD.equals(costCalculation.getDataDefinition().getName()), "unsupported entity type");

        DataDefinition costCalculationDD = dataDefinitionService.get(COST_CALCULATION_FIELD, COST_CALCULATION_FIELD);

        OperationsCostCalculationConstants mode = getOperationModeFromField(costCalculation
                .getField("calculateOperationCostsMode"));
        BigDecimal quantity = getBigDecimal(costCalculation.getField(QUANTITY_FIELD));
        Boolean includeTPZ = costCalculation.getBooleanField("includeTPZ");
        BigDecimal margin = getBigDecimal(costCalculation.getField("productionCostMargin"));

        copyTechnologyTree(costCalculation);

        Entity yetAnotherCostCalculation = costCalculationDD.save(costCalculation);
        Entity newCostCalculation = costCalculationDD.get(yetAnotherCostCalculation.getId());
        EntityTree operationComponents = newCostCalculation.getTreeField(CALCULATION_OPERATION_COMPONENTS_FIELD);

        checkArgument(operationComponents != null, "given operation components is null");

        Entity entity = costCalculation.getBelongsToField("technology");
        Entity order = costCalculation.getBelongsToField("order");
        if (order != null) {
            entity = order;
        }

        if (mode == PIECEWORK) {
            BigDecimal totalPieceworkCost = estimateCostCalculationForPieceWork(operationComponents.getRoot(), margin, quantity);
            costCalculation.setField("totalPieceworkCosts", totalPieceworkCost);
        } else {
            Map<Entity, Integer> realizationTimes = orderRealizationTimeService.estimateRealizationTimes(entity, quantity,
                    includeTPZ);

            Map<String, BigDecimal> hourlyResultsMap = estimateCostCalculationForHourly(operationComponents.getRoot(), margin,
                    quantity, realizationTimes);
            costCalculation
                    .setField("totalMachineHourlyCosts", numberService.setScale(hourlyResultsMap.get(MACHINE_HOURLY_COST)));
            costCalculation.setField("totalLaborHourlyCosts", numberService.setScale(hourlyResultsMap.get(LABOR_HOURLY_COST)));
        }

        costCalculation.setField(CALCULATION_OPERATION_COMPONENTS_FIELD, operationComponents);
    }

    private Map<String, BigDecimal> estimateCostCalculationForHourly(final EntityTreeNode operationComponent,
            final BigDecimal margin, final BigDecimal plannedQuantity, final Map<Entity, Integer> realizationTimes) {
        checkArgument(operationComponent != null, "given operationComponent is null");

        BigDecimal hourlyMachineCost = getBigDecimal(operationComponent.getField(MACHINE_HOURLY_COST));
        BigDecimal hourlyLaborCost = getBigDecimal(operationComponent.getField(LABOR_HOURLY_COST));
        Map<String, BigDecimal> resultsMap = Maps.newHashMapWithExpectedSize(PATH_COST_KEYS.size());

        for (String key : PATH_COST_KEYS) {
            resultsMap.put(key, BigDecimal.ZERO);
        }

        for (EntityTreeNode child : operationComponent.getChildren()) {
            Map<String, BigDecimal> unitResultsMap = estimateCostCalculationForHourly(child, margin, plannedQuantity,
                    realizationTimes);
            for (String key : PATH_COST_KEYS) {
                BigDecimal unitOperationCost = resultsMap.get(key).add(unitResultsMap.get(key), numberService.getMathContext());
                resultsMap.put(key, unitOperationCost);
            }
        }
        BigDecimal machineUtilization = getBigDecimal(operationComponent.getField("machineUtilization"));
        BigDecimal laborUtilization = getBigDecimal(operationComponent.getField("laborUtilization"));

        Entity techOperComp = operationComponent.getBelongsToField("technologyOperationComponent");
        int dur = realizationTimes.get(techOperComp);
        BigDecimal duration = BigDecimal.valueOf(dur);

        BigDecimal durationInHours = duration.divide(BigDecimal.valueOf(3600), numberService.getMathContext());
        BigDecimal durationMachine = durationInHours.multiply(machineUtilization, numberService.getMathContext());
        BigDecimal durationLabor = durationInHours.multiply(laborUtilization, numberService.getMathContext());
        BigDecimal operationMachineCost = durationMachine.multiply(hourlyMachineCost, numberService.getMathContext());
        BigDecimal operationLaborCost = durationLabor.multiply(hourlyLaborCost, numberService.getMathContext());
        BigDecimal operationCost = operationMachineCost.add(operationLaborCost, numberService.getMathContext());
        BigDecimal operationMarginCost = operationCost.multiply(
                margin.divide(BigDecimal.valueOf(100), numberService.getMathContext()), numberService.getMathContext());

        operationComponent.setField("operationCost", numberService.setScale(operationCost));
        operationComponent.setField("operationMarginCost", numberService.setScale(operationMarginCost));
        operationComponent.setField("totalOperationCost",
                numberService.setScale(operationCost.add(operationMarginCost, numberService.getMathContext())));
        operationComponent.setField("duration", duration.setScale(0, ROUND_UP).longValue());

        checkArgument(operationComponent.getDataDefinition().save(operationComponent).isValid(), "invalid operationComponent");

        resultsMap.put(MACHINE_HOURLY_COST,
                resultsMap.get(MACHINE_HOURLY_COST).add(operationMachineCost, numberService.getMathContext()));
        resultsMap.put(LABOR_HOURLY_COST,
                resultsMap.get(LABOR_HOURLY_COST).add(operationLaborCost, numberService.getMathContext()));

        return resultsMap;
    }

    private BigDecimal countNumberOfOutputProducts(final Entity givenTechnologyOperation) {
        if (givenTechnologyOperation == null) {
            return BigDecimal.ZERO;
        }

        EntityList outProductsTree = givenTechnologyOperation.getHasManyField("operationProductOutComponents");
        Entity technology = givenTechnologyOperation.getBelongsToField(TECHNOLOGY_FIELD);
        for (Entity outProduct : outProductsTree) {
            Entity product = outProduct.getBelongsToField(PRODUCT_FIELD);
            if (!(technologyService.getProductType(product, technology).equals(TechnologyService.WASTE))) {
                return getBigDecimal(outProduct.getField(QUANTITY_FIELD));
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal estimateCostCalculationForPieceWork(final EntityTreeNode operationComponent, final BigDecimal margin,
            final BigDecimal plannedQuantity) {

        BigDecimal pathCost = BigDecimal.ZERO;

        for (EntityTreeNode child : operationComponent.getChildren()) {
            pathCost = pathCost.add(estimateCostCalculationForPieceWork(child, margin, plannedQuantity),
                    numberService.getMathContext());
        }

        BigDecimal pieceworkCost = getBigDecimal(operationComponent.getField("pieceworkCost"));
        BigDecimal numberOfOperations = getBigDecimal(operationComponent.getField("numberOfOperations"));
        BigDecimal numOfProducts = countNumberOfOutputProducts(operationComponent
                .getBelongsToField(TECHNOLOGY_OPERATION_COMPONENT_FIELD));
        BigDecimal pieces = numOfProducts.multiply(plannedQuantity, numberService.getMathContext());
        BigDecimal pieceworkCostPerOperation = pieceworkCost.divide(numberOfOperations, numberService.getMathContext());
        BigDecimal operationCost = pieces.multiply(pieceworkCostPerOperation, numberService.getMathContext());
        BigDecimal operationMarginCost = operationCost.multiply(margin.divide(BigDecimal.valueOf(100),
                numberService.getMathContext()));

        operationComponent.setField("pieces", numberService.setScale(pieces));
        operationComponent.setField("operationCost", numberService.setScale(operationCost));
        operationComponent.setField("operationMarginCost", numberService.setScale(operationMarginCost));
        operationComponent.setField("totalOperationCost",
                numberService.setScale(operationCost.add(operationMarginCost, numberService.getMathContext())));

        checkArgument(operationComponent.getDataDefinition().save(operationComponent).isValid(), "invalid operationComponent");

        return operationCost.add(pathCost, numberService.getMathContext());
    }

    @Transactional
    private void copyTechnologyTree(final Entity costCalculation) {
        EntityTree sourceOperationComponents;

        deleteOperationsTreeIfExists(costCalculation);

        if (costCalculation.getBelongsToField(ORDER_L) == null) {
            sourceOperationComponents = costCalculation.getBelongsToField(TECHNOLOGY_FIELD).getTreeField("operationComponents");
        } else {
            sourceOperationComponents = costCalculation.getBelongsToField(ORDER_L).getTreeField("orderOperationComponents");
        }

        createTechnologyInstanceForCalculation(sourceOperationComponents, costCalculation);
    }

    public void createTechnologyInstanceForCalculation(final EntityTree sourceTree, final Entity costCalculation) {
        checkArgument(sourceTree != null, "source is null");
        DataDefinition calculationOperationComponentDD = dataDefinitionService.get(
                CostNormsForOperationConstants.PLUGIN_IDENTIFIER, MODEL_CALCULATION_OPERATION_COMPONENT);

        // drop old operation components tree
        EntityTree oldCalculationOperationComponents = costCalculation.getTreeField(CALCULATION_OPERATION_COMPONENTS_FIELD);
        if (oldCalculationOperationComponents != null && oldCalculationOperationComponents.getRoot() != null) {
            calculationOperationComponentDD.delete(oldCalculationOperationComponents.getRoot().getId());
        }

        Entity tree = createCalculationOperationComponent(sourceTree.getRoot(), null, calculationOperationComponentDD,
                costCalculation);

        costCalculation.setField(CALCULATION_OPERATION_COMPONENTS_FIELD, asList(tree));
    }

    private Entity createCalculationOperationComponent(final EntityTreeNode sourceTreeNode, final Entity parent,
            final DataDefinition calculationOperationComponentDD, final Entity costCalculation) {
        Entity calculationOperationComponent = calculationOperationComponentDD.create();

        calculationOperationComponent.setField("parent", parent);
        calculationOperationComponent.setField(COST_CALCULATION_FIELD, costCalculation);

        if (OPERATION_FIELD.equals(sourceTreeNode.getField(ENTITY_TYPE_FIELD))) {
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

        for (String fieldName : Arrays.asList("priority", "nodeNumber", "tpz", "tj", PRODUCTION_IN_ONE_CYCLE_FIELD,
                "countMachine", "timeNextOperation", "operationOffSet", "effectiveOperationRealizationTime", "effectiveDateFrom",
                "effectiveDateTo", "pieceworkCost", "laborHourlyCost", "machineHourlyCost", "numberOfOperations",
                "totalOperationCost", "laborUtilization", "machineUtilization")) {
            calculationOperationComponent.setField(fieldName, operationComponent.getField(fieldName));
        }

        calculationOperationComponent.setField(OPERATION_FIELD, operationComponent.getBelongsToField(OPERATION_FIELD));

        calculationOperationComponent.setField("countRealized", operationComponent.getField("countRealized") == null ? "01all"
                : operationComponent.getField("countRealized"));

        if (TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT.equals(sourceDD.getName())) {
            calculationOperationComponent.setField(TECHNOLOGY_OPERATION_COMPONENT_FIELD, operationComponent);
        } else if (ProductionSchedulingConstants.MODEL_ORDER_OPERATION_COMPONENT.equals(sourceDD.getName())) {
            calculationOperationComponent.setField(TECHNOLOGY_OPERATION_COMPONENT_FIELD,
                    operationComponent.getBelongsToField(TECHNOLOGY_OPERATION_COMPONENT_FIELD));
        }

        calculationOperationComponent.setField(ENTITY_TYPE_FIELD, OPERATION_FIELD);

        List<Entity> newOrderOperationComponents = new ArrayList<Entity>();

        for (EntityTreeNode child : operationComponent.getChildren()) {
            newOrderOperationComponents.add(createCalculationOperationComponent(child, calculationOperationComponent,
                    calculationOperationComponentDD, costCalculation));
        }

        calculationOperationComponent.setField("children", newOrderOperationComponents);
    }

    private void deleteOperationsTreeIfExists(final Entity costCalculation) {
        Entity yetAnotherCostCalculation = costCalculation.getDataDefinition().get(costCalculation.getId());
        EntityTree existingOperationsTree = yetAnotherCostCalculation.getTreeField(CALCULATION_OPERATION_COMPONENTS_FIELD);

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

    private OperationsCostCalculationConstants getOperationModeFromField(final Object value) {
        checkArgument(value != null, "field value is null");
        String strValue = value.toString();
        return OperationsCostCalculationConstants.valueOf(strValue.toUpperCase(Locale.ENGLISH));
    }

    private void debug(final String message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(message);
        }
    }

}
