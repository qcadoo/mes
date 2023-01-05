package com.qcadoo.mes.technologies.states;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.MachineWorkingPeriodFields;
import com.qcadoo.mes.basic.constants.UserFieldsB;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.basic.hooks.MachineWorkingPeriodHooks;
import com.qcadoo.mes.basic.states.constants.WorkstationStateStringValues;
import com.qcadoo.mes.newstates.BasicStateService;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.UserService;

@Service
public class WorkstationStateService extends BasicStateService implements WorkstationServiceMarker {

    @Autowired
    private WorkstationStateChangeDescriber workstationStateChangeDescriber;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private UserService userService;
    @Autowired
    private MachineWorkingPeriodHooks machineWorkingPeriodHooks;

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return workstationStateChangeDescriber;
    }

    @Override
    public Entity onValidate(Entity entity, String sourceState, String targetState, Entity stateChangeEntity,
                             StateChangeEntityDescriber describer) {
        switch (targetState) {
            case WorkstationStateStringValues.STOPPED:
                checkMachineWorkingPeriodsForStopped(entity);
                break;

            case WorkstationStateStringValues.LAUNCHED:
                checkMachineWorkingPeriodsForLaunched(entity);
        }

        return entity;
    }

    private void checkMachineWorkingPeriodsForLaunched(Entity entity) {
        if (entity.getHasManyField(WorkstationFields.MACHINE_WORKING_PERIODS).stream()
                .anyMatch(e -> e.getDateField(MachineWorkingPeriodFields.STOP_DATE) == null)) {
            entity.addGlobalError("basic.workstation.machineWorkingPeriods.withoutStopDate");
        }
    }

    private void checkMachineWorkingPeriodsForStopped(Entity entity) {
        if (entity.getHasManyField(WorkstationFields.MACHINE_WORKING_PERIODS).stream()
                .noneMatch(e -> e.getDateField(MachineWorkingPeriodFields.STOP_DATE) == null)) {
            entity.addGlobalError("basic.workstation.machineWorkingPeriods.withStopDate");
        }
    }

    @Override
    public Entity onBeforeSave(Entity entity, String sourceState, String targetState, Entity stateChangeEntity,
                               StateChangeEntityDescriber describer) {
        switch (targetState) {
            case WorkstationStateStringValues.STOPPED:
                updateStopDate(entity);
                break;

            case WorkstationStateStringValues.LAUNCHED:
                addMachineWorkingPeriod(entity);
        }

        return entity;
    }

    private void addMachineWorkingPeriod(Entity entity) {
        if (!entity.getBooleanField(WorkstationFields.MANUAL_STATE_CHANGE)) {
            DataDefinition machineWorkingPeriodDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                    BasicConstants.MODEL_MACHINE_WORKING_PERIOD);
            Entity machineWorkingPeriod = machineWorkingPeriodDD.create();
            machineWorkingPeriod.setField(MachineWorkingPeriodFields.LAUNCH_DATE, new Date());
            machineWorkingPeriod.setField(MachineWorkingPeriodFields.WORKSTATION, entity);
            machineWorkingPeriod.setField(MachineWorkingPeriodFields.WORKING_TIME, 0);
            machineWorkingPeriod.setField(MachineWorkingPeriodFields.LAUNCHED_BY, getStaff());
            machineWorkingPeriodDD.fastSave(machineWorkingPeriod);
        }
    }

    private void updateStopDate(Entity entity) {
        if (!entity.getBooleanField(WorkstationFields.MANUAL_STATE_CHANGE)) {
            List<Entity> machineWorkingPeriods = entity.getHasManyField(WorkstationFields.MACHINE_WORKING_PERIODS).stream()
                    .filter(e -> e.getDateField(MachineWorkingPeriodFields.STOP_DATE) == null).collect(Collectors.toList());
            for (Entity machineWorkingPeriod : machineWorkingPeriods) {
                machineWorkingPeriod.setField(MachineWorkingPeriodFields.STOP_DATE, new Date());
                machineWorkingPeriod.setField(MachineWorkingPeriodFields.STOPPED_BY, getStaff());
                machineWorkingPeriodHooks.onSave(null, machineWorkingPeriod);
                machineWorkingPeriod.getDataDefinition().fastSave(machineWorkingPeriod);
            }
        }
    }

    private Entity getStaff() {
        return userService.getCurrentUserEntity().getBelongsToField(UserFieldsB.STAFF);
    }
}
