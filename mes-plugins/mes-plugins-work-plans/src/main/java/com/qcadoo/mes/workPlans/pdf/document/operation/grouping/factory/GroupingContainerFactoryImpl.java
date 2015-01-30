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
package com.qcadoo.mes.workPlans.pdf.document.operation.grouping.factory;

import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.technologies.grouping.OperationMergeService;
import com.qcadoo.mes.workPlans.constants.WorkPlanFields;
import com.qcadoo.mes.workPlans.constants.WorkPlanType;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.DivisionGroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.EndProductGroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.GroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.NoDistinctionGroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.OperationProductInGroupingContainerDecorator;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.WorkstationTypeGroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.column.OperationProductColumn;
import com.qcadoo.mes.workPlans.pdf.document.order.WorkPlanColumnService;
import com.qcadoo.mes.workPlans.pdf.document.order.column.OrderColumn;
import com.qcadoo.model.api.Entity;

@Service
public class GroupingContainerFactoryImpl implements GroupingContainerFactory {

    protected static final Logger LOG = LoggerFactory.getLogger(GroupingContainerFactoryImpl.class);

    private TranslationService translationService;

    private WorkPlanColumnService workPlanColumnService;

    private OperationMergeService operationMergeService;

    private ProductionCountingService productionCountingService;

    private ParameterService parameterService;

    @Autowired
    public GroupingContainerFactoryImpl(TranslationService translationService, WorkPlanColumnService workPlanColumnService,
            OperationMergeService operationMergeService, ProductionCountingService productionCountingService,
            ParameterService parameterService) {
        this.translationService = translationService;
        this.workPlanColumnService = workPlanColumnService;
        this.operationMergeService = operationMergeService;
        this.productionCountingService = productionCountingService;
        this.parameterService = parameterService;
    }

    public GroupingContainer create(Entity workPlan, Locale locale) {
        String type = workPlan.getStringField(WorkPlanFields.TYPE);
        if (WorkPlanType.NO_DISTINCTION.getStringValue().equals(type)) {
            return new OperationProductInGroupingContainerDecorator(operationMergeService, noDistinction(workPlan, locale),
                    productionCountingService, parameterService);
        } else if (WorkPlanType.BY_END_PRODUCT.getStringValue().equals(type)) {
            return new OperationProductInGroupingContainerDecorator(operationMergeService, byEndProduct(workPlan, locale),
                    productionCountingService, parameterService);
        } else if (WorkPlanType.BY_WORKSTATION_TYPE.getStringValue().equals(type)) {
            return new OperationProductInGroupingContainerDecorator(operationMergeService, byWorkstationType(workPlan, locale),
                    productionCountingService, parameterService);
        } else if (WorkPlanType.BY_DIVISION.getStringValue().equals(type)) {
            return new OperationProductInGroupingContainerDecorator(operationMergeService, byDivision(workPlan, locale),
                    productionCountingService, parameterService);
        } else {
            LOG.warn("There is no grouping container defined for work plan type: " + type);
            LOG.warn("Returning noDistinctionGroupingContainer ...");
            return new OperationProductInGroupingContainerDecorator(operationMergeService, noDistinction(workPlan, locale),
                    productionCountingService, parameterService);
        }
    }

    private GroupingContainer noDistinction(Entity workPlan, Locale locale) {
        String titleAppend = translationService.translate("workPlans.workPlan.report.title.noDistinction", locale);
        return new NoDistinctionGroupingContainer(orderColumns(workPlan), operationProductsIn(workPlan),
                operationProductsOut(workPlan), titleAppend);
    }

    private Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationProductsOut(Entity workPlan) {
        return workPlanColumnService.operationProductsOut(workPlan);
    }

    private Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationProductsIn(Entity workPlan) {
        return workPlanColumnService.operationProductsIn(workPlan);
    }

    private Map<OrderColumn, ColumnAlignment> orderColumns(Entity workPlan) {
        return workPlanColumnService.getOrderColumns(workPlan);
    }

    private GroupingContainer byEndProduct(Entity workPlan, Locale locale) {
        String titleAppend = translationService.translate("workPlans.workPlan.report.title.byEndProduct", locale);
        return new EndProductGroupingContainer(orderColumns(workPlan), operationProductsIn(workPlan),
                operationProductsOut(workPlan), titleAppend);
    }

    private GroupingContainer byWorkstationType(Entity workPlan, Locale locale) {
        String titleAppend = translationService.translate("workPlans.workPlan.report.title.byWorkstationType", locale);
        String nullWorkstationTitle = translationService.translate("workPlans.workPlan.report.title.noWorkstationType", locale);
        return new WorkstationTypeGroupingContainer(orderColumns(workPlan), operationProductsIn(workPlan),
                operationProductsOut(workPlan), titleAppend, nullWorkstationTitle);
    }

    private GroupingContainer byDivision(Entity workPlan, Locale locale) {
        String titleAppend = translationService.translate("workPlans.workPlan.report.title.byDivision", locale);
        String nullDivision = translationService.translate("workPlans.workPlan.report.title.noDivision", locale);
        return new DivisionGroupingContainer(orderColumns(workPlan), operationProductsIn(workPlan),
                operationProductsOut(workPlan), titleAppend, nullDivision);
    }

}
