package com.qcadoo.mes.productionScheduling.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.basic.constants.*;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.orders.constants.*;
import com.qcadoo.mes.orders.services.WorkstationChangeoverService;
import com.qcadoo.mes.orders.validators.SchedulePositionValidators;
import com.qcadoo.mes.productionLines.constants.WorkstationFieldsPL;
import com.qcadoo.mes.productionScheduling.states.ScheduleServiceMarker;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.*;
import com.qcadoo.mes.timeNormsForOperations.NormService;
import com.qcadoo.mes.timeNormsForOperations.constants.TechOperCompWorkstationTimeFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.*;
import com.qcadoo.plugin.api.PluginManager;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues.FINISHED;
import static com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues.REJECTED;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.NEXT_OPERATION_AFTER_PRODUCED_TYPE;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO.SPECIFIED;
import static com.qcadoo.model.api.search.SearchProjections.*;
import static java.util.Map.Entry.comparingByValue;

@Service
public class ScheduleDetailsListenersPS {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleDetailsListenersPS.class);

    private static final String ORDERS_FOR_SUBPRODUCTS_GENERATION = "ordersForSubproductsGeneration";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private SchedulePositionValidators schedulePositionValidators;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NormService normService;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private StateExecutorService stateExecutorService;

    @Autowired
    private WorkstationChangeoverService workstationChangeoverService;

    @Transactional
    public void generatePlan(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        getOperations(view, state, args);
        assignOperationsToWorkstations(view, state, args);
        assignWorkersToOperations(view, state, args);
    }

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
    public void assignOperationsToWorkstations(final ViewDefinitionState view, final ComponentState state,
                                               final String[] args) {
        long startAll = System.currentTimeMillis();
        Entity schedule = ((FormComponent) state).getEntity();
        Map<Long, Date> workstationsFinishDates = Maps.newHashMap();
        Map<Long, Entity> workstationsPositions = Maps.newHashMap();
        Set<Long> ordersToAvoid = Sets.newHashSet();
        List<Long> positionsIds = sortPositionsForWorkstations(schedule.getId());
        Date scheduleStartTime = schedule.getDateField(ScheduleFields.START_TIME);
        for (Long positionId : positionsIds) {
            long start = System.currentTimeMillis();
            Entity position = dataDefinitionService
                    .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SCHEDULE_POSITION).get(positionId);
            Entity order = position.getBelongsToField(SchedulePositionFields.ORDER);
            if (ordersToAvoid.contains(order.getId())) {
                continue;
            }
            List<Entity> workstations = schedulePositionValidators.getWorkstationsFromTOC(schedule, position.getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT), order);
            if (workstations.isEmpty()) {
                ordersToAvoid.add(order.getId());
                continue;
            }
            Map<Long, PositionNewData> operationWorkstationsPositionNewData = Maps.newHashMap();

            boolean allMachineWorkTimesEqualsZero = getWorkstationsNewFinishDate(workstationsFinishDates, scheduleStartTime,
                    position, workstations, operationWorkstationsPositionNewData, workstationsPositions);

            if (allMachineWorkTimesEqualsZero) {
                ordersToAvoid.add(order.getId());
                continue;
            }

            if (ScheduleWorkstationAssignCriterion.SHORTEST_TIME.getStringValue()
                    .equals(schedule.getStringField(ScheduleFields.WORKSTATION_ASSIGN_CRITERION))) {
                operationWorkstationsPositionNewData.entrySet().stream()
                        .min(Comparator.comparing(e -> e.getValue().getFinishDate()))
                        .ifPresent(entry -> updatePositionWorkstationAndDates(entry, workstationsFinishDates, position, workstationsPositions));
            } else {
                Map.Entry<Long, PositionNewData> firstEntry;
                if (workstationsFinishDates.isEmpty()) {
                    firstEntry = operationWorkstationsPositionNewData.entrySet().iterator().next();
                } else {
                    firstEntry = operationWorkstationsPositionNewData.entrySet().stream()
                            .filter(entry -> workstationsFinishDates.containsKey(entry.getKey())).findFirst()
                            .orElse(operationWorkstationsPositionNewData.entrySet().iterator().next());
                }
                updatePositionWorkstationAndDates(firstEntry, workstationsFinishDates, position, workstationsPositions);
            }
            long finish = System.currentTimeMillis();
            LOG.info("Plan for shift - workstation assignment: {}s.", (finish - start) / 1000);
        }
        long finishAll = System.currentTimeMillis();
        LOG.info("Plan for shift {} - workstations assignment: {}s.", schedule.getStringField(ScheduleFields.NUMBER), (finishAll - startAll) / 1000);
    }

    private boolean getWorkstationsNewFinishDate(Map<Long, Date> workstationsFinishDates, Date scheduleStartTime,
                                                 Entity position,
                                                 List<Entity> workstations,
                                                 Map<Long, PositionNewData> operationWorkstationsPositionNewData,
                                                 Map<Long, Entity> workstationsPositions) {
        Entity schedule = position.getBelongsToField(SchedulePositionFields.SCHEDULE);
        Entity technologyOperationComponent = position.getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT);
        BigDecimal staffFactor = normService.getStaffFactor(technologyOperationComponent, technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.OPTIMAL_STAFF));
        boolean allMachineWorkTimesEqualsZero = true;
        for (Entity workstation : workstations) {
            Integer laborWorkTime = position.getIntegerField(SchedulePositionFields.LABOR_WORK_TIME);
            Integer machineWorkTime = position.getIntegerField(SchedulePositionFields.MACHINE_WORK_TIME);
            Integer additionalTime = position.getIntegerField(SchedulePositionFields.ADDITIONAL_TIME);
            Optional<Entity> techOperCompWorkstationTime = normService.getTechOperCompWorkstationTime(technologyOperationComponent, workstation);
            if (techOperCompWorkstationTime.isPresent()) {
                OperationWorkTime operationWorkTime = operationWorkTimeService.estimateTechOperationWorkTimeForWorkstation(
                        technologyOperationComponent,
                        position.getDecimalField(SchedulePositionFields.OPERATION_RUNS),
                        schedule.getBooleanField(ScheduleFields.INCLUDE_TPZ), false, techOperCompWorkstationTime.get(),
                        staffFactor);
                laborWorkTime = operationWorkTime.getLaborWorkTime();
                machineWorkTime = operationWorkTime.getMachineWorkTime();
                additionalTime = techOperCompWorkstationTime.get()
                        .getIntegerField(TechOperCompWorkstationTimeFields.TIME_NEXT_OPERATION);
            }
            if (machineWorkTime == 0) {
                continue;
            } else {
                allMachineWorkTimesEqualsZero = false;
            }
            Date finishDate = getFinishDate(workstationsFinishDates, scheduleStartTime, schedule, workstation);
            finishDate = getFinishDateWithChildren(position, finishDate);
            Entity previousPosition = workstationsPositions.get(workstation.getId());
            List<Entity> workstationChangeovers = workstationChangeoverService.findWorkstationChangeoversForSchedulePosition(finishDate, workstation, position, previousPosition);
            finishDate = getFinisDateWithChangeovers(finishDate, workstationChangeovers);
            DateTime finishDateTime = new DateTime(finishDate);
            Entity productionLine = workstation.getBelongsToField(WorkstationFieldsPL.PRODUCTION_LINE);
            Date newStartDate = shiftsService
                    .getNearestWorkingDate(finishDateTime, productionLine).orElse(finishDateTime).toDate();

            Date newFinishDate = shiftsService.findDateToForProductionLine(newStartDate, machineWorkTime, productionLine);
            if (schedule.getBooleanField(ScheduleFields.ADDITIONAL_TIME_EXTENDS_OPERATION)) {
                newFinishDate = Date.from(newFinishDate.toInstant().plusSeconds(additionalTime));
            }
            Date childrenEndTime = schedulePositionValidators.getChildrenMaxEndTime(position);
            if (!Objects.isNull(childrenEndTime) && childrenEndTime.after(newFinishDate)) {
                newFinishDate = childrenEndTime;
            }
            PositionNewData positionNewData = new PositionNewData(laborWorkTime, machineWorkTime, additionalTime, newStartDate,
                    newFinishDate, workstationChangeovers);
            operationWorkstationsPositionNewData.put(workstation.getId(), positionNewData);
        }
        return allMachineWorkTimesEqualsZero;
    }

    private Date getFinisDateWithChangeovers(Date finishDate, List<Entity> workstationChangeovers) {
        if (!workstationChangeovers.isEmpty()) {
            Optional<Date> mayBeMaxFinishDate = workstationChangeoverService.getWorkstationChangeoversMaxFinishDate(workstationChangeovers);
            if (mayBeMaxFinishDate.isPresent()) {
                finishDate = mayBeMaxFinishDate.get();
            }
        }
        return finishDate;
    }

    private Date getFinishDateWithChildren(Entity position, Date finishDate) {
        Date childrenEndTime = getChildrenMaxEndTime(position);
        if (!Objects.isNull(childrenEndTime) && childrenEndTime.after(finishDate)) {
            finishDate = childrenEndTime;
        }
        if (pluginManager.isPluginEnabled(ORDERS_FOR_SUBPRODUCTS_GENERATION)) {
            childrenEndTime = schedulePositionValidators.getOrdersChildrenMaxEndTime(position);
            if (!Objects.isNull(childrenEndTime) && childrenEndTime.after(finishDate)) {
                finishDate = childrenEndTime;
            }
        }
        return finishDate;
    }

    private Date getChildrenMaxEndTime(Entity position) {
        Date childrenEndTime = null;
        Entity schedule = position.getBelongsToField(SchedulePositionFields.SCHEDULE);
        boolean includeTpz = schedule.getBooleanField(ScheduleFields.INCLUDE_TPZ);
        List<Entity> children = schedule.getHasManyField(ScheduleFields.POSITIONS).stream()
                .filter(e -> e.getBelongsToField(SchedulePositionFields.ORDER).getId().equals(position.getBelongsToField(SchedulePositionFields.ORDER).getId())
                        && e.getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT).getBelongsToField(TechnologyOperationComponentFields.PARENT) != null
                        && e.getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT).getBelongsToField(TechnologyOperationComponentFields.PARENT).getId()
                        .equals(position.getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT).getId())).collect(Collectors.toList());
        for (Entity child : children) {
            Entity operationComponent = child.getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT);
            Date childEndTime;
            if (SPECIFIED.equals(operationComponent.getStringField(NEXT_OPERATION_AFTER_PRODUCED_TYPE))) {
                Entity workstation = child.getBelongsToField(SchedulePositionFields.WORKSTATION);
                Entity productionLine = workstation.getBelongsToField(WorkstationFieldsPL.PRODUCTION_LINE);
                Integer machineWorkTime = getMachineWorkTime(includeTpz, child, operationComponent, workstation);

                childEndTime = shiftsService.findDateToForProductionLine(child.getDateField(SchedulePositionFields.START_TIME), machineWorkTime, productionLine);
            } else {
                if (!schedule.getBooleanField(ScheduleFields.ADDITIONAL_TIME_EXTENDS_OPERATION)) {
                    childEndTime = Date.from(child.getDateField(SchedulePositionFields.END_TIME).toInstant().plusSeconds(child.getIntegerField(SchedulePositionFields.ADDITIONAL_TIME)));
                } else {
                    childEndTime = child.getDateField(SchedulePositionFields.END_TIME);
                }
            }
            if (childrenEndTime == null || childEndTime.after(childrenEndTime)) {
                childrenEndTime = childEndTime;
            }
        }
        return childrenEndTime;
    }

    private Integer getMachineWorkTime(boolean includeTpz, Entity child, Entity operationComponent,
                                       Entity workstation) {
        BigDecimal partialOperationComponentRuns = child.getDecimalField(SchedulePositionFields.PARTIAL_OPERATION_RUNS);
        BigDecimal staffFactor = normService.getStaffFactor(operationComponent, operationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.OPTIMAL_STAFF));
        Optional<Entity> techOperCompWorkstationTime = normService.getTechOperCompWorkstationTime(operationComponent, workstation);
        OperationWorkTime partialOperationWorkTime;
        if (techOperCompWorkstationTime.isPresent()) {
            partialOperationWorkTime = operationWorkTimeService.estimateTechOperationWorkTimeForWorkstation(
                    operationComponent,
                    partialOperationComponentRuns,
                    includeTpz, true, techOperCompWorkstationTime.get(),
                    staffFactor);
        } else {
            partialOperationWorkTime = operationWorkTimeService.estimateOperationWorkTime(null, operationComponent,
                    partialOperationComponentRuns, includeTpz, true, false, staffFactor);
        }
        return partialOperationWorkTime.getMachineWorkTime();
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

    private Date getOperationalTasksMaxFinishDateForWorkstation(Date scheduleStartTime, Entity workstation) {
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_OPERATIONAL_TASK).find()
                .add(SearchRestrictions.belongsTo(OperationalTaskFields.WORKSTATION, workstation))
                .add(SearchRestrictions.ne(OperationalTaskFields.STATE, REJECTED));
        Entity parameter = parameterService.getParameter();
        if (parameter.getBooleanField(ParameterFieldsO.SKIP_FINISHED_TASKS)) {
            scb.add(SearchRestrictions.ne(OperationalTaskFields.STATE, FINISHED));
        }

        Entity operationalTasksMaxFinishDateEntity = scb.add(SearchRestrictions.gt(OperationalTaskFields.FINISH_DATE, scheduleStartTime))
                .setProjection(list()
                        .add(alias(SearchProjections.max(OperationalTaskFields.FINISH_DATE), OperationalTaskFields.FINISH_DATE))
                        .add(rowCount()))
                .addOrder(SearchOrders.desc(OperationalTaskFields.FINISH_DATE)).setMaxResults(1).uniqueResult();
        return operationalTasksMaxFinishDateEntity.getDateField(OperationalTaskFields.FINISH_DATE);
    }

    private void updatePositionWorkstationAndDates(Map.Entry<Long, PositionNewData> entry,
                                                   Map<Long, Date> workstationsFinishDates, Entity position,
                                                   Map<Long, Entity> workstationsPositions) {
        PositionNewData positionNewData = entry.getValue();
        workstationsFinishDates.put(entry.getKey(), positionNewData.getFinishDate());
        position.setField(SchedulePositionFields.WORKSTATION, entry.getKey());
        position.setField(SchedulePositionFields.START_TIME, positionNewData.getStartDate());
        position.setField(SchedulePositionFields.END_TIME, positionNewData.getFinishDate());
        position.setField(SchedulePositionFields.STAFF, null);
        position.setField(SchedulePositionFields.LABOR_WORK_TIME, positionNewData.getLaborWorkTime());
        position.setField(SchedulePositionFields.MACHINE_WORK_TIME, positionNewData.getMachineWorkTime());
        position.setField(SchedulePositionFields.ADDITIONAL_TIME, positionNewData.getAdditionalTime());
        position.setField(SchedulePositionFields.CURRENT_WORKSTATION_CHANGEOVER_FOR_SCHEDULE_POSITIONS, positionNewData.getWorkstationChangeovers());
        position = position.getDataDefinition().fastSave(position);
        workstationsPositions.put(entry.getKey(), position);
    }

    private List<Long> sortPositionsForWorkstations(Long scheduleId) {
        Entity schedule = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_SCHEDULE)
                .get(scheduleId);
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("scheduleId", scheduleId);
        StringBuilder query = new StringBuilder();
        query.append("SELECT id FROM ");
        query.append("(SELECT sp.id, sp.machineworktime, ");
        query.append(
                "string_to_array(regexp_replace(REVERSE(SPLIT_PART(REVERSE(o.number), '-', 1)), '[^0-9.]', '0', 'g'), '.')::int[] AS osort, ");
        query.append("string_to_array(regexp_replace(rtrim(toc.nodenumber, '.'), '[^0-9.]', '0', 'g'), '.')::int[] AS opsort ");
        query.append("FROM orders_scheduleposition sp JOIN technologies_technologyoperationcomponent toc ");
        query.append("ON sp.technologyoperationcomponent_id = toc.id JOIN orders_order o ON sp.order_id = o.id ");
        query.append("WHERE sp.schedule_id = :scheduleId AND o.parent_id IS NOT NULL ");
        query.append("UNION ");
        query.append("SELECT sp.id, sp.machineworktime, ARRAY[]::int[] AS osort, ");
        query.append("string_to_array(regexp_replace(rtrim(toc.nodenumber, '.'), '[^0-9.]', '0', 'g'), '.')::int[] AS opsort ");
        query.append("FROM orders_scheduleposition sp JOIN technologies_technologyoperationcomponent toc ");
        query.append("ON sp.technologyoperationcomponent_id = toc.id JOIN orders_order o ON sp.order_id = o.id ");
        query.append("WHERE sp.schedule_id = :scheduleId AND o.parent_id IS NULL) AS positions ");
        query.append("ORDER BY osort desc, opsort desc, ");
        if (ScheduleSortOrder.DESCENDING.getStringValue().equals(schedule.getStringField(ScheduleFields.SORT_ORDER))) {
            query.append("machineworktime desc");
        } else {
            query.append("machineworktime asc");
        }
        return jdbcTemplate.queryForList(query.toString(), parameters, Long.class);
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
