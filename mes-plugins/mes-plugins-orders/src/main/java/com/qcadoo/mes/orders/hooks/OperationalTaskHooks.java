package com.qcadoo.mes.orders.hooks;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.OperationalTasksService;
import com.qcadoo.mes.orders.WorkstationChangeoverService;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.WorkstationChangeoverForOperationalTaskFields;
import com.qcadoo.mes.orders.states.OperationalTasksServiceMarker;
import com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
        setWorkstationChangeoverForOperationalTasks(operationalTaskDD, operationalTask);
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
