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
package com.qcadoo.mes.productFlowThruDivision.states.aop.listeners;

import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.states.ProductionTrackingListenerServicePFTD;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentEntityType;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Map;
import java.util.Set;

@Aspect
@Configurable
@RunIfEnabled(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER)
public class ProductionTrackingStateServiceAspectPFTD {

    @Autowired
    private ProductionTrackingListenerServicePFTD productionTrackingListenerService;

    @Pointcut("execution(private boolean com.qcadoo.mes.productionCounting.hooks.helpers.OperationProductsExtractor.shouldSkipAddingProduct(..)) "
            + " && args(operationProductComponentHolder,entityTypeWithAlreadyAddedProducts,typeOfProductionRecording)")
    public void shouldSkipAddingProduct(OperationProductComponentHolder operationProductComponentHolder,
            Map<OperationProductComponentEntityType, Set<Entity>> entityTypeWithAlreadyAddedProducts,
            String typeOfProductionRecording) {        
    }

    @Around("shouldSkipAddingProduct(operationProductComponentHolder,entityTypeWithAlreadyAddedProducts,typeOfProductionRecording)")
    public boolean aroundShouldSkipAddingProduct(final ProceedingJoinPoint pjp,
            OperationProductComponentHolder operationProductComponentHolder,
            Map<OperationProductComponentEntityType, Set<Entity>> entityTypeWithAlreadyAddedProducts,
            String typeOfProductionRecording) throws Throwable {

        if (cumulated(typeOfProductionRecording)) {

            Entity product = operationProductComponentHolder.getProduct();
            Entity toc = operationProductComponentHolder.getTechnologyOperationComponent();
            if (toc != null) {
                Entity technology = toc.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);

                if (operationProductComponentHolder.getProductMaterialType().getStringValue().equals(TechnologyService.L_02_INTERMEDIATE)) {
                    boolean isProductToSkip = !productionTrackingListenerService.isOperationProductComponentToRegister(
                            operationProductComponentHolder, product, toc);
                    if (isProductToSkip) {
                        return true;
                    }

                }
            }

        }

        return (boolean) pjp.proceed();
    }

    private boolean cumulated(String typeOfProductionRecording) {
        return TypeOfProductionRecording.CUMULATED.getStringValue().equals(typeOfProductionRecording);
    }
}
