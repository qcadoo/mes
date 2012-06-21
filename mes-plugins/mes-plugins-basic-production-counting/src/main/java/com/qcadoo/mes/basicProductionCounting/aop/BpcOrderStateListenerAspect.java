package com.qcadoo.mes.basicProductionCounting.aop;

import static com.qcadoo.mes.orders.states.constants.OrderStateChangePhase.DEFAULT;
import static com.qcadoo.mes.orders.states.constants.OrderStateStringValues.ACCEPTED;
import static com.qcadoo.mes.states.aop.RunForStateTransitionAspect.WILDCARD_STATE;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.basicProductionCounting.BpcOrderStateListenerService;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(BasicProductionCountingConstants.PLUGIN_IDENTIFIER)
public class BpcOrderStateListenerAspect extends AbstractStateListenerAspect {

    @Autowired
    private BpcOrderStateListenerService listenerService;

    @RunInPhase(DEFAULT)
    @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = ACCEPTED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onAccept(final StateChangeContext stateChangeContext, final int phase) {
        listenerService.onAccept(stateChangeContext);
    }

    @Pointcut(OrderStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }

}
