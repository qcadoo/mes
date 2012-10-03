package com.qcadoo.mes.deliveries.states.aop.listeners;

import static com.qcadoo.mes.states.aop.RunForStateTransitionAspect.WILDCARD_STATE;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.states.DeliveryStateValidationService;
import com.qcadoo.mes.deliveries.states.aop.DeliveryStateChangeAspect;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateChangePhase;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(DeliveriesConstants.PLUGIN_IDENTIFIER)
public class DeliveryStateValidationAspect extends AbstractStateListenerAspect {

    @Autowired
    private DeliveryStateValidationService validationService;

    @Pointcut(DeliveryStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }

    @RunInPhase(DeliveryStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = DeliveryStateStringValues.APPROVED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void preValidationOnApproved(final StateChangeContext stateChangeContext, final int phase) {
        validationService.validationOnApproved(stateChangeContext);
    }

    @RunInPhase(DeliveryStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(sourceState = DeliveryStateStringValues.APPROVED, targetState = DeliveryStateStringValues.RECEIVED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void preValidationOnReceived(final StateChangeContext stateChangeContext, final int phase) {
        validationService.validationOnReceived(stateChangeContext);
    }

}
