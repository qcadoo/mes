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
package com.qcadoo.mes.techSubcontracting.aop;

import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

<<<<<<< HEAD
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
=======
import com.qcadoo.mes.techSubcontracting.constants.TechSubcontractingConstants;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;
>>>>>>> master

@Aspect
@Configurable
@RunIfEnabled(TechSubcontractingConstants.PLUGIN_IDENTIFIER)
public class ProductQuantitiesServiceImplTSOverrideAspect {

    @Autowired
    private ProductQuantitiesServiceImplTSOverrideUtil productQuantitiesServiceImplTSOverrideUtil;

    @Pointcut("execution(private com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer com.qcadoo.mes.technologies.ProductQuantitiesServiceImpl.getProductComponentWithQuantitiesWithoutNonComponents(..)) "
            + "&& args(productComponentWithQuantities, nonComponents)")
    public void getProductComponentWithQuantitiesWithoutNonComponentsExecution(
            final OperationProductComponentWithQuantityContainer productComponentWithQuantities,
            final Set<OperationProductComponentHolder> nonComponents) {
    }

    @Around("getProductComponentWithQuantitiesWithoutNonComponentsExecution(productComponentWithQuantities, nonComponents)")
    public OperationProductComponentWithQuantityContainer aroundGetProductComponentWithQuantitiesWithoutNonComponentsExecution(
            final ProceedingJoinPoint pjp, final OperationProductComponentWithQuantityContainer productComponentWithQuantities,
            final Set<OperationProductComponentHolder> nonComponents) throws Throwable {
        if (productQuantitiesServiceImplTSOverrideUtil.shouldOverride()) {
            return productQuantitiesServiceImplTSOverrideUtil.getProductComponentWithQuantitiesWithoutNonComponents(
                    productComponentWithQuantities, nonComponents);
        } else {
            return (OperationProductComponentWithQuantityContainer) pjp.proceed();
        }
    }

}
