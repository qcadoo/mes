/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.1
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
package com.qcadoo.mes.materialFlow.print.pdf;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.mes.materialFlow.MaterialFlowService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfUtil;

@Service
public final class MaterialFlowPdfService extends PdfDocumentService {

    @Autowired
    private MaterialFlowService materialFlowService;

    @Override
    protected void buildPdfContent(final Document document, final Entity materialsInStockAreas, final Locale locale)
            throws DocumentException {
        Map<Entity, BigDecimal> reportData = materialFlowService.calculateMaterialQuantitiesInStockArea(materialsInStockAreas);

        String documenTitle = getTranslationService().translate("materialFlow.materialFlow.report.title", locale);
        String documentAuthor = getTranslationService().translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, "", documenTitle, documentAuthor, (Date) materialsInStockAreas.getField("time"),
                materialsInStockAreas.getStringField("worker"));

        PdfPTable panelTable = PdfUtil.createPanelTable(2);
        PdfUtil.addTableCellAsTable(panelTable,
                getTranslationService().translate("materialFlow.materialFlow.report.panel.materialFlowForDate", locale),
                ((Date) materialsInStockAreas.getField("materialFlowForDate")).toString(), null, PdfUtil.getArialBold10Dark(),
                PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(panelTable,
                getTranslationService().translate("materialFlow.materialFlow.report.panel.time", locale),
                ((Date) materialsInStockAreas.getField("time")).toString(), null, PdfUtil.getArialBold10Dark(),

                PdfUtil.getArialRegular10Dark());

        List<Entity> stockAreas = materialsInStockAreas.getHasManyField("stockAreas");
        List<String> names = new ArrayList<String>();
        for (Entity component : stockAreas) {
            Entity stockArea = (Entity) component.getField("stockAreas");
            names.add(stockArea.getField("number").toString());
        }
        PdfUtil.addTableCellAsTable(panelTable,
                getTranslationService().translate("materialFlow.materialFlow.report.panel.stockAreas", locale), names, null,
                PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(panelTable, "", "", null, PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());

        panelTable.setSpacingBefore(20);
        panelTable.setSpacingAfter(20);
        document.add(panelTable);

        List<String> tableHeader = new ArrayList<String>();
        tableHeader.add(getTranslationService().translate("materialFlow.materialFlow.report.columnHeader.number", locale));
        tableHeader.add(getTranslationService().translate("materialFlow.materialFlow.report.columnHeader.name", locale));
        tableHeader.add(getTranslationService().translate("materialFlow.materialFlow.report.columnHeader.quantity", locale));
        tableHeader.add(getTranslationService().translate("materialFlow.materialFlow.report.columnHeader.unit", locale));
        PdfPTable table = PdfUtil.createTableWithHeader(4, tableHeader, false);

        for (Map.Entry<Entity, BigDecimal> data : reportData.entrySet()) {
            table.addCell(new Phrase(data.getKey().getStringField("number"), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(data.getKey().getStringField("name"), PdfUtil.getArialRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(new Phrase(getDecimalFormat().format(data.getValue()), PdfUtil.getArialRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(new Phrase(data.getKey().getStringField("unit"), PdfUtil.getArialRegular9Dark()));
        }
        document.add(table);
    }

    @Override
    protected String getReportTitle(final Locale locale) {
        return getTranslationService().translate("materialFlow.materialFlow.report.title", locale);
    }

    @Override
    protected String getSuffix() {
        return "";
    }

}
