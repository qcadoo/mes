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
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;

public class OperationsCostCalculationServiceImpl implements OperationsCostCalculationService {

    @Autowired
    private OrderRealizationTimeService orderRealizationTimeService;

    @Override
    public Map<String, BigDecimal> calculateOperationsCost(Entity source, OperationsCostCalculationConstants mode,
            boolean includeTPZs, BigDecimal quantity) {
        checkArgument(quantity != null, "quantity is null");
        checkArgument(quantity.compareTo(BigDecimal.valueOf(0)) == 1, "quantity should be greather than 0");
        BigDecimal totalMachineHourlyCost = new BigDecimal(0);
        BigDecimal totalLaborHourlyCost = new BigDecimal(0);
        BigDecimal totalPieceWorkCost = new BigDecimal(0);
        Map<String, BigDecimal> result = new HashMap<String, BigDecimal>();

        EntityTree operationComponents = getOperationComponentsTree(source);
        if (operationComponents == null) {
            throw new IllegalArgumentException("Incompatible source entity type..");
        }

        if (mode == PIECEWORK) {
            totalPieceWorkCost = calculateCostPieceWork(operationComponents);
        }
        if (mode == HOURLY) {
            int time = orderRealizationTimeService.estimateRealizationTimeForOperation(operationComponents.getRoot(), quantity,
                    includeTPZs);
            if (time == 0) {
                totalMachineHourlyCost = new BigDecimal(0);
                totalLaborHourlyCost = new BigDecimal(0);
            } else {
                totalLaborHourlyCost = calculateCostHourly(operationComponents, time, "laborHourlyCost");
                totalMachineHourlyCost = calculateCostHourly(operationComponents, time, "machineHourlyCost");
            }

        }
        result.put("machineHourlyCost", totalMachineHourlyCost);
        result.put("laborHourlyCost", totalLaborHourlyCost);
        result.put("pieceWorkCost", totalPieceWorkCost);
        return result;
    }

    private EntityTree getOperationComponentsTree(final Entity source) {
        checkArgument(source != null, "source is null");
        DataDefinition dd = source.getDataDefinition();
        if (dd == null) {
            return null;
        }

        Entity operationComponentsProvider = source;
        if ("order".equals(dd.getName())) {
            operationComponentsProvider = operationComponentsProvider.getBelongsToField("technology");
            if (operationComponentsProvider != null) {
                return operationComponentsProvider.getTreeField("operationComponents");
            }
        }

        if ("technology".equals(dd.getName())) {
            return operationComponentsProvider.getTreeField("operationComponents");
        }

        return null;
    }

    private BigDecimal checkData(Entity operationComponent, String name) {
        BigDecimal data = (BigDecimal) operationComponent.getField(name);
        if (data == null) {

            data = (BigDecimal) operationComponent.getBelongsToField("operation").getField(name);
            if (data == null) {
                if ("numberOfOperations".equals(name)) {
                    data = new BigDecimal(1);
                } else {
                    data = new BigDecimal(0);
                }
            }
        }
        return data;
    }

    private BigDecimal calculateCostPieceWork(EntityTree operationComponents) {

        BigDecimal totalPieceWorkCost = new BigDecimal(0);
        for (Entity operationComponent : operationComponents) {
            BigDecimal piecework = checkData(operationComponent, "pieceworkCost");

            BigDecimal numberOfOperations = checkData(operationComponent, "numberOfOperations");
            BigDecimal pieceWorkCost = piecework.divide(numberOfOperations);

            EntityList outputProducts = operationComponent.getHasManyField("operationProductOutComponents");
            BigDecimal totalQuantityOutputProduct = new BigDecimal(0);
            for (Entity outputProduct : outputProducts) {
                totalQuantityOutputProduct = totalQuantityOutputProduct.add((BigDecimal) outputProduct.getField("quantity"));
            }
            totalPieceWorkCost = totalPieceWorkCost.add(pieceWorkCost.multiply(totalQuantityOutputProduct));

        }
        return totalPieceWorkCost;
    }

    private BigDecimal calculateCostHourly(EntityTree operationComponents, int time, String name) {

        BigDecimal realizationtime = BigDecimal.valueOf(time);
        BigDecimal totalHourlyCost = new BigDecimal(0);
        for (Entity operationComponent : operationComponents) {
            BigDecimal hourlyCost = checkData(operationComponent, name);
            realizationtime = realizationtime.multiply(hourlyCost);
            totalHourlyCost = totalHourlyCost.add(realizationtime);
        }
        return totalHourlyCost;
    }
}
