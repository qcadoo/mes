/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.products.print.pdf;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.products.print.ProductReportService;
import com.qcadoo.mes.products.print.pdf.util.PdfUtil;

@Service
public final class WorkPlanForMachinePdfService extends PdfDocumentService {

    @Autowired
    private ProductReportService productReportService;

    @Override
    protected void buildPdfContent(final Document document, final Entity entity, final Locale locale) throws DocumentException {
        addOperationSeries(document, (DefaultEntity) entity, locale);
    }

    @Override
    protected void buildPdfMetadata(final Document document, final Locale locale) {
        document.addTitle(getTranslationService().translate("products.workPlan.report.title", locale));
        PdfUtil.addMetaData(document);
    }

    private void addOperationSeries(final Document document, final DefaultEntity entity, final Locale locale)
            throws DocumentException {
        boolean firstPage = true;
        List<Entity> orders = entity.getHasManyField("orders");
        for (Entity component : orders) {
            Entity order = (Entity) component.getField("order");
            Entity technology = (Entity) order.getField("technology");
            if (technology != null) {
                List<Entity> operationComponents = technology.getHasManyField("operationComponents");
                for (Entity operationComponent : operationComponents) {
                    if (!firstPage) {
                        document.newPage();
                    }
                    productReportService.addOrderHeader(document, entity, locale, df);
                    Entity operation = (Entity) operationComponent.getField("operation");
                    Entity machine = (Entity) operation.getField("machine");
                    document.add(new Paragraph(getTranslationService().translate("products.workPlan.report.paragrah3", locale)
                            + " " + machine.getField("name"), PdfUtil.getArialBold11Dark()));
                    PdfPTable table = PdfUtil.createTableWithHeader(5, productReportService.addOperationHeader(locale));
                    table.addCell(new Phrase(operation.getField("number").toString(), PdfUtil.getArialRegular9Dark()));
                    table.addCell(new Phrase(operation.getField("name").toString(), PdfUtil.getArialRegular9Dark()));
                    table.addCell(new Phrase(order.getField("number").toString(), PdfUtil.getArialRegular9Dark()));
                    List<Entity> operationProductComponents = operationComponent.getHasManyField("operationProductComponents");
                    productReportService.addProductOutSeries(table, operationProductComponents);
                    productReportService.addProductInSeries(table, operationProductComponents);
                    document.add(table);
                    firstPage = false;
                }
            }

        }
    }

    @Override
    protected String getSuffix() {
        return "ForMachine";
    }
}
