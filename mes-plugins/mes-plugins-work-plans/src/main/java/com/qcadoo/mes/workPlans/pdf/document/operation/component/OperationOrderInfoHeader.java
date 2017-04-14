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
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfHelper;

@Component
public class OperationOrderInfoHeader {

    private PdfHelper pdfHelper;

    private TranslationService translationService;

    @Autowired
    public OperationOrderInfoHeader(PdfHelper pdfHelper, TranslationService translationService) {
        this.pdfHelper = pdfHelper;
        this.translationService = translationService;
    }

    public void print(Entity order, PdfPTable operationTable, Locale locale) throws DocumentException {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        String technologyNumber = null;
        if (technology != null) {
            technologyNumber = technology.getStringField(TechnologyFields.NUMBER);
        }
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.technology", locale), technologyNumber);

        String orderName = order.getStringField(OrderFields.NAME);
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.orderName", locale), orderName);

        String orderNumber = order.getStringField(OrderFields.NUMBER);
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.orderNumber", locale), orderNumber);

        Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);
        String productionLineNumber = null;
        if (productionLine != null) {
            productionLineNumber = productionLine.getStringField(ProductionLineFields.NUMBER);
        }
        pdfHelper.addTableCellAsOneColumnTable(operationTable,
                translationService.translate("workPlans.workPlan.report.operation.productionLineNumber", locale), productionLineNumber);

        String orderCategory = order.getStringField(OrderFields.ORDER_CATEGORY);
        if(!Objects.isNull(orderCategory)){
            pdfHelper.addTableCellAsOneColumnTable(operationTable,
                    translationService.translate("workPlans.workPlan.report.operation.orderCategory", locale), orderCategory);
        }

        Entity company = order.getBelongsToField(OrderFields.COMPANY);
        String companyNumber = null;
        if (company != null) {
            companyNumber = company.getStringField(CompanyFields.NUMBER);
        }
        if(!Objects.isNull(companyNumber)) {
            pdfHelper.addTableCellAsOneColumnTable(operationTable,
                    translationService.translate("workPlans.workPlan.report.operation.companyNumber", locale), companyNumber);
        }

        String orderDescription = order.getStringField(OrderFields.DESCRIPTION);
        if(!Objects.isNull(orderDescription)) {
            pdfHelper.addTableCellAsOneColumnTable(operationTable,
                    translationService.translate("workPlans.workPlan.report.operation.orderDescription", locale), orderDescription);
        }
        operationTable.completeRow();
    }

}
