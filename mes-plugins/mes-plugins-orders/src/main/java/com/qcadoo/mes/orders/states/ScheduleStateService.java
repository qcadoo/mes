package com.qcadoo.mes.orders.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.newstates.BasicStateService;
import com.qcadoo.mes.orders.states.constants.ScheduleStateStringValues;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.model.api.Entity;

@Service
public class ScheduleStateService extends BasicStateService implements ScheduleServiceMarker {

    @Autowired
    private ScheduleStateChangeDescriber scheduleStateChangeDescriber;

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return scheduleStateChangeDescriber;
    }

    @Override
    public Entity onAfterSave(Entity entity, String sourceState, String targetState, Entity stateChangeEntity,
            StateChangeEntityDescriber describer) {
        switch (targetState) {
            case ScheduleStateStringValues.APPROVED:
                break;

            case ScheduleStateStringValues.REJECTED:
                if (ScheduleStateStringValues.APPROVED.equals(sourceState)) {
                }
                break;
        }

        return entity;
    }
}
