/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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

import java.math.BigDecimal;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.OrderStateService;
import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.orders.states.constants.OrderStateChangePhase;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(OrdersConstants.PLUGIN_IDENTIFIER)
public class FillFieldsOnChangesStatesAspect extends AbstractStateListenerAspect {

    @Autowired
    private OrderStateService orderStateService;

    @RunInPhase(OrderStateChangePhase.LAST)
    @RunForStateTransition(sourceState = OrderStateStringValues.ACCEPTED, targetState = OrderStateStringValues.IN_PROGRESS)
    @After(PHASE_EXECUTION_POINTCUT)
    public void afterStartProgress(final StateChangeContext stateChangeContext, final int phase) {
        stateChangeContext.getOwner().setField(OrderFields.DONE_QUANTITY, BigDecimal.ZERO);
        orderStateService.checkOrderDates(stateChangeContext);
    }

    @RunInPhase(OrderStateChangePhase.LAST)
    @RunForStateTransition(targetState = OrderStateStringValues.COMPLETED)
    @After(PHASE_EXECUTION_POINTCUT)
    public void afterComplete(final StateChangeContext stateChangeContext, final int phase) {
    }

    @RunInPhase(OrderStateChangePhase.LAST)
    @RunForStateTransition(targetState = OrderStateStringValues.ABANDONED)
    @After(PHASE_EXECUTION_POINTCUT)
    public void afterAbandoning(final StateChangeContext stateChangeContext, final int phase) {
    }

    @Pointcut(OrderStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }

}
