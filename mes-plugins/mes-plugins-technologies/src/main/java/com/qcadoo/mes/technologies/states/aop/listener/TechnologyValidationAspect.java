package com.qcadoo.mes.technologies.states.aop.listener;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunForStateTransitions;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.states.aop.TechnologyStateChangeAspect;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateChangePhase;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.mes.technologies.states.listener.TechnologyValidationService;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(TechnologiesConstants.PLUGIN_IDENTIFIER)
public class TechnologyValidationAspect extends AbstractStateListenerAspect {

    @Autowired
    private TechnologyValidationService technologyValidationService;

    @Pointcut(TechnologyStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }

    @RunInPhase(TechnologyStateChangePhase.PRE_VALIDATION)
    @RunForStateTransitions({ @RunForStateTransition(targetState = TechnologyStateStringValues.ACCEPTED),
            @RunForStateTransition(targetState = TechnologyStateStringValues.CHECKED) })
    @Before(PHASE_EXECUTION_POINTCUT)
    public void preValidationOnAcceptingOrChecking(final StateChangeContext stateChangeContext, final int phase) {
        technologyValidationService.checkConsumingManyProductsFromOneSubOp(stateChangeContext);
    }

    @RunInPhase(TechnologyStateChangePhase.PRE_VALIDATION)
    @RunForStateTransitions({ @RunForStateTransition(targetState = TechnologyStateStringValues.OUTDATED),
            @RunForStateTransition(targetState = TechnologyStateStringValues.DECLINED) })
    @Before(PHASE_EXECUTION_POINTCUT)
    public void preValidationOnOutdatingOrDeclining(final StateChangeContext stateChangeContext, final int phase) {
        technologyValidationService.checkIfTechnologyIsNotUsedInActiveOrder(stateChangeContext);
    }

}
