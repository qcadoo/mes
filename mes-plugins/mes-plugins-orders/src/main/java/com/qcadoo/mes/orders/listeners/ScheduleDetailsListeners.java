package com.qcadoo.mes.orders.listeners;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.StaffSkillsFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.basic.constants.WorkstationTypeFields;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.constants.*;
import com.qcadoo.mes.orders.states.ScheduleServiceMarker;
import com.qcadoo.mes.productionLines.constants.WorkstationFieldsPL;
import com.qcadoo.mes.technologies.constants.AssignedToOperation;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.OperationSkillFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.*;
import com.qcadoo.plugin.api.PluginManager;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static com.qcadoo.model.api.search.SearchProjections.*;
import static java.util.Map.Entry.comparingByValue;

@Service
public class ScheduleDetailsListeners {

    private static final String FINISH_DATE = "finishDate";

    private static final String REJECTED = "04rejected";

    private static final String STATE = "state";

    private static final String SCHEDULE_ID = "scheduleId";

    private static final String ORDERS_FOR_SUBPRODUCTS_GENERATION = "ordersForSubproductsGeneration";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private StateExecutorService stateExecutorService;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private PluginManager pluginManager;

    @Transactional
    public void assignOperationsToWorkstations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Entity schedule = ((FormComponent) state).getEntity();
        Map<Long, Date> workstationsFinishDates = Maps.newHashMap();
        Set<Long> ordersToAvoid = Sets.newHashSet();
        List<Long> positionsIds = sortPositionsForWorkstations(schedule.getId());
        Date scheduleStartTime = schedule.getDateField(ScheduleFields.START_TIME);
        for (Long positionId : positionsIds) {
            Entity position = dataDefinitionService
                    .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SCHEDULE_POSITION).get(positionId);
            if (ordersToAvoid.contains(position.getBelongsToField(SchedulePositionFields.ORDER).getId())) {
                continue;
            }
            List<Entity> workstations = getWorkstationsFromTOC(position);
            if (workstations.isEmpty() || position.getIntegerField(SchedulePositionFields.MACHINE_WORK_TIME) == 0) {
                ordersToAvoid.add(position.getBelongsToField(SchedulePositionFields.ORDER).getId());
                continue;
            }
            Map<Long, Date> operationWorkstationsFinishDates = Maps.newHashMap();
            Map<Long, Date> operationWorkstationsStartDates = Maps.newHashMap();

            getWorkstationsNewFinishDate(workstationsFinishDates, scheduleStartTime, position, workstations,
                    operationWorkstationsFinishDates, operationWorkstationsStartDates);

            if (ScheduleWorkstationAssignCriterion.SHORTEST_TIME.getStringValue()
                    .equals(schedule.getStringField(ScheduleFields.WORKSTATION_ASSIGN_CRITERION))) {
                operationWorkstationsFinishDates.entrySet().stream().min(comparingByValue())
                        .ifPresent(entry -> updatePositionWorkstationAndDates(entry, workstationsFinishDates, position,
                                operationWorkstationsStartDates.get(entry.getKey())));
            } else {
                Entry<Long, Date> firstEntry;
                if (workstationsFinishDates.isEmpty()) {
                    firstEntry = operationWorkstationsFinishDates.entrySet().iterator().next();
                } else {
                    firstEntry = operationWorkstationsFinishDates.entrySet().stream()
                            .filter(entry -> workstationsFinishDates.containsKey(entry.getKey())).findFirst()
                            .orElse(operationWorkstationsFinishDates.entrySet().iterator().next());
                }
                updatePositionWorkstationAndDates(firstEntry, workstationsFinishDates, position,
                        operationWorkstationsStartDates.get(firstEntry.getKey()));
            }
        }
    }

    private List<Entity> getWorkstationsFromTOC(Entity position) {
        Entity technologyOperationComponent = position.getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT);
        List<Entity> workstations;
        if (AssignedToOperation.WORKSTATIONS.getStringValue()
                .equals(technologyOperationComponent.getStringField(TechnologyOperationComponentFields.ASSIGNED_TO_OPERATION))) {
            workstations = technologyOperationComponent.getManyToManyField(TechnologyOperationComponentFields.WORKSTATIONS);
        } else {
            Entity workstationType = technologyOperationComponent
                    .getBelongsToField(TechnologyOperationComponentFields.WORKSTATION_TYPE);
            if (workstationType == null) {
                workstations = Collections.emptyList();
            } else {
                workstations = workstationType.getHasManyField(WorkstationTypeFields.WORKSTATIONS);
            }
        }
        if (position.getBelongsToField(SchedulePositionFields.SCHEDULE).getBooleanField(ScheduleFields.SCHEDULE_FOR_BUFFER)) {
            List<Entity> bufferWorkstations = workstations.stream().filter(e -> e.getBooleanField(WorkstationFields.BUFFER))
                    .collect(Collectors.toList());
            if (!bufferWorkstations.isEmpty()) {
                return bufferWorkstations;
            }
        }
        return workstations;
    }

    private void getWorkstationsNewFinishDate(Map<Long, Date> workstationsFinishDates, Date scheduleStartTime, Entity position,
            List<Entity> workstations, Map<Long, Date> operationWorkstationsFinishDates,
            Map<Long, Date> operationWorkstationsStartDates) {
        Entity schedule = position.getBelongsToField(SchedulePositionFields.SCHEDULE);
        for (Entity workstation : workstations) {
            Date finishDate = getFinishDate(workstationsFinishDates, scheduleStartTime, schedule, workstation);
            List<Entity> children = getChildren(position.getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT),
                    position.getBelongsToField(SchedulePositionFields.ORDER), schedule);
            if (pluginManager.isPluginEnabled(ORDERS_FOR_SUBPRODUCTS_GENERATION)) {
                children.addAll(getOrderChildren(position.getBelongsToField(SchedulePositionFields.ORDER), schedule));
            }
            for (Entity child : children) {
                Date childEndTime = child.getDateField(SchedulePositionFields.END_TIME);
                if (schedule.getBooleanField(ScheduleFields.ADDITIONAL_TIME_EXTENDS_OPERATION)) {
                    childEndTime = Date.from(
                            childEndTime.toInstant().plusSeconds(child.getIntegerField(SchedulePositionFields.ADDITIONAL_TIME)));
                }
                if (childEndTime.after(finishDate)) {
                    finishDate = childEndTime;
                }
            }
            DateTime finishDateTime = new DateTime(finishDate);
            Date newStartDate = shiftsService
                    .getNearestWorkingDate(finishDateTime, workstation.getBelongsToField(WorkstationFieldsPL.PRODUCTION_LINE))
                    .orElse(finishDateTime).toDate();

            Integer machineWorkTime = position.getIntegerField(SchedulePositionFields.MACHINE_WORK_TIME);
            Date newFinishDate = shiftsService.findDateToForProductionLine(newStartDate, machineWorkTime,
                    workstation.getBelongsToField(WorkstationFieldsPL.PRODUCTION_LINE));
            operationWorkstationsStartDates.put(workstation.getId(), newStartDate);
            operationWorkstationsFinishDates.put(workstation.getId(), newFinishDate);
        }
    }

    private Date getFinishDate(Map<Long, Date> workstationsFinishDates, Date scheduleStartTime, Entity schedule,
            Entity workstation) {
        Date finishDate;
        if (schedule.getBooleanField(ScheduleFields.SCHEDULE_FOR_BUFFER)
                && workstation.getBooleanField(WorkstationFields.BUFFER)) {
            finishDate = scheduleStartTime;
        } else {
            finishDate = workstationsFinishDates.get(workstation.getId());
            if (finishDate == null) {
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
        }
        return finishDate;
    }

    private List<Entity> getChildren(Entity technologyOperationComponent, Entity order, Entity schedule) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SCHEDULE_POSITION).find()
                .createAlias(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT, "toc", JoinType.INNER)
                .add(SearchRestrictions.belongsTo("toc." + TechnologyOperationComponentFields.PARENT,
                        technologyOperationComponent))
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.SCHEDULE, schedule)).list().getEntities();
    }

    private List<Entity> getOrderChildren(Entity order, Entity schedule) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SCHEDULE_POSITION).find()
                .createAlias(SchedulePositionFields.ORDER, "o", JoinType.INNER)
                .add(SearchRestrictions.belongsTo("o.parent", order))
                .createAlias(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT, "toc", JoinType.INNER)
                .add(SearchRestrictions.isNull("toc." + TechnologyOperationComponentFields.PARENT))
                .add(SearchRestrictions.isNotNull(SchedulePositionFields.END_TIME))
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.SCHEDULE, schedule)).list().getEntities();
    }

    private Date getOperationalTasksMaxFinishDateForWorkstation(Date scheduleStartTime, Entity workstation) {
        Entity operationalTasksMaxFinishDateEntity = dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK).find()
                .add(SearchRestrictions.belongsTo(SchedulePositionFields.WORKSTATION, workstation))
                .add(SearchRestrictions.ne(STATE, REJECTED)).add(SearchRestrictions.gt(FINISH_DATE, scheduleStartTime))
                .setProjection(list().add(alias(SearchProjections.max(FINISH_DATE), FINISH_DATE)).add(rowCount()))
                .addOrder(SearchOrders.desc(FINISH_DATE)).setMaxResults(1).uniqueResult();
        return operationalTasksMaxFinishDateEntity.getDateField(FINISH_DATE);
    }

    private void updatePositionWorkstationAndDates(Entry<Long, Date> entry, Map<Long, Date> workstationsFinishDates,
            Entity position, Date startTime) {
        workstationsFinishDates.put(entry.getKey(), entry.getValue());
        position.setField(SchedulePositionFields.WORKSTATION, entry.getKey());
        position.setField(SchedulePositionFields.START_TIME, startTime);
        position.setField(SchedulePositionFields.END_TIME, entry.getValue());
        position.setField(SchedulePositionFields.STAFF, null);
        position.getDataDefinition().save(position);
    }

    private List<Long> sortPositionsForWorkstations(Long scheduleId) {
        Entity schedule = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SCHEDULE)
                .get(scheduleId);
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(SCHEDULE_ID, scheduleId);
        StringBuilder query = new StringBuilder();
        query.append("SELECT sp.id FROM orders_scheduleposition sp JOIN technologies_technologyoperationcomponent toc ");
        query.append("ON sp.technologyoperationcomponent_id = toc.id JOIN orders_order o ON sp.order_id = o.id ");
        query.append("WHERE sp.schedule_id = :scheduleId ORDER BY ");
        query.append("string_to_array(regexp_replace(SPLIT_PART(o.number, '-', 2), '[^0-9.]', '0', 'g'), '.')::int[] desc, ");
        query.append("string_to_array(regexp_replace(rtrim(toc.nodenumber, '.'), '[^0-9.]', '0', 'g'), '.')::int[] desc, ");
        if (ScheduleSortOrder.DESCENDING.getStringValue().equals(schedule.getStringField(ScheduleFields.SORT_ORDER))) {
            query.append("sp.machineworktime desc");
        } else {
            query.append("sp.machineworktime asc");
        }
        return jdbcTemplate.queryForList(query.toString(), parameters, Long.class);
    }

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
            if (position.getIntegerField(SchedulePositionFields.MACHINE_WORK_TIME) == 0 || workstation == null) {
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
            position.getDataDefinition().save(position);
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
                .add(SearchRestrictions.ne(STATE, REJECTED))
                .setProjection(list().add(alias(SearchProjections.max(FINISH_DATE), FINISH_DATE)).add(rowCount()))
                .addOrder(SearchOrders.desc(FINISH_DATE)).setMaxResults(1).uniqueResult();
        return operationalTasksMaxFinishDateEntity.getDateField(FINISH_DATE);
    }

    public void changeState(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        stateExecutorService.changeState(ScheduleServiceMarker.class, view, args);
    }
}
