package com.qcadoo.mes.operationalTasks.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.newstates.BasicStateService;
import com.qcadoo.mes.operationalTasks.constants.OperationalTaskFields;
import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.ScheduleFields;
import com.qcadoo.mes.orders.constants.SchedulePositionFields;
import com.qcadoo.mes.orders.constants.ScheduleStateChangeFields;
import com.qcadoo.mes.orders.states.ScheduleServiceMarker;
import com.qcadoo.mes.orders.states.ScheduleStateChangeDescriber;
import com.qcadoo.mes.orders.states.constants.ScheduleStateStringValues;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
@RunIfEnabled(OperationalTasksConstants.PLUGIN_IDENTIFIER)
public class ScheduleStateServiceOT extends BasicStateService implements ScheduleServiceMarker {

    @Autowired
    private ScheduleStateChangeDescriber scheduleStateChangeDescriber;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return scheduleStateChangeDescriber;
    }

    @Override
    public Entity onBeforeSave(Entity entity, String sourceState, String targetState, Entity stateChangeEntity,
                              StateChangeEntityDescriber describer) {
        switch (targetState) {
            case ScheduleStateStringValues.APPROVED:
                entity.setField(ScheduleFields.APPROVE_TIME,
                        stateChangeEntity.getDateField(ScheduleStateChangeFields.DATE_AND_TIME));
        }

        return entity;
    }

    @Override
    public Entity onAfterSave(Entity entity, String sourceState, String targetState, Entity stateChangeEntity,
            StateChangeEntityDescriber describer) {
        switch (targetState) {
            case ScheduleStateStringValues.APPROVED:
                generateOperationalTasks(entity);
                break;

            case ScheduleStateStringValues.REJECTED:
                if (ScheduleStateStringValues.APPROVED.equals(sourceState)) {
                    // TODO KASI reject operationalTasks
                }
        }

        return entity;
    }

    private void generateOperationalTasks(Entity schedule) {
        // TODO KASI change fields and enum constants
        DataDefinition operationalTaskDD = dataDefinitionService.get(OperationalTasksConstants.PLUGIN_IDENTIFIER,
                OperationalTasksConstants.MODEL_OPERATIONAL_TASK);
        for (Entity position : schedule.getHasManyField(ScheduleFields.POSITIONS)) {
            Entity operationalTask = operationalTaskDD.create();
            operationalTask.setField(OperationalTaskFields.NUMBER, numberGeneratorService.generateNumber(
                    OperationalTasksConstants.PLUGIN_IDENTIFIER, OperationalTasksConstants.MODEL_OPERATIONAL_TASK));
            operationalTask.setField(OperationalTaskFields.START_DATE, position.getField(SchedulePositionFields.START_TIME));
            operationalTask.setField(OperationalTaskFields.FINISH_DATE, position.getField(SchedulePositionFields.END_TIME));
            operationalTask.setField(OperationalTaskFields.TYPE, "02executionOperationInOrder");
            Entity order = position.getBelongsToField(SchedulePositionFields.ORDER);
            operationalTask.setField("order", order);
            Entity technologyOperationComponent = position
                    .getBelongsToField(SchedulePositionFields.TECHNOLOGY_OPERATION_COMPONENT);
            if (!technologyOperationComponent.getBooleanField("isSubcontracting")) {
                operationalTask.setField(OperationalTaskFields.PRODUCTION_LINE,
                        order.getBelongsToField(OrderFields.PRODUCTION_LINE));
            }

            operationalTask.setField("technologyOperationComponent", technologyOperationComponent);

            operationalTask.setField(OperationalTaskFields.WORKSTATION, position.getField(SchedulePositionFields.WORKSTATION));
            operationalTask.setField(OperationalTaskFields.STAFF, position.getField(SchedulePositionFields.STAFF));
            operationalTask.setField("product", position.getField(SchedulePositionFields.PRODUCT));
            operationalTask.setField("plannedQuantity", position.getField(SchedulePositionFields.QUANTITY));

            operationalTaskDD.save(operationalTask);
        }
        schedule.addGlobalMessage("productionScheduling.operationDurationDetailsInOrder.info.operationalTasksCreated");
    }
}
