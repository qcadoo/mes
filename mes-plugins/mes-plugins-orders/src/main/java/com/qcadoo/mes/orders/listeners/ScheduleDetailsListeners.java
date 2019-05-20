package com.qcadoo.mes.orders.listeners;

import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.list;
import static com.qcadoo.model.api.search.SearchProjections.rowCount;
import static java.util.Map.Entry.comparingByValue;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ScheduleFields;
import com.qcadoo.mes.orders.constants.SchedulePositionFields;
import com.qcadoo.mes.orders.constants.ScheduleSortOrder;
import com.qcadoo.mes.orders.constants.ScheduleWorkstationAssignCriterion;
import com.qcadoo.mes.orders.states.ScheduleServiceMarker;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginManager;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ScheduleDetailsListeners {

    private static final String FINISH_DATE = "finishDate";

    public static final String OPERATIONAL_TASKS = "operationalTasks";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private StateExecutorService stateExecutorService;

    public void assignOperationsToWorkstations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Entity schedule = ((FormComponent) state).getEntity();
        Map<Long, Date> workstationsFinishDates = Maps.newHashMap();
        Set<Long> orderWithOperationWithoutWorkstations = Sets.newHashSet();
        List<Entity> positions = sortPositionsForWorkstations(schedule.getId());
        Date scheduleStartTime = schedule.getDateField(ScheduleFields.START_TIME);
        for (Entity position : positions) {
            Entity order = position.getBelongsToField(SchedulePositionFields.ORDER);
            if (orderWithOperationWithoutWorkstations.contains(order.getId())) {
                continue;
            }
            Integer machineWorkTime = position.getIntegerField(SchedulePositionFields.MACHINE_WORK_TIME);
            if (machineWorkTime == 0) {
                continue;
            }
            Entity technologyOperationComponent = position
                    .getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT);
            List<Entity> workstations = technologyOperationComponent
                    .getManyToManyField(TechnologyOperationComponentFields.WORKSTATIONS);
            if (workstations.isEmpty()) {
                orderWithOperationWithoutWorkstations.add(order.getId());
                continue;
            }
            Map<Long, Date> operationWorkstationsFinishDates = Maps.newHashMap();

            for (Entity workstation : workstations) {
                Date finishDate = workstationsFinishDates.get(workstation.getId());
                if (finishDate == null && pluginManager.isPluginEnabled(OPERATIONAL_TASKS)) {
                    Date operationalTasksMaxFinishDate = getOperationalTasksMaxFinishDateForWorkstation(scheduleStartTime,
                            workstation);
                    if (operationalTasksMaxFinishDate != null) {
                        finishDate = operationalTasksMaxFinishDate;
                        workstationsFinishDates.put(workstation.getId(), finishDate);
                    }
                }
                if (finishDate == null) {
                    finishDate = scheduleStartTime;
                }
                List<Entity> children = getChildren(technologyOperationComponent, order,
                        position.getBelongsToField(SchedulePositionFields.SCHEDULE));
                for (Entity child : children) {
                    Date childEndTimeWithAdditionalTime = Date.from(child.getDateField(SchedulePositionFields.END_TIME)
                            .toInstant().plusSeconds(child.getIntegerField(SchedulePositionFields.ADDITIONAL_TIME)));
                    if (childEndTimeWithAdditionalTime.after(finishDate)) {
                        finishDate = childEndTimeWithAdditionalTime;
                    }
                }
                Date newFinishDate = Date.from(finishDate.toInstant().plusSeconds(machineWorkTime));
                operationWorkstationsFinishDates.put(workstation.getId(), newFinishDate);
            }

            Entry<Long, Date> firstEntry;
            if (ScheduleWorkstationAssignCriterion.SHORTEST_TIME.getStringValue()
                    .equals(schedule.getStringField(ScheduleFields.WORKSTATION_ASSIGN_CRITERION))) {
                firstEntry = operationWorkstationsFinishDates.entrySet().stream().min(comparingByValue()).get();
            } else {
                if (workstationsFinishDates.isEmpty()) {
                    firstEntry = operationWorkstationsFinishDates.entrySet().iterator().next();
                } else {
                    firstEntry = operationWorkstationsFinishDates.entrySet().stream()
                            .filter(entry -> workstationsFinishDates.containsKey(entry.getKey())).findFirst()
                            .orElse(operationWorkstationsFinishDates.entrySet().iterator().next());
                }
            }
            updatePositionWorkstationAndDates(firstEntry, workstationsFinishDates, position, workstations);
        }
    }

    private List<Entity> getChildren(Entity technologyOperationComponent, Entity order, Entity schedule) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SCHEDULE_POSITION).find()
                .createAlias(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT, "toc", JoinType.INNER)
                .add(SearchRestrictions.belongsTo("toc." + TechnologyOperationComponentFields.PARENT,
                        technologyOperationComponent))
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.SCHEDULE, schedule)).list().getEntities();
    }

    private Date getOperationalTasksMaxFinishDateForWorkstation(Date scheduleStartTime, Entity workstation) {
        Entity operationalTasksMaxFinishDateEntity = dataDefinitionService.get(OPERATIONAL_TASKS, "operationalTask").find()
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.WORKSTATION, workstation))
                .add(SearchRestrictions.gt(FINISH_DATE, scheduleStartTime))
                .setProjection(list().add(alias(SearchProjections.max(FINISH_DATE), FINISH_DATE)).add(rowCount()))
                .addOrder(SearchOrders.desc(FINISH_DATE)).setMaxResults(1).uniqueResult();
        return operationalTasksMaxFinishDateEntity.getDateField(FINISH_DATE);
    }

    private void updatePositionWorkstationAndDates(Entry<Long, Date> firstEntry, Map<Long, Date> workstationsFinishDates,
            Entity position, List<Entity> workstations) {
        workstationsFinishDates.put(firstEntry.getKey(), firstEntry.getValue());
        position.setField(SchedulePositionFields.WORKSTATION,
                workstations.stream().filter(entity -> entity.getId().equals(firstEntry.getKey())).findFirst().get());
        position.setField(SchedulePositionFields.START_TIME, Date.from(firstEntry.getValue().toInstant()
                .minusSeconds(position.getIntegerField(SchedulePositionFields.MACHINE_WORK_TIME))));
        position.setField(SchedulePositionFields.END_TIME, firstEntry.getValue());
        position.setField(SchedulePositionFields.STAFF, null);
        position.getDataDefinition().save(position);
    }

    private List<Entity> sortPositionsForWorkstations(Long scheduleId) {
        Entity schedule = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SCHEDULE)
                .get(scheduleId);
        if (ScheduleSortOrder.DESCENDING.getStringValue().equals(schedule.getStringField(ScheduleFields.SORT_ORDER))) {
            return schedule.getHasManyField(ScheduleFields.POSITIONS).find()
                    .createAlias(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT,
                            SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT, JoinType.INNER)
                    .addOrder(SearchOrders.desc(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT + "."
                            + TechnologyOperationComponentFields.NODE_NUMBER))
                    .addOrder(SearchOrders.desc(SchedulePositionFields.MACHINE_WORK_TIME)).list().getEntities();
        } else {
            return schedule.getHasManyField(ScheduleFields.POSITIONS).find()
                    .createAlias(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT,
                            SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT, JoinType.INNER)
                    .addOrder(SearchOrders.desc(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT + "."
                            + TechnologyOperationComponentFields.NODE_NUMBER))
                    .addOrder(SearchOrders.asc(SchedulePositionFields.MACHINE_WORK_TIME)).list().getEntities();
        }
    }

    public void assignWorkersToOperations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Entity schedule = ((FormComponent) state).getEntity();
        Map<Long, Date> workersFinishDates = Maps.newHashMap();
        Map<Long, Long> workstationLastWorkers = Maps.newHashMap();
        List<Entity> positions = sortPositionsForWorkers(schedule.getId());
        Date scheduleStartTime = schedule.getDateField(ScheduleFields.START_TIME);
        for (Entity position : positions) {
            Integer machineWorkTime = position.getIntegerField(SchedulePositionFields.MACHINE_WORK_TIME);
            if (machineWorkTime == 0) {
                continue;
            }
            Entity workstation = position.getBelongsToField(SchedulePositionFields.WORKSTATION);
            if (workstation == null) {
                continue;
            }
            List<Entity> operationSkills = position.getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT)
                    .getBelongsToField(TechnologyOperationComponentFields.OPERATION)
                    .getManyToManyField(OperationFields.OPERATION_SKILLS);
            SearchCriteriaBuilder staffScb = dataDefinitionService
                    .get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_STAFF).find();
            for (Entity operationSkill : operationSkills) {
                // staffScb.add(SearchRestrictions.belongsTo(StaffFields))
            }
            List<Entity> workers = staffScb.list().getEntities();
            Map<Long, Date> operationWorkersFinishDates = Maps.newHashMap();
            for (Entity worker : workers) {
                Date finishDate = workersFinishDates.get(worker.getId());
                if (finishDate == null && pluginManager.isPluginEnabled(OPERATIONAL_TASKS)) {
                    Date operationalTasksMaxFinishDate = getOperationalTasksMaxFinishDateForWorker(scheduleStartTime, worker);
                    if (operationalTasksMaxFinishDate != null) {
                        finishDate = operationalTasksMaxFinishDate;
                    }
                }
                if (finishDate == null) {
                    finishDate = scheduleStartTime;
                }
                Date positionStartTime = position.getDateField(SchedulePositionFields.START_TIME);
                if (finishDate.compareTo(positionStartTime) <= 0) {
                    Date newFinishDate = Date.from(positionStartTime.toInstant().plusSeconds(machineWorkTime));
                    operationWorkersFinishDates.put(worker.getId(), newFinishDate);
                }
            }
            Optional<Entry<Long, Date>> firstEntryOptional = operationWorkersFinishDates.entrySet().stream()
                    .filter(entry -> entry.getKey().equals(workstationLastWorkers.get(workstation.getId()))).findFirst();

            if (!firstEntryOptional.isPresent()) {
                firstEntryOptional = operationWorkersFinishDates.entrySet().stream().max(comparingByValue());
            }
            firstEntryOptional.ifPresent(firstEntry -> {
                workersFinishDates.put(firstEntry.getKey(), firstEntry.getValue());
                workstationLastWorkers.put(workstation.getId(), firstEntry.getKey());
                position.setField(SchedulePositionFields.STAFF,
                        workers.stream().filter(entity -> entity.getId().equals(firstEntry.getKey())).findFirst().get());
                position.getDataDefinition().save(position);
            });
        }
    }

    private List<Entity> sortPositionsForWorkers(Long scheduleId) {
        Entity schedule = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SCHEDULE)
                .get(scheduleId);
        return schedule.getHasManyField(ScheduleFields.POSITIONS).find()
                .createAlias(SchedulePositionFields.WORKSTATION, SchedulePositionFields.WORKSTATION, JoinType.INNER)
                .addOrder(SearchOrders.asc(SchedulePositionFields.WORKSTATION + "." + WorkstationFields.NUMBER))
                .addOrder(SearchOrders.asc(SchedulePositionFields.START_TIME)).list().getEntities();

    }

    private Date getOperationalTasksMaxFinishDateForWorker(Date scheduleStartTime, Entity worker) {
        Entity operationalTasksMaxFinishDateEntity = dataDefinitionService.get(OPERATIONAL_TASKS, "operationalTask").find()
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.STAFF, worker))
                .add(SearchRestrictions.gt(FINISH_DATE, scheduleStartTime))
                .setProjection(list().add(alias(SearchProjections.max(FINISH_DATE), FINISH_DATE)).add(rowCount()))
                .addOrder(SearchOrders.desc(FINISH_DATE)).setMaxResults(1).uniqueResult();
        return operationalTasksMaxFinishDateEntity.getDateField(FINISH_DATE);
    }

    public void changeState(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        stateExecutorService.changeState(ScheduleServiceMarker.class, view, args);
    }
}
