package com.qcadoo.mes.states.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.qcadoo.mes.states.StateChangeContext;

@Aspect
public class StatesXpi {

    @Pointcut("(adviceexecution() || execution(* *(..))) && args(com.qcadoo.mes.states.StateChangeContext,..) && within(com.qcadoo.mes.states.aop.AbstractStateListenerAspect+)")
    public void listenerExecution() {
    }

    @Pointcut("(adviceexecution() || execution(* *(..))) && args(stateChangeContext,..) && within(com.qcadoo.mes.states.aop.AbstractStateListenerAspect+)")
    public void listenerExecutionWithContext(final StateChangeContext stateChangeContext) {
    }
}
