/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.operationTimeCalculations;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.operationTimeCalculations.constants.OperCompTimeCalculationsFields;
import com.qcadoo.mes.operationTimeCalculations.constants.OperationTimeCalculationsConstants;
import com.qcadoo.mes.operationTimeCalculations.constants.OrderTimeCalculationFields;
import com.qcadoo.mes.operationTimeCalculations.constants.PlanOrderTimeCalculationFields;
import com.qcadoo.mes.operationTimeCalculations.dto.OperationTimesContainer;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.dto.ProductQuantitiesHolder;
import com.qcadoo.mes.timeNormsForOperations.constants.TechOperCompWorkstationTimeFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.TECHNOLOGY;

@Service
public class OperationWorkTimeServiceImpl implements OperationWorkTimeService {

    @Autowired
    private NumberService numberService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Override
    public BigDecimal estimateAbstractOperationWorkTime(final Entity operationComponent, final BigDecimal neededNumberOfCycles,
                                                        final boolean includeTpz, final boolean includeAdditionalTime, final BigDecimal staffFactor) {
        MathContext mc = numberService.getMathContext();
        BigDecimal tj = BigDecimalUtils
                .convertNullToZero(operationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.TJ));
        BigDecimal abstractOperationWorkTime = tj.multiply(staffFactor, mc).multiply(neededNumberOfCycles, mc);
        if (includeTpz) {
            BigDecimal tpz = BigDecimalUtils
                    .convertNullToZero(operationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.TPZ));
            abstractOperationWorkTime = abstractOperationWorkTime.add(tpz, mc);
        }
        if (includeAdditionalTime) {
            BigDecimal additionalTime = BigDecimalUtils.convertNullToZero(
                    operationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.TIME_NEXT_OPERATION));
            abstractOperationWorkTime = abstractOperationWorkTime.add(additionalTime, mc);
        }
        return numberService.setScaleWithDefaultMathContext(abstractOperationWorkTime);
    }

    @Override
    public OperationWorkTime estimateTechOperationWorkTimeForWorkstation(final Entity operationComponent,
                                                                         final BigDecimal neededNumberOfCycles, final boolean includeTpz, final boolean includeAdditionalTime,
                                                                         Entity techOperCompWorkstationTime, BigDecimal staffFactor) {
        MathContext mc = numberService.getMathContext();
        BigDecimal laborUtilization = BigDecimalUtils
                .convertNullToZero(operationComponent.getDecimalField(TechnologyOperationComponentFieldsTNFO.LABOR_UTILIZATION));
        BigDecimal machineUtilization = BigDecimalUtils.convertNullToZero(
                operationComponent.getDecimalField(TechnologyOperationComponentFieldsTNFO.MACHINE_UTILIZATION));

        BigDecimal abstractOperationWorkTime = BigDecimal
                .valueOf(techOperCompWorkstationTime.getIntegerField(TechOperCompWorkstationTimeFields.TJ))
                .multiply(staffFactor, mc).multiply(neededNumberOfCycles, mc);
        if (includeTpz) {
            abstractOperationWorkTime = abstractOperationWorkTime
                    .add(new BigDecimal(techOperCompWorkstationTime.getIntegerField(TechOperCompWorkstationTimeFields.TPZ)), mc);
        }
        if (includeAdditionalTime) {
            abstractOperationWorkTime = abstractOperationWorkTime.add(
                    new BigDecimal(
                            techOperCompWorkstationTime.getIntegerField(TechOperCompWorkstationTimeFields.TIME_NEXT_OPERATION)),
                    mc);
        }
        abstractOperationWorkTime = numberService.setScaleWithDefaultMathContext(abstractOperationWorkTime);

        Integer laborWorkTime = abstractOperationWorkTime.multiply(laborUtilization, mc).intValue();
        Integer machineWorkTime = abstractOperationWorkTime.multiply(machineUtilization, mc).intValue();
        Integer duration = abstractOperationWorkTime.intValue();
        OperationWorkTime operationWorkTime = new OperationWorkTime();
        operationWorkTime.setDuration(duration);
        operationWorkTime.setLaborWorkTime(laborWorkTime);
        operationWorkTime.setMachineWorkTime(machineWorkTime);

        return operationWorkTime;
    }

    @Override
    public OperationWorkTime estimateOperationWorkTime(final Entity order, final Entity operationComponent, final BigDecimal neededNumberOfCycles,
                                                       final boolean includeTpz, final boolean includeAdditionalTime, final boolean saved, final BigDecimal staffFactor) {
        BigDecimal laborUtilization = BigDecimalUtils
                .convertNullToZero(operationComponent.getDecimalField(TechnologyOperationComponentFieldsTNFO.LABOR_UTILIZATION));
        BigDecimal machineUtilization = BigDecimalUtils.convertNullToZero(
                operationComponent.getDecimalField(TechnologyOperationComponentFieldsTNFO.MACHINE_UTILIZATION));

        BigDecimal abstractOperationWorkTime = estimateAbstractOperationWorkTime(operationComponent, neededNumberOfCycles,
                includeTpz, includeAdditionalTime, staffFactor);

        Integer laborWorkTime = abstractOperationWorkTime.multiply(laborUtilization, numberService.getMathContext()).intValue();
        Integer machineWorkTime = abstractOperationWorkTime.multiply(machineUtilization, numberService.getMathContext()).intValue();
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
    public OperationTimesContainer estimateOperationsWorkTimes(List<Entity> operationComponents,
                                                               Map<Long, BigDecimal> operationRuns, boolean includeTpz, boolean includeAdditionalTime, boolean saved) {
        OperationTimesContainer operationTimesContainer = new OperationTimesContainer();
        for (Entity operationComponent : operationComponents) {
            OperationWorkTime operationWorkTime = estimateOperationWorkTime(null, operationComponent,
                    BigDecimalUtils.convertNullToZero(operationRuns.get(operationComponent.getId())), includeTpz,
                    includeAdditionalTime, saved, BigDecimal.ONE);
            operationTimesContainer.add(operationComponent.getDataDefinition().get(operationComponent.getId()),
                    operationWorkTime);
        }
        return operationTimesContainer;
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
            Entity orderTimeCalculation = dataDefinitionService
                    .get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,
                            OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION)
                    .find().add(SearchRestrictions.belongsTo(OrderTimeCalculationFields.ORDER, order)).setMaxResults(1).uniqueResult();
            if (Objects.isNull(orderTimeCalculation)) {
                orderTimeCalculation = dataDefinitionService.get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,
                        OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION).create();
                orderTimeCalculation.setField(OrderTimeCalculationFields.ORDER, order);
                orderTimeCalculation = orderTimeCalculation.getDataDefinition().save(orderTimeCalculation);
            }
            Entity operCompTimeCalculation = dataDefinitionService
                    .get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,
                            OperationTimeCalculationsConstants.MODEL_OPER_COMP_TIME_CALCULATION)
                    .find()
                    .add(SearchRestrictions.belongsTo(OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION, orderTimeCalculation))
                    .add(SearchRestrictions.belongsTo(OperCompTimeCalculationsFields.TECHNOLOGY_OPERATION_COMPONENT,
                            technologyOperationComponent))
                    .setMaxResults(1).uniqueResult();
            if (Objects.isNull(operCompTimeCalculation)) {
                operCompTimeCalculation = dataDefinitionService.get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,
                        OperationTimeCalculationsConstants.MODEL_OPER_COMP_TIME_CALCULATION).create();
                operCompTimeCalculation.setField(OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION, orderTimeCalculation);
                operCompTimeCalculation.setField(OperCompTimeCalculationsFields.TECHNOLOGY_OPERATION_COMPONENT,
                        technologyOperationComponent);
                operCompTimeCalculation = operCompTimeCalculation.getDataDefinition().save(operCompTimeCalculation);
            }
            return operCompTimeCalculation;
        } else {
            Entity operCompTimeCalculation = dataDefinitionService
                    .get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,
                            OperationTimeCalculationsConstants.MODEL_OPER_COMP_TIME_CALCULATION)
                    .find().add(SearchRestrictions.isNull(OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION))
                    .add(SearchRestrictions.belongsTo(OperCompTimeCalculationsFields.TECHNOLOGY_OPERATION_COMPONENT,
                            technologyOperationComponent))
                    .setMaxResults(1).uniqueResult();
            if (Objects.isNull(operCompTimeCalculation)) {
                operCompTimeCalculation = dataDefinitionService.get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,
                        OperationTimeCalculationsConstants.MODEL_OPER_COMP_TIME_CALCULATION).create();
                operCompTimeCalculation.setField(OperCompTimeCalculationsFields.TECHNOLOGY_OPERATION_COMPONENT,
                        technologyOperationComponent);
                operCompTimeCalculation = operCompTimeCalculation.getDataDefinition().save(operCompTimeCalculation);
            }
            return operCompTimeCalculation;
        }
    }

    @Override
    public Entity createOrGetPlanOperCompTimeCalculation(Entity productionLineSchedule, Entity order, Entity productionLine, Entity technologyOperationComponent) {
        Entity orderTimeCalculation = dataDefinitionService
                .get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,
                        OperationTimeCalculationsConstants.MODEL_PLAN_ORDER_TIME_CALCULATION)
                .find()
                .add(SearchRestrictions.belongsTo(PlanOrderTimeCalculationFields.PRODUCTION_LINE_SCHEDULE, productionLineSchedule))
                .add(SearchRestrictions.belongsTo(OrderTimeCalculationFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(PlanOrderTimeCalculationFields.PRODUCTION_LINE, productionLine))
                .setMaxResults(1).uniqueResult();
        if (Objects.isNull(orderTimeCalculation)) {
            orderTimeCalculation = dataDefinitionService.get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,
                    OperationTimeCalculationsConstants.MODEL_PLAN_ORDER_TIME_CALCULATION).create();
            orderTimeCalculation.setField(PlanOrderTimeCalculationFields.PRODUCTION_LINE_SCHEDULE, productionLineSchedule);
            orderTimeCalculation.setField(OrderTimeCalculationFields.ORDER, order);
            orderTimeCalculation.setField(PlanOrderTimeCalculationFields.PRODUCTION_LINE, productionLine);
            orderTimeCalculation = orderTimeCalculation.getDataDefinition().save(orderTimeCalculation);
        }
        Entity operCompTimeCalculation = dataDefinitionService
                .get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,
                        OperationTimeCalculationsConstants.MODEL_PLAN_OPER_COMP_TIME_CALCULATION)
                .find()
                .add(SearchRestrictions.belongsTo(OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION, orderTimeCalculation))
                .add(SearchRestrictions.belongsTo(OperCompTimeCalculationsFields.TECHNOLOGY_OPERATION_COMPONENT,
                        technologyOperationComponent))
                .setMaxResults(1).uniqueResult();
        if (Objects.isNull(operCompTimeCalculation)) {
            operCompTimeCalculation = dataDefinitionService.get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,
                    OperationTimeCalculationsConstants.MODEL_PLAN_OPER_COMP_TIME_CALCULATION).create();
            operCompTimeCalculation.setField(OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION, orderTimeCalculation);
            operCompTimeCalculation.setField(OperCompTimeCalculationsFields.TECHNOLOGY_OPERATION_COMPONENT,
                    technologyOperationComponent);
            operCompTimeCalculation = operCompTimeCalculation.getDataDefinition().save(operCompTimeCalculation);
        }
        return operCompTimeCalculation;
    }

    @Override
    public void deleteOperCompTimeCalculations(Entity order) {
        Entity orderTimeCalculation = dataDefinitionService
                .get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER, OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION)
                .find().add(SearchRestrictions.belongsTo(OrderTimeCalculationFields.ORDER, order)).setMaxResults(1).uniqueResult();
        if (!Objects.isNull(orderTimeCalculation)
                && !orderTimeCalculation.getHasManyField(OrderTimeCalculationFields.OPER_COMP_TIME_CALCULATIONS).isEmpty()) {
            dataDefinitionService
                    .get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,
                            OperationTimeCalculationsConstants.MODEL_OPER_COMP_TIME_CALCULATION)
                    .delete(orderTimeCalculation.getHasManyField(OrderTimeCalculationFields.OPER_COMP_TIME_CALCULATIONS).stream().mapToLong(Entity::getId)
                            .boxed().toArray(Long[]::new));
        }
    }

    @Override
    public void deletePlanOperCompTimeCalculations(Entity productionLineSchedule, Entity order, Entity productionLine) {
        Entity orderTimeCalculation = dataDefinitionService
                .get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER, OperationTimeCalculationsConstants.MODEL_PLAN_ORDER_TIME_CALCULATION)
                .find()
                .add(SearchRestrictions.belongsTo(PlanOrderTimeCalculationFields.PRODUCTION_LINE_SCHEDULE, productionLineSchedule))
                .add(SearchRestrictions.belongsTo(OrderTimeCalculationFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(PlanOrderTimeCalculationFields.PRODUCTION_LINE, productionLine))
                .setMaxResults(1).uniqueResult();
        if (!Objects.isNull(orderTimeCalculation)
                && !orderTimeCalculation.getHasManyField(OrderTimeCalculationFields.OPER_COMP_TIME_CALCULATIONS).isEmpty()) {
            dataDefinitionService
                    .get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,
                            OperationTimeCalculationsConstants.MODEL_PLAN_OPER_COMP_TIME_CALCULATION)
                    .delete(orderTimeCalculation.getHasManyField(OrderTimeCalculationFields.OPER_COMP_TIME_CALCULATIONS).stream().mapToLong(Entity::getId)
                            .boxed().toArray(Long[]::new));
        }
    }

    @Override
    public OperationWorkTime estimateTotalWorkTimeForTechnology(final Entity technology,
                                                                final Map<Long, BigDecimal> operationRuns, final boolean includeTpz, final boolean includeAdditionalTime,
                                                                final boolean saved) {
        List<Entity> operationComponents = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);
        OperationWorkTime totalWorkTime = new OperationWorkTime();
        Integer totalLaborWorkTime = 0;
        Integer totalMachineWorkTime = 0;
        Integer duration = 0;

        for (Entity operationComponent : operationComponents) {
            OperationWorkTime abstractOperationWorkTime = estimateOperationWorkTime(null, operationComponent,
                    BigDecimalUtils.convertNullToZero(operationRuns.get(operationComponent.getId())), includeTpz,
                    includeAdditionalTime, saved, BigDecimal.ONE);
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
    public BigDecimal getQuantityCyclesNeededToProducedNextOperationAfterProducedQuantity(Entity operationComponent, BigDecimal operationRuns,
                                                                                          BigDecimal productComponentQuantity, Entity outputProduct) {
        Entity technology = operationComponent.getBelongsToField(TECHNOLOGY);

        ProductQuantitiesHolder productQuantitiesAndOperationRuns = productQuantitiesService
                .getProductComponentQuantities(technology, BigDecimal.ONE);

        BigDecimal operationsRunsForOneMainProduct = productQuantitiesAndOperationRuns.getOperationRuns().get(operationComponent.getId());
        BigDecimal quantityOutputProductProduced = productQuantitiesAndOperationRuns.getProductQuantities().get(new OperationProductComponentHolder(outputProduct));
        BigDecimal nextOperationAfterProducedQuantity = BigDecimalUtils
                .convertNullToZero(operationComponent.getDecimalField(TechnologyOperationComponentFieldsTNFO.NEXT_OPERATION_AFTER_PRODUCED_QUANTITY));
        if (nextOperationAfterProducedQuantity.compareTo(productComponentQuantity) >= 0) {
            return operationRuns;
        }

        return numberService.setScaleWithDefaultMathContext(operationsRunsForOneMainProduct.multiply(nextOperationAfterProducedQuantity, numberService.getMathContext())
                .divide(quantityOutputProductProduced, numberService.getMathContext()));
    }

    @Override
    public Object setDateToField(final Date date) {
        return new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault()).format(date);
    }
}
