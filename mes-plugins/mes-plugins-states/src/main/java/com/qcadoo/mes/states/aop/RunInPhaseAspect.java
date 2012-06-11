package com.qcadoo.mes.states.aop;

import org.apache.commons.lang.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareWarning;
import org.aspectj.lang.annotation.Pointcut;

import com.qcadoo.mes.states.annotation.RunInPhase;

@Aspect
public class RunInPhaseAspect {

    @DeclareWarning("adviceexecution() && within(com.qcadoo.mes.states.aop.AbstractStateListenerAspect+) && (!@annotation(com.qcadoo.mes.states.annotation.RunInPhase) && !@within(com.qcadoo.mes.states.annotation.RunInPhase))")
    protected static final String LISTENER_WITHOUT_PHASE_WARNING = "State change listener should be annotated with @RunInPhase annotation.";

    @Pointcut("(adviceexecution() || execution(* *(..))) && args(com.qcadoo.model.api.Entity,currentPhase,..) && !within(RunInPhaseAspect)")
    public void stateChangeListenerExecution(final int currentPhase) {
    }

    @Around("stateChangeListenerExecution(currentPhase) && @annotation(annotation)")
    public Object runInPhaseMethodLevelAnnotated(final ProceedingJoinPoint pjp, final int currentPhase,
            final RunInPhase annotation) throws Throwable {
        return runInPhase(pjp, currentPhase, annotation);
    }

    @Around("stateChangeListenerExecution(currentPhase) && @within(annotation) && !@annotation(com.qcadoo.mes.states.annotation.RunInPhase)")
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
