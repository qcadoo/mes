package com.qcadoo.mes.orders.listeners;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.qcadoo.mes.orders.constants.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.ProductionLineScheduleService;
import com.qcadoo.mes.orders.ProductionLineScheduleServicePPSExecutorService;
import com.qcadoo.mes.orders.ProductionLineScheduleServicePSExecutorService;
import com.qcadoo.mes.orders.states.ProductionLineScheduleServiceMarker;
import com.qcadoo.mes.orders.validators.ProductionLineSchedulePositionValidators;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class ProductionLineScheduleDetailsListeners {

    @Autowired
    private StateExecutorService stateExecutorService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private ProductionLineScheduleService productionLineScheduleService;

    @Autowired
    private ProductionLineSchedulePositionValidators productionLineSchedulePositionValidators;

    @Autowired
    private ProductionLineScheduleServicePSExecutorService productionLineScheduleServicePSExecutorService;

    @Autowired
    private ProductionLineScheduleServicePPSExecutorService productionLineScheduleServicePPSExecutorService;

    public void changeState(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        stateExecutorService.changeState(ProductionLineScheduleServiceMarker.class, view, args);
    }

    @Transactional
    public void generatePlan(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        List<Entity> productionLines = dataDefinitionService
                .get(ProductionLinesConstants.PLUGIN_IDENTIFIER, ProductionLinesConstants.MODEL_PRODUCTION_LINE).find()
                .add(SearchRestrictions.eq(ProductionLineFields.PRODUCTION, true))
                .add(SearchRestrictions.eq(ProductionLineFields.ACTIVE, true)).list().getEntities();
        if (productionLines.isEmpty()) {
            view.addMessage("orders.error.productionLineScheduleNoProductionLines", ComponentState.MessageType.INFO);
            return;
        }
        FormComponent formComponent = (FormComponent) state;
        Entity schedule = formComponent.getPersistedEntityWithIncludedFormValues();
        List<Entity> orders = schedule.getManyToManyField(ProductionLineScheduleFields.ORDERS);
        if (orders.isEmpty()) {
            view.addMessage("orders.error.productionLineScheduleNoOrders", ComponentState.MessageType.INFO);
            return;
        }
        schedule = getOrders(orders, schedule);
        formComponent.setEntity(schedule);
        Entity parameter = parameterService.getParameter();
        if (DurationOfOrderCalculatedOnBasis.PLAN_FOR_SHIFT.getStringValue()
                .equals(schedule.getStringField(ProductionLineScheduleFields.DURATION_OF_ORDER_CALCULATED_ON_BASIS))
                && !parameter.getBooleanField(ParameterFieldsO.PPS_IS_AUTOMATIC)) {
            view.addMessage("orders.error.productionLineSchedulePPSIsNotAutomatic", ComponentState.MessageType.INFO);
            return;
        }
        assignOrdersToProductionLines(schedule, productionLines, parameter);
        view.addMessage("orders.info.productionLineSchedulePositionsGenerated", ComponentState.MessageType.SUCCESS);
    }

    private void assignOrdersToProductionLines(Entity schedule, List<Entity> productionLines, Entity parameter) {
        Map<Long, Date> productionLinesFinishDates = Maps.newHashMap();
        Map<Long, Entity> productionLinesOrders = Maps.newHashMap();
        List<Long> positionsIds = sortPositionsForProductionLines(schedule.getId());
        Date scheduleStartTime = schedule.getDateField(ProductionLineScheduleFields.START_TIME);
        boolean allowProductionLineChange = schedule.getBooleanField(ProductionLineScheduleFields.ALLOW_PRODUCTION_LINE_CHANGE);
        boolean canChangeProdLineForAcceptedOrders = parameter.getBooleanField(ParameterFieldsO.CAN_CHANGE_PROD_LINE_FOR_ACCEPTED_ORDERS);
        String durationOfOrderCalculatedOnBasis = schedule.getStringField(ProductionLineScheduleFields.DURATION_OF_ORDER_CALCULATED_ON_BASIS);
        for (Long positionId : positionsIds) {
            Entity position = dataDefinitionService
                    .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_PRODUCTION_LINE_SCHEDULE_POSITION).get(positionId);
            List<Entity> orderProductionLines = productionLineSchedulePositionValidators.getProductionLinesFromTechnology(position, productionLines,
                    allowProductionLineChange, canChangeProdLineForAcceptedOrders);
            Map<Long, ProductionLinePositionNewData> orderProductionLinesPositionNewData = Maps.newHashMap();
            getProductionLinesNewFinishDate(productionLinesFinishDates, productionLinesOrders, scheduleStartTime,
                    position, orderProductionLines, orderProductionLinesPositionNewData, durationOfOrderCalculatedOnBasis);

            if (orderProductionLinesPositionNewData.isEmpty()) {
                continue;
            }

            if (ProductionLineAssignCriterion.LEAST_PRODUCTION_LINES.getStringValue()
                    .equals(schedule.getStringField(ProductionLineScheduleFields.PRODUCTION_LINE_ASSIGN_CRITERION))) {
                Map.Entry<Long, ProductionLinePositionNewData> firstEntry;
                if (productionLinesFinishDates.isEmpty()) {
                    firstEntry = orderProductionLinesPositionNewData.entrySet().iterator().next();
                } else {
                    firstEntry = orderProductionLinesPositionNewData.entrySet().stream()
                            .filter(entry -> productionLinesFinishDates.containsKey(entry.getKey())).findFirst()
                            .orElse(orderProductionLinesPositionNewData.entrySet().iterator().next());
                }
                updatePositionProductionLineAndDates(firstEntry, productionLinesFinishDates, productionLinesOrders, position);
            } else if (ProductionLineAssignCriterion.SHORTEST_TIME.getStringValue()
                    .equals(schedule.getStringField(ProductionLineScheduleFields.PRODUCTION_LINE_ASSIGN_CRITERION))) {
                orderProductionLinesPositionNewData.entrySet().stream()
                        .min(Comparator.comparing(e -> e.getValue().getFinishDate()))
                        .ifPresent(entry -> updatePositionProductionLineAndDates(entry, productionLinesFinishDates, productionLinesOrders, position));
            } else if (ProductionLineAssignCriterion.LEAST_CHANGEOVERS.getStringValue()
                    .equals(schedule.getStringField(ProductionLineScheduleFields.PRODUCTION_LINE_ASSIGN_CRITERION))) {
                Optional<Map.Entry<Long, ProductionLinePositionNewData>> optionalEntry = orderProductionLinesPositionNewData
                        .entrySet().stream().filter(e -> e.getValue().getChangeover() == null).findFirst();
                if (!optionalEntry.isPresent()) {
                    optionalEntry = orderProductionLinesPositionNewData.entrySet().stream().filter(e -> e.getValue().getChangeover() != null)
                            .min(Comparator.comparing(e -> e.getValue().getChangeover().getIntegerField("duration")));
                }
                optionalEntry.ifPresent(entry -> updatePositionProductionLineAndDates(entry, productionLinesFinishDates, productionLinesOrders, position));
            }
        }
    }

    private void updatePositionProductionLineAndDates(Map.Entry<Long, ProductionLinePositionNewData> entry,
                                                      Map<Long, Date> productionLinesFinishDates, Map<Long, Entity> productionLinesOrders, Entity position) {
        ProductionLinePositionNewData productionLinePositionNewData = entry.getValue();
        productionLinesFinishDates.put(entry.getKey(), productionLinePositionNewData.getFinishDate());
        productionLinesOrders.put(entry.getKey(), position.getBelongsToField(ProductionLineSchedulePositionFields.ORDER));
        position.setField(ProductionLineSchedulePositionFields.PRODUCTION_LINE, entry.getKey());
        position.setField(ProductionLineSchedulePositionFields.START_TIME, productionLinePositionNewData.getStartDate());
        position.setField(ProductionLineSchedulePositionFields.END_TIME, productionLinePositionNewData.getFinishDate());
        String durationOfOrderCalculatedOnBasis = position.getBelongsToField(ProductionLineSchedulePositionFields.PRODUCTION_LINE_SCHEDULE)
                .getStringField(ProductionLineScheduleFields.DURATION_OF_ORDER_CALCULATED_ON_BASIS);
        if (DurationOfOrderCalculatedOnBasis.TIME_CONSUMING_TECHNOLOGY.getStringValue()
                .equals(durationOfOrderCalculatedOnBasis)) {
            productionLineScheduleServicePSExecutorService.savePosition(position, productionLinePositionNewData);
        } else if (DurationOfOrderCalculatedOnBasis.PLAN_FOR_SHIFT.getStringValue()
                .equals(durationOfOrderCalculatedOnBasis)) {
            productionLineScheduleServicePPSExecutorService.savePosition(position, productionLinePositionNewData);
        }
    }

    private void getProductionLinesNewFinishDate(Map<Long, Date> productionLinesFinishDates, Map<Long, Entity> productionLinesOrders, Date scheduleStartTime,
                                                 Entity position, List<Entity> orderProductionLines,
                                                 Map<Long, ProductionLinePositionNewData> orderProductionLinesPositionNewData, String durationOfOrderCalculatedOnBasis) {
        for (Entity productionLine : orderProductionLines) {
            Entity order = position.getBelongsToField(ProductionLineSchedulePositionFields.ORDER);
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
            Date finishDate = productionLineScheduleService.getFinishDate(productionLinesFinishDates, scheduleStartTime, productionLine, order);
            finishDate = productionLineScheduleService.getFinishDateWithChildren(position, finishDate);
            Entity previousOrder = productionLineScheduleService.getPreviousOrder(productionLinesOrders, productionLine, finishDate);
            if (DurationOfOrderCalculatedOnBasis.TIME_CONSUMING_TECHNOLOGY.getStringValue()
                    .equals(durationOfOrderCalculatedOnBasis)) {
                productionLineScheduleServicePSExecutorService.createProductionLinePositionNewData(orderProductionLinesPositionNewData,
                        productionLine, finishDate, position, technology, previousOrder);
            } else if (DurationOfOrderCalculatedOnBasis.PLAN_FOR_SHIFT.getStringValue()
                    .equals(durationOfOrderCalculatedOnBasis)) {
                productionLineScheduleServicePPSExecutorService.createProductionLinePositionNewData(orderProductionLinesPositionNewData,
                        productionLine, finishDate, position, technology, previousOrder);
            }
        }
    }

    private List<Long> sortPositionsForProductionLines(Long scheduleId) {
        Entity schedule = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_PRODUCTION_LINE_SCHEDULE)
                .get(scheduleId);
        String sortOrder = schedule.getStringField(ProductionLineScheduleFields.SORT_ORDER);
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("scheduleId", scheduleId);
        StringBuilder query = new StringBuilder();
        query.append("SELECT id FROM ");
        query.append("(SELECT sp.id, o.finishdate - o.startdate AS duration, o.plannedquantity, o.deadline, c.abcanalysis, ");
        query.append("string_to_array(regexp_replace(REVERSE(SPLIT_PART(REVERSE(o.number), '-', 1)), '[^0-9.]', '0', 'g'), '.')::int[] AS osort ");
        query.append("FROM orders_productionlinescheduleposition sp JOIN orders_order o ON sp.order_id = o.id ");
        query.append("LEFT JOIN basic_company c ON o.company_id = c.id ");
        query.append("WHERE sp.productionlineschedule_id = :scheduleId AND o.parent_id IS NOT NULL ");
        query.append("UNION ");
        query.append("SELECT sp.id, o.finishdate - o.startdate AS duration, o.plannedquantity, o.deadline, c.abcanalysis, ");
        query.append("ARRAY[]::int[] AS osort ");
        query.append("FROM orders_productionlinescheduleposition sp JOIN orders_order o ON sp.order_id = o.id ");
        query.append("LEFT JOIN basic_company c ON o.company_id = c.id ");
        query.append("WHERE sp.productionlineschedule_id = :scheduleId AND o.parent_id IS NULL) AS positions ");
        query.append("ORDER BY osort desc, ");
        if (ProductionLineScheduleSortOrder.LONGEST_ORDERS.getStringValue().equals(sortOrder)) {
            query.append("duration desc");
        } else if (ProductionLineScheduleSortOrder.SHORTEST_ORDERS.getStringValue().equals(sortOrder)) {
            query.append("duration asc");
        } else if (ProductionLineScheduleSortOrder.IMPORTANT_CLIENTS.getStringValue().equals(sortOrder)) {
            query.append("abcanalysis asc");
        } else if (ProductionLineScheduleSortOrder.GREATEST_ORDERED_QUANTITY.getStringValue().equals(sortOrder)) {
            query.append("plannedquantity desc");
        } else if (ProductionLineScheduleSortOrder.SMALLEST_ORDERED_QUANTITY.getStringValue().equals(sortOrder)) {
            query.append("plannedquantity asc");
        } else if (ProductionLineScheduleSortOrder.EARLIEST_DEADLINE.getStringValue().equals(sortOrder)) {
            query.append("deadline asc");
        }
        return jdbcTemplate.queryForList(query.toString(), parameters, Long.class);
    }

    private Entity getOrders(List<Entity> orders, Entity schedule) {
        DataDefinition productionLineSchedulePositionDD = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                OrdersConstants.MODEL_PRODUCTION_LINE_SCHEDULE_POSITION);
        List<Entity> positions = Lists.newArrayList();
        for (Entity order : orders) {
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
            if (technology == null) {
                continue;
            }
            Entity schedulePosition = createSchedulePosition(schedule, productionLineSchedulePositionDD, order);
            positions.add(schedulePosition);
        }

        schedule.setField(ProductionLineScheduleFields.POSITIONS, positions);
        return schedule.getDataDefinition().save(schedule);
    }

    private Entity createSchedulePosition(Entity schedule, DataDefinition productionLineSchedulePositionDD, Entity order) {
        Entity schedulePosition = productionLineSchedulePositionDD.create();
        schedulePosition.setField(ProductionLineSchedulePositionFields.PRODUCTION_LINE_SCHEDULE, schedule);
        schedulePosition.setField(ProductionLineSchedulePositionFields.ORDER, order);
        schedulePosition.setField(ProductionLineSchedulePositionFields.ADDITIONAL_TIME,
                order.getBelongsToField(OrderFields.TECHNOLOGY).getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot()
                        .getIntegerField(TechnologyOperationComponentFieldsTNFO.TIME_NEXT_OPERATION));
        return schedulePosition;
    }

}
