/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.deliveriesToMaterialFlow.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.deliveries.constants.DeliveredProductMultiPositionFields;
import com.qcadoo.mes.deliveriesToMaterialFlow.aop.helper.DeliveredProductValidationHelper;
import com.qcadoo.mes.deliveriesToMaterialFlow.constants.DeliveredProductFieldsDTMF;
import com.qcadoo.mes.deliveriesToMaterialFlow.constants.DeliveriesToMaterialFlowConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(DeliveriesToMaterialFlowConstants.PLUGIN_IDENTIFIER)
public class DeliveredProductAddMultiListenersAspect {

    @Autowired
    private DeliveredProductValidationHelper deliveredProductValidaionHelper;

    @Pointcut("execution(private com.qcadoo.model.api.Entity com.qcadoo.mes.deliveries.listeners.DeliveredProductAddMultiListeners.createDeliveredProduct(..))"
            + "&& args(position, deliveredProductDD)")
    public void createDeliveredProduct(Entity position, DataDefinition deliveredProductDD) {
    }

    @Around("createDeliveredProduct(position, deliveredProductDD)")
    public Entity aroundCreateDeliveredProduct(final ProceedingJoinPoint pjp, Entity position, DataDefinition deliveredProductDD)
            throws Throwable {
        Entity deliveredProduct = (Entity) pjp.proceed();
        deliveredProduct.setField(DeliveredProductFieldsDTMF.EXPIRATION_DATE,
                position.getStringField(DeliveredProductMultiPositionFields.EXPIRATION_DATE));
        return deliveredProduct;
    }

}
