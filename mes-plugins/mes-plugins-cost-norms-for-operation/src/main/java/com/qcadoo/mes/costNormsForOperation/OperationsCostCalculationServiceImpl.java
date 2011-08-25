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
        if (operationComponents == null && source.getDataDefinition() != null) {

            throw new IllegalArgumentException("Incompatible source entity type..");
        }

        if (mode == PIECEWORK) {
            for (Entity operationComponent : operationComponents) {
                BigDecimal numberOfOperations = (BigDecimal) operationComponent.getField("numberOfOperations");
                BigDecimal piecework = (BigDecimal) operationComponent.getField("pieceworkCost");
                BigDecimal pieceWorkCost = piecework.divide(numberOfOperations);

                EntityList outputProducts = operationComponent.getHasManyField("operationProductOutComponents");
                BigDecimal totalQuantityOutputProduct = new BigDecimal(0);
                for (Entity outputProduct : outputProducts) {
                    totalQuantityOutputProduct = totalQuantityOutputProduct.add((BigDecimal) outputProduct.getField("quantity"));
                }
                pieceWorkCost = pieceWorkCost.multiply(totalQuantityOutputProduct);
                totalPieceWorkCost = totalPieceWorkCost.add(pieceWorkCost);

            }
        }
        if (mode == HOURLY) {
            int time = orderRealizationTimeService.estimateRealizationTimeForOperation(operationComponents.getRoot(), quantity,
                    includeTPZs);
            if (time == 0) {
                result.put("machineHourlyCost", new BigDecimal(0));
                result.put("laborHourlyCost", new BigDecimal(0));
                result.put("pieceWorkCost", new BigDecimal(0));
            }

            BigDecimal realizationtime = BigDecimal.valueOf(time);

            for (Entity operationComponent : operationComponents) {
                BigDecimal laborHourlyCost = (BigDecimal) operationComponent.getField("laborHourlyCost");
                BigDecimal machineHourlyCost = (BigDecimal) operationComponent.getField("machineHourlyCost");
                totalMachineHourlyCost = realizationtime.multiply(machineHourlyCost);
                totalLaborHourlyCost = realizationtime.multiply(laborHourlyCost);
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

}
