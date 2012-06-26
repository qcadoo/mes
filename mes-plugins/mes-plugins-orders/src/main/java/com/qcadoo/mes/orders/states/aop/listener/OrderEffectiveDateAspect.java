package com.qcadoo.mes.orders.states.aop.listener;

import java.util.Date;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.orders.states.constants.OrderStateChangePhase;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@RunIfEnabled(OrdersConstants.PLUGIN_IDENTIFIER)
public class OrderEffectiveDateAspect extends AbstractStateListenerAspect {

    @RunInPhase(OrderStateChangePhase.LAST)
    @RunForStateTransition(sourceState = OrderStateStringValues.ACCEPTED, targetState = OrderStateStringValues.IN_PROGRESS)
    @After(PHASE_EXECUTION_POINTCUT)
    public void afterStartProgress(final StateChangeContext stateChangeContext, final int phase) {
        stateChangeContext.getOwner().setField(OrderFields.EFFECTIVE_DATE_FROM, new Date());
    }

    @RunInPhase(OrderStateChangePhase.LAST)
    @RunForStateTransition(targetState = OrderStateStringValues.COMPLETED)
    @After(PHASE_EXECUTION_POINTCUT)
    public void afterComplete(final StateChangeContext stateChangeContext, final int phase) {
        stateChangeContext.getOwner().setField(OrderFields.EFFECTIVE_DATE_TO, new Date());
    }

    @Pointcut(OrderStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }

}
