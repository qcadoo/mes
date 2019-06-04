package com.qcadoo.mes.operationalTasks.states.aop.listeners;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.operationalTasks.constants.OperationalTasksConstants;
import com.qcadoo.mes.operationalTasks.states.OperationalTaskOrderStateService;
import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.orders.states.constants.OrderStateChangePhase;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(OperationalTasksConstants.PLUGIN_IDENTIFIER)
public class OperationalTaskOrderStateAspect extends AbstractStateListenerAspect {

    @Autowired
    private OperationalTaskOrderStateService operationalTaskOrderStateService;

    @Pointcut(OrderStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {

    }

    @RunInPhase(OrderStateChangePhase.DEFAULT)
    @RunForStateTransition(targetState = OrderStateStringValues.IN_PROGRESS)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void startOperationalTask(final StateChangeContext stateChangeContext, final int phase) {
        operationalTaskOrderStateService.startOperationalTask(stateChangeContext);
    }

    @RunInPhase(OrderStateChangePhase.DEFAULT)
    @RunForStateTransition(targetState = OrderStateStringValues.DECLINED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void rejectOperationalTaskForDeclinedOrder(final StateChangeContext stateChangeContext, final int phase) {
        operationalTaskOrderStateService.rejectOperationalTask(stateChangeContext);
    }

    @RunInPhase(OrderStateChangePhase.DEFAULT)
    @RunForStateTransition(targetState = OrderStateStringValues.ABANDONED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void rejectOperationalTaskForAbandonedOrder(final StateChangeContext stateChangeContext, final int phase) {
        operationalTaskOrderStateService.rejectOperationalTask(stateChangeContext);
    }

    @RunInPhase(OrderStateChangePhase.DEFAULT)
    @RunForStateTransition(targetState = OrderStateStringValues.COMPLETED)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void finishOperationalTask(final StateChangeContext stateChangeContext, final int phase) {
        operationalTaskOrderStateService.finishOperationalTask(stateChangeContext);
    }
}
