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
package com.qcadoo.mes.productionScheduling.listeners;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.common.collect.Maps;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ShiftsServiceImpl;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.operationTimeCalculations.OrderRealizationTimeService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.productionScheduling.constants.OrderFieldsPS;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.mes.timeNormsForOperations.constants.TechOperCompTimeCalculationsFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OrderTimePredictionListeners {

    private static final String L_FORM = "form";

    private static final String L_PRODUCTION_SCHEDULING_ERROR_FIELD_REQUIRED = "productionScheduling.error.fieldRequired";

    private static final Integer MAX = 7;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ShiftsServiceImpl shiftsService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private OrderRealizationTimeService orderRealizationTimeService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    public void clearValueOnTechnologyChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(OrderFields.TECHNOLOGY);
        Entity technology = technologyLookup.getEntity();

        if (technology == null) {
            return;
        }

        List<Entity> technologyOperationsComponents = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);

        for (Entity technologyOperationComponent : technologyOperationsComponents) {
            Entity techOperCompTimeCalculation = technologyOperationComponent
                    .getBelongsToField(TechnologyOperationComponentFieldsTNFO.TECH_OPER_COMP_TIME_CALCULATION);

            if (techOperCompTimeCalculation != null) {
                techOperCompTimeCalculation.setField(TechOperCompTimeCalculationsFields.DURATION, null);
                techOperCompTimeCalculation.setField(TechOperCompTimeCalculationsFields.EFFECTIVE_DATE_FROM, null);
                techOperCompTimeCalculation.setField(TechOperCompTimeCalculationsFields.EFFECTIVE_DATE_TO, null);
                techOperCompTimeCalculation.setField(TechOperCompTimeCalculationsFields.EFFECTIVE_OPERATION_REALIZATION_TIME,
                        null);
                techOperCompTimeCalculation.setField(TechOperCompTimeCalculationsFields.LABOR_WORK_TIME, null);
                techOperCompTimeCalculation.setField(TechOperCompTimeCalculationsFields.MACHINE_WORK_TIME, null);
                techOperCompTimeCalculation.setField(TechOperCompTimeCalculationsFields.OPERATION_OFF_SET, null);

                techOperCompTimeCalculation = techOperCompTimeCalculation.getDataDefinition().save(techOperCompTimeCalculation);
            }
        }
    }

    public void fillUnitField(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(OrderFields.TECHNOLOGY);
        FieldComponent unitField = (FieldComponent) view.getComponentByReference(OrderFieldsPS.OPERATION_DURATION_QUANTITY_UNIT);

        Entity technology = technologyLookup.getEntity();

        if (technology != null) {
            Entity product = technology.getBelongsToField(TechnologyFields.PRODUCT);

            if (product != null) {
                unitField.setFieldValue(product.getField(ProductFields.UNIT));
                unitField.requestComponentUpdateState();
            }
        }
    }

    @Transactional
    public void changeRealizationTime(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        FieldComponent technologyLookup = (FieldComponent) view.getComponentByReference(OrderFields.TECHNOLOGY);
        FieldComponent plannedQuantityField = (FieldComponent) view.getComponentByReference(OrderFields.PLANNED_QUANTITY);
        FieldComponent dateFromField = (FieldComponent) view.getComponentByReference(OrderFields.DATE_FROM);
        FieldComponent dateToField = (FieldComponent) view.getComponentByReference(OrderFields.DATE_TO);
        FieldComponent productionLineLookup = (FieldComponent) view.getComponentByReference(OrderFields.PRODUCTION_LINE);

        boolean isGenerated = false;

        if (technologyLookup.getFieldValue() == null) {
            technologyLookup.addMessage(L_PRODUCTION_SCHEDULING_ERROR_FIELD_REQUIRED, MessageType.FAILURE);

            return;
        }

        if (!StringUtils.hasText((String) dateFromField.getFieldValue())) {
            dateFromField.addMessage(L_PRODUCTION_SCHEDULING_ERROR_FIELD_REQUIRED, MessageType.FAILURE);

            return;
        }

        if (!StringUtils.hasText((String) plannedQuantityField.getFieldValue())) {
            plannedQuantityField.addMessage(L_PRODUCTION_SCHEDULING_ERROR_FIELD_REQUIRED, MessageType.FAILURE);

            return;
        }

        if (productionLineLookup.getFieldValue() == null) {
            productionLineLookup.addMessage(L_PRODUCTION_SCHEDULING_ERROR_FIELD_REQUIRED, MessageType.FAILURE);

            return;
        }

        BigDecimal quantity = null;
        Object value = plannedQuantityField.getFieldValue();

        if (value instanceof BigDecimal) {
            quantity = (BigDecimal) value;
        } else {
            try {
                ParsePosition parsePosition = new ParsePosition(0);
                String trimedValue = value.toString().replaceAll(" ", "");
                DecimalFormat formatter = (DecimalFormat) NumberFormat.getNumberInstance(view.getLocale());
                formatter.setParseBigDecimal(true);
                quantity = new BigDecimal(String.valueOf(formatter.parseObject(trimedValue, parsePosition)));
            } catch (NumberFormatException e) {
                plannedQuantityField.addMessage("qcadooView.validate.field.error.invalidNumericFormat", MessageType.FAILURE);

                return;
            }
        }

        int scale = quantity.scale();

        if (MAX != null && scale > MAX) {
            plannedQuantityField.addMessage("qcadooView.validate.field.error.invalidScale.max", MessageType.FAILURE,
                    MAX.toString());
            return;
        }

        int presicion = quantity.precision() - scale;

        if (MAX != null && presicion > MAX) {
            plannedQuantityField.addMessage("qcadooView.validate.field.error.invalidPrecision.max", MessageType.FAILURE,
                    MAX.toString());
            return;
        }

        if (BigDecimal.ZERO.compareTo(quantity) >= 0) {
            plannedQuantityField.addMessage("qcadooView.validate.field.error.outOfRange.toSmall", MessageType.FAILURE);
            return;
        }

        int maxPathTime = 0;

        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get((Long) technologyLookup.getFieldValue());
        Validate.notNull(technology, "technology is null");

        if (technology.getStringField(TechnologyFields.STATE).equals(TechnologyState.DRAFT.getStringValue())
                || technology.getStringField(TechnologyFields.STATE).equals(TechnologyState.OUTDATED.getStringValue())) {
            technologyLookup.addMessage("productionScheduling.technology.incorrectState", MessageType.FAILURE);

            return;
        }

        FieldComponent laborWorkTimeField = (FieldComponent) view.getComponentByReference(OrderFieldsPS.LABOR_WORK_TIME);
        FieldComponent machineWorkTimeField = (FieldComponent) view.getComponentByReference(OrderFieldsPS.MACHINE_WORK_TIME);
        FieldComponent includeTpzField = (FieldComponent) view.getComponentByReference(OrderFieldsPS.INCLUDE_TPZ);
        FieldComponent includeAdditionalTimeField = (FieldComponent) view
                .getComponentByReference(OrderFieldsPS.INCLUDE_ADDITIONAL_TIME);

        Boolean includeTpz = "1".equals(includeTpzField.getFieldValue());
        Boolean includeAdditionalTime = "1".equals(includeAdditionalTimeField.getFieldValue());

        Entity productionLine = dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_PRODUCTION_LINE).get((Long) productionLineLookup.getFieldValue());

        final Map<Long, BigDecimal> operationRuns = Maps.newHashMap();

        productQuantitiesService.getProductComponentQuantities(technology, quantity, operationRuns);

        OperationWorkTime workTime = operationWorkTimeService.estimateTotalWorkTimeForTechnology(technology, operationRuns,
                includeTpz, includeAdditionalTime, productionLine, true);

        laborWorkTimeField.setFieldValue(workTime.getLaborWorkTime());
        machineWorkTimeField.setFieldValue(workTime.getMachineWorkTime());

        maxPathTime = orderRealizationTimeService.estimateOperationTimeConsumption(
                technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot(), quantity, includeTpz,
                includeAdditionalTime, productionLine);

        if (maxPathTime > OrderRealizationTimeService.MAX_REALIZATION_TIME) {
            state.addMessage("orders.validate.global.error.RealizationTimeIsToLong", MessageType.FAILURE);

            dateToField.setFieldValue(null);
        } else {
            Date startTime = DateUtils.parseDate(dateFromField.getFieldValue());

            if (startTime == null) {
                dateFromField.addMessage("orders.validate.global.error.dateFromIsNull", MessageType.FAILURE);
            } else {
                Date stopTime = shiftsService.findDateToForOrder(startTime, maxPathTime);

                if (stopTime == null) {
                    orderForm.addMessage("productionScheduling.timenorms.isZero", MessageType.FAILURE, false);

                    dateToField.setFieldValue(null);
                } else {
                    dateToField.setFieldValue(orderRealizationTimeService.setDateToField(stopTime));

                    startTime = shiftsService.findDateFromForOrder(stopTime, maxPathTime);

                    scheduleOperationComponents(technology.getId(), startTime);

                    isGenerated = true;
                }

                if (startTime != null) {
                    orderForm.addMessage("orders.dateFrom.info.dateFromSetToFirstPossible", MessageType.INFO, false);
                }
            }
        }

        laborWorkTimeField.requestComponentUpdateState();
        machineWorkTimeField.requestComponentUpdateState();
        dateFromField.requestComponentUpdateState();
        dateToField.requestComponentUpdateState();

        orderForm.setEntity(orderForm.getEntity());

        state.performEvent(view, "refresh", new String[0]);

        if (isGenerated) {
            orderForm.addMessage("productionScheduling.info.calculationGenerated", MessageType.SUCCESS);
        }
    }

    private void scheduleOperationComponents(final Long technologyId, final Date startDate) {
        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyId);

        if (technology == null) {
            return;
        }

        DataDefinition technologyOperationComponentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);

        List<Entity> operations = technologyOperationComponentDD.find()
                .add(SearchRestrictions.belongsTo(TechnologiesConstants.MODEL_TECHNOLOGY, technology)).list().getEntities();

        for (Entity operation : operations) {
            Entity techOperCompTimeCalculation = operation
                    .getBelongsToField(TechnologyOperationComponentFieldsTNFO.TECH_OPER_COMP_TIME_CALCULATION);

            if (techOperCompTimeCalculation == null) {
                continue;
            }

            Integer offset = (Integer) techOperCompTimeCalculation.getField(TechOperCompTimeCalculationsFields.OPERATION_OFF_SET);
            Integer duration = (Integer) techOperCompTimeCalculation
                    .getField(TechOperCompTimeCalculationsFields.EFFECTIVE_OPERATION_REALIZATION_TIME);

            techOperCompTimeCalculation.setField(TechOperCompTimeCalculationsFields.EFFECTIVE_DATE_FROM, null);
            techOperCompTimeCalculation.setField(TechOperCompTimeCalculationsFields.EFFECTIVE_DATE_TO, null);

            if (offset == null || duration == null) {
                continue;
            }
            if (duration.equals(0)) {
                duration = duration + 1;
            }

            Date dateFrom = shiftsService.findDateToForOrder(startDate, offset);
            if (dateFrom == null) {
                continue;
            }

            Date dateTo = shiftsService.findDateToForOrder(startDate, offset + duration);
            if (dateTo == null) {
                continue;
            }

            techOperCompTimeCalculation.setField(TechOperCompTimeCalculationsFields.EFFECTIVE_DATE_FROM, dateFrom);
            techOperCompTimeCalculation.setField(TechOperCompTimeCalculationsFields.EFFECTIVE_DATE_TO, dateTo);

            techOperCompTimeCalculation.getDataDefinition().save(techOperCompTimeCalculation);
        }
    }

}
