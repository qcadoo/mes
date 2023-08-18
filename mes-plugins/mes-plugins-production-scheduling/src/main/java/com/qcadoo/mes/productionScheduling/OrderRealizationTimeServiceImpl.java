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
package com.qcadoo.mes.productionScheduling;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.operationTimeCalculations.constants.OperCompTimeCalculationsFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.technologies.ProductionLinesService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.dto.ProductQuantitiesHolder;
import com.qcadoo.mes.timeNormsForOperations.NormService;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.NEXT_OPERATION_AFTER_PRODUCED_TYPE;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.SPECIFIED;

@Service
public class OrderRealizationTimeServiceImpl implements OrderRealizationTimeService {

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductionLinesService productionLinesService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Autowired
    private NormService normService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Override
    @Transactional
    public int estimateOperationTimeConsumption(final Entity productionLineSchedule, final Entity order,
                                                final Entity operationComponent, final boolean includeTpz,
                                                final boolean includeAdditionalTime, boolean maxForWorkstation,
                                                final Entity productionLine,
                                                Optional<ProductQuantitiesHolder> productQuantitiesAndOperationRuns) {
        BigDecimal operationComponentRuns;
        if (productQuantitiesAndOperationRuns.isPresent()) {
            operationComponentRuns = productQuantitiesAndOperationRuns.get().getOperationRuns().get(operationComponent.getId());
        } else {
            operationComponentRuns = basicProductionCountingService.getOperationComponentRuns(order, operationComponent);
        }
        int operationTime = evaluateOperationDurationOutOfCycles(operationComponentRuns, operationComponent, productionLine, maxForWorkstation, includeTpz,
                includeAdditionalTime);
        int offset = 0;

        List<Entity> children = Lists.newArrayList(operationComponent.getHasManyField(TechnologyOperationComponentFields.CHILDREN));
        for (Entity child : children) {
            int childTime = estimateOperationTimeConsumption(productionLineSchedule, order, child, includeTpz, includeAdditionalTime,
                    maxForWorkstation, productionLine, productQuantitiesAndOperationRuns);

            if (SPECIFIED.equals(child.getStringField(NEXT_OPERATION_AFTER_PRODUCED_TYPE))) {
                Entity outputProduct = technologyService.getMainOutputProductComponent(child);
                BigDecimal childRuns;
                BigDecimal productComponentQuantity;
                if (productQuantitiesAndOperationRuns.isPresent()) {
                    childRuns = productQuantitiesAndOperationRuns.get().getOperationRuns().get(child.getId());
                    productComponentQuantity = productQuantitiesAndOperationRuns.get().getProductQuantities().get(new OperationProductComponentHolder(outputProduct));
                } else {
                    childRuns = basicProductionCountingService.getOperationComponentRuns(order, child);
                    productComponentQuantity = basicProductionCountingService.getProductPlannedQuantity(order, child, outputProduct.getBelongsToField(OperationProductOutComponentFields.PRODUCT));
                }
                int childTimeTotal = evaluateOperationDurationOutOfCycles(childRuns, child, productionLine, true, includeTpz,
                        includeAdditionalTime);
                BigDecimal cycles = operationWorkTimeService.getQuantityCyclesNeededToProducedNextOperationAfterProducedQuantity(child, childRuns, productComponentQuantity, outputProduct);
                int childTimeForQuantity = evaluateOperationDurationOutOfCycles(cycles, child, productionLine, true, includeTpz, false);

                int difference = childTimeTotal - childTimeForQuantity;
                childTime -= difference;
            }

            if (childTime > offset) {
                offset = childTime;
            }
        }
        Entity operCompTimeCalculation;
        if (productionLineSchedule == null) {
            operCompTimeCalculation = operationWorkTimeService.createOrGetOperCompTimeCalculation(order, operationComponent);
        } else {
            operCompTimeCalculation = operationWorkTimeService.createOrGetPlanOperCompTimeCalculation(productionLineSchedule, order, productionLine, operationComponent);
        }


        if (operCompTimeCalculation != null) {
            operCompTimeCalculation.setField(OperCompTimeCalculationsFields.OPERATION_OFF_SET, offset);
            operCompTimeCalculation.setField(OperCompTimeCalculationsFields.EFFECTIVE_OPERATION_REALIZATION_TIME, operationTime);
            operCompTimeCalculation.getDataDefinition().save(operCompTimeCalculation);
        }

        return offset + operationTime;
    }

    private Integer retrieveWorkstationTypesCount(final Entity operationComponent, final Entity productionLine) {

            if (parameterService.getParameter().getBooleanField("workstationsQuantityFromProductionLine")) {
                return productionLinesService.getWorkstationTypesCount(operationComponent, productionLine);
            } else {
                return getIntegerValue(
                        operationComponent.getIntegerField(TechnologyOperationComponentFields.QUANTITY_OF_WORKSTATIONS));

            }
    }

    private int evaluateOperationDurationOutOfCycles(final BigDecimal cycles, final Entity operationComponent,
                                                     final Entity productionLine, final boolean maxForWorkstation,
                                                     final boolean includeTpz,
                                                     final boolean includeAdditionalTime) {
        boolean isTjDivisible = operationComponent.getBooleanField(TechnologyOperationComponentFieldsTNFO.IS_TJ_DIVISIBLE);

        Integer workstationsCount = retrieveWorkstationTypesCount(operationComponent, productionLine);
        BigDecimal cyclesPerOperation = cycles;

        if (maxForWorkstation) {
            cyclesPerOperation = cycles.divide(BigDecimal.valueOf(workstationsCount), numberService.getMathContext());

            if (!isTjDivisible) {
                cyclesPerOperation = cyclesPerOperation.setScale(0, RoundingMode.CEILING);
            }
        }
        BigDecimal staffFactor = normService.getStaffFactor(operationComponent, operationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.OPTIMAL_STAFF));
        int tj = getIntegerValue(operationComponent.getField(TechnologyOperationComponentFieldsTNFO.TJ));
        int operationTime = cyclesPerOperation.multiply(BigDecimal.valueOf(tj), numberService.getMathContext()).multiply(staffFactor, numberService.getMathContext()).intValue();

        if (includeTpz) {
            int tpz = getIntegerValue(operationComponent.getField(TechnologyOperationComponentFieldsTNFO.TPZ));
            operationTime += (maxForWorkstation ? tpz : (tpz * workstationsCount));
        }

        if (includeAdditionalTime) {
            int additionalTime = getIntegerValue(operationComponent.getField(TechnologyOperationComponentFieldsTNFO.TIME_NEXT_OPERATION));
            operationTime += (maxForWorkstation ? additionalTime : (additionalTime * workstationsCount));
        }

        return operationTime;
    }

    private Integer getIntegerValue(final Object value) {
        return value == null ? Integer.valueOf(0) : (Integer) value;
    }

    @Override
    public OperationWorkTime estimateTotalWorkTimeForOrder(final Entity order,
                                                           final boolean includeTpz,
                                                           final boolean includeAdditionalTime, final boolean saved) {
        List<Entity> operationComponents = order.getBelongsToField(OrderFields.TECHNOLOGY)
                .getHasManyField(TechnologyFields.OPERATION_COMPONENTS);

        OperationWorkTime totalWorkTime = new OperationWorkTime();
        Integer totalLaborWorkTime = 0;
        Integer totalMachineWorkTime = 0;
        Integer duration = 0;

        for (Entity operationComponent : operationComponents) {
            BigDecimal staffFactor = BigDecimal.ONE;
            if (operationComponent
                    .getBooleanField(TechnologyOperationComponentFieldsTNFO.TJ_DECREASES_FOR_ENLARGED_STAFF)) {
                Integer optimalStaff = operationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.OPTIMAL_STAFF);
                int minStaff = operationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.MIN_STAFF);
                staffFactor = BigDecimal.valueOf(minStaff).divide(BigDecimal.valueOf(optimalStaff), numberService.getMathContext());
            }
            OperationWorkTime abstractOperationWorkTime = operationWorkTimeService.estimateOperationWorkTime(order, operationComponent,
                    basicProductionCountingService.getOperationComponentRuns(order, operationComponent), includeTpz,
                    includeAdditionalTime, saved, staffFactor);
            totalLaborWorkTime += abstractOperationWorkTime.getLaborWorkTime();
            totalMachineWorkTime += abstractOperationWorkTime.getMachineWorkTime();
            duration += abstractOperationWorkTime.getDuration();
        }

        totalWorkTime.setLaborWorkTime(totalLaborWorkTime);
        totalWorkTime.setMachineWorkTime(totalMachineWorkTime);
        totalWorkTime.setDuration(duration);

        return totalWorkTime;
    }

}
