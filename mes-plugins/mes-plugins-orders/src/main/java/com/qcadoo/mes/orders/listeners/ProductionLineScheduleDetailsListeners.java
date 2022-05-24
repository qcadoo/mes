package com.qcadoo.mes.orders.listeners;

import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.list;
import static com.qcadoo.model.api.search.SearchProjections.rowCount;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.constants.ProductionLineAssignCriterion;
import com.qcadoo.mes.orders.constants.ProductionLineScheduleFields;
import com.qcadoo.mes.orders.constants.ProductionLineSchedulePositionFields;
import com.qcadoo.mes.orders.constants.ProductionLineScheduleSortOrder;
import com.qcadoo.mes.orders.states.ProductionLineScheduleServiceMarker;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.orders.validators.ProductionLineSchedulePositionValidators;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginManager;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class ProductionLineScheduleDetailsListeners {

    private static final String ORDERS_FOR_SUBPRODUCTS_GENERATION = "ordersForSubproductsGeneration";

    @Autowired
    private StateExecutorService stateExecutorService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private ProductionLineSchedulePositionValidators productionLineSchedulePositionValidators;

    public void changeState(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        stateExecutorService.changeState(ProductionLineScheduleServiceMarker.class, view, args);
    }

    @Transactional
    public void generatePlan(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        List<Entity> productionLines = dataDefinitionService
                .get(ProductionLinesConstants.PLUGIN_IDENTIFIER, ProductionLinesConstants.MODEL_PRODUCTION_LINE).find()
                .add(SearchRestrictions.eq(ProductionLineFields.PRODUCTION, true)).list().getEntities();
        if (productionLines.isEmpty()) {
            view.addMessage("orders.error.productionLineScheduleNoProductionLines", ComponentState.MessageType.SUCCESS);
            return;
        }
        FormComponent formComponent = (FormComponent) state;
        GridComponent ordersGrid = (GridComponent) view.getComponentByReference(ProductionLineScheduleFields.ORDERS);
        Entity schedule = getOrders(ordersGrid.getEntities(), formComponent.getEntity());
        formComponent.setEntity(schedule);
        assignOrdersToProductionLines(schedule, productionLines);
        view.addMessage("orders.info.productionLineSchedulePositionsGenerated", ComponentState.MessageType.SUCCESS);
    }

    private void assignOrdersToProductionLines(Entity schedule, List<Entity> productionLines) {
        Map<Long, Date> productionLinesFinishDates = Maps.newHashMap();
        List<Long> positionsIds = sortPositionsForProductionLines(schedule.getId());
        Date scheduleStartTime = schedule.getDateField(ProductionLineScheduleFields.START_TIME);
        boolean allowProductionLineChange = schedule.getBooleanField(ProductionLineScheduleFields.ALLOW_PRODUCTION_LINE_CHANGE);
        boolean canChangeProdLineForAcceptedOrders = parameterService.getParameter().getBooleanField(ParameterFieldsO.CAN_CHANGE_PROD_LINE_FOR_ACCEPTED_ORDERS);
        for (Long positionId : positionsIds) {
            Entity position = dataDefinitionService
                    .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_PRODUCTION_LINE_SCHEDULE_POSITION).get(positionId);
            List<Entity> orderProductionLines = productionLineSchedulePositionValidators.getProductionLinesFromTechnology(position, productionLines,
                    allowProductionLineChange, canChangeProdLineForAcceptedOrders);
            Map<Long, ProductionLinePositionNewData> orderProductionLinesPositionNewData = Maps.newHashMap();
            getProductionLinesNewFinishDate(productionLinesFinishDates, scheduleStartTime,
                    position, orderProductionLines, orderProductionLinesPositionNewData);

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
                updatePositionProductionLineAndDates(firstEntry, productionLinesFinishDates, position);
            } else if (ProductionLineAssignCriterion.SHORTEST_TIME.getStringValue()
                    .equals(schedule.getStringField(ProductionLineScheduleFields.PRODUCTION_LINE_ASSIGN_CRITERION))) {
                orderProductionLinesPositionNewData.entrySet().stream()
                        .min(Comparator.comparing(e -> e.getValue().getFinishDate()))
                        .ifPresent(entry -> updatePositionProductionLineAndDates(entry, productionLinesFinishDates, position));
            } else if (ProductionLineAssignCriterion.LEAST_CHANGEOVERS.getStringValue()
                    .equals(schedule.getStringField(ProductionLineScheduleFields.PRODUCTION_LINE_ASSIGN_CRITERION))) {
            }
        }
    }

    private void updatePositionProductionLineAndDates(Map.Entry<Long, ProductionLinePositionNewData> entry,
                                                      Map<Long, Date> productionLinesFinishDates, Entity position) {
        ProductionLinePositionNewData productionLinePositionNewData = entry.getValue();
        productionLinesFinishDates.put(entry.getKey(), productionLinePositionNewData.getFinishDate());
        position.setField(ProductionLineSchedulePositionFields.PRODUCTION_LINE, entry.getKey());
        position.setField(ProductionLineSchedulePositionFields.START_TIME, productionLinePositionNewData.getStartDate());
        position.setField(ProductionLineSchedulePositionFields.END_TIME, productionLinePositionNewData.getFinishDate());
        position.getDataDefinition().fastSave(position);
    }

    private void getProductionLinesNewFinishDate(Map<Long, Date> productionLinesFinishDates, Date scheduleStartTime,
                                                 Entity position, List<Entity> orderProductionLines,
                                                 Map<Long, ProductionLinePositionNewData> orderProductionLinesPositionNewData) {
        for (Entity productionLine : orderProductionLines) {
            Integer duration = position.getIntegerField(ProductionLineSchedulePositionFields.DURATION);

            Date finishDate = getFinishDate(productionLinesFinishDates, scheduleStartTime, productionLine);
            finishDate = getFinishDateWithChildren(position, finishDate);
            DateTime finishDateTime = new DateTime(finishDate);
            Date newStartDate = shiftsService.getNearestWorkingDate(finishDateTime, productionLine)
                    .orElse(finishDateTime).toDate();

            Date newFinishDate = shiftsService.findDateToForProductionLine(newStartDate, duration,
                    productionLine);
            ProductionLinePositionNewData productionLinePositionNewData = new ProductionLinePositionNewData(newStartDate,
                    newFinishDate);
            orderProductionLinesPositionNewData.put(productionLine.getId(), productionLinePositionNewData);
        }
    }

    private Date getFinishDateWithChildren(Entity position, Date finishDate) {
        if (pluginManager.isPluginEnabled(ORDERS_FOR_SUBPRODUCTS_GENERATION)) {
            Date childEndTime = productionLineSchedulePositionValidators.getOrdersChildrenMaxEndTime(position);
            if (!Objects.isNull(childEndTime) && childEndTime.after(finishDate)) {
                finishDate = childEndTime;
            }
        }
        return finishDate;
    }

    private Date getFinishDate(Map<Long, Date> productionLinesFinishDates, Date scheduleStartTime, Entity productionLine) {
        Date finishDate = productionLinesFinishDates.get(productionLine.getId());
        if (finishDate == null) {
            Date ordersMaxFinishDate = getOrdersMaxFinishDateForProductionLine(scheduleStartTime,
                    productionLine);
            if (ordersMaxFinishDate != null) {
                finishDate = ordersMaxFinishDate;
                productionLinesFinishDates.put(productionLine.getId(), finishDate);
            }
        }
        if (finishDate == null) {
            finishDate = scheduleStartTime;
        }
        return finishDate;
    }

    private Date getOrdersMaxFinishDateForProductionLine(Date scheduleStartTime, Entity productionLine) {
        Entity ordersMaxFinishDateEntity = dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                .add(SearchRestrictions.belongsTo(OrderFields.PRODUCTION_LINE, productionLine))
                .add(SearchRestrictions.ne(OrderFields.STATE, OrderStateStringValues.ABANDONED))
                .add(SearchRestrictions.ne(OrderFields.STATE, OrderStateStringValues.DECLINED))
                .add(SearchRestrictions.gt(OrderFields.FINISH_DATE, scheduleStartTime))
                .setProjection(list()
                        .add(alias(SearchProjections.max(OrderFields.FINISH_DATE), OrderFields.FINISH_DATE))
                        .add(rowCount()))
                .addOrder(SearchOrders.desc(OrderFields.FINISH_DATE)).setMaxResults(1).uniqueResult();
        return ordersMaxFinishDateEntity.getDateField(OrderFields.FINISH_DATE);
    }



    private List<Long> sortPositionsForProductionLines(Long scheduleId) {
        Entity schedule = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_PRODUCTION_LINE_SCHEDULE)
                .get(scheduleId);
        String sortOrder = schedule.getStringField(ProductionLineScheduleFields.SORT_ORDER);
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("scheduleId", scheduleId);
        StringBuilder query = new StringBuilder();
        query.append("SELECT id FROM ");
        query.append("(SELECT sp.id, sp.duration, o.plannedquantity, o.deadline, c.abcanalysis, ");
        query.append("string_to_array(regexp_replace(REVERSE(SPLIT_PART(REVERSE(o.number), '-', 1)), '[^0-9.]', '0', 'g'), '.')::int[] AS osort ");
        query.append("FROM orders_productionlinescheduleposition sp JOIN orders_order o ON sp.order_id = o.id ");
        query.append("LEFT JOIN basic_company c ON o.company_id = c.id ");
        query.append("WHERE sp.productionlineschedule_id = :scheduleId AND o.parent_id IS NOT NULL ");
        query.append("UNION ");
        query.append("SELECT sp.id, sp.duration, o.plannedquantity, o.deadline, c.abcanalysis, ");
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
        schedulePosition.setField(ProductionLineSchedulePositionFields.DURATION, 0);
        return schedulePosition;
    }
}
