/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
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
import com.qcadoo.mes.technologies.validators.TechnologyTreeValidators;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(TechnologiesConstants.PLUGIN_IDENTIFIER)
public class TechnologyValidationAspect extends AbstractStateListenerAspect {

    @Autowired
    private TechnologyValidationService technologyValidationService;

    @Autowired
    private TechnologyTreeValidators technologyTreeValidators;

    @Pointcut(TechnologyStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }

    @RunInPhase(TechnologyStateChangePhase.PRE_VALIDATION)
    @RunForStateTransitions({ @RunForStateTransition(targetState = TechnologyStateStringValues.ACCEPTED),
            @RunForStateTransition(targetState = TechnologyStateStringValues.CHECKED) })
    @Before(PHASE_EXECUTION_POINTCUT)
    public void preValidationOnAcceptingOrChecking(final StateChangeContext stateChangeContext, final int phase) {
        technologyValidationService.checkConsumingManyProductsFromOneSubOp(stateChangeContext);
        Entity technology = stateChangeContext.getOwner();
        technologyTreeValidators.checkConsumingTheSameProductFromManySubOperations(technology.getDataDefinition(), technology,
                true);
        technologyValidationService.checkIfTechnologyHasAtLeastOneComponent(stateChangeContext);
        // TODO DEV_TEAM when we fixed problem with referenced technology
        // technologyValidationService.checkIfAllReferenceTechnologiesAreAceepted(stateChangeContext);
        technologyValidationService.checkTopComponentsProducesProductForTechnology(stateChangeContext);
        technologyValidationService.checkIfOperationsUsesSubOperationsProds(stateChangeContext);
    }

    @RunInPhase(TechnologyStateChangePhase.PRE_VALIDATION)
    @RunForStateTransitions({ @RunForStateTransition(targetState = TechnologyStateStringValues.OUTDATED),
            @RunForStateTransition(targetState = TechnologyStateStringValues.DECLINED) })
    @Before(PHASE_EXECUTION_POINTCUT)
    public void preValidationOnOutdatingOrDeclining(final StateChangeContext stateChangeContext, final int phase) {
        technologyValidationService.checkIfTechnologyIsNotUsedInActiveOrder(stateChangeContext);
    }

}
