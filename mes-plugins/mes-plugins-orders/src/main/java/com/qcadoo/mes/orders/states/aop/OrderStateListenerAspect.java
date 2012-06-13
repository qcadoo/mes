package com.qcadoo.mes.orders.states.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.mes.states.messages.constants.MessageType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled("orders")
public class OrderStateListenerAspect extends AbstractStateListenerAspect {

    @Autowired
    private OrderStateChangeAspect orderStateChangeService;

    @RunInPhase(1)
    @Before("phaseExecution(stateChangeEntity, phase)")
    public void test(final Entity stateChangeEntity, final int phase) {
        orderStateChangeService.addMessage(stateChangeEntity, MessageType.INFO, "test");
    }

    @Pointcut("this(OrderStateChangeAspect)")
    protected void targetServicePointcut() {
    }

    @Override
    public StateChangeEntityDescriber getStateChangeEntityDescriber() {
        return orderStateChangeService.getChangeEntityDescriber();
    }

}
