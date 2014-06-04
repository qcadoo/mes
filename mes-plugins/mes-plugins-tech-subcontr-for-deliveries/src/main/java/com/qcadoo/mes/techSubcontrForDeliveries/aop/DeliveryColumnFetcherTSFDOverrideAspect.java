/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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

import com.qcadoo.mes.deliveries.print.DeliveryProduct;
import com.qcadoo.mes.techSubcontrForDeliveries.constants.TechSubcontrForDeliveriesConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(TechSubcontrForDeliveriesConstants.PLUGIN_IDENTIFIER)
public class DeliveryColumnFetcherTSFDOverrideAspect {

    @Autowired
    private DeliveryColumnFetcherTSFDOverrideUtil deliveryColumnFetcherTSFDOverrideUtil;

    @Pointcut("execution(private boolean com.qcadoo.mes.deliveries.print.DeliveryColumnFetcher.compareProducts(..)) "
            + "&& args(deliveryProduct, deliveredProduct)")
    public void compareProductsExecution(final DeliveryProduct deliveryProduct, final Entity deliveredProduct) {
    }

    @Around("compareProductsExecution(deliveryProduct, deliveredProduct)")
    public boolean aroundCompareProductsExecution(final ProceedingJoinPoint pjp, final DeliveryProduct deliveryProduct,
            final Entity deliveredProduct) throws Throwable {
        if (deliveryColumnFetcherTSFDOverrideUtil.shouldOverride()) {
            return deliveryColumnFetcherTSFDOverrideUtil.compareProductsAndOperation(deliveryProduct, deliveredProduct);
        } else {
            return (Boolean) pjp.proceed();
        }
    }

}
