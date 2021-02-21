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
package com.qcadoo.mes.workPlans.pdf.document.operation.grouping.factory;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.workPlans.constants.WorkPlanFields;
import com.qcadoo.mes.workPlans.constants.WorkPlanType;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.DivisionGroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.EndProductGroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.GroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.NoDistinctionGroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.OperationProductInGroupingContainerDecorator;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.ProductionLineGroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.StaffGroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.WorkstationGroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.WorkstationTypeGroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.column.OperationProductColumn;
import com.qcadoo.mes.workPlans.pdf.document.order.WorkPlanColumnService;
import com.qcadoo.mes.workPlans.pdf.document.order.column.OrderColumn;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupingContainerFactoryImpl implements GroupingContainerFactory {

    protected static final Logger LOG = LoggerFactory.getLogger(GroupingContainerFactoryImpl.class);

    private TranslationService translationService;

    private WorkPlanColumnService workPlanColumnService;

    private ProductionCountingService productionCountingService;

    private ParameterService parameterService;

    private DataDefinitionService dataDefinitionService;

    @Autowired
    public GroupingContainerFactoryImpl(final DataDefinitionService dataDefinitionService, final TranslationService translationService,
            final WorkPlanColumnService workPlanColumnService,
            final ProductionCountingService productionCountingService, final ParameterService parameterService) {
        this.translationService = translationService;
        this.workPlanColumnService = workPlanColumnService;
        this.productionCountingService = productionCountingService;
        this.parameterService = parameterService;
        this.dataDefinitionService = dataDefinitionService;
    }

    public GroupingContainer create(final Entity workPlan, final Locale locale) {
        String type = workPlan.getStringField(WorkPlanFields.TYPE);
        if (WorkPlanType.NO_DISTINCTION.getStringValue().equals(type)) {
            return new OperationProductInGroupingContainerDecorator(dataDefinitionService, noDistinction(workPlan, locale),
                    productionCountingService, parameterService);
        } else if (WorkPlanType.BY_END_PRODUCT.getStringValue().equals(type)) {
            return new OperationProductInGroupingContainerDecorator(dataDefinitionService, byEndProduct(workPlan, locale),
                    productionCountingService, parameterService);
        } else if (WorkPlanType.BY_WORKSTATION_TYPE.getStringValue().equals(type)) {
            return new OperationProductInGroupingContainerDecorator(dataDefinitionService, byWorkstationType(workPlan, locale),
                    productionCountingService, parameterService);
        } else if (WorkPlanType.BY_DIVISION.getStringValue().equals(type)) {
            return new OperationProductInGroupingContainerDecorator(dataDefinitionService, byDivision(workPlan, locale),
                    productionCountingService, parameterService);
        } else if (WorkPlanType.BY_WORKSTATION.getStringValue().equals(type)) {
            return new OperationProductInGroupingContainerDecorator(dataDefinitionService, byWorkstation(workPlan, locale),
                    productionCountingService, parameterService);
        } else if (WorkPlanType.BY_STAFF.getStringValue().equals(type)) {
            return new OperationProductInGroupingContainerDecorator(dataDefinitionService, byStaff(workPlan, locale),
                    productionCountingService, parameterService);
        } else if (WorkPlanType.BY_PRODUCTION_LINE.getStringValue().equals(type)) {
            return new OperationProductInGroupingContainerDecorator(dataDefinitionService, byProductionLine(workPlan, locale),
                    productionCountingService, parameterService);
        } else {
            LOG.warn("There is no grouping container defined for work plan type: " + type);
            LOG.warn("Returning noDistinctionGroupingContainer ...");

            return new OperationProductInGroupingContainerDecorator(dataDefinitionService, noDistinction(workPlan, locale),
                    productionCountingService, parameterService);
        }
    }

    private GroupingContainer noDistinction(final Entity workPlan, final Locale locale) {
        String titleAppend = translationService.translate("workPlans.workPlan.report.title.noDistinction", locale);

        return new NoDistinctionGroupingContainer(orderColumns(workPlan), operationProductsIn(workPlan),
                operationProductsOut(workPlan), titleAppend);
    }

    private Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationProductsOut(final Entity workPlan) {
        return workPlanColumnService.getOperationProductOutputColumns(workPlan);
    }

    private Map<Long, Map<OperationProductColumn, ColumnAlignment>> operationProductsIn(final Entity workPlan) {
        return workPlanColumnService.getOperationProductInputColumns(workPlan);
    }

    private Map<OrderColumn, ColumnAlignment> orderColumns(final Entity workPlan) {
        return workPlanColumnService.getOrderColumns(workPlan);
    }

    private GroupingContainer byEndProduct(final Entity workPlan, final Locale locale) {
        String titleAppend = translationService.translate("workPlans.workPlan.report.title.byEndProduct", locale);

        return new EndProductGroupingContainer(orderColumns(workPlan), operationProductsIn(workPlan),
                operationProductsOut(workPlan), titleAppend);
    }

    private GroupingContainer byWorkstationType(final Entity workPlan, final Locale locale) {
        String titleAppend = translationService.translate("workPlans.workPlan.report.title.byWorkstationType", locale);
        String nullWorkstationTitle = translationService.translate("workPlans.workPlan.report.title.noWorkstationType", locale);

        return new WorkstationTypeGroupingContainer(orderColumns(workPlan), operationProductsIn(workPlan),
                operationProductsOut(workPlan), titleAppend, nullWorkstationTitle);
    }

    private GroupingContainer byDivision(final Entity workPlan, final Locale locale) {
        String titleAppend = translationService.translate("workPlans.workPlan.report.title.byDivision", locale);
        String nullDivision = translationService.translate("workPlans.workPlan.report.title.noDivision", locale);

        return new DivisionGroupingContainer(orderColumns(workPlan), operationProductsIn(workPlan),
                operationProductsOut(workPlan), titleAppend, nullDivision);
    }

    private GroupingContainer byWorkstation(final Entity workPlan, final Locale locale) {
        String titleAppend = translationService.translate("workPlans.workPlan.report.title.byWorkstation", locale);
        String nullWorkstation = translationService.translate("workPlans.workPlan.report.title.noWorkstation", locale);

        return new WorkstationGroupingContainer(orderColumns(workPlan), operationProductsIn(workPlan),
                operationProductsOut(workPlan), titleAppend, nullWorkstation);
    }

    private GroupingContainer byStaff(final Entity workPlan, final Locale locale) {
        String titleAppend = translationService.translate("workPlans.workPlan.report.title.byStaff", locale);
        String nullStaff = translationService.translate("workPlans.workPlan.report.title.noStaff", locale);

        return new StaffGroupingContainer(orderColumns(workPlan), operationProductsIn(workPlan), operationProductsOut(workPlan),
                titleAppend, nullStaff);
    }

    private GroupingContainer byProductionLine(final Entity workPlan, final Locale locale) {
        String titleAppend = translationService.translate("workPlans.workPlan.report.title.byProductionLine", locale);
        String nullProductionLine = translationService.translate("workPlans.workPlan.report.title.noProductionLine", locale);

        return new ProductionLineGroupingContainer(orderColumns(workPlan), operationProductsIn(workPlan),
                operationProductsOut(workPlan), titleAppend, nullProductionLine);
    }

}
