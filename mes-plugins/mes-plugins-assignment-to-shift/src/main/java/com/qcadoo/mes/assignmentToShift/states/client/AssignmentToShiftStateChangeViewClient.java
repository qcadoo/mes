package com.qcadoo.mes.assignmentToShift.states.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.states.aop.AssignmentToShiftStateChangeAspect;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.mes.states.service.client.AbstractStateChangeViewClient;

@Service
public class AssignmentToShiftStateChangeViewClient extends AbstractStateChangeViewClient {

    @Autowired
    private AssignmentToShiftStateChangeAspect assignmentToShiftStateChangeAspect;

    @Override
    protected StateChangeService getStateChangeService() {
        return assignmentToShiftStateChangeAspect;
    }

}
