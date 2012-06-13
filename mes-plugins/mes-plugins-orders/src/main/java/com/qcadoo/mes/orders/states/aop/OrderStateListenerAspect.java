package com.qcadoo.mes.orders.states.aop;

import static com.qcadoo.mes.orders.states.constants.OrderStateStringValues.ACCEPTED;
import static com.qcadoo.mes.states.aop.RunForStateTransitionAspect.STATE_WILDCARD;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.orders.states.constants.OrderStateChangePhase;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled("orders")
public class OrderStateListenerAspect extends AbstractStateListenerAspect {

    @Autowired
    private OrderStateChangeAspect orderStateChangeService;

    @RunInPhase(OrderStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(sourceState = STATE_WILDCARD, targetState = ACCEPTED)
    @Before("phaseExecution(stateChangeEntity, phase)")
    public void preValidationAccept(final Entity stateChangeEntity, final int phase) {
        // orderStateChangeService.addValidationError(stateChangeEntity, "name", "siakiś błąd");
        // orderStateChangeService.addValidationError(stateChangeEntity, null, "siakiś globalny błąd");
    }

    @Pointcut("this(com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect)")
    protected void targetServicePointcut() {
    }

    @Override
    public StateChangeEntityDescriber getStateChangeEntityDescriber() {
        return orderStateChangeService.getChangeEntityDescriber();
    }

}
