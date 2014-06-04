/**
 * ***************************************************************************
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

import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.states.aop.ProductionTrackingStateChangeAspect;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateChangePhase;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.productionCounting.states.listener.ProductionTrackingListenerService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(ProductionCountingConstants.PLUGIN_IDENTIFIER)
public class ProductionTrackingListenerAspect extends AbstractStateListenerAspect {

    @Autowired
    private ProductionTrackingListenerService productionTrackingListenerService;

    @Pointcut(ProductionTrackingStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }


    @RunInPhase(ProductionTrackingStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(targetState = ProductionTrackingStateStringValues.ACCEPTED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void validationOnAccept(final StateChangeContext stateChangeContext, final int phase) {
        productionTrackingListenerService.validationOnAccept(stateChangeContext);
    }

    @RunInPhase(ProductionTrackingStateChangePhase.LAST)
    @RunForStateTransition(targetState = ProductionTrackingStateStringValues.ACCEPTED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onAccept(final StateChangeContext stateChangeContext, final int phase) {
        productionTrackingListenerService.onAccept(stateChangeContext);
    }

    @RunInPhase(ProductionTrackingStateChangePhase.LAST)
    @RunForStateTransition(sourceState = ProductionTrackingStateStringValues.ACCEPTED, targetState = ProductionTrackingStateStringValues.DECLINED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onChangeFromAcceptedToDecline(final StateChangeContext stateChangeContext, final int phase) {
        productionTrackingListenerService.onChangeFromAcceptedToDeclined(stateChangeContext);
    }
    
    @RunInPhase(ProductionTrackingStateChangePhase.DEFAULT)
    @RunForStateTransition(sourceState = ProductionTrackingStateStringValues.DRAFT)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onChangeFromDraftToAny(final StateChangeContext stateChangeContext, final int phase) {
        productionTrackingListenerService.onLeavingDraft(stateChangeContext);
    }
}
