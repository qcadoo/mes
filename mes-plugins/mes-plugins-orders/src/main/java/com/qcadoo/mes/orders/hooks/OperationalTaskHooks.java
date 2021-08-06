package com.qcadoo.mes.orders.hooks;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.OperationalTasksService;
import com.qcadoo.mes.orders.constants.OperationalTaskFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.OperationalTasksServiceMarker;
import com.qcadoo.mes.orders.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationalTaskHooks {

    @Autowired
    private StateExecutorService stateExecutorService;

    @Autowired
    private OperationalTasksService operationalTasksService;

    @Autowired
    private ParameterService parameterService;

    public void onCopy(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        setInitialState(operationalTask);
    }

    public void onCreate(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        setInitialState(operationalTask);
    }

    public void onSave(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        fillNameAndDescription(operationalTask);
        fillProductionLine(operationalTask);
        fillWorkstation(operationalTask);
        changeDateInOrder(operationalTask);

    }

    private void fillWorkstation(Entity operationalTask) {
        if (Objects.nonNull(operationalTask.getId())) {
            return;
        }

        String type = operationalTask.getStringField(OperationalTaskFields.TYPE);

        if (operationalTasksService.isOperationalTaskTypeExecutionOperationInOrder(type)) {
            Entity technologyOperationComponent = operationalTask
                    .getBelongsToField(OperationalTaskFields.TECHNOLOGY_OPERATION_COMPONENT);
            List<Entity> workstations = technologyOperationComponent
                    .getHasManyField(TechnologyOperationComponentFields.WORKSTATIONS);
            if (workstations.size() == 1) {
                Entity workstation = workstations.get(0);
                operationalTask.setField(OperationalTaskFields.WORKSTATION, workstation);
                if (Objects.nonNull(workstation.getBelongsToField(WorkstationFields.STAFF))) {
                    operationalTask.setField(OperationalTaskFields.STAFF, workstation.getBelongsToField(WorkstationFields.STAFF));
                }
            }
        }

    }

    private void changeDateInOrder(Entity operationalTask) {
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
                if (!order.getDateField(OrderFields.START_DATE).equals(start)) {
                    changed = true;
                    order.setField(OrderFields.START_DATE, operationalTask.getDateField(OperationalTaskFields.START_DATE));
                }
                if (!order.getDateField(OrderFields.FINISH_DATE).equals(finish)) {
                    changed = true;
                    order.setField(OrderFields.FINISH_DATE, operationalTask.getDateField(OperationalTaskFields.FINISH_DATE));
                }
                if (changed) {
                    order.getDataDefinition().save(order);
                }
            }
        }
    }

    private void fillNameAndDescription(final Entity operationalTask) {
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

                if(Objects.isNull(operationalTask.getId())) {
                    boolean copyDescriptionFromProductionOrder = parameterService.getParameter().getBooleanField("otCopyDescriptionFromProductionOrder");
                    if (copyDescriptionFromProductionOrder) {

                        StringBuilder descriptionBuilder = new StringBuilder();
                        descriptionBuilder.append(Strings.nullToEmpty(technologyOperationComponent.getStringField(TechnologyOperationComponentFields.COMMENT)));
                        if (StringUtils.isNoneBlank(descriptionBuilder.toString())) {
                            descriptionBuilder.append("\n");
                        }
                        Entity order = operationalTask.getBelongsToField(OperationalTaskFields.ORDER);
                        if (Objects.nonNull(order)) {
                            descriptionBuilder.append(Strings.nullToEmpty(order.getStringField(OrderFields.DESCRIPTION)));
                        }
                        operationalTask.setField(OperationalTaskFields.DESCRIPTION, descriptionBuilder.toString());

                    } else {
                        operationalTask.setField(OperationalTaskFields.DESCRIPTION, technologyOperationComponent.getStringField(TechnologyOperationComponentFields.COMMENT));
                    }
                }
            }
        }
    }

    private void fillProductionLine(final Entity operationalTask) {
        String type = operationalTask.getStringField(OperationalTaskFields.TYPE);

        if (operationalTasksService.isOperationalTaskTypeExecutionOperationInOrder(type)) {
            Entity order = operationalTask.getBelongsToField(OperationalTaskFields.ORDER);

            if (order == null) {
                operationalTask.setField(OperationalTaskFields.PRODUCTION_LINE, null);
            } else {
                Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);

                operationalTask.setField(OperationalTaskFields.PRODUCTION_LINE, productionLine);
            }
        }
    }

    private void setInitialState(final Entity operationalTask) {
        stateExecutorService.buildInitial(OperationalTasksServiceMarker.class, operationalTask,
                OperationalTaskStateStringValues.PENDING);
    }
}
