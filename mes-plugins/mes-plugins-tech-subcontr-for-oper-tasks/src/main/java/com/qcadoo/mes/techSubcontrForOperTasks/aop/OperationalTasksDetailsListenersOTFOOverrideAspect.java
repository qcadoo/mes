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
package com.qcadoo.mes.techSubcontrForOperTasks.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Aspect
@Configurable
public class OperationalTasksDetailsListenersOTFOOverrideAspect {

    @Autowired
    private OperationalTasksDetailsListenersOTFOOverrideUtil tasksDetailsListenersOTFOOverrideUtil;

    @Pointcut("execution(public void com.qcadoo.mes.operationalTasksForOrders.listeners.OperationalTasksDetailsListenersOTFO.setProductionLineFromOrderAndClearOperation(..)) "
            + "&& args(viewDefinitionState, state, args)")
    public void setProductionLineFromOrderListenerExecution(final ViewDefinitionState viewDefinitionState,
            final ComponentState state, final String[] args) {
    }

    @Around("setProductionLineFromOrderListenerExecution(viewDefinitionState, state, args)")
    public void aroundSetProductionLineFromOrderListenerExecution(final ProceedingJoinPoint pjp,
            final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) throws Throwable {
        if (PluginUtils.isEnabled("techSubcontracting")) {
            tasksDetailsListenersOTFOOverrideUtil.checkIfOperationIsSubcontracted(viewDefinitionState);
        } else {
            pjp.proceed();
        }
    }

    @Pointcut("execution(public void com.qcadoo.mes.operationalTasksForOrders.listeners.OperationalTasksDetailsListenersOTFO.setOperationalNameAndDescription(..)) "
            + "&& args(viewDefinitionState, state, args)")
    public void setOperationalNameAndDescriptionListenerExecution(final ViewDefinitionState viewDefinitionState,
            final ComponentState state, final String[] args) {
    }

    @Around("setOperationalNameAndDescriptionListenerExecution(viewDefinitionState, state, args)")
    public void aroundSetOperationalNameAndDescriptionListenerExecution(final ProceedingJoinPoint pjp,
            final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) throws Throwable {
        if (PluginUtils.isEnabled("techSubcontracting")) {
            tasksDetailsListenersOTFOOverrideUtil.setOperationalNameAndDescriptionForSubcontractedOperation(viewDefinitionState);
        } else {
            pjp.proceed();
        }
    }

}
