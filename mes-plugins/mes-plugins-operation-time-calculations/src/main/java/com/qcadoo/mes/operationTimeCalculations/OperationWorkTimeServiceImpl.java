package com.qcadoo.mes.operationTimeCalculations;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionLines.ProductionLinesService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Service
public class OperationWorkTimeServiceImpl implements OperationWorkTimeService {

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductionLinesService productionLinesService;

    @Override
    public BigDecimal estimateAbstractOperationWorkTime(final Entity operationComponent, final BigDecimal neededNumberOfCycles,
            final boolean includeTpz, final boolean includeAdditionalTime, final Integer workstations) {
        MathContext mc = numberService.getMathContext();
        BigDecimal tj = BigDecimal.valueOf(getIntegerValue(operationComponent.getField("tj")));
        BigDecimal abstractOperationWorkTime = tj.multiply(neededNumberOfCycles, mc);
        BigDecimal workstationsDecimalValue = new BigDecimal(workstations);
        if (includeTpz) {
            BigDecimal tpz = new BigDecimal(getIntegerValue(operationComponent.getField("tpz")));
            abstractOperationWorkTime = abstractOperationWorkTime.add(tpz.multiply(workstationsDecimalValue, mc));
        }
        if (includeAdditionalTime) {
            BigDecimal additionalTime = new BigDecimal(getIntegerValue(operationComponent.getField("additionalTime")));
            abstractOperationWorkTime = abstractOperationWorkTime.add(additionalTime.multiply(workstationsDecimalValue, mc), mc);
        }
        return numberService.setScale(abstractOperationWorkTime);
    }

    @Override
    public OperationWorkTime estimateOperationWorkTime(final Entity operationComponent, final BigDecimal neededNumberOfCycles,
            final boolean includeTpz, final boolean includeAdditionalTime, final Integer workstations) {

        MathContext mc = numberService.getMathContext();
        BigDecimal laborUtilization = operationComponent.getDecimalField("laborUtilization");
        BigDecimal machineUtilization = operationComponent.getDecimalField("machineUtilization");

        BigDecimal abstractOperationWorkTime = estimateAbstractOperationWorkTime(operationComponent, neededNumberOfCycles,
                includeTpz, includeAdditionalTime, workstations);

        OperationWorkTime operationWorkTime = new OperationWorkTime();
        operationWorkTime.setDuration(abstractOperationWorkTime.intValue());
        operationWorkTime.setLaborWorkTime(abstractOperationWorkTime.multiply(laborUtilization, mc).intValue());
        operationWorkTime.setMachineWorkTime(abstractOperationWorkTime.multiply(machineUtilization, mc).intValue());

        return operationWorkTime;
    }

    @Override
    public Map<Entity, OperationWorkTime> estimateOperationsWorkTime(final List<Entity> operationComponents,
            final Map<Entity, BigDecimal> operationRuns, final boolean includeTpz, final boolean includeAdditionalTime,
            final Map<Entity, Integer> workstations) {
        Map<Entity, OperationWorkTime> operationsWorkTimes = new HashMap<Entity, OperationWorkTime>();
        for (Entity operationComponent : operationComponents) {
            OperationWorkTime operationWorkTime = estimateOperationWorkTime(operationComponent,
                    getOperationRuns(operationRuns, operationComponent), includeTpz, includeAdditionalTime,
                    getWorkstationsQuantity(workstations, operationComponent));
            operationsWorkTimes.put(operationComponent, operationWorkTime);
        }
        return operationsWorkTimes;
    }

    @Override
    public Map<Entity, OperationWorkTime> estimateOperationsWorkTime(List<Entity> operationComponents,
            Map<Entity, BigDecimal> operationRuns, boolean includeTpz, boolean includeAdditionalTime, Entity productionLine) {
        Map<Entity, Integer> workstations = getWorkstationsMapsForOperationsComponent(operationComponents, productionLine);
        return estimateOperationsWorkTime(operationComponents, operationRuns, includeTpz, includeAdditionalTime, workstations);
    }

    @Override
    public Map<Entity, OperationWorkTime> estimateOperationsWorkTimeForOrder(Entity order, Map<Entity, BigDecimal> operationRuns,
            boolean includeTpz, boolean includeAdditionalTime, Entity productionLine) {
        List<Entity> operationComponents = order.getHasManyField("technologyInstanceOperationComponents");
        Map<Entity, Integer> workstations = getWorkstationsFromOrder(order, productionLine);
        return estimateOperationsWorkTime(operationComponents, operationRuns, includeTpz, includeAdditionalTime, workstations);
    }

    @Override
    public Map<Entity, OperationWorkTime> estimateOperationsWorkTimeForTechnology(Entity technology,
            Map<Entity, BigDecimal> operationRuns, boolean includeTpz, boolean includeAdditionalTime, Entity productionLine) {
        List<Entity> operationComponents = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);
        Map<Entity, Integer> workstations = getWorkstationsFromTechnology(technology, productionLine);
        return estimateOperationsWorkTime(operationComponents, operationRuns, includeTpz, includeAdditionalTime, workstations);
    }

    @Override
    public OperationWorkTime estimateTotalWorkTime(final List<Entity> operationComponents,
            final Map<Entity, BigDecimal> operationRuns, final boolean includeTpz, final boolean includeAdditionalTime,
            final Map<Entity, Integer> workstations) {
        OperationWorkTime totalWorkTime = new OperationWorkTime();
        Integer totalLaborWorkTime = new Integer(0);
        Integer totalMachineWorkTime = new Integer(0);
        Integer duration = new Integer(0);
        for (Entity operationComponent : operationComponents) {
            Entity operComp = operationComponent;
            OperationWorkTime abstractOperationWorkTime = estimateOperationWorkTime(operComp,
                    getOperationRuns(operationRuns, operationComponent), includeTpz, includeAdditionalTime,
                    workstations.get(operationComponent));
            totalLaborWorkTime += abstractOperationWorkTime.getLaborWorkTime();
            totalMachineWorkTime += abstractOperationWorkTime.getMachineWorkTime();
            duration += abstractOperationWorkTime.getDuration();
        }
        totalWorkTime.setLaborWorkTime(totalLaborWorkTime);
        totalWorkTime.setMachineWorkTime(totalMachineWorkTime);
        totalWorkTime.setDuration(duration);

        return totalWorkTime;
    }

    @Override
    public OperationWorkTime estimateTotalWorkTime(List<Entity> operationComponents, Map<Entity, BigDecimal> operationRuns,
            boolean includeTpz, boolean includeAdditionalTime, Entity productionLine) {
        Map<Entity, Integer> workstations = getWorkstationsMapsForOperationsComponent(operationComponents, productionLine);
        return estimateTotalWorkTime(operationComponents, operationRuns, includeTpz, includeAdditionalTime, workstations);
    }

    @Override
    public OperationWorkTime estimateTotalWorkTimeForOrder(Entity order, Map<Entity, BigDecimal> operationRuns,
            boolean includeTpz, boolean includeAdditionalTime, Entity productionLine) {
        List<Entity> operationComponents = order.getHasManyField("technologyInstanceOperationComponents");
        Map<Entity, Integer> workstations = getWorkstationsFromOrder(order, productionLine);
        return estimateTotalWorkTime(operationComponents, operationRuns, includeTpz, includeAdditionalTime, workstations);
    }

    @Override
    public OperationWorkTime estimateTotalWorkTimeForTechnology(Entity technology, Map<Entity, BigDecimal> operationRuns,
            boolean includeTpz, boolean includeAdditionalTime, Entity productionLine) {
        List<Entity> operationComponents = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);
        Map<Entity, Integer> workstations = getWorkstationsFromTechnology(technology, productionLine);
        return estimateTotalWorkTime(operationComponents, operationRuns, includeTpz, includeAdditionalTime, workstations);
    }

    @Override
    public BigDecimal estimateNumberOfCyclesForWorkstations(final boolean isTjDivisible, final BigDecimal operationRuns,
            final Integer workstations) {
        MathContext mc = numberService.getMathContext();
        BigDecimal numberOfCycles = BigDecimal.ONE;
        BigDecimal workstationsDecimalValue = new BigDecimal(workstations);
        if (isTjDivisible) {
            numberOfCycles = operationRuns.divide(workstationsDecimalValue, mc);
        } else {
            numberOfCycles = numberOfCycles.setScale(0, RoundingMode.CEILING);
        }
        return numberOfCycles;
    }

    private Map<Entity, Integer> getWorkstationsMapsForOperationsComponent(final List<Entity> operationsComponents,
            final Entity productionLine) {
        Map<Entity, Integer> workstations = new HashMap<Entity, Integer>();
        for (Entity operComp : operationsComponents) {
            String entityType = operComp.getDataDefinition().getName();
            if (!"technologyOperationComponent".equals(entityType)) {
                operComp = operComp.getBelongsToField("technologyOperationComponent").getDataDefinition()
                        .get(operComp.getBelongsToField("technologyOperationComponent").getId());
            }
            workstations.put(operComp, productionLinesService.getWorkstationTypesCount(operComp, productionLine));
        }
        return workstations;
    }

    private Integer getIntegerValue(final Object value) {
        return value == null ? Integer.valueOf(0) : (Integer) value;
    }

    private BigDecimal getOperationRuns(final Map<Entity, BigDecimal> operationRuns, final Entity operationComponent) {
        Entity operComp = operationComponent;
        String entityType = operationComponent.getDataDefinition().getName();
        if (!TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT.equals(entityType)) {
            operComp = operComp.getBelongsToField("technologyOperationComponent").getDataDefinition()
                    .get(operComp.getBelongsToField("technologyOperationComponent").getId());
        }
        return operationRuns.get(operComp);
    }

    private Integer getWorkstationsQuantity(final Map<Entity, Integer> workstations, final Entity operationComponent) {
        Entity operComp = operationComponent;
        String entityType = operationComponent.getDataDefinition().getName();
        if (!TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT.equals(entityType)) {
            operComp = operComp.getBelongsToField("technologyOperationComponent").getDataDefinition()
                    .get(operComp.getBelongsToField("technologyOperationComponent").getId());
        }
        return workstations.get(operComp);
    }

    private Map<Entity, Integer> getWorkstationsFromTechnology(final Entity technology, final Entity productionLine) {
        Map<Entity, Integer> workstations = new HashMap<Entity, Integer>();
        for (Entity operComp : technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS)) {
            workstations.put(operComp, productionLinesService.getWorkstationTypesCount(operComp, productionLine));
        }
        return workstations;
    }

    private Map<Entity, Integer> getWorkstationsFromOrder(final Entity order, final Entity productionLine) {
        Map<Entity, Integer> workstations = new HashMap<Entity, Integer>();
        for (Entity operComp : order.getHasManyField("technologyInstanceOperationComponents")) {
            workstations.put(operComp.getBelongsToField("technologyOperationComponent"),
                    (Integer) operComp.getField("quantityOfWorkstationTypes"));
        }
        return workstations;
    }

}
