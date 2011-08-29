/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.4.6
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
package com.qcadoo.mes.inventory.print.pdf;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.mes.inventory.InventoryService;
import com.qcadoo.mes.inventory.constants.InventoryConstants;
import com.qcadoo.mes.inventory.print.utils.EntityTransferComparator;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfUtil;

@Service
public final class InventoryPdfService extends PdfDocumentService {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    protected void buildPdfContent(final Document document, final Entity inventoryReport, final Locale locale)
            throws DocumentException {
        String documenTitle = getTranslationService().translate("inventory.inventory.report.title", locale);
        String documentAuthor = getTranslationService().translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, "", documenTitle, documentAuthor, (Date) inventoryReport.getField("date"),
                inventoryReport.getStringField("worker"));

        PdfPTable panelTable = PdfUtil.createPanelTable(2);
        PdfUtil.addTableCellAsTable(panelTable,
                getTranslationService().translate("inventory.inventory.report.panel.inventoryForDate", locale),
                ((Date) inventoryReport.getField("inventoryForDate")).toString(), null, PdfUtil.getArialBold10Dark(),
                PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(panelTable, getTranslationService()
                .translate("inventory.inventory.report.panel.date", locale),
                ((Date) inventoryReport.getField("date")).toString(), null, PdfUtil.getArialBold10Dark(),

                PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(panelTable,
                getTranslationService().translate("inventory.inventory.report.panel.warehouse", locale), inventoryReport
                        .getBelongsToField("warehouse").getStringField("number"), null, PdfUtil.getArialBold10Dark(), PdfUtil
                        .getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(panelTable,
                getTranslationService().translate("inventory.inventory.report.panel.worker", locale),
                inventoryReport.getStringField("worker"), null, PdfUtil.getArialBold10Dark(), PdfUtil.getArialRegular10Dark());

        panelTable.setSpacingBefore(20);
        panelTable.setSpacingAfter(20);
        document.add(panelTable);

        List<String> tableHeader = new ArrayList<String>();
        tableHeader.add(getTranslationService().translate("inventory.inventory.report.columnHeader.number", locale));
        tableHeader.add(getTranslationService().translate("inventory.inventory.report.columnHeader.name", locale));
        tableHeader.add(getTranslationService().translate("inventory.inventory.report.columnHeader.quantity", locale));
        tableHeader.add(getTranslationService().translate("inventory.inventory.report.columnHeader.unit", locale));
        PdfPTable table = PdfUtil.createTableWithHeader(4, tableHeader, false);

        DataDefinition dataDefTransfer = dataDefinitionService.get(InventoryConstants.PLUGIN_IDENTIFIER,
                InventoryConstants.MODEL_TRANSFER);
        List<Entity> transfers = dataDefTransfer
                .find("where warehouseTo.id = " + Long.toString(inventoryReport.getBelongsToField("warehouse").getId())).list()
                .getEntities();
        Collections.sort(transfers, new EntityTransferComparator());

        String warehouseNumber = inventoryReport.getBelongsToField("warehouse").getStringField("number");
        String forDate = ((Date) inventoryReport.getField("inventoryForDate")).toString();

        String numberBefore = "";
        for (Entity e : transfers) {
            String numberNow = e.getBelongsToField("product").getStringField("number");

            if (!numberBefore.equals(numberNow)) {
                BigDecimal quantity = inventoryService.calculateShouldBe(warehouseNumber.toString(),
                        e.getBelongsToField("product").getStringField("number"), forDate);

                table.addCell(new Phrase(e.getBelongsToField("product").getStringField("number"), PdfUtil.getArialRegular9Dark()));
                table.addCell(new Phrase(e.getBelongsToField("product").getStringField("name"), PdfUtil.getArialRegular9Dark()));
                table.addCell(new Phrase(quantity.toString(), PdfUtil.getArialRegular9Dark()));
                table.addCell(new Phrase(e.getBelongsToField("product").getStringField("unit"), PdfUtil.getArialRegular9Dark()));
                numberBefore = numberNow;
            }

        }
        document.add(table);
    }

    @Override
    protected String getReportTitle(final Locale locale) {
        return getTranslationService().translate("inventory.inventory.report.title", locale);
    }

    @Override
    protected String getSuffix() {
        return "";
    }

}
