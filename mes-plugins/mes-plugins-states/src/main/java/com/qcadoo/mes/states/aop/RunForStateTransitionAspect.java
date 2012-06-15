package com.qcadoo.mes.states.aop;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunForStateTransitions;

@Aspect
public class RunForStateTransitionAspect {

    public static final String WILDCARD_STATE = "*";

    @Around("StatesXpiAspect.listenerExecutionWithContext(stateChangeContext) && @annotation(annotation) "
            + "&& !@annotation(com.qcadoo.mes.states.annotation.RunForStateTransitions)")
    public Object runOnlyIfMatchSpecifiedTransition(final ProceedingJoinPoint pjp, final StateChangeContext stateChangeContext,
            final RunForStateTransition annotation) throws Throwable {
        return runOnlyIfMatchAtLeastOneSpecifiedTransitions(pjp, stateChangeContext, new RunForStateTransition[] { annotation });
    }

    @Around("StatesXpiAspect.listenerExecutionWithContext(stateChangeContext) && @annotation(annotation)")
    public Object runOnlyIfMatchSpecifiedTransitions(final ProceedingJoinPoint pjp, final StateChangeContext stateChangeContext,
            final RunForStateTransitions annotation) throws Throwable {
        return runOnlyIfMatchAtLeastOneSpecifiedTransitions(pjp, stateChangeContext, annotation.value());
    }

    private Object runOnlyIfMatchAtLeastOneSpecifiedTransitions(final ProceedingJoinPoint pjp,
            final StateChangeContext stateChangeContext, final RunForStateTransition[] transitions) throws Throwable {
        Object returnValue = null;
        if (stateChangeMatchAnyTransition(stateChangeContext, transitions)) {
            returnValue = pjp.proceed();
        }
        return returnValue;
    }

    private boolean stateChangeMatchAnyTransition(final StateChangeContext stateChangeContext,
            final RunForStateTransition[] transitions) {
        final StateChangeEntityDescriber describer = stateChangeContext.getDescriber();
        final String givenSource = stateChangeContext.getStateChangeEntity().getStringField(describer.getSourceStateFieldName());
        final String givenTarget = stateChangeContext.getStateChangeEntity().getStringField(describer.getTargetStateFieldName());
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
        return WILDCARD_STATE.equals(expected) || (StringUtils.isBlank(expected) && StringUtils.isBlank(given))
                || expected.equalsIgnoreCase(given);
    }

}
