/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.ordersForSubproductsGeneration.productionScheduling.aop;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.operationTimeCalculations.OrderRealizationTimeService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.ordersForSubproductsGeneration.constants.OrdersForSubproductsGenerationConstans;
import com.qcadoo.mes.ordersForSubproductsGeneration.productionScheduling.OrdersByLevel;
import com.qcadoo.mes.ordersForSubproductsGeneration.productionScheduling.ProductionSchedulingForComponentsService;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.productionScheduling.ProductionSchedulingService;
import com.qcadoo.mes.productionScheduling.constants.OrderFieldsPS;
import com.qcadoo.mes.productionScheduling.constants.OrderTimeCalculationFields;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.timeNormsForOperations.constants.OperCompTimeCalculationsFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TimeNormsConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.RunIfEnabled;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Aspect
@Configurable
@RunIfEnabled(OrdersForSubproductsGenerationConstans.PLUGIN_IDENTIFIER)
public class OperationDurationDetailsInOrderListenersOFSPGOverrideAspect {

    private static final String L_START_TIME = "startTime";

    private static final String L_STOP_TIME = "stopTime";

    private static final String L_PRODUCTION_SCHEDULING_ERROR_FIELD_REQUIRED = "productionScheduling.error.fieldRequired";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private OrderRealizationTimeService orderRealizationTimeService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Autowired
    private ProductionSchedulingForComponentsService productionSchedulingForComponentsService;

    @Autowired
    private ProductionSchedulingService productionSchedulingService;

    @Pointcut("execution(public void com.qcadoo.mes.productionScheduling.listeners.OperationDurationDetailsInOrderListeners.copyRealizationTime(..)) "
            + "&& args(viewDefinitionState, state, args)")
    public void copyRealizationTimeE(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
    }

    @Around("copyRealizationTimeE(viewDefinitionState, state, args)")
    public void aroundCopyRealizationTime(final ProceedingJoinPoint pjp, final ViewDefinitionState viewDefinitionState,
            final ComponentState state, final String[] args) throws Throwable {
        CheckBoxComponent component = (CheckBoxComponent) viewDefinitionState
                .getComponentByReference("includeOrdersForComponent");

        if (!component.isChecked()) {
            pjp.proceed();

            return;
        }

        FormComponent orderForm = (FormComponent) viewDefinitionState.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)
                .get(orderForm.getEntity().getId());

        Date startTimeOrder = findCalculatedStartAllOrders(order);

        FieldComponent startTimeField = (FieldComponent) viewDefinitionState.getComponentByReference(L_START_TIME);

        FieldComponent generatedEndDateField = (FieldComponent) viewDefinitionState
                .getComponentByReference("calculatedFinishAllOrders");
        FieldComponent stopTimeField = (FieldComponent) viewDefinitionState.getComponentByReference(L_STOP_TIME);

        startTimeField.setFieldValue(orderRealizationTimeService.setDateToField(startTimeOrder));
        stopTimeField.setFieldValue(generatedEndDateField.getFieldValue());

        state.performEvent(viewDefinitionState, "save");
    }

    @Pointcut("execution(public void com.qcadoo.mes.productionScheduling.listeners.OperationDurationDetailsInOrderListeners.generateRealizationTime(..)) "
            + "&& args(viewDefinitionState, state, args)")
    public void generateRealizationTimeE(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
    }

    @Around("generateRealizationTimeE(viewDefinitionState, state, args)")
    public void aroundGenerateRealizationTime(final ProceedingJoinPoint pjp, final ViewDefinitionState viewDefinitionState,
            final ComponentState state, final String[] args) throws Throwable {
        CheckBoxComponent component = (CheckBoxComponent) viewDefinitionState
                .getComponentByReference("includeOrdersForComponent");

        if (!component.isChecked()) {
            pjp.proceed();

            return;
        }

        FormComponent orderForm = (FormComponent) viewDefinitionState.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent startTimeField = (FieldComponent) viewDefinitionState.getComponentByReference(L_START_TIME);

        if (!StringUtils.hasText((String) startTimeField.getFieldValue())) {
            startTimeField.addMessage(L_PRODUCTION_SCHEDULING_ERROR_FIELD_REQUIRED, ComponentState.MessageType.FAILURE);
            return;
        }

        FieldComponent plannedQuantityField = (FieldComponent) viewDefinitionState
                .getComponentByReference(OrderFields.PLANNED_QUANTITY);
        FieldComponent productionLineLookup = (FieldComponent) viewDefinitionState
                .getComponentByReference(OrderFields.PRODUCTION_LINE);
        FieldComponent generatedEndDateField = (FieldComponent) viewDefinitionState
                .getComponentByReference(OrderFieldsPS.GENERATED_END_DATE);
        FieldComponent calculatedFinishAllOrdersField = (FieldComponent) viewDefinitionState
                .getComponentByReference("calculatedFinishAllOrders");
        FieldComponent calculatedStartAllOrdersField = (FieldComponent) viewDefinitionState
                .getComponentByReference("calculatedStartAllOrders");
        FieldComponent includeTpzField = (FieldComponent) viewDefinitionState.getComponentByReference(OrderFieldsPS.INCLUDE_TPZ);
        FieldComponent includeAdditionalTimeField = (FieldComponent) viewDefinitionState
                .getComponentByReference(OrderFieldsPS.INCLUDE_ADDITIONAL_TIME);

        boolean isGenerated = false;

        Entity productionLine = dataDefinitionService
                .get(ProductionLinesConstants.PLUGIN_IDENTIFIER, ProductionLinesConstants.MODEL_PRODUCTION_LINE)
                .get((Long) productionLineLookup.getFieldValue());

        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)
                .get(orderForm.getEntity().getId());

        // copy of technology from order
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        Validate.notNull(technology, "technology is null");
        BigDecimal quantity = orderRealizationTimeService.getBigDecimalFromField(plannedQuantityField.getFieldValue(),
                viewDefinitionState.getLocale());

        // Included in work time
        boolean includeTpz = "1".equals(includeTpzField.getFieldValue());
        boolean includeAdditionalTime = "1".equals(includeAdditionalTimeField.getFieldValue());

        final Map<Long, BigDecimal> operationRuns = Maps.newHashMap();

        productQuantitiesService.getProductComponentQuantities(technology, quantity, operationRuns);

        operationWorkTimeService.deleteOperCompTimeCalculations(order);

        OperationWorkTime workTime = operationWorkTimeService.estimateTotalWorkTimeForOrder(order, operationRuns, includeTpz,
                includeAdditionalTime, true);

        List<Entity> orders = getOrderAndSubOrders(order.getId());

        Map<Integer, OrdersByLevel> ordersByLevel = productionSchedulingForComponentsService.mapToOrdersByLevel(orders);

        List<Integer> keys = Lists.newArrayList(ordersByLevel.keySet());
        keys.sort(Collections.reverseOrder());

        Date orderStartDate = order.getDateField(OrderFields.START_DATE);
        Date lastDateTo = orderStartDate;

        for (Integer key : keys) {
            OrdersByLevel ords = ordersByLevel.get(key);
            Date currentDateTo = lastDateTo;

            for (Entity o : ords.getOrders()) {
                Entity t = o.getBelongsToField(OrderFields.TECHNOLOGY);
                final Map<Long, BigDecimal> oR = Maps.newHashMap();

                productQuantitiesService.getProductComponentQuantities(t, quantity, oR);
                operationWorkTimeService.estimateTotalWorkTimeForOrder(o, oR, includeTpz, includeAdditionalTime, true);

                int maxPathTime = orderRealizationTimeService.estimateMaxOperationTimeConsumptionForWorkstation(o,
                        t.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot(), quantity, includeTpz,
                        includeAdditionalTime, productionLine);

                if (maxPathTime > OrderRealizationTimeService.MAX_REALIZATION_TIME) {
                    state.addMessage("orders.validate.global.error.RealizationTimeIsToLong", ComponentState.MessageType.FAILURE);

                    if (o.getId().equals(order.getId())) {
                        generatedEndDateField.setFieldValue(null);
                    }
                } else {
                    o.setField(OrderFieldsPS.REALIZATION_TIME, maxPathTime);

                    Date startTime = o.getDateField(OrderFields.DATE_FROM);
                    if (Objects.isNull(startTime)) {
                        startTime = orderStartDate;
                    }

                    if (startTime == null) {
                        startTimeField.addMessage("orders.validate.global.error.dateFromIsNull",
                                ComponentState.MessageType.FAILURE);
                    } else {
                        if (maxPathTime == 0) {
                            orderForm.addMessage("productionScheduling.timenorms.isZero", ComponentState.MessageType.FAILURE,
                                    false);

                            if (o.getId().equals(order.getId())) {
                                generatedEndDateField.setFieldValue(null);
                            }
                        } else {

                            lastDateTo = scheduleOperationsInOrder(o, currentDateTo);

                            isGenerated = true;
                        }

                        orderForm.addMessage("orders.dateFrom.info.dateFromSetToFirstPossible", ComponentState.MessageType.INFO,
                                false);
                    }

                    o.getDataDefinition().save(o);
                }
            }
        }

        fillWorkTimeFields(viewDefinitionState, workTime);

        generatedEndDateField.requestComponentUpdateState();

        if (isGenerated) {
            order = getActualOrderWithChanges(order);
            Entity orderTimeCalculation = dataDefinitionService
                    .get(TimeNormsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,
                            TimeNormsConstants.MODEL_ORDER_TIME_CALCULATION)
                    .find().add(SearchRestrictions.belongsTo("order", order)).setMaxResults(1).uniqueResult();

            Date startTimeOrders = findCalculatedStartAllOrders(order);
            order.setField("calculatedStartAllOrders", orderRealizationTimeService.setDateToField(startTimeOrders));
            calculatedStartAllOrdersField.setFieldValue(orderRealizationTimeService.setDateToField(startTimeOrders));

            Date finishDate = orderTimeCalculation.getDateField(OrderTimeCalculationFields.EFFECTIVE_DATE_TO);
            generatedEndDateField.setFieldValue(orderRealizationTimeService.setDateToField(finishDate));
            calculatedFinishAllOrdersField.setFieldValue(orderRealizationTimeService.setDateToField(finishDate));

            order.setField("calculatedFinishAllOrders", orderRealizationTimeService.setDateToField(finishDate));

            order = order.getDataDefinition().save(order);
            orderForm.setEntity(order);
            orderForm.addMessage("productionScheduling.info.calculationGenerated", ComponentState.MessageType.SUCCESS);
            state.performEvent(viewDefinitionState, "reset");
        }
    }

    private Date findCalculatedStartAllOrders(final Entity order) {
        List<Entity> ordersTimeCalculations = dataDefinitionService
                .get(TimeNormsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER, TimeNormsConstants.MODEL_ORDER_TIME_CALCULATION)
                .find().createAlias("order", "ord", JoinType.LEFT)
                .add(SearchRestrictions.in("ord.id",
                        getOrderAndSubOrders(order.getId()).stream().map(Entity::getId).collect(Collectors.toList())))
                .list().getEntities();

        return ordersTimeCalculations.stream().map(e -> e.getDateField(OrderTimeCalculationFields.EFFECTIVE_DATE_FROM))
                .min(Comparator.naturalOrder()).get();
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

    private Date scheduleOperationsInOrder(final Entity order, final Date currentDateTo) {
        if (order == null) {
            return null;
        }

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (technology == null) {
            return null;
        }

        List<Date> operationStartDates = Lists.newArrayList();
        List<Date> operationEndDates = Lists.newArrayList();

        DataDefinition technologyOperationComponentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);

        List<Entity> operations = technologyOperationComponentDD.find()
                .add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology)).list()
                .getEntities();

        Date orderFinishDate = currentDateTo;

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

            Date dateFrom = productionSchedulingService.getStartDate(order, currentDateTo, offset);

            if (dateFrom == null) {
                continue;
            }

            Date dateTo = productionSchedulingService.getFinishDate(order, currentDateTo, (long) offset + duration);

            operCompTimeCalculation.setField(OperCompTimeCalculationsFields.EFFECTIVE_DATE_FROM, dateFrom);
            operCompTimeCalculation.setField(OperCompTimeCalculationsFields.EFFECTIVE_DATE_TO, dateTo);

            operationStartDates.add(dateFrom);
            operationEndDates.add(dateTo);

            operCompTimeCalculation.getDataDefinition().save(operCompTimeCalculation);

            if (Objects.isNull(orderFinishDate)) {
                orderFinishDate = dateTo;
            } else if (dateTo.after(orderFinishDate)) {
                orderFinishDate = dateTo;
            }
        }

        Entity orderTimeCalculation = dataDefinitionService
                .get(TimeNormsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER, TimeNormsConstants.MODEL_ORDER_TIME_CALCULATION)
                .find().add(SearchRestrictions.belongsTo("order", order)).setMaxResults(1).uniqueResult();
        orderTimeCalculation.setField(OrderTimeCalculationFields.EFFECTIVE_DATE_FROM,
                operationStartDates.stream().min(Comparator.naturalOrder()).get());
        orderTimeCalculation.setField(OrderTimeCalculationFields.EFFECTIVE_DATE_TO,
                operationEndDates.stream().max(Comparator.naturalOrder()).get());
        orderTimeCalculation.getDataDefinition().save(orderTimeCalculation);
        order.setField(OrderFieldsPS.GENERATED_END_DATE, orderRealizationTimeService
                .setDateToField(orderTimeCalculation.getDateField(OrderTimeCalculationFields.EFFECTIVE_DATE_TO)));

        return orderFinishDate;
    }

    private DataDefinition getOrderDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
    }

    private List<Entity> getOrderAndSubOrders(final Long orderID) {
        String sql = "SELECT o FROM #orders_order AS o WHERE o.root = :orderID OR o.id = :orderID";

        return getOrderDD().find(sql).setLong("orderID", orderID).list().getEntities();
    }

}
