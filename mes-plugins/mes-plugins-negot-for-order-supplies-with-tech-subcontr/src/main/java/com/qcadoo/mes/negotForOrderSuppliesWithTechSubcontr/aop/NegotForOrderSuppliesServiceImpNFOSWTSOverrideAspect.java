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
package com.qcadoo.mes.negotForOrderSuppliesWithTechSubcontr.aop;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.negotForOrderSuppliesWithTechSubcontr.constants.NegotForOrderSuppliesWithTechSubcontrConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(NegotForOrderSuppliesWithTechSubcontrConstants.PLUGIN_IDENTIFIER)
public class NegotForOrderSuppliesServiceImpNFOSWTSOverrideAspect {

    @Autowired
    private NegotForOrderSuppliesServiceImpNFOSWTSOverrideUtil negotForOrderSuppliesServiceImpNFOSWTSOverrideUtil;

    @Pointcut("execution(private java.util.List<com.qcadoo.model.api.Entity> com.qcadoo.mes.negotForOrderSupplies.NegotForOrderSuppliesServiceImpl.createNegotiationProducts(..)) "
            + "&& args(coverageProducts)")
    public void createNegotiationProductsExecution(final List<Entity> coverageProducts) {
    }

    @Around("createNegotiationProductsExecution(coverageProducts)")
    public List<Entity> aroundChangeDeliveredQuantityFromNegotiationDetailsExecution(final ProceedingJoinPoint pjp,
            final List<Entity> coverageProducts) throws Throwable {
        return negotForOrderSuppliesServiceImpNFOSWTSOverrideUtil.createNegotiationProducts(coverageProducts);
    }

}
