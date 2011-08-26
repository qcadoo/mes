package com.qcadoo.mes.costNormsForOperation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants.HOURLY;
import static com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants.PIECEWORK;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants;
import com.qcadoo.mes.productionScheduling.OrderRealizationTimeService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.EntityTreeNode;

public class OperationsCostCalculationServiceImpl implements OperationsCostCalculationService {

    @Autowired
    private OrderRealizationTimeService orderRealizationTimeService;

    private static final String OPERATION_NODE_ENTITY_TYPE = "operation";

    @Override
    public Map<String, BigDecimal> calculateOperationsCost(Entity source, OperationsCostCalculationConstants mode,
            boolean includeTPZs, BigDecimal quantity) {
        checkArgument(quantity != null, "quantity is null");
        checkArgument(quantity.compareTo(BigDecimal.valueOf(0)) == 1, "quantity should be greather than 0");
        BigDecimal totalMachineHourlyCost = new BigDecimal(0);
        BigDecimal totalLaborHourlyCost = new BigDecimal(0);
        BigDecimal totalPieceWorkCost = new BigDecimal(0);
        Map<String, BigDecimal> result = new HashMap<String, BigDecimal>();

        EntityTree operationComponents = source.getTreeField("operationComponents");
        if (operationComponents == null) {
            throw new IllegalArgumentException("Incompatible source entity type..");
        }

        if (mode == PIECEWORK) {
            totalPieceWorkCost = estimateCostCalculationForPieceWork(operationComponents.getRoot(), quantity, includeTPZs);
        }
        if (mode == HOURLY) {
            int time = orderRealizationTimeService.estimateRealizationTimeForOperation(operationComponents.getRoot(), quantity,
                    includeTPZs);
            if (time == 0) {
                totalLaborHourlyCost = new BigDecimal(0);
                totalMachineHourlyCost = new BigDecimal(0);
            } else {
                totalLaborHourlyCost = estimateCostCalculationForHourly(operationComponents.getRoot(), quantity, includeTPZs,
                        OperationsCostCalculationConstants.LABOR_HOURLY_COST, time);
                totalMachineHourlyCost = estimateCostCalculationForHourly(operationComponents.getRoot(), quantity, includeTPZs,
                        OperationsCostCalculationConstants.MACHINE_HOURLY_COST, time);
            }
        }
        result.put("machineHourlyCost", totalMachineHourlyCost);
        result.put("laborHourlyCost", totalLaborHourlyCost);
        result.put("pieceWorkCost", totalPieceWorkCost);
        return result;
    }

    public BigDecimal estimateCostCalculationForHourly(final EntityTreeNode operationComponent, final BigDecimal plannedQuantity,
            Boolean includeTpz, String name, int time) {

        if (operationComponent.getField("entityType") != null
                && !OPERATION_NODE_ENTITY_TYPE.equals(operationComponent.getField("entityType"))) {
            return estimateCostCalculationForHourly(
                    operationComponent.getBelongsToField("referenceTechnology").getTreeField("operationComponents").getRoot(),
                    plannedQuantity, includeTpz, name, time);
        } else {
            BigDecimal pathCost = new BigDecimal(0);
            for (EntityTreeNode child : operationComponent.getChildren()) {
                BigDecimal tmpPathCost = estimateCostCalculationForHourly(child, plannedQuantity, includeTpz, name, time);
                if (tmpPathCost.compareTo(pathCost) == 1) {
                    pathCost = tmpPathCost;
                }
            }
            BigDecimal realizationTime = BigDecimal.valueOf(time);
            BigDecimal hourlyCost = (BigDecimal) operationComponent.getField(name);
            if (hourlyCost == null) {
                hourlyCost = new BigDecimal(0);
            }
            BigDecimal operationCost = realizationTime.multiply(hourlyCost);

            pathCost = pathCost.add(operationCost);

            return pathCost;
        }
    }

    private BigDecimal estimateCostCalculationForPieceWork(final EntityTreeNode operationComponent,
            final BigDecimal plannedQuantity, Boolean includeTpz) {

        if (operationComponent.getField("entityType") != null
                && !OPERATION_NODE_ENTITY_TYPE.equals(operationComponent.getField("entityType"))) {
            return estimateCostCalculationForPieceWork(
                    operationComponent.getBelongsToField("referenceTechnology").getTreeField("operationComponents").getRoot(),
                    plannedQuantity, includeTpz);
        } else {
            BigDecimal operationCost = new BigDecimal(0);
            BigDecimal pathCost = new BigDecimal(0);
            for (EntityTreeNode child : operationComponent.getChildren()) {
                BigDecimal tmpPathCost = estimateCostCalculationForPieceWork(child, plannedQuantity, includeTpz);
                if (tmpPathCost.compareTo(pathCost) == 1) {
                    pathCost = tmpPathCost;
                }
            }
            BigDecimal piecework = (BigDecimal) operationComponent.getField("pieceworkCost");
            if (piecework == null) {
                piecework = new BigDecimal(0);
            }
            BigDecimal numberOfOperations = (BigDecimal) operationComponent.getField("numberOfOperations");
            if (numberOfOperations == null) {
                numberOfOperations = new BigDecimal(1);
            }
            BigDecimal pieceWorkCost = piecework.divide(numberOfOperations);

            EntityList outputProducts = operationComponent.getHasManyField("operationProductOutComponents");
            BigDecimal totalQuantityOutputProduct = new BigDecimal(0);
            for (Entity outputProduct : outputProducts) {
                BigDecimal quantity = (BigDecimal) outputProduct.getField("quantity");
                totalQuantityOutputProduct = totalQuantityOutputProduct.add(quantity);
            }
            operationCost = operationCost.add(pieceWorkCost.multiply(totalQuantityOutputProduct));

            pathCost = pathCost.add(operationCost);
            return pathCost;
        }
    }

    /*
     * private EntityTree getOperationComponentsTree(final Entity source) { checkArgument(source != null, "source is null");
     * DataDefinition dd = source.getDataDefinition(); if (dd == null) { return null; } Entity operationComponentsProvider =
     * source; if ("order".equals(dd.getName())) { operationComponentsProvider =
     * operationComponentsProvider.getBelongsToField("technology"); if (operationComponentsProvider != null) { return
     * operationComponentsProvider.getTreeField("operationComponents"); } } if ("technology".equals(dd.getName())) { return
     * operationComponentsProvider.getTreeField("operationComponents"); } return null; } private BigDecimal checkData(Entity
     * operationComponent, String name) { BigDecimal data = (BigDecimal) operationComponent.getField(name); if (data == null) {
     * data = (BigDecimal) operationComponent.getBelongsToField("operation").getField(name); if (data == null) { if
     * ("numberOfOperations".equals(name)) { data = new BigDecimal(1); } else { data = new BigDecimal(0); } } } return data; }
     * private BigDecimal calculateCostPieceWork(EntityTree operationComponents) { BigDecimal totalPieceWorkCost = new
     * BigDecimal(0); for (Entity operationComponent : operationComponents) { BigDecimal piecework = checkData(operationComponent,
     * "pieceworkCost"); BigDecimal numberOfOperations = checkData(operationComponent, "numberOfOperations"); BigDecimal
     * pieceWorkCost = piecework.divide(numberOfOperations); EntityList outputProducts =
     * operationComponent.getHasManyField("operationProductOutComponents"); BigDecimal totalQuantityOutputProduct = new
     * BigDecimal(0); for (Entity outputProduct : outputProducts) { totalQuantityOutputProduct =
     * totalQuantityOutputProduct.add((BigDecimal) outputProduct.getField("quantity")); } totalPieceWorkCost =
     * totalPieceWorkCost.add(pieceWorkCost.multiply(totalQuantityOutputProduct)); } return totalPieceWorkCost; } private
     * BigDecimal calculateCostHourly(EntityTree operationComponents, int time, String name) { BigDecimal realizationTime =
     * BigDecimal.valueOf(time); BigDecimal totalHourlyCost = new BigDecimal(0); for (Entity operationComponent :
     * operationComponents) { BigDecimal hourlyCost = checkData(operationComponent, name); realizationTime =
     * realizationTime.multiply(hourlyCost); totalHourlyCost = totalHourlyCost.add(realizationTime); } return totalHourlyCost; }
     */

}
