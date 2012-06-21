package com.qcadoo.mes.technologies.states.aop;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.aop.AbstractStateChangeAspect;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.TechnologyStateChangeDescriber;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateChangePhase;

@Aspect
@Service
public class TechnologyStateChangeAspect extends AbstractStateChangeAspect {

    @Autowired
    private TechnologyStateChangeDescriber technologyStateChangeDescriber;

    public static final String SELECTOR_POINTCUT = "this(com.qcadoo.mes.technologies.states.aop.TechnologyStateChangeAspect)";

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return technologyStateChangeDescriber;
    }

    @Override
    protected String getStateFieldName() {
        return TechnologyFields.STATE;
    }

    @Override
    protected void changeStatePhase(final StateChangeContext stateChangeContext, final int phaseNumber) {
    }

    @Override
    protected int getNumOfPhases() {
        return TechnologyStateChangePhase.getNumOfPhases();
    }

}
