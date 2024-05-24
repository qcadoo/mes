package com.qcadoo.mes.orders.hooks;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.OperationalTasksService;
import com.qcadoo.mes.orders.constants.*;
import com.qcadoo.mes.orders.services.WorkstationChangeoverService;
import com.qcadoo.mes.orders.states.OperationalTasksServiceMarker;
import com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.mes.orders.validators.OperationalTaskValidators;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
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
    private OperationalTaskValidators operationalTaskValidators;

    @Autowired
    private WorkstationChangeoverService workstationChangeoverService;

    public void onCopy(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        setInitialState(operationalTask);
    }

    public void onCreate(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        setInitialState(operationalTask);
    }

    public void setInitialState(final Entity operationalTask) {
        stateExecutorService.buildInitial(OperationalTasksServiceMarker.class, operationalTask,
                OperationalTaskStateStringValues.PENDING);
    }

    public void onSave(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        fillNameAndDescription(operationalTask);
        setWorkstationChangeoverForOperationalTasks(operationalTask);

        operationalTaskValidators.datesAreCorrect(operationalTaskDD, operationalTask);

        changeDateInOrder(operationalTask);
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

    public void setWorkstationChangeoverForOperationalTasks(final Entity operationalTask) {
        Long operationalTaskId = operationalTask.getId();
        String type = operationalTask.getStringField(OperationalTaskFields.TYPE);
        Entity workstation = operationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION);
        Date startDate = operationalTask.getDateField(OperationalTaskFields.START_DATE);
        boolean shouldSkip = operationalTask.getBooleanField(OperationalTaskFields.SHOULD_SKIP);

        if (OperationalTaskType.EXECUTION_OPERATION_IN_ORDER.getStringValue().equals(type)) {
            if (Objects.isNull(workstation) || Objects.isNull(startDate)) {
                deleteWorkstationChangeoverForOperationalTasks(operationalTask);
                setPreviousWorkstationChangeoverForOperationalTasks(operationalTask, true);
            } else {
                if (!shouldSkip && (Objects.isNull(operationalTaskId) || checkIfOperationalTaskDataChanged(operationalTask, workstation, startDate))) {
                    setCurrentWorkstationChangeoverForOperationalTasks(operationalTask);
                    setPreviousWorkstationChangeoverForOperationalTasks(operationalTask, false);
                }
            }
        }
    }

    private boolean checkIfOperationalTaskDataChanged(final Entity operationalTask, final Entity workstation, final Date startDate) {
        Entity operationalTaskFromDB = operationalTask.getDataDefinition().get(operationalTask.getId());
        Entity workstationFromDB = operationalTaskFromDB.getBelongsToField(OperationalTaskFields.WORKSTATION);
        Date startDateFromDB = operationalTaskFromDB.getDateField(OperationalTaskFields.START_DATE);

        return Objects.isNull(workstationFromDB) || !workstation.getId().equals(workstationFromDB.getId())
                || Objects.isNull(startDateFromDB) || !startDate.equals(startDateFromDB);
    }

    public void deleteWorkstationChangeoverForOperationalTasks(final Entity operationalTask) {
        List<Entity> currentWorkstationChangeoverForOperationalTasks = operationalTask.getHasManyField(OperationalTaskFields.CURRENT_WORKSTATION_CHANGEOVER_FOR_OPERATIONAL_TASKS);

        currentWorkstationChangeoverForOperationalTasks.forEach(workstationChangeoverForOperationalTask ->
                workstationChangeoverForOperationalTask.getDataDefinition().delete(workstationChangeoverForOperationalTask.getId()));

        List<Entity> previousWorkstationChangeoverForOperationalTasks = operationalTask.getHasManyField(OperationalTaskFields.PREVIOUS_WORKSTATION_CHANGEOVER_FOR_OPERATIONAL_TASKS);

        previousWorkstationChangeoverForOperationalTasks.stream().filter(filterByChangeoverTypeOwn()).forEach(workstationChangeoverForOperationalTask ->
                workstationChangeoverForOperationalTask.getDataDefinition().delete(workstationChangeoverForOperationalTask.getId()));
    }

    private Predicate<Entity> filterByChangeoverTypeOwn() {
        return workstationChangeoverForOperationalTask -> WorkstationChangeoverForOperationalTaskChangeoverType.OWN.getStringValue().equals(workstationChangeoverForOperationalTask.getStringField(WorkstationChangeoverForOperationalTaskFields.CHANGEOVER_TYPE));
    }

    private void setCurrentWorkstationChangeoverForOperationalTasks(final Entity operationalTask) {
        List<Entity> workstationChangeoverForOperationalTasks = workstationChangeoverService.findWorkstationChangeoverForOperationalTasks(operationalTask);

        setOperationalTaskDates(operationalTask, workstationChangeoverForOperationalTasks);

        operationalTask.setField(OperationalTaskFields.CURRENT_WORKSTATION_CHANGEOVER_FOR_OPERATIONAL_TASKS, workstationChangeoverForOperationalTasks);
    }

    public void setPreviousWorkstationChangeoverForOperationalTasks(final Entity operationalTask, final boolean shouldSkip) {
        List<Entity> previousWorkstationChangeoverForOperationalTasks = operationalTask.getHasManyField(OperationalTaskFields.PREVIOUS_WORKSTATION_CHANGEOVER_FOR_OPERATIONAL_TASKS);

        previousWorkstationChangeoverForOperationalTasks.stream().filter(filterByChangeoverTypeBasedOnNorm()).forEach(workstationChangeoverForOperationalTask -> {
            Entity currentOperationalTask = workstationChangeoverForOperationalTask.getBelongsToField(WorkstationChangeoverForOperationalTaskFields.CURRENT_OPERATIONAL_TASK);

            currentOperationalTask.setField(OperationalTaskFields.SHOULD_SKIP, true);

            setWorkstationChangeoverForOperationalTasksAndUpdateDates(currentOperationalTask, operationalTask, shouldSkip);
        });
    }

    private Predicate<Entity> filterByChangeoverTypeBasedOnNorm() {
        return workstationChangeoverForOperationalTask -> WorkstationChangeoverForOperationalTaskChangeoverType.BASED_ON_NORM.getStringValue().equals(workstationChangeoverForOperationalTask.getStringField(WorkstationChangeoverForOperationalTaskFields.CHANGEOVER_TYPE));
    }

    private void setWorkstationChangeoverForOperationalTasksAndUpdateDates(final Entity operationalTask, final Entity skipOperationalTask, final boolean shouldSkip) {
        if (Objects.nonNull(operationalTask) && Objects.nonNull(skipOperationalTask)) {
            Optional<Entity> mayBePreviousOperationalTask;

            if (shouldSkip) {
                mayBePreviousOperationalTask = workstationChangeoverService.findPreviousOperationalTask(operationalTask, skipOperationalTask);
            } else {
                if (checkIfWorkstationsAreSame(operationalTask, skipOperationalTask)) {
                    List<Entity> previousOperationalTasks = filterOperationalTasks(workstationChangeoverService.getPreviousOperationalTasks(operationalTask), skipOperationalTask);

                    if (checkIfDateIfBefore(operationalTask, skipOperationalTask)) {
                        previousOperationalTasks.add(skipOperationalTask);
                    }

                    mayBePreviousOperationalTask = previousOperationalTasks.stream().max(Comparator.comparing(previousOperationalTask -> previousOperationalTask.getDateField(OperationalTaskFields.FINISH_DATE)));
                } else {
                    mayBePreviousOperationalTask = workstationChangeoverService.findPreviousOperationalTask(operationalTask, skipOperationalTask);
                }
            }

            if (mayBePreviousOperationalTask.isPresent()) {
                Entity previousOperationalTask = mayBePreviousOperationalTask.get();

                List<Entity> workstationChangeoverForOperationalTasks = workstationChangeoverService.findWorkstationChangeoverForOperationalTasks(operationalTask, previousOperationalTask);

                setOperationalTaskDates(operationalTask, workstationChangeoverForOperationalTasks);

                operationalTask.setField(OperationalTaskFields.CURRENT_WORKSTATION_CHANGEOVER_FOR_OPERATIONAL_TASKS, workstationChangeoverForOperationalTasks);

                operationalTask.getDataDefinition().save(operationalTask);
            }
        }
    }

    private List<Entity> filterOperationalTasks(final List<Entity> operationalTasks, final Entity skipOperationalTask) {
        return operationalTasks.stream().filter(operationalTask -> !operationalTask.getId().equals(skipOperationalTask.getId())).collect(Collectors.toList());
    }

    private boolean checkIfWorkstationsAreSame(final Entity operationalTask, final Entity skipOperationalTask) {
        Entity operationalTaskWorkstation = operationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION);
        Entity skipOperationalTaskWorkstation = skipOperationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION);

        return Objects.nonNull(operationalTaskWorkstation) && Objects.nonNull(skipOperationalTaskWorkstation) &&
                operationalTaskWorkstation.getId().equals(skipOperationalTaskWorkstation.getId());
    }

    private boolean checkIfDateIfBefore(final Entity operationalTask, final Entity skipOperationalTask) {
        Date operationalTaskStartDate = operationalTask.getDateField(OperationalTaskFields.START_DATE);
        Date skipOperationalFinishDate = skipOperationalTask.getDateField(OperationalTaskFields.FINISH_DATE);

        return skipOperationalFinishDate.before(operationalTaskStartDate);
    }

    private void setOperationalTaskDates(final Entity operationalTask, final List<Entity> workstationChangeoverForOperationalTasks) {
        if (!workstationChangeoverForOperationalTasks.isEmpty()) {
            Optional<Date> mayBeMaxFinishDate = workstationChangeoverService.getWorkstationChangeoversMaxFinishDate(workstationChangeoverForOperationalTasks);

            Date startDate = operationalTask.getDateField(OperationalTaskFields.START_DATE);
            if (mayBeMaxFinishDate.isPresent() && startDate.compareTo(mayBeMaxFinishDate.get()) < 0) {
                Date finishDate = operationalTask.getDateField(OperationalTaskFields.FINISH_DATE);

                int duration = Seconds.secondsBetween(new DateTime(startDate), new DateTime(finishDate)).getSeconds();

                startDate = mayBeMaxFinishDate.get();
                finishDate = new DateTime(startDate).plusSeconds(duration).toDate();

                operationalTask.setField(OperationalTaskFields.START_DATE, startDate);
                operationalTask.setField(OperationalTaskFields.FINISH_DATE, finishDate);
            }
        }
    }

    public void onDelete(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        Entity workstation = operationalTask.getBelongsToField(OperationalTaskFields.WORKSTATION);
        Date startDate = operationalTask.getDateField(OperationalTaskFields.START_DATE);

        if (Objects.nonNull(workstation) && Objects.nonNull(startDate)) {
            setPreviousWorkstationChangeoverForOperationalTasks(operationalTask, true);
        }
    }

}
