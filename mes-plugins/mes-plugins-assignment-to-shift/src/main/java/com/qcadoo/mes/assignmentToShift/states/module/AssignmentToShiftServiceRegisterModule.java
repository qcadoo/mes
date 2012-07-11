package com.qcadoo.mes.assignmentToShift.states.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.states.aop.AssignmentToShiftStateChangeAspect;
import com.qcadoo.mes.states.module.AbstractStateServiceRegisterModule;
import com.qcadoo.mes.states.service.StateChangeService;

@Service
public final class AssignmentToShiftServiceRegisterModule extends AbstractStateServiceRegisterModule {

    @Autowired
    private AssignmentToShiftStateChangeAspect assignmentToShiftStateChangeAspect;

    @Override
    protected StateChangeService getStateChangeService() {
        return assignmentToShiftStateChangeAspect;
    }

}
