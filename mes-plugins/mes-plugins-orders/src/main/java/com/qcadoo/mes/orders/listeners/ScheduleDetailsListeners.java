package com.qcadoo.mes.orders.listeners;

import static com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues.REJECTED;
import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.list;
import static com.qcadoo.model.api.search.SearchProjections.rowCount;
import static java.util.Map.Entry.comparingByValue;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.StaffSkillsFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ScheduleFields;
import com.qcadoo.mes.orders.constants.SchedulePositionFields;
import com.qcadoo.mes.orders.constants.ScheduleWorkerAssignCriterion;
import com.qcadoo.mes.orders.states.ScheduleServiceMarker;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.OperationSkillFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchSubqueries;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ScheduleDetailsListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private StateExecutorService stateExecutorService;

    @Transactional
    public void assignWorkersToOperations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Entity schedule = ((FormComponent) state).getEntity();
        String scheduleWorkerAssignCriterion = schedule.getStringField(ScheduleFields.WORKER_ASSIGN_CRITERION);
        Map<Long, Date> workersFinishDates = Maps.newHashMap();
        Map<Long, Long> workstationLastWorkers = Maps.newHashMap();
        List<Entity> positions = sortPositionsForWorkers(schedule.getId());
        Date scheduleStartTime = schedule.getDateField(ScheduleFields.START_TIME);
        for (Entity position : positions) {
            Entity workstation = position.getBelongsToField(SchedulePositionFields.WORKSTATION);
            Entity technologyOperationComponent = position
                    .getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT);
            if (position.getIntegerField(SchedulePositionFields.MACHINE_WORK_TIME) == 0 || workstation == null
                    || technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.OPTIMAL_STAFF) > 1) {
                continue;
            }
            if (ScheduleWorkerAssignCriterion.WORKSTATION_DEFAULT_OPERATOR.getStringValue()
                    .equals(scheduleWorkerAssignCriterion)) {
                position.setField(SchedulePositionFields.STAFF, workstation.getBelongsToField(WorkstationFields.STAFF));
            } else {
                List<Entity> workers = getWorkers(position);
                Map<Long, Date> operationWorkersFinishDates = Maps.newHashMap();
                getWorkersFinishDate(workersFinishDates, scheduleStartTime, position, workers, operationWorkersFinishDates);
                if (workstationLastWorkers.get(workstation.getId()) == null) {
                    workstationLastWorkers.put(workstation.getId(), getOperationalTasksLastWorkerForWorkstation(workstation));
                }
                Optional<Entry<Long, Date>> firstEntryOptional = getFirstEntryOptional(scheduleWorkerAssignCriterion,
                        workstationLastWorkers, workstation, operationWorkersFinishDates);
                position.setField(SchedulePositionFields.STAFF, null);
                firstEntryOptional.ifPresent(firstEntry -> updatePositionWorker(workersFinishDates, workstationLastWorkers,
                        position, workstation, firstEntry));
            }
            position.getDataDefinition().fastSave(position);
        }
    }

    private Optional<Entry<Long, Date>> getFirstEntryOptional(String scheduleWorkerAssignCriterion,
            Map<Long, Long> workstationLastWorkers, Entity workstation, Map<Long, Date> operationWorkersFinishDates) {
        Long workstationLastWorkerId = workstationLastWorkers.get(workstation.getId());
        Optional<Entry<Long, Date>> firstEntryOptional = operationWorkersFinishDates.entrySet().stream()
                .filter(entry -> entry.getKey().equals(workstationLastWorkerId)).findFirst();

        if (!firstEntryOptional.isPresent()) {
            if (ScheduleWorkerAssignCriterion.WORKSTATION_LAST_OPERATOR_LATEST_FINISHED.getStringValue()
                    .equals(scheduleWorkerAssignCriterion)) {
                firstEntryOptional = operationWorkersFinishDates.entrySet().stream().max(comparingByValue());
            } else {
                firstEntryOptional = operationWorkersFinishDates.entrySet().stream().min(comparingByValue());
            }
        }
        return firstEntryOptional;
    }

    private Long getOperationalTasksLastWorkerForWorkstation(Entity workstation) {
        Entity entity = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK)
                .find().add(SearchRestrictions.belongsTo(SchedulePositionFields.WORKSTATION, workstation))
                .add(SearchRestrictions.isNotNull(SchedulePositionFields.STAFF))
                .add(SearchRestrictions.ne(OperationalTaskFields.STATE, REJECTED))
                .addOrder(SearchOrders.desc(OperationalTaskFields.FINISH_DATE)).setMaxResults(1).uniqueResult();
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
        if (operationSkills.isEmpty()) {
            return Collections.emptyList();
        }
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

    private void getWorkersFinishDate(Map<Long, Date> workersFinishDates, Date scheduleStartTime, Entity position,
            List<Entity> workers, Map<Long, Date> operationWorkersFinishDates) {
        for (Entity worker : workers) {
            Date finishDate = workersFinishDates.get(worker.getId());
            if (finishDate == null) {
                Date operationalTasksMaxFinishDate = getOperationalTasksMaxFinishDateForWorker(worker);
                if (operationalTasksMaxFinishDate != null) {
                    finishDate = operationalTasksMaxFinishDate;
                    workersFinishDates.put(worker.getId(), finishDate);
                }
            }
            if (finishDate == null) {
                finishDate = scheduleStartTime;
            }
            if (finishDate.compareTo(position.getDateField(SchedulePositionFields.START_TIME)) <= 0) {
                operationWorkersFinishDates.put(worker.getId(), finishDate);
            }
        }
    }

    private void updatePositionWorker(Map<Long, Date> workersFinishDates, Map<Long, Long> workstationLastWorkers, Entity position,
            Entity workstation, Entry<Long, Date> firstEntry) {
        workersFinishDates.put(firstEntry.getKey(), position.getDateField(SchedulePositionFields.END_TIME));
        workstationLastWorkers.put(workstation.getId(), firstEntry.getKey());
        position.setField(SchedulePositionFields.STAFF, firstEntry.getKey());
    }

    private List<Entity> sortPositionsForWorkers(Long scheduleId) {
        Entity schedule = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SCHEDULE)
                .get(scheduleId);
        return schedule.getHasManyField(ScheduleFields.POSITIONS).find()
                .addOrder(SearchOrders.asc(SchedulePositionFields.START_TIME)).list().getEntities();

    }

    private Date getOperationalTasksMaxFinishDateForWorker(Entity worker) {
        Entity operationalTasksMaxFinishDateEntity = dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK).find()
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.STAFF, worker))
                .add(SearchRestrictions.ne(OperationalTaskFields.STATE, REJECTED))
                .setProjection(list()
                        .add(alias(SearchProjections.max(OperationalTaskFields.FINISH_DATE), OperationalTaskFields.FINISH_DATE))
                        .add(rowCount()))
                .addOrder(SearchOrders.desc(OperationalTaskFields.FINISH_DATE)).setMaxResults(1).uniqueResult();
        return operationalTasksMaxFinishDateEntity.getDateField(OperationalTaskFields.FINISH_DATE);
    }

    public void changeState(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        stateExecutorService.changeState(ScheduleServiceMarker.class, view, args);
    }
}
