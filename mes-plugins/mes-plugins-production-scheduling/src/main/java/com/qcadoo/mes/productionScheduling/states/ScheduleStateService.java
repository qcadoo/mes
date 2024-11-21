package com.qcadoo.mes.productionScheduling.states;

import com.qcadoo.mes.newstates.BasicStateService;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.constants.*;
import com.qcadoo.mes.orders.hooks.OperationalTaskHooks;
import com.qcadoo.mes.orders.services.WorkstationChangeoverService;
import com.qcadoo.mes.orders.states.OperationalTaskOrderStateService;
import com.qcadoo.mes.orders.states.ProductionLineScheduleStateService;
import com.qcadoo.mes.orders.states.constants.OperationalTaskState;
import com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.orders.states.constants.ScheduleStateStringValues;
import com.qcadoo.mes.productionScheduling.hooks.OperationalTaskHooksPS;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.constants.WorkstationChangeoverNormFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.qcadoo.model.api.search.SearchOrders.desc;
import static com.qcadoo.model.api.search.SearchProjections.*;

@Service
public class ScheduleStateService extends BasicStateService implements ScheduleServiceMarker {

    private static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    private static final String L_FOR_EACH = "03forEach";

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    @Autowired
    private ScheduleStateChangeDescriber scheduleStateChangeDescriber;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OperationalTaskOrderStateService operationalTaskOrderStateService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Autowired
    private OperationalTaskHooks operationalTaskHooks;

    @Autowired
    private OperationalTaskHooksPS operationalTaskHooksPS;

    @Autowired
    private ProductionLineScheduleStateService productionLineScheduleStateService;

    @Autowired
    private WorkstationChangeoverService workstationChangeoverService;

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return scheduleStateChangeDescriber;
    }

    @Override
    public Entity onValidate(Entity entity, String sourceState, String targetState, Entity stateChangeEntity,
                             StateChangeEntityDescriber describer) {
        switch (targetState) {
            case ScheduleStateStringValues.APPROVED:
                checkIfScheduleHasNotPositions(entity);
                checkOrderTypeOfProductionRecording(entity);
                checkOrderStatesForApproved(entity);
                checkExistingOperationalTasksState(entity);
                checkSchedulePositionsDatesIsNotEmpty(entity);
                checkOrdersTechnologiesChanged(entity);
                break;

            case ScheduleStateStringValues.REJECTED:
                checkOrderStatesForRejected(entity);
        }

        return entity;
    }

    private void checkOrdersTechnologiesChanged(Entity entity) {
        List<Entity> orders = dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SCHEDULE_POSITION).find()
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.SCHEDULE, entity))
                .createAlias(SchedulePositionFields.ORDER, SchedulePositionFields.ORDER, JoinType.INNER)
                .createAlias(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT,
                        SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT, JoinType.INNER)
                .add(SearchRestrictions.or(SearchRestrictions.isNull(SchedulePositionFields.ORDER + "." + OrderFields.TECHNOLOGY),
                        SearchRestrictions.neField(SchedulePositionFields.ORDER + "." + OrderFields.TECHNOLOGY,
                                SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT + "."
                                        + TechnologyOperationComponentFields.TECHNOLOGY)))
                .setProjection(
                        list().add(alias(SearchProjections.groupField(SchedulePositionFields.ORDER + "." + OrderFields.NUMBER),
                                OrderFields.NUMBER)))
                .addOrder(desc(SchedulePositionFields.ORDER + "." + OrderFields.NUMBER)).list().getEntities();
        if (!orders.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Entity order : orders) {
                sb.append(order.getStringField(OrderFields.NUMBER));
                sb.append(", ");
            }
            entity.addGlobalError("orders.schedule.orders.technologyChanged", false, true, sb.toString());
        }
    }

    private void checkOrderTypeOfProductionRecording(Entity entity) {
        List<Entity> orders = entity.getHasManyField(ScheduleFields.POSITIONS).stream()
                .map(e -> e.getBelongsToField(SchedulePositionFields.ORDER)).distinct().collect(Collectors.toList());
        for (Entity order : orders) {
            if (!L_FOR_EACH.equals(order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING))) {
                entity.addGlobalError("orders.schedule.orders.wrongTypeOfProductionRecording");
                break;
            }
        }
    }

    private void checkOrderStatesForRejected(Entity entity) {
        List<Entity> orders = entity.getHasManyField(ScheduleFields.POSITIONS).stream()
                .map(e -> e.getBelongsToField(SchedulePositionFields.ORDER)).distinct().collect(Collectors.toList());
        for (Entity order : orders) {
            if (OrderStateStringValues.COMPLETED.equals(order.getStringField(OrderFields.STATE))
                    || OrderStateStringValues.IN_PROGRESS.equals(order.getStringField(OrderFields.STATE))) {
                entity.addGlobalError("orders.schedule.orders.wrongState");
                break;
            }
        }
    }

    private void checkOrderStatesForApproved(Entity entity) {
        List<Entity> orders = entity.getHasManyField(ScheduleFields.POSITIONS).stream()
                .map(e -> e.getBelongsToField(SchedulePositionFields.ORDER)).distinct().collect(Collectors.toList());
        for (Entity order : orders) {
            if (!OrderStateStringValues.PENDING.equals(order.getStringField(OrderFields.STATE))
                    && !OrderStateStringValues.ACCEPTED.equals(order.getStringField(OrderFields.STATE))) {
                entity.addGlobalError("orders.schedule.orders.wrongState");
                break;
            }
        }
    }

    private void checkIfScheduleHasNotPositions(final Entity entity) {
        if (entity.getHasManyField(ScheduleFields.POSITIONS).isEmpty()) {
            entity.addGlobalError("orders.schedule.positions.isEmpty");
        }
    }

    private void checkExistingOperationalTasksState(Entity entity) {
        List<Entity> scheduleOrders = entity.getHasManyField(ScheduleFields.POSITIONS).stream()
                .map(e -> e.getBelongsToField(SchedulePositionFields.ORDER)).distinct().collect(Collectors.toList());
        if (!scheduleOrders.isEmpty()) {
            List<Entity> orders = getOperationalTaskDD().find()
                    .add(SearchRestrictions.ne(OperationalTaskFields.STATE, OperationalTaskStateStringValues.REJECTED))
                    .createAlias(OperationalTaskFields.ORDER, OperationalTaskFields.ORDER, JoinType.INNER)
                    .add(SearchRestrictions.in(OperationalTaskFields.ORDER + ".id",
                            scheduleOrders.stream().mapToLong(Entity::getId).boxed().collect(Collectors.toList())))
                    .setProjection(
                            list().add(alias(SearchProjections.groupField(OperationalTaskFields.ORDER + "." + OrderFields.NUMBER),
                                    OrderFields.NUMBER)))
                    .addOrder(desc(OperationalTaskFields.ORDER + "." + OrderFields.NUMBER)).list().getEntities();
            if (!orders.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (Entity order : orders) {
                    sb.append(order.getStringField(OrderFields.NUMBER));
                    sb.append(", ");
                }
                entity.addGlobalError("orders.schedule.operationalTasks.wrongState", false, true, sb.toString());
            }
        }
    }

    private void checkSchedulePositionsDatesIsNotEmpty(Entity entity) {
        for (Entity position : entity.getHasManyField(ScheduleFields.POSITIONS)) {
            if (position.getField(SchedulePositionFields.START_TIME) == null
                    || position.getField(SchedulePositionFields.END_TIME) == null) {
                entity.addGlobalError("orders.schedule.positions.datesIsEmpty");
                break;
            }
        }
    }

    @Override
    public Entity onBeforeSave(Entity entity, String sourceState, String targetState, Entity stateChangeEntity,
                               StateChangeEntityDescriber describer) {
        if (ScheduleStateStringValues.APPROVED.equals(targetState)) {
            entity.setField(ScheduleFields.APPROVE_TIME, stateChangeEntity.getDateField(ScheduleStateChangeFields.DATE_AND_TIME));
        }

        return entity;
    }

    @Override
    public Entity onAfterSave(Entity entity, String sourceState, String targetState, Entity stateChangeEntity,
                              StateChangeEntityDescriber describer) {
        switch (targetState) {
            case ScheduleStateStringValues.APPROVED:
                generateOperationalTasks(entity);
                updateOrderDates(entity);
                break;

            case ScheduleStateStringValues.REJECTED:
                if (ScheduleStateStringValues.APPROVED.equals(sourceState)) {
                    operationalTaskOrderStateService.rejectOperationalTasksForSchedule(entity);
                }
        }

        return entity;
    }

    private void updateOrderDates(Entity entity) {
        List<Entity> orders = entity.getHasManyField(ScheduleFields.POSITIONS).stream()
                .map(e -> e.getBelongsToField(SchedulePositionFields.ORDER)).distinct().collect(Collectors.toList());
        for (Entity order : orders) {
            Date startTime = getOrderStartTime(entity, order);
            Date endTime = getOrderEndTime(entity, order);
            Entity orderFromDB = order.getDataDefinition().get(order.getId());
            productionLineScheduleStateService.setOrderDateFrom(order, startTime, orderFromDB);
            productionLineScheduleStateService.setOrderDateTo(order, endTime, orderFromDB);
            order.getDataDefinition().fastSave(order);
        }
    }

    private Date getOrderEndTime(Entity entity, Entity order) {
        Entity schedulePositionMaxEndTimeEntity = dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SCHEDULE_POSITION).find()
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.SCHEDULE, entity))
                .setProjection(
                        list().add(alias(SearchProjections.max(SchedulePositionFields.END_TIME), SchedulePositionFields.END_TIME))
                                .add(rowCount()))
                .addOrder(SearchOrders.desc(SchedulePositionFields.END_TIME)).setMaxResults(1).uniqueResult();
        return schedulePositionMaxEndTimeEntity.getDateField(SchedulePositionFields.END_TIME);
    }

    private Date getOrderStartTime(Entity entity, Entity order) {
        Entity schedulePositionMinStartTimeEntity = dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SCHEDULE_POSITION).find()
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.SCHEDULE, entity))
                .setProjection(list()
                        .add(alias(SearchProjections.min(SchedulePositionFields.START_TIME), SchedulePositionFields.START_TIME))
                        .add(rowCount()))
                .addOrder(SearchOrders.asc(SchedulePositionFields.START_TIME)).setMaxResults(1).uniqueResult();
        return schedulePositionMinStartTimeEntity.getDateField(SchedulePositionFields.START_TIME);
    }

    private void generateOperationalTasks(Entity schedule) {
        DataDefinition operationalTaskDD = getOperationalTaskDD();
        DataDefinition workstationChangeoverForOperationalTaskDD = workstationChangeoverService.getWorkstationChangeoverForOperationalTaskDD();
        List<Entity> scheduleTasks = new ArrayList<>();
        for (Entity position : schedule.getHasManyField(ScheduleFields.POSITIONS).stream().sorted(Comparator.comparing(e -> e.getDateField(SchedulePositionFields.START_TIME))).collect(Collectors.toList())) {
            Entity operationalTask = operationalTaskDD.create();
            operationalTask.setField(OperationalTaskFields.START_DATE, position.getField(SchedulePositionFields.START_TIME));
            operationalTask.setField(OperationalTaskFields.FINISH_DATE, position.getField(SchedulePositionFields.END_TIME));
            operationalTask.setField(OperationalTaskFields.TYPE,
                    OperationalTaskType.EXECUTION_OPERATION_IN_ORDER.getStringValue());
            Entity order = position.getBelongsToField(SchedulePositionFields.ORDER);
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
            operationalTask.setField(OperationalTaskFields.ORDER, order);

            Entity technologyOperationComponent = position
                    .getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT);

            operationalTask.setField(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent);

            operationalTask.setField(OperationalTaskFields.WORKSTATION, position.getField(SchedulePositionFields.WORKSTATION));
            operationalTask.setField(OperationalTaskFields.STAFF, position.getField(SchedulePositionFields.STAFF));
            operationalTask.setField(OperationalTaskFields.PRODUCT, position.getField(SchedulePositionFields.PRODUCT));
            operationalTask.setField(OperationalTaskFields.SCHEDULE_POSITION, position);

            Optional<Entity> maybeDivision = technologyServiceO.extractDivision(technology, technologyOperationComponent);
            if (maybeDivision.isPresent()) {
                operationalTask.setField(OperationalTaskFields.DIVISION, maybeDivision.get());
            }
            operationalTaskHooks.setInitialState(operationalTask);
            operationalTaskHooks.fillNameAndDescription(operationalTask);
            operationalTaskHooksPS.setStaff(operationalTaskDD, operationalTask);
            operationalTask = operationalTaskDD.fastSave(operationalTask);
            scheduleTasks.add(operationalTask);
            List<Entity> workstationChangeovers = position.getHasManyField(SchedulePositionFields.CURRENT_WORKSTATION_CHANGEOVER_FOR_SCHEDULE_POSITIONS);
            boolean recalculateChangeovers = isRecalculateChangeovers(workstationChangeovers);
            if (recalculateChangeovers) {
                List<Entity> workstationChangeoverForOperationalTasks = workstationChangeoverService.findWorkstationChangeoverForOperationalTasks(operationalTask);
                for (Entity workstationChangeoverForOperationalTask : workstationChangeoverForOperationalTasks) {
                    workstationChangeoverForOperationalTaskDD.save(workstationChangeoverForOperationalTask);
                }
            } else {
                for (Entity workstationChangeover : workstationChangeovers) {
                    createWorkstationChangeoverForOperationalTask(operationalTask, workstationChangeover, workstationChangeoverForOperationalTaskDD);
                }
            }
        }
        recalculateTaskChangeovers(schedule, scheduleTasks, workstationChangeoverForOperationalTaskDD);
        schedule.addGlobalMessage("productionScheduling.operationDurationDetailsInOrder.info.operationalTasksCreated");
    }

    private void recalculateTaskChangeovers(Entity schedule, List<Entity> scheduleTasks,
                                            DataDefinition workstationChangeoverForOperationalTaskDD) {
        for (Entity scheduleTask : scheduleTasks) {
            Optional<Entity> nextOperationalTask = getNextOperationalTask(scheduleTask, schedule);
            nextOperationalTask.ifPresent(not -> {
                List<Entity> currentWorkstationChangeoverForOperationalTasks = not.getHasManyField(OperationalTaskFields.CURRENT_WORKSTATION_CHANGEOVER_FOR_OPERATIONAL_TASKS);
                currentWorkstationChangeoverForOperationalTasks.forEach(workstationChangeoverForOperationalTask ->
                        workstationChangeoverForOperationalTaskDD.delete(workstationChangeoverForOperationalTask.getId()));
                List<Entity> workstationChangeoverForOperationalTasks = workstationChangeoverService.findWorkstationChangeoverForOperationalTasks(not, scheduleTask);
                for (Entity workstationChangeoverForOperationalTask : workstationChangeoverForOperationalTasks) {
                    workstationChangeoverForOperationalTaskDD.save(workstationChangeoverForOperationalTask);
                }
            });
        }
    }

    private boolean isRecalculateChangeovers(List<Entity> workstationChangeovers) {
        boolean recalculateChangeovers = false;
        for (Entity workstationChangeover : workstationChangeovers) {
            Entity workstationChangeoverNorm = workstationChangeover.getBelongsToField(WorkstationChangeoverForSchedulePositionFields.WORKSTATION_CHANGEOVER_NORM);
            if (workstationChangeoverNorm == null) {
                recalculateChangeovers = true;
                break;
            }
        }
        return recalculateChangeovers;
    }

    private void createWorkstationChangeoverForOperationalTask(final Entity currentOperationalTask,
                                                               final Entity workstationChangeover,
                                                               DataDefinition dataDefinition) {
        Entity workstationChangeoverForOperationalTask = dataDefinition.create();

        Entity workstationChangeoverNorm = workstationChangeover.getBelongsToField(WorkstationChangeoverForSchedulePositionFields.WORKSTATION_CHANGEOVER_NORM);
        String name = workstationChangeoverNorm.getStringField(WorkstationChangeoverNormFields.NAME);
        String description = workstationChangeoverNorm.getStringField(WorkstationChangeoverNormFields.DESCRIPTION);
        Entity workstation = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.WORKSTATION);
        Entity attribute = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.ATTRIBUTE);
        Entity fromAttributeValue = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.FROM_ATTRIBUTE_VALUE);
        Entity toAttributeValue = workstationChangeoverNorm.getBelongsToField(WorkstationChangeoverNormFields.TO_ATTRIBUTE_VALUE);
        Integer duration = workstationChangeoverNorm.getIntegerField(WorkstationChangeoverNormFields.DURATION);
        boolean isParallel = workstationChangeoverNorm.getBooleanField(WorkstationChangeoverNormFields.IS_PARALLEL);

        Entity previousSchedulePosition = workstationChangeover.getBelongsToField(WorkstationChangeoverForSchedulePositionFields.PREVIOUS_SCHEDULE_POSITION);
        Entity previousOperationalTask;
        if (previousSchedulePosition != null) {
            previousOperationalTask = previousSchedulePosition.getHasManyField(SchedulePositionFields.OPERATIONAL_TASKS).get(0);
        } else {
            previousOperationalTask = workstationChangeover.getBelongsToField(WorkstationChangeoverForSchedulePositionFields.PREVIOUS_OPERATIONAL_TASK);
        }

        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.NAME, name);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.DESCRIPTION, description);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.WORKSTATION, workstation);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.ATTRIBUTE, attribute);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.FROM_ATTRIBUTE_VALUE, fromAttributeValue);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.TO_ATTRIBUTE_VALUE, toAttributeValue);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.WORKSTATION_CHANGEOVER_NORM, workstationChangeoverNorm);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.CURRENT_OPERATIONAL_TASK, currentOperationalTask);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.PREVIOUS_OPERATIONAL_TASK, previousOperationalTask);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.CHANGEOVER_TYPE, WorkstationChangeoverForOperationalTaskChangeoverType.BASED_ON_NORM.getStringValue());
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.DURATION, duration);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.IS_PARALLEL, isParallel);
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.START_DATE, workstationChangeover.getDateField(WorkstationChangeoverForSchedulePositionFields.START_DATE));
        workstationChangeoverForOperationalTask.setField(WorkstationChangeoverForOperationalTaskFields.FINISH_DATE, workstationChangeover.getDateField(WorkstationChangeoverForSchedulePositionFields.FINISH_DATE));

        dataDefinition.save(workstationChangeoverForOperationalTask);
    }

    private Optional<Entity> getNextOperationalTask(final Entity operationalTask, Entity schedule) {
        Entity workstation = operationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION);
        if (workstation == null) {
            return Optional.empty();
        }
        SearchCriteriaBuilder searchCriteriaBuilder = getOperationalTaskDD().find();

        Date endDate = operationalTask.getDateField(OperationalTaskFields.FINISH_DATE);

        addWorkstationAndDateSearchRestrictions(searchCriteriaBuilder, workstation, endDate);

        Entity nextOperationalTask = searchCriteriaBuilder.addOrder(SearchOrders.asc(OperationalTaskFields.START_DATE))
                .setMaxResults(1).uniqueResult();

        if (nextOperationalTask != null) {
            Entity schedulePosition = nextOperationalTask.getBelongsToField(OperationalTaskFields.SCHEDULE_POSITION);
            if (schedulePosition != null && schedulePosition.getBelongsToField(SchedulePositionFields.SCHEDULE).getId().equals(schedule.getId())) {
                nextOperationalTask = null;
            }
        }

        return Optional.ofNullable(nextOperationalTask);
    }

    private void addWorkstationAndDateSearchRestrictions(final SearchCriteriaBuilder searchCriteriaBuilder,
                                                         final Entity workstation, final Date endDate) {
        searchCriteriaBuilder.createAlias(OperationalTaskFields.WORKSTATION, OperationalTaskFields.WORKSTATION, JoinType.INNER);
        searchCriteriaBuilder.add(SearchRestrictions.eq(OperationalTaskFields.WORKSTATION + L_DOT + L_ID, workstation.getId()));
        searchCriteriaBuilder.add(SearchRestrictions.ne(OperationalTaskFields.STATE, OperationalTaskState.REJECTED.getStringValue()));
        searchCriteriaBuilder.add(SearchRestrictions.ge(OperationalTaskFields.START_DATE, endDate));
        searchCriteriaBuilder.add(SearchRestrictions.eq(OperationalTaskFields.TYPE, OperationalTaskType.EXECUTION_OPERATION_IN_ORDER.getStringValue()));
    }

    private DataDefinition getOperationalTaskDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK);
    }
}
