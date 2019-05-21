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
import com.qcadoo.mes.basic.constants.StaffSkillsFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ScheduleFields;
import com.qcadoo.mes.orders.constants.SchedulePositionFields;
import com.qcadoo.mes.orders.constants.ScheduleSortOrder;
import com.qcadoo.mes.orders.constants.ScheduleWorkerAssignCriterion;
import com.qcadoo.mes.orders.constants.ScheduleWorkstationAssignCriterion;
import com.qcadoo.mes.orders.states.ScheduleServiceMarker;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.OperationSkillFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchSubqueries;
import com.qcadoo.plugin.api.PluginManager;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ScheduleDetailsListeners {

    private static final String FINISH_DATE = "finishDate";

    private static final String OPERATIONAL_TASKS = "operationalTasks";

    private static final String OPERATIONAL_TASK = "operationalTask";

    private static final String REJECTED = "04rejected";

    private static final String STATE = "state";

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
            Integer machineWorkTime = position.getIntegerField(SchedulePositionFields.MACHINE_WORK_TIME);
            if (orderWithOperationWithoutWorkstations.contains(order.getId()) || machineWorkTime == 0) {
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

            getWorkstationsNewFinishDate(workstationsFinishDates, scheduleStartTime, position, order, machineWorkTime,
                    technologyOperationComponent, workstations, operationWorkstationsFinishDates);

            if (ScheduleWorkstationAssignCriterion.SHORTEST_TIME.getStringValue()
                    .equals(schedule.getStringField(ScheduleFields.WORKSTATION_ASSIGN_CRITERION))) {
                operationWorkstationsFinishDates.entrySet().stream().min(comparingByValue()).ifPresent(
                        entry -> updatePositionWorkstationAndDates(entry, workstationsFinishDates, position, workstations));
            } else {
                Entry<Long, Date> firstEntry;
                if (workstationsFinishDates.isEmpty()) {
                    firstEntry = operationWorkstationsFinishDates.entrySet().iterator().next();
                } else {
                    firstEntry = operationWorkstationsFinishDates.entrySet().stream()
                            .filter(entry -> workstationsFinishDates.containsKey(entry.getKey())).findFirst()
                            .orElse(operationWorkstationsFinishDates.entrySet().iterator().next());
                }
                updatePositionWorkstationAndDates(firstEntry, workstationsFinishDates, position, workstations);
            }
        }
    }

    private void getWorkstationsNewFinishDate(Map<Long, Date> workstationsFinishDates, Date scheduleStartTime, Entity position,
            Entity order, Integer machineWorkTime, Entity technologyOperationComponent, List<Entity> workstations,
            Map<Long, Date> operationWorkstationsFinishDates) {
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
                Date childEndTimeWithAdditionalTime = Date.from(child.getDateField(SchedulePositionFields.END_TIME).toInstant()
                        .plusSeconds(child.getIntegerField(SchedulePositionFields.ADDITIONAL_TIME)));
                if (childEndTimeWithAdditionalTime.after(finishDate)) {
                    finishDate = childEndTimeWithAdditionalTime;
                }
            }
            Date newFinishDate = Date.from(finishDate.toInstant().plusSeconds(machineWorkTime));
            operationWorkstationsFinishDates.put(workstation.getId(), newFinishDate);
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
        Entity operationalTasksMaxFinishDateEntity = dataDefinitionService.get(OPERATIONAL_TASKS, OPERATIONAL_TASK).find()
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.WORKSTATION, workstation))
                .add(SearchRestrictions.ne(STATE, REJECTED)).add(SearchRestrictions.gt(FINISH_DATE, scheduleStartTime))
                .setProjection(list().add(alias(SearchProjections.max(FINISH_DATE), FINISH_DATE)).add(rowCount()))
                .addOrder(SearchOrders.desc(FINISH_DATE)).setMaxResults(1).uniqueResult();
        return operationalTasksMaxFinishDateEntity.getDateField(FINISH_DATE);
    }

    private void updatePositionWorkstationAndDates(Entry<Long, Date> firstEntry, Map<Long, Date> workstationsFinishDates,
            Entity position, List<Entity> workstations) {
        workstationsFinishDates.put(firstEntry.getKey(), firstEntry.getValue());
        workstations.stream().filter(entity -> entity.getId().equals(firstEntry.getKey())).findFirst()
                .ifPresent(entity -> position.setField(SchedulePositionFields.WORKSTATION, entity));
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
            Entity workstation = position.getBelongsToField(SchedulePositionFields.WORKSTATION);
            if (machineWorkTime == 0 || workstation == null) {
                continue;
            }
            List<Entity> workers = getWorkers(position);
            Map<Long, Date> operationWorkersFinishDates = Maps.newHashMap();
            getWorkersNewFinishDate(workersFinishDates, scheduleStartTime, position, workers, operationWorkersFinishDates);
            if (workstationLastWorkers.get(workstation.getId()) == null) {
                workstationLastWorkers.put(workstation.getId(), getOperationalTasksLastWorkerForWorkstation(workstation));
            }
            Long workstationLastWorkerId = workstationLastWorkers.get(workstation.getId());
            Optional<Entry<Long, Date>> firstEntryOptional = operationWorkersFinishDates.entrySet().stream()
                    .filter(entry -> entry.getKey().equals(workstationLastWorkerId)).findFirst();

            if (!firstEntryOptional.isPresent()) {
                if (ScheduleWorkerAssignCriterion.WORKSTATION_LAST_OPERATOR_LATEST_FINISHED.getStringValue()
                        .equals(schedule.getStringField(ScheduleFields.WORKER_ASSIGN_CRITERION))) {
                    firstEntryOptional = operationWorkersFinishDates.entrySet().stream().max(comparingByValue());
                } else {
                    firstEntryOptional = operationWorkersFinishDates.entrySet().stream().min(comparingByValue());
                }
            }
            firstEntryOptional.ifPresent(firstEntry -> updatePositionWorker(workersFinishDates, workstationLastWorkers, position,
                    workstation, workers, firstEntry));
        }
    }

    private Long getOperationalTasksLastWorkerForWorkstation(Entity workstation) {
        Entity entity = dataDefinitionService.get(OPERATIONAL_TASKS, OPERATIONAL_TASK).find()
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.WORKSTATION, workstation))
                .add(SearchRestrictions.isNotNull(SchedulePositionFields.STAFF)).add(SearchRestrictions.ne(STATE, REJECTED))
                .addOrder(SearchOrders.desc(FINISH_DATE)).setMaxResults(1).uniqueResult();
        if (entity != null) {
            return entity.getBelongsToField(SchedulePositionFields.STAFF).getId();
        } else {
            return null;
        }
    }

    private List<Entity> getWorkers(Entity position) {
        List<Entity> operationSkills = position.getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT)
                .getBelongsToField(TechnologyOperationComponentFields.OPERATION)
                .getManyToManyField(OperationFields.OPERATION_SKILLS);
        SearchCriteriaBuilder staffScb = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_STAFF)
                .find();
        for (Entity operationSkill : operationSkills) {
            SearchCriteriaBuilder subCriteria = dataDefinitionService
                    .get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_STAFF_SKILL)
                    .findWithAlias(BasicConstants.MODEL_STAFF_SKILL)
                    .createAlias(StaffSkillsFields.STAFF, StaffSkillsFields.STAFF, JoinType.INNER)
                    .add(SearchRestrictions.eqField(StaffSkillsFields.STAFF + ".id", "this.id"))
                    .add(SearchRestrictions.belongsTo(StaffSkillsFields.SKILL,
                            operationSkill.getBelongsToField(OperationSkillFields.SKILL)))
                    .add(SearchRestrictions.ge(StaffSkillsFields.LEVEL,
                            operationSkill.getIntegerField(OperationSkillFields.REQUIRED_LEVEL)))
                    .setProjection(SearchProjections.id());
            staffScb.add(SearchSubqueries.exists(subCriteria));
        }
        return staffScb.list().getEntities();
    }

    private void getWorkersNewFinishDate(Map<Long, Date> workersFinishDates, Date scheduleStartTime, Entity position,
            List<Entity> workers, Map<Long, Date> operationWorkersFinishDates) {
        for (Entity worker : workers) {
            Date finishDate = workersFinishDates.get(worker.getId());
            if (finishDate == null && pluginManager.isPluginEnabled(OPERATIONAL_TASKS)) {
                Date operationalTasksMaxFinishDate = getOperationalTasksMaxFinishDateForWorker(scheduleStartTime, worker);
                if (operationalTasksMaxFinishDate != null) {
                    finishDate = operationalTasksMaxFinishDate;
                    workersFinishDates.put(worker.getId(), finishDate);
                }
            }
            if (finishDate == null) {
                finishDate = scheduleStartTime;
            }
            if (finishDate.compareTo(position.getDateField(SchedulePositionFields.START_TIME)) <= 0) {
                operationWorkersFinishDates.put(worker.getId(), position.getDateField(SchedulePositionFields.END_TIME));
            }
        }
    }

    private void updatePositionWorker(Map<Long, Date> workersFinishDates, Map<Long, Long> workstationLastWorkers, Entity position,
            Entity workstation, List<Entity> workers, Entry<Long, Date> firstEntry) {
        workersFinishDates.put(firstEntry.getKey(), firstEntry.getValue());
        workstationLastWorkers.put(workstation.getId(), firstEntry.getKey());
        workers.stream().filter(entity -> entity.getId().equals(firstEntry.getKey())).findFirst()
                .ifPresent(entity -> position.setField(SchedulePositionFields.STAFF, entity));
        position.getDataDefinition().save(position);
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
        Entity operationalTasksMaxFinishDateEntity = dataDefinitionService.get(OPERATIONAL_TASKS, OPERATIONAL_TASK).find()
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.STAFF, worker))
                .add(SearchRestrictions.ne(STATE, REJECTED)).add(SearchRestrictions.gt(FINISH_DATE, scheduleStartTime))
                .setProjection(list().add(alias(SearchProjections.max(FINISH_DATE), FINISH_DATE)).add(rowCount()))
                .addOrder(SearchOrders.desc(FINISH_DATE)).setMaxResults(1).uniqueResult();
        return operationalTasksMaxFinishDateEntity.getDateField(FINISH_DATE);
    }

    public void changeState(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        stateExecutorService.changeState(ScheduleServiceMarker.class, view, args);
    }
}
