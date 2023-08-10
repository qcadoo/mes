package com.qcadoo.mes.productionScheduling;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.operationTimeCalculations.constants.OperCompTimeCalculationsFields;
import com.qcadoo.mes.operationTimeCalculations.constants.OperationTimeCalculationsConstants;
import com.qcadoo.mes.operationTimeCalculations.constants.OrderTimeCalculationFields;
import com.qcadoo.mes.operationTimeCalculations.constants.PlanOrderTimeCalculationFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionScheduling.constants.OrderFieldsPS;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
public class ProductionSchedulingService {

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public Date getFinishDate(Entity productionLine, Date orderStartDate, long seconds) {
        return shiftsService.findDateToForProductionLine(orderStartDate, seconds,
                productionLine);
    }

    public Date getStartDate(Entity productionLine, Date orderStartDate, Integer offset) {
        if (offset == 0) {
            Date dateFrom = null;
            Optional<DateTime> maybeDate = shiftsService.getNearestWorkingDate(new DateTime(orderStartDate),
                    productionLine);
            if (maybeDate.isPresent()) {
                dateFrom = maybeDate.get().toDate();
            }
            return dateFrom;
        } else {
            return shiftsService.findDateToForProductionLine(orderStartDate, offset,
                    productionLine);
        }
    }

    public void scheduleOrder(final Entity order) {
        Date orderStartDate = order.getDateField(OrderFields.START_DATE);
        Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);

        Entity orderTimeCalculation = scheduleOperationsInOrder(null, order, orderStartDate, productionLine);
        order.setField(OrderFieldsPS.GENERATED_END_DATE, operationWorkTimeService
                .setDateToField(orderTimeCalculation.getDateField(OrderTimeCalculationFields.EFFECTIVE_DATE_TO)));
    }

    public Entity scheduleOperationsInOrder(Entity productionLineSchedule, Entity order,
                                            Date orderStartDate, Entity productionLine) {
        List<Date> operationStartDates = Lists.newArrayList();
        List<Date> operationEndDates = Lists.newArrayList();
        List<Entity> operCompTimeCalculations;
        if (productionLineSchedule == null) {
            operCompTimeCalculations = getOperCompTimeCalculations(order);
        } else {
            operCompTimeCalculations = dataDefinitionService.get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,
                            OperationTimeCalculationsConstants.MODEL_PLAN_OPER_COMP_TIME_CALCULATION).find()
                    .createAlias(OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION, OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION, JoinType.INNER)
                    .createAlias(OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION + L_DOT + OrderTimeCalculationFields.ORDER, OrderTimeCalculationFields.ORDER,
                            JoinType.INNER)
                    .add(SearchRestrictions.eq(OrderTimeCalculationFields.ORDER + L_DOT + L_ID, order.getId()))
                    .add(SearchRestrictions.belongsTo(OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION + L_DOT + PlanOrderTimeCalculationFields.PRODUCTION_LINE_SCHEDULE, productionLineSchedule))
                    .add(SearchRestrictions.belongsTo(OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION + L_DOT + PlanOrderTimeCalculationFields.PRODUCTION_LINE, productionLine))
                    .addOrder(SearchOrders.asc(OperCompTimeCalculationsFields.OPERATION_OFF_SET)).list().getEntities();
        }

        for (Entity operCompTimeCalculation : operCompTimeCalculations) {
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

            Date dateFrom = getStartDate(productionLine, orderStartDate, offset);

            if (dateFrom == null) {
                continue;
            }

            Date dateTo = getFinishDate(productionLine, orderStartDate, (long) offset + duration);
            dateTo = getFinishDateWithChildren(productionLineSchedule, order, productionLine, operCompTimeCalculation.getBelongsToField(OperCompTimeCalculationsFields.TECHNOLOGY_OPERATION_COMPONENT), dateTo);
            operCompTimeCalculation.setField(OperCompTimeCalculationsFields.EFFECTIVE_DATE_FROM, dateFrom);
            operCompTimeCalculation.setField(OperCompTimeCalculationsFields.EFFECTIVE_DATE_TO, dateTo);
            operationStartDates.add(dateFrom);
            operationEndDates.add(dateTo);
            operCompTimeCalculation.getDataDefinition().save(operCompTimeCalculation);
        }
        Entity orderTimeCalculation;
        if (productionLineSchedule == null) {
            orderTimeCalculation = dataDefinitionService
                    .get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER, OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION)
                    .find().add(SearchRestrictions.belongsTo(OrderTimeCalculationFields.ORDER, order)).setMaxResults(1).uniqueResult();
        } else {
            orderTimeCalculation = dataDefinitionService
                    .get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER, OperationTimeCalculationsConstants.MODEL_PLAN_ORDER_TIME_CALCULATION)
                    .find()
                    .add(SearchRestrictions.belongsTo(PlanOrderTimeCalculationFields.PRODUCTION_LINE_SCHEDULE, productionLineSchedule))
                    .add(SearchRestrictions.belongsTo(OrderTimeCalculationFields.ORDER, order))
                    .add(SearchRestrictions.belongsTo(PlanOrderTimeCalculationFields.PRODUCTION_LINE, productionLine))
                    .setMaxResults(1).uniqueResult();
        }
        orderTimeCalculation.setField(OrderTimeCalculationFields.EFFECTIVE_DATE_FROM,
                operationStartDates.stream().min(Comparator.naturalOrder()).get());
        orderTimeCalculation.setField(OrderTimeCalculationFields.EFFECTIVE_DATE_TO,
                operationEndDates.stream().max(Comparator.naturalOrder()).get());
        return orderTimeCalculation.getDataDefinition().save(orderTimeCalculation);
    }

    public List<Entity> getOperCompTimeCalculations(Entity order) {
        return dataDefinitionService.get(OperationTimeCalculationsConstants.PLUGIN_PRODUCTION_SCHEDULING_IDENTIFIER,
                        OperationTimeCalculationsConstants.MODEL_OPER_COMP_TIME_CALCULATION).find()
                .createAlias(OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION, OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION, JoinType.INNER)
                .createAlias(OperationTimeCalculationsConstants.MODEL_ORDER_TIME_CALCULATION + L_DOT + OrderTimeCalculationFields.ORDER, OrderTimeCalculationFields.ORDER,
                        JoinType.INNER)
                .add(SearchRestrictions.eq(OrderTimeCalculationFields.ORDER + L_DOT + L_ID, order.getId()))
                .addOrder(SearchOrders.asc(OperCompTimeCalculationsFields.OPERATION_OFF_SET)).list().getEntities();
    }

    private Date getFinishDateWithChildren(Entity productionLineSchedule, Entity order, Entity productionLine,
                                           Entity technologyOperationComponent, Date dateTo) {
        Date childrenEndTime;
        if (productionLineSchedule == null) {
            childrenEndTime = getChildrenMaxEndTime(order, technologyOperationComponent);
        } else {
            childrenEndTime = getPlanChildrenMaxEndTime(productionLineSchedule, order, productionLine, technologyOperationComponent);
        }
        if (!Objects.isNull(childrenEndTime) && childrenEndTime.after(dateTo)) {
            return childrenEndTime;
        }
        return dateTo;
    }

    private Date getPlanChildrenMaxEndTime(Entity productionLineSchedule, Entity order, Entity productionLine,
                                           Entity technologyOperationComponent) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("productionLineScheduleId", productionLineSchedule.getId());
        parameters.put("orderId", order.getId());
        parameters.put("productionLineId", productionLine.getId());
        parameters.put("tocId", technologyOperationComponent.getId());
        StringBuilder query = new StringBuilder("SELECT MAX(poctc.effectivedateto) ")
                .append("FROM productionscheduling_planopercomptimecalculation poctc ")
                .append("JOIN technologies_technologyoperationcomponent toc ON poctc.technologyoperationcomponent_id = toc.id ")
                .append("JOIN productionscheduling_planordertimecalculation potc ON potc.id = poctc.ordertimecalculation_id ")
                .append("WHERE toc.parent_id = :tocId AND potc.order_id = :orderId ")
                .append("AND potc.productionlineschedule_id = :productionLineScheduleId AND potc.productionline_id = :productionLineId ");

        return jdbcTemplate.queryForObject(query.toString(), parameters, Timestamp.class);
    }

    public Date getChildrenMaxEndTime(Entity order, Entity technologyOperationComponent) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("tocId", technologyOperationComponent.getId());
        StringBuilder query = new StringBuilder("SELECT MAX(octc.effectivedateto) ")
                .append("FROM productionscheduling_opercomptimecalculation octc ")
                .append("JOIN technologies_technologyoperationcomponent toc ON octc.technologyoperationcomponent_id = toc.id ")
                .append("LEFT JOIN productionscheduling_ordertimecalculation otc ON otc.id = octc.ordertimecalculation_id ")
                .append("WHERE toc.parent_id = :tocId ");
        if (order == null) {
            query.append("AND octc.ordertimecalculation_id IS NULL ");
        } else {
            parameters.put("orderId", order.getId());
            query.append("AND otc.order_id = :orderId ");
        }

        return jdbcTemplate.queryForObject(query.toString(), parameters, Timestamp.class);
    }
}
