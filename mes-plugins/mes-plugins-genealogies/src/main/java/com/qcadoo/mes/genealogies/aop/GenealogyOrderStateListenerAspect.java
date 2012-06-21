package com.qcadoo.mes.genealogies.aop;

import static com.qcadoo.mes.orders.states.constants.OrderStateStringValues.COMPLETED;
import static com.qcadoo.mes.orders.states.constants.OrderStateStringValues.WILDCARD_STATE;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.genealogies.GenealogyOrderStateListenerService;
import com.qcadoo.mes.genealogies.constants.GenealogiesConstants;
import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.orders.states.constants.OrderStateChangePhase;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

@RunIfEnabled(GenealogiesConstants.PLUGIN_IDENTIFIER)
@Configurable
@Aspect
public class GenealogyOrderStateListenerAspect extends AbstractStateListenerAspect {

    @Autowired
    private GenealogyOrderStateListenerService listenerService;

    @Pointcut(OrderStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }

    @RunInPhase(OrderStateChangePhase.DEFAULT)
    @RunForStateTransition(sourceState = WILDCARD_STATE, targetState = COMPLETED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onCompleted(final StateChangeContext stateChangeContext, final int phase) {
        listenerService.onCompleted(stateChangeContext);
    }

}
