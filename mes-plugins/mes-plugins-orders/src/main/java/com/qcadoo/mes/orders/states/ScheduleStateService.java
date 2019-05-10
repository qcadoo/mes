package com.qcadoo.mes.orders.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.newstates.BasicStateService;
import com.qcadoo.mes.states.StateChangeEntityDescriber;

@Service
public class ScheduleStateService extends BasicStateService implements ScheduleServiceMarker {

    @Autowired
    private ScheduleStateChangeDescriber scheduleStateChangeDescriber;

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return scheduleStateChangeDescriber;
    }

}
