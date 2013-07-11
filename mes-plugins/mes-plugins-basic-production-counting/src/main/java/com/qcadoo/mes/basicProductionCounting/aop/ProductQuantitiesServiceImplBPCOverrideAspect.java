/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.basicProductionCounting.aop;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(BasicProductionCountingConstants.PLUGIN_IDENTIFIER)
public class ProductQuantitiesServiceImplBPCOverrideAspect {

    @Autowired
    private ProductQuantitiesServiceImplBPCOverrideUtil productQuantitiesServiceImplBPCOverrideUtil;

    @Pointcut("execution(private com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer com.qcadoo.mes.technologies.ProductQuantitiesServiceImpl.getProductComponentWithQuantitiesForOrders(..)) "
            + "&& args(orders, operationRuns, nonComponents, onTheFly)")
    public void getProductComponentWithQuantitiesForOrdersExecution(final List<Entity> orders,
            final Map<Long, BigDecimal> operationRuns, final Set<OperationProductComponentHolder> nonComponents,
            final boolean onTheFly) {
    }

    @Around("getProductComponentWithQuantitiesForOrdersExecution(orders, operationRuns, nonComponents, onTheFly)")
    public OperationProductComponentWithQuantityContainer aroundGetProductComponentWithQuantitiesForOrdersExecution(
            final ProceedingJoinPoint pjp, final List<Entity> orders, final Map<Long, BigDecimal> operationRuns,
            final Set<OperationProductComponentHolder> nonComponents, final boolean onTheFly) throws Throwable {
        return productQuantitiesServiceImplBPCOverrideUtil.getProductComponentWithQuantitiesForOrders(orders, operationRuns,
                nonComponents, onTheFly);
    }

}
