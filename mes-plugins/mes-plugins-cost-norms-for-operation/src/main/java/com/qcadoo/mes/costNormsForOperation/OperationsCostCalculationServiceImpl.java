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
    public Map<String, BigDecimal> calculateOperationsCost(Entity source,
            OperationsCostCalculationConstants calculateOperationCostsMode, boolean includeTPZ, BigDecimal quantity) {
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

        if (calculateOperationCostsMode == PIECEWORK) {
            totalPieceWorkCost = estimateCostCalculationForPieceWork(operationComponents.getRoot(), quantity, includeTPZ);
        }
        if (calculateOperationCostsMode == HOURLY) {
            int time = orderRealizationTimeService.estimateRealizationTimeForOperation(operationComponents.getRoot(), quantity,
                    includeTPZ);
            if (time == 0) {
                totalLaborHourlyCost = new BigDecimal(0);
                totalMachineHourlyCost = new BigDecimal(0);
            } else {
                totalLaborHourlyCost = estimateCostCalculationForHourly(operationComponents.getRoot(), quantity, includeTPZ,
                        OperationsCostCalculationConstants.LABOR_HOURLY_COST, time);
                totalMachineHourlyCost = estimateCostCalculationForHourly(operationComponents.getRoot(), quantity, includeTPZ,
                        OperationsCostCalculationConstants.MACHINE_HOURLY_COST, time);
            }
        }
        result.put("totalMachineHourlyCost", totalMachineHourlyCost);
        result.put("totalLaborHourlyCost", totalLaborHourlyCost);
        result.put("totalPieceWorkCost", totalPieceWorkCost);
        return result;
    }

    public BigDecimal estimateCostCalculationForHourly(final EntityTreeNode operationComponent, final BigDecimal plannedQuantity,
            Boolean includeTPZ, String name, int time) {

        if (operationComponent.getField("entityType") != null
                && !OPERATION_NODE_ENTITY_TYPE.equals(operationComponent.getField("entityType"))) {
            return estimateCostCalculationForHourly(
                    operationComponent.getBelongsToField("referenceTechnology").getTreeField("operationComponents").getRoot(),
                    plannedQuantity, includeTPZ, name, time);
        } else {
            BigDecimal pathCost = new BigDecimal(0);
            for (EntityTreeNode child : operationComponent.getChildren()) {
                BigDecimal tmpPathCost = estimateCostCalculationForHourly(child, plannedQuantity, includeTPZ, name, time);
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
            final BigDecimal plannedQuantity, Boolean includeTPZ) {

        if (operationComponent.getField("entityType") != null
                && !OPERATION_NODE_ENTITY_TYPE.equals(operationComponent.getField("entityType"))) {
            return estimateCostCalculationForPieceWork(
                    operationComponent.getBelongsToField("referenceTechnology").getTreeField("operationComponents").getRoot(),
                    plannedQuantity, includeTPZ);
        } else {
            BigDecimal operationCost = new BigDecimal(0);
            BigDecimal pathCost = new BigDecimal(0);
            for (EntityTreeNode child : operationComponent.getChildren()) {
                BigDecimal tmpPathCost = estimateCostCalculationForPieceWork(child, plannedQuantity, includeTPZ);
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

                totalQuantityOutputProduct = totalQuantityOutputProduct.add((BigDecimal) outputProduct.getField("quantity"));

            }
            operationCost = operationCost.add(pieceWorkCost.multiply(totalQuantityOutputProduct));

            pathCost = pathCost.add(operationCost);
            return pathCost;
        }
    }

}
