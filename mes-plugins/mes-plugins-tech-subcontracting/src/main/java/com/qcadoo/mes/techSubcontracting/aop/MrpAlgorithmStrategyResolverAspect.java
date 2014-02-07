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

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.qcadoo.mes.techSubcontracting.constants.TechSubcontractingConstants;
import com.qcadoo.mes.technologies.MrpAlgorithmStrategy;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@RunIfEnabled(TechSubcontractingConstants.PLUGIN_IDENTIFIER)
public abstract class MrpAlgorithmStrategyResolverAspect {

    protected abstract MrpAlgorithmStrategy getAlgorithmService();

    @Pointcut("execution(private java.util.Map<com.qcadoo.model.api.Entity, java.math.BigDecimal> com.qcadoo.mes.technologies.ProductQuantitiesServiceImpl.getProductWithQuantities(..)) "
            + "&& args(productComponentWithQuantities, nonComponents, mrpAlgorithm, operationProductComponentModelName)")
    public void getProductsMethodExecution(final Map<Entity, BigDecimal> productComponentWithQuantities,
            final Set<Entity> nonComponents, final MrpAlgorithm mrpAlgorithm, final String operationProductComponentModelName) {
    }

    @SuppressWarnings("unchecked")
    @Around("getProductsMethodExecution(productComponentWithQuantities, nonComponents, mrpAlgorithm, operationProductComponentModelName)")
    public Map<Entity, BigDecimal> aroundGetProductsMethodExecution(final ProceedingJoinPoint pjp,
            final Map<Entity, BigDecimal> productComponentWithQuantities, final Set<Entity> nonComponents,
            final MrpAlgorithm mrpAlgorithm, final String operationProductComponentModelName) throws Throwable {
        if (PluginUtils.isEnabled(TechSubcontractingConstants.PLUGIN_IDENTIFIER)
                && getAlgorithmService().isApplicableFor(mrpAlgorithm)) {
            return getAlgorithmService().perform(productComponentWithQuantities, nonComponents, mrpAlgorithm,
                    operationProductComponentModelName);
        } else {
            return (Map<Entity, BigDecimal>) pjp.proceed();
        }
    }

}
