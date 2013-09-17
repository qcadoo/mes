/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.productionScheduling;

import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.START_DATE;

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
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
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
public class OrderTimePredictionService {

    private static final String L_PRODUCTION_SCHEDULING_ERROR_FIELD_REQUIRED = "productionScheduling.error.fieldRequired";

    private static final String L_TECHNOLOGY = "technology";

    private static final String L_QUANTITY = "quantity";

    private static final Integer MAX = 5;

    @Autowired
    private OrderRealizationTimeService orderRealizationTimeService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ShiftsServiceImpl shiftsService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    private void scheduleOrder(final Long orderId) {
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);

        if (order == null) {
            return;
        }

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (technology == null) {
            return;
        }

        DataDefinition dataDefinitionTOC = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);

        List<Entity> operations = dataDefinitionTOC.find()
                .add(SearchRestrictions.belongsTo(TechnologiesConstants.MODEL_TECHNOLOGY, technology)).list().getEntities();

        Date orderStartDate = (Date) order.getField(START_DATE);
        for (Entity operation : operations) {
            Entity techOperCompTimeCalculations = operation.getBelongsToField("techOperCompTimeCalculations");

            if (techOperCompTimeCalculations == null) {
                continue;
            }
            Integer offset = (Integer) techOperCompTimeCalculations.getField("operationOffSet");
            Integer duration = (Integer) techOperCompTimeCalculations.getField("effectiveOperationRealizationTime");

            techOperCompTimeCalculations.setField(EFFECTIVE_DATE_FROM, null);
            techOperCompTimeCalculations.setField(EFFECTIVE_DATE_TO, null);

            if (offset == null || duration == null) {
                continue;
            }
            if (duration.equals(0)) {
                duration = duration + 1;
            }
            Date dateFrom = shiftsService.findDateToForOrder(orderStartDate, offset);
            if (dateFrom == null) {
                continue;
            }
            Date dateTo = shiftsService.findDateToForOrder(orderStartDate, offset + duration);
            if (dateTo == null) {
                continue;
            }
            techOperCompTimeCalculations.setField(EFFECTIVE_DATE_FROM, dateFrom);
            techOperCompTimeCalculations.setField(EFFECTIVE_DATE_TO, dateTo);
            techOperCompTimeCalculations.getDataDefinition().save(techOperCompTimeCalculations);
        }
    }

    private void scheduleOperationComponents(final Long technologyId, final Date startDate) {
        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyId);

        if (technology == null) {
            return;
        }

        DataDefinition dataDefinitionTOC = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);

        List<Entity> operations = dataDefinitionTOC.find()
                .add(SearchRestrictions.belongsTo(TechnologiesConstants.MODEL_TECHNOLOGY, technology)).list().getEntities();

        for (Entity operation : operations) {
            Entity techOperCompTimeCalculations = operation.getBelongsToField("techOperCompTimeCalculations");

            if (techOperCompTimeCalculations == null) {
                continue;
            }
            Integer offset = (Integer) techOperCompTimeCalculations.getField("operationOffSet");
            Integer duration = (Integer) techOperCompTimeCalculations.getField("effectiveOperationRealizationTime");

            techOperCompTimeCalculations.setField(EFFECTIVE_DATE_FROM, null);
            techOperCompTimeCalculations.setField(EFFECTIVE_DATE_TO, null);

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
            techOperCompTimeCalculations.setField(EFFECTIVE_DATE_FROM, dateFrom);
            techOperCompTimeCalculations.setField(EFFECTIVE_DATE_TO, dateTo);
            techOperCompTimeCalculations.getDataDefinition().save(techOperCompTimeCalculations);
        }
    }

    public void copyRealizationTime(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        FieldComponent generatedEndDate = (FieldComponent) viewDefinitionState.getComponentByReference("generatedEndDate");
        FieldComponent endDate = (FieldComponent) viewDefinitionState.getComponentByReference("stopTime");
        endDate.setFieldValue(generatedEndDate.getFieldValue());

        state.performEvent(viewDefinitionState, "save", new String[0]);
    }

    @Transactional
    public void generateRealizationTime(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        FieldComponent startTimeField = (FieldComponent) viewDefinitionState.getComponentByReference("startTime");
        if (!StringUtils.hasText((String) startTimeField.getFieldValue())) {
            startTimeField.addMessage("productionScheduling.error.fieldRequired", MessageType.FAILURE);
            return;
        }
        FieldComponent plannedQuantity = (FieldComponent) viewDefinitionState.getComponentByReference("plannedQuantity");
        FieldComponent productionLineLookup = (FieldComponent) viewDefinitionState.getComponentByReference("productionLine");
        FieldComponent generatedEndDate = (FieldComponent) viewDefinitionState.getComponentByReference("generatedEndDate");
        Entity productionLine = dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_PRODUCTION_LINE).get((Long) productionLineLookup.getFieldValue());

        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                form.getEntity().getId());
        // copy of technology from order
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        Validate.notNull(technology, "technology is null");
        BigDecimal quantity = orderRealizationTimeService.getBigDecimalFromField(plannedQuantity.getFieldValue(),
                viewDefinitionState.getLocale());

        // Included in work time
        Boolean includeTpz = "1".equals(viewDefinitionState.getComponentByReference("includeTpz").getFieldValue());
        Boolean includeAdditionalTime = "1".equals(viewDefinitionState.getComponentByReference("includeAdditionalTime")
                .getFieldValue());

        final Map<Long, BigDecimal> operationRuns = Maps.newHashMap();

        productQuantitiesService.getProductComponentQuantities(technology, quantity, operationRuns);

        OperationWorkTime workTime = operationWorkTimeService.estimateTotalWorkTimeForOrder(order, operationRuns, includeTpz,
                includeAdditionalTime, productionLine, true);
        fillWorkTimeFields(viewDefinitionState, workTime);

        order = getActualOrderWithChanges(order);
        int maxPathTime = orderRealizationTimeService.estimateMaxOperationTimeConsumptionForWorkstation(
                technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot(), quantity, includeTpz,
                includeAdditionalTime, productionLine);

        if (maxPathTime > OrderRealizationTimeService.MAX_REALIZATION_TIME) {
            state.addMessage("orders.validate.global.error.RealizationTimeIsToLong", MessageType.FAILURE);
            generatedEndDate.setFieldValue(null);
            generatedEndDate.requestComponentUpdateState();
        } else {
            order.setField("realizationTime", maxPathTime);
            Date startTime = (Date) order.getField(DATE_FROM);
            if (startTime == null) {
                startTimeField.addMessage("orders.validate.global.error.dateFromIsNull", MessageType.FAILURE);
            } else {
                Date generatedStopTime = shiftsService.findDateToForOrder(startTime, maxPathTime);
                if (generatedStopTime == null) {
                    form.addMessage("productionScheduling.timenorms.isZero", MessageType.FAILURE, false);
                } else {
                    generatedEndDate.setFieldValue(orderRealizationTimeService.setDateToField(generatedStopTime));
                    order.setField("generatedEndDate", orderRealizationTimeService.setDateToField(generatedStopTime));
                    scheduleOrder(order.getId());
                }
                generatedEndDate.requestComponentUpdateState();
            }
            order.getDataDefinition().save(order);
        }
    }

    private void fillWorkTimeFields(final ViewDefinitionState view, final OperationWorkTime workTime) {
        FieldComponent laborWorkTime = (FieldComponent) view.getComponentByReference("laborWorkTime");
        FieldComponent machineWorkTime = (FieldComponent) view.getComponentByReference("machineWorkTime");
        laborWorkTime.setFieldValue(workTime.getLaborWorkTime());
        machineWorkTime.setFieldValue(workTime.getMachineWorkTime());
        laborWorkTime.requestComponentUpdateState();
        machineWorkTime.requestComponentUpdateState();
    }

    private Entity getActualOrderWithChanges(final Entity entity) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(entity.getId());
    }

    public Date getDateFromOrdersFromOperation(final List<Entity> operations) {
        Date beforeOperation = null;
        for (Entity operation : operations) {
            Date operationDateFrom = operation.getBelongsToField("techOperCompTimeCalculations")
                    .getDateField("effectiveDateFrom");
            if (operationDateFrom != null) {
                if (beforeOperation == null) {
                    beforeOperation = operationDateFrom;
                }
                if (operationDateFrom.compareTo(beforeOperation) == -1) {
                    beforeOperation = operationDateFrom;
                }
            }
        }
        return beforeOperation;
    }

    public Date getDateToOrdersFromOperation(final List<Entity> operations) {
        Date laterOperation = null;
        for (Entity operation : operations) {
            Date operationDateTo = operation.getBelongsToField("techOperCompTimeCalculations").getDateField("effectiveDateTo");
            if (operationDateTo != null) {
                if (laterOperation == null) {
                    laterOperation = operationDateTo;
                }
                if (operationDateTo.compareTo(laterOperation) == 1) {
                    laterOperation = operationDateTo;
                }
            }
        }
        return laterOperation;
    }

    @Transactional
    public void changeRealizationTime(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FieldComponent technologyLookup = (FieldComponent) viewDefinitionState.getComponentByReference(L_TECHNOLOGY);
        FieldComponent plannedQuantity = (FieldComponent) viewDefinitionState.getComponentByReference(L_QUANTITY);
        FieldComponent dateFrom = (FieldComponent) viewDefinitionState.getComponentByReference(DATE_FROM);
        FieldComponent dateTo = (FieldComponent) viewDefinitionState.getComponentByReference(DATE_TO);
        FieldComponent productionLineLookup = (FieldComponent) viewDefinitionState.getComponentByReference("productionLine");

        if (technologyLookup.getFieldValue() == null) {
            technologyLookup.addMessage(L_PRODUCTION_SCHEDULING_ERROR_FIELD_REQUIRED, MessageType.FAILURE);
            return;
        }
        if (!StringUtils.hasText((String) dateFrom.getFieldValue())) {
            dateFrom.addMessage(L_PRODUCTION_SCHEDULING_ERROR_FIELD_REQUIRED, MessageType.FAILURE);
            return;
        }

        if (!StringUtils.hasText((String) plannedQuantity.getFieldValue())) {
            plannedQuantity.addMessage(L_PRODUCTION_SCHEDULING_ERROR_FIELD_REQUIRED, MessageType.FAILURE);
            return;
        }
        if (productionLineLookup.getFieldValue() == null) {
            productionLineLookup.addMessage(L_PRODUCTION_SCHEDULING_ERROR_FIELD_REQUIRED, MessageType.FAILURE);
            return;
        }

        BigDecimal quantity = null;
        Object value = plannedQuantity.getFieldValue();
        if (value instanceof BigDecimal) {
            quantity = (BigDecimal) value;
        } else {
            try {
                ParsePosition parsePosition = new ParsePosition(0);
                String trimedValue = value.toString().replaceAll(" ", "");
                DecimalFormat formatter = (DecimalFormat) NumberFormat.getNumberInstance(viewDefinitionState.getLocale());
                formatter.setParseBigDecimal(true);
                quantity = new BigDecimal(String.valueOf(formatter.parseObject(trimedValue, parsePosition)));

            } catch (NumberFormatException e) {
                plannedQuantity.addMessage("qcadooView.validate.field.error.invalidNumericFormat", MessageType.FAILURE);
                return;
            }
        }

        int scale = quantity.scale();

        if (MAX != null && scale > MAX) {
            plannedQuantity.addMessage("qcadooView.validate.field.error.invalidScale.max", MessageType.FAILURE, MAX.toString());
            return;
        }

        int presicion = quantity.precision() - scale;
        if (MAX != null && presicion > MAX) {
            plannedQuantity.addMessage("qcadooView.validate.field.error.invalidPrecision.max", MessageType.FAILURE,
                    MAX.toString());
            return;
        }

        if (BigDecimal.ZERO.compareTo(quantity) >= 0) {
            plannedQuantity.addMessage("qcadooView.validate.field.error.outOfRange.toSmall", MessageType.FAILURE);
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
        FieldComponent laborWorkTime = (FieldComponent) viewDefinitionState.getComponentByReference("laborWorkTime");
        FieldComponent machineWorkTime = (FieldComponent) viewDefinitionState.getComponentByReference("machineWorkTime");

        Boolean includeTpz = "1".equals(viewDefinitionState.getComponentByReference("includeTpz").getFieldValue());
        Boolean includeAdditionalTime = "1".equals(viewDefinitionState.getComponentByReference("includeAdditionalTime")
                .getFieldValue());

        Entity productionLine = dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_PRODUCTION_LINE).get((Long) productionLineLookup.getFieldValue());

        final Map<Long, BigDecimal> operationRuns = Maps.newHashMap();

        productQuantitiesService.getProductComponentQuantities(technology, quantity, operationRuns);

        boolean saved = true;
        OperationWorkTime workTime = operationWorkTimeService.estimateTotalWorkTimeForTechnology(technology, operationRuns,
                includeTpz, includeAdditionalTime, productionLine, saved);

        laborWorkTime.setFieldValue(workTime.getLaborWorkTime());
        machineWorkTime.setFieldValue(workTime.getMachineWorkTime());

        maxPathTime = orderRealizationTimeService.estimateOperationTimeConsumption(technology.getTreeField("operationComponents")
                .getRoot(), quantity, productionLine);

        if (maxPathTime > OrderRealizationTimeService.MAX_REALIZATION_TIME) {
            state.addMessage("orders.validate.global.error.RealizationTimeIsToLong", MessageType.FAILURE);
            dateTo.setFieldValue(null);
        } else {
            Date startTime = DateUtils.parseDate(dateFrom.getFieldValue());
            Date stopTime = shiftsService.findDateToForOrder(startTime, maxPathTime);

            if (stopTime != null) {
                startTime = shiftsService.findDateFromForOrder(stopTime, maxPathTime);
            }
            if (startTime == null) {
                dateFrom.setFieldValue(null);
            } else {
                dateFrom.setFieldValue(orderRealizationTimeService.setDateToField(startTime));
            }
            if (stopTime == null) {
                dateTo.setFieldValue(null);
            } else {
                dateTo.setFieldValue(orderRealizationTimeService.setDateToField(stopTime));
            }

            scheduleOperationComponents(technology.getId(), startTime);

        }
        laborWorkTime.requestComponentUpdateState();
        machineWorkTime.requestComponentUpdateState();
        dateFrom.requestComponentUpdateState();
        dateTo.requestComponentUpdateState();
    }

    public void fillUnitField(final ViewDefinitionState viewDefinitionState) {
        FormComponent orderForm = (FormComponent) viewDefinitionState.getComponentByReference("form");
        FieldComponent unitField = (FieldComponent) viewDefinitionState.getComponentByReference("operationDurationQuantityUNIT");

        Long orderId = orderForm.getEntityId();

        if (orderId != null) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);

            if (order != null) {
                Entity product = order.getBelongsToField(OrderFields.PRODUCT);

                if (product != null) {
                    unitField.setFieldValue(product.getField(ProductFields.UNIT));
                }
            }
        }
    }

    public void fillUnitField(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        LookupComponent technologyLookup = (LookupComponent) viewDefinitionState.getComponentByReference("technology");
        FieldComponent unitField = (FieldComponent) viewDefinitionState.getComponentByReference("operationDurationQuantityUNIT");

        Entity technology = technologyLookup.getEntity();

        if (technology != null) {
            Entity product = technology.getBelongsToField(TechnologyFields.PRODUCT);

            if (product != null) {
                unitField.setFieldValue(product.getField(ProductFields.UNIT));
            }
        }
    }

    public void clearFieldValue(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        state.setFieldValue(null);
    }

}
