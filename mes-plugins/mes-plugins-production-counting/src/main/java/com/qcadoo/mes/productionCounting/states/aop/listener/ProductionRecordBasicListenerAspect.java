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
package com.qcadoo.mes.productionCounting.states.aop.listener;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.states.aop.ProductionRecordStateChangeAspect;
import com.qcadoo.mes.productionCounting.states.constants.ProductionRecordStateChangePhase;
import com.qcadoo.mes.productionCounting.states.constants.ProductionRecordStateStringValues;
import com.qcadoo.mes.productionCounting.states.listener.ProductionRecordBasicListenerService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(ProductionCountingConstants.PLUGIN_IDENTIFIER)
public class ProductionRecordBasicListenerAspect extends AbstractStateListenerAspect {

    @Autowired
    private ProductionRecordBasicListenerService productionRecordBasicListenerService;

    @Pointcut(ProductionRecordStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }

    @RunInPhase(ProductionRecordStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(targetState = ProductionRecordStateStringValues.ACCEPTED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void validationOnAccept(final StateChangeContext stateChangeContext, final int phase) {
        productionRecordBasicListenerService.validationOnAccept(stateChangeContext);
    }

    @RunInPhase(ProductionRecordStateChangePhase.LAST)
    @RunForStateTransition(targetState = ProductionRecordStateStringValues.ACCEPTED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onAccept(final StateChangeContext stateChangeContext, final int phase) {
        productionRecordBasicListenerService.onAccept(stateChangeContext);
    }

    @RunInPhase(ProductionRecordStateChangePhase.DEFAULT)
    @RunForStateTransition(sourceState = ProductionRecordStateStringValues.ACCEPTED, targetState = ProductionRecordStateStringValues.DECLINED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onChangeFromAcceptedToDecline(final StateChangeContext stateChangeContext, final int phase) {
        productionRecordBasicListenerService.onChangeFromAcceptedToDeclined(stateChangeContext);
    }

    @RunInPhase(ProductionRecordStateChangePhase.DEFAULT)
    @RunForStateTransition(sourceState = ProductionRecordStateStringValues.DRAFT)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onChangeFromDraftToAny(final StateChangeContext stateChangeContext, final int phase) {
        productionRecordBasicListenerService.onLeavingDraft(stateChangeContext);
    }

}
