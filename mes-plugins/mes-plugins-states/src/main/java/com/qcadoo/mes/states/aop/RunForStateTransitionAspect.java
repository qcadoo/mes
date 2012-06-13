package com.qcadoo.mes.states.aop;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunForStateTransitions;
import com.qcadoo.model.api.Entity;

@Aspect
public class RunForStateTransitionAspect {

    public static final String STATE_WILDCARD = "*";

    @Pointcut("(adviceexecution() || execution(* *(..))) && args(stateChangeEntity,..) && !within(RunForStateTransitionAspect) && this(listener)")
    public void stateChangeListenerExecution(final Entity stateChangeEntity, final AbstractStateListenerAspect listener) {
    }

    @Around("stateChangeListenerExecution(stateChangeEntity, listener) && @annotation(annotation) "
            + "&& !@annotation(com.qcadoo.mes.states.annotation.RunForStateTransitions)")
    public Object runOnlyIfMatchSpecifiedTransition(final ProceedingJoinPoint pjp, final Entity stateChangeEntity,
            final AbstractStateListenerAspect listener, final RunForStateTransition annotation) throws Throwable {
        final StateChangeEntityDescriber describer = listener.getStateChangeEntityDescriber();
        return runOnlyIfMatchAtLeastOneSpecifiedTransitions(pjp, stateChangeEntity, describer,
                new RunForStateTransition[] { annotation });
    }

    @Around("stateChangeListenerExecution(stateChangeEntity, listener) && @annotation(annotation)")
    public Object runOnlyIfMatchSpecifiedTransitions(final ProceedingJoinPoint pjp, final Entity stateChangeEntity,
            final AbstractStateListenerAspect listener, final RunForStateTransitions annotation) throws Throwable {
        final StateChangeEntityDescriber describer = listener.getStateChangeEntityDescriber();
        return runOnlyIfMatchAtLeastOneSpecifiedTransitions(pjp, stateChangeEntity, describer, annotation.value());
    }

    private Object runOnlyIfMatchAtLeastOneSpecifiedTransitions(final ProceedingJoinPoint pjp, final Entity stateChangeEntity,
            final StateChangeEntityDescriber describer, final RunForStateTransition[] transitions) throws Throwable {
        Object returnValue = null;
        if (stateChangeMatchAnyTransition(stateChangeEntity, describer, transitions)) {
            returnValue = pjp.proceed();
        }
        return returnValue;
    }

    private boolean stateChangeMatchAnyTransition(final Entity stateChangeEntity, final StateChangeEntityDescriber describer,
            final RunForStateTransition[] transitions) {
        final String givenSource = stateChangeEntity.getStringField(describer.getSourceStateFieldName());
        final String givenTarget = stateChangeEntity.getStringField(describer.getTargetStateFieldName());
        for (RunForStateTransition transition : transitions) {
            final String expectedSource = transition.sourceState();
            final String expectedTarget = transition.targetState();
            if (matchTransition(expectedSource, givenSource) && matchTransition(expectedTarget, givenTarget)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchTransition(final String expected, final String given) {
        return STATE_WILDCARD.equals(expected) || (StringUtils.isBlank(expected) && StringUtils.isBlank(given))
                || expected.equalsIgnoreCase(given);
    }

}
