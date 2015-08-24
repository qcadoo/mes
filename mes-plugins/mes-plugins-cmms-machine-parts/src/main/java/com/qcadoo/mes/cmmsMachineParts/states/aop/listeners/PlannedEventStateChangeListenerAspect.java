package com.qcadoo.mes.cmmsMachineParts.states.aop.listeners;

import static com.qcadoo.mes.states.aop.RunForStateTransitionAspect.WILDCARD_STATE;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
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
public class PlannedEventStateChangeListenerAspect extends AbstractStateListenerAspect {

    @Autowired
    private EventDocumentsService eventDocumentsService;

    @Autowired
    PlannedEventStateValidationService validationService;

    @Pointcut(PlannedEventStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {

    }

    @RunInPhase(PlannedEventStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = PlannedEventStateStringValues.IN_PLAN)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void validationOnInPlan(final StateChangeContext stateChangeContext, final int phase) {
        validationService.validationOnInPlan(stateChangeContext);
    }

    @RunInPhase(PlannedEventStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = PlannedEventStateStringValues.CANCELED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void validationOnClosed(final StateChangeContext stateChangeContext, final int phase) {
    }

    @RunInPhase(PlannedEventStateChangePhase.DEFAULT)
    @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = PlannedEventStateStringValues.REALIZED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void createDocumentsForMachineParts(final StateChangeContext stateChangeContext, final int phase) {
        eventDocumentsService.createDocumentsForMachineParts(stateChangeContext);
    }

    @RunInPhase(PlannedEventStateChangePhase.DEFAULT)
    @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = PlannedEventStateStringValues.PLANNED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onCancelled(final StateChangeContext stateChangeContext, final int phase) {
    }

    @RunInPhase(PlannedEventStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = PlannedEventStateStringValues.REALIZED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void validationOnRevoked(final StateChangeContext stateChangeContext, final int phase) {
    }

    @RunInPhase(PlannedEventStateChangePhase.SETUP)
    @RunForStateTransition(targetState = PlannedEventStateStringValues.REALIZED)
    @Before("phaseExecution(stateChangeContext, phase) && cflow(viewClientExecution(viewContext))")
    public void askForRevokeReason(final StateChangeContext stateChangeContext, final int phase,
            final ViewContextHolder viewContext) {
    }
}
