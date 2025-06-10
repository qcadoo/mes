package com.qcadoo.mes.productionScheduling.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.*;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.orders.constants.*;
import com.qcadoo.mes.productionScheduling.states.ScheduleServiceMarker;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.*;
import com.qcadoo.mes.timeNormsForOperations.NormService;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.*;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues.REJECTED;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.NEXT_OPERATION_AFTER_PRODUCED_TYPE;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.SPECIFIED;
import static com.qcadoo.model.api.search.SearchProjections.*;
import static java.util.Map.Entry.comparingByValue;

@Service
public class ScheduleDetailsListenersPS {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleDetailsListenersPS.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private NormService normService;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private StateExecutorService stateExecutorService;

    @Transactional
    public void getOperations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        long start = System.currentTimeMillis();
        FormComponent formComponent = (FormComponent) state;
        Entity schedule = formComponent.getPersistedEntityWithIncludedFormValues();
        List<Entity> orders = schedule.getManyToManyField(ScheduleFields.ORDERS);
        if (orders.isEmpty()) {
            view.addMessage("productionScheduling.error.scheduleNoOrders", ComponentState.MessageType.INFO);
            return;
        }
        boolean includeTpz = schedule.getBooleanField(ScheduleFields.INCLUDE_TPZ);
        DataDefinition schedulePositionDD = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                OrdersConstants.MODEL_SCHEDULE_POSITION);
        List<Entity> positions = Lists.newArrayList();
        for (Entity order : orders) {
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
            if (technology == null) {
                continue;
            }

            List<Entity> operationComponents = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);
            for (Entity operationComponent : operationComponents) {
                BigDecimal operationComponentRuns = basicProductionCountingService.getOperationComponentRuns(order, operationComponent);
                BigDecimal staffFactor = normService.getStaffFactor(operationComponent, operationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.OPTIMAL_STAFF));
                OperationWorkTime operationWorkTime = operationWorkTimeService.estimateOperationWorkTime(null, operationComponent,
                        operationComponentRuns, includeTpz, false, false, staffFactor);
                Entity schedulePosition = createSchedulePosition(schedule, schedulePositionDD, order, operationComponent,
                        operationWorkTime, operationComponentRuns);
                positions.add(schedulePosition);
            }
        }

        schedule.setField(ScheduleFields.POSITIONS, positions);
        schedule.setField(ScheduleFields.INCLUDE_TPZ_CHANGED, false);
        schedule = schedule.getDataDefinition().save(schedule);
        formComponent.setEntity(schedule);
        view.addMessage("productionScheduling.info.schedulePositionsGenerated", ComponentState.MessageType.SUCCESS);
        long finish = System.currentTimeMillis();
        LOG.info("Plan for shift {} - get operations: {}s.", schedule.getStringField(ScheduleFields.NUMBER), (finish - start) / 1000);
    }

    private Entity createSchedulePosition(Entity schedule, DataDefinition schedulePositionDD, Entity order,
                                          Entity technologyOperationComponent, OperationWorkTime operationWorkTime,
                                          BigDecimal operationComponentRuns) {
        BigDecimal partialOperationComponentRuns = operationComponentRuns;
        Entity outputProduct = technologyService.getMainOutputProductComponent(technologyOperationComponent);
        Entity product = outputProduct.getBelongsToField(OperationProductOutComponentFields.PRODUCT);
        if (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue().equals(product.getField(ProductFields.ENTITY_TYPE))) {
            product = order.getBelongsToField(OrderFields.PRODUCT);
        }
        BigDecimal productComponentQuantity = basicProductionCountingService.getProductPlannedQuantity(order, technologyOperationComponent, product);
        if (SPECIFIED.equals(technologyOperationComponent.getStringField(NEXT_OPERATION_AFTER_PRODUCED_TYPE))) {
            partialOperationComponentRuns = operationWorkTimeService.getQuantityCyclesNeededToProducedNextOperationAfterProducedQuantity(technologyOperationComponent,
                    operationComponentRuns, productComponentQuantity, outputProduct);
        }
        Entity schedulePosition = schedulePositionDD.create();
        schedulePosition.setField(SchedulePositionFields.SCHEDULE, schedule);
        schedulePosition.setField(SchedulePositionFields.ORDER, order);
        schedulePosition.setField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent);
        schedulePosition.setField(SchedulePositionFields.PRODUCT, product);
        schedulePosition.setField(SchedulePositionFields.QUANTITY, productComponentQuantity);
        schedulePosition.setField(SchedulePositionFields.ADDITIONAL_TIME,
                technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.TIME_NEXT_OPERATION));
        schedulePosition.setField(SchedulePositionFields.LABOR_WORK_TIME, operationWorkTime.getLaborWorkTime());
        schedulePosition.setField(SchedulePositionFields.MACHINE_WORK_TIME, operationWorkTime.getMachineWorkTime());
        schedulePosition.setField(SchedulePositionFields.OPERATION_RUNS, operationComponentRuns);
        schedulePosition.setField(SchedulePositionFields.PARTIAL_OPERATION_RUNS, partialOperationComponentRuns);
        return schedulePosition;
    }

    @Transactional
    public void assignWorkersToOperations(final ViewDefinitionState view, final ComponentState state,
                                          final String[] args) {
        long start = System.currentTimeMillis();
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
                Optional<Map.Entry<Long, Date>> firstEntryOptional = getFirstEntryOptional(scheduleWorkerAssignCriterion,
                        workstationLastWorkers, workstation, operationWorkersFinishDates);
                position.setField(SchedulePositionFields.STAFF, null);
                firstEntryOptional.ifPresent(firstEntry -> updatePositionWorker(workersFinishDates, workstationLastWorkers,
                        position, workstation, firstEntry));
            }
            position.getDataDefinition().fastSave(position);
        }
        long finish = System.currentTimeMillis();
        LOG.info("Plan for shift {} - workers assignment: {}s.", schedule.getStringField(ScheduleFields.NUMBER), (finish - start) / 1000);
    }

    private Optional<Map.Entry<Long, Date>> getFirstEntryOptional(String scheduleWorkerAssignCriterion,
                                                                  Map<Long, Long> workstationLastWorkers,
                                                                  Entity workstation,
                                                                  Map<Long, Date> operationWorkersFinishDates) {
        Long workstationLastWorkerId = workstationLastWorkers.get(workstation.getId());
        Optional<Map.Entry<Long, Date>> firstEntryOptional = operationWorkersFinishDates.entrySet().stream()
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

    private void updatePositionWorker(Map<Long, Date> workersFinishDates, Map<Long, Long> workstationLastWorkers,
                                      Entity position,
                                      Entity workstation, Map.Entry<Long, Date> firstEntry) {
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
