package com.qcadoo.mes.states.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareError;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.service.StateChangePhaseUtil;

@Aspect
public class StateChangePhaseAspect {

    @DeclareError("(execution(@com.qcadoo.mes.states.annotation.StateChangePhase * *.*(!com.qcadoo.mes.states.StateChangeContext,..)) "
            + "|| execution(@com.qcadoo.mes.states.annotation.StateChangePhase * *.*()))")
    public static final String ERROR = "Only methods with state change context as a first argument can be annotated using @StateChangePhase";

    @Around("(execution(@com.qcadoo.mes.states.annotation.StateChangePhase * *.*(..)) "
            + "|| execution(public void com.qcadoo.mes.states.service.StateChangeService.changeState(..)) "
            + "|| execution(public void com.qcadoo.mes.states.service.StateChangeService.changeStatePhase(..))) "
            + "&& args(stateChangeContext,..) && within(com.qcadoo.mes.states.service.StateChangeService+)")
    public Object omitExecutionIfStateChangeEntityHasErrors(final ProceedingJoinPoint pjp,
            final StateChangeContext stateChangeContext) throws Throwable {
        Object result = null;
        if (StateChangePhaseUtil.canRun(stateChangeContext)) {
            result = pjp.proceed(pjp.getArgs());
        }
        return result;
    }

}
