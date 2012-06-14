package com.qcadoo.mes.states.aop;

import org.apache.commons.lang.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareWarning;

import com.qcadoo.mes.states.annotation.RunInPhase;

@Aspect
public class RunInPhaseAspect {

    @DeclareWarning("adviceexecution() && within(com.qcadoo.mes.states.aop.AbstractStateListenerAspect+) && (!@annotation(com.qcadoo.mes.states.annotation.RunInPhase) && !@within(com.qcadoo.mes.states.annotation.RunInPhase))")
    protected static final String LISTENER_WITHOUT_PHASE_WARNING = "State change listener method should be annotated with @RunInPhase annotation.";

    @Around("StatesXpi.listenerExecution() && @annotation(annotation) && args(*, currentPhase,..)")
    public Object runInPhaseMethodLevelAnnotated(final ProceedingJoinPoint pjp, final int currentPhase,
            final RunInPhase annotation) throws Throwable {
        return runInPhase(pjp, currentPhase, annotation);
    }

    @Around("StatesXpi.listenerExecution() && @within(annotation) && !@annotation(com.qcadoo.mes.states.annotation.RunInPhase) && args(*, currentPhase,..)")
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
