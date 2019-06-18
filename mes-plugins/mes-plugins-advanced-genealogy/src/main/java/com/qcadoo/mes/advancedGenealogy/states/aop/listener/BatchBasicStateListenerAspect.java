/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.advancedGenealogy.states.aop.listener;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.mes.advancedGenealogy.states.aop.BatchStateChangeAspect;
import com.qcadoo.mes.advancedGenealogy.states.constants.BatchStateChangePhase;
import com.qcadoo.mes.advancedGenealogy.states.constants.BatchStateStringValues;
import com.qcadoo.mes.advancedGenealogy.states.listener.BatchBasicStateListenerService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(AdvancedGenealogyConstants.PLUGIN_IDENTIFIER)
public class BatchBasicStateListenerAspect extends AbstractStateListenerAspect {

    @Autowired
    private BatchBasicStateListenerService basicStateListenerService;

    @Pointcut(BatchStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }

    @RunInPhase(BatchStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(targetState = BatchStateStringValues.BLOCKED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onBlock(final StateChangeContext stateChangeContext, final int phase) {
        basicStateListenerService.checkIfOccursAsProducedBatch(stateChangeContext);
        basicStateListenerService.checkIfOccursAsUsedBatch(stateChangeContext);
    }

}
