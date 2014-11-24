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
package com.qcadoo.mes.workPlans.pdf.document.component;

import com.google.common.collect.ListMultimap;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.workPlans.constants.ParameterFieldsWP;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.GroupingContainer;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.holder.OrderOperationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class OperationSection {

    private ParameterService parameterService;
    private OperationSectionHeader operationSectionHeader;
    private OperationOrderSection operationOrderSection;

    @Autowired
    public OperationSection(ParameterService parameterService, OperationSectionHeader operationSectionHeader, OperationOrderSection operationOrderSection) {
        this.operationSectionHeader = operationSectionHeader;
        this.operationOrderSection = operationOrderSection;
        this.parameterService = parameterService;
    }

    public void print(PdfWriter pdfWriter, GroupingContainer groupingContainer, Document document, Locale locale) throws DocumentException {
        if (notPrintOperationAtFirstPage())
            document.newPage();

        ListMultimap<String, OrderOperationComponent> titleToOperationComponent = groupingContainer.getTitleToOperationComponent();
        for (String title : titleToOperationComponent.keySet()) {
            operationSectionHeader.print(document, title);
            int count = 0;
            for (OrderOperationComponent orderOperationComponent : groupingContainer.getTitleToOperationComponent().get(title)) {
                count++;
                operationOrderSection.print(pdfWriter, groupingContainer, orderOperationComponent.getOrder(), orderOperationComponent.getOperationComponent(), document, locale);
                if (count != titleToOperationComponent.get(title).size())
                    if (notPrintOperationAtFirstPage())
                        document.add(Chunk.NEXTPAGE);
            }
        }
    }

    private boolean notPrintOperationAtFirstPage() {
        return !parameterService.getParameter().getBooleanField(ParameterFieldsWP.PRINT_OPERATION_AT_FIRST_PAGE_IN_WORK_PLANS);
    }

}
