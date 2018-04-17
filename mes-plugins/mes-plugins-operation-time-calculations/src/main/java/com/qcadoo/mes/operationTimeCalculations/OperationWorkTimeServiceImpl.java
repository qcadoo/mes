/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.operationTimeCalculations;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.operationTimeCalculations.dto.OperationTimesContainer;
import com.qcadoo.mes.technologies.ProductionLinesService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.timeNormsForOperations.constants.OperCompTimeCalculationsFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TimeNormsConstants;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class OperationWorkTimeServiceImpl implements OperationWorkTimeService {

    private static final String L_TECHNOLOGY = "technology";

    private static final String L_TECHNOLOGY_OPERATION_COMPONENT = "technologyOperationComponent";

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductionLinesService productionLinesService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    @Override
    public BigDecimal estimateAbstractOperationWorkTime(final Entity operationComponent, final BigDecimal neededNumberOfCycles,
            final boolean includeTpz, final boolean includeAdditionalTime, final Integer workstations) {
        MathContext mc = numberService.getMathContext();
        BigDecimal tj = BigDecimal.valueOf(getValueOfIntFiled(operationComponent, "tj"));
        BigDecimal abstractOperationWorkTime = tj.multiply(neededNumberOfCycles, mc);
        if (includeTpz) {
            BigDecimal tpz = new BigDecimal(getValueOfIntFiled(operationComponent, "tpz"));
            abstractOperationWorkTime = abstractOperationWorkTime.add(tpz, mc);
        }
        if (includeAdditionalTime) {
            BigDecimal additionalTime = new BigDecimal(getValueOfIntFiled(operationComponent, "timeNextOperation"));
            abstractOperationWorkTime = abstractOperationWorkTime.add(additionalTime, mc);
        }
        return numberService.setScale(abstractOperationWorkTime);
    }

    private Integer getValueOfIntFiled(final Entity operationComponent, final String field) {
        String entityType = operationComponent.getDataDefinition().getName();
        if (TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT.equals(entityType)) {
            return getIntegerValue(operationComponent.getField(field));
        } else {
            return getIntegerValue(operationComponent.getBelongsToField("technologyOperationComponent").getField(field));
        }
    }

    private BigDecimal getValueOfDecimalFiled(final Entity operationComponent, final String field) {
        String entityType = operationComponent.getDataDefinition().getName();
        if (TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT.equals(entityType)) {
            return operationComponent.getDecimalField(field);
        } else {
            return operationComponent.getBelongsToField("technologyOperationComponent").getDecimalField(field);
        }
    }

    @Override
    public OperationWorkTime estimateTechOperationWorkTime(final Entity operationComponent,
            final BigDecimal neededNumberOfCycles, final boolean includeTpz, final boolean includeAdditionalTime,
            final Integer workstations, final boolean saved) {

        return estimateOperationWorkTime(null, operationComponent, neededNumberOfCycles, includeTpz, includeAdditionalTime,
                workstations, saved);
    }

    @Override
    public OperationWorkTime estimateOrderOperationWorkTime(final Entity order, final Entity operationComponent,
            final BigDecimal neededNumberOfCycles, final boolean includeTpz, final boolean includeAdditionalTime,
            final Integer workstations, final boolean saved) {

        return estimateOperationWorkTime(order, operationComponent, neededNumberOfCycles, includeTpz, includeAdditionalTime,
                workstations, saved);

    }

    private OperationWorkTime estimateOperationWorkTime(final Entity order, final Entity operationComponent,
            final BigDecimal neededNumberOfCycles, final boolean includeTpz, final boolean includeAdditionalTime,
            final Integer workstations, final boolean saved) {
        MathContext mc = numberService.getMathContext();
        BigDecimal laborUtilization = BigDecimalUtils.convertNullToZero(getValueOfDecimalFiled(operationComponent,
                "laborUtilization"));
        BigDecimal machineUtilization = BigDecimalUtils.convertNullToZero(getValueOfDecimalFiled(operationComponent,
                "machineUtilization"));

        BigDecimal abstractOperationWorkTime = estimateAbstractOperationWorkTime(operationComponent, neededNumberOfCycles,
                includeTpz, includeAdditionalTime, workstations);

        Integer laborWorkTime = abstractOperationWorkTime.multiply(laborUtilization, mc).intValue();
        Integer machineWorkTime = abstractOperationWorkTime.multiply(machineUtilization, mc).intValue();
        Integer duration = abstractOperationWorkTime.intValue();
        OperationWorkTime operationWorkTime = new OperationWorkTime();
        operationWorkTime.setDuration(duration);
        operationWorkTime.setLaborWorkTime(laborWorkTime);
        operationWorkTime.setMachineWorkTime(machineWorkTime);

        if (saved) {
            savedWorkTime(order, operationComponent, machineWorkTime, laborWorkTime, duration);
        }
        return operationWorkTime;

    }

    @Override
    public Map<Entity, OperationWorkTime> estimateOperationsWorkTime(final List<Entity> operationComponents,
            final Map<Entity, BigDecimal> operationRuns, final boolean includeTpz, final boolean includeAdditionalTime,
            final Map<Entity, Integer> workstations, final boolean saved) {
        Map<Entity, OperationWorkTime> operationsWorkTimes = new HashMap<Entity, OperationWorkTime>();
        for (Entity operationComponent : operationComponents) {
            OperationWorkTime operationWorkTime = estimateTechOperationWorkTime(operationComponent,
                    getOperationRuns(operationRuns, operationComponent), includeTpz, includeAdditionalTime,
                    getWorkstationsQuantity(workstations, operationComponent), saved);
            operationsWorkTimes.put(operationComponent.getDataDefinition().get(operationComponent.getId()), operationWorkTime);
        }
        return operationsWorkTimes;
    }

    @Override
    public OperationTimesContainer estimateOperationsWorkTimes(List<Entity> operationComponents,
            Map<Long, BigDecimal> operationRuns, boolean includeTpz, boolean includeAdditionalTime,
            Map<Long, Integer> workstations, boolean saved) {
        OperationTimesContainer operationTimesContainer = new OperationTimesContainer();
        for (Entity operationComponent : operationComponents) {
            OperationWorkTime operationWorkTime = estimateTechOperationWorkTime(operationComponent,
                    getOperationRunsFromMap(operationRuns, operationComponent), includeTpz, includeAdditionalTime,
                    getWorkstationsQuantityFromMap(workstations, operationComponent), saved);
            operationTimesContainer
                    .add(operationComponent.getDataDefinition().get(operationComponent.getId()), operationWorkTime);
        }
        return operationTimesContainer;
    }

    @Override
    public Map<Entity, OperationWorkTime> estimateOperationsWorkTime(final List<Entity> operationComponents,
            final Map<Entity, BigDecimal> operationRuns, final boolean includeTpz, final boolean includeAdditionalTime,
            final Entity productionLine, final boolean saved) {
        Map<Entity, Integer> workstations = getWorkstationsForOperationsComponent(operationComponents, productionLine);
        return estimateOperationsWorkTime(operationComponents, operationRuns, includeTpz, includeAdditionalTime, workstations,
                saved);
    }

    @Override
    public Map<Entity, OperationWorkTime> estimateOperationsWorkTimeForTechnology(final Entity technology,
            final Map<Entity, BigDecimal> operationRuns, final boolean includeTpz, final boolean includeAdditionalTime,
            final Entity productionLine, final boolean saved) {
        List<Entity> operationComponents = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);
        Map<Entity, Integer> workstations = getWorkstationsFromTechnology(technology, productionLine);
        return estimateOperationsWorkTime(operationComponents, operationRuns, includeTpz, includeAdditionalTime, workstations,
                saved);
    }

    @Override
    public OperationWorkTime estimateTotalWorkTime(Entity order, List<Entity> operationComponents,
            Map<Long, BigDecimal> operationRuns, boolean includeTpz, boolean includeAdditionalTime,
            Map<Long, Integer> workstations, boolean saved) {
        OperationWorkTime totalWorkTime = new OperationWorkTime();
        Integer totalLaborWorkTime = Integer.valueOf(0);
        Integer totalMachineWorkTime = Integer.valueOf(0);
        Integer duration = Integer.valueOf(0);

        for (Entity operationComponent : operationComponents) {
            Entity operComp = operationComponent;
            OperationWorkTime abstractOperationWorkTime = estimateOrderOperationWorkTime(order, operComp,
                    getOperationRunsFromMap(operationRuns, operationComponent), includeTpz, includeAdditionalTime,
                    getWorkstationsQuantityFromMap(workstations, operationComponent), saved);
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
    public OperationWorkTime estimateTotalWorkTime(final List<Entity> operationComponents,
            final Map<Long, BigDecimal> operationRuns, final boolean includeTpz, final boolean includeAdditionalTime,
            final Map<Long, Integer> workstations, final boolean saved) {
        OperationWorkTime totalWorkTime = new OperationWorkTime();
        Integer totalLaborWorkTime = Integer.valueOf(0);
        Integer totalMachineWorkTime = Integer.valueOf(0);
        Integer duration = Integer.valueOf(0);

        for (Entity operationComponent : operationComponents) {
            Entity operComp = operationComponent;
            OperationWorkTime abstractOperationWorkTime = estimateTechOperationWorkTime(operComp,
                    getOperationRunsFromMap(operationRuns, operationComponent), includeTpz, includeAdditionalTime,
                    getWorkstationsQuantityFromMap(workstations, operationComponent), saved);
            totalLaborWorkTime += abstractOperationWorkTime.getLaborWorkTime();
            totalMachineWorkTime += abstractOperationWorkTime.getMachineWorkTime();
            duration += abstractOperationWorkTime.getDuration();
        }

        totalWorkTime.setLaborWorkTime(totalLaborWorkTime);
        totalWorkTime.setMachineWorkTime(totalMachineWorkTime);
        totalWorkTime.setDuration(duration);

        return totalWorkTime;
    }

    private void savedWorkTime(Entity order, final Entity technologyOperationComponent, final Integer machineWorkTime,
            final Integer laborWorkTime, final Integer duration) {

        Entity operCompTimeCalculation = createOrGetOperCompTimeCalculation(order, technologyOperationComponent);

        if (operCompTimeCalculation != null) {
            operCompTimeCalculation.setField(OperCompTimeCalculationsFields.MACHINE_WORK_TIME, machineWorkTime);
            operCompTimeCalculation.setField(OperCompTimeCalculationsFields.LABOR_WORK_TIME, laborWorkTime);
            operCompTimeCalculation.setField(OperCompTimeCalculationsFields.DURATION, duration);

            operCompTimeCalculation.getDataDefinition().save(operCompTimeCalculation);
        }

    }

    @Override
    public Entity createOrGetOperCompTimeCalculation(Entity order, Entity technologyOperationComponent) {
        if (Objects.nonNull(order)) {
            Entity orderTimeCalculation = dataDefinitionService.get(TimeNormsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER, TimeNormsConstants.MODEL_ORDER_TIME_CALCULATION).find()
                    .add(SearchRestrictions.belongsTo("order", order)).setMaxResults(1).uniqueResult();
            if (Objects.isNull(orderTimeCalculation)) {
                orderTimeCalculation = dataDefinitionService.get(TimeNormsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER, TimeNormsConstants.MODEL_ORDER_TIME_CALCULATION).create();
                orderTimeCalculation.setField("order", order);
                orderTimeCalculation = orderTimeCalculation.getDataDefinition().save(orderTimeCalculation);
            }
            Entity operCompTimeCalculation = dataDefinitionService.get(TimeNormsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER, TimeNormsConstants.MODEL_OPER_COMP_TIME_CALCULATION).find()
                    .add(SearchRestrictions.belongsTo(TimeNormsConstants.MODEL_ORDER_TIME_CALCULATION, orderTimeCalculation))
                    .add(SearchRestrictions.belongsTo(OperCompTimeCalculationsFields.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent))
                    .setMaxResults(1).uniqueResult();
            if (Objects.isNull(operCompTimeCalculation)) {
                operCompTimeCalculation = dataDefinitionService.get(TimeNormsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER, TimeNormsConstants.MODEL_OPER_COMP_TIME_CALCULATION).create();
                operCompTimeCalculation.setField(TimeNormsConstants.MODEL_ORDER_TIME_CALCULATION, orderTimeCalculation);
                operCompTimeCalculation.setField(OperCompTimeCalculationsFields.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent);
                operCompTimeCalculation = operCompTimeCalculation.getDataDefinition().save(operCompTimeCalculation);
            }
            return operCompTimeCalculation;
        } else {
            Entity operCompTimeCalculation = dataDefinitionService.get(TimeNormsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,TimeNormsConstants.MODEL_OPER_COMP_TIME_CALCULATION).find()
                    .add(SearchRestrictions.isNull(TimeNormsConstants.MODEL_ORDER_TIME_CALCULATION))
                    .add(SearchRestrictions.belongsTo(OperCompTimeCalculationsFields.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent))
                    .setMaxResults(1).uniqueResult();
            if (Objects.isNull(operCompTimeCalculation)) {
                operCompTimeCalculation = dataDefinitionService.get(TimeNormsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,TimeNormsConstants.MODEL_OPER_COMP_TIME_CALCULATION).create();
                operCompTimeCalculation.setField(OperCompTimeCalculationsFields.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent);
                operCompTimeCalculation = operCompTimeCalculation.getDataDefinition().save(operCompTimeCalculation);
            }
            return operCompTimeCalculation;
        }
    }

    @Override
    public OperationWorkTime estimateTotalWorkTime(final List<Entity> operationComponents,
            final Map<Long, BigDecimal> operationRuns, final boolean includeTpz, final boolean includeAdditionalTime,
            final Entity productionLine, final boolean saved) {
        Map<Long, Integer> workstations = getWorkstationsMapsForOperationsComponent(operationComponents, productionLine);
        return estimateTotalWorkTime(operationComponents, operationRuns, includeTpz, includeAdditionalTime, workstations, saved);
    }

    @Override
    public OperationWorkTime estimateTotalWorkTimeForOrder(final Entity order, final Map<Long, BigDecimal> operationRuns,
            final boolean includeTpz, final boolean includeAdditionalTime, final Entity productionLine, final boolean saved) {
        List<Entity> operationComponents = order.getBelongsToField(L_TECHNOLOGY).getHasManyField(
                TechnologyFields.OPERATION_COMPONENTS);
        Map<Long, Integer> workstations = getWorkstationsMapFromOrder(order);

        return estimateTotalWorkTime(order, operationComponents, operationRuns, includeTpz, includeAdditionalTime, workstations,
                saved);
    }

    @Override
    public OperationWorkTime estimateTotalWorkTimeForTechnology(final Entity technology,
            final Map<Long, BigDecimal> operationRuns, final boolean includeTpz, final boolean includeAdditionalTime,
            final Entity productionLine, final boolean saved) {
        List<Entity> operationComponents = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);
        Map<Long, Integer> workstations = getWorkstationsMapFromTechnology(technology, productionLine);
        return estimateTotalWorkTime(operationComponents, operationRuns, includeTpz, includeAdditionalTime, workstations, saved);
    }

    private Map<Long, Integer> getWorkstationsMapsForOperationsComponent(final List<Entity> operationsComponents,
            final Entity productionLine) {
        Map<Long, Integer> workstations = new HashMap<Long, Integer>();
        for (Entity operComp : operationsComponents) {
            String entityType = operComp.getDataDefinition().getName();
            if (!L_TECHNOLOGY_OPERATION_COMPONENT.equals(entityType)) {
                operComp = operComp.getBelongsToField(L_TECHNOLOGY_OPERATION_COMPONENT).getDataDefinition()
                        .get(operComp.getBelongsToField(L_TECHNOLOGY_OPERATION_COMPONENT).getId());
            }
            if (parameterService.getParameter().getBooleanField("workstationsQuantityFromProductionLine")) {
                workstations.put(operComp.getId(), productionLinesService.getWorkstationTypesCount(operComp, productionLine));
            } else {
                workstations.put(operComp.getId(),
                        operComp.getIntegerField(TechnologyOperationComponentFields.QUANTITY_OF_WORKSTATIONS));

            }
        }
        return workstations;
    }

    private Map<Entity, Integer> getWorkstationsForOperationsComponent(final List<Entity> operationsComponents,
            final Entity productionLine) {
        Map<Entity, Integer> workstations = new HashMap<Entity, Integer>();
        for (Entity operComp : operationsComponents) {
            String entityType = operComp.getDataDefinition().getName();
            if (!L_TECHNOLOGY_OPERATION_COMPONENT.equals(entityType)) {
                operComp = operComp.getBelongsToField(L_TECHNOLOGY_OPERATION_COMPONENT).getDataDefinition()
                        .get(operComp.getBelongsToField(L_TECHNOLOGY_OPERATION_COMPONENT).getId());
            }
            if (parameterService.getParameter().getBooleanField("workstationsQuantityFromProductionLine")) {
                workstations.put(operComp, productionLinesService.getWorkstationTypesCount(operComp, productionLine));
            } else {
                workstations.put(operComp, operComp.getIntegerField(TechnologyOperationComponentFields.QUANTITY_OF_WORKSTATIONS));

            }
        }
        return workstations;
    }

    private BigDecimal getOperationRuns(final Map<Entity, BigDecimal> operationRuns, final Entity operationComponent) {
        Entity operComp = operationComponent;
        String entityType = operationComponent.getDataDefinition().getName();
        if (!TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT.equals(entityType)) {
            operComp = operComp.getBelongsToField(L_TECHNOLOGY_OPERATION_COMPONENT).getDataDefinition()
                    .get(operComp.getBelongsToField(L_TECHNOLOGY_OPERATION_COMPONENT).getId());
        }
        return BigDecimalUtils.convertNullToZero(operationRuns.get(operComp));
    }

    private BigDecimal getOperationRunsFromMap(final Map<Long, BigDecimal> operationRuns, final Entity operationComponent) {
        Entity operComp = operationComponent;
        String entityType = operationComponent.getDataDefinition().getName();
        if (!TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT.equals(entityType)) {
            operComp = operComp.getBelongsToField(L_TECHNOLOGY_OPERATION_COMPONENT).getDataDefinition()
                    .get(operComp.getBelongsToField(L_TECHNOLOGY_OPERATION_COMPONENT).getId());
        }
        return BigDecimalUtils.convertNullToZero(operationRuns.get(operComp.getId()));
    }

    private Integer getWorkstationsQuantity(final Map<Entity, Integer> workstations, final Entity operationComponent) {
        Entity operComp = operationComponent;
        String entityType = operationComponent.getDataDefinition().getName();
        if (!TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT.equals(entityType)) {
            operComp = operComp.getBelongsToField(L_TECHNOLOGY_OPERATION_COMPONENT).getDataDefinition()
                    .get(operComp.getBelongsToField(L_TECHNOLOGY_OPERATION_COMPONENT).getId());
        }
        return workstations.get(operComp);
    }

    private Integer getWorkstationsQuantityFromMap(final Map<Long, Integer> workstations, final Entity operationComponent) {
        Entity operComp = operationComponent;
        String entityType = operationComponent.getDataDefinition().getName();
        if (!TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT.equals(entityType)) {
            operComp = operComp.getBelongsToField(L_TECHNOLOGY_OPERATION_COMPONENT).getDataDefinition()
                    .get(operComp.getBelongsToField(L_TECHNOLOGY_OPERATION_COMPONENT).getId());
        }
        return workstations.get(operComp.getId());
    }

    private Map<Long, Integer> getWorkstationsMapFromTechnology(final Entity technology, final Entity productionLine) {
        Map<Long, Integer> workstations = new HashMap<Long, Integer>();
        for (Entity operComp : technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS)) {
            workstations.put(operComp.getId(), productionLinesService.getWorkstationTypesCount(operComp, productionLine));
        }
        return workstations;
    }

    private Map<Entity, Integer> getWorkstationsFromTechnology(final Entity technology, final Entity productionLine) {
        Map<Entity, Integer> workstations = new HashMap<Entity, Integer>();
        for (Entity operComp : technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS)) {
            if (parameterService.getParameter().getBooleanField("workstationsQuantityFromProductionLine")) {
                workstations.put(operComp, productionLinesService.getWorkstationTypesCount(operComp, productionLine));
            } else {
                workstations.put(operComp, operComp.getIntegerField(TechnologyOperationComponentFields.QUANTITY_OF_WORKSTATIONS));

            }
        }
        return workstations;
    }

    private Map<Long, Integer> getWorkstationsMapFromOrder(final Entity order) {
        Map<Long, Integer> workstations = new HashMap<Long, Integer>();
        for (Entity operComp : order.getBelongsToField(L_TECHNOLOGY).getHasManyField("operationComponents")) {
            workstations.put(operComp.getId(),
                    operComp.getIntegerField(TechnologyOperationComponentFields.QUANTITY_OF_WORKSTATIONS));
        }
        return workstations;
    }

    private Integer getIntegerValue(final Object value) {
        return value == null ? Integer.valueOf(0) : (Integer) value;
    }

}
