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
