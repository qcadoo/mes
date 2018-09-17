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
/**
 * ***************************************************************************
 w * Copyright (c) 2010 Qcadoo Limited
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

package com.qcadoo.mes.techSubcontrForOrderSupplies.aop;

import org.springframework.beans.factory.annotation.Autowired;

//@Aspect
//@Configurable
//@RunIfEnabled(TechSubcontrForOrderSuppliesConstants.PLUGIN_IDENTIFIER)
public class MaterialRequirementCoverageServiceImplTSFOSOverrideAspect {

    @Autowired
    private MaterialRequirementCoverageServiceImplTSFOSOverrideUtil materialRequirementCoverageServiceImplTSFOSOverrideUtil;
/*
    @Pointcut("execution(private com.qcadoo.model.api.Entity com.qcadoo.mes.orderSupplies.coverage.MaterialRequirementCoverageServiceImpl.createCoverageProductLoggingForDelivery(..)) "
            + " && args(coverageProductForDelivery)")
    public void createCoverageProductLoggingForDeliveryExecution(final CoverageProductForDelivery coverageProductForDelivery) {
    }

    @RunIfEnabled(TechSubcontrForOrderSuppliesConstants.PLUGIN_IDENTIFIER)
    @AfterReturning(value = "createCoverageProductLoggingForDeliveryExecution(coverageProductForDelivery)", returning = "coverageProductLogging")
    public void afterCreateCoverageProductLoggingForDeliveryExecution(
            final CoverageProductForDelivery coverageProductForDelivery, final Entity coverageProductLogging) {
        materialRequirementCoverageServiceImplTSFOSOverrideUtil
                .createCoverageProductLoggingForDeliveryWithSubcontractedOperation(coverageProductForDelivery,
                        coverageProductLogging);
    }

    @Pointcut("execution(private com.qcadoo.model.api.Entity com.qcadoo.mes.orderSupplies.coverage.MaterialRequirementCoverageServiceImpl.createCoverageProductLoggingForOrder(..)) "
            + " && args(coverageProductForOrder)")
    public void createCoverageProductLoggingForOrderExecution(final CoverageProductForOrder coverageProductForOrder) {
    }

    @RunIfEnabled(TechSubcontrForOrderSuppliesConstants.PLUGIN_IDENTIFIER)
    @AfterReturning(value = "createCoverageProductLoggingForOrderExecution(coverageProductForOrder)", returning = "coverageProductLogging")
    public void afterCreateCoverageProductLoggingForOrderExecution(final CoverageProductForOrder coverageProductForOrder,
            final Entity coverageProductLogging) {
        materialRequirementCoverageServiceImplTSFOSOverrideUtil.createCoverageProductLoggingForOrderWithSubcontractedOperation(
                coverageProductForOrder, coverageProductLogging);
    }

    @Pointcut("execution(private java.util.List<com.qcadoo.model.api.Entity> com.qcadoo.mes.orderSupplies.coverage.MaterialRequirementCoverageServiceImpl.getCoverageProducts(..)) "
            + " && args(productAndCoverageProducts)")
    public void getCoverageProductsExecution(final Map<Long, Entity> productAndCoverageProducts) {
    }

    @RunIfEnabled(TechSubcontrForOrderSuppliesConstants.PLUGIN_IDENTIFIER)
    @AfterReturning(value = "getCoverageProductsExecution(productAndCoverageProducts)", returning = "coverageProducts")
    public void afterGetCoverageProductsExecution(final Map<Long, Entity> productAndCoverageProducts,
            final List<Entity> coverageProducts) {
        materialRequirementCoverageServiceImplTSFOSOverrideUtil.fillIsSubcontractedAndIsPurchased(coverageProducts);
    }

    @Pointcut("execution(private java.util.List<com.qcadoo.model.api.Entity> com.qcadoo.mes.orderSupplies.coverage.MaterialRequirementCoverageServiceImpl.filterCoverageProductsWithProductExtracted(..)) "
            + " && args(coverageProducts, productExtracted)")
    public void filterCoverageProductsWithProductExtractedExecution(final List<Entity> coverageProducts,
            final String productExtracted) {
    }

    @SuppressWarnings("unchecked")
    @Around("filterCoverageProductsWithProductExtractedExecution(coverageProducts, productExtracted)")
    public List<Entity> aroundFilterCoverageProductsWithProductExtractedExecution(final ProceedingJoinPoint pjp,
            final List<Entity> coverageProducts, final String productExtracted) throws Throwable {
        if (materialRequirementCoverageServiceImplTSFOSOverrideUtil.shouldOverride()
                && !ProductExtracted.ALL.getStringValue().equals(productExtracted)) {
            return materialRequirementCoverageServiceImplTSFOSOverrideUtil.filterCoverageProductsWithProductExtracted(
                    coverageProducts, productExtracted);
        } else {
            return (List<Entity>) pjp.proceed();
        }
    }
*/
}
