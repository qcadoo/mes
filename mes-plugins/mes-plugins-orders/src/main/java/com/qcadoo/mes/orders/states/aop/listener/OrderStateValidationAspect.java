package com.qcadoo.mes.orders.states.aop.listener;

import static com.qcadoo.mes.orders.states.constants.OrderStateStringValues.ACCEPTED;
import static com.qcadoo.mes.orders.states.constants.OrderStateStringValues.COMPLETED;
import static com.qcadoo.mes.orders.states.constants.OrderStateStringValues.IN_PROGRESS;
import static com.qcadoo.mes.states.aop.RunForStateTransitionAspect.WILDCARD_STATE;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.orders.states.OrderStateValidationService;
import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.orders.states.constants.OrderStateChangePhase;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled("orders")
public class OrderStateValidationAspect extends AbstractStateListenerAspect {

    @Autowired
    private OrderStateValidationService validationService;

    @Pointcut(OrderStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }

    @RunInPhase(OrderStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = ACCEPTED)
    @Before("phaseExecution(stateChangeContext, phase)")
    public void preValidationOnAccept(final StateChangeContext stateChangeContext, final int phase) {
        validationService.validationOnAccepted(stateChangeContext);
    }

    @RunInPhase(OrderStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = IN_PROGRESS)
    @Before("phaseExecution(stateChangeContext, phase)")
    public void preValidationOnInProgress(final StateChangeContext stateChangeContext, final int phase) {
        validationService.validationOnInProgress(stateChangeContext);
    }

    @RunInPhase(OrderStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = COMPLETED)
    @Before("phaseExecution(stateChangeContext, phase)")
    public void preValidationOnCompleted(final StateChangeContext stateChangeContext, final int phase) {
        validationService.validationOnCompleted(stateChangeContext);
    }

}
