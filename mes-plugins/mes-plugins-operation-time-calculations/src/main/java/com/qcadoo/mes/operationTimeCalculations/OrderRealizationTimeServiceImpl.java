/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ShiftsServiceImpl;
import com.qcadoo.mes.productionLines.ProductionLinesService;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTreeNode;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.utils.TimeConverterService;

@Service
public class OrderRealizationTimeServiceImpl implements OrderRealizationTimeService {

    private static final String OPERATION_NODE_ENTITY_TYPE = "operation";

    private static final String REFERENCE_TECHNOLOGY_ENTITY_TYPE = "referenceTechnology";

    private final Map<Entity, BigDecimal> operationRunsField = new HashMap<Entity, BigDecimal>();

    @Autowired
    private ShiftsServiceImpl shiftsService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private TimeConverterService timeConverterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionLinesService productionLinesService;

    @Autowired
    private TechnologyService technologyService;

    @Override
    public void changeDateFrom(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent dateTo = (FieldComponent) state;
        FieldComponent dateFrom = (FieldComponent) viewDefinitionState.getComponentByReference("dateFrom");
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference("realizationTime");
        if (StringUtils.hasText((String) dateTo.getFieldValue()) && !StringUtils.hasText((String) dateFrom.getFieldValue())) {
            Date date = shiftsService.findDateFromForOrder(timeConverterService.getDateFromField(dateTo.getFieldValue()),
                    Integer.valueOf((String) realizationTime.getFieldValue()));
            if (date != null) {
                dateFrom.setFieldValue(setDateToField(date));
            }
        }
    }

    @Override
    public void changeDateTo(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent dateFrom = (FieldComponent) state;
        FieldComponent dateTo = (FieldComponent) viewDefinitionState.getComponentByReference("dateTo");
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference("realizationTime");
        if (!StringUtils.hasText((String) dateTo.getFieldValue()) && StringUtils.hasText((String) dateFrom.getFieldValue())) {
            Date date = shiftsService.findDateToForOrder(timeConverterService.getDateFromField(dateFrom.getFieldValue()),
                    Integer.valueOf((String) realizationTime.getFieldValue()));
            if (date != null) {
                dateTo.setFieldValue(setDateToField(date));
            }
        }
    }

    @Override
    public Object setDateToField(final Date date) {
        return new SimpleDateFormat(DateUtils.L_DATE_TIME_FORMAT, Locale.getDefault()).format(date);
    }

    @Override
    @Transactional
    public int estimateOperationTimeConsumption(final EntityTreeNode operationComponent, final BigDecimal plannedQuantity,
            final Entity productionLine) {
        return estimateOperationTimeConsumption(operationComponent, plannedQuantity, true, true, productionLine);
    }

    @Override
    @Transactional
    public int estimateOperationTimeConsumption(final EntityTreeNode operationComponent, final BigDecimal plannedQuantity,
            final boolean includeTpz, final boolean includeAdditionalTime, final Entity productionLine) {
        Entity technology = operationComponent.getBelongsToField("technology");

        Map<Entity, BigDecimal> productComponentQuantities = productQuantitiesService.getProductComponentQuantities(technology,
                plannedQuantity, operationRunsField);

        return evaluateOperationTime(operationComponent, includeTpz, includeAdditionalTime, operationRunsField, productionLine,
                false, productComponentQuantities);
    }

    @Override
    @Transactional
    public int estimateMaxOperationTimeConsumptionForWorkstation(final EntityTreeNode operationComponent,
            final BigDecimal plannedQuantity, final boolean includeTpz, final boolean includeAdditionalTime,
            final Entity productionLine) {
        Entity technology = operationComponent.getBelongsToField("technology");

        Map<Entity, BigDecimal> productComponentQuantities = productQuantitiesService.getProductComponentQuantities(technology,
                plannedQuantity, operationRunsField);

        return evaluateOperationTime(operationComponent, includeTpz, includeAdditionalTime, operationRunsField, productionLine,
                true, productComponentQuantities);
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
        return estimateOperationTimeConsumptions(entity, plannedQuantity, includeTpz, includeAdditionalTime, productionLine, true);
    }

    private Map<Entity, Integer> estimateOperationTimeConsumptions(final Entity entity, final BigDecimal plannedQuantity,
            final boolean includeTpz, final boolean includeAdditionalTime, final Entity productionLine,
            final boolean maxForWorkstation) {
        Map<Entity, Integer> operationDurations = new HashMap<Entity, Integer>();

        String entityType = entity.getDataDefinition().getName();
        Entity technology;
        List<Entity> operationComponents;

        if ("technology".equals(entityType)) {
            technology = entity;

            operationComponents = technology.getTreeField("operationComponents");
        } else if ("order".equals(entityType)) {
            technology = entity.getBelongsToField("technology");

            operationComponents = entity.getTreeField("technologyInstanceOperationComponents");
        } else {
            throw new IllegalStateException("Entity has to be either order or technology");
        }

        productQuantitiesService.getProductComponentQuantities(technology, plannedQuantity, operationRunsField);

        for (Entity operationComponent : operationComponents) {
            evaluateTimesConsideringOperationCanBeReferencedTechnology(operationDurations, operationComponent, includeTpz,
                    includeAdditionalTime, operationRunsField, productionLine, maxForWorkstation);
        }

        return operationDurations;
    }

    private void evaluateTimesConsideringOperationCanBeReferencedTechnology(final Map<Entity, Integer> operationDurations,
            final Entity operationComponent, final boolean includeTpz, final boolean includeAdditionalTime,
            final Map<Entity, BigDecimal> operationRuns, final Entity productionLine, final boolean maxForWorkstation) {
        if (REFERENCE_TECHNOLOGY_ENTITY_TYPE.equals(operationComponent.getStringField("entityType"))) {
            for (Entity operComp : operationComponent.getBelongsToField("referenceTechnology")
                    .getTreeField("operationComponents")) {
                evaluateTimesConsideringOperationCanBeReferencedTechnology(operationDurations, operComp, includeTpz,
                        includeAdditionalTime, operationRuns, productionLine, maxForWorkstation);
            }
        } else {
            int duration = evaluateSingleOperationTime(operationComponent, includeTpz, includeAdditionalTime, operationRunsField,
                    productionLine, maxForWorkstation);
            if ("technologyInstanceOperationComponent".equals(operationComponent.getDataDefinition().getName())) {
                operationDurations.put(operationComponent.getBelongsToField("technologyOperationComponent"), duration);
            } else {
                operationDurations.put(operationComponent, duration);
            }
        }
    }

    private int evaluateOperationTime(final Entity operationComponent, final boolean includeTpz,
            final boolean includeAdditionalTime, final Map<Entity, BigDecimal> operationRuns, final Entity productionLine,
            final boolean maxForWorkstation, Map<Entity, BigDecimal> productComponentQuantities) {
        String entityType = operationComponent.getStringField("entityType");

        if (REFERENCE_TECHNOLOGY_ENTITY_TYPE.equals(entityType)) {
            EntityTreeNode actualOperationComponent = operationComponent.getBelongsToField("referenceTechnology")
                    .getTreeField("operationComponents").getRoot();

            return evaluateOperationTime(actualOperationComponent, includeTpz, includeAdditionalTime, operationRuns,
                    productionLine, maxForWorkstation, productComponentQuantities);
        } else if (OPERATION_NODE_ENTITY_TYPE.equals(entityType)) {
            int operationTime = evaluateSingleOperationTime(operationComponent, includeTpz, includeAdditionalTime, operationRuns,
                    productionLine, maxForWorkstation);
            int offset = 0;

            for (Entity child : operationComponent.getHasManyField("children")) {
                int childTime = evaluateOperationTime(child, includeTpz, includeAdditionalTime, operationRuns, productionLine,
                        maxForWorkstation, productComponentQuantities);

                if ("02specified".equals(child.getStringField("countRealized"))) {
                    BigDecimal quantity = child.getDecimalField("countMachine");

                    int childTimeTotal = evaluateSingleOperationTime(child, true, false, operationRuns, productionLine, true);

                    int childTimeForQuantity = evaluateSingleOperationTime(child, true, false, operationRuns, productionLine,
                            true, quantity);

                    int difference = childTimeTotal - childTimeForQuantity;

                    childTime -= difference;

                    // Entity productComponent = technologyService.getMainOutputProductComponent(child);
                    // BigDecimal operationProduces = productComponentQuantities.get(productComponent);
                    //
                    // if (productCountWhenToStartNextOperation.compareTo(operationProduces) < 0) {
                    // int tpz = includeTpz ? getIntegerValue(child.getField("tpz")) : 0;
                    //
                    // // TODO mici, i think additional time should be omitted
                    // // int additionalTime = includeAdditionalTime ? getIntegerValue(child.getField("timeNextOperation")) : 0;
                    //
                    // Entity technologyOperationComponent = child;
                    //
                    // if ("technologyInstanceOperationComponent".equals(operationComponent.getDataDefinition().getName())) {
                    // technologyOperationComponent = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    // TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(
                    // operationComponent.getBelongsToField("technologyOperationComponent").getId());
                    // }
                    //
                    // BigDecimal cycles = operationRuns.get(technologyOperationComponent);
                    //
                    // BigDecimal producedInOneCycle = technologyService.getProductCountForOperationComponent(child);
                    //
                    // BigDecimal howManyCyclesIsThat = producedInOneCycle.multiply(cycles, numberService.getMathContext())
                    // .divide(productCountWhenToStartNextOperation, numberService.getMathContext());
                    //
                    // boolean isTjDivisable = child.getBooleanField("isTjDivisible");
                    //
                    // if (!isTjDivisable) {
                    // howManyCyclesIsThat = howManyCyclesIsThat.setScale(0, RoundingMode.CEILING);
                    // }
                    //
                    // int tj = getIntegerValue(child.getField("tj"));
                    //
                    // childTimeConsumption = tpz
                    // + howManyCyclesIsThat.multiply(BigDecimal.valueOf(tj), numberService.getMathContext()).intValue();
                    // }
                }

                if (childTime > offset) {
                    offset = childTime;
                }
            }

            if ("technologyInstanceOperationComponent".equals(operationComponent.getDataDefinition().getName())) {
                operationComponent.setField("effectiveOperationRealizationTime", operationTime);
                operationComponent.setField("operationOffSet", offset);
                operationComponent.getDataDefinition().save(operationComponent);
            }

            return offset + operationTime;
        }

        throw new IllegalStateException("entityType has to be either operation or referenceTechnology");
    }

    private Integer retrieveWorkstationTypesCount(final Entity operationComponent, final Entity productionLine) {
        String modelName = operationComponent.getDataDefinition().getName();

        if ("technologyInstanceOperationComponent".equals(modelName)) {
            return (Integer) operationComponent.getField("quantityOfWorkstationTypes");
        } else if ("technologyOperationComponent".equals(modelName)) {
            return productionLinesService.getWorkstationTypesCount(operationComponent, productionLine);
        }

        throw new IllegalStateException(
                "operationComponent is neither technologyInstanceOperationComponent nor technologyOperationComponent");
    }

    private int evaluateSingleOperationTime(Entity operationComponent, final boolean includeTpz,
            final boolean includeAdditionalTime, final Map<Entity, BigDecimal> operationRuns, final Entity productionLine,
            final boolean maxForWorkstation) {
        operationComponent = operationComponent.getDataDefinition().get(operationComponent.getId());

        Entity technologyOperationComponent = operationComponent;

        if ("technologyInstanceOperationComponent".equals(operationComponent.getDataDefinition().getName())) {
            technologyOperationComponent = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(
                    operationComponent.getBelongsToField("technologyOperationComponent").getId());
        }

        BigDecimal cycles = operationRuns.get(technologyOperationComponent);
        return evaluateOperationDurationOutOfCycles(cycles, operationComponent, productionLine, maxForWorkstation, includeTpz,
                includeAdditionalTime);
    }

    private int evaluateSingleOperationTime(Entity operationComponent, final boolean includeTpz,
            final boolean includeAdditionalTime, final Map<Entity, BigDecimal> operationRuns, final Entity productionLine,
            final boolean maxForWorkstation, BigDecimal forQuantity) {
        operationComponent = operationComponent.getDataDefinition().get(operationComponent.getId());

        Entity technologyOperationComponent = operationComponent;

        if ("technologyInstanceOperationComponent".equals(operationComponent.getDataDefinition().getName())) {
            technologyOperationComponent = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).get(
                    operationComponent.getBelongsToField("technologyOperationComponent").getId());
        }

        boolean isTjDivisable = operationComponent.getBooleanField("isTjDivisible");

        BigDecimal producedInOneCycle = technologyService.getProductCountForOperationComponent(operationComponent);
        BigDecimal cycles = operationRuns.get(technologyOperationComponent);
        BigDecimal operationProduces = cycles.multiply(producedInOneCycle, numberService.getMathContext());

        if (forQuantity.compareTo(operationProduces) == -1) {
            cycles = forQuantity.divide(producedInOneCycle, numberService.getMathContext());
            if (!isTjDivisable) {
                cycles = cycles.setScale(0, RoundingMode.CEILING);
            }
        }

        return evaluateOperationDurationOutOfCycles(cycles, operationComponent, productionLine, maxForWorkstation, includeTpz,
                includeAdditionalTime);
    }

    private int evaluateOperationDurationOutOfCycles(final BigDecimal cycles, final Entity operationComponent,
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

    private Integer getIntegerValue(final Object value) {
        return value == null ? Integer.valueOf(0) : (Integer) value;
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
}
