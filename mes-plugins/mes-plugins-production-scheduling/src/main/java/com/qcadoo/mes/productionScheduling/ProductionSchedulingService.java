package com.qcadoo.mes.productionScheduling;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.operationTimeCalculations.OrderRealizationTimeService;
import com.qcadoo.mes.operationTimeCalculations.constants.OperCompTimeCalculationsFields;
import com.qcadoo.mes.operationTimeCalculations.constants.OperationTimeCalculationsConstants;
import com.qcadoo.mes.operationTimeCalculations.constants.OrderTimeCalculationFields;
import com.qcadoo.mes.operationTimeCalculations.constants.PlanOrderTimeCalculationFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionScheduling.constants.OrderFieldsPS;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductionSchedulingService {

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OrderRealizationTimeService orderRealizationTimeService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

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

    public void scheduleOrder(final Long orderId) {
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);

        if (order == null) {
            return;
        }

        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (technology == null) {
            return;
        }

        Date orderStartDate = order.getDateField(OrderFields.START_DATE);
        Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);

        Entity orderTimeCalculation = scheduleOperationsInOrder(null, order, technology, orderStartDate, productionLine);
        order.setField(OrderFieldsPS.GENERATED_END_DATE, orderRealizationTimeService
                .setDateToField(orderTimeCalculation.getDateField(OrderTimeCalculationFields.EFFECTIVE_DATE_TO)));
    }

    public Entity scheduleOperationsInOrder(Entity productionLineSchedule, Entity order, Entity technology, Date orderStartDate, Entity productionLine) {
        List<Date> operationStartDates = Lists.newArrayList();
        List<Date> operationEndDates = Lists.newArrayList();

        DataDefinition technologyOperationComponentDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT);

        List<Entity> operations = technologyOperationComponentDD.find()
                .add(SearchRestrictions.belongsTo(TechnologyOperationComponentFields.TECHNOLOGY, technology)).list()
                .getEntities();


        for (Entity operation : operations) {
            Entity operCompTimeCalculation;
            if (productionLineSchedule == null) {
                operCompTimeCalculation = operationWorkTimeService.createOrGetOperCompTimeCalculation(order, operation);
            } else {
                operCompTimeCalculation = operationWorkTimeService.createOrGetPlanOperCompTimeCalculation(productionLineSchedule, order, productionLine, operation);
            }

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

            Date dateFrom = getStartDate(productionLine, orderStartDate, offset);

            if (dateFrom == null) {
                continue;
            }

            Date dateTo = getFinishDate(productionLine, orderStartDate, (long) offset + duration);

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
}
