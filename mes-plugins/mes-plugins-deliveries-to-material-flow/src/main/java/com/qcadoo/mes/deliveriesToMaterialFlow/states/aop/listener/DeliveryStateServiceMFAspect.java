package com.qcadoo.mes.deliveriesToMaterialFlow.states.aop.listener;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.deliveries.states.aop.DeliveryStateChangeAspect;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateChangePhase;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.mes.deliveriesToMaterialFlow.constants.DeliveriesToMaterialFlowConstants;
import com.qcadoo.mes.deliveriesToMaterialFlow.states.DeliveryStateServiceMF;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(DeliveriesToMaterialFlowConstants.PLUGIN_IDENTIFIER)
public class DeliveryStateServiceMFAspect extends AbstractStateListenerAspect {

    @Autowired
    DeliveryStateServiceMF deliveryStateServiceMF;

    @Pointcut(DeliveryStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {

    }

    @RunInPhase(DeliveryStateChangePhase.LAST)
    @RunForStateTransition(targetState = DeliveryStateStringValues.RECEIVED)
    @After(PHASE_EXECUTION_POINTCUT)
    public void createTransfersForTheReceivedProducts(final StateChangeContext stateChangeContext, final int phase) {
        deliveryStateServiceMF.createTransfersForTheReceivedProducts(stateChangeContext);
    }
}
