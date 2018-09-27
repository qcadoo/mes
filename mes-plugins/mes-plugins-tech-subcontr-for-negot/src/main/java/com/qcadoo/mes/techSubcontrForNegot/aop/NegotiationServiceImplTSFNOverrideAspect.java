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
package com.qcadoo.mes.techSubcontrForNegot.aop;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.techSubcontrForNegot.constants.TechSubcontrForNegotConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(TechSubcontrForNegotConstants.PLUGIN_IDENTIFIER)
public class NegotiationServiceImplTSFNOverrideAspect {

    @Autowired
    private NegotiationServiceImplTSFNOverrideUtil negotiationServiceImplTSFNOverrideUtil;

    @Pointcut("execution(private java.util.List<com.qcadoo.model.api.Entity> com.qcadoo.mes.supplyNegotiations.NegotiationServiceImpl.getCompaniesForNegotiationProduct(..)) "
            + "&& args(negotiationProduct)")
    public void getCompaniesForNegotiationProductExecution(final Entity negotiationProduct) {
    }

    @SuppressWarnings("unchecked")
    @Around("getCompaniesForNegotiationProductExecution(negotiationProduct)")
    public List<Entity> aroundGetCompaniesForNegotiationProductExecution(final ProceedingJoinPoint pjp,
            final Entity negotiationProduct) throws Throwable {
        if (negotiationServiceImplTSFNOverrideUtil.shouldOverride(negotiationProduct)) {
            return negotiationServiceImplTSFNOverrideUtil.getCompaniesForNegotiationProduct(negotiationProduct);
        } else {
            return (List<Entity>) pjp.proceed();
        }
    }

    @Pointcut("execution(private com.qcadoo.model.api.Entity com.qcadoo.mes.supplyNegotiations.NegotiationServiceImpl.createRequestForQuotationProduct(..)) "
            + " && args(negotiationProduct)")
    public void createRequestForQuotationProductExecution(final Entity negotiationProduct) {
    }

    @RunIfEnabled(TechSubcontrForNegotConstants.PLUGIN_IDENTIFIER)
    @AfterReturning(value = "createRequestForQuotationProductExecution(negotiationProduct)", returning = "requestForQuotationProduct")
    public void afterCreateRequestForQuotationProductExecution(final Entity negotiationProduct,
            final Entity requestForQuotationProduct) {
        negotiationServiceImplTSFNOverrideUtil.fillRequestForQuotationProductOperation(negotiationProduct,
                requestForQuotationProduct);
    }

}
