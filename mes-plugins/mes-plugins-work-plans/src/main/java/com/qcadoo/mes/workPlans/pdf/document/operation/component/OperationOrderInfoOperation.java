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
package com.qcadoo.mes.workPlans.pdf.document.operation.component;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class OperationOrderInfoOperation {

    private PdfHelper pdfHelper;
    private TranslationService translationService;

    @Autowired
    public OperationOrderInfoOperation(PdfHelper pdfHelper, TranslationService translationService) {
        this.pdfHelper = pdfHelper;
        this.translationService = translationService;
    }

    public void print(Entity operationComponent, PdfPTable operationTable, Locale locale) throws DocumentException {
        String operationLevel = operationComponent.getStringField(TechnologyOperationComponentFields.NODE_NUMBER);
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.level", locale), operationLevel);

        String operationName = operationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION).getStringField(
                OperationFields.NAME);
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.name", locale), operationName);

        String operationNumber = operationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION)
                .getStringField(OperationFields.NUMBER);
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.number", locale), operationNumber);
    }
}
