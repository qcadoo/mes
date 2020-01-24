package com.qcadoo.mes.technologies.states.aop.listener;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunForStateTransitions;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.aop.TechnologyStateChangeAspect;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateChangePhase;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.plugin.api.RunIfEnabled;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

@Aspect
@Configurable
@RunIfEnabled(TechnologiesConstants.PLUGIN_IDENTIFIER)
public class TechnologyProductStructureDeletingAspect extends AbstractStateListenerAspect {

    @RunInPhase(TechnologyStateChangePhase.LAST)
    @RunForStateTransitions({ @RunForStateTransition(targetState = TechnologyStateStringValues.DRAFT) })
    @After(PHASE_EXECUTION_POINTCUT)
    public void postHookOnDraft(final StateChangeContext stateChangeContext, final int phase) {
        Entity technology = stateChangeContext.getOwner();
        Entity technologyFromDB = technology.getDataDefinition().get(technology.getId());
        EntityTree tree = technologyFromDB.getTreeField(TechnologyFields.PRODUCT_STRUCTURE_TREE);
        if (tree.getRoot() != null) {
            List<Entity> treeEntities = tree.find().list().getEntities();
            for (Entity entity : treeEntities) {
                entity.getDataDefinition().delete(entity.getId());
            }
        }
    }

    @Pointcut(TechnologyStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }
}
