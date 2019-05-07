package com.qcadoo.mes.operationalTasksForOrders.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.operationalTasks.constants.OperationalTaskFields;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasksForOrders.OperationalTasksForOrdersService;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskFieldsOTFO;
import com.qcadoo.mes.operationalTasksForOrders.constants.OperationalTaskTypeOTFO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.ScheduleFields;
import com.qcadoo.mes.orders.constants.SchedulePositionFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class ScheduleDetailsListenersOTFO {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private OperationalTasksForOrdersService operationalTasksForOrdersService;

    public void generateOperationalTasks(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent scheduleForm = (FormComponent) state;

        Long scheduleId = scheduleForm.getEntityId();

        if (scheduleId == null) {
            return;
        }

        Entity schedule = scheduleForm.getEntity().getDataDefinition().get(scheduleId);
        DataDefinition operationalTaskDD = dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                OperationalTasksConstants.MODEL_OPERATIONAL_TASK);
        for (Entity position : schedule.getHasManyField(ScheduleFields.POSITIONS)) {
            Entity operationalTask = operationalTaskDD.create();
            operationalTask.setField(OperationalTaskFields.NUMBER, numberGeneratorService.generateNumber(
                    OperationalTasksConstants.PLUGIN_IDENTIFIER, OperationalTasksConstants.MODEL_OPERATIONAL_TASK));
            operationalTask.setField(OperationalTaskFields.START_DATE, position.getField(SchedulePositionFields.START_TIME));
            operationalTask.setField(OperationalTaskFields.FINISH_DATE, position.getField(SchedulePositionFields.END_TIME));
            operationalTask.setField(OperationalTaskFields.TYPE,
                    OperationalTaskTypeOTFO.EXECUTION_OPERATION_IN_ORDER.getStringValue());
            Entity order = position.getBelongsToField(SchedulePositionFields.ORDER);
            operationalTask.setField(OperationalTaskFieldsOTFO.ORDER, order);
            Entity technologyOperationComponent = position
                    .getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT);
            if (!technologyOperationComponent.getBooleanField("isSubcontracting")) {
                operationalTask.setField(OperationalTaskFields.PRODUCTION_LINE,
                        order.getBelongsToField(OrderFields.PRODUCTION_LINE));
            }

            operationalTask.setField(OperationalTaskFieldsOTFO.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent);

            operationalTask.setField(OperationalTaskFields.WORKSTATION, position.getField(SchedulePositionFields.WORKSTATION));
            operationalTask.setField(OperationalTaskFields.STAFF, position.getField(SchedulePositionFields.STAFF));

            operationalTask = operationalTaskDD.save(operationalTask);
            if (operationalTask.isValid()) {
                operationalTask.setField(OperationalTaskFieldsOTFO.TECH_OPER_COMP_OPERATIONAL_TASK,
                        operationalTasksForOrdersService.createTechOperCompOperationalTask(technologyOperationComponent));
            }
        }
        scheduleForm.addMessage("productionScheduling.operationDurationDetailsInOrder.info.operationalTasksCreated",
                ComponentState.MessageType.SUCCESS);
    }
}
