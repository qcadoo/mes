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
package com.qcadoo.mes.materialRequirementCoverageForOrder.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.materialRequirementCoverageForOrder.constans.MaterialRequirementCoverageForOrderConstans;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.RunIfEnabled;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Aspect
@Configurable
@RunIfEnabled(MaterialRequirementCoverageForOrderConstans.PLUGIN_IDENTIFIER)
public class MRCCriteriaModifiersMRCFOOverideAspect {

    public static final String ORDER_PARAMETER = "order";

    @Pointcut("execution(public void com.qcadoo.mes.orderSupplies.criteriaModifiers.MRCCriteriaModifiers.baseModifier(..)) "
            + "&& args(scb, filterValue)")
    public void baseModifierA(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
    }

    @Around("baseModifierA(scb, filterValue)")
    public void aroundBaseModifier(final ProceedingJoinPoint pjp, final SearchCriteriaBuilder scb,
            final FilterValueHolder filterValue) throws Throwable {
        if (filterValue.has(ORDER_PARAMETER)) {
            scb.add(SearchRestrictions.isNotNull("productType"));
        } else {
            pjp.proceed();
        }
    }
}
