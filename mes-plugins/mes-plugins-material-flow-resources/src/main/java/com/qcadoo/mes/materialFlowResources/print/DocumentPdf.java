package com.qcadoo.mes.materialFlowResources.print;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlowResources.print.helper.DocumentPdfHelper;
import com.qcadoo.mes.materialFlowResources.print.helper.DocumentPdfHelper.HeaderPair;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.report.api.pdf.ReportPdfView;

@Component(value = "documentPdf")
public class DocumentPdf extends ReportPdfView {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private DocumentPdfHelper documentPdfHelper;

    @Override
    protected String addContent(Document document, Map<String, Object> model, Locale locale, PdfWriter writer)
            throws DocumentException, IOException {
        Long id = Long.valueOf(model.get("id").toString());
        Entity documentEntity = documentPdfHelper.getDocumentEntity(id);
        String documentHeader = documentPdfHelper.getDocumentHeader(documentEntity, locale);
        pdfHelper.addDocumentHeader(document, "", documentHeader, "", new Date());
        addHeaderTable(document, documentEntity, locale);
        return documentPdfHelper.getFileName(documentEntity, locale);
    }

    @Override
    protected void addTitle(Document document, Locale locale) {
        document.addTitle(translationService.translate("materialFlowResources.report.title", locale));
    }

    private void addHeaderTable(Document document, Entity documentEntity, Locale locale) throws DocumentException {
        PdfPTable table = pdfHelper.createPanelTable(3);

        List<HeaderPair> headerValues = documentPdfHelper.getDocumentHeaderTableContent(documentEntity, locale);
        for (HeaderPair pair : headerValues) {
            if (pair.getValue() != null && !pair.getValue().isEmpty()) {
                pdfHelper.addTableCellAsOneColumnTable(table, pair.getLabel(), pair.getValue());
            } else {
                pdfHelper.addTableCellAsOneColumnTable(table, StringUtils.EMPTY, StringUtils.EMPTY);
            }
        }
        HeaderPair description = documentPdfHelper.getDescription(documentEntity, locale);
        pdfHelper.addTableCellAsTwoColumnsTable(table, description.getLabel(), description.getValue());

        table.completeRow();
        table.setSpacingAfter(20);

        document.add(table);

    }
}
