package com.qcadoo.mes.technologies.states.aop.listener;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunForStateTransitions;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.states.aop.TechnologyStateChangeAspect;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateChangePhase;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@RunIfEnabled(TechnologiesConstants.PLUGIN_IDENTIFIER)
public class TechnologyMasterUnmarkingAspect extends AbstractStateListenerAspect {

    @RunInPhase(TechnologyStateChangePhase.LAST)
    @RunForStateTransitions({ @RunForStateTransition(targetState = TechnologyStateStringValues.OUTDATED),
            @RunForStateTransition(targetState = TechnologyStateStringValues.DECLINED) })
    @After(PHASE_EXECUTION_POINTCUT)
    public void postHookOnOutdatingOrDeclining(final StateChangeContext stateChangeContext, final int phase) {
        stateChangeContext.getOwner().setField("master", false);
    }

    @Pointcut(TechnologyStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }
}
