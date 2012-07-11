package com.qcadoo.mes.assignmentToShift.states.aop;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftStateChangeDescriber;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftStateChangePhase;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.aop.AbstractStateChangeAspect;

@Aspect
@Service
public class AssignmentToShiftStateChangeAspect extends AbstractStateChangeAspect {

    @Autowired
    private AssignmentToShiftStateChangeDescriber describer;

    public static final String SELECTOR_POINTCUT = "this(com.qcadoo.mes.assignmentToShift.states.aop.AssignmentToShiftStateChangeAspect)";

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return describer;
    }

    @Override
    protected void changeStatePhase(final StateChangeContext stateChangeContext, final int phaseNumber) {
    }

    @Override
    protected int getNumOfPhases() {
        return AssignmentToShiftStateChangePhase.getNumOfPhases();
    }

}
