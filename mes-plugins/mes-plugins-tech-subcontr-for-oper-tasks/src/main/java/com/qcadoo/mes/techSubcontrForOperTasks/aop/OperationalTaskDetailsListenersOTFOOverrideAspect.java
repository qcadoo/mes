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
package com.qcadoo.mes.techSubcontrForOperTasks.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.techSubcontrForOperTasks.constants.TechSubcontrForOperTasksConstants;
import com.qcadoo.plugin.api.RunIfEnabled;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Aspect
@Configurable
@RunIfEnabled(TechSubcontrForOperTasksConstants.PLUGIN_IDENTIFIER)
public class OperationalTaskDetailsListenersOTFOOverrideAspect {

    @Autowired
    private OperationalTaskDetailsListenersOTFOOverrideUtil operationalTaskDetailsListenersOTFOOverrideUtil;

    @Pointcut("execution(public void com.qcadoo.mes.operationalTasksForOrders.listeners.OperationalTaskDetailsListenersOTFO.setOperationalTaskNameAndDescription(..)) "
            + "&& args(view, state, args)")
    public void setOperationalTaskNameAndDescriptionExecution(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
    }

    @After("setOperationalTaskNameAndDescriptionExecution(view, state, args)")
    public void afterSetOperationalTaskNameAndDescriptionExecution(final ProceedingJoinPoint pjp, final ViewDefinitionState view,
            final ComponentState state, final String[] args) throws Throwable {
        operationalTaskDetailsListenersOTFOOverrideUtil.setOperationalTaskNameDescriptionAndProductionLineForSubcontracted(view);
    }

}
