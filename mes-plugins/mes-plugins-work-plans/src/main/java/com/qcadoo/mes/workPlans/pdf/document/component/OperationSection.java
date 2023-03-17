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
package com.qcadoo.mes.workPlans.pdf.document.component;

import com.google.common.collect.ListMultimap;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.workPlans.constants.ParameterFieldsWP;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.GroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.holder.OrderOperationComponent;
import com.qcadoo.model.api.Entity;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Comparator.nullsFirst;
import static java.util.Comparator.reverseOrder;

@Component
public class OperationSection {

    private ParameterService parameterService;

    private OperationSectionHeader operationSectionHeader;

    private OperationOrderSection operationOrderSection;

    @Autowired
    public OperationSection(final ParameterService parameterService, final OperationSectionHeader operationSectionHeader,
            final OperationOrderSection operationOrderSection) {
        this.operationSectionHeader = operationSectionHeader;
        this.operationOrderSection = operationOrderSection;
        this.parameterService = parameterService;
    }

    public void print(final Entity workPlan, final PdfWriter pdfWriter, final GroupingContainer groupingContainer,
            final Document document, final Locale locale) throws DocumentException {
        if (notPrintOperationAtFirstPage()) {
            document.newPage();
        }

        ListMultimap<String, OrderOperationComponent> titleToOperationComponent = groupingContainer
                .getTitleToOperationComponent();

        for (String title : titleToOperationComponent.keySet()) {
            operationSectionHeader.print(document, title);
            List<OrderOperationComponent> orderOperationComponents = groupingContainer.getTitleToOperationComponent().get(title);

            orderOperationComponents.sort(Comparator.comparing(o -> o.getOperationComponent().getStringField(TechnologyOperationComponentFields.NODE_NUMBER), nullsFirst(reverseOrder())));

            for (OrderOperationComponent orderOperationComponent : orderOperationComponents) {
                operationOrderSection.print(workPlan, pdfWriter, groupingContainer, orderOperationComponent, document, locale);
                if (generateEachOnSeparatePage()) {
                    document.newPage();
                }
            }
        }
    }

    private boolean notPrintOperationAtFirstPage() {
        return !parameterService.getParameter().getBooleanField(ParameterFieldsWP.PRINT_OPERATION_AT_FIRST_PAGE_IN_WORK_PLANS);
    }

    private boolean generateEachOnSeparatePage() {
        return parameterService.getParameter().getBooleanField(ParameterFieldsWP.GENERATE_EACH_ON_SEPARATE_PAGE);
    }

}
