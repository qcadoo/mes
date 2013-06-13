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

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.TechnologyStateChangeListenerService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.aop.TechnologyStateChangeAspect;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateChangePhase;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(OrdersConstants.PLUGIN_IDENTIFIER)
public class TechnologyStateChangeListenerAspect extends AbstractStateListenerAspect {

    @Autowired
    private TechnologyStateChangeListenerService technologyStateChangeListener;

    @Pointcut(TechnologyStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }

    @RunInPhase(TechnologyStateChangePhase.LAST)
    @RunForStateTransition(targetState = TechnologyStateStringValues.CHECKED)
    @After(PHASE_EXECUTION_POINTCUT)
    public void afterChangeToChecked(final StateChangeContext stateChangeContext, final int phase) {
        Entity technology = stateChangeContext.getOwner();
        if (!StringUtils.isNotBlank(technology.getStringField(TechnologyFields.TECHNOLOGY_TYPE))) {
            stateChangeContext.addMessage("orders.order.technology.info.aboutChecked", StateMessageType.INFO, false);
        }

    }

}
