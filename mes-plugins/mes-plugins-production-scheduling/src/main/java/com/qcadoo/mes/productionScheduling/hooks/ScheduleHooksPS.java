package com.qcadoo.mes.productionScheduling.hooks;

import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.orders.states.constants.ScheduleStateStringValues;
import com.qcadoo.mes.productionScheduling.states.ScheduleServiceMarker;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduleHooksPS {

    @Autowired
    private StateExecutorService stateExecutorService;

    public void onCreate(final DataDefinition scheduleDD, final Entity schedule) {
        setInitialState(schedule);
    }

    public void onCopy(final DataDefinition scheduleDD, final Entity schedule) {
        setInitialState(schedule);
    }

    private void setInitialState(final Entity schedule) {
        stateExecutorService.buildInitial(ScheduleServiceMarker.class, schedule, ScheduleStateStringValues.DRAFT);
    }
}
