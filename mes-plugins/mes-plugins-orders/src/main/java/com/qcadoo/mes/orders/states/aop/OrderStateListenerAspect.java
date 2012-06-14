package com.qcadoo.mes.orders.states.aop;

import static com.qcadoo.mes.orders.states.constants.OrderStateStringValues.ACCEPTED;
import static com.qcadoo.mes.states.aop.RunForStateTransitionAspect.WILDCARD_STATE;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import com.qcadoo.mes.orders.states.constants.OrderStateChangePhase;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@RunIfEnabled("orders")
public class OrderStateListenerAspect extends AbstractStateListenerAspect {

    @RunInPhase(OrderStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = ACCEPTED)
    @Before("phaseExecution(stateChangeContext, phase)")
    public void preValidationOnAccept(final StateChangeContext stateChangeContext, final int phase) {
        // stateChangeContext.addFieldValidationError("qcadooView.validate.field.error.missing", "name");
        // stateChangeContext.addValidationError("siakiś globalny błąd");
    }

    @Pointcut("this(com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect)")
    protected void targetServicePointcut() {
    }

}
