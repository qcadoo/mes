/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.supplyNegotiations.states.aop;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.mes.states.aop.AbstractStateChangeAspect;
import com.qcadoo.mes.supplyNegotiations.states.constants.OfferStateChangeDescriber;
import com.qcadoo.mes.supplyNegotiations.states.constants.OfferStateChangePhase;

@Aspect
@Service
public class OfferStateChangeAspect extends AbstractStateChangeAspect {

    @Autowired
    private OfferStateChangeDescriber offerStateChangeDescriber;

    public static final String SELECTOR_POINTCUT = "this(com.qcadoo.mes.supplyNegotiations.states.aop.OfferStateChangeAspect)";

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return offerStateChangeDescriber;
    }

    @Override
    protected void changeStatePhase(final StateChangeContext stateChangeContext, final int phase) {

    }

    @Override
    protected int getNumOfPhases() {
        return OfferStateChangePhase.getNumOfPhases();
    }

}
