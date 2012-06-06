package com.qcadoo.mes.orders.states.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.aop.AbstractStateChangeAspect;

@Aspect
@Service
public class OrderStateChangeService extends AbstractStateChangeAspect {

    private final StateChangeEntityDescriber describer = new OrderStateChangeDescriber();

    @Override
    protected String getStateFieldName() {
        return "state";
    }

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return describer;
    }

    @Pointcut("this(OrderStateChangeService)")
    public void stateChangeServiceSelector() {
    }

}
