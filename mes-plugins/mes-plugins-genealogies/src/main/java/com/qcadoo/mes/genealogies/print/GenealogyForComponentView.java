package com.qcadoo.mes.genealogies.print;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;
import com.qcadoo.mes.api.SecurityService;
import com.qcadoo.mes.beans.users.UsersUser;
import com.qcadoo.mes.utils.pdf.PdfUtil;
import com.qcadoo.mes.utils.pdf.ReportPdfView;

public class GenealogyForComponentView extends ReportPdfView {

    @Autowired
    private SecurityService securityService;

    @Override
    protected String addContent(final Document document, final Object value, final Locale locale, final PdfWriter writer)
            throws DocumentException, IOException {
        String documentTitle = getTranslationService().translate("genealogies.genealogyForComponent.report.title", locale);
        String documentAuthor = getTranslationService().translate("genealogies.genealogyForComponent.report.author", locale);
        UsersUser user = securityService.getCurrentUser();
        PdfUtil.addDocumentHeader(document, "nazwa", documentTitle, documentAuthor, new Date(), user);
        String text = getTranslationService().translate("core.report.endOfReport", locale);
        PdfUtil.addEndOfDocument(document, writer, text);
        return getTranslationService().translate("genealogies.genealogyForComponent.report.fileName", locale);
    }

    @Override
    protected void addTitle(final Document document, final Locale locale) {
        document.addTitle(getTranslationService().translate("genealogies.genealogyForComponent.report.title", locale));
    }

}
