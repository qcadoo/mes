/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Aspect
@Configurable
public class DeliveredProductHooksTSFDOverrideAspect {

    @Autowired
    private DeliveredProductHooksTSFDOverrideUtil deliveredProductHooksTSFDOverrideUtil;

    @Pointcut("execution(public boolean com.qcadoo.mes.deliveries.hooks.DeliveredProductHooks.checkIfDeliveredProductAlreadyExists(..)) "
            + "&& args(deliveredProductDD, deliveredProduct)")
    public void checkIfDeliveredProductAlreadyExistsExecution(final DataDefinition deliveredProductDD,
            final Entity deliveredProduct) {
    }

    @Around("checkIfDeliveredProductAlreadyExistsExecution(deliveredProductDD, deliveredProduct)")
    public boolean aroundCheckIfDeliveredProductAlreadyExistsExecution(final ProceedingJoinPoint pjp,
            final DataDefinition deliveredProductDD, final Entity deliveredProduct) throws Throwable {
        if (deliveredProductHooksTSFDOverrideUtil.shouldOverride()) {
            return deliveredProductHooksTSFDOverrideUtil.checkIfDeliveredProductAlreadyExists(deliveredProductDD,
                    deliveredProduct);
        } else {
            return (Boolean) pjp.proceed();
        }
    }

}
