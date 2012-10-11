package com.qcadoo.mes.deliveries.states.aop;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.states.constants.DeliveryStateChangeDescriber;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateChangePhase;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.aop.AbstractStateChangeAspect;

@Aspect
@Service
public class DeliveryStateChangeAspect extends AbstractStateChangeAspect {

    @Autowired
    private DeliveryStateChangeDescriber describer;

    public static final String SELECTOR_POINTCUT = "this(com.qcadoo.mes.deliveries.states.aop.DeliveryStateChangeAspect)";

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return describer;
    }

    @Override
    protected void changeStatePhase(final StateChangeContext stateChangeContext, final int phaseNumber) {
    }

    @Override
    protected int getNumOfPhases() {
        return DeliveryStateChangePhase.getNumOfPhases();
    }
}
