package com.qcadoo.mes.orders.states.aop.listener;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.TechnologyStateChangeListenerService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.mes.technologies.states.aop.TechnologyStateChangeAspect;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateChangePhase;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(OrdersConstants.PLUGIN_IDENTIFIER)
public class TechnologyStateChangeListenerAspect extends AbstractStateListenerAspect {

    @Autowired
    private TechnologyStateChangeListenerService technologyStateChangeListener;

    @Pointcut(TechnologyStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }

    @RunInPhase(TechnologyStateChangePhase.LAST)
    @RunForStateTransition(sourceState = TechnologyStateStringValues.CHECKED, targetState = TechnologyStateStringValues.DRAFT)
    @After(PHASE_EXECUTION_POINTCUT)
    public void afterChangeFromCheckedToDraft(final StateChangeContext stateChangeContext, final int phase) {
        technologyStateChangeListener.deleteCheckedTechnologyFromOrder(stateChangeContext);
    }

    @RunInPhase(TechnologyStateChangePhase.LAST)
    @RunForStateTransition(targetState = TechnologyStateStringValues.CHECKED)
    @After(PHASE_EXECUTION_POINTCUT)
    public void afterChangeToChecked(final StateChangeContext stateChangeContext, final int phase) {
        stateChangeContext.addMessage("orders.order.technology.info.aboutChecked", StateMessageType.INFO, false);
    }

}
