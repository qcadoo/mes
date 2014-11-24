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

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.workPlans.constants.ParameterFieldsWP;
import com.qcadoo.mes.workPlans.constants.TechnologyOperationComponentFieldsWP;
import com.qcadoo.mes.workPlans.pdf.document.operation.component.*;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.GroupingContainer;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class OperationOrderSection {

    private ParameterService parameterService;
    private OperationProductInTableHeader operationProductInTableHeader;
    private OperationProductOutTableHeader operationProductOutTableHeader;
    private OperationBarcode operationBarcode;
    private OperationProductOutTable operationProductOutTable;
    private OperationProductInTable operationProductInTable;
    private OperationCommentOperation operationComment;
    private OperationOrderHeader operationOrderHeader;
    private OperationAdditionalFields operationAdditionalFields;

    @Autowired
    public OperationOrderSection(ParameterService parameterService,
                                 OperationProductInTableHeader operationProductInTableHeader,
                                 OperationProductOutTableHeader operationProductOutTableHeader,
                                 OperationBarcode operationBarcode,
                                 OperationProductOutTable operationProductOutTable,
                                 OperationProductInTable operationProductInTable,
                                 OperationCommentOperation operationComment,
                                 OperationOrderHeader operationOrderHeader,
                                 OperationAdditionalFields operationAdditionalFields) {

        this.parameterService = parameterService;
        this.operationProductInTableHeader = operationProductInTableHeader;
        this.operationProductOutTableHeader = operationProductOutTableHeader;
        this.operationBarcode = operationBarcode;
        this.operationProductOutTable = operationProductOutTable;
        this.operationProductInTable = operationProductInTable;
        this.operationComment = operationComment;
        this.operationOrderHeader = operationOrderHeader;
        this.operationAdditionalFields = operationAdditionalFields;

    }

    public void print(PdfWriter pdfWriter, GroupingContainer groupingContainer, Entity order, Entity operationComponent, Document document, Locale locale) throws DocumentException {
        operationOrderHeader.print(order, groupingContainer, operationComponent, document, locale);

        if(isCommentEnabled(operationComponent)) {
            operationComment.print(operationComponent, document, locale);
        }

        if (isBarcodeEnabled()) {
            operationBarcode.print(pdfWriter, operationComponent, document);
        }

        if (isOutputProductTableEnabled(operationComponent)) {
            operationProductOutTableHeader.print(document, locale);
            operationProductOutTable.print(groupingContainer, operationComponent, document, locale);

        }

        if(isInputProductTableEnabled(operationComponent)){
            operationProductInTableHeader.print(document, locale);
            operationProductInTable.print(groupingContainer, operationComponent, document, locale);
        }

        operationAdditionalFields.print(operationComponent, document, locale);


    }

    private boolean isOutputProductTableEnabled(Entity operationComponent) {
        return !operationComponent.getBooleanField(TechnologyOperationComponentFieldsWP.DONT_PRINT_OUTPUT_PRODUCTS_IN_WORK_PLANS);
    }

    boolean isInputProductTableEnabled(final Entity operationComponent) {
        return !operationComponent.getBooleanField(TechnologyOperationComponentFieldsWP.DONT_PRINT_INPUT_PRODUCTS_IN_WORK_PLANS);
    }

    private boolean isBarcodeEnabled() {
        return !parameterService.getParameter().getBooleanField(ParameterFieldsWP.HIDE_BARCODE_OPERATION_COMPONENT_IN_WORK_PLAN);
    }

    private boolean isCommentEnabled(final Entity operationComponent) {
        return !operationComponent.getBooleanField(TechnologyOperationComponentFieldsWP.HIDE_DESCRIPTION_IN_WORK_PLANS);
    }
}
