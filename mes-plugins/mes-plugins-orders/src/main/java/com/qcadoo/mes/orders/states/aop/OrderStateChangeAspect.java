package com.qcadoo.mes.orders.states.aop;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.states.OrderStateChangeDescriber;
import com.qcadoo.mes.orders.states.constants.OrderStateChangePhase;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.aop.AbstractStateChangeAspect;

@Aspect
@Service
public class OrderStateChangeAspect extends AbstractStateChangeAspect {

    @Autowired
    private OrderStateChangeDescriber describer;

    public static final String SELECTOR_POINTCUT = "this(com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect)";

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return describer;
    }

    @Override
    protected void changeStatePhase(final StateChangeContext stateChangeContext, final int phaseNumber) {
    }

    @Override
    protected int getNumOfPhases() {
        return OrderStateChangePhase.getNumOfPhases();
    }

}
