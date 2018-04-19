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
package com.qcadoo.mes.productionScheduling.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.commons.dateTime.TimeRange;
import com.qcadoo.mes.basic.ShiftsServiceImpl;
import com.qcadoo.mes.basic.TimetableExceptionService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ShiftTimetableExceptionFields;
import com.qcadoo.mes.basic.constants.TimetableExceptionType;
import com.qcadoo.mes.basic.shift.Shift;
import com.qcadoo.mes.basic.shift.ShiftsDataProvider;
import com.qcadoo.mes.basic.util.DateTimeRange;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.operationTimeCalculations.OrderRealizationTimeService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.productionScheduling.constants.OrderFieldsPS;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.timeNormsForOperations.constants.OperCompTimeCalculationsFields;
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
import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OperationDurationDetailsInOrderListeners {

    private static final String L_FORM = "form";

    private static final String L_START_TIME = "startTime";

    private static final String L_STOP_TIME = "stopTime";

    private static final String L_PRODUCTION_SCHEDULING_ERROR_FIELD_REQUIRED = "productionScheduling.error.fieldRequired";

    public static final int MAX_LOOPS = 1000;

    public static final int MILLS = 1000;

    public static final int ONE_DAY = 1;

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

    @Autowired
    private ShiftsDataProvider shiftsDataProvider;

    @Autowired
    private TimetableExceptionService timetableExceptionService;

    public void showCopyOfTechnology(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Long orderId = (Long) state.getFieldValue();

        if (orderId != null) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);

            if (OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(order.getField(OrderFields.ORDER_TYPE))
                    && (order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE) == null)) {
                state.addMessage("order.technology.patternTechnology.not.set", MessageType.INFO);

                return;
            }

            Long technologyId = order.getBelongsToField(OrderFields.TECHNOLOGY).getId();
            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put("form.id", technologyId);

            String url = "../page/orders/copyOfTechnologyDetails.html";
            view.redirectTo(url, false, true, parameters);
        }
    }

    @Transactional
    public void generateRealizationTime(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent orderForm = (FormComponent) viewDefinitionState.getComponentByReference(L_FORM);
        FieldComponent startTimeField = (FieldComponent) viewDefinitionState.getComponentByReference(L_START_TIME);
        LookupComponent prodLine = (LookupComponent) viewDefinitionState.getComponentByReference(OrderFields.PRODUCTION_LINE);
        if (!StringUtils.hasText((String) startTimeField.getFieldValue())) {
            startTimeField.addMessage(L_PRODUCTION_SCHEDULING_ERROR_FIELD_REQUIRED, MessageType.FAILURE);

            return;
        }
        if (prodLine.isEmpty()) {
            prodLine.addMessage(L_PRODUCTION_SCHEDULING_ERROR_FIELD_REQUIRED, MessageType.FAILURE);
            return;
        }

        FieldComponent plannedQuantityField = (FieldComponent) viewDefinitionState
                .getComponentByReference(OrderFields.PLANNED_QUANTITY);
        FieldComponent productionLineLookup = (FieldComponent) viewDefinitionState
                .getComponentByReference(OrderFields.PRODUCTION_LINE);
        FieldComponent generatedEndDateField = (FieldComponent) viewDefinitionState
                .getComponentByReference(OrderFieldsPS.GENERATED_END_DATE);
        FieldComponent includeTpzField = (FieldComponent) viewDefinitionState.getComponentByReference(OrderFieldsPS.INCLUDE_TPZ);
        FieldComponent includeAdditionalTimeField = (FieldComponent) viewDefinitionState
                .getComponentByReference(OrderFieldsPS.INCLUDE_ADDITIONAL_TIME);

        boolean isGenerated = false;

        Entity productionLine = dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_PRODUCTION_LINE).get((Long) productionLineLookup.getFieldValue());

        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                orderForm.getEntity().getId());

        // copy of technology from order
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        Validate.notNull(technology, "technology is null");
        BigDecimal quantity = orderRealizationTimeService.getBigDecimalFromField(plannedQuantityField.getFieldValue(),
                viewDefinitionState.getLocale());

        // Included in work time
        Boolean includeTpz = "1".equals(includeTpzField.getFieldValue());
        Boolean includeAdditionalTime = "1".equals(includeAdditionalTimeField.getFieldValue());

        final Map<Long, BigDecimal> operationRuns = Maps.newHashMap();

        productQuantitiesService.getProductComponentQuantities(technology, quantity, operationRuns);

        OperationWorkTime workTime = operationWorkTimeService.estimateTotalWorkTimeForOrder(order, operationRuns, includeTpz,
                includeAdditionalTime, productionLine, true);

        fillWorkTimeFields(viewDefinitionState, workTime);

        order = getActualOrderWithChanges(order);

        int maxPathTime = orderRealizationTimeService.estimateMaxOperationTimeConsumptionForWorkstation(order, technology
                .getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot(), quantity, includeTpz, includeAdditionalTime,
                productionLine);

        if (maxPathTime > OrderRealizationTimeService.MAX_REALIZATION_TIME) {
            state.addMessage("orders.validate.global.error.RealizationTimeIsToLong", MessageType.FAILURE);

            generatedEndDateField.setFieldValue(null);
        } else {
            order.setField(OrderFieldsPS.REALIZATION_TIME, maxPathTime);

            Date startTime = order.getDateField(OrderFields.DATE_FROM);

            if (startTime == null) {
                startTimeField.addMessage("orders.validate.global.error.dateFromIsNull", MessageType.FAILURE);
            } else {
                Date stopTime = shiftsService.findDateToForOrder(startTime, maxPathTime);

                if (stopTime == null) {
                    orderForm.addMessage("productionScheduling.timenorms.isZero", MessageType.FAILURE, false);

                    generatedEndDateField.setFieldValue(null);
                } else {
                    generatedEndDateField.setFieldValue(orderRealizationTimeService.setDateToField(stopTime));

                    order.setField(OrderFieldsPS.GENERATED_END_DATE, orderRealizationTimeService.setDateToField(stopTime));

                    scheduleOrder(order.getId());

                    isGenerated = true;
                }

                orderForm.addMessage("orders.dateFrom.info.dateFromSetToFirstPossible", MessageType.INFO, false);
            }

            order = order.getDataDefinition().save(order);
            orderForm.setEntity(order);
        }

        generatedEndDateField.requestComponentUpdateState();

        if (isGenerated) {
            orderForm.addMessage("productionScheduling.info.calculationGenerated", MessageType.SUCCESS);
        }
    }

    private void fillWorkTimeFields(final ViewDefinitionState view, final OperationWorkTime workTime) {
        FieldComponent laborWorkTimeField = (FieldComponent) view.getComponentByReference(OrderFieldsPS.LABOR_WORK_TIME);
        FieldComponent machineWorkTimeField = (FieldComponent) view.getComponentByReference(OrderFieldsPS.MACHINE_WORK_TIME);

        laborWorkTimeField.setFieldValue(workTime.getLaborWorkTime());
        machineWorkTimeField.setFieldValue(workTime.getMachineWorkTime());

        laborWorkTimeField.requestComponentUpdateState();
        machineWorkTimeField.requestComponentUpdateState();
    }

    private Entity getActualOrderWithChanges(final Entity order) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(order.getId());
    }

    private void scheduleOrder(final Long orderId) {

        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);

        if (order == null) {
            return;
        }

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (technology == null) {
            return;
        }

        List<Entity> shifts = getAllShifts();
        DataDefinition technologyOperationComponentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);

        List<Entity> operations = technologyOperationComponentDD.find()
                .add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology)).list()
                .getEntities();

        Date orderStartDate = order.getDateField(OrderFields.START_DATE);

        for (Entity operation : operations) {
            Entity operCompTimeCalculation = operationWorkTimeService.createOrGetOperCompTimeCalculation(order, operation);

            if (operCompTimeCalculation == null) {
                continue;
            }

            Integer offset = operCompTimeCalculation.getIntegerField(OperCompTimeCalculationsFields.OPERATION_OFF_SET);
            Integer duration = operCompTimeCalculation
                    .getIntegerField(OperCompTimeCalculationsFields.EFFECTIVE_OPERATION_REALIZATION_TIME);

            operCompTimeCalculation.setField(OperCompTimeCalculationsFields.EFFECTIVE_DATE_FROM, null);
            operCompTimeCalculation.setField(OperCompTimeCalculationsFields.EFFECTIVE_DATE_TO, null);

            if (offset == null || duration == null) {
                continue;
            }

            if (duration.equals(0)) {
                duration = duration + 1;
            }

            Date dateFrom = null;
            if (offset == 0) {
                Optional<DateTime> maybeDate = findFirstWorkingDate(order, orderStartDate);
                if (maybeDate.isPresent()) {
                    dateFrom = maybeDate.get().toDate();
                }
            } else {
                Optional<DateTime> maybeDate = findDateFromDatePlusMilliseconds(order, orderStartDate, offset * MILLS);
                if (maybeDate.isPresent()) {
                    dateFrom = maybeDate.get().toDate();
                }
            }

            if (dateFrom == null) {
                continue;
            }

            long milliseconds = offset * MILLS + duration * MILLS;

            Date dateTo = null;
            Optional<DateTime> maybeDate = findDateFromDatePlusMilliseconds(order, orderStartDate, milliseconds);
            if (maybeDate.isPresent()) {
                dateTo = maybeDate.get().toDate();
            }

            if (dateTo == null) {
                continue;
            }

            operCompTimeCalculation.setField(OperCompTimeCalculationsFields.EFFECTIVE_DATE_FROM, dateFrom);
            operCompTimeCalculation.setField(OperCompTimeCalculationsFields.EFFECTIVE_DATE_TO, dateTo);

            operCompTimeCalculation.getDataDefinition().save(operCompTimeCalculation);
        }
    }

    private Optional<DateTime> findDateFromDatePlusMilliseconds(Entity order, Date orderStartDate, long milliseconds) {
        DateTime orderStartDateDT = new DateTime(orderStartDate, DateTimeZone.getDefault());

        List<Shift> shifts = shiftsDataProvider.findAll();
        DateTime dateOfDay = new DateTime(orderStartDate);
        dateOfDay = dateOfDay.minusDays(ONE_DAY);
        dateOfDay = dateOfDay.toLocalDate().toDateTimeAtStartOfDay();
        Long leftMilliseconds = milliseconds;
        int loopCount = 0;
        while (leftMilliseconds > 0l) {
            if (loopCount > MAX_LOOPS) {
                return Optional.empty();
            }
            for (Shift shift : shifts) {
                for (DateTimeRange range : getShiftWorkDateTimes(order.getBelongsToField(OrderFields.PRODUCTION_LINE), shift,
                        dateOfDay, orderStartDate)) {
                    if (orderStartDate.after(dateOfDay.toDate())) {
                        range = range.trimBefore(orderStartDateDT);
                    }
                    if (range != null) {
                        if (leftMilliseconds > range.durationMillis()) {
                            leftMilliseconds = leftMilliseconds - range.durationMillis();
                        } else {
                            return Optional.of(range.getFrom().plusMillis(leftMilliseconds.intValue()));
                        }
                    }
                }
            }
            loopCount++;
            dateOfDay = dateOfDay.plusDays(1);
        }

        return Optional.empty();
    }

    private Optional<DateTime> findFirstWorkingDate(Entity order, Date orderStartDate) {
        DateTime orderStartDateDT = new DateTime(orderStartDate, DateTimeZone.getDefault());

        List<Shift> shifts = shiftsDataProvider.findAll();
        DateTime dateOfDay = new DateTime(orderStartDate);
        dateOfDay = dateOfDay.minusDays(ONE_DAY);
        dateOfDay = dateOfDay.toLocalDate().toDateTimeAtStartOfDay();
        boolean notFound = true;
        int loopCount = 0;
        while (notFound) {
            if (loopCount > MAX_LOOPS) {
                return Optional.empty();
            }
            for (Shift shift : shifts) {
                for (DateTimeRange range : getShiftWorkDateTimes(order.getBelongsToField(OrderFields.PRODUCTION_LINE), shift,
                        dateOfDay, orderStartDate)) {
                    if (orderStartDate.after(dateOfDay.toDate())) {
                        range = range.trimBefore(orderStartDateDT);
                    }
                    if (range != null) {

                        return Optional.of(range.getFrom());

                    }
                }
            }
            loopCount++;
            dateOfDay = dateOfDay.plusDays(1);
        }

        return Optional.empty();
    }

    private List<DateTimeRange> getShiftWorkDateTimes(final Entity productionLine, final Shift shift, DateTime dateOfDay,
            final Date orderStartDate) {
        DateTime dateOfDayDT = dateOfDay;
        List<TimeRange> shiftWorkTime = Lists.newArrayList();
        List<DateTimeRange> shiftWorkDateTime = Lists.newArrayList();
        if (shift.worksAt(dateOfDay.dayOfWeek().get())) {
            shiftWorkTime = shift.findWorkTimeAt(dateOfDay.toLocalDate());
        }
        for (TimeRange range : shiftWorkTime) {
            shiftWorkDateTime.add(new DateTimeRange(dateOfDayDT, range));
        }

        shiftWorkDateTime = manageExceptions(shiftWorkDateTime, productionLine, shift, dateOfDay.toDate());

        return shiftWorkDateTime;
    }

    public List<DateTimeRange> manageExceptions(List<DateTimeRange> shiftWorkDateTime, final Entity productionLine,
            final Shift shift, final Date dateOfDay) {
        Entity shiftEntity = shift.getEntity();
        Shift shiftForDay = new Shift(shiftEntity, new DateTime(dateOfDay), false);

        List<Entity> exceptions = timetableExceptionService.findForLineAndShift(productionLine, shiftEntity);

        if (!exceptions.isEmpty()) {
            for (Entity exception : exceptions) {
                if (TimetableExceptionType.FREE_TIME.getStringValue().equals(
                        exception.getStringField(ShiftTimetableExceptionFields.TYPE))
                        && checkExceptionDates(exception, dateOfDay)) {
                    shiftWorkDateTime = removeFreeTimeException(shiftWorkDateTime, exception, shiftForDay);
                }
            }

            for (Entity exception : exceptions) {
                if (TimetableExceptionType.WORK_TIME.getStringValue().equals(
                        exception.getStringField(ShiftTimetableExceptionFields.TYPE))
                        && checkExceptionDates(exception, dateOfDay)) {
                    shiftWorkDateTime = addWorkTimeException(shiftWorkDateTime, exception, shiftForDay);
                }
            }
        }

        return shiftWorkDateTime;
    }

    private boolean checkExceptionDates(final Entity exception, final Date dateOfDay) {
        return ((new LocalDate(exception.getDateField(ShiftTimetableExceptionFields.TO_DATE)).compareTo(new DateTime(dateOfDay)
                .toLocalDate()) >= 0) && (new LocalDate(exception.getDateField(ShiftTimetableExceptionFields.FROM_DATE))
                .compareTo(new DateTime(dateOfDay).toLocalDate()) <= 0));
    }

    private List<DateTimeRange> removeFreeTimeException(final List<DateTimeRange> shiftWorkDateTime, final Entity exception,
            final Shift shift) {
        Optional<DateTimeRange> exceptionRange = getExceptionRange(exception, shift);

        if (exceptionRange.isPresent()) {
            List<DateTimeRange> result = Lists.newArrayList();

            for (DateTimeRange range : shiftWorkDateTime) {
                result.addAll(range.remove(exceptionRange.get()));
            }

            return result;
        } else {
            return shiftWorkDateTime;
        }
    }

    private List<DateTimeRange> addWorkTimeException(final List<DateTimeRange> shiftWorkDateTime, final Entity exception,
            final Shift shift) {
        Optional<DateTimeRange> exceptionRange = getExceptionRange(exception, shift);

        if (exceptionRange.isPresent()) {
            if (shiftWorkDateTime.isEmpty()) {
                return Lists.newArrayList(exceptionRange.get());
            }

            List<DateTimeRange> result = Lists.newArrayList();

            for (DateTimeRange range : shiftWorkDateTime) {
                result.addAll(range.add(exceptionRange.get()));
            }

            return result;
        } else {
            return shiftWorkDateTime;
        }
    }

    private Optional<DateTimeRange> getExceptionRange(final Entity exception, final Shift shift) {
        Date fromDate = exception.getDateField(ShiftTimetableExceptionFields.FROM_DATE);
        Date toDate = exception.getDateField(ShiftTimetableExceptionFields.TO_DATE);

        if (toDate.before(shift.getShiftStartDate().toDate())) {
            return Optional.empty();
        }

        if (fromDate.before(shift.getShiftStartDate().toDate())) {
            fromDate = shift.getShiftStartDate().toDate();
        }

        if (toDate.after(shift.getShiftEndDate().toDate())) {
            toDate = shift.getShiftEndDate().toDate();
        }

        return Optional.of(new DateTimeRange(fromDate, toDate));
    }

    public void copyRealizationTime(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent generatedEndDateField = (FieldComponent) view.getComponentByReference(OrderFieldsPS.GENERATED_END_DATE);
        FieldComponent stopTimeField = (FieldComponent) view.getComponentByReference(L_STOP_TIME);

        stopTimeField.setFieldValue(generatedEndDateField.getFieldValue());

        state.performEvent(view, "save", new String[0]);
    }

    private List<Entity> getAllShifts() {
        return getShiftDataDefinition().find().list().getEntities();
    }

    private DataDefinition getShiftDataDefinition() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SHIFT);
    }
}
