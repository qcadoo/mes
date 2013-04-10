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
package com.qcadoo.mes.basicProductionCounting.aop;

import static com.qcadoo.mes.orders.states.constants.OrderStateChangePhase.DEFAULT;
import static com.qcadoo.mes.orders.states.constants.OrderStateStringValues.ACCEPTED;
import static com.qcadoo.mes.states.aop.RunForStateTransitionAspect.WILDCARD_STATE;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.basicProductionCounting.BpcOrderStateListenerService;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(BasicProductionCountingConstants.PLUGIN_IDENTIFIER)
public class BpcOrderStateListenerAspect extends AbstractStateListenerAspect {

    @Autowired
    private BpcOrderStateListenerService listenerService;

    @RunInPhase(DEFAULT)
    @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = ACCEPTED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onAccept(final StateChangeContext stateChangeContext, final int phase) {
        listenerService.onAccept(stateChangeContext);
    }

    @Pointcut(OrderStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }

}