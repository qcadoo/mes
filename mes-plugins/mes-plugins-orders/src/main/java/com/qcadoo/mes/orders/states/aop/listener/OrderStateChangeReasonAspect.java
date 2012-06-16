package com.qcadoo.mes.orders.states.aop.listener;

import static com.qcadoo.mes.orders.states.constants.OrderStateStringValues.ABANDONED;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.orders.OrderStateChangeReasonService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.orders.states.constants.OrderStateChangePhase;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.service.client.util.ViewContextHolder;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(OrdersConstants.PLUGIN_IDENTIFIER)
public class OrderStateChangeReasonAspect extends AbstractStateListenerAspect {

    @Autowired
    private OrderStateChangeReasonService stateChangeReasonService;

    @Pointcut(OrderStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }

    @RunForStateTransition(targetState = ABANDONED)
    @RunInPhase(OrderStateChangePhase.FILL_REASON)
    @Before("phaseExecution(stateChangeContext, phase) && cflow(viewClientExecution(viewContext))")
    public void askForAbandonReason(final StateChangeContext stateChangeContext, final int phase,
            final ViewContextHolder viewContext) {
        // if (stateChangeReasonService.neededForAbandon()) {
        showReasonForm(stateChangeContext, viewContext);
        // }
    }

    @RunForStateTransition(targetState = OrderStateStringValues.INTERRUPTED)
    @RunInPhase(OrderStateChangePhase.FILL_REASON)
    @Before("phaseExecution(stateChangeContext, phase) && cflow(viewClientExecution(viewContext))")
    public void askForInterruptReason(final StateChangeContext stateChangeContext, final int phase,
            final ViewContextHolder viewContext) {
        // if (stateChangeReasonService.neededForInterrupt()) {
        showReasonForm(stateChangeContext, viewContext);
        // }
    }

    @RunForStateTransition(targetState = OrderStateStringValues.DECLINED)
    @RunInPhase(OrderStateChangePhase.FILL_REASON)
    @Before("phaseExecution(stateChangeContext, phase) && cflow(viewClientExecution(viewContext))")
    public void askForDeclineReason(final StateChangeContext stateChangeContext, final int phase,
            final ViewContextHolder viewContext) {
        // if (stateChangeReasonService.neededForDecline()) {
        showReasonForm(stateChangeContext, viewContext);
        // }
    }

    private void showReasonForm(final StateChangeContext stateChangeContext, final ViewContextHolder viewContext) {
        stateChangeContext.setStatus(StateChangeStatus.PAUSED);
        stateChangeContext.save();
        viewContext.getViewDefinitionState().openModal(
                "../page/orders/orderStateChangeReasonDialog.html?context={\"form.id\": "
                        + stateChangeContext.getStateChangeEntity().getId() + "}");
    }

}
