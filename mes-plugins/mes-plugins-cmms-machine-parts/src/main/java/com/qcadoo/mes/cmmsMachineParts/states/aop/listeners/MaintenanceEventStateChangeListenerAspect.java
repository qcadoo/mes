package com.qcadoo.mes.cmmsMachineParts.states.aop.listeners;

import static com.qcadoo.mes.states.aop.RunForStateTransitionAspect.WILDCARD_STATE;

import com.qcadoo.mes.cmmsMachineParts.states.MaintenanceEventStateSetupService;
import com.qcadoo.mes.cmmsMachineParts.states.MaintenanceEventStateValidationService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.states.MaintenanceEventStateChangeListenerService;
import com.qcadoo.mes.cmmsMachineParts.states.aop.MaintenanceEventStateChangeAspect;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventStateChangePhase;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventStateStringValues;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER)
public class MaintenanceEventStateChangeListenerAspect extends AbstractStateListenerAspect {

    @Autowired
    private MaintenanceEventStateValidationService validationService;

    @Autowired
    private MaintenanceEventStateSetupService setupService;

    @Autowired
    private MaintenanceEventStateChangeListenerService listenerService;

    @Pointcut(MaintenanceEventStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {

    }

    @RunInPhase(MaintenanceEventStateChangePhase.SETUP)
    @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = MaintenanceEventStateStringValues.IN_PROGRESS)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void setupOnInProgress(final StateChangeContext stateChangeContext, final int phase) {
        setupService.setupOnInProgress(stateChangeContext);
    }

    @RunInPhase(MaintenanceEventStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = MaintenanceEventStateStringValues.IN_PROGRESS)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void validationOnInProgress(final StateChangeContext stateChangeContext, final int phase) {
        validationService.validationOnInProgress(stateChangeContext);
    }


    @RunInPhase(MaintenanceEventStateChangePhase.DEFAULT)
    @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = MaintenanceEventStateStringValues.PLANNED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onCancelled(final StateChangeContext stateChangeContext, final int phase) {
    }

}
