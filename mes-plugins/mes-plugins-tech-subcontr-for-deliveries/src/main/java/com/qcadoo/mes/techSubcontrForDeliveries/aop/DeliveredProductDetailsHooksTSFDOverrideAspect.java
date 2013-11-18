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
package com.qcadoo.mes.techSubcontrForDeliveries.aop;

import java.math.BigDecimal;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.techSubcontrForDeliveries.constants.TechSubcontrForDeliveriesConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(TechSubcontrForDeliveriesConstants.PLUGIN_IDENTIFIER)
public class DeliveredProductDetailsHooksTSFDOverrideAspect {

    @Autowired
    private DeliveredProductDetailsHooksTSFDOverrideUtil deliveredProductDetailsHooksTSFDOverrideUtil;

    @Pointcut("execution(private java.math.BigDecimal com.qcadoo.mes.deliveries.hooks.DeliveredProductDetailsHooks.getOrderedProductQuantity(..)) "
            + "&& args(deliveredProduct)")
    public void getOrderedProductQuantityExecution(final Entity deliveredProduct) {
    }

    @Around("getOrderedProductQuantityExecution(deliveredProduct)")
    public BigDecimal aroundGetOrderedProductQuantityExecution(final ProceedingJoinPoint pjp, final Entity deliveredProduct)
            throws Throwable {
        if (deliveredProductDetailsHooksTSFDOverrideUtil.shouldOverride(deliveredProduct)) {
            return deliveredProductDetailsHooksTSFDOverrideUtil.getOrderedProductQuantityWithOperation(deliveredProduct);
        } else {
            return (BigDecimal) pjp.proceed();
        }
    }

}
