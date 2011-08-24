package com.qcadoo.mes.costNormsForOperation;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants;
import com.qcadoo.mes.productionScheduling.OrderRealizationTimeService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

public class OperationsCostCalculationServiceImpl implements OperationsCostCalculationService {

    @Autowired
    private OrderRealizationTimeService orderRealizationTimeService;

    @Override
    public Map<String, BigDecimal> calculateOperationsCost(Entity source, OperationsCostCalculationConstants mode,
            boolean includeTPZs, BigDecimal quantity) {
        checkArgument(source != null, "source is null");
        checkArgument(quantity != null, "quantity is null");
        checkArgument(quantity.compareTo(BigDecimal.valueOf(0)) == 1, "quantity should be greather than 0");

        BigDecimal totalMachineHourlyCost = new BigDecimal(0);
        BigDecimal totalLaborHourlyCost = new BigDecimal(0);
        Map<String, BigDecimal> result = new HashMap<String, BigDecimal>();

        EntityTree operationComponents = getOperationComponentsTree(source);
        if (operationComponents == null) {
            throw new IllegalArgumentException("Incompatible source entity type..");
        }
        int time = orderRealizationTimeService.estimateRealizationTimeForOperation(operationComponents.getRoot(), quantity,
                includeTPZs);
        BigDecimal realizationtime = BigDecimal.valueOf(time);

        for (Entity operationComponent : operationComponents) {
            BigDecimal laborHourlyCost = (BigDecimal) operationComponent.getField("laborHourlyCost");
            BigDecimal machineHourlyCost = (BigDecimal) operationComponent.getField("machineHourlyCost");
            BigDecimal numberOfOperations = (BigDecimal) operationComponent.getField("numberOfOperations");
            totalMachineHourlyCost = realizationtime.multiply(machineHourlyCost);
            totalLaborHourlyCost = realizationtime.multiply(laborHourlyCost);
        }

        result.put("machineHourlyCost", totalMachineHourlyCost);
        result.put("laborHourlyCost", totalLaborHourlyCost);
        return result;
    }

    private EntityTree getOperationComponentsTree(final Entity source) {
        DataDefinition dd = source.getDataDefinition();
        if (dd == null) {
            return null;
        }

        Entity operationComponentsProvider = source;
        if ("order".equals(dd.getName())) {
            operationComponentsProvider = operationComponentsProvider.getBelongsToField("technology");
        } else if (!("technology".equals(dd.getName())) || operationComponentsProvider == null) {
            return null;
        }

        return operationComponentsProvider.getTreeField("operationComponents");
    }

}
