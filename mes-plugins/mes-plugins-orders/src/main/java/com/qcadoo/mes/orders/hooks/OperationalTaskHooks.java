package com.qcadoo.mes.orders.hooks;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.orders.OperationalTasksService;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.OperationalTasksServiceMarker;
import com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.mes.productionLines.constants.WorkstationFieldsPL;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechOperCompWorkstationTimeFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Service
public class OperationalTaskHooks {

    @Autowired
    private StateExecutorService stateExecutorService;

    @Autowired
    private OperationalTasksService operationalTasksService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onCopy(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        setInitialState(operationalTask);
    }

    public void onCreate(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        setInitialState(operationalTask);
    }

    public void onSave(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        fillNameAndDescription(operationalTask);
        changeDateInOrder(operationalTask);
        setStaff(operationalTask);
    }

    public void setStaff(final Entity operationalTask) {
        Entity technologyOperationComponent = operationalTask
                .getBelongsToField(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);
        int plannedStaff;
        if (!Objects.isNull(technologyOperationComponent)) {
            plannedStaff = technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.MIN_STAFF);
        } else {
            plannedStaff = 1;
        }
        Integer actualStaff = operationalTask.getIntegerField(OperationalTaskFields.ACTUAL_STAFF);
        if (actualStaff == null) {
            actualStaff = plannedStaff;
            operationalTask.setField(OperationalTaskFields.ACTUAL_STAFF, plannedStaff);
        }
        List<Entity> workers = operationalTask.getManyToManyField(OperationalTaskFields.WORKERS);
        Entity staff = operationalTask.getBelongsToField(OperationalTaskFields.STAFF);
        Entity operationalTaskDB = null;
        if (operationalTask.getId() != null) {
            operationalTaskDB = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                    OrdersConstants.MODEL_OPERATIONAL_TASK).get(operationalTask.getId());
        }
        if (workers.size() > 1 || workers.isEmpty() && operationalTaskDB != null
                && !operationalTaskDB.getManyToManyField(OperationalTaskFields.WORKERS).isEmpty()) {
            operationalTask.setField(OperationalTaskFields.STAFF, null);
        } else if (staff != null && workers.size() <= 1) {
            operationalTask.setField(OperationalTaskFields.WORKERS, Collections.singletonList(staff));
        } else if (staff == null && workers.size() == 1) {
            operationalTask.setField(OperationalTaskFields.STAFF, workers.get(0));
        }

        updateFinishDate(operationalTask, technologyOperationComponent, plannedStaff, actualStaff, operationalTaskDB);
        if (actualStaff != operationalTask.getManyToManyField(OperationalTaskFields.WORKERS).size()) {
            operationalTask.addGlobalMessage(
                    "orders.operationalTask.error.workersQuantityDifferentThanActualStaff");
        }
    }

    private void updateFinishDate(Entity operationalTask, Entity technologyOperationComponent, int plannedStaff, Integer actualStaff, Entity operationalTaskDB) {
        if (!Objects.isNull(technologyOperationComponent) && technologyOperationComponent
                .getBooleanField(TechnologyOperationComponentFieldsTNFO.TJ_DECREASES_FOR_ENLARGED_STAFF) &&
                (operationalTask.getId() == null && actualStaff != plannedStaff || operationalTaskDB != null &&
                        actualStaff != operationalTaskDB.getIntegerField(OperationalTaskFields.ACTUAL_STAFF).intValue())) {
            operationalTask.setField(OperationalTaskFields.FINISH_DATE,
                    getFinishDate(operationalTask, technologyOperationComponent));
        }
    }

    private Date getFinishDate(Entity task, Entity technologyOperationComponent) {
        Entity order = task.getBelongsToField(OperationalTaskFields.ORDER);
        Entity workstation = task.getBelongsToField(OperationalTaskFields.WORKSTATION);
        Date startDate = task.getDateField(OperationalTaskFields.START_DATE);
        Entity parameter = parameterService.getParameter();
        boolean includeTpz = parameter.getBooleanField("includeTpzSG");
        Entity technology = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
        final Map<Long, BigDecimal> operationRuns = Maps.newHashMap();

        productQuantitiesService.getProductComponentQuantities(technology, order.getDecimalField(OrderFields.PLANNED_QUANTITY),
                operationRuns);

        Optional<Entity> techOperCompWorkstationTime = getTechOperCompWorkstationTime(technologyOperationComponent, workstation);
        Integer machineWorkTime;
        Integer additionalTime;
        Integer actualStaff = task.getIntegerField(OperationalTaskFields.ACTUAL_STAFF);
        int plannedStaff = technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.MIN_STAFF);
        BigDecimal staffFactor = BigDecimal.valueOf(plannedStaff).divide(BigDecimal.valueOf(actualStaff), numberService.getMathContext());
        if (techOperCompWorkstationTime.isPresent()) {
            OperationWorkTime operationWorkTime = operationWorkTimeService.estimateTechOperationWorkTimeForWorkstation(
                    technologyOperationComponent,
                    BigDecimalUtils.convertNullToZero(operationRuns.get(technologyOperationComponent.getId())), includeTpz, false,
                    techOperCompWorkstationTime.get(), staffFactor);
            machineWorkTime = operationWorkTime.getMachineWorkTime();
            additionalTime = techOperCompWorkstationTime.get()
                    .getIntegerField(TechOperCompWorkstationTimeFields.TIME_NEXT_OPERATION);
        } else {
            OperationWorkTime operationWorkTime = operationWorkTimeService.estimateTechOperationWorkTime(
                    technologyOperationComponent,
                    BigDecimalUtils.convertNullToZero(operationRuns.get(technologyOperationComponent.getId())), includeTpz, false,
                    false, staffFactor);
            machineWorkTime = operationWorkTime.getMachineWorkTime();
            additionalTime = technologyOperationComponent
                    .getIntegerField(TechnologyOperationComponentFieldsTNFO.TIME_NEXT_OPERATION);
        }

        Entity productionLine = null;
        if (workstation != null) {
            productionLine = workstation.getBelongsToField(WorkstationFieldsPL.PRODUCTION_LINE);
        }
        Date finishDate = shiftsService.findDateToForProductionLine(startDate, machineWorkTime, productionLine);
        if (parameter.getBooleanField("includeAdditionalTimeSG")) {
            finishDate = Date.from(finishDate.toInstant().plusSeconds(additionalTime));
        }
        return finishDate;
    }

    private Optional<Entity> getTechOperCompWorkstationTime(Entity technologyOperationComponent, Entity workstation) {
        List<Entity> techOperCompWorkstationTimes = technologyOperationComponent
                .getHasManyField(TechnologyOperationComponentFieldsTNFO.TECH_OPER_COMP_WORKSTATION_TIMES);
        for (Entity techOperCompWorkstationTime : techOperCompWorkstationTimes) {
            if (techOperCompWorkstationTime.getBelongsToField(TechOperCompWorkstationTimeFields.WORKSTATION).getId()
                    .equals(workstation.getId())) {
                return Optional.of(techOperCompWorkstationTime);
            }
        }
        return Optional.empty();
    }

    public void changeDateInOrder(Entity operationalTask) {
        String type = operationalTask.getStringField(OperationalTaskFields.TYPE);

        if (operationalTasksService.isOperationalTaskTypeExecutionOperationInOrder(type)) {
            if (parameterService.getParameter().getBooleanField("setOrderDatesBasedOnTaskDates")) {
                Entity order = operationalTask.getBelongsToField(OperationalTaskFields.ORDER);
                List<Entity> operationalTasks = Lists.newArrayList(order.getHasManyField(OrderFields.OPERATIONAL_TASKS));
                if (Objects.nonNull(operationalTask.getId())) {
                    operationalTasks = operationalTasks.stream().filter(op -> !op.getId().equals(operationalTask.getId()))
                            .collect(Collectors.toList());
                }

                operationalTasks.add(operationalTask);
                Date start = operationalTasks.stream().map(o -> o.getDateField(OperationalTaskFields.START_DATE))
                        .min(Date::compareTo).get();

                Date finish = operationalTasks.stream().map(o -> o.getDateField(OperationalTaskFields.FINISH_DATE))
                        .max(Date::compareTo).get();

                boolean changed = false;
                if (Objects.isNull(order.getDateField(OrderFields.START_DATE))
                        || !order.getDateField(OrderFields.START_DATE).equals(start)) {
                    changed = true;
                    order.setField(OrderFields.START_DATE, start);
                }

                if (Objects.isNull(order.getDateField(OrderFields.FINISH_DATE))
                        || !order.getDateField(OrderFields.FINISH_DATE).equals(finish)) {
                    changed = true;
                    order.setField(OrderFields.FINISH_DATE, finish);
                }
                if (changed) {
                    order.getDataDefinition().save(order);
                }
            }
        }
    }

    public void fillNameAndDescription(final Entity operationalTask) {
        String type = operationalTask.getStringField(OperationalTaskFields.TYPE);

        if (operationalTasksService.isOperationalTaskTypeExecutionOperationInOrder(type)) {
            Entity technologyOperationComponent = operationalTask
                    .getBelongsToField(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);

            if (technologyOperationComponent == null) {
                operationalTask.setField(OperationalTaskFields.NAME, null);
                operationalTask.setField(OperationalTaskFields.DESCRIPTION, null);
            } else {
                Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);
                operationalTask.setField(OperationalTaskFields.NAME, operation.getStringField(OperationFields.NAME));

                if (Objects.isNull(operationalTask.getId())) {
                    boolean copyDescriptionFromProductionOrder = parameterService.getParameter()
                            .getBooleanField("otCopyDescriptionFromProductionOrder");
                    if (copyDescriptionFromProductionOrder) {
                        StringBuilder descriptionBuilder = new StringBuilder();
                        descriptionBuilder.append(Strings.nullToEmpty(
                                technologyOperationComponent.getStringField(TechnologyOperationComponentFields.COMMENT)));
                        if (StringUtils.isNoneBlank(descriptionBuilder.toString())) {
                            descriptionBuilder.append("\n");
                        }
                        Entity order = operationalTask.getBelongsToField(OperationalTaskFields.ORDER);
                        if (Objects.nonNull(order)) {
                            descriptionBuilder.append(Strings.nullToEmpty(order.getStringField(OrderFields.DESCRIPTION)));
                        }
                        operationalTask.setField(OperationalTaskFields.DESCRIPTION, descriptionBuilder.toString());
                    } else {
                        operationalTask.setField(OperationalTaskFields.DESCRIPTION,
                                technologyOperationComponent.getStringField(TechnologyOperationComponentFields.COMMENT));
                    }
                }
            }
        }
    }

    public void setInitialState(final Entity operationalTask) {
        stateExecutorService.buildInitial(OperationalTasksServiceMarker.class, operationalTask,
                OperationalTaskStateStringValues.PENDING);
    }
}
