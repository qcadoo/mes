package com.qcadoo.mes.orders.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.newstates.BasicStateService;
import com.qcadoo.mes.states.StateChangeEntityDescriber;

@Service
public class OperationalTaskStateService extends BasicStateService implements OperationalTasksServiceMarker {

    @Autowired
    private OperationalTaskStateChangeDescriber operationalTaskStateChangeDescriber;

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return operationalTaskStateChangeDescriber;
    }
}
