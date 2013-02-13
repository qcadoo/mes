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
package com.qcadoo.mes.techSubcontracting.aop;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.model.api.Entity;

@Aspect
@Configurable
public class ProductQuantitiesServiceImplTSOverrideAspect {

    @Autowired
    private ProductQuantitiesServiceImplTSOverrideUtil productQuantitiesServiceImplTSOverrideUtil;

    @Pointcut("execution(private java.util.Map<com.qcadoo.model.api.Entity, java.math.BigDecimal> com.qcadoo.mes.technologies.ProductQuantitiesServiceImpl.getProductComponentWithQuantitiesWithoutNonComponents(..)) "
            + "&& args(productComponentWithQuantities, nonComponents)")
    public void getProductComponentWithQuantitiesWithoutNonComponentsExecution(
            final Map<Entity, BigDecimal> productComponentWithQuantities, final Set<Entity> nonComponents) {
    }

    @SuppressWarnings("unchecked")
    @Around("getProductComponentWithQuantitiesWithoutNonComponentsExecution(productComponentWithQuantities, nonComponents)")
    public Map<Entity, BigDecimal> aroundGetProductComponentWithQuantitiesWithoutNonComponentsExecution(
            final ProceedingJoinPoint pjp, final Map<Entity, BigDecimal> productComponentWithQuantities,
            final Set<Entity> nonComponents) throws Throwable {
        if (productQuantitiesServiceImplTSOverrideUtil.shouldOverride()) {
            return productQuantitiesServiceImplTSOverrideUtil.getProductComponentWithQuantitiesWithoutNonComponents(
                    productComponentWithQuantities, nonComponents);
        } else {
            return (Map<Entity, BigDecimal>) pjp.proceed();
        }
    }

}
