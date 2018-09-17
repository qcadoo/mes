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
package com.qcadoo.mes.ordersForSubproductsGeneration.costCalculation.aop;

import java.math.BigDecimal;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.operationCostCalculations.OperationsCostCalculationService;
import com.qcadoo.mes.ordersForSubproductsGeneration.constants.CostCalculationFieldsOFSPG;
import com.qcadoo.mes.ordersForSubproductsGeneration.constants.OrdersForSubproductsGenerationConstans;
import com.qcadoo.mes.technologies.ProductQuantitiesWithComponentsService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.mes.technologies.dto.ProductQuantitiesHolder;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(OrdersForSubproductsGenerationConstans.PLUGIN_IDENTIFIER)
public class OperationsCostCalculationServiceImplOFSPGAspect {

    @Autowired
    private OperationsCostCalculationService operatonsCostCalculationService;

    @Autowired
    private ProductQuantitiesWithComponentsService productQuantitiesWithComponentsService;

    @Pointcut("execution(private com.qcadoo.mes.technologies.dto.ProductQuantitiesHolder "
            + "com.qcadoo.mes.operationCostCalculations.OperationsCostCalculationServiceImpl.getProductQuantitiesAndOperationRuns(..)) "
            + "&& args(technology, quantity, costCalculationOrProductionBalance)")
    public void getProductQuantitiesAndOperationRunsPointcut(final Entity technology, final BigDecimal quantity,
            final Entity costCalculationOrProductionBalance) {
    }

    @Around("getProductQuantitiesAndOperationRunsPointcut(technology, quantity, costCalculationOrProductionBalance)")
    public ProductQuantitiesHolder aroundGetProductQuantitiesAndOperationRuns(final ProceedingJoinPoint pjp,
            final Entity technology, final BigDecimal quantity, final Entity costCalculationOrProductionBalance)
                    throws Throwable {
        boolean includeComponents = costCalculationOrProductionBalance
                .getBooleanField(CostCalculationFieldsOFSPG.INCLUDE_COMPONENTS);
        if (!includeComponents) {
            return (ProductQuantitiesHolder) pjp.proceed();
        }
        return productQuantitiesWithComponentsService.getProductComponentQuantities(technology, quantity);
    }

    @Pointcut("execution(private java.util.Map<java.lang.Long, java.math.BigDecimal> "
            + "com.qcadoo.mes.costNormsForMaterials.ProductsCostCalculationServiceImpl.getNeededProductQuantities(..)) "
            + "&& args(costCalculationOrProductionBalance, technology, quantity, algorithm)")
    public void getNeededProductQuantitiesPointcut(final Entity costCalculationOrProductionBalance, final Entity technology,
            final BigDecimal quantity, final MrpAlgorithm algorithm) {
    }

    @Around("getNeededProductQuantitiesPointcut(costCalculationOrProductionBalance, technology, quantity, algorithm)")
    public Map<Long, BigDecimal> aroundGetNeededProductQuantities(final ProceedingJoinPoint pjp,
            final Entity costCalculationOrProductionBalance, final Entity technology, final BigDecimal quantity,
            final MrpAlgorithm algorithm) throws Throwable {
        if (costCalculationOrProductionBalance != null) {
            boolean includeComponents = costCalculationOrProductionBalance
                    .getBooleanField(CostCalculationFieldsOFSPG.INCLUDE_COMPONENTS);
            if (!includeComponents) {
                return (Map<Long, BigDecimal>) pjp.proceed();
            }
        }
        return productQuantitiesWithComponentsService.getNeededProductQuantities(technology, quantity,
                MrpAlgorithm.ONLY_MATERIALS);
    }

    @Pointcut("execution(private java.util.Map<java.lang.Long, java.math.BigDecimal> "
            + "com.qcadoo.mes.costCalculation.print.CostCalculationPdfService.getNeededProductQuantities(..)) "
            + "&& args(costCalculationOrProductionBalance, technology, quantity, algorithm)")
    public void getNeededProductQuantitiesForPdfPointcut(final Entity costCalculationOrProductionBalance, final Entity technology,
            final BigDecimal quantity, final MrpAlgorithm algorithm) {
    }

    @Around("getNeededProductQuantitiesForPdfPointcut(costCalculationOrProductionBalance, technology, quantity, algorithm)")
    public Map<Long, BigDecimal> aroundGetNeededProductQuantitiesForPdf(final ProceedingJoinPoint pjp,
            final Entity costCalculationOrProductionBalance, final Entity technology, final BigDecimal quantity,
            final MrpAlgorithm algorithm) throws Throwable {
        boolean includeComponents = costCalculationOrProductionBalance
                .getBooleanField(CostCalculationFieldsOFSPG.INCLUDE_COMPONENTS);
        if (!includeComponents) {
            return (Map<Long, BigDecimal>) pjp.proceed();
        }
        return productQuantitiesWithComponentsService.getNeededProductQuantities(technology, quantity,
                MrpAlgorithm.ONLY_MATERIALS);
    }
}
