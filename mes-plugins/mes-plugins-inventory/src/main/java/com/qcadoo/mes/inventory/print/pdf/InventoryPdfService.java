package com.qcadoo.mes.inventory.print.pdf;

import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfUtil;
import com.qcadoo.security.api.SecurityService;

@Service
public final class InventoryPdfService extends PdfDocumentService {

    @Autowired
    private SecurityService securityService;

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
                ((Date) inventoryReport.getField("inventoryForDate")).toString(), null, PdfUtil.getArialBold10Dark(),
                PdfUtil.getArialRegular10Dark());
        PdfUtil.addTableCellAsTable(panelTable,
                getTranslationService().translate("inventory.inventory.report.panel.generationDate", locale),
                ((Date) inventoryReport.getField("generationDate")).toString(), null, PdfUtil.getArialBold10Dark(),
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
        document.add(panelTable);
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
