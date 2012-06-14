/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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

import static com.qcadoo.mes.basic.constants.ProductFields.NAME;
import static com.qcadoo.mes.basic.constants.ProductFields.NUMBER;
import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static com.qcadoo.mes.materialFlow.constants.MaterialsInStockAreasFields.MATERIAL_FLOW_FOR_DATE;
import static com.qcadoo.mes.materialFlow.constants.MaterialsInStockAreasFields.STOCK_AREAS;
import static com.qcadoo.mes.materialFlow.constants.MaterialsInStockAreasFields.TIME;
import static com.qcadoo.mes.materialFlow.constants.MaterialsInStockAreasFields.WORKER;

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
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlow.MaterialFlowService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.report.api.FontUtils;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;

@Service
public final class MaterialFlowPdfService extends PdfDocumentService {

    @Autowired
    private MaterialFlowService materialFlowService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private PdfHelper pdfHelper;

    @Override
    protected void buildPdfContent(final Document document, final Entity materialsInStockAreas, final Locale locale)
            throws DocumentException {
        Map<Entity, BigDecimal> reportData = materialFlowService.calculateMaterialQuantitiesInStockArea(materialsInStockAreas);

        String documenTitle = translationService.translate("materialFlow.materialFlow.report.title", locale);
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);
        pdfHelper.addDocumentHeader(document, "", documenTitle, documentAuthor, (Date) materialsInStockAreas.getField(TIME),
                materialsInStockAreas.getStringField(WORKER));

        PdfPTable panelTable = pdfHelper.createPanelTable(2);
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("materialFlow.materialFlow.report.panel.materialFlowForDate", locale),
                ((Date) materialsInStockAreas.getField(MATERIAL_FLOW_FOR_DATE)).toString());
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("materialFlow.materialFlow.report.panel.time", locale),
                ((Date) materialsInStockAreas.getField(TIME)).toString());

        List<Entity> stockAreas = materialsInStockAreas.getHasManyField(STOCK_AREAS);
        List<String> names = new ArrayList<String>();
        for (Entity component : stockAreas) {
            Entity stockArea = (Entity) component.getField(STOCK_AREAS);
            names.add(stockArea.getField(NUMBER).toString());
        }
        pdfHelper.addTableCellAsOneColumnTable(panelTable,
                translationService.translate("materialFlow.materialFlow.report.panel.stockAreas", locale), names);
        pdfHelper.addTableCellAsOneColumnTable(panelTable, "", "");

        panelTable.setSpacingBefore(20);
        panelTable.setSpacingAfter(20);
        document.add(panelTable);

        List<String> tableHeader = new ArrayList<String>();
        tableHeader.add(translationService.translate("materialFlow.materialFlow.report.columnHeader.number", locale));
        tableHeader.add(translationService.translate("materialFlow.materialFlow.report.columnHeader.name", locale));
        tableHeader.add(translationService.translate("materialFlow.materialFlow.report.columnHeader.quantity", locale));
        tableHeader.add(translationService.translate("materialFlow.materialFlow.report.columnHeader.unit", locale));
        PdfPTable table = pdfHelper.createTableWithHeader(4, tableHeader, false);

        for (Map.Entry<Entity, BigDecimal> data : reportData.entrySet()) {
            table.addCell(new Phrase(data.getKey().getStringField(NUMBER), FontUtils.getDejavuRegular9Dark()));
            table.addCell(new Phrase(data.getKey().getStringField(NAME), FontUtils.getDejavuRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(new Phrase(numberService.format(data.getValue()), FontUtils.getDejavuRegular9Dark()));
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(new Phrase(data.getKey().getStringField(UNIT), FontUtils.getDejavuRegular9Dark()));
        }
        document.add(table);
    }

    @Override
    public String getReportTitle(final Locale locale) {
        return translationService.translate("materialFlow.materialFlow.report.title", locale);
    }
}
