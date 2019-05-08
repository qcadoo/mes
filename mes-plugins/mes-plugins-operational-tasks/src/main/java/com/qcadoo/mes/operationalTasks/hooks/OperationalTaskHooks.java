package com.qcadoo.mes.operationalTasks.hooks;

import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.mes.operationalTasks.states.OperationalTasksServiceMarker;
import com.qcadoo.mes.operationalTasks.states.constants.OperationalTaskStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationalTaskHooks {

    @Autowired
    private StateExecutorService stateExecutorService;

    public void onCreate(final DataDefinition operationalTaskDD, final Entity operationalTask) {
        setInitialState(operationalTask);
    }

    private void setInitialState(final Entity operationalTask) {
        stateExecutorService.buildInitial(OperationalTasksServiceMarker.class, operationalTask, OperationalTaskStateStringValues.PENDING);
    }
}
