/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.cmmsMachineParts.states.aop.listeners;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclarePrecedence;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.cmmsMachineParts.PlannedEventChangeService;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.states.AfterReviewEventsService;
import com.qcadoo.mes.cmmsMachineParts.states.EventDocumentsService;
import com.qcadoo.mes.cmmsMachineParts.states.PlannedEventStateValidationService;
import com.qcadoo.mes.cmmsMachineParts.states.aop.PlannedEventStateChangeAspect;
import com.qcadoo.mes.cmmsMachineParts.states.constants.PlannedEventStateChangePhase;
import com.qcadoo.mes.cmmsMachineParts.states.constants.PlannedEventStateStringValues;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.mes.states.service.client.util.ViewContextHolder;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER)
@DeclarePrecedence("*,askForNotAcceptReason")
public class PlannedEventStateChangeListenerAspect extends AbstractStateListenerAspect {

    @Autowired
    private EventDocumentsService eventDocumentsService;

    @Autowired
    private AfterReviewEventsService afterReviewEventsService;

    @Autowired
    private PlannedEventChangeService plannedEventChangeService;

    @Autowired
    private PlannedEventStateValidationService validationService;

    @Pointcut(PlannedEventStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {

    }

    @RunInPhase(PlannedEventStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(targetState = PlannedEventStateStringValues.IN_PLAN)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void validationOnInPlan(final StateChangeContext stateChangeContext, final int phase) {
        validationService.validationOnInPlan(stateChangeContext);
    }

    @RunInPhase(PlannedEventStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(targetState = PlannedEventStateStringValues.PLANNED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onPlanned(final StateChangeContext stateChangeContext, final int phase) {
        validationService.validationOnPlanned(stateChangeContext);
    }

    @RunInPhase(PlannedEventStateChangePhase.DEFAULT)
    @RunForStateTransition(targetState = PlannedEventStateStringValues.REALIZED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void createDocumentsForMachineParts(final StateChangeContext stateChangeContext, final int phase) {
        eventDocumentsService.createDocumentsForMachineParts(stateChangeContext);
    }

    @RunInPhase(PlannedEventStateChangePhase.LAST)
    @RunForStateTransition(targetState = PlannedEventStateStringValues.CANCELED)
    @Before("phaseExecution(stateChangeContext, phase) && cflow(viewClientExecution(viewContext))")
    public void askForRevokeReason(final StateChangeContext stateChangeContext, final int phase,
            final ViewContextHolder viewContext) {
        plannedEventChangeService.showReasonForm(stateChangeContext, viewContext);
    }

    @RunInPhase(PlannedEventStateChangePhase.LAST)
    @RunForStateTransition(sourceState = PlannedEventStateStringValues.IN_EDITING, targetState = PlannedEventStateStringValues.IN_REALIZATION)
    @Before("phaseExecution(stateChangeContext, phase) && cflow(viewClientExecution(viewContext))")
    public void askForNotAcceptReason(final StateChangeContext stateChangeContext, final int phase,
            final ViewContextHolder viewContext) {
        plannedEventChangeService.showReasonForm(stateChangeContext, viewContext);
    }

    @RunInPhase(PlannedEventStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(targetState = PlannedEventStateStringValues.IN_REALIZATION)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onInRealization(final StateChangeContext stateChangeContext, final int phase) {
        validationService.validationOnInRealization(stateChangeContext);
    }

    @RunInPhase(PlannedEventStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(targetState = PlannedEventStateStringValues.REALIZED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onRealized(final StateChangeContext stateChangeContext, final int phase) {
        validationService.validationOnRealized(stateChangeContext);
    }

    @RunInPhase(PlannedEventStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(targetState = PlannedEventStateStringValues.IN_EDITING)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onInEditing(final StateChangeContext stateChangeContext, final int phase) {
        validationService.validationOnInEditing(stateChangeContext);
    }

    @RunInPhase(PlannedEventStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(targetState = PlannedEventStateStringValues.CANCELED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onCanceled(final StateChangeContext stateChangeContext, final int phase) {
        validationService.validationOnCanceled(stateChangeContext);
    }

    @RunInPhase(PlannedEventStateChangePhase.LAST)
    @RunForStateTransition(targetState = PlannedEventStateStringValues.REALIZED)
    @AfterReturning(PHASE_EXECUTION_POINTCUT)
    public void createAfterReviewEvents(final StateChangeContext stateChangeContext, final int phase) {
        afterReviewEventsService.createAfterReviewEvents(stateChangeContext);
    }

}
