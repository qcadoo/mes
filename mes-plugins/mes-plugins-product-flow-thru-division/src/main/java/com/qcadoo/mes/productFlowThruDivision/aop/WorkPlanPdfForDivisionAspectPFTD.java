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
package com.qcadoo.mes.productFlowThruDivision.aop;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.google.common.collect.Lists;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productFlowThruDivision.constants.OperationProductInComponentFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionFlowComponent;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER)
public class WorkPlanPdfForDivisionAspectPFTD {

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Pointcut("execution(private java.util.List<com.qcadoo.model.api.Entity> com.qcadoo.mes.workPlans.pdf.document.WorkPlanPdfForDivision.operationProductOutComponents(..)) "
            + "&& args(operationComponent)")
    public void operationProductOutComponents(final Entity operationComponent) {
    }

    @Around("operationProductOutComponents(operationComponent)")
    public List<Entity> aroundOperationProductOutComponents(final ProceedingJoinPoint pjp, final Entity operationComponent)
            throws Throwable {
        Entity technology = operationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
        Entity order = getOrderForTechnology(technology);
        if (order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)
                .equals(TypeOfProductionRecording.CUMULATED.getStringValue())) {
            List<Entity> outComponents = operationComponent
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);

            List<Entity> filtered = Lists.newArrayList();
            for (Entity component : outComponents) {
                boolean isIntermediate = !technologyService.isFinalProduct(component);
                if (!isIntermediate) {
                    filtered.add(component);
                } else {
                    String productionFlow = component.getStringField(OperationProductInComponentFieldsPFTD.PRODUCTION_FLOW);
                    if (productionFlow != null
                            && !productionFlow.equals(ProductionFlowComponent.WITHIN_THE_PROCESS.getStringValue())) {
                        filtered.add(component);
                    }
                }
            }
            return filtered;
        }
        return (List<Entity>) pjp.proceed();
    }

    @Pointcut("execution(private java.util.List<com.qcadoo.model.api.Entity> com.qcadoo.mes.workPlans.pdf.document.WorkPlanPdfForDivision.operationProductInComponents(..)) "
            + "&& args(operationComponent)")
    public void operationProductInComponents(final Entity operationComponent) {
    }

    @Around("operationProductInComponents(operationComponent)")
    public List<Entity> aroundOperationProductInComponents(final ProceedingJoinPoint pjp, final Entity operationComponent)
            throws Throwable {

        Entity technology = operationComponent.getBelongsToField(TechnologyOperationComponentFields.TECHNOLOGY);
        Entity order = getOrderForTechnology(technology);
        if (order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING)
                .equals(TypeOfProductionRecording.CUMULATED.getStringValue())) {
            List<Entity> inComponents = operationComponent
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);

            List<Entity> filtered = Lists.newArrayList();
            for (Entity component : inComponents) {
                boolean isIntermediate = technologyService.isIntermediateProduct(component);
                if (!isIntermediate) {
                    filtered.add(component);
                } else {
                    String productionFlow = component.getStringField(OperationProductInComponentFieldsPFTD.PRODUCTION_FLOW);
                    if (productionFlow != null
                            && !productionFlow.equals(ProductionFlowComponent.WITHIN_THE_PROCESS.getStringValue())) {
                        filtered.add(component);
                    }
                }
            }
            return filtered;
        }
        return (List<Entity>) pjp.proceed();
    }

    private Entity getOrderForTechnology(Entity technology) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                .add(SearchRestrictions.belongsTo(OrderFields.TECHNOLOGY, technology)).setMaxResults(1).uniqueResult();
    }
}
