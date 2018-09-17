package com.qcadoo.mes.productFlowThruDivision.deliveries.states.aop.listeners;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.deliveries.states.aop.DeliveryStateChangeAspect;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateChangePhase;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.deliveries.states.DeliveryStatePFTDService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER)
public class DeliveryStateListenerPFTDAspect extends AbstractStateListenerAspect {

    @Autowired
    private DeliveryStatePFTDService deliveryStatePFTDService;

    @Pointcut(DeliveryStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {

    }

    @RunInPhase(DeliveryStateChangePhase.PRE_VALIDATION)
    @RunForStateTransition(targetState = DeliveryStateStringValues.RECEIVED)
    @After(PHASE_EXECUTION_POINTCUT)
    public void validate(final StateChangeContext stateChangeContext, final int phase) {
        deliveryStatePFTDService.validate(stateChangeContext);
    }

    @Pointcut("execution(public void com.qcadoo.mes.deliveriesToMaterialFlow.states.DeliveryStateServiceMF.tryCreateIssuesForDeliveriesReservations(..)) "
            + "&& args(stateChangeContext)")
    public void tryCreateIssuesForDeliveriesReservations(final StateChangeContext stateChangeContext) {
    }

    @Around("tryCreateIssuesForDeliveriesReservations(stateChangeContext)")
    public void aroundTryCreateIssuesForDeliveriesReservations(final ProceedingJoinPoint pjp,
            final StateChangeContext stateChangeContext) throws Throwable {
        deliveryStatePFTDService.tryCreateIssuesForDeliveriesReservations(stateChangeContext);
    }
}
