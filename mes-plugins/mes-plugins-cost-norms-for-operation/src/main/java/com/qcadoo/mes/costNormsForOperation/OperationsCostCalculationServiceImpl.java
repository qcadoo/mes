package com.qcadoo.mes.costNormsForOperation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.costNormsForOperation.constants.CostNormsForOperationConstants.MODEL_CALCULATION_OPERATION_COMPONENT;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_TECHNOLOGY;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costNormsForOperation.constants.CostNormsForOperationConstants;
import com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants;
import com.qcadoo.mes.productionScheduling.OrderRealizationTimeService;
import com.qcadoo.mes.productionScheduling.constants.ProductionSchedulingConstants;
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
    private OrderRealizationTimeService orderRealizationTimeService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public Map<String, BigDecimal> calculateOperationsCost(Entity source,
            OperationsCostCalculationConstants calculateOperationCostsMode, boolean includeTPZ, BigDecimal quantity) {
        checkArgument(quantity != null, "quantity is null");
        checkArgument(quantity.compareTo(BigDecimal.valueOf(0)) == 1, "quantity should be greather than 0");
        checkArgument(source != null, "source entity is null");
        EntityTree operationComponents;
        BigDecimal totalMachineHourlyCost = BigDecimal.ZERO;
        BigDecimal totalLaborHourlyCost = BigDecimal.ZERO;
        BigDecimal totalPieceWorkCost = BigDecimal.ZERO;
        Map<String, BigDecimal> result = new HashMap<String, BigDecimal>();

        DataDefinition dataDefinition = source.getDataDefinition();

        if (MODEL_ORDER.equals(dataDefinition.getName())) {
            operationComponents = source.getTreeField("orderOperationComponents");
        } else {
            operationComponents = source.getTreeField("operationComponents");
        }
        if (operationComponents == null) {
            throw new IllegalArgumentException("Incompatible source entity type..");
        }

        if (calculateOperationCostsMode == OperationsCostCalculationConstants.PIECEWORK) {
            totalPieceWorkCost = estimateCostCalculationForPieceWork(operationComponents.getRoot(), quantity, includeTPZ);
        }
        if (calculateOperationCostsMode == OperationsCostCalculationConstants.HOURLY) {

            totalLaborHourlyCost = estimateCostCalculationForHourly(operationComponents.getRoot(), quantity, includeTPZ,
                    OperationsCostCalculationConstants.LABOR_HOURLY_COST);
            totalMachineHourlyCost = estimateCostCalculationForHourly(operationComponents.getRoot(), quantity, includeTPZ,
                    OperationsCostCalculationConstants.MACHINE_HOURLY_COST);
        }
        result.put("totalMachineHourlyCosts", totalMachineHourlyCost);
        result.put("totalLaborHourlyCosts", totalLaborHourlyCost);
        result.put("totalPieceworkCosts", totalPieceWorkCost);
        return result;
    }

    public BigDecimal estimateCostCalculationForHourly(final EntityTreeNode operationComponent, final BigDecimal plannedQuantity,
            Boolean includeTPZ, String hourly) {
        BigDecimal pathCost = BigDecimal.ZERO;
        for (EntityTreeNode child : operationComponent.getChildren()) {
            BigDecimal tmpPathCost = estimateCostCalculationForHourly(child, plannedQuantity, includeTPZ, hourly);
            if (tmpPathCost.compareTo(pathCost) == 1) {
                pathCost = tmpPathCost;
            }
        }
        Double time = new Double(orderRealizationTimeService.estimateRealizationTimeForOperation(operationComponent,
                plannedQuantity, includeTPZ));
        BigDecimal realizationTime = BigDecimal.valueOf(time / 3600);
        BigDecimal hourlyCost = (BigDecimal) operationComponent.getField(hourly);
        if (hourlyCost == null) {
            hourlyCost = BigDecimal.ZERO;
        }

        // BigDecimal hourlyCost = getHourlyCost(operationComponent, hourly);
        BigDecimal operationCost = realizationTime.multiply(hourlyCost).setScale(8, BigDecimal.ROUND_UP);
        pathCost = pathCost.add(operationCost);
        return pathCost;
    }

    private BigDecimal estimateCostCalculationForPieceWork(final EntityTreeNode operationComponent,
            final BigDecimal plannedQuantity, Boolean includeTPZ) {

        BigDecimal operationCost = BigDecimal.ZERO;
        BigDecimal pathCost = BigDecimal.ZERO;
        for (EntityTreeNode child : operationComponent.getChildren()) {
            BigDecimal tmpPathCost = estimateCostCalculationForPieceWork(child, plannedQuantity, includeTPZ);
            if (tmpPathCost.compareTo(pathCost) == 1) {
                pathCost = tmpPathCost;
            }
        }
        BigDecimal piecework = (BigDecimal) operationComponent.getField("pieceworkCost");
        BigDecimal numberOfOperations = new BigDecimal(operationComponent.getField("numberOfOperations").toString());
        if (piecework == null) {
            piecework = BigDecimal.ZERO;
            numberOfOperations = new BigDecimal(1);
        }

        BigDecimal pieceWorkCost = piecework.divide(numberOfOperations, 3);
        BigDecimal totalQuantityOutputProduct = BigDecimal.ZERO;
        EntityList outputProducts = operationComponent.getHasManyField("operationProductOutComponents");

        if (outputProducts == null) {
            outputProducts = operationComponent.getBelongsToField("technology").getTreeField("operationComponents").getRoot()
                    .getHasManyField("operationProductOutComponents");
        }
        if (outputProducts != null && !outputProducts.isEmpty()) {
            for (Entity outputProduct : outputProducts) {
                totalQuantityOutputProduct = totalQuantityOutputProduct.add((BigDecimal) outputProduct.getField("quantity"));

            }
            operationCost = operationCost.add(pieceWorkCost.multiply(totalQuantityOutputProduct))
                    .setScale(4, BigDecimal.ROUND_UP);
        }

        pathCost = operationCost.multiply(plannedQuantity);
        return pathCost;
    }

    public EntityTree createTechnologyInstanceForCalculation(final Entity source) {
        DataDefinition sourceDD = source.getDataDefinition();
        DataDefinition calculationOperationComponentDD = dataDefinitionService.get(
                CostNormsForOperationConstants.PLUGIN_IDENTIFIER, MODEL_CALCULATION_OPERATION_COMPONENT);
        EntityTree sourceTree;

        if (MODEL_TECHNOLOGY.equals(sourceDD.getName())) {
            sourceTree = source.getTreeField("operationComponents");
        } else if (MODEL_ORDER.equals(sourceDD.getName())) {
            sourceTree = source.getTreeField("orderOperationComponents");
        } else {
            throw new IllegalArgumentException("Unsupported source entity type - " + sourceDD.getName());
        }

        return (EntityTree) Collections.singletonList(createCalculationOperationComponent(sourceTree.getRoot(), null,
                calculationOperationComponentDD));
    }

    private Entity createCalculationOperationComponent(final EntityTreeNode sourceTree, final Entity parent,
            final DataDefinition calculationOperationComponentDD) {
        Entity calculationOperationComponent = calculationOperationComponentDD.create();

        calculationOperationComponent.setField("parent", parent);

        if ("operation".equals(sourceTree.getField("entityType"))) {
            createOrCopyCalculationOperationComponent(sourceTree, calculationOperationComponentDD, calculationOperationComponent);
        } else {
            Entity referenceTechnology = sourceTree.getBelongsToField("referenceTechnology");
            createOrCopyCalculationOperationComponent(referenceTechnology.getTreeField("operationComponents").getRoot(),
                    calculationOperationComponentDD, calculationOperationComponent);
        }

        return calculationOperationComponent;
    }

    private void createOrCopyCalculationOperationComponent(final EntityTreeNode operationComponent,
            final DataDefinition calculationOperationComponentDD, final Entity calculationOperationComponent) {
        DataDefinition sourceDD = operationComponent.getDataDefinition();

        for (String fieldName : Arrays.asList("operation", "priority", "tpz", "tj", "productionInOneCycle", "countMachine",
                "timeNextOperation", "operationOffSet", "effectiveOperationRealizationTime", "effectiveDateFrom",
                "effectiveDateTo", "pieceworkCost", "laborHourlyCost", "machineHourlyCost", "numberOfOperations",
                "totalOperationCost")) {
            calculationOperationComponent.setField(fieldName, operationComponent.getField(fieldName));
        }

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
                    calculationOperationComponentDD));
        }

        calculationOperationComponent.setField("children", newOrderOperationComponents);
    }
}
