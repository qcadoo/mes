package com.qcadoo.mes.productionScheduling.listeners;

import static com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues.FINISHED;
import static com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues.REJECTED;
import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.list;
import static com.qcadoo.model.api.search.SearchProjections.rowCount;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.basic.constants.WorkstationTypeFields;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.constants.ScheduleFields;
import com.qcadoo.mes.orders.constants.SchedulePositionFields;
import com.qcadoo.mes.orders.constants.ScheduleSortOrder;
import com.qcadoo.mes.orders.constants.ScheduleWorkstationAssignCriterion;
import com.qcadoo.mes.orders.listeners.ScheduleDetailsListeners;
import com.qcadoo.mes.orders.validators.SchedulePositionValidators;
import com.qcadoo.mes.productionLines.constants.WorkstationFieldsPL;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.AssignedToOperation;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.mes.timeNormsForOperations.NormService;
import com.qcadoo.mes.timeNormsForOperations.constants.TechOperCompWorkstationTimeFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginManager;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class ScheduleDetailsListenersPS {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduleDetailsListenersPS.class);

    private static final String ORDERS_FOR_SUBPRODUCTS_GENERATION = "ordersForSubproductsGeneration";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private ScheduleDetailsListeners scheduleDetailsListeners;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private SchedulePositionValidators schedulePositionValidators;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NormService normService;

    @Transactional
    public void generatePlan(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        getOperations(view, state, args);
        assignOperationsToWorkstations(view, state, args);
        scheduleDetailsListeners.assignWorkersToOperations(view, state, args);
    }

    @Transactional
    public void getOperations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        long start = System.currentTimeMillis();
        GridComponent ordersGrid = (GridComponent) view.getComponentByReference(ScheduleFields.ORDERS);
        List<Entity> orders = ordersGrid.getEntities();
        if (orders.isEmpty()) {
            view.addMessage("productionScheduling.error.scheduleNoOrders", ComponentState.MessageType.INFO);
            return;
        }
        FormComponent formComponent = (FormComponent) state;
        Entity schedule = formComponent.getEntity();
        boolean includeTpz = schedule.getBooleanField(ScheduleFields.INCLUDE_TPZ);
        DataDefinition schedulePositionDD = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                OrdersConstants.MODEL_SCHEDULE_POSITION);
        List<Entity> positions = Lists.newArrayList();
        for (Entity order : orders) {
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
            if (technology == null) {
                continue;
            }
            final Map<Long, BigDecimal> operationRuns = Maps.newHashMap();

            OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer = productQuantitiesService
                    .getProductComponentQuantities(technology, order.getDecimalField(OrderFields.PLANNED_QUANTITY),
                            operationRuns);
            List<Entity> operationComponents = technology.getHasManyField(TechnologyFields.OPERATION_COMPONENTS);
            for (Entity operationComponent : operationComponents) {
                BigDecimal operationComponentRuns = numberService.setScaleWithDefaultMathContext(BigDecimalUtils
                        .convertNullToZero(operationRuns.get(operationComponent.getId())));
                BigDecimal staffFactor = normService.getStaffFactor(operationComponent, operationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.OPTIMAL_STAFF));
                OperationWorkTime operationWorkTime = operationWorkTimeService.estimateTechOperationWorkTime(operationComponent,
                        operationComponentRuns, includeTpz, false, false, staffFactor);
                Entity schedulePosition = createSchedulePosition(schedule, schedulePositionDD, order, operationComponent,
                        operationWorkTime, operationProductComponentWithQuantityContainer, operationComponentRuns);
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
                                          OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer,
                                          BigDecimal operationComponentRuns) {
        Entity schedulePosition = schedulePositionDD.create();
        schedulePosition.setField(SchedulePositionFields.SCHEDULE, schedule);
        schedulePosition.setField(SchedulePositionFields.ORDER, order);
        schedulePosition.setField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent);
        Entity mainOutputProductComponent = technologyService.getMainOutputProductComponent(technologyOperationComponent);
        Entity product = mainOutputProductComponent.getBelongsToField(OperationProductOutComponentFields.PRODUCT);
        if (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue().equals(product.getField(ProductFields.ENTITY_TYPE))) {
            product = order.getBelongsToField(OrderFields.PRODUCT);
        }
        schedulePosition.setField(SchedulePositionFields.PRODUCT, product);
        schedulePosition.setField(SchedulePositionFields.QUANTITY,
                operationProductComponentWithQuantityContainer.get(mainOutputProductComponent));
        schedulePosition.setField(SchedulePositionFields.ADDITIONAL_TIME,
                technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.TIME_NEXT_OPERATION));
        schedulePosition.setField(SchedulePositionFields.LABOR_WORK_TIME, operationWorkTime.getLaborWorkTime());
        schedulePosition.setField(SchedulePositionFields.MACHINE_WORK_TIME, operationWorkTime.getMachineWorkTime());
        schedulePosition.setField(SchedulePositionFields.OPERATION_RUNS, operationComponentRuns);
        return schedulePosition;
    }

    @Transactional
    public void assignOperationsToWorkstations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        long startAll = System.currentTimeMillis();
        Entity schedule = ((FormComponent) state).getEntity();
        Map<Long, Date> workstationsFinishDates = Maps.newHashMap();
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
            List<Entity> workstations = schedulePositionValidators.getWorkstationsFromTOC(schedule, position, order);
            if (workstations.isEmpty()) {
                ordersToAvoid.add(order.getId());
                continue;
            }
            Map<Long, PositionNewData> operationWorkstationsPositionNewData = Maps.newHashMap();

            boolean allMachineWorkTimesEqualsZero = getWorkstationsNewFinishDate(workstationsFinishDates, scheduleStartTime,
                    position, workstations, operationWorkstationsPositionNewData);

            if (allMachineWorkTimesEqualsZero) {
                ordersToAvoid.add(order.getId());
                continue;
            }

            if (ScheduleWorkstationAssignCriterion.SHORTEST_TIME.getStringValue()
                    .equals(schedule.getStringField(ScheduleFields.WORKSTATION_ASSIGN_CRITERION))) {
                operationWorkstationsPositionNewData.entrySet().stream()
                        .min(Comparator.comparing(e -> e.getValue().getFinishDate()))
                        .ifPresent(entry -> updatePositionWorkstationAndDates(entry, workstationsFinishDates, position));
            } else {
                Map.Entry<Long, PositionNewData> firstEntry;
                if (workstationsFinishDates.isEmpty()) {
                    firstEntry = operationWorkstationsPositionNewData.entrySet().iterator().next();
                } else {
                    firstEntry = operationWorkstationsPositionNewData.entrySet().stream()
                            .filter(entry -> workstationsFinishDates.containsKey(entry.getKey())).findFirst()
                            .orElse(operationWorkstationsPositionNewData.entrySet().iterator().next());
                }
                updatePositionWorkstationAndDates(firstEntry, workstationsFinishDates, position);
            }
            long finish = System.currentTimeMillis();
            LOG.info("Plan for shift - workstation assignment: {}s.", (finish - start) / 1000);
        }
        long finishAll = System.currentTimeMillis();
        LOG.info("Plan for shift {} - workstations assignment: {}s.", schedule.getStringField(ScheduleFields.NUMBER), (finishAll - startAll) / 1000);
    }

    private boolean getWorkstationsNewFinishDate(Map<Long, Date> workstationsFinishDates, Date scheduleStartTime, Entity position,
                                                 List<Entity> workstations, Map<Long, PositionNewData> operationWorkstationsPositionNewData) {
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
            DateTime finishDateTime = new DateTime(finishDate);
            Entity productionLine = workstation.getBelongsToField(WorkstationFieldsPL.PRODUCTION_LINE);
            Date newStartDate = shiftsService
                    .getNearestWorkingDate(finishDateTime, productionLine).orElse(finishDateTime).toDate();

            Date newFinishDate = shiftsService.findDateToForProductionLine(newStartDate, machineWorkTime, productionLine);
            if (schedule.getBooleanField(ScheduleFields.ADDITIONAL_TIME_EXTENDS_OPERATION)) {
                newFinishDate = Date.from(newFinishDate.toInstant().plusSeconds(additionalTime));
            }
            PositionNewData positionNewData = new PositionNewData(laborWorkTime, machineWorkTime, additionalTime, newStartDate,
                    newFinishDate);
            operationWorkstationsPositionNewData.put(workstation.getId(), positionNewData);
        }
        return allMachineWorkTimesEqualsZero;
    }

    private Date getFinishDateWithChildren(Entity position, Date finishDate) {
        Date childEndTime = schedulePositionValidators.getChildrenMaxEndTime(position);
        if (!Objects.isNull(childEndTime) && childEndTime.after(finishDate)) {
            finishDate = childEndTime;
        }
        if (pluginManager.isPluginEnabled(ORDERS_FOR_SUBPRODUCTS_GENERATION)) {
            childEndTime = schedulePositionValidators.getOrdersChildrenMaxEndTime(position);
            if (!Objects.isNull(childEndTime) && childEndTime.after(finishDate)) {
                finishDate = childEndTime;
            }
        }
        return finishDate;
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
                                                   Map<Long, Date> workstationsFinishDates, Entity position) {
        PositionNewData positionNewData = entry.getValue();
        workstationsFinishDates.put(entry.getKey(), positionNewData.getFinishDate());
        position.setField(SchedulePositionFields.WORKSTATION, entry.getKey());
        position.setField(SchedulePositionFields.START_TIME, positionNewData.getStartDate());
        position.setField(SchedulePositionFields.END_TIME, positionNewData.getFinishDate());
        position.setField(SchedulePositionFields.STAFF, null);
        position.setField(SchedulePositionFields.LABOR_WORK_TIME, positionNewData.getLaborWorkTime());
        position.setField(SchedulePositionFields.MACHINE_WORK_TIME, positionNewData.getMachineWorkTime());
        position.setField(SchedulePositionFields.ADDITIONAL_TIME, positionNewData.getAdditionalTime());
        position.getDataDefinition().fastSave(position);
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

}
