package com.qcadoo.mes.states.aop;

import org.apache.commons.lang.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.qcadoo.mes.states.annotation.RunInPhase;

@Aspect
public class RunInPhaseAspect {

    @Pointcut("(adviceexecution() || execution(* *(..))) && args(com.qcadoo.model.api.Entity,currentPhase,..) && !within(RunInPhaseAspect)")
    public void stateChangeListenerExecution(final int currentPhase) {
    }

    @Around("stateChangeListenerExecution(currentPhase) && @annotation(annotation)")
    public Object runInPhaseMethodLevelAnnotated(final ProceedingJoinPoint pjp, final int currentPhase,
            final RunInPhase annotation) throws Throwable {
        return runInPhase(pjp, currentPhase, annotation);
    }

    @Around("stateChangeListenerExecution(currentPhase) && @within(annotation)")
    public Object runInPhaseClassLevelAnnotated(final ProceedingJoinPoint pjp, final int currentPhase, final RunInPhase annotation)
            throws Throwable {
        return runInPhase(pjp, currentPhase, annotation);
    }

    private Object runInPhase(final ProceedingJoinPoint pjp, final int currentPhase, final RunInPhase annotation)
            throws Throwable {
        Object result = null;
        if (ArrayUtils.contains(annotation.value(), currentPhase)) {
            result = pjp.proceed();
        }
        return result;
    }

}
