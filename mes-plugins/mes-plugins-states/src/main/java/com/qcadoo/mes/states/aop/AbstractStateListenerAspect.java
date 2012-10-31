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

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.mes.states.service.client.util.ViewContextHolder;
import com.qcadoo.model.api.Entity;

/**
 * This aspect provides XPI for state change advices (listeners).
 * 
 * @since 1.1.7
 */
@Aspect
public abstract class AbstractStateListenerAspect {

    protected static final String PHASE_EXECUTION_POINTCUT = "phaseExecution(stateChangeContext, phase)";

    protected static final String CHANGE_STATE_EXECUTION_POINTCUT = "phaseExecution(stateChangeContext)";

    /**
     * Pointcut for execution state change phase
     * 
     * @param stateChangeEntity
     * @param phase
     */
    @Pointcut("execution(* *.changeStatePhase(..)) && args(stateChangeContext, phase, ..) && targetServicePointcut()")
    public void phaseExecution(final StateChangeContext stateChangeContext, final int phase) {
    }

    /**
     * Pointcut for changing state join points ({@link StateChangeService#changeState(Entity)}) using additional restrictions from
     * {@link AbstractStateListenerAspect#targetServicePointcut()} pointcut.
     * 
     * @param stateChangeEntity
     *            entity which represent state change flow
     * @param annotation
     */
    @Pointcut("execution(public void com.qcadoo.mes.states.service.StateChangeService+.changeState(..)) "
            + "&& args(stateChangeContext) && targetServicePointcut()")
    public void changeStateExecution(final StateChangeContext stateChangeContext) {
    }

    @Pointcut("execution(public void com.qcadoo.mes.states.service.client.StateChangeViewClient.changeState(..)) && args(viewContext,..)")
    public void viewClientExecution(final ViewContextHolder viewContext) {
    }

    /**
     * Select {@link StateChangeService} to be woven with this listener. Usually pointcut expression looks like "this(TypeName)"
     */
    @Pointcut
    protected abstract void targetServicePointcut();

}
