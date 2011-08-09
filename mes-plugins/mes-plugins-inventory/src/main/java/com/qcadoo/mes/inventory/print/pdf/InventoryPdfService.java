package com.qcadoo.mes.inventory.print.pdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.mes.inventory.constants.InventoryConstants;
import com.qcadoo.mes.inventory.print.utils.EntityInventoryComparator;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.security.api.SecurityService;

@Service
public final class InventoryPdfService extends PdfDocumentService {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    protected void buildPdfContent(final Document document, final Entity inventoryReport, final Locale locale)
            throws DocumentException {
        String documenTitle = getTranslationService().translate("inventory.inventory.report.title", locale);
        String documentAuthor = getTranslationService().translate("qcadooReport.commons.generatedBy.label", locale);
        PdfUtil.addDocumentHeader(document, "", documenTitle, documentAuthor, new Date(), securityService.getCurrentUserName());
        document.add(Chunk.NEWLINE);

        PdfPTable panelTable = PdfUtil.createPanelTable(2);
        PdfUtil.addTableCellAsTable(panelTable,
                getTranslationService().translate("inventory.inventory.report.panel.inventoryForDate", locale),
                inventoryReport.getStringField("inventoryForDate"), null, PdfUtil.getArialBold10Dark(),
                PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(panelTable,
                getTranslationService().translate("inventory.inventory.report.panel.generationDate", locale),
                inventoryReport.getStringField("generationDate"), null, PdfUtil.getArialBold10Dark(),
                PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(panelTable,
                getTranslationService().translate("inventory.inventory.report.panel.warehouse", locale), inventoryReport
                        .getBelongsToField("warehouse").getStringField("number"), null, PdfUtil.getArialBold10Dark(), PdfUtil
                        .getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(panelTable,
                getTranslationService().translate("inventory.inventory.report.panel.worker", locale), inventoryReport
                        .getBelongsToField("staff").getStringField("name")
                        + " "
                        + inventoryReport.getBelongsToField("staff").getStringField("surname"), null, PdfUtil
                        .getArialBold10Dark(), PdfUtil.getArialRegular10Dark());
        panelTable.setSpacingAfter(20);
        document.add(panelTable);

        List<String> tableHeader = new ArrayList<String>();
        tableHeader.add(getTranslationService().translate("inventory.inventory.report.columnHeader.number", locale));
        tableHeader.add(getTranslationService().translate("inventory.inventory.report.columnHeader.name", locale));
        tableHeader.add(getTranslationService().translate("inventory.inventory.report.columnHeader.quantity", locale));
        tableHeader.add(getTranslationService().translate("inventory.inventory.report.columnHeader.unit", locale));
        PdfPTable table = PdfUtil.createTableWithHeader(4, tableHeader, false);

        DataDefinition dataDefInventory = dataDefinitionService.get(InventoryConstants.PLUGIN_IDENTIFIER,
                InventoryConstants.MODEL_INVENTORY);
        List<Entity> inventories = dataDefInventory.find("where id = " + Long.toString(inventoryReport.getId())).list()
                .getEntities();
        Collections.sort(inventories, new EntityInventoryComparator());

        for (Entity e : inventories) {
            table.addCell(new Phrase(e.getBelongsToField("product").getStringField("numer"), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(e.getBelongsToField("product").getStringField("name"), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(e.getStringField("quantity"), PdfUtil.getArialRegular9Dark()));
            table.addCell(new Phrase(e.getBelongsToField("product").getStringField("unit"), PdfUtil.getArialRegular9Dark()));
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
