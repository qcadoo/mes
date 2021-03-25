package com.qcadoo.mes.costCalculation.print;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.costCalculation.constants.ModelCardFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfDocumentService;
import com.qcadoo.report.api.pdf.PdfHelper;

@Service
public final class ModelCardPdfService extends PdfDocumentService {

    private static final int[] defaultModelCardProductColumnWidth = new int[] { 10, 20, 5, 10, 10 };

    @Autowired
    private TranslationService translationService;

    @Autowired
    private PdfHelper pdfHelper;

    @Override
    public String getReportTitle(Locale locale) {
        return translationService.translate("costCalculation.modelCard.report.title", locale);
    }

    @Override
    protected void buildPdfContent(Document document, Entity entity, Locale locale) throws DocumentException {
        String documentTitle = translationService.translate("costCalculation.modelCard.report.header", locale,
                entity.getStringField(ModelCardFields.NAME));
        String documentAuthor = translationService.translate("qcadooReport.commons.generatedBy.label", locale);

        pdfHelper.addDocumentHeader(document, StringUtils.EMPTY, documentTitle, documentAuthor,
                entity.getDateField(ModelCardFields.DATE));

        for (Entity product : entity.getHasManyField(ModelCardFields.MODEL_CARD_PRODUCTS)) {
            PdfPTable panelTable = pdfHelper.createPanelTable(5);
            panelTable.setWidths(defaultModelCardProductColumnWidth);

            document.add(panelTable);
        }
    }
}
