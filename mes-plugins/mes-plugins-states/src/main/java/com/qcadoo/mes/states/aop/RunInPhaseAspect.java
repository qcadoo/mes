/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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

    @Around("StatesXpiAspect.listenerExecution() && @annotation(annotation) && args(*, currentPhase,..)")
    public Object runInPhaseMethodLevelAnnotated(final ProceedingJoinPoint pjp, final int currentPhase,
            final RunInPhase annotation) throws Throwable {
        return runInPhase(pjp, currentPhase, annotation);
    }

    @Around("StatesXpiAspect.listenerExecution() && @within(annotation) && !@annotation(com.qcadoo.mes.states.annotation.RunInPhase) && args(*, currentPhase,..)")
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
