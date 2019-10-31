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
package com.qcadoo.mes.workPlans.pdf.document.operation.component;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.workPlans.constants.ParameterFieldsWP;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfHelper;

@Component
public class OperationOrderHeader {

    private ParameterService parameterService;

    private PdfHelper pdfHelper;

    private OperationOrderInfoOperation operationOrderInfoOperation;

    private OperationOrderInfoHeader operationOrderInfoHeader;

    private OperationOrderInfoOperationalTask operationOrderInfoOperationalTask;

    private OperationOrderInfoWorkstation operationOrderInfoWorkstation;

    @Autowired
    public OperationOrderHeader(final ParameterService parameterService, final PdfHelper pdfHelper,
            final OperationOrderInfoOperation operationOrderInfoOperation,
            final OperationOrderInfoHeader operationOrderInfoHeader,
            final OperationOrderInfoOperationalTask operationOrderInfoOperationalTask,
            final OperationOrderInfoWorkstation operationOrderInfoWorkstation) {
        this.parameterService = parameterService;
        this.pdfHelper = pdfHelper;
        this.operationOrderInfoOperation = operationOrderInfoOperation;
        this.operationOrderInfoHeader = operationOrderInfoHeader;
        this.operationOrderInfoOperationalTask = operationOrderInfoOperationalTask;
        this.operationOrderInfoWorkstation = operationOrderInfoWorkstation;
    }

    public void print(final Entity order, final Entity operationComponent, final Document document, final Locale locale)
            throws DocumentException {
        PdfPTable operationTable = pdfHelper.createPanelTable(3);

        operationOrderInfoOperation.print(operationComponent, operationTable, locale);

        if (isOrderInfoEnabled()) {
            operationOrderInfoHeader.print(order, operationTable, locale);
        }

        operationOrderInfoOperationalTask.print(order, operationComponent, operationTable, locale);
        operationOrderInfoWorkstation.print(operationComponent, operationTable, locale);

        operationTable.setSpacingAfter(18);
        operationTable.setSpacingBefore(9);

        document.add(operationTable);
    }

    private boolean isOrderInfoEnabled() {
        return !parameterService.getParameter().getBooleanField(ParameterFieldsWP.HIDE_TECHNOLOGY_AND_ORDER_IN_WORK_PLANS);
    }

}
