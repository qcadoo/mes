package com.qcadoo.mes.productionCounting.internal.print;

import java.util.Locale;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.qcadoo.model.api.Entity;
import com.qcadoo.report.api.pdf.PdfDocumentService;

@Service
public class ProductionCountingPdfService extends PdfDocumentService {

    @Override
    protected void buildPdfContent(final Document document, final Entity productionBalance, final Locale locale)
            throws DocumentException {
        // TODO production counting report content ANKI
    }

    @Override
    protected String getSuffix() {
        return "";
    }

    @Override
    protected String getReportTitle(final Locale locale) {
        return getTranslationService().translate("productionCounting.productionBalance.report.title", locale);
    }

}
