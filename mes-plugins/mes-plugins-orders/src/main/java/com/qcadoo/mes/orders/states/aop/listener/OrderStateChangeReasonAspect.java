/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.orders.states.aop.listener;

import static com.qcadoo.mes.orders.states.constants.OrderStateStringValues.ABANDONED;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.orders.OrderStateChangeReasonService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.orders.states.constants.OrderStateChangePhase;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.mes.states.service.client.util.ViewContextHolder;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(OrdersConstants.PLUGIN_IDENTIFIER)
public class OrderStateChangeReasonAspect extends AbstractStateListenerAspect {

    @Autowired
    private OrderStateChangeReasonService stateChangeReasonService;

    @Pointcut(OrderStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }

    @RunForStateTransition(targetState = ABANDONED)
    @RunInPhase(OrderStateChangePhase.FILL_REASON)
    @Before("phaseExecution(stateChangeContext, phase) && cflow(viewClientExecution(viewContext))")
    public void askForAbandonReason(final StateChangeContext stateChangeContext, final int phase,
            final ViewContextHolder viewContext) {
        if (stateChangeReasonService.neededForAbandon()) {
            stateChangeReasonService.showReasonForm(stateChangeContext, viewContext);
        }
    }

    @RunForStateTransition(targetState = OrderStateStringValues.INTERRUPTED)
    @RunInPhase(OrderStateChangePhase.FILL_REASON)
    @Before("phaseExecution(stateChangeContext, phase) && cflow(viewClientExecution(viewContext))")
    public void askForInterruptReason(final StateChangeContext stateChangeContext, final int phase,
            final ViewContextHolder viewContext) {
        if (stateChangeReasonService.neededForInterrupt()) {
            stateChangeReasonService.showReasonForm(stateChangeContext, viewContext);
        }
    }

    @RunForStateTransition(targetState = OrderStateStringValues.DECLINED)
    @RunInPhase(OrderStateChangePhase.FILL_REASON)
    @Before("phaseExecution(stateChangeContext, phase) && cflow(viewClientExecution(viewContext))")
    public void askForDeclineReason(final StateChangeContext stateChangeContext, final int phase,
            final ViewContextHolder viewContext) {
        if (stateChangeReasonService.neededForDecline()) {
            stateChangeReasonService.showReasonForm(stateChangeContext, viewContext);
        }
    }

    @RunForStateTransition(targetState = OrderStateStringValues.COMPLETED)
    @RunInPhase(OrderStateChangePhase.FILL_REASON)
    @Before("phaseExecution(stateChangeContext, phase) && cflow(viewClientExecution(viewContext))")
    public void askForCompletionTimeDifferenceReason(final StateChangeContext stateChangeContext, final int phase,
            final ViewContextHolder viewContext) {
        stateChangeReasonService.onComplete(stateChangeContext, viewContext);
    }

    @RunForStateTransition(sourceState = OrderStateStringValues.ACCEPTED, targetState = OrderStateStringValues.IN_PROGRESS)
    @RunInPhase(OrderStateChangePhase.FILL_REASON)
    @Before("phaseExecution(stateChangeContext, phase) && cflow(viewClientExecution(viewContext))")
    public void askForStartProgressTimeDifferenceReason(final StateChangeContext stateChangeContext, final int phase,
            final ViewContextHolder viewContext) {
        stateChangeReasonService.onStart(stateChangeContext, viewContext);
    }

}
