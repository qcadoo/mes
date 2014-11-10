/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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

import static com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields.TECHNOLOGY;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.productionLines.ProductionLinesService;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentEntityType;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.NumberService;

@Service
public class OrderRealizationTimeServiceImpl implements OrderRealizationTimeService {

    private static final String L_ORDER = "order";

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductionLinesService productionLinesService;

    @Autowired
    private ParameterService parameterService;

    @Override
    public Object setDateToField(final Date date) {
        return new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault()).format(date);
    }

    @Override
    @Transactional
    public int estimateOperationTimeConsumption(final EntityTreeNode operationComponent, final BigDecimal plannedQuantity,
            final boolean includeTpz, final boolean includeAdditionalTime, final Entity productionLine) {
        Entity technology = operationComponent.getBelongsToField(TECHNOLOGY);

        Map<Long, BigDecimal> operationRunsFromProductionQuantities = Maps.newHashMap();

        OperationProductComponentWithQuantityContainer productComponentQuantities = productQuantitiesService
                .getProductComponentQuantities(technology, plannedQuantity, operationRunsFromProductionQuantities);

        return evaluateOperationTime(operationComponent, includeTpz, includeAdditionalTime,
                operationRunsFromProductionQuantities, productionLine, false, productComponentQuantities);
    }

    @Override
    @Transactional
    public int estimateMaxOperationTimeConsumptionForWorkstation(final EntityTreeNode operationComponent,
            final BigDecimal plannedQuantity, final boolean includeTpz, final boolean includeAdditionalTime,
            final Entity productionLine) {
        Entity technology = operationComponent.getBelongsToField(TECHNOLOGY);

        Map<Long, BigDecimal> operationRunsFromProductionQuantities = Maps.newHashMap();

        OperationProductComponentWithQuantityContainer productComponentQuantities = productQuantitiesService
                .getProductComponentQuantities(technology, plannedQuantity, operationRunsFromProductionQuantities);

        return evaluateOperationTime(operationComponent, includeTpz, includeAdditionalTime,
                operationRunsFromProductionQuantities, productionLine, true, productComponentQuantities);
    }

    @Override
    public Map<Entity, Integer> estimateOperationTimeConsumptions(final Entity entity, final BigDecimal plannedQuantity,
            final boolean includeTpz, final boolean includeAdditionalTime, final Entity productionLine) {
        return estimateOperationTimeConsumptions(entity, plannedQuantity, includeTpz, includeAdditionalTime, productionLine,
                false);
    }

    @Override
    public Map<Entity, Integer> estimateMaxOperationTimeConsumptionsForWorkstations(final Entity entity,
            final BigDecimal plannedQuantity, final boolean includeTpz, final boolean includeAdditionalTime,
            final Entity productionLine) {
        return estimateOperationTimeConsumptions(entity, plannedQuantity, includeTpz, includeAdditionalTime, productionLine,
                true);
    }

    private Map<Entity, Integer> estimateOperationTimeConsumptions(final Entity entity, final BigDecimal plannedQuantity,
            final boolean includeTpz, final boolean includeAdditionalTime, final Entity productionLine,
            final boolean maxForWorkstation) {
        Map<Entity, Integer> operationDurations = new HashMap<Entity, Integer>();

        String entityType = entity.getDataDefinition().getName();
        Entity technology;
        List<Entity> operationComponents;

        if (TechnologiesConstants.MODEL_TECHNOLOGY.equals(entityType)) {
            technology = entity;

            operationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
        } else if (L_ORDER.equals(entityType)) {
            technology = entity.getBelongsToField(TECHNOLOGY);

            operationComponents = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS);
        } else {
            throw new IllegalStateException("Entity has to be either order or technology");
        }

        Map<Long, BigDecimal> operationRunsFromProductionQuantities = Maps.newHashMap();

        productQuantitiesService
                .getProductComponentQuantities(technology, plannedQuantity, operationRunsFromProductionQuantities);

        for (Entity operationComponent : operationComponents) {
            evaluateTimesConsideringOperationCanBeReferencedTechnology(operationDurations, operationComponent, includeTpz,
                    includeAdditionalTime, operationRunsFromProductionQuantities, productionLine, maxForWorkstation);
        }

        return operationDurations;
    }

    private void evaluateTimesConsideringOperationCanBeReferencedTechnology(final Map<Entity, Integer> operationDurations,
            final Entity operationComponent, final boolean includeTpz, final boolean includeAdditionalTime,
            final Map<Long, BigDecimal> operationRuns, final Entity productionLine, final boolean maxForWorkstation) {
        String entityType = operationComponent.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE);

        if (TechnologyOperationComponentEntityType.REFERENCE_TECHNOLOGY.getStringValue().equals(entityType)) {
            for (Entity operComp : operationComponent.getBelongsToField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY)
                    .getTreeField(TechnologyFields.OPERATION_COMPONENTS)) {
                evaluateTimesConsideringOperationCanBeReferencedTechnology(operationDurations, operComp, includeTpz,
                        includeAdditionalTime, operationRuns, productionLine, maxForWorkstation);
            }
        } else {
            int duration = evaluateSingleOperationTime(operationComponent, includeTpz, includeAdditionalTime, operationRuns,
                    productionLine, maxForWorkstation);

            operationDurations.put(operationComponent, duration);

        }
    }

    private int evaluateOperationTime(final Entity operationComponent, final boolean includeTpz,
            final boolean includeAdditionalTime, final Map<Long, BigDecimal> operationRuns, final Entity productionLine,
            final boolean maxForWorkstation, final OperationProductComponentWithQuantityContainer productComponentQuantities) {
        String entityType = operationComponent.getStringField(TechnologyOperationComponentFields.ENTITY_TYPE);

        if (TechnologyOperationComponentEntityType.REFERENCE_TECHNOLOGY.getStringValue().equals(entityType)) {
            EntityTreeNode actualOperationComponent = operationComponent
                    .getBelongsToField(TechnologyOperationComponentFields.REFERENCE_TECHNOLOGY)
                    .getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot();

            return evaluateOperationTime(actualOperationComponent, includeTpz, includeAdditionalTime, operationRuns,
                    productionLine, maxForWorkstation, productComponentQuantities);
        } else if (TechnologyOperationComponentEntityType.OPERATION.getStringValue().equals(entityType)) {
            int operationTime = evaluateSingleOperationTime(operationComponent, includeTpz, includeAdditionalTime, operationRuns,
                    productionLine, maxForWorkstation);
            int offset = 0;

            List<Entity> childs = Lists.newArrayList(operationComponent.getHasManyField("children"));
            for (Entity child : childs) {
                int childTime = evaluateOperationTime(child, includeTpz, includeAdditionalTime, operationRuns,
                        productionLine,
                        maxForWorkstation, productComponentQuantities);

                if ("02specified".equals(child.getStringField("nextOperationAfterProducedType"))) {

                    int childTimeTotal = evaluateSingleOperationTime(child, includeTpz, includeAdditionalTime, operationRuns,
                            productionLine, true);
                    int childTimeForQuantity = evaluateSingleOperationTimeIncludedNextOperationAfterProducedQuantity(child,
                            includeTpz, false, operationRuns, productionLine, true, productComponentQuantities);

                    int difference = childTimeTotal - childTimeForQuantity;
                    childTime -= difference;
                }

                if (childTime > offset) {
                    offset = childTime;
                }
            }

            if (TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT.equals(operationComponent.getDataDefinition()
                    .getName())) {
                Entity techOperCompTimeCalculation = operationComponent
                        .getBelongsToField(TechnologyOperationComponentFieldsTNFO.TECH_OPER_COMP_TIME_CALCULATION);

                if (techOperCompTimeCalculation != null) {
                    techOperCompTimeCalculation.setField("operationOffSet", offset);
                    techOperCompTimeCalculation.setField("effectiveOperationRealizationTime", operationTime);

                    techOperCompTimeCalculation.getDataDefinition().save(techOperCompTimeCalculation);
                }
            }

            return offset + operationTime;
        }

        throw new IllegalStateException("entityType has to be either operation or referenceTechnology");
    }

    private Integer retrieveWorkstationTypesCount(final Entity operationComponent, final Entity productionLine) {
        if (StringUtils.isEmpty(operationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY)
                .getStringField(TechnologyFields.TECHNOLOGY_TYPE))) {
            if (parameterService.getParameter().getBooleanField("workstationsQuantityFromProductionLine")) {
                return productionLinesService.getWorkstationTypesCount(operationComponent, productionLine);
            } else {
                return getIntegerValue(operationComponent
                        .getIntegerField(TechnologyOperationComponentFields.QUANTITY_OF_WORKSTATIONS));

            }
        } else {
            return getIntegerValue(operationComponent
                    .getIntegerField(TechnologyOperationComponentFields.QUANTITY_OF_WORKSTATIONS));
        }

    }

    @Override
    public int evaluateSingleOperationTime(Entity operationComponent, final boolean includeTpz,
            final boolean includeAdditionalTime, final Map<Long, BigDecimal> operationRuns, final Entity productionLine,
            final boolean maxForWorkstation) {
        operationComponent = operationComponent.getDataDefinition().get(operationComponent.getId());

        BigDecimal cycles = operationRuns.get(operationComponent.getId());
        if (cycles == null) {
            Map<Long, BigDecimal> operationRunsFromProductionQuantities = Maps.newHashMap();

            OperationProductComponentWithQuantityContainer productComponentQuantities = productQuantitiesService
                    .getProductComponentQuantities(
                            operationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY),
                            new BigDecimal("56", numberService.getMathContext()), operationRunsFromProductionQuantities);
            cycles = operationRunsFromProductionQuantities.get(operationComponent.getId());
        }
        return evaluateOperationDurationOutOfCycles(cycles, operationComponent, productionLine, maxForWorkstation, includeTpz,
                includeAdditionalTime);
    }

    @Override
    public int evaluateSingleOperationTimeIncludedNextOperationAfterProducedQuantity(Entity operationComponent,
            final boolean includeTpz, final boolean includeAdditionalTime, final Map<Long, BigDecimal> operationRuns,
            final Entity productionLine, final boolean maxForWorkstation,
            final OperationProductComponentWithQuantityContainer productComponentQuantities) {
        operationComponent = operationComponent.getDataDefinition().get(operationComponent.getId());
        BigDecimal cycles = BigDecimal.ONE;
        BigDecimal nextOperationAfterProducedQuantity = BigDecimalUtils.convertNullToZero(operationComponent
                .getDecimalField("nextOperationAfterProducedQuantity"));
        BigDecimal productComponentQuantity = productComponentQuantities.get(getOutputProduct(operationComponent));
        Entity technologyOperationComponent = getTechnologyOperationComponent(operationComponent);

        if (nextOperationAfterProducedQuantity.compareTo(productComponentQuantity) != 1) {
            cycles = getQuantityCyclesNeededToProducedNextOperationAfterProducedQuantity(technologyOperationComponent,
                    nextOperationAfterProducedQuantity);
        } else {
            cycles = operationRuns.get(technologyOperationComponent.getId());
        }
        return evaluateOperationDurationOutOfCycles(cycles, operationComponent, productionLine, maxForWorkstation, includeTpz,
                includeAdditionalTime);
    }

    private Entity getTechnologyOperationComponent(final Entity operationComponent) {
        return operationComponent;
    }

    private Entity getOutputProduct(final Entity operationComponent) {
        return productQuantitiesService.getOutputProductsFromOperationComponent(operationComponent);
    }

    private BigDecimal getQuantityCyclesNeededToProducedNextOperationAfterProducedQuantity(final Entity operationComponent,
            final BigDecimal nextOperationAfterProducedQuantity) {
        MathContext mc = numberService.getMathContext();
        Entity technology = operationComponent.getBelongsToField("technology");

        Map<Long, BigDecimal> operationRunsFromProductionQuantities = Maps.newHashMap();

        OperationProductComponentWithQuantityContainer productQuantities = productQuantitiesService
                .getProductComponentQuantities(technology, BigDecimal.ONE, operationRunsFromProductionQuantities);

        BigDecimal operationsRunsForOneMainProduct = operationRunsFromProductionQuantities.get(operationComponent.getId());
        BigDecimal quantityOutputProductProduced = productQuantities.get(getOutputProduct(operationComponent));
        BigDecimal cycles = operationsRunsForOneMainProduct.multiply(nextOperationAfterProducedQuantity, mc).divide(
                quantityOutputProductProduced, mc);

        return numberService.setScale(cycles);
    }

    @Override
    public int evaluateOperationDurationOutOfCycles(final BigDecimal cycles, final Entity operationComponent,
            final Entity productionLine, final boolean maxForWorkstation, final boolean includeTpz,
            final boolean includeAdditionalTime) {
        boolean isTjDivisable = operationComponent.getBooleanField("isTjDivisible");

        Integer workstationsCount = retrieveWorkstationTypesCount(operationComponent, productionLine);
        BigDecimal cyclesPerOperation = cycles;

        if (maxForWorkstation) {
            cyclesPerOperation = cycles.divide(BigDecimal.valueOf(workstationsCount), numberService.getMathContext());

            if (!isTjDivisable) {
                cyclesPerOperation = cyclesPerOperation.setScale(0, RoundingMode.CEILING);
            }
        }

        int tj = getIntegerValue(operationComponent.getField("tj"));
        int operationTime = cyclesPerOperation.multiply(BigDecimal.valueOf(tj), numberService.getMathContext()).intValue();

        if (includeTpz) {
            int tpz = getIntegerValue(operationComponent.getField("tpz"));
            operationTime += (maxForWorkstation ? tpz : (tpz * workstationsCount));
        }

        if (includeAdditionalTime) {
            int additionalTime = getIntegerValue(operationComponent.getField("timeNextOperation"));
            operationTime += (maxForWorkstation ? additionalTime : (additionalTime * workstationsCount));
        }

        return operationTime;
    }

    @Override
    public BigDecimal getBigDecimalFromField(final Object value, final Locale locale) {
        try {
            DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(locale);
            format.setParseBigDecimal(true);
            return new BigDecimal(format.parse(value.toString()).doubleValue());
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private Integer getIntegerValue(final Object value) {
        return value == null ? Integer.valueOf(0) : (Integer) value;
    }

    @Override
    public int estimateOperationTimeConsumption(EntityTreeNode operationComponent, BigDecimal plannedQuantity,
            Entity productionLine) {
        return estimateOperationTimeConsumption(operationComponent, plannedQuantity, true, true, productionLine);
    }
}
