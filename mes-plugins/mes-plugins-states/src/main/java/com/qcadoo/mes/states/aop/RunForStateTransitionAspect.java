/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.states.aop;

import org.apache.commons.lang3.StringUtils;
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
