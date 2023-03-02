package com.qcadoo.mes.orders.hooks;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.ShiftsService;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTime;
import com.qcadoo.mes.operationTimeCalculations.OperationWorkTimeService;
import com.qcadoo.mes.orders.OperationalTasksService;
import com.qcadoo.mes.orders.WorkstationChangeoverService;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.WorkstationChangeoverForOperationalTaskFields;
import com.qcadoo.mes.orders.states.OperationalTasksServiceMarker;
import com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.mes.productionLines.constants.WorkstationFieldsPL;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.ProductQuantitiesHolder;
import com.qcadoo.mes.timeNormsForOperations.NormService;
import com.qcadoo.mes.timeNormsForOperations.constants.TechOperCompWorkstationTimeFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OperationalTaskHooks {

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private StateExecutorService stateExecutorService;

    @Autowired
    private OperationalTasksService operationalTasksService;

    @Autowired
    private OperationWorkTimeService operationWorkTimeService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private ShiftsService shiftsService;

    @Autowired
    private NormService normService;

    @Autowired
    private WorkstationChangeoverService workstationChangeoverService;

    public void onCopy(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        setInitialState(operationalTask);
    }

    public void onCreate(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        setInitialState(operationalTask);
    }

    public void onSave(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        fillNameAndDescription(operationalTask);
        changeDateInOrder(operationalTask);
        setStaff(operationalTaskDD, operationalTask);
        setWorkstationChangeoverForOperationalTasks(operationalTaskDD, operationalTask);
    }

    private void updateFinishDate(final Entity operationalTask, final Entity technologyOperationComponent, final Integer actualStaff, final Entity operationalTaskDB) {
        if (!Objects.isNull(technologyOperationComponent) && technologyOperationComponent
                .getBooleanField(TechnologyOperationComponentFieldsTNFO.TJ_DECREASES_FOR_ENLARGED_STAFF) &&
                (Objects.isNull(operationalTask.getId()) && !actualStaff.equals(technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.MIN_STAFF))
                        || Objects.nonNull(operationalTaskDB) && actualStaff != operationalTaskDB.getIntegerField(OperationalTaskFields.ACTUAL_STAFF).intValue())) {
            operationalTask.setField(OperationalTaskFields.FINISH_DATE,
                    getFinishDate(operationalTask, technologyOperationComponent));
        }
    }

    private Date getFinishDate(final Entity operationalTask, final Entity technologyOperationComponent) {
        Entity order = operationalTask.getBelongsToField(OperationalTaskFields.ORDER);
        Entity workstation = operationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION);
        Date startDate = operationalTask.getDateField(OperationalTaskFields.START_DATE);
        Entity parameter = parameterService.getParameter();
        boolean includeTpz = parameter.getBooleanField("includeTpzSG");
        Entity technology = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);

        ProductQuantitiesHolder productQuantitiesAndOperationRuns = productQuantitiesService.getProductComponentQuantities(technology, order.getDecimalField(OrderFields.PLANNED_QUANTITY));

        Optional<Entity> techOperCompWorkstationTime = normService.getTechOperCompWorkstationTime(technologyOperationComponent, workstation);
        BigDecimal staffFactor = normService.getStaffFactor(technologyOperationComponent, operationalTask.getIntegerField(OperationalTaskFields.ACTUAL_STAFF));

        Integer machineWorkTime;
        Integer additionalTime;

        if (techOperCompWorkstationTime.isPresent()) {
            OperationWorkTime operationWorkTime = operationWorkTimeService.estimateTechOperationWorkTimeForWorkstation(
                    technologyOperationComponent,
                    BigDecimalUtils.convertNullToZero(productQuantitiesAndOperationRuns.getOperationRuns().get(technologyOperationComponent.getId())), includeTpz, false,
                    techOperCompWorkstationTime.get(), staffFactor);

            machineWorkTime = operationWorkTime.getMachineWorkTime();
            additionalTime = techOperCompWorkstationTime.get()
                    .getIntegerField(TechOperCompWorkstationTimeFields.TIME_NEXT_OPERATION);
        } else {
            OperationWorkTime operationWorkTime = operationWorkTimeService.estimateOperationWorkTime(null,
                    technologyOperationComponent,
                    BigDecimalUtils.convertNullToZero(productQuantitiesAndOperationRuns.getOperationRuns().get(technologyOperationComponent.getId())), includeTpz, false,
                    false, staffFactor);

            machineWorkTime = operationWorkTime.getMachineWorkTime();
            additionalTime = technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.TIME_NEXT_OPERATION);
        }

        Entity productionLine = null;

        if (Objects.nonNull(workstation)) {
            productionLine = workstation.getBelongsToField(WorkstationFieldsPL.PRODUCTION_LINE);
        }

        Date finishDate = shiftsService.findDateToForProductionLine(startDate, machineWorkTime, productionLine);

        if (parameter.getBooleanField("includeAdditionalTimeSG")) {
            finishDate = Date.from(finishDate.toInstant().plusSeconds(additionalTime));
        }

        return finishDate;
    }

    public void fillNameAndDescription(final Entity operationalTask) {
        String type = operationalTask.getStringField(OperationalTaskFields.TYPE);

        if (operationalTasksService.isOperationalTaskTypeExecutionOperationInOrder(type)) {
            Entity technologyOperationComponent = operationalTask
                    .getBelongsToField(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);

            if (Objects.isNull(technologyOperationComponent)) {
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

    public void changeDateInOrder(final Entity operationalTask) {
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

    public void setStaff(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        Entity technologyOperationComponent = operationalTask
                .getBelongsToField(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);

        int optimalStaff = getOptimalStaff(technologyOperationComponent);

        Integer actualStaff = operationalTask.getIntegerField(OperationalTaskFields.ACTUAL_STAFF);

        if (Objects.isNull(actualStaff)) {
            actualStaff = optimalStaff;
            operationalTask.setField(OperationalTaskFields.ACTUAL_STAFF, actualStaff);
        }

        List<Entity> workers = operationalTask.getManyToManyField(OperationalTaskFields.WORKERS);

        Entity staff = operationalTask.getBelongsToField(OperationalTaskFields.STAFF);
        Entity operationalTaskDB = null;

        if (Objects.nonNull(operationalTask.getId())) {
            operationalTaskDB = operationalTaskDD.get(operationalTask.getId());
        }

        if (workers.size() > 1 || workers.isEmpty() && Objects.nonNull(operationalTaskDB)
                && !operationalTaskDB.getManyToManyField(OperationalTaskFields.WORKERS).isEmpty()) {
            operationalTask.setField(OperationalTaskFields.STAFF, null);
        } else if (Objects.nonNull(staff) && workers.size() <= 1) {
            operationalTask.setField(OperationalTaskFields.WORKERS, Collections.singletonList(staff));
        } else if (Objects.isNull(staff) && workers.size() == 1) {
            if (Objects.nonNull(operationalTaskDB) && operationalTaskDB.getManyToManyField(OperationalTaskFields.WORKERS).size() != 1) {
                operationalTask.setField(OperationalTaskFields.STAFF, workers.get(0));
            } else {
                operationalTask.setField(OperationalTaskFields.WORKERS, Collections.emptyList());
            }
        }

        updateFinishDate(operationalTask, technologyOperationComponent, actualStaff, operationalTaskDB);

        if (actualStaff != operationalTask.getManyToManyField(OperationalTaskFields.WORKERS).size()) {
            operationalTask.addGlobalMessage(
                    "orders.operationalTask.error.workersQuantityDifferentThanActualStaff");
        }
    }

    private int getOptimalStaff(final Entity technologyOperationComponent) {
        if (!Objects.isNull(technologyOperationComponent)) {
            return technologyOperationComponent.getIntegerField(TechnologyOperationComponentFieldsTNFO.OPTIMAL_STAFF);
        } else {
            return 1;
        }
    }

    private void setWorkstationChangeoverForOperationalTasks(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        Entity workstation = operationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION);
        Date startDate = operationalTask.getDateField(OperationalTaskFields.START_DATE);

        if (Objects.isNull(workstation) || Objects.isNull(startDate)) {
            List<Entity> currentWorkstationChangeoverForOperationalTasks = operationalTask.getHasManyField(OperationalTaskFields.CURRENT_WORKSTATION_CHANGEOVER_FOR_OPERATIONAL_TASKS);

            currentWorkstationChangeoverForOperationalTasks.forEach(workstationChangeoverForOperationalTask ->
                    workstationChangeoverForOperationalTask.getDataDefinition().delete(workstationChangeoverForOperationalTask.getId()));
        } else {
            Long operationalTaskId = operationalTask.getId();

            if (Objects.nonNull(operationalTaskId)) {
                Entity operationalTaskFromDB = operationalTaskDD.get(operationalTaskId);
                Entity workstationFromDB = operationalTaskFromDB.getBelongsToField(OperationalTaskFields.WORKSTATION);
                Date startDateFromDB = operationalTaskFromDB.getDateField(OperationalTaskFields.START_DATE);

                if (Objects.isNull(workstationFromDB) || !workstation.getId().equals(workstationFromDB.getId())
                        || Objects.isNull(startDateFromDB) || !startDate.equals(startDateFromDB)) {
                    setWorkstationChangeoverForOperationalTasksAndUpdateDates(operationalTask);
                }
            } else {
                setWorkstationChangeoverForOperationalTasksAndUpdateDates(operationalTask);
            }
        }
    }

    private void setWorkstationChangeoverForOperationalTasksAndUpdateDates(final Entity operationalTask) {
        List<Entity> workstationChangeoverForOperationalTasks = workstationChangeoverService.findWorkstationChangeoverForOperationalTasks(operationalTask);

        if (!workstationChangeoverForOperationalTasks.isEmpty()) {
            Optional<Date> mayBeMaxFinishDate = getWorkstationChangeoverForOperationalTasksMaxFinishDate(workstationChangeoverForOperationalTasks);

            if (mayBeMaxFinishDate.isPresent()) {
                Date startDate = operationalTask.getDateField(OperationalTaskFields.START_DATE);
                Date finishDate = operationalTask.getDateField(OperationalTaskFields.FINISH_DATE);

                int duration = Seconds.secondsBetween(new DateTime(startDate), new DateTime(finishDate)).getSeconds();

                startDate = mayBeMaxFinishDate.get();
                finishDate = new DateTime(startDate).plusSeconds(duration).toDate();

                operationalTask.setField(OperationalTaskFields.START_DATE, startDate);
                operationalTask.setField(OperationalTaskFields.FINISH_DATE, finishDate);
            }
        }

        operationalTask.setField(OperationalTaskFields.CURRENT_WORKSTATION_CHANGEOVER_FOR_OPERATIONAL_TASKS, workstationChangeoverForOperationalTasks);
    }

    private Optional<Date> getWorkstationChangeoverForOperationalTasksMaxFinishDate(final List<Entity> workstationChangeoverForOperationalTasks) {
        return workstationChangeoverForOperationalTasks.stream().map(workstationChangeoverForOperationalTask ->
                        workstationChangeoverForOperationalTask.getDateField(WorkstationChangeoverForOperationalTaskFields.FINISH_DATE))
                .max(Date::compareTo);
    }

    public void setInitialState(final Entity operationalTask) {
        stateExecutorService.buildInitial(OperationalTasksServiceMarker.class, operationalTask,
                OperationalTaskStateStringValues.PENDING);
    }

}
